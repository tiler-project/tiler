package io.tiler.internal.queries.clauses;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;

public class WhereClause {
  private final Expression expression;

  public WhereClause(Expression expression) {
    this.expression = expression;
  }

  public Expression expression() {
    return expression;
  }

  public void applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyToMetric(clock, metric);
    }
  }

  private void applyToMetric(Clock clock, JsonObject metric) throws EvaluationException {
    JsonArray matchingPoints = new JsonArray();

    for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
      if (pointMatches(clock, point)) {
        matchingPoints.addObject(point);
      }
    }

    metric.putArray("points", matchingPoints);
  }

  private boolean pointMatches(Clock clock, JsonObject point) throws EvaluationException {
    EvaluationContext context = new EvaluationContext(clock, point);
    Object value = expression.evaluate(context);

    if (!(value instanceof Boolean)) {
      throw new EvaluationException(expression.queryContext(), "where clause must evaluate to a boolean value");
    }

    return (boolean) value;
  }
}
