package io.tiler.internal.queries;

import io.tiler.internal.queries.expressions.*;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Query {
  private final JsonObject metricClause;
  private final HashMap<String, AggregateField> pointClause;
  private final FromClause fromClause;
  private final Map<String, Expression> whereClause;
  private final JsonArray groupClause;
  private final Map<String, Expression> aggregateClause;

  public Query(JsonObject metricClause, HashMap<String, AggregateField> pointClause, FromClause fromClause, Map<String, Expression> whereClause, JsonArray groupClause, Map<String, Expression> aggregateClause) {
    this.metricClause = metricClause;
    this.pointClause = pointClause;
    this.fromClause = fromClause;
    this.whereClause = whereClause;
    this.groupClause = groupClause;
    this.aggregateClause = aggregateClause;
  }

  public static Query fromJsonObject(JsonObject value) throws InvalidQueryException {
    return new Query(getMetricClauseFromQuery(value),
      getPointClauseFromQuery(value),
      getFromClauseFromQuery(value),
      getWhereClauseFromQuery(value),
      getGroupClauseFromQuery(value),
      getAggregateClauseFromQuery(value));
  }

  private static HashMap<String, AggregateField> getPointClauseFromQuery(JsonObject query) throws InvalidQueryException {
    JsonObject pointJsonExpression = query.getObject("point");

    if (pointJsonExpression == null) {
      return null;
    }

    HashMap<String, AggregateField> pointClause = new HashMap<>();

    for (String pointFieldName : pointJsonExpression.getFieldNames()) {
      Object pointFieldValue = pointJsonExpression.getValue(pointFieldName);
      AggregateField aggregateField;

      if (pointFieldValue instanceof String) {
        aggregateField = new AggregateField((String) pointFieldValue);
      }
      else if (pointFieldValue instanceof JsonObject) {
        JsonObject pointFieldValueJsonObject = (JsonObject) pointFieldValue;

        switch (pointFieldValueJsonObject.size()) {
          case 0:
            throw new InvalidQueryException("Invalid point clause. No field in object for '" + pointFieldName + "'");
          case 1:
            String aggregateFieldName = JsonHelper.getFirstFieldNameFromJsonObject(pointFieldValueJsonObject);
            Expression expression;

            try {
              expression = ExpressionFactory.createExpressionFromJsonExpression(
                pointFieldValueJsonObject.getValue(aggregateFieldName));
            }
            catch (InvalidExpressionException e) {
              throw new InvalidQueryException("Invalid point clause.  Invalid expression for field '" + pointFieldName + "'", e);
            }

            aggregateField = new AggregateField(aggregateFieldName, expression);

            break;
          default:
            throw new InvalidQueryException("Invalid point clause. More than 1 field in object for '" + pointFieldName + "'");
        }
      }
      else {
        throw new InvalidQueryException("Invalid point clause. Field must be mapped to a string or a JSON object");
      }

      pointClause.put(pointFieldName, aggregateField);
    }

    return pointClause;
  }

  private static JsonObject getMetricClauseFromQuery(JsonObject query) {
    JsonObject metricJsonExpression = query.getObject("metric");

    if (metricJsonExpression == null) {
      return null;
    }

    return metricJsonExpression;
  }

  private static FromClause getFromClauseFromQuery(JsonObject query) throws InvalidQueryException {
    return FromClause.fromJsonExpression(query.getValue("from"));
  }

  private static Map<String, Expression> getWhereClauseFromQuery(JsonObject query) throws InvalidQueryException {
    JsonObject whereJsonExpression = query.getObject("where");

    if (whereJsonExpression == null) {
      return null;
    }

    HashMap<String, Expression> whereClause = new HashMap<>();

    if (whereJsonExpression != null) {
      try {
        for (String whereFieldName : whereJsonExpression.getFieldNames()) {
          Expression expression = ExpressionFactory.createExpressionFromJsonExpression(whereJsonExpression.getValue(whereFieldName));

          if (!(expression instanceof Operation)) {
            expression = new EqualsOperation(expression);
          }

          whereClause.put(whereFieldName, expression);
        }
      } catch (InvalidExpressionException e) {
        throw new InvalidQueryException("Invalid where clause in query", e);
      }
    }

    return whereClause;
  }

  private static JsonArray getGroupClauseFromQuery(JsonObject query) {
    JsonArray groupJsonExpression = query.getArray("group");

    if (groupJsonExpression == null) {
      return null;
    }

    return groupJsonExpression;
  }

  private static Map<String, Expression> getAggregateClauseFromQuery(JsonObject query) throws InvalidQueryException {
    JsonObject aggregateJsonExpression = query.getObject("aggregate");

    if (aggregateJsonExpression == null) {
      return null;
    }

    HashMap<String, Expression> aggregateClause = new HashMap<>();

    if (aggregateJsonExpression != null) {
      try {
        for (String aggregateFieldName : aggregateJsonExpression.getFieldNames()) {
          Expression expression = ExpressionFactory.createExpressionFromJsonExpression(aggregateJsonExpression.getValue(aggregateFieldName));
          aggregateClause.put(aggregateFieldName, expression);
        }
      }
      catch (InvalidExpressionException e) {
        throw new InvalidQueryException("Invalid aggregate clause in query", e);
      }
    }

    return aggregateClause;
  }

  public HashMap<String, AggregateField> pointClause() {
    return pointClause;
  }

  public JsonObject metricClause() {
    return metricClause;
  }

  public FromClause fromClause() {
    return fromClause;
  }

  public Map<String, Expression> whereClause() {
    return whereClause;
  }

  public JsonArray groupClause() {
    return groupClause;
  }

  public Map<String, Expression> aggregateClause() {
    return aggregateClause;
  }

  public boolean hasMetricClause() {
    return metricClause() != null;
  }

  public boolean hasPointClause() {
    return pointClause() != null;
  }

  public boolean hasGroupClause() {
    return groupClause() != null;
  }

  public boolean hasAggregateClause() {
    return aggregateClause() != null;
  }
}
