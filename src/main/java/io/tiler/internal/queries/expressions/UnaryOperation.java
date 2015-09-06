package io.tiler.internal.queries.expressions;

import io.tiler.internal.queries.QueryContext;

public abstract class UnaryOperation extends Operation {
  private final Expression operand;

  public UnaryOperation(QueryContext queryContext, Expression operand) {
    super(queryContext);
    this.operand = operand;
  }

  public Expression operand() {
    return operand;
  }
}
