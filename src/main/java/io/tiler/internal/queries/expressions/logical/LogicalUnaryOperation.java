package io.tiler.internal.queries.expressions.logical;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.BinaryOperation;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.UnaryOperation;

public abstract class LogicalUnaryOperation extends UnaryOperation {
  public LogicalUnaryOperation(QueryContext queryContext, Expression operand) {
    super(queryContext, operand);
  }

  protected boolean evaluateOperand(EvaluationContext context) throws EvaluationException {
    Object value = operand().evaluate(context);
    checkOperand("operand", value);
    return (boolean) value;
  }

  private void checkOperand(String name, Object value) throws EvaluationException {
    if (value == null) {
      throw new EvaluationException(queryContext(), name + " cannot be null");
    }

    if (!(value instanceof Boolean)) {
      throw new EvaluationException(queryContext(), name + " must be a '" + Boolean.class.getName() + "' but is actually a '" + value.getClass().getName() + "'");
    }
  }
}
