package io.tiler.internal.queries.expressions;

import java.util.List;

public class LastOperation extends AggregationOperation {
  @Override
  protected Object aggregate(List<?> values) throws InvalidExpressionException {
    return getLastValue(values);
  }

  private Object getLastValue(List<?> values) throws InvalidExpressionException {
    if (values.size() == 0) {
      return null;
    }

    return values.get(values.size() - 1);
  }
}
