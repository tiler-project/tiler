package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.metrics.SelectClause;
import io.tiler.internal.queries.expressions.Expression;

import java.util.HashMap;

public class MetricClauseBuilder {
  HashMap<String, Expression> namedExpressions = new HashMap<>();

  public void namedExpression(String name, Expression Expression) {
    namedExpressions.put(name, Expression);
  }

  public SelectClause build() {
    return new SelectClause(namedExpressions);
  }
}
