package io.tiler;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine;
import com.jetdrone.vertx.yoke.middleware.*;
import io.tiler.internal.RedisException;
import io.tiler.json.JsonArrayIterable;
import io.tiler.internal.queries.AggregateField;
import io.tiler.internal.queries.FromClause;
import io.tiler.internal.queries.InvalidQueryException;
import io.tiler.internal.queries.Query;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.InvalidExpressionException;
import io.vertx.java.redis.RedisClient;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.Series;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.platform.Verticle;

import java.util.*;

public class ServerVerticle extends Verticle {
  private static final String METRIC_NAMES_REDIS_KEY = "metricNames";
  private static final String METRIC_REDIS_KEY_PREFIX = "metrics.";
  private JsonObject config;
  private org.vertx.java.core.logging.Logger logger;
  private EventBus eventBus;
  private RedisClient redis;
  private final HashMap<SockJSSocket, SocketState> socketStates = new HashMap<>();

  public void start(Future<Void> startFuture) {
    config = container.config();
    logger = container.logger();
    eventBus = vertx.eventBus();
    redis = new RedisClient(eventBus, getRedisAddress());

    new Series<Void>()
      .task(handler -> container.deployModule("io.vertx~mod-redis~1.1.4", config.getObject("redis"), 1, result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result.cause()));
          return;
        }

