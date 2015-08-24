package io.tiler.internal.queries.expressions;

import io.tiler.internal.queries.QueryContext;

public abstract class Operation extends Expression {
  public Operation(QueryContext queryContext) {
    super(queryContext);
  }
}
