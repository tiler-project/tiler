package io.tiler.internal.queries;

import io.tiler.internal.queries.expressions.Expression;

public class AggregateField {
  private final String fieldName;
  private final Expression expression;

  public AggregateField(String fieldName) {
    this.fieldName = fieldName;
    this.expression = null;
  }

  public AggregateField(String fieldName, Expression expression) {
    this.fieldName = fieldName;
    this.expression = expression;
  }

  public boolean hasExpression() {
    return expression != null;
  }

  public String fieldName() {
    return fieldName;
  }

  public Expression expression() {
    return expression;
  }
}
