package io.tiler.internal.queries.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexOperation extends BaseRegexOperation {
  private final Pattern pattern;

  public RegexOperation(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    super(arguments);

    pattern = getPattern();
  }

  @Override
  public List<String> getMandatoryArgumentNames() {
    return Arrays.asList("$pattern");
  }

  @Override
  public List<String> getOptionalArgumentNames() {
    return Arrays.asList("$options");
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    if (!(leftHandValue instanceof String)) {
      throw new InvalidExpressionException("Left hand value must be a String");
    }

    return pattern.matcher((String) leftHandValue).find();
  }

  public Pattern pattern() {
    return pattern;
  }
}
