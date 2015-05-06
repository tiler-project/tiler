package io.tiler.internal.queries.expressions;

import java.util.ArrayList;
import java.util.List;

public class MeanOperation extends Operation {
  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    if (!(leftHandValue instanceof List<?>)) {
      throw new InvalidExpressionException("Left hand value must be a list");
    }

    ArrayList<Number> values = new ArrayList<>();

    for (Object value : (List<?>) leftHandValue) {
      if (!(value instanceof Number)) {
        throw new InvalidExpressionException("Left hand value must be a list of numbers");
      }

      values.add((Number) value);
    }

    return calculateMean(values);
  }

  private double calculateMean(Iterable<Number> values) throws InvalidExpressionException {
    double sum = 0;
    int count = 0;

    for (Number value : values) {
      System.console().printf(Double.toString(sum));
      sum = sum + value.doubleValue();
      count++;
    }

    return sum / count;
  }
}
