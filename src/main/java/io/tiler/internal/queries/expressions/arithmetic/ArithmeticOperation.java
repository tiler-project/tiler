package io.tiler.internal.queries.expressions.arithmetic;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.TypeHelper;
import io.tiler.internal.queries.expressions.BinaryOperation;
import io.tiler.internal.queries.expressions.Expression;

public abstract class ArithmeticOperation extends BinaryOperation {
  public ArithmeticOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object operand1 = operand1().evaluate(context);
    Object operand2 = operand2().evaluate(context);

    if (operand1 == null) {
      throw new EvaluationException(queryContext(), "operand1 cannot be null");
    }

    if (!(operand1 instanceof Number)) {
      throw new EvaluationException(queryContext(), "operand1 must be a Number");
    }

    if (operand2 == null) {
      throw new EvaluationException(queryContext(), "operand2 cannot be null");
    }

    if (!(operand2 instanceof Number)) {
      throw new EvaluationException(queryContext(), "operand2 must be a Number");
    }

    double operand1Double = ((Number) operand1).doubleValue();
    double operand2Double = ((Number) operand2).doubleValue();

    return performArithmetic(operand1Double, operand2Double);
  }

  protected abstract double performArithmetic(double operand1, double operand2);
}
