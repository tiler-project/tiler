package io.tiler.internal.queries;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;

public class WhereClause {
  private Expression expression;

  public WhereClause(Expression expression) {
    this.expression = expression;
  }

  public Expression expression() {
    return expression;
  }

  public void applyToMetrics(JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyToMetric(metric);
    }
  }

  private void applyToMetric(JsonObject metric) throws EvaluationException {
    JsonArray matchingPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      if (pointMatches(point)) {
        matchingPoints.addObject(point);
      }
    }

    metric.putArray("points", matchingPoints);
  }

  private boolean pointMatches(JsonObject point) throws EvaluationException {
    EvaluationContext context = new EvaluationContext(Clock.systemUTC(), point);
    Object value = expression.evaluate(context);

    if (!(value instanceof Boolean)) {
      throw new EvaluationException(expression.queryContext(), "where clause must evaluate to a boolean value");
    }

    return (boolean) value;
  }
}
