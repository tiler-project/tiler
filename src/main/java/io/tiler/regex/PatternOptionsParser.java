package io.tiler.regex;

import java.util.regex.Pattern;

public class PatternOptionsParser {
  public int parsePatternOptions(String patternOptions) throws InvalidPatternOptionsException {
    int flags = 0;

    for (int index = 0, length = patternOptions.length(); index < length; index++) {
      char option = patternOptions.charAt(index);
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
          throw new InvalidPatternOptionsException("Unsupported option '" + option + "'");
      }

      if ((flags & flag) != 0) {
        throw new InvalidPatternOptionsException("Same option '" + option + "' specified multiple times");
      }

      flags |= flag;
    }

    return flags;
  }
}
