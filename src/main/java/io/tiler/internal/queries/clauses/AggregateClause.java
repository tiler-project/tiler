package io.tiler.internal.queries.clauses;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.core.json.JsonArrayUtils;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.aggregations.AggregateExpression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.util.*;

public class AggregateClause {
  private final Map<String, Expression> namedExpressions;

  public AggregateClause(Map<String, Expression> namedExpressions) {
    this.namedExpressions = Collections.unmodifiableMap(namedExpressions);
  }

  public Map<String, Expression> namedExpressions() {
    return namedExpressions;
  }

  public void applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      JsonArray transformedPoints = applyToPoints(clock, metric.getArray("points"));
      metric.putArray("points", transformedPoints);
    }
  }

  private JsonArray applyToPoints(Clock clock, JsonArray points) throws EvaluationException {
    // TODO: Maybe combine this method with the equivalent for the group clause
    HashMap<ArrayList<Object>, JsonObject> aggregatePoints = new HashMap<>();
    HashSet<String> aggregateFieldNames = new HashSet<>();

    for (Map.Entry<String, Expression> aggregateClauseEntry : namedExpressions.entrySet()) {
      aggregateFieldNames.add(aggregateClauseEntry.getKey());
    }

    for (JsonObject point : new JsonArrayIterable<JsonObject>(points)) {
      EvaluationContext context = new EvaluationContext(clock, point);
      ArrayList<Object> aggregateKey = new ArrayList<>();

      for (Map.Entry<String, Expression> aggregateClauseEntry : namedExpressions.entrySet()) {
        String fieldName = aggregateClauseEntry.getKey();
        Expression expression = aggregateClauseEntry.getValue();
        Object aggregateValue = expression.evaluate(context);

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
        if (!aggregateFieldNames.contains(pointFieldName)) {
          JsonArray aggregatePointFieldValue = aggregatePoint.getArray(pointFieldName);

          if (aggregatePointFieldValue == null) {
            aggregatePointFieldValue = new JsonArray();
            aggregatePoint.putArray(pointFieldName, aggregatePointFieldValue);
          }

          aggregatePointFieldValue.add(point.getValue(pointFieldName));
        }
      }
    }

    return JsonArrayUtils.convertToJsonArray(aggregatePoints.values());
  }
}
