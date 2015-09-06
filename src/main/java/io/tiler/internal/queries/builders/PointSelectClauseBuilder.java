package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.clauses.points.SelectClause;
import io.tiler.internal.queries.expressions.Expression;

import java.util.HashMap;

public class PointSelectClauseBuilder {
  HashMap<String, Expression> namedExpressions = new HashMap<>();

  public void namedExpression(String name, Expression expression) {
    namedExpressions.put(name, expression);
  }
  
  public SelectClause build() {
    return new SelectClause(namedExpressions);
  }
}
