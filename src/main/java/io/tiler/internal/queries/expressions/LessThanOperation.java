package io.tiler.internal.queries.expressions;

public class LessThanOperation extends ComparisonOperation {
  public LessThanOperation(Expression argument) {
    super(argument);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    return compare(leftHandValue, getArgument(0).evaluate(leftHandValue)) < 0;
  }
}
