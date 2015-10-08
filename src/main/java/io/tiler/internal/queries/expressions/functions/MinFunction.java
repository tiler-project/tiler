package io.tiler.internal.queries.expressions.functions;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

public class MinFunction extends ListFunction<Number> {
  public MinFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
  }

  @Override
  public Object applyToList(JsonArray list) {
    Double min = null;

    for (Number value : new JsonArrayIterable<Number>(list)) {
      double doubleValue = value.doubleValue();

      if (min == null || doubleValue < min) {
        min = doubleValue;
      }
    }

    return min == null ? Double.NaN : min;
  }
}
