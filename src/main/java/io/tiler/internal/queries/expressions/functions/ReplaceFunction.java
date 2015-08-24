package io.tiler.internal.queries.expressions.functions;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.util.regex.Pattern;

public class ReplaceFunction extends Function {
  private final Expression value;
  private final Expression regex;
  private final Expression replacement;

  public ReplaceFunction(QueryContext queryContext, Expression value, Expression regex, Expression replacement) {
    super(queryContext);
    this.value = value;
    this.regex = regex;
    this.replacement = replacement;
  }

  public Expression value() {
    return value;
  }

  public Expression regex() {
    return regex;
  }

  public Expression replacement() {
    return replacement;
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object value = this.value.evaluate(context);
    Object regex = this.regex.evaluate(context);
    Object replacement = this.replacement.evaluate(context);

    if (value == null) {
      throw new EvaluationException(queryContext(), "value cannot be null");
    }

    if (!(value instanceof String)) {
      throw new EvaluationException(queryContext(), "value must be a String");
    }

    if (regex == null) {
      throw new EvaluationException(queryContext(), "regex cannot be null");
    }

    if (!(regex instanceof Pattern)) {
      throw new EvaluationException(queryContext(), "regex must be a Pattern");
    }

    if (replacement == null) {
      throw new EvaluationException(queryContext(), "replacement cannot be null");
    }

    if (!(replacement instanceof String)) {
      throw new EvaluationException(queryContext(), "replacement must be a String");
    }

    String valueString = (String) value;
    Pattern regexPattern = (Pattern) regex;
    String replacementString = (String) replacement;

    return regexPattern.matcher(valueString).replaceAll(replacementString);
  }
}
