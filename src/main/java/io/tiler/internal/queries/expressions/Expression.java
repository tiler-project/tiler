package io.tiler.internal.queries.expressions;

public abstract class Expression {
  public abstract Object evaluate(Object leftHandValue) throws InvalidExpressionException;
}
