package io.tiler.internal.queries.clauses;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.util.*;

public class BaseSortClause {
  private final Comparator<Item> comparator;
  private List<SortExpression> sortExpressions;

  public BaseSortClause(ArrayList<SortExpression> sortExpressions) {
    this.sortExpressions = Collections.unmodifiableList(sortExpressions);
    comparator = createComparator(sortExpressions);
  }

  private Comparator<Item> createComparator(ArrayList<SortExpression> sortExpressions) {
    Comparator<Item> compositeComparator = null;

    for (int i = 0, count = sortExpressions.size(); i < count; i++) {
      ItemComparator comparator = new ItemComparator(i, sortExpressions.get(i).sortDirection());

      if (compositeComparator == null) {
        compositeComparator = comparator;
      }
      else {
        compositeComparator = compositeComparator.thenComparing(comparator);
      }
    }

    return compositeComparator;
  }

  public List<SortExpression> sortExpressions() {
    return sortExpressions;
  }

  public JsonArray applyToItems(Clock clock, JsonArray metrics) throws EvaluationException {
    ArrayList<Item> items = convertJsonArrayToItems(clock, metrics);

    try {
      Collections.sort(items, comparator);
    }
    catch (RuntimeException e) {
      Throwable cause = e.getCause();

      if (cause != null && cause instanceof EvaluationException) {
        EvaluationException realException = (EvaluationException) cause;
        throw realException;
      }

      throw e;
    }

    return convertItemsToJsonArray(items);
  }

  private ArrayList<Item> convertJsonArrayToItems(Clock clock, JsonArray metrics) {
    ArrayList<Item> items = new ArrayList<>();

    for (JsonObject fields : new JsonArrayIterable<JsonObject>(metrics)) {
      items.add(new Item(clock, fields));
    }
    return items;
  }

  private JsonArray convertItemsToJsonArray(ArrayList<Item> items) {
    JsonArray jsonArray = new JsonArray();

    for (Item item : items) {
      jsonArray.addObject(item.fields);
    }

    return jsonArray;
  }

  private class Item {
    private final Clock clock;
    private final JsonObject fields;
    private HashMap<Integer, Object> sortValues = new HashMap<>();

    public Item(Clock clock, JsonObject fields) {
      this.clock = clock;
      this.fields = fields;
    }

    public Object getSortValue(int sortExpressionIndex) throws EvaluationException {
      if (sortValues.containsKey(sortExpressionIndex)) {
        return sortValues.get(sortExpressionIndex);
      }

      Expression expression = sortExpressions.get(sortExpressionIndex).expression();
      Object value = expression.evaluate(new EvaluationContext(clock, fields));
      sortValues.put(sortExpressionIndex, value);
      return value;
    }
  }

  public class ItemComparator implements Comparator<Item> {
    private final int sortExpressionIndex;
    private final SortDirection sortDirection;

    public ItemComparator(int sortExpressionIndex, SortDirection sortDirection) {
      this.sortExpressionIndex = sortExpressionIndex;
      this.sortDirection = sortDirection;
    }

    private int innerCompare(Item o1, Item o2) throws EvaluationException {
      Object value1 = o1.getSortValue(sortExpressionIndex);
      Object value2 = o2.getSortValue(sortExpressionIndex);

      if (value1 == null) {
        if (value2 == null) {
          return 0;
        }
        else {
          return -1;
        }
      }
      else if (value2 == null) {
        return 1;
      }

      checkValueIsComparable(value1);
      checkValueIsComparable(value2);

      Comparable comparable1 = (Comparable) value1;
      Comparable comparable2 = (Comparable) value2;

      return comparable1.compareTo(comparable2);
    }

    private void checkValueIsComparable(Object value) throws EvaluationException {
      if (!(value instanceof Comparable)) {
        throw new EvaluationException(
          sortExpressions.get(sortExpressionIndex).queryContext(),
          "Object does not implement the Comparable interface");
      }
    }

    @Override
    public int compare(Item o1, Item o2) {
      int result;

      try {
        result = innerCompare(o1, o2);
      }
      catch (EvaluationException e) {
        throw new RuntimeException("Wrapped EvaluationException", e);
      }

      return sortDirection == SortDirection.Descending ? -result : result;
    }
  }
}
