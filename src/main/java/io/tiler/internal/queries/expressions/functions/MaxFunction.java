package io.tiler.internal.queries.expressions.functions;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

public class MaxFunction extends ListFunction<Number> {
  public MaxFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
  }

  @Override
  public Object applyToList(JsonArray list) {
    Double max = null;

    for (Number value : new JsonArrayIterable<Number>(list)) {
      double doubleValue = value.doubleValue();

      if (max == null || doubleValue > max) {
        max = doubleValue;
      }
    }

    return max == null ? Double.NaN : max;
  }
}
