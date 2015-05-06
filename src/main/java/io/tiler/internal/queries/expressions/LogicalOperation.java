package io.tiler.internal.queries.expressions;

public abstract class LogicalOperation extends OperationWithArguments {
  public LogicalOperation(Iterable<Expression> arguments) {
    super(arguments);
  }

  public LogicalOperation(Expression argument) {
    super(argument);
  }

  protected boolean evaluatesToTrue(Object value) {
    if (value == null || !(value instanceof Boolean)) {
      return false;
    }

    return (boolean) value;
  }
}
