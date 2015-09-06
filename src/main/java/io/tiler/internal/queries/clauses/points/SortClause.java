package io.tiler.internal.queries.clauses.points;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.clauses.BaseSortClause;
import io.tiler.internal.queries.clauses.SortExpression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.util.ArrayList;

public class SortClause extends BaseSortClause {
  public SortClause(ArrayList<SortExpression> sortExpressions) {
    super(sortExpressions);
  }

  public void applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      applyToPoints(clock, metric);
    }
  }

  private void applyToPoints(Clock clock, JsonObject metric) throws EvaluationException {
    metric.putArray("points", applyToItems(clock, metric.getArray("points")));
  }
}
