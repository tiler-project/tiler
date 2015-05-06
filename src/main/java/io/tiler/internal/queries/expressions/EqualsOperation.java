package io.tiler.internal.queries.expressions;

public class EqualsOperation extends ComparisonOperation {
  public EqualsOperation(Expression argument) {
    super(argument);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    return compare(leftHandValue, getArgument(0).evaluate(leftHandValue)) == 0;
  }
}
