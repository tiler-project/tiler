package io.tiler.internal.queries.clauses.points;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.clauses.ProjectionClause;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

public class SelectClause extends ProjectionClause {
  public SelectClause(Map<String, Expression> namedExpressions) {
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
