package io.tiler.internal.queries.expressions;

import java.util.List;

public class MaxOperation extends NumberAggregationOperation {
  @Override
  protected Number aggregateNumbers(List<Number> numbers) throws InvalidExpressionException {
    return calculateMax(numbers);
  }

  private Double calculateMax(List<Number> values) throws InvalidExpressionException {
    Double max = null;

    for (Number value : values) {
      double doubleValue = value.doubleValue();

      if (max == null || doubleValue < max) {
        max = doubleValue;
      }
    }

    return max;
  }
}
