package io.tiler.internal.queries.expressions;

public abstract class ComparisonOperation extends OperationWithArguments {
  public ComparisonOperation(Expression argument) {
    super(argument);
  }

  protected int compare(Object x, Object y) throws InvalidExpressionException {
    if (x == null && y == null) {
      return 0;
    }

    if (x == null) {
      return -1;
    }

    if (y == null) {
      return 1;
    }

    if (TypeHelper.areBothStrings(x, y)) {
      return compareStrings((String) x, (String) y);
    }

    if (TypeHelper.areBothBooleans(x, y)) {
      return compareBooleans((Boolean) x, (Boolean) y);
    }

    if (TypeHelper.areBothNumbers(x, y)) {
      return compareNumbers((Number) x, (Number) y);
    }

    throw new InvalidExpressionException("Expressions are not comparable '" + x.getClass().getName() + "', '" + y.getClass().getName() + "'");
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
