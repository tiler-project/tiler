package io.tiler.internal.queries.expressions;

import java.util.List;

public class MinOperation extends NumberAggregationOperation {
  @Override
  protected Number aggregateNumbers(List<Number> numbers) throws InvalidExpressionException {
    return calculateMin(numbers);
  }

  private Double calculateMin(List<Number> values) throws InvalidExpressionException {
    Double min = null;

    for (Number value : values) {
      double doubleValue = value.doubleValue();

      if (min == null || doubleValue < min) {
        min = doubleValue;
      }
    }

    return min;
  }
}
