package io.tiler.internal.queries.expressions;

import java.util.List;

public class MeanOperation extends NumberAggregationOperation {
  @Override
  protected Number aggregateNumbers(List<Number> numbers) throws InvalidExpressionException {
    return calculateMean(numbers);
  }

  private double calculateMean(List<Number> values) throws InvalidExpressionException {
    double sum = 0;
    int count = 0;

    for (Number value : values) {
      sum = sum + value.doubleValue();
      count++;
    }

    return sum / count;
  }
}
