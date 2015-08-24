package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.List;

public class MinFunction extends ListFunction<Number> {
  private final Expression list;

  public MinFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
    this.list = list;
  }

  public Expression list() {
    return list;
  }

  @Override
  public Object applyToList(List<Number> list) {
    Double min = null;

    for (Number value : list) {
      double doubleValue = value.doubleValue();

      if (min == null || doubleValue < min) {
        min = doubleValue;
      }
    }

    return min == null ? Double.NaN : min;
  }
}
