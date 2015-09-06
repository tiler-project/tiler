package io.tiler.internal.queries.clauses;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class SortExpression {
  private final QueryContext queryContext;
  private Expression expression;
  private SortDirection sortDirection;

  public SortExpression(QueryContext queryContext, Expression expression, SortDirection sortDirection) {
    this.queryContext = queryContext;
    this.expression = expression;
    this.sortDirection = sortDirection;
  }

  public QueryContext queryContext() { return queryContext; }

  public Expression expression() {
    return expression;
  }

  public SortDirection sortDirection() {
    return sortDirection;
  }
}
