package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.List;

public class MaxFunction extends ListFunction<Number> {
  private final Expression list;

  public MaxFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
    this.list = list;
  }

  public Expression list() {
    return list;
  }

  @Override
  public Object applyToList(List<Number> list) {
    Double max = null;

    for (Number value : list) {
      double doubleValue = value.doubleValue();

      if (max == null || doubleValue > max) {
        max = doubleValue;
      }
    }

    return max == null ? Double.NaN : max;
  }
}
