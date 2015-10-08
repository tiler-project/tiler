package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

public class FirstFunction extends ListFunction<Object> {
  public FirstFunction(QueryContext queryContext, Expression list) {
    super(queryContext, list, Object.class);
  }

  @Override
  public Object applyToList(JsonArray list) throws EvaluationException {
    if (list.size() == 0) {
      throw new EvaluationException(queryContext(), "list must not be empty");
    }

    return list.get(0);
  }
}
