package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

public abstract class
  ListFunction<T> extends Function {
  private final Expression list;
  private final Class itemClass;

  public ListFunction(QueryContext queryContext, Expression list, Class itemClass) {
    super(queryContext);
    this.list = list;
    this.itemClass = itemClass;
  }

  public Expression list() {
    return list;
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object list = this.list.evaluate(context);

    if (!(list instanceof JsonArray)) {
      throw new EvaluationException(queryContext(), "list must evaluate to a JsonArray");
    }

    JsonArray list2 = (JsonArray)list;

    for (Object item : list2) {
      if (item != null && !itemClass.isInstance(item)) {
        throw new EvaluationException(queryContext(), "Each item in list must be a '" + itemClass.getName() + "'");
      }
    }

    return applyToList(list2);
  }

  public abstract Object applyToList(JsonArray list) throws EvaluationException;
}
