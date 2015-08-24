package io.tiler.internal.queries;

import org.vertx.java.core.json.JsonArray;

public class Query {
  private FromClause fromClause;
  private WhereClause whereClause;
  private GroupClause groupClause;
  private AggregateClause aggregateClause;
  private final PointClause pointClause;
  private final MetricClause metricClause;

  public Query(FromClause fromClause, WhereClause whereClause, GroupClause groupClause, AggregateClause aggregateClause, PointClause pointClause, MetricClause metricClause) {
    this.fromClause = fromClause;
    this.whereClause = whereClause;
    this.groupClause = groupClause;
    this.aggregateClause = aggregateClause;
    this.pointClause = pointClause;
    this.metricClause = metricClause;
  }

  public FromClause fromClause() {
    return fromClause;
  }

  public WhereClause whereClause() {
    return whereClause;
  }

  public GroupClause groupClause() {
    return groupClause;
  }

  public AggregateClause aggregateClause() { return aggregateClause; }

  public PointClause pointClause() {
    return pointClause;
  }

  public MetricClause metricClause() {
    return metricClause;
  }

  public boolean hasWhereClause() {
    return whereClause != null;
  }

  public boolean hasGroupClause() {
    return groupClause != null;
  }

  public boolean hasAggregateClause() {
    return aggregateClause != null;
  }

  public boolean hasPointClause() {
    return pointClause != null;
  }

  public boolean hasMetricClause() {
    return metricClause != null;
  }

  public JsonArray applyToMetrics(JsonArray metrics) throws EvaluationException {
    JsonArray transformedMetrics = copyMetrics(metrics);

    if (hasWhereClause()) {
      whereClause.applyToMetrics(transformedMetrics);
    }

    if (hasGroupClause()) {
      transformedMetrics = groupClause.applyToMetrics(transformedMetrics);
    }

    if (hasAggregateClause()) {
      aggregateClause.applyToMetrics(transformedMetrics);
    }

    if (hasPointClause()) {
      pointClause.applyToMetrics(transformedMetrics);
    }

    if (hasMetricClause()) {
      transformedMetrics = metricClause.applyToMetrics(transformedMetrics);
    }

    return transformedMetrics;
  }

  private JsonArray copyMetrics(JsonArray metrics) {
    return metrics.copy();
  }
}
