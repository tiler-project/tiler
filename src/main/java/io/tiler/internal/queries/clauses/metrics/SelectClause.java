package io.tiler.internal.queries.clauses.metrics;

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

  public JsonArray applyToMetrics(JsonArray metrics) throws EvaluationException {
    JsonArray transformedMetrics = new JsonArray();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      JsonObject transformedMetric = applyToFields(metric);
      transformedMetric.putArray("points", metric.getArray("points"));

      transformedMetrics.addObject(transformedMetric);
    }

    return transformedMetrics;
  }
}
