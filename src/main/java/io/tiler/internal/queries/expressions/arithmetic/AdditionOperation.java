package io.tiler.internal.queries.expressions.arithmetic;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class AdditionOperation extends ArithmeticOperation {
  public AdditionOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  protected double performArithmetic(double operand1, double operand2) {
    return operand1 + operand2;
  }
}