        handler.handle(DefaultAsyncResult.succeed(null));
      }))
      .task(handler -> {
        HttpServer httpServer = vertx.createHttpServer();

        Yoke yoke = new Yoke(vertx);
        yoke.engine(new StringPlaceholderEngine("views"));
        yoke.use(new Logger());
        yoke.use(new ErrorHandler(true));
        yoke.use(new Favicon());
        yoke.use("/static", new Static("static"));
        yoke.use(new BodyParser());
        yoke.use(new Router()
          .get("/", (request, next) -> {
            request.response().redirect("/dashboards/sample");
          })
          .get("/dashboards/:dashboardName", (request, next) -> {
            String dashboardName = request.getParameter("dashboardName");
            logger.info("Serving dashboard '" + dashboardName + "'");
            request.put("dashboardName", dashboardName);
            request.response().setContentType("text/html", "utf-8")
              .render("dashboard.shtml", next);
          })
          .post("/api/metrics", (request, next) -> {
            Object body = request.body();
            YokeResponse response = request.response();

            if (!(body instanceof JsonObject)) {
              sendClientError(response, "Request body needs to be a JSON object");
              return;
            }

            JsonObject jsonBody = (JsonObject) body;

            // TODO: Move this logic into a method for validating metrics JSON
            // TODO: Reuse the method for metrics that arrive via the ESB
            if (!jsonBody.containsField("metrics")) {
              sendClientError(response, "Request body needs to contain a 'metrics' field");
              return;
            }

            Object metrics = jsonBody.getValue("metrics");

            if (!(metrics instanceof JsonArray)) {
              sendClientError(response, "'metrics' field in request body must be an array");
              return;
            }

            JsonArray jsonMetrics = (JsonArray) metrics;

            // TODO: Validate name and points

            saveAndPublishMetrics(jsonMetrics, result -> {
              if (result.failed()) {
                logger.info("Metrics could not be saved or published", result.cause());
                return;
              }

              logger.info("Metrics saved to Redis and published");

              // TODO: Send a different status code when not successful?
              response.setStatusCode(204).end();
            });
          }));
        yoke.listen(httpServer);

        SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);

        // TODO: Set the library_url value
        JsonObject sockJSConfig = new JsonObject().putString("prefix", "/events");

        sockJSServer.installApp(sockJSConfig, socket -> {
          socket.dataHandler(buffer -> {
            JsonObject message = new JsonObject(buffer.toString());

            if ("subscribe".equals(message.getString("type"))) {
              JsonObject payload = message.getObject("payload");
              JsonObject queries = payload.getObject("queries");

              SocketState socketState = createStateForSocket(queries);
              socketStates.put(socket, socketState);

              // TODO: Use timestamp on metrics to discard old metrics.  Where should this be done?  Client side or server side?

              getMetricsForQueries(socketState.queries().values(), metrics -> {
                if (metrics.failed()) {
                  logger.info("Metrics could not be retrieved", metrics.cause());
                  return;
                }

                publishMetrics(metrics.result(), result -> {
                  if (result.failed()) {
                    logger.info("Metrics could not be published", result.cause());
                    return;
                  }

                  logger.info("Metrics retrieved from Redis and published");
                });
              });
            }
          });

          socket.endHandler(aVoid -> {
            logger.info("Removing listener");
            socketStates.remove(socket);
          });
        });

        httpServer.listen(8080, result -> {
          if (result.failed()) {
            handler.handle(DefaultAsyncResult.fail(result.cause()));
            return;
          }

          handler.handle(DefaultAsyncResult.succeed(null));
        });
      })
      .task(handler -> {
        vertx.eventBus().registerHandler("io.tiler", (Message<JsonObject> message) -> {
          String messageType = message.body().getString("type");
          logger.info("Received " + messageType + " message");
          JsonObject messageBody = message.body().getObject("body");

          switch (messageType) {
            case "publishMetrics": {
              JsonArray metrics = messageBody.getArray("metrics");

              saveAndPublishMetrics(metrics, aVoid -> {
                logger.info("Metrics saved to Redis and published");
              });

              return;
            }
            case "getMetrics": {
              JsonArray metricNames = messageBody.getArray("metricNames");

              getMetrics(metricNames.toList(), result -> {
                if (result.failed()) {
                  logger.error("Failed to retrieve metrics", result.cause());
                  JsonObject replyMessage = new JsonObject()
                    .putObject("body", new JsonObject()
                      .putObject("error", new JsonObject()
                        .putString("message", "Failed to retrieve metrics")));

                  message.reply(replyMessage);
                  return;
                }

                JsonObject replyMessage = new JsonObject()
                  .putObject("body", new JsonObject()
                    .putArray("metrics", result.result()));

                message.reply(replyMessage);
                logger.info("Replied with metrics");
              });

              return;
            }
            default: {
              logger.error("Unrecognised message type '" + messageType + "'");
              return;
            }
          }
        });
        handler.handle(DefaultAsyncResult.succeed(null));
      })
      .task(handler -> {
        container.logger().info("ServerVerticle started");
        handler.handle(DefaultAsyncResult.succeed(null));
      })
      .run(handler -> {
        if (handler.failed()) {
          startFuture.setFailure(handler.cause());
          return;
        }

        startFuture.setResult(null);
      });
  }

  private String getRedisAddress() {
    JsonObject redisConfig = config.getObject("redis");

    if (redisConfig != null && redisConfig.containsField("address")) {
      return config.getObject("redis").getString("address");
    }

    return "io.tiler.redis";
  }

  private SocketState createStateForSocket(JsonObject queries) {
    SocketState socketState = new SocketState();

    HashMap<String, Query> socketStateQueries = socketState.queries();

    for (String key : queries.getFieldNames()) {
      Query query = null;

      try {
        query = Query.fromJsonObject(queries.getObject(key));
      } catch (InvalidQueryException e) {
        logger.error("Received invalid query from socket", e);
      }

      if (query != null) {
        socketStateQueries.put(key, query);
      }
    }
    return socketState;
  }

  private void sendClientError(YokeResponse response, String message) {
    JsonObject body = new JsonObject()
      .putObject("error", new JsonObject()
        .putString("message", message));
    response.setStatusCode(400).end(body);
  }

  private void saveAndPublishMetrics(JsonArray metrics, AsyncResultHandler<Void> handler) {
    saveMetrics(metrics, result -> {
      if (result.failed()) {
        handler.handle(result);
        return;
      }

      checkForMissingMetrics(getQueriesThatMatchMetrics(metrics), metrics, missingMetricNames -> {
        if (missingMetricNames.failed()) {
          handler.handle(new DefaultFutureResult(missingMetricNames.cause()));
        }

        getMetrics(missingMetricNames.result(), missingMetrics -> {
          if (missingMetrics.failed()) {
            handler.handle(new DefaultFutureResult(missingMetrics.cause()));
            return;
          }

          JsonArray combinedMetrics = metrics.copy();

          for (JsonObject metric : new JsonArrayIterable<JsonObject>(missingMetrics.result())) {
            combinedMetrics.addObject(metric);
          }

          publishMetrics(combinedMetrics, handler);
        });
      });
    });
  }

  private void saveMetrics(JsonArray metrics, AsyncResultHandler<Void> handler) {
    logger.info("Saving metrics to Redis");
    ArrayList<Object> saddArgs = new ArrayList<>();
    saddArgs.add(METRIC_NAMES_REDIS_KEY);

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      saddArgs.add(metric.getString("name"));
    }

    saddArgs.add((Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(new DefaultFutureResult(new RedisException(body)));
        return;
      }

      saveMetric(metrics, 0, handler);
    });

    redis.sadd(saddArgs.toArray());
  }

  private void saveMetric(JsonArray metrics, int metricIndex, AsyncResultHandler<Void> handler) {
    if (metricIndex >= metrics.size()) {
      handler.handle(new DefaultFutureResult());
      return;
    }

    JsonObject metric = metrics.get(metricIndex);

    String metricName = metric.getString("name");
    logger.info("Saving metric '" + metricName + "'");
    redis.set(METRIC_REDIS_KEY_PREFIX + metricName, metric.toString(), (Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(new DefaultFutureResult(new RedisException(body)));
        return;
      }

      saveMetric(metrics, metricIndex + 1, handler);
    });
  }

  private void publishMetrics(JsonArray metrics, AsyncResultHandler<Void> handler) {
    for (Map.Entry<SockJSSocket, SocketState> socketAndSocketState : socketStates.entrySet()) {
      SockJSSocket socket = socketAndSocketState.getKey();
      SocketState socketState = socketAndSocketState.getValue();

      for (Map.Entry<String, Query> queryEntry : socketState.queries().entrySet()) {
        String queryKey = queryEntry.getKey();
        Query query = queryEntry.getValue();

        JsonArray matchingMetrics = query.fromClause().findMatchingMetrics(metrics);

        if (matchingMetrics.size() > 0) {
          StringBuilder logMessageBuilder = new StringBuilder();
          logMessageBuilder.append("Query '")
            .append(queryKey)
            .append("' matches metrics ");

          String separator = "";

          for (JsonObject metric : new JsonArrayIterable<JsonObject>(matchingMetrics)) {
            logMessageBuilder.append(metric.getString("name"));
            logMessageBuilder.append(separator);
            separator = ", ";
          }

          JsonArray transformedMetrics = null;

          try {
            transformedMetrics = applyQueryToMetrics(query, matchingMetrics);
          } catch (InvalidExpressionException e) {
            logger.error("Invalid query expression", e);
          }

          if (transformedMetrics != null) {
            JsonObject payload = new JsonObject();
            payload.putString("key", queryKey);
            payload.putArray("metrics", transformedMetrics);
            JsonObject newMessage = new JsonObject();
            newMessage.putString("type", "notify");
            newMessage.putObject("payload", payload);

            logger.info("Sending SockJS message " + newMessage);

            Buffer newMessageBuffer = new Buffer(newMessage.encode());
            socket.write(newMessageBuffer);
          }
        } else {
          logger.info("Query '" + queryKey + "' does not match metrics");
        }
      }
    }

    handler.handle(new DefaultFutureResult());
  }

  private void checkForMissingMetrics(Collection<Query> queries, JsonArray metrics, AsyncResultHandler<Collection<String>> handler) {
    HashSet<String> availableMetricNames = new HashSet<>();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      availableMetricNames.add(metric.getString("name"));
    }

    if (!anyQueryIsPotentiallyMissingAMetric(queries, availableMetricNames)) {
      handler.handle(new DefaultFutureResult(new ArrayList<>()));
      return;
    }

    getMetricNames(metricNames -> {
      if (metricNames.failed()) {
        handler.handle(metricNames);
        return;
      }

      ArrayList<String> missingMetricNames = new ArrayList<>();

      for (String metricName : metricNames.result()) {
        if (!availableMetricNames.contains(metricName)) {
          if (metricNameMatchesAnyQuery(metricName, queries)) {
            missingMetricNames.add(metricName);
          }
        }
      }

      handler.handle(new DefaultFutureResult(missingMetricNames));
    });
  }

  private void getMetricsForQueries(Collection<Query> queries, AsyncResultHandler<JsonArray> handler) {
    getMetricNames(metricNames -> {
      if (metricNames.failed()) {
        handler.handle(new DefaultFutureResult(metricNames.cause()));
        return;
      }

      ArrayList<String> matchingMetricNames = new ArrayList<>();

      for (String metricName : metricNames.result()) {
        for (Query query : queries) {
          if (query.fromClause().matchesMetricName(metricName)) {
            matchingMetricNames.add(metricName);
          }
        }
      }

      getMetrics(matchingMetricNames, handler);
    });
  }

  private void getMetricNames(AsyncResultHandler<Collection<String>> handler) {
    redis.smembers("metricNames", (Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      logger.info("Received Redis values " + body);
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(new DefaultFutureResult(new RedisException(body)));
        return;
      }

      ArrayList<String> metricNames = new ArrayList<>();

      for (String redisValue : new JsonArrayIterable<String>(body.getArray("value"))) {
        metricNames.add(redisValue);
      }

      handler.handle(new DefaultFutureResult(metricNames));
    });
  }

  private void getMetrics(Collection<String> metricNames, AsyncResultHandler<JsonArray> handler) {
    if (metricNames.size() == 0) {
      handler.handle(new DefaultFutureResult(new JsonArray()));
      return;
    }

    ArrayList<Object> mgetArgs = new ArrayList<>();

    for (String metricName : metricNames) {
      mgetArgs.add(METRIC_REDIS_KEY_PREFIX + metricName);
    }

    mgetArgs.add((Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      logger.info("Received Redis values " + body);
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(new DefaultFutureResult(new RedisException(body)));
        return;
      }

      JsonArray metrics = new JsonArray();

      for (String redisValue : new JsonArrayIterable<String>(body.getArray("value"))) {
        if (redisValue != null) {
          metrics.addObject(new JsonObject(redisValue));
        }
      }

      handler.handle(new DefaultFutureResult(metrics));
    });

    redis.mget(mgetArgs.toArray());
  }

  private boolean metricNameMatchesAnyQuery(String metricName, Collection<Query> queries) {
    for (Query query : queries) {
      if (query.fromClause().matchesMetricName(metricName)) {
        return true;
      }
    }

    return false;
  }

  private List<Query> getQueriesThatMatchMetrics(JsonArray metrics) {
    ArrayList<Query> matchingQueries = new ArrayList<>();

    for (SocketState socketState : socketStates.values()) {
      for (Query query : socketState.queries().values()) {
        FromClause fromClause = query.fromClause();

        for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
          if (fromClause.matchesMetricName(metric.getString("name"))) {
            matchingQueries.add(query);
            break;
          }
        }
      }
    }

    return matchingQueries;
  }

  private boolean anyQueryIsPotentiallyMissingAMetric(Collection<Query> queries, Set<String> metricNames) {
    for (Query query : queries) {
      if (query.fromClause().isPotentiallyMissingAnyMetrics(metricNames)) {
        return true;
      }
    }

    return false;
  }

  private JsonArray applyQueryToMetrics(Query query, JsonArray metrics) throws InvalidExpressionException {
    JsonArray transformedMetrics = copyMetrics(metrics);
    applyWhereClauseToMetrics(query, transformedMetrics);
    transformedMetrics = applyGroupClauseToMetrics(query, transformedMetrics);
    applyAggregateClauseToMetrics(query, transformedMetrics);
    applyPointClauseToMetrics(query, transformedMetrics);
    transformedMetrics = applyMetricClauseToMetrics(query, transformedMetrics);

    return transformedMetrics;
  }

  private JsonArray copyMetrics(JsonArray metrics) {
    return metrics.copy();
  }

  private void applyWhereClauseToMetrics(Query query, JsonArray metrics) throws InvalidExpressionException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyWhereClauseToMetric(query, metric);
    }
  }

  private void applyWhereClauseToMetric(Query query, JsonObject metric) throws InvalidExpressionException {
    Map<String, Expression> whereClause = query.whereClause();
    JsonArray matchingPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      if (pointMatchesWhereClause(point, whereClause)) {
        matchingPoints.addObject(point);
      }
    }

    metric.putArray("points", matchingPoints);
  }

  private JsonArray applyGroupClauseToMetrics(Query query, JsonArray metrics) {
    if (!query.hasGroupClause()) {
      return metrics;
    }

    JsonArray groups = applyGroupClauseToPoints(query.groupClause(), metrics);
    JsonArray transformedMetrics = new JsonArray();

    for (JsonObject group : new JsonArrayIterable<JsonObject>(groups)) {
      transformedMetrics.addObject(new JsonObject()
        .mergeIn(group)
        .putArray("points", group.getArray("points")));
    }

    return transformedMetrics;
  }

  private JsonArray applyGroupClauseToPoints(JsonArray groupClause, JsonArray metrics) {
    HashMap<ArrayList<Object>, JsonObject> groups = new HashMap<>();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
        ArrayList<Object> groupKey = new ArrayList<>();

        for (String groupFieldName : new JsonArrayIterable<String>(groupClause)) {
          groupKey.add(groupFieldName);
          groupKey.add(point.getValue(groupFieldName));
        }

        JsonObject group = groups.get(groupKey);
        JsonArray groupPoints;

        if (group == null) {
          groupPoints = new JsonArray();
          group = new JsonObject();

          for (String groupFieldName : new JsonArrayIterable<String>(groupClause)) {
            group.putValue(groupFieldName, point.getValue(groupFieldName));
          }

          group.putArray("points", groupPoints);
          groups.put(groupKey, group);
        } else {
          groupPoints = group.getArray("points");
        }

        groupPoints.addObject(point);
      }
    }

    return convertCollectionToJsonArray(groups.values());
  }

  private void applyAggregateClauseToMetrics(Query query, JsonArray metrics) throws InvalidExpressionException {
    if (!query.hasAggregateClause()) {
      return;
    }

    Map<String, Expression> aggregateClause = query.aggregateClause();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      JsonArray transformedPoints = applyAggregateClauseToPoints(aggregateClause, metric.getArray("points"));
      metric.putArray("points", transformedPoints);
    }
  }

  private JsonArray applyAggregateClauseToPoints(Map<String, Expression> aggregateClause, JsonArray points) throws InvalidExpressionException {
    // TODO: Maybe combine this method with the equivalent for the group clause
    Set<Map.Entry<String, Expression>> aggregateClauseEntries = aggregateClause.entrySet();
    Set<String> aggregateClauseFieldNames = aggregateClause.keySet();
    HashMap<ArrayList<Object>, JsonObject> aggregatePoints = new HashMap<>();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(points)) {
      ArrayList<Object> aggregateKey = new ArrayList<>();

      for (Map.Entry<String, Expression> aggregateClauseEntry : aggregateClauseEntries) {
        String fieldName = aggregateClauseEntry.getKey();
        Expression expression = aggregateClauseEntry.getValue();
        Object aggregateValue = expression.evaluate(point.getValue(fieldName));

        aggregateKey.add(fieldName);
        aggregateKey.add(aggregateValue);
      }

      JsonObject aggregatePoint = aggregatePoints.get(aggregateKey);

      if (aggregatePoint == null) {
        aggregatePoint = new JsonObject();

        for (int index = 0, count = aggregateKey.size(); index < count; index += 2) {
          aggregatePoint.putValue((String) aggregateKey.get(index), aggregateKey.get(index + 1));
        }

        aggregatePoints.put(aggregateKey, aggregatePoint);
      }

      for (String pointFieldName : point.getFieldNames()) {
        if (!aggregateClauseFieldNames.contains(pointFieldName)) {
          JsonArray aggregatePointFieldValue = aggregatePoint.getArray(pointFieldName);

          if (aggregatePointFieldValue == null) {
            aggregatePointFieldValue = new JsonArray();
            aggregatePoint.putArray(pointFieldName, aggregatePointFieldValue);
          }

          aggregatePointFieldValue.add(point.getValue(pointFieldName));
        }
      }
    }

    return convertCollectionToJsonArray(aggregatePoints.values());
  }

  private void applyPointClauseToMetrics(Query query, JsonArray metrics) throws InvalidExpressionException {
    if (!query.hasPointClause()) {
      return;
    }

    HashMap<String, AggregateField> pointClause = query.pointClause();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyPointClauseToPoints(pointClause, metric);
    }
  }

  private void applyPointClauseToPoints(HashMap<String, AggregateField> pointClause, JsonObject metric) throws InvalidExpressionException {
    Set<Map.Entry<String, AggregateField>> pointClauseEntries = pointClause.entrySet();
    JsonArray transformedPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      JsonObject transformedPoint = new JsonObject();

      for (Map.Entry<String, AggregateField> pointClauseEntry : pointClauseEntries) {
        AggregateField aggregateField = pointClauseEntry.getValue();
        Object transformedPointFieldValue;

        if (aggregateField.hasExpression()) {
          Object pointFieldValue = point.getValue(aggregateField.fieldName());
          Expression expression = aggregateField.expression();

          if (pointFieldValue instanceof JsonArray) {
            pointFieldValue = ((JsonArray) pointFieldValue).toList();
          }

          transformedPointFieldValue = expression.evaluate(pointFieldValue);
        } else {
          transformedPointFieldValue = point.getValue(aggregateField.fieldName());
        }

        String transformedPointFieldName = pointClauseEntry.getKey();
        transformedPoint.putValue(transformedPointFieldName, transformedPointFieldValue);
      }

      transformedPoints.addObject(transformedPoint);
    }

    metric.putArray("points", transformedPoints);
  }

  private JsonArray applyMetricClauseToMetrics(Query query, JsonArray metrics) {
    if (!query.hasMetricClause()) {
      logger.info("No projection to apply to metrics");
      return metrics;
    }

    JsonObject metricClause = query.metricClause();
    JsonArray transformedMetrics = new JsonArray();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      logger.info("Applying projection " + metricClause + " to metric " + metric);
      JsonObject transformedMetric = new JsonObject();

      for (String projectionFieldName : metricClause.getFieldNames()) {
        String metricFieldName = metricClause.getString(projectionFieldName);
        transformedMetric.putValue(projectionFieldName, metric.getValue(metricFieldName));
      }

      transformedMetric.putArray("points", metric.getArray("points"));

      transformedMetrics.addObject(transformedMetric);
    }

    return transformedMetrics;
  }

  private <T> JsonArray convertCollectionToJsonArray(Collection<T> collection) {
    JsonArray jsonArray = new JsonArray();

    collection.forEach(jsonArray::add);

    return jsonArray;
  }

  private boolean pointMatchesWhereClause(JsonObject point, Map<String, Expression> where) throws InvalidExpressionException {
    // TODO: Move method to WhereClause class

    if (where == null) {
      return true;
    }

    boolean isMatch = true;

    for (Map.Entry<String, Expression> whereClauseEntry : where.entrySet()) {
      String fieldName = whereClauseEntry.getKey();
      Expression expression = whereClauseEntry.getValue();
      Object pointFieldValue = point.getValue(fieldName);

      Object result = expression.evaluate(pointFieldValue);

      if (result == null || !(result instanceof Boolean)) {
        isMatch = false;
        break;
      }

      boolean booleanResult = (boolean) result;

      if (booleanResult == false) {
        isMatch = false;
        break;
      }
    }

    return isMatch;
  }

  private class SocketState {
    private HashMap<String, Query> queries = new HashMap<>();

    public HashMap<String, Query> queries() {
      return queries;
    }
  }
}
