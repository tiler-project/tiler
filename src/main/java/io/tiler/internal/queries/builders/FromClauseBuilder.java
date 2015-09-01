package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.FromClause;
import io.tiler.internal.queries.clauses.MetricExpression;
import io.tiler.internal.queries.clauses.RegexMetricExpression;
import io.tiler.internal.queries.clauses.SimpleMetricExpression;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class FromClauseBuilder {
  private ArrayList<MetricExpression> metricExpressions = new ArrayList<>();

  public FromClauseBuilder metricExpression(String metricName) {
    metricExpressions.add(new SimpleMetricExpression(metricName));
    return this;
  }

  public FromClauseBuilder metricExpression(Pattern metricNamePattern) {
    metricExpressions.add(new RegexMetricExpression(metricNamePattern));
    return this;
  }

  public FromClause build() {
    return new FromClause(metricExpressions);
  }
}
