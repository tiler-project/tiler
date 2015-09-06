package io.tiler.internal.queries.expressions.logical;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class NotOperation extends LogicalUnaryOperation {
  public NotOperation(QueryContext queryContext, Expression operand) {
    super(queryContext, operand);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    return !evaluateOperand(context);
  }
}
