package io.tiler.internal.queries.expressions.aggregations;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.functions.Function;

public abstract class AggregateExpression extends Function {
  public AggregateExpression(QueryContext queryContext) {
    super(queryContext);
  }
}
