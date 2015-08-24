package io.tiler.internal.queries.expressions;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;

public abstract class Expression {
  private final QueryContext queryContext;

  public Expression(QueryContext queryContext) {
    this.queryContext = queryContext;
  }

  public abstract Object evaluate(EvaluationContext context) throws EvaluationException;

  public QueryContext queryContext() {
    return queryContext;
  }
}
