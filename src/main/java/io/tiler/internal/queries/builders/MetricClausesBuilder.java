package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.metrics.MetricClauses;
import io.tiler.internal.queries.clauses.metrics.SelectClause;
import io.tiler.internal.queries.clauses.metrics.SortClause;

public class MetricClausesBuilder {
  private SelectClause selectClause;
  private SortClause sortClause;

  public MetricClauses build() {
    return new MetricClauses(selectClause, sortClause);
  }

  public MetricClausesBuilder selectClause(SelectClause selectClause) {
    this.selectClause = selectClause;
    return this;
  }

  public MetricClausesBuilder sortClause(SortClause sortClause) {
    this.sortClause = sortClause;
    return this;
  }
}
