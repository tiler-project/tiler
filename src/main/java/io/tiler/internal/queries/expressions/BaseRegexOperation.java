package io.tiler.internal.queries.expressions;

import io.tiler.core.regex.InvalidPatternOptionsException;
import io.tiler.core.regex.PatternOptionsParser;

import java.util.Map;
import java.util.regex.Pattern;

public abstract class BaseRegexOperation extends OperationWithNamedArguments {
  private final PatternOptionsParser patternOptionsParser;
  private final Pattern pattern;

  public BaseRegexOperation(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    super(arguments);

    patternOptionsParser = new PatternOptionsParser();
    pattern = getPattern();
  }

  protected Pattern getPattern() throws InvalidExpressionException {
    Object pattern = getArgument("$pattern").evaluate(null);

    if (pattern == null) {
      throw new InvalidExpressionException("$pattern argument must not evaluate to null");
    }

    if (!(pattern instanceof String)) {
      throw new InvalidExpressionException("$pattern argument must evaluate to a string");
    }

    Expression optionsArgument = getArgument("$options");
    int flags;

    if (optionsArgument != null) {
      Object options = optionsArgument.evaluate(null);

      if (options == null) {
        throw new InvalidExpressionException("options argument must not evaluate to null");
      }

      if (!(options instanceof String)) {
        throw new InvalidExpressionException("options argument must evaluate to a string");
      }

      flags = parseOptions((String) options);
    } else {
      flags = 0;
    }

    return Pattern.compile((String) pattern, flags);
  }

  private int parseOptions(String options) throws InvalidExpressionException {
    try {
      return patternOptionsParser.parsePatternOptions(options);
    } catch (InvalidPatternOptionsException e) {
      throw new InvalidExpressionException("Invalid options argument", e);
    }
  }
}
