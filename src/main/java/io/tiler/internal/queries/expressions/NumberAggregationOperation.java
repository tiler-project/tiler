package io.tiler.internal.queries.expressions;

import java.util.ArrayList;
import java.util.List;

public abstract class NumberAggregationOperation extends AggregationOperation {
  @Override
  protected Object aggregate(List<?> values) throws InvalidExpressionException {
    ArrayList<Number> numbers = new ArrayList<>();

    for (Object value : values) {
      if (!(value instanceof Number)) {
        throw new InvalidExpressionException("Left hand value must be a list of numbers");
      }

      numbers.add((Number) value);
    }

    return aggregateNumbers(numbers);
  }

  protected abstract Number aggregateNumbers(List<Number> numbers) throws InvalidExpressionException;
}
