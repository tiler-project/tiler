package io.tiler.internal.queries.expressions.aggregations;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;

public class AllFunction extends AggregateExpression {
  public AllFunction(QueryContext queryContext) {
    super(queryContext);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    return true;
  }
}
