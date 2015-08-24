package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.*;

public class QueryBuilder {
  private FromClause fromClause;
  private WhereClause whereClause;
  private GroupClause groupClause;
  private AggregateClause aggregateClause;
  private PointClause pointClause;
  private MetricClause metricClause;

  public Query build() {
    return new Query(fromClause, whereClause, groupClause, aggregateClause, pointClause, metricClause);
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

  public void pointClause(PointClause pointClause) {
    this.pointClause = pointClause;
  }

  public void metricClause(MetricClause metricClause) {
    this.metricClause = metricClause;
  }
}
