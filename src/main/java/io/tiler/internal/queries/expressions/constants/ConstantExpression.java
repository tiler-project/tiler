package io.tiler.internal.queries.expressions.constants;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class ConstantExpression<T> extends Expression {
  private final T value;

  public ConstantExpression(QueryContext queryContext, T value) {
    super(queryContext);
    this.value = value;
  }

  public T value() {
    return value;
  }

  @Override
  public Object evaluate(EvaluationContext context) {
    return value();
  }
}
