package io.tiler.internal.queries.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceOperation extends BaseRegexOperation {
  private final Pattern pattern;
  private final String replacement;

  public ReplaceOperation(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    super(arguments);

    pattern = getPattern();
    replacement = getReplacement();
  }

  private String getReplacement() throws InvalidExpressionException {
    Object replacement = getArgument("$replacement").evaluate(null);

    if (replacement == null) {
      throw new InvalidExpressionException("$replacement argument must not evaluate to null");
    }

    if (!(replacement instanceof String)) {
      throw new InvalidExpressionException("$replacement argument must evaluate to a string");
    }

    return (String) replacement;
  }

  @Override
  public List<String> getMandatoryArgumentNames() {
    return Arrays.asList("$pattern", "$replacement");
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

    String leftHandValueString = (String) leftHandValue;
    Matcher matcher = pattern.matcher(leftHandValueString);
    leftHandValueString = matcher.replaceAll(replacement);

    return leftHandValueString;
  }
}
