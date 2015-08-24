package io.tiler.internal.queries;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MetricClause extends ProjectionClause {
  public MetricClause(Map<String, Expression> namedExpressions) {
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
