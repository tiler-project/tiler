package io.tiler.internal.queries.expressions;

import java.util.List;

public abstract class AggregationOperation extends Operation {
  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    if (!(leftHandValue instanceof List<?>)) {
      throw new InvalidExpressionException("Left hand value must be a list");
    }

    return aggregate((List<?>) leftHandValue);
  }

  protected abstract Object aggregate(List<?> values) throws InvalidExpressionException;
}
