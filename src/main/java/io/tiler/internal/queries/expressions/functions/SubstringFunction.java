package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class SubstringFunction extends Function {
  private final Expression value;
  private final Expression beginIndex;
  private final Expression endIndex;

  public SubstringFunction(QueryContext queryContext, Expression value, Expression beginIndex, Expression endIndex) {
    super(queryContext);
    this.value = value;
    this.beginIndex = beginIndex;
    this.endIndex = endIndex;
  }

  public Expression value() {
    return value;
  }

  public Expression beginIndex() {
    return beginIndex;
  }

  public Expression endIndex() {
    return endIndex;
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object value = this.value.evaluate(context);
    Object beginIndex = this.beginIndex.evaluate(context);
    Object endIndex = this.endIndex.evaluate(context);

    if (value == null) {
      throw new EvaluationException(queryContext(), "value cannot be null");
    }

    if (!(value instanceof String)) {
      throw new EvaluationException(queryContext(), "value must be a 'java.lang.String' but was a '" + value.getClass().getName() + "'");
    }

    if (beginIndex == null) {
      throw new EvaluationException(queryContext(), "beginIndex cannot be null");
    }

    if (!(beginIndex instanceof Integer)) {
      throw new EvaluationException(queryContext(), "beginIndex must be a 'java.lang.Integer' but was a '" + beginIndex.getClass().getName() + "'");
    }

    if (endIndex == null) {
      throw new EvaluationException(queryContext(), "endIndex cannot be null");
    }

    if (!(endIndex instanceof Integer)) {
      throw new EvaluationException(queryContext(), "endIndex must be a 'java.lang.Integer' but was a '" + endIndex.getClass().getName() + "'");
    }

    String valueString = (String) value;
    Integer beginIndexInteger = (Integer) beginIndex;
    Integer endIndexInteger = (Integer) endIndex;

    if (beginIndexInteger < 0) {
      throw new EvaluationException(queryContext(), "beginIndex cannot be less than zero but was " + beginIndexInteger);
    }

    if (endIndexInteger > valueString.length()) {
      throw new EvaluationException(queryContext(), "endIndex cannot be greater than the length of value but endIndex was " + endIndexInteger + " and length was " + valueString.length());
    }

    if (endIndexInteger < beginIndexInteger) {
      throw new EvaluationException(queryContext(), "endIndex cannot be less than beginIndex but beginIndex was " + beginIndexInteger + " and endIndex was " + endIndexInteger);
    }

    return valueString.substring(beginIndexInteger, endIndexInteger);
  }
}
