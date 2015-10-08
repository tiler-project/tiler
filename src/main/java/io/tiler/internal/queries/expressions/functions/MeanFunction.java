package io.tiler.internal.queries.expressions.functions;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

public class MeanFunction extends ListFunction<Number> {
  public MeanFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
  }

  @Override
  public Object applyToList(JsonArray list) {
    double sum = 0;

    for (Number item : new JsonArrayIterable<Number> (list)) {
      sum += item.doubleValue();
    }

    return sum / list.size();
  }
}
