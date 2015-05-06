package io.tiler.internal.queries.expressions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RegularExpressionOperation extends OperationWithNamedArguments {
  public RegularExpressionOperation(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    super(arguments);
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
    int flags = 0;

    for (int index = 0, length = options.length(); index < length; index++) {
      char option = options.charAt(index);
      int flag;

      switch (option) {
        case 'd':
          flag = Pattern.UNIX_LINES;
          break;
        case 'i':
          flag = Pattern.CASE_INSENSITIVE;
          break;
        case 'x':
          flag = Pattern.COMMENTS;
          break;
        case 'm':
          flag = Pattern.MULTILINE;
          break;
        case 's':
          flag = Pattern.DOTALL;
          break;
        case 'u':
          flag = Pattern.UNICODE_CASE;
          break;
        case 'U':
          flag = Pattern.UNICODE_CHARACTER_CLASS;
          break;
        default:
          throw new InvalidExpressionException("Unsupported option '" + option + "'");
      }

      if ((flags & flag) != 0) {
        throw new InvalidExpressionException("Same option '" + option + "' specified multiple times");
      }

      flags |= flag;
    }

    return flags;
  }
}
