package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.AggregateClause;
import io.tiler.internal.queries.expressions.Expression;

import java.util.HashMap;

public class AggregateClauseBuilder {
  HashMap<String, Expression> namedExpressions = new HashMap<>();

  public void namedExpression(String name, Expression Expression) {
    namedExpressions.put(name, Expression);
  }

  public AggregateClause build() {
    return new AggregateClause(namedExpressions);
  }
}
