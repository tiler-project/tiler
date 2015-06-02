package io.tiler.internal.queries.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IntervalsOperation extends OperationWithNamedArguments {
  public IntervalsOperation(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    super(arguments);
  }

  @Override
  public List<String> getMandatoryArgumentNames() {
    return Arrays.asList("$size");
  }

  @Override
  public List<String> getOptionalArgumentNames() {
    return Arrays.asList("$offset");
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    if (!(leftHandValue instanceof Number)) {
      throw new InvalidExpressionException("Left hand value must be a number");
    }

    Object size = getArgument("$size").evaluate(leftHandValue);
    Object offset = hasArgument("$size") ? getArgument("$size").evaluate(leftHandValue) : 0;

    if (!(size instanceof Number)) {
      throw new InvalidExpressionException("$size must be a number");
    }

    if (!(offset instanceof Number)) {
      throw new InvalidExpressionException("$offset must be a number");
    }

    long leftHandValueNumber = ((Number) leftHandValue).longValue();
    long sizeNumber = ((Number) size).longValue();
    long offsetNumber = ((Number) offset).longValue();

    long multiplier = (leftHandValueNumber - offsetNumber) / sizeNumber;
    return offsetNumber + (sizeNumber * multiplier);
  }
}
