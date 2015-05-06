package io.tiler.internal.queries.expressions;

public class AndOperation extends LogicalOperation {
  public AndOperation(Iterable<Expression> arguments) {
    super(arguments);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    for (Expression argument : getArguments()) {
      if (!evaluatesToTrue(argument.evaluate(leftHandValue))) {
        return false;
      }
    }

    return true;
  }
}
