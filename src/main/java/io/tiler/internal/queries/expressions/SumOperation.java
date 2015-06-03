package io.tiler.internal.queries.expressions;

import java.util.List;

public class SumOperation extends NumberAggregationOperation {
  @Override
  protected Number aggregateNumbers(List<Number> numbers) throws InvalidExpressionException {
    return calculateSum(numbers);
  }

  private double calculateSum(List<Number> values) throws InvalidExpressionException {
    double sum = 0;

    for (Number value : values) {
      sum += value.doubleValue();
    }

    return sum;
  }
}
