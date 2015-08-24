package io.tiler.internal.queries.expressions.logical;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class OrOperation extends LogicalBinaryOperation {
  public OrOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    return evaluateOperand1(context) || evaluateOperand2(context);
  }
}
