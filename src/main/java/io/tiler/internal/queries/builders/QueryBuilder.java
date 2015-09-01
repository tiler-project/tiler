package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.*;
import io.tiler.internal.queries.clauses.AggregateClause;
import io.tiler.internal.queries.clauses.FromClause;
import io.tiler.internal.queries.clauses.GroupClause;
import io.tiler.internal.queries.clauses.WhereClause;
import io.tiler.internal.queries.clauses.metrics.MetricClauses;
import io.tiler.internal.queries.clauses.points.PointClauses;
import io.tiler.internal.queries.clauses.points.SelectClause;

public class QueryBuilder {
  private FromClause fromClause;
  private WhereClause whereClause;
  private GroupClause groupClause;
  private AggregateClause aggregateClause;
  private PointClausesBuilder pointClausesBuilder = new PointClausesBuilder();
  private MetricClausesBuilder metricClausesBuilder = new MetricClausesBuilder();

  public Query build() {
    return new Query(
      fromClause,
      whereClause,
      groupClause,
      aggregateClause,
      pointClausesBuilder.build(),
      metricClausesBuilder.build());
  }

  public void fromClause(FromClause fromClause) {
    this.fromClause = fromClause;
  }

  public void whereClause(WhereClause whereClause) {
    this.whereClause = whereClause;
  }

  public void groupClause(GroupClause groupClause) {
    this.groupClause = groupClause;
  }

  public void aggregateClause(AggregateClause aggregateClause) {
    this.aggregateClause = aggregateClause;
  }

  public PointClausesBuilder pointClauses() {
    return pointClausesBuilder;
  }

  public MetricClausesBuilder metricClauses() {
    return metricClausesBuilder;
  }
}
