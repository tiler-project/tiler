package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.Collections;
import java.util.List;

public class ConcatFunction extends Function {
  private final List<Expression> parameters;

  public ConcatFunction(QueryContext queryContext, List<Expression> parameters) {
    super(queryContext);
    this.parameters = Collections.unmodifiableList(parameters);
  }

  public List<Expression> parameters() {
    return parameters;
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    StringBuilder builder = new StringBuilder();

    for (Expression parameter : parameters) {
      builder.append(parameter.evaluate(context));
    }

    return builder.toString();
  }
}
