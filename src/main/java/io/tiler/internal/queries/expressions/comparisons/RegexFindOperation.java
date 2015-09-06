package io.tiler.internal.queries.expressions.comparisons;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.BinaryOperation;
import io.tiler.internal.queries.expressions.Expression;

import java.util.regex.Pattern;

public class RegexFindOperation extends BinaryOperation {
  public RegexFindOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object operand1 = operand1().evaluate(context);
    Object operand2 = operand2().evaluate(context);

    if (operand1 == null) {
      throw new EvaluationException(queryContext(), "operand1 cannot be null");
    }

    if (!(operand1 instanceof String)) {
      throw new EvaluationException(queryContext(), "operand1 must be a String");
    }

    if (operand2 == null) {
      throw new EvaluationException(queryContext(), "operand2 cannot be null");
    }

    if (!(operand2 instanceof Pattern)) {
      throw new EvaluationException(queryContext(), "operand2 must be a regular expression Pattern");
    }

    String operand1String = (String) operand1;
    Pattern operand2Pattern = (Pattern) operand2;

    return operand2Pattern.matcher(operand1String).find();
  }
}
