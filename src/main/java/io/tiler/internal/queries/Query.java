package io.tiler.internal.queries;

import io.tiler.internal.queries.clauses.AggregateClause;
import io.tiler.internal.queries.clauses.FromClause;
import io.tiler.internal.queries.clauses.GroupClause;
import io.tiler.internal.queries.clauses.WhereClause;
import io.tiler.internal.queries.clauses.metrics.MetricClauses;
import io.tiler.internal.queries.clauses.points.PointClauses;
import org.vertx.java.core.json.JsonArray;

import java.time.Clock;

public class Query {
  private final FromClause fromClause;
  private final WhereClause whereClause;
  private final GroupClause groupClause;
  private final AggregateClause aggregateClause;
  private final PointClauses pointClauses;
  private final MetricClauses metricClauses;

  public Query(FromClause fromClause, WhereClause whereClause, GroupClause groupClause, AggregateClause aggregateClause, PointClauses pointClauses, MetricClauses metricClauses) {
    this.fromClause = fromClause;
    this.whereClause = whereClause;
    this.groupClause = groupClause;
    this.aggregateClause = aggregateClause;
    this.pointClauses = pointClauses;
    this.metricClauses = metricClauses;
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

  public PointClauses pointClauses() {
    return pointClauses;
  }

  public MetricClauses metricClauses() {
    return metricClauses;
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

  public JsonArray applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    JsonArray transformedMetrics = copyMetrics(metrics);

    if (hasWhereClause()) {
      whereClause.applyToMetrics(clock, transformedMetrics);
    }

    if (hasGroupClause()) {
      transformedMetrics = groupClause.applyToMetrics(transformedMetrics);
    }

    if (hasAggregateClause()) {
      aggregateClause.applyToMetrics(clock, transformedMetrics);
    }

    pointClauses.applyToMetrics(clock, transformedMetrics);
    transformedMetrics = metricClauses.applyToMetrics(clock, transformedMetrics);

    return transformedMetrics;
  }

  private JsonArray copyMetrics(JsonArray metrics) {
    return metrics.copy();
  }
}
