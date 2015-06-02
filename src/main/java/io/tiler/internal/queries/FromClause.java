package io.tiler.internal.queries;

import io.tiler.json.JsonArrayIterable;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.ExpressionFactory;
import io.tiler.internal.queries.expressions.InvalidExpressionException;
import io.tiler.internal.queries.expressions.RegularExpressionOperation;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FromClause {
  private final ArrayList<Object> items;
  private final boolean containsAtLeastOnePattern;

  protected FromClause() {
    items = new ArrayList<>();
    containsAtLeastOnePattern = false;
  }

  protected FromClause(Collection<Object> items, boolean containsAtLeastOnePattern) {
    this.items = new ArrayList<>(items);
    this.containsAtLeastOnePattern = containsAtLeastOnePattern;
  }

  public static FromClause fromJsonExpression(Object jsonExpression) throws InvalidQueryException {
    if (jsonExpression == null) {
      return new FromClause();
    }

    JsonArray jsonItems;

    if (jsonExpression instanceof JsonArray) {
      jsonItems = (JsonArray)jsonExpression;
    }
    else {
      jsonItems = new JsonArray();
      jsonItems.add(jsonExpression);
    }

    ArrayList<Object> items = new ArrayList<>();
    boolean containsAtLeastOnePattern = false;

    for (Object jsonItem : jsonItems) {
      Expression expression;

      try {
        expression = ExpressionFactory.createExpressionFromJsonExpression(jsonItem);
      } catch (InvalidExpressionException e) {
        throw new InvalidQueryException("Invalid from clause in query", e);
      }

      if (expression instanceof RegularExpressionOperation) {
        containsAtLeastOnePattern = true;
        RegularExpressionOperation regularExpressionOperation = (RegularExpressionOperation) expression;

        items.add(regularExpressionOperation.pattern());
      } else {
        Object value;

        try {
          value = expression.evaluate(null);
        } catch (InvalidExpressionException e) {
          throw new InvalidQueryException("Invalid from clause in query", e);
        }

        if (!(value instanceof String)) {
          throw new InvalidQueryException("Items in from clause must be strings or regular expressions");
        }

        items.add(value);
      }
    }

    return new FromClause(items, containsAtLeastOnePattern);
  }

  public List<Object> items() {
    return items;
  }

  public boolean isPotentiallyMissingAnyMetrics(Set<String> metricNames) {
    if (containsAtLeastOnePattern) {
      return true;
    }

    for (Object item : items) {
      if (!metricNames.contains(item)) {
        return true;
      }
    }

    return false;
  }

  public boolean matchesMetricName(String metricName) {
    for (Object item : items) {
      if (item instanceof String) {
        if (metricName.equals(item)) {
          return true;
        }
      } else {
        Pattern pattern = (Pattern) item;

        if (pattern.matcher(metricName).matches()) {
          return true;
        }
      }
    }

    return false;
  }

  public JsonArray findMatchingMetrics(JsonArray metrics) {
    JsonArray matchingMetrics = new JsonArray();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      if (matchesMetricName(metric.getString("name"))) {
        matchingMetrics.addObject(metric);
      }
    }

    return matchingMetrics;
  }
}
