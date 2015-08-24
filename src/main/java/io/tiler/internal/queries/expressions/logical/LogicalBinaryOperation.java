package io.tiler.internal.queries.expressions.logical;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.BinaryOperation;
import io.tiler.internal.queries.expressions.Expression;

public abstract class LogicalBinaryOperation extends BinaryOperation {
  public LogicalBinaryOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  protected boolean evaluateOperand1(EvaluationContext context) throws EvaluationException {
    Object value = operand1().evaluate(context);
    checkOperand("operand1", value);
    return (boolean) value;
  }

  protected boolean evaluateOperand2(EvaluationContext context) throws EvaluationException {
    Object value = operand2().evaluate(context);
    checkOperand("operand2", value);
    return (boolean) value;
  }

  private void checkOperand(String name, Object value) throws EvaluationException {
    if (value == null) {
      throw new EvaluationException(queryContext(), name + " cannot be null");
    }

    if (!(value instanceof Boolean)) {
      throw new EvaluationException(queryContext(), name + " must be a boolean but is actually a '" + value.getClass().getName() + "'");
    }
  }
}
