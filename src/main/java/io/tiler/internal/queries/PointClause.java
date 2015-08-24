package io.tiler.internal.queries;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class PointClause extends ProjectionClause {
  public PointClause(Map<String, Expression> namedExpressions) {
    super(namedExpressions);
  }

  public void applyToMetrics(JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyToPoints(metric);
    }
  }

  private void applyToPoints(JsonObject metric) throws EvaluationException {
    JsonArray transformedPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      JsonObject transformedPoint = applyToFields(point);
      transformedPoints.addObject(transformedPoint);
    }

    metric.putArray("points", transformedPoints);
  }
}
