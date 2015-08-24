package io.tiler.internal.queries;

import java.util.regex.Pattern;

public class RegexMetricExpression extends MetricExpression {
  private Pattern pattern;

  public RegexMetricExpression(Pattern pattern) {
    this.pattern = pattern;
  }

  public Pattern pattern() {
    return pattern;
  }
}
