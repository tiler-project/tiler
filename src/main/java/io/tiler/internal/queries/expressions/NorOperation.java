package io.tiler.internal.queries.expressions;

public class NorOperation extends LogicalOperation {
  public NorOperation(Iterable<Expression> expressions) {
    super(expressions);
  }

  @Override
  public Object evaluate(Object leftHandValue) throws InvalidExpressionException {
    for (Expression argument : getArguments()) {
      if (evaluatesToTrue(argument.evaluate(leftHandValue))) {
        return false;
      }
    }

    return true;
  }
}
