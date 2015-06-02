package io.tiler.internal.queries.expressions;

import java.util.List;

public class FirstOperation extends AggregationOperation {
  @Override
  protected Object aggregate(List<?> values) throws InvalidExpressionException {
    return getFirstValue(values);
  }

  private Object getFirstValue(List<?> values) throws InvalidExpressionException {
    if (values.size() == 0) {
      return null;
    }

    return values.get(0);
  }
}
