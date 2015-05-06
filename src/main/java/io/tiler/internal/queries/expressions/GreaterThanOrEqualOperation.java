package io.tiler.internal.queries.expressions;

public class GreaterThanOrEqualOperation extends ComparisonOperation {
  public GreaterThanOrEqualOperation(Expression argument) {
    super(argument);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    return compare(leftHandValue, getArgument(0).evaluate(leftHandValue)) >= 0;
  }
}
