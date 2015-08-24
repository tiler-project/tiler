package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.AggregateClause;
import io.tiler.internal.queries.expressions.aggregations.AggregateExpression;

import java.util.HashMap;

public class AggregateClauseBuilder {
  HashMap<String, AggregateExpression> namedAggregateExpressions = new HashMap<>();

  public void namedAggregateExpression(String name, AggregateExpression aggregateExpression) {
    namedAggregateExpressions.put(name, aggregateExpression);
  }

  public AggregateClause build() {
    return new AggregateClause(namedAggregateExpressions);
  }
}
