package io.tiler.internal.queries;

import io.tiler.internal.queries.expressions.*;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Query {
  private final HashMap<String, AggregateField> metricClause;
  private final HashMap<String, AggregateField> pointClause;
  private final FromClause fromClause;
  private final Map<String, Expression> whereClause;
  private final JsonArray groupClause;
  private final Map<String, Expression> aggregateClause;

  public Query(HashMap<String, AggregateField> metricClause, HashMap<String, AggregateField> pointClause, FromClause fromClause, Map<String, Expression> whereClause, JsonArray groupClause, Map<String, Expression> aggregateClause) {
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

    try {
      return getProjectionClause(pointJsonExpression);
    } catch (InvalidQueryException e) {
      throw new InvalidQueryException("Invalid point clause", e);
    }
  }

  private static HashMap<String, AggregateField> getMetricClauseFromQuery(JsonObject query) throws InvalidQueryException {
    JsonObject metricJsonExpression = query.getObject("metric");

    if (metricJsonExpression == null) {
      return null;
    }

    try {
      return getProjectionClause(metricJsonExpression);
    } catch (InvalidQueryException e) {
      throw new InvalidQueryException("Invalid metric clause", e);
    }
  }

  private static HashMap<String, AggregateField> getProjectionClause(JsonObject clauseJsonExpression) throws InvalidQueryException {
    HashMap<String, AggregateField> clause = new HashMap<>();

    for (String fieldName : clauseJsonExpression.getFieldNames()) {
      Object fieldValue = clauseJsonExpression.getValue(fieldName);
      AggregateField aggregateField;

      if (fieldValue instanceof String) {
        aggregateField = new AggregateField((String) fieldValue);
      }
      else if (fieldValue instanceof JsonObject) {
        JsonObject fieldValueJsonObject = (JsonObject) fieldValue;

        switch (fieldValueJsonObject.size()) {
          case 0:
            throw new InvalidQueryException("No field in object for '" + fieldName + "'");
          case 1:
            String aggregateFieldName = JsonHelper.getFirstFieldNameFromJsonObject(fieldValueJsonObject);
            Expression expression;

            try {
              expression = ExpressionFactory.createExpressionFromJsonExpression(
                fieldValueJsonObject.getValue(aggregateFieldName));
            }
            catch (InvalidExpressionException e) {
              throw new InvalidQueryException("Invalid expression for field '" + fieldName + "'", e);
            }

            aggregateField = new AggregateField(aggregateFieldName, expression);

            break;
          default:
            throw new InvalidQueryException("More than 1 field in object for '" + fieldName + "'");
        }
      }
      else {
        throw new InvalidQueryException("Field must be mapped to a string or a JSON object");
      }

      clause.put(fieldName, aggregateField);
    }

    return clause;
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

  public HashMap<String, AggregateField> metricClause() {
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
