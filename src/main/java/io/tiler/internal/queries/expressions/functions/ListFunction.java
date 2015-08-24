package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFunction<T> extends Function {
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

    if (!(list instanceof List<?>)) {
      throw new EvaluationException(queryContext(), "list must evaluate to a List");
    }

    List<?> list2 = (List<?>) list;
    List<T> typedList = new ArrayList<>(list2.size());

    for (Object item : list2) {
      if (item != null && !itemClass.isInstance(item)) {
        throw new EvaluationException(queryContext(), "items in list must be numbers");
      }

      typedList.add((T) item);
    }

    return applyToList(typedList);
  }

  public abstract Object applyToList(List<T> list);
}
