package io.tiler.internal.queries.expressions;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpressionFactory {
  private static final String GREATER_THAN_OPERATOR = "$gt";
  private static final String GREATER_THAN_OR_EQUAL_TO_OPERATOR = "$gte";
  private static final String LESS_THAN_OPERATOR = "$lt";
  private static final String LESS_THAN_OR_EQUAL_TO_OPERATOR = "$lte";
  private static final String EQUALS_OPERATOR = "$eq";
  private static final String NOT_EQUALS_OPERATOR = "$ne";
  private static final String NOW_OPERATOR = "$now";
  private static final String MEAN_OPERATOR = "$mean";
  private static final String SUM_OPERATOR = "$sum";
  private static final String MIN_OPERATOR = "$min";
  private static final String MAX_OPERATOR = "$max";
  private static final String FIRST_OPERATOR = "$first";
  private static final String LAST_OPERATOR = "$last";
  private static final String MINUS_OPERATOR = "$minus";
  private static final String AND_OPERATOR = "$and";
  private static final String OR_OPERATOR = "$or";
  private static final String NOR_OPERATOR = "$nor";
  private static final String NOT_OPERATOR = "$not";
  private static final String INTERVALS_OPERATOR = "$intervals";
  private static final String ALL_OPERATOR = "$all";
  private static final String REGEX_OPERATOR = "$regex";
  private static final String REPLACE_OPERATOR = "$replace";

  private static Iterable<Expression> createExpressionsFromJsonObject(JsonObject jsonObject) throws InvalidExpressionException {
    ArrayList<Expression> expressions = new ArrayList<>();

    for (String fieldName : jsonObject.getFieldNames()) {
      expressions.add(createOperationWithArguments(fieldName, jsonObject.getField(fieldName)));
    }

    return expressions;
  }

  private static Operation createOperationWithArguments(String operator, Object jsonExpression) throws InvalidExpressionException {
    switch (operator) {
      case AND_OPERATOR:
        checkIsJsonArray(jsonExpression);
        return new AndOperation(createArgumentsFromJsonArray((JsonArray) jsonExpression));
      case OR_OPERATOR:
        checkIsJsonArray(jsonExpression);
        return new OrOperation(createArgumentsFromJsonArray((JsonArray) jsonExpression));
      case NOR_OPERATOR:
        checkIsJsonArray(jsonExpression);
        return new NorOperation(createArgumentsFromJsonArray((JsonArray) jsonExpression));
      case MINUS_OPERATOR:
        checkIsJsonArray(jsonExpression);
        return new MinusOperation(createArgumentsFromJsonArray((JsonArray) jsonExpression));
      case INTERVALS_OPERATOR:
        checkIsJsonObject(jsonExpression);
        return new IntervalsOperation(createNamedArgumentsFromJsonObject((JsonObject) jsonExpression));
      case REGEX_OPERATOR:
        checkIsJsonObject(jsonExpression);
        return new RegexOperation(createNamedArgumentsFromJsonObject((JsonObject) jsonExpression));
      case REPLACE_OPERATOR:
        checkIsJsonObject(jsonExpression);
        return new ReplaceOperation(createNamedArgumentsFromJsonObject((JsonObject) jsonExpression));
      case NOT_OPERATOR:
        return new NotOperation(createExpressionFromJsonExpression(jsonExpression));
      case EQUALS_OPERATOR:
        return new EqualsOperation(createExpressionFromJsonExpression(jsonExpression));
      case NOT_EQUALS_OPERATOR:
        return new NotEqualsOperation(createExpressionFromJsonExpression(jsonExpression));
      case GREATER_THAN_OPERATOR:
        return new GreaterThanOperation(createExpressionFromJsonExpression(jsonExpression));
      case GREATER_THAN_OR_EQUAL_TO_OPERATOR:
        return new GreaterThanOrEqualOperation(createExpressionFromJsonExpression(jsonExpression));
      case LESS_THAN_OPERATOR:
        return new LessThanOperation(createExpressionFromJsonExpression(jsonExpression));
      case LESS_THAN_OR_EQUAL_TO_OPERATOR:
        return new LessThanOrEqualOperation(createExpressionFromJsonExpression(jsonExpression));
      default:
        throw new InvalidExpressionException("Invalid operator " + operator);
    }
  }

  private static Operation createOperationWithNoArguments(String operator) throws InvalidExpressionException {
    switch (operator) {
      case NOW_OPERATOR:
        return new NowOperation();
      case MEAN_OPERATOR:
        return new MeanOperation();
      case SUM_OPERATOR:
        return new SumOperation();
      case MIN_OPERATOR:
        return new MinOperation();
      case MAX_OPERATOR:
        return new MaxOperation();
      case FIRST_OPERATOR:
        return new FirstOperation();
      case LAST_OPERATOR:
        return new LastOperation();
      case ALL_OPERATOR:
        return new AllOperation();
      default:
        throw new InvalidExpressionException("Invalid operator " + operator);
    }
  }

  public static Expression createExpressionFromJsonExpression(Object value) throws InvalidExpressionException {
    if (value == null) {
      return new ConstantExpression(null);
    }

    if (value instanceof JsonObject) {
      JsonObject jsonObject = (JsonObject) value;

      switch (jsonObject.size()) {
        case 0:
          throw new InvalidExpressionException("Empty object");
        case 1:
          String fieldName = JsonHelper.getFirstFieldNameFromJsonObject(jsonObject);
          return createOperationWithArguments(fieldName, jsonObject.getValue(fieldName));
        default:
          return new AndOperation(createExpressionsFromJsonObject(jsonObject));
      }
    }

    if (value instanceof String) {
      String string = (String) value;

      if (string.startsWith("$")) {
        return createOperationWithNoArguments(string);
      }
    }

    return new ConstantExpression(value);
  }

  private static void checkIsJsonArray(Object value) throws InvalidExpressionException {
    if (!(value instanceof JsonArray)) {
      throw new InvalidExpressionException("Expressions must be in a JsonArray");
    }
  }

  private static void checkIsJsonObject(Object value) throws InvalidExpressionException {
    if (!(value instanceof JsonObject)) {
      throw new InvalidExpressionException("Expressions must be in a JsonObject");
    }
  }

  private static Iterable<Expression> createArgumentsFromJsonArray(JsonArray value) throws InvalidExpressionException {
    ArrayList<Expression> expressions = new ArrayList<>();

    for (Object item : value) {
      expressions.add(createExpressionFromJsonExpression(item));
    }

    return expressions;
  }

  private static Iterable<Map.Entry<String, Expression>> createNamedArgumentsFromJsonObject(JsonObject value) throws InvalidExpressionException {
    HashMap<String, Expression> expressions = new HashMap<>();

    for (String fieldName : value.getFieldNames()) {
      expressions.put(fieldName, createExpressionFromJsonExpression(value.getValue(fieldName)));
    }

    return expressions.entrySet();
  }
}
