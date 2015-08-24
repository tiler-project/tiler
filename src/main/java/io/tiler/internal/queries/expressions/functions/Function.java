package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public abstract class Function extends Expression {
  public Function(QueryContext queryContext) {
    super(queryContext);
  }
}
