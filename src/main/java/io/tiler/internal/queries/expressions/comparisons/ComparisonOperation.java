package io.tiler.internal.queries.expressions.comparisons;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.TypeHelper;
import io.tiler.internal.queries.expressions.BinaryOperation;
import io.tiler.internal.queries.expressions.Expression;

public abstract class ComparisonOperation extends BinaryOperation {
  public ComparisonOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    return evaluateComparisonResult(compare(context));
  }

  protected abstract boolean evaluateComparisonResult(int comparisonResult);

  private int compare(EvaluationContext context) throws EvaluationException {
    Object operand1 = operand1().evaluate(context);
    Object operand2 = operand2().evaluate(context);

    if (operand1 == null) {
      throw new EvaluationException(queryContext(), "operand1 cannot be null");
    }

    if (operand2 == null) {
      throw new EvaluationException(queryContext(), "operand2 cannot be null");
    }

    if (TypeHelper.areBothStrings(operand1, operand2)) {
      return compareStrings((String) operand1, (String) operand2);
    }

    if (TypeHelper.areBothBooleans(operand1, operand2)) {
      return compareBooleans((Boolean) operand1, (Boolean) operand2);
    }

    if (TypeHelper.areBothNumbers(operand1, operand2)) {
      return compareNumbers((Number) operand1, (Number) operand2);
    }

    throw
      new EvaluationException(queryContext(), "Expressions are not comparable '" + operand1.getClass().getName() + "', '" + operand2.getClass().getName() + "'");
  }
  
  private int compareNumbers(Number x, Number y) {
    if (TypeHelper.isAFloatingPointNumber(x) || TypeHelper.isAFloatingPointNumber(y)) {
      return Double.compare(x.doubleValue(), y.doubleValue());
    }
    else {
      return Long.compare(x.longValue(), y.longValue());
    }
  }

  private int compareBooleans(Boolean x, Boolean y) {
    return Boolean.compare(x, y);
  }

  private int compareStrings(String x, String y) {
    return x.compareTo(y);
  }
}
