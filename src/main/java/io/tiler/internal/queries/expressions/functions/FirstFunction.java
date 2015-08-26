package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.List;

public class FirstFunction extends ListFunction<Object> {
  private final Expression list;

  public FirstFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Object.class);
    this.list = list;
  }

  public Expression list() {
    return list;
  }

  @Override
  public Object applyToList(List<Object> list) throws EvaluationException {
    if (list.isEmpty()) {
      throw new EvaluationException(queryContext(), "list must not be empty");
    }

    return list.get(0);
  }
}
