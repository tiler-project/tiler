package io.tiler.internal.queries.clauses.points;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.clauses.BaseSelectClause;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.util.Map;

public class SelectClause extends BaseSelectClause {
  public SelectClause(Map<String, Expression> namedExpressions) {
    super(namedExpressions);
  }

  public void applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyToPoints(clock, metric);
    }
  }

  private void applyToPoints(Clock clock, JsonObject metric) throws EvaluationException {
    JsonArray transformedPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      JsonObject transformedPoint = applyToItem(clock, point);
      transformedPoints.addObject(transformedPoint);
    }

    metric.putArray("points", transformedPoints);
  }
}
