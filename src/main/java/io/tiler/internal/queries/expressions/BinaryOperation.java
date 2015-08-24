package io.tiler.internal.queries.expressions;

import io.tiler.internal.queries.QueryContext;

public abstract class BinaryOperation extends Operation {
  private final Expression operand1;
  private final Expression operand2;

  public BinaryOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext);
    this.operand1 = operand1;
    this.operand2 = operand2;
  }

  public Expression operand1() {
    return operand1;
  }

  public Expression operand2() {
    return operand2;
  }
}
