package io.tiler.internal.queries.expressions.aggregations;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public abstract class AggregateExpression extends Expression {
  public AggregateExpression(QueryContext queryContext) {
    super(queryContext);
  }
}
