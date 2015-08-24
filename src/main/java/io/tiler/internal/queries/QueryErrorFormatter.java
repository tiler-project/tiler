package io.tiler.internal.queries;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

public class QueryErrorFormatter {
  private static final Pattern newlinePattern = Pattern.compile("\\r?\\n");

  public static String format(String query, QueryError error) {
    StringBuilder builder = new StringBuilder();
    String[] lines = newlinePattern.split(query);
    formatError(builder, lines, error);
    return builder.toString();
  }

  public static String format(String query, List<QueryError> errors) {
    StringBuilder builder = new StringBuilder();
    String[] lines = newlinePattern.split(query);

    for (int index = 0, count = errors.size(); index < count; index++) {
      if (index > 0) {
        builder.append(System.lineSeparator());
      }

      formatError(builder, lines, errors.get(index));
    }

    return builder.toString();
  }

  private static void formatError(StringBuilder builder, String[] lines, QueryError error) {
    int line = error.line();
    int column = error.column();
    builder
      .append("Line " + line + ":" + column)
      .append(System.lineSeparator())
      .append(lines[line - 1])
      .append(System.lineSeparator())
      .append(StringUtils.repeat(' ', column))
      .append("^ ")
      .append(error.message());
  }
}
