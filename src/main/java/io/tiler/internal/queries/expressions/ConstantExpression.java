package io.tiler.internal.queries.expressions;

import io.tiler.time.TimePeriodParser;

public class ConstantExpression extends Expression {
  private Object value;

  public ConstantExpression(Object value) {
    super();
    this.value = transformValue(value);
  }

  private Object transformValue(Object value) {
    if (value instanceof String) {
      String string = (String)value;

      if (TimePeriodParser.isATimePeriod(string)) {
        return TimePeriodParser.parseTimePeriodToMicroseconds(string);
      }
    }

    return value;
  }

  @Override
  public Object evaluate(Object leftHandValue) {
    return value;
  }
}
