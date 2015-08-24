package io.tiler.internal.queries;

public class SimpleMetricExpression extends MetricExpression {
  private String metricName;

  public SimpleMetricExpression(String metricName) {
    this.metricName = metricName;
  }

  public String metricName() {
    return metricName;
  }
}
