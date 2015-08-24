package io.tiler.internal.queries;

import java.math.BigDecimal;
import java.util.List;

public class TypeHelper {
  public static boolean areBothNumbers(Object x, Object y) {
    return isANumber(x) && isANumber(y);
  }

  public static boolean isAString(Object value) {
    return value instanceof String;
  }

  public static boolean isANumber(Object value) {
    return value instanceof Number;
  }

  public static boolean isABoolean(Object value) {
    return value instanceof Boolean;
  }

  public static boolean isAFloatingPointNumber(Number value) {
    return value instanceof Double || value instanceof Float;
  }

  public static boolean areBothBooleans(Object x, Object y) {
    return isABoolean(x) && isABoolean(y);
  }

  public static boolean areBothStrings(Object x, Object y) {
    return isAString(x) && isAString(y);
  }

  public static boolean containsAFloatingPointNumber(List<Number> values) {
    for (Number value : values) {
      if (isAFloatingPointNumber(value)) {
        return true;
      }
    }

    return false;
  }

  public static BigDecimal numberToBigDecimal(Number value) {
    if (isAFloatingPointNumber(value)) {
      return new BigDecimal(value.doubleValue());
    }
    else {
      return new BigDecimal(value.longValue());
    }
  }
}
