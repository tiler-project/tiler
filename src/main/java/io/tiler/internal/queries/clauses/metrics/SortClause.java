package io.tiler.internal.queries.clauses.metrics;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.clauses.BaseSortClause;
import io.tiler.internal.queries.clauses.SortExpression;
import org.vertx.java.core.json.JsonArray;

import java.time.Clock;
import java.util.ArrayList;

public class SortClause extends BaseSortClause {
  public SortClause(ArrayList<SortExpression> sortExpressions) {
    super(sortExpressions);
  }

  public JsonArray applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    return applyToItems(clock, metrics);
  }
}
