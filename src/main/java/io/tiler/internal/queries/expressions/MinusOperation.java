package io.tiler.internal.queries.expressions;

import java.util.ArrayList;

public class MinusOperation extends OperationWithArguments {
  public MinusOperation(Iterable<Expression> arguments) {
    super(arguments);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    ArrayList<Number> numbers = new ArrayList<>();

    for (Expression argument : getArguments()) {
      Object evaluatedArgument = argument.evaluate(leftHandValue);

      if (!TypeHelper.isANumber(evaluatedArgument)) {
        throw new InvalidExpressionException("Minus operation only supports numeric arguments");
      }

      numbers.add((Number)evaluatedArgument);
    }

    if (TypeHelper.containsAFloatingPointNumber(numbers)) {
      double result = numbers.get(0).doubleValue();

      for (Number number : numbers.subList(1, numbers.size())) {
        result -= number.doubleValue();
      }

      return result;
    }
    else {
      long result = numbers.get(0).longValue();

      for (Number number : numbers.subList(1, numbers.size())) {
        result -= number.longValue();
      }

      return result;
    }
  }
}
