package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.WhereClause;
import io.tiler.internal.queries.expressions.Expression;

public class WhereClauseBuilder {
  private Expression expression;

  public WhereClauseBuilder expression(Expression expression) {
    this.expression = expression;
    return this;
  }

  public WhereClause build() {
    return new WhereClause(expression);
  }
}
