package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.List;

public class MeanFunction extends ListFunction<Number> {
  private final Expression list;

  public MeanFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Number.class);
    this.list = list;
  }

  public Expression list() {
    return list;
  }

  @Override
  public Object applyToList(List<Number> list) {
    double sum = 0;

    for (Number item : list) {
      sum = sum + item.doubleValue();
    }

    return sum / list.size();
  }
}
