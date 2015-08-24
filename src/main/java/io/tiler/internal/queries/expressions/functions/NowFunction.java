package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;

public class NowFunction extends Function {
  public NowFunction(QueryContext queryContext) {
    super(queryContext);
  }

  @Override
  public Object evaluate(EvaluationContext context) {
    return context.clock().millis() * 1000L;
  }
}
