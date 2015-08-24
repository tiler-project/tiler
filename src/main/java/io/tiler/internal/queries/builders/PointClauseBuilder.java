package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.PointClause;
import io.tiler.internal.queries.expressions.Expression;

import java.util.HashMap;

public class PointClauseBuilder {
  HashMap<String, Expression> namedExpressions = new HashMap<>();

  public void namedExpression(String name, Expression Expression) {
    namedExpressions.put(name, Expression);
  }
  
  public PointClause build() {
    return new PointClause(namedExpressions);
  }
}
