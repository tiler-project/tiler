package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.SortExpression;
import io.tiler.internal.queries.clauses.metrics.SortClause;

import java.util.ArrayList;

public class MetricSortClauseBuilder {
  ArrayList<SortExpression> sortExpressions = new ArrayList<>();

  public void sortExpression(SortExpression sortExpression) {
    sortExpressions.add(sortExpression);
  }

  public SortClause build() {
    return new SortClause(sortExpressions);
  }
}
