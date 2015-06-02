package io.tiler.internal.queries.expressions;

public class AllOperation extends Operation {
  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    return true;
  }
}
