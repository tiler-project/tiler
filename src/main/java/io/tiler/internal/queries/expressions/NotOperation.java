package io.tiler.internal.queries.expressions;

public class NotOperation extends LogicalOperation {
  public NotOperation(Expression argument) {
    super(argument);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    return !evaluatesToTrue(getArgument(0).evaluate(leftHandValue));
  }
}

