package io.tiler.internal.queries.expressions;

public class OrOperation extends LogicalOperation {
  public OrOperation(Iterable<Expression> arguments) {
    super(arguments);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    for (Expression argument : getArguments()) {
      if (evaluatesToTrue(argument.evaluate(leftHandValue))) {
        return true;
      }
    }

    return false;
  }
}
