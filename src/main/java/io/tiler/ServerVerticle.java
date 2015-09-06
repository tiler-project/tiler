package io.tiler;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.engine.StringPlaceholderEngine;
import com.jetdrone.vertx.yoke.middleware.*;
import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.RedisException;
import io.tiler.internal.SocketState;
import io.tiler.internal.config.Config;
import io.tiler.internal.config.ConfigFactory;
import io.tiler.internal.queries.*;
import io.tiler.internal.queries.clauses.FromClause;
import io.vertx.java.redis.RedisClient;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.AsyncResultHandlerWrapper;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.platform.Verticle;

import java.time.Clock;
import java.util.*;

public class ServerVerticle extends Verticle {
  private Config config;
  private org.vertx.java.core.logging.Logger logger;
  private EventBus eventBus;
  private RedisClient redis;
  private QueryFactory queryFactory;
  private final HashMap<SockJSSocket, SocketState> socketStates = new HashMap<>();

  public void start(Future<Void> startFuture) {
    config = new ConfigFactory().load(container.config());
    logger = container.logger();
    eventBus = vertx.eventBus();
    redis = new RedisClient(eventBus, config.redis().address());
    queryFactory = new QueryFactory();

    Async.series()
      .task(handler -> container.deployModule("io.vertx~mod-redis~1.1.4", config.redis().toRedisModuleConfig(), 1, AsyncResultHandlerWrapper.wrap(handler)))
      .task(handler -> {
        HttpServer httpServer = vertx.createHttpServer();

        Router router = new Router()
          .get("/", (request, next) -> {
            // TODO: Replace the redirect with a page that lists all the available dashboards
            request.response().redirect("/dashboards/sample");
          })
          .get("/dashboards/:dashboardName", (request, next) -> {
            String dashboardName = request.getParameter("dashboardName");
            logger.info("Serving dashboard '" + dashboardName + "'");
            request.put("dashboardName", dashboardName);
            request.response().setContentType("text/html", "utf-8")
              .render("dashboard.shtml", next);
          })
          .post("/api/v1/query", this::queryMetricsMiddleware);

        if (!config.api().readOnly()) {
          router.post("/api/v1/metrics", this::createMetricsMiddleware);
        }

        // TODO: Implement metric search
        //.get("/api/v1/metric-search", this::getMetricSearchMiddleware)

        Yoke yoke = new Yoke(vertx);
        yoke.engine(new StringPlaceholderEngine("views"));
        yoke.use(new Logger());
        yoke.use(new ErrorHandler(true));
        yoke.use(new Favicon());
        yoke.use("/static", new Static("static"));
        yoke.use(new BodyParser());
        yoke.use(router);
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

                publishMetrics(metrics.result(), socket, socketState);
                logger.info("Metrics retrieved from Redis and published");
              });
            }
          });

          socket.endHandler(aVoid -> {
            logger.info("Socket closed");
            socketStates.remove(socket);
          });
        });

        httpServer.listen(config.port(), AsyncResultHandlerWrapper.wrap(handler));
      })
      .task(handler -> {
        eventBus.registerHandler("io.tiler", (Message<JsonObject> message) -> {
          String messageType = message.body().getString("type");
          logger.info("Received " + messageType + " message");
          JsonObject messageBody = message.body().getObject("body");

          switch (messageType) {
            case "publishMetrics": {
              JsonArray metrics = messageBody.getArray("metrics");

              saveAndPublishMetrics(metrics, result -> {
                if (result.failed()) {
                  logger.error("Failed to save or publish metrics", result.cause());
                  return;
                }

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
            }
          }
        });

        handler.handle(DefaultAsyncResult.succeed(null));
      })
      .run(handler -> {
        if (handler.failed()) {
          container.logger().error("ServerVerticle failed to start", handler.cause());
          startFuture.setFailure(handler.cause());
          return;
        }

        container.logger().info("ServerVerticle started");
        startFuture.setResult(null);
      });
  }

  private void createMetricsMiddleware(YokeRequest request, Handler<Object> next) {
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
  }

  private void queryMetricsMiddleware(YokeRequest request, Handler<Object> next) {
    Object body = request.body();
    YokeResponse response = request.response();

    if (!(body instanceof JsonObject)) {
      sendClientError(response, "Request body needs to be a JSON object");
      return;
    }

    JsonObject jsonBody = (JsonObject) body;

    if (!jsonBody.containsField("queries")) {
      sendClientError(response, "'queries' field missing from request body");
      return;
    }

    Object queriesObject = jsonBody.getValue("queries");

    if (!(queriesObject instanceof JsonArray)) {
      sendClientError(response, "'queries' field should be a '" + JsonArray.class.getName() + "' but was a '" + queriesObject.getClass().getName() + "'");
      return;
    }

    JsonArray queryObjects = (JsonArray) queriesObject;
    ArrayList<Query> queries = new ArrayList<>();

    for (Object queryObject : queryObjects) {
      if (!(queryObject instanceof String)) {
        sendClientError(response, "'queries' field item should be a '" + String.class.getName() + "' but was a '" + queryObject.getClass().getName() + "'");
        return;
      }

      Query query;

      try {
        query = queryFactory.parseQuery((String) queryObject);
      } catch (InvalidQueryException e) {
        sendClientError(response, e.getMessage());
        return;
      }

      queries.add(query);
    }

    getMetricsForQueries(queries, result -> {
      if (result.failed()) {
        sendClientError(response, "Failed to retrieve metrics. " + result.cause().getMessage());
        return;
      }

      JsonArray metrics = result.result();
      Clock clock = Clock.systemUTC();

      JsonArray results = new JsonArray();

      for (Query query : queries) {
        JsonArray transformedMetrics;

        try {
          transformedMetrics = query.applyToMetrics(clock, metrics);
        } catch (EvaluationException e) {
          sendClientError(response, "Error evaluating query. " + e.getMessage());
          return;
        }

        results.addArray(transformedMetrics);

        JsonObject responseBody = new JsonObject()
          .putArray("results", results);
        response.setStatusCode(200).end(responseBody);
      }
    });
  }

  private SocketState createStateForSocket(JsonObject queries) {
    SocketState socketState = new SocketState();

    HashMap<String, Query> socketStateQueries = socketState.queries();

    for (String key : queries.getFieldNames()) {
      Query query = null;

      try {
        query = queryFactory.parseQuery(queries.getString(key));
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
          handler.handle(DefaultAsyncResult.fail(missingMetricNames));
        }

        getMetrics(missingMetricNames.result(), missingMetrics -> {
          if (missingMetrics.failed()) {
            handler.handle(DefaultAsyncResult.fail(missingMetrics));
            return;
          }

          JsonArray combinedMetrics = metrics.copy();

          for (JsonObject metric : new JsonArrayIterable<JsonObject>(missingMetrics.result())) {
            combinedMetrics.addObject(metric);
          }

          publishMetrics(combinedMetrics);
          handler.handle(DefaultAsyncResult.succeed());
        });
      });
    });
  }

  private void saveMetrics(JsonArray metrics, AsyncResultHandler<Void> handler) {
    logger.info("Saving metrics to Redis");
    ArrayList<Object> saddArgs = new ArrayList<>();
    saddArgs.add(config.getMetricNamesKey());

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      saddArgs.add(metric.getString("name"));
    }

    saddArgs.add((Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(DefaultAsyncResult.fail(new RedisException(body)));
        return;
      }

      saveMetric(metrics, 0, handler);
    });

    redis.sadd(saddArgs.toArray());
  }

  private void saveMetric(JsonArray metrics, int metricIndex, AsyncResultHandler<Void> handler) {
    if (metricIndex >= metrics.size()) {
      handler.handle(DefaultAsyncResult.succeed());
      return;
    }

    JsonObject metric = metrics.get(metricIndex);

    String metricName = metric.getString("name");
    logger.info("Saving metric '" + metricName + "'");
    redis.set(config.getMetricKey(metricName), metric.toString(), (Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(DefaultAsyncResult.fail(new RedisException(body)));
        return;
      }

      saveMetric(metrics, metricIndex + 1, handler);
    });
  }

  private void publishMetrics(JsonArray metrics) {
    for (Map.Entry<SockJSSocket, SocketState> socketAndSocketState : socketStates.entrySet()) {
      SockJSSocket socket = socketAndSocketState.getKey();
      SocketState socketState = socketAndSocketState.getValue();
      publishMetrics(metrics, socket, socketState);
    }
  }

  private void publishMetrics(JsonArray metrics, SockJSSocket socket, SocketState socketState) {
    for (Map.Entry<String, Query> queryEntry : socketState.queries().entrySet()) {
      String queryKey = queryEntry.getKey();
      Query query = queryEntry.getValue();

      JsonArray matchingMetrics = query.fromClause().findMatchingMetrics(metrics);

      if (matchingMetrics.size() > 0) {
        StringBuilder logMessageBuilder = new StringBuilder()
          .append("Query '")
          .append(queryKey)
          .append("' matches metrics ");

        String separator = "";

        for (JsonObject metric : new JsonArrayIterable<JsonObject>(matchingMetrics)) {
          logMessageBuilder.append(separator);
          logMessageBuilder.append(metric.getString("name"));
          separator = ", ";
        }

        logger.info(logMessageBuilder.toString());

        JsonArray transformedMetrics = null;

        try {
          transformedMetrics = query.applyToMetrics(Clock.systemUTC(), matchingMetrics);
        } catch (EvaluationException e) {
          logger.error("Invalid query expression", e);
        }

        if (transformedMetrics != null) {
          JsonObject payload = new JsonObject();
          payload.putString("key", queryKey);
          payload.putArray("metrics", transformedMetrics);
          JsonObject newMessage = new JsonObject();
          newMessage.putString("type", "notify");
          newMessage.putObject("payload", payload);

          logger.info("Sending SockJS message");

          Buffer newMessageBuffer = new Buffer(newMessage.encode());
          socket.write(newMessageBuffer);
        }
      } else {
        logger.info("Query '" + queryKey + "' does not match metrics");
      }
    }
  }

  private void checkForMissingMetrics(Collection<Query> queries, JsonArray metrics, AsyncResultHandler<Collection<String>> handler) {
    HashSet<String> availableMetricNames = new HashSet<>();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      availableMetricNames.add(metric.getString("name"));
    }

    if (!anyQueryIsPotentiallyMissingAMetric(queries, availableMetricNames)) {
      handler.handle(DefaultAsyncResult.succeed(new ArrayList<>()));
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

      handler.handle(DefaultAsyncResult.succeed(missingMetricNames));
    });
  }

  private void getMetricsForQueries(Collection<Query> queries, AsyncResultHandler<JsonArray> handler) {
    getMetricNames(metricNames -> {
      if (metricNames.failed()) {
        handler.handle(DefaultAsyncResult.fail(metricNames));
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
    redis.smembers(config.getMetricNamesKey(), (Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        handler.handle(DefaultAsyncResult.fail(new RedisException(body)));
        return;
      }

      ArrayList<String> metricNames = new ArrayList<>();

      for (String redisValue : new JsonArrayIterable<String>(body.getArray("value"))) {
        metricNames.add(redisValue);
      }

      handler.handle(DefaultAsyncResult.succeed(metricNames));
    });
  }

  private void getMetrics(Collection<String> metricNames, AsyncResultHandler<JsonArray> handler) {
    if (metricNames.size() == 0) {
      handler.handle(DefaultAsyncResult.succeed(new JsonArray()));
      return;
    }

    ArrayList<Object> mgetArgs = new ArrayList<>();

    for (String metricName : metricNames) {
      mgetArgs.add(config.getMetricKey(metricName));
    }

    mgetArgs.add((Handler<Message<JsonObject>>) reply -> {
      JsonObject body = reply.body();
      String status = body.getString("status");

      if (!"ok".equals(status)) {
        RedisException e = new RedisException(body);
        handler.handle(DefaultAsyncResult.fail(e));
        return;
      }

      JsonArray metrics = new JsonArray();

      for (String redisValue : new JsonArrayIterable<String>(body.getArray("value"))) {
        if (redisValue != null) {
          metrics.addObject(new JsonObject(redisValue));
        }
      }

      handler.handle(DefaultAsyncResult.succeed(metrics));
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
}
