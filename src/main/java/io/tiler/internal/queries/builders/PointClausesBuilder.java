package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.points.PointClauses;
import io.tiler.internal.queries.clauses.points.SelectClause;
import io.tiler.internal.queries.clauses.points.SortClause;

public class PointClausesBuilder {
  private SelectClause selectClause;
  private SortClause sortClause;

  public PointClauses build() {
    return new PointClauses(selectClause, sortClause);
  }

  public PointClausesBuilder selectClause(SelectClause selectClause) {
    this.selectClause = selectClause;
    return this;
  }

  public PointClausesBuilder sortClause(SortClause sortClause) {
    this.sortClause = sortClause;
    return this;
  }
}
