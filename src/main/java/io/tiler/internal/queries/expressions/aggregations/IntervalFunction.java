package io.tiler.internal.queries.expressions.aggregations;

import com.google.common.math.LongMath;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

import java.math.RoundingMode;

public class IntervalFunction extends AggregateExpression {
  private final Expression value;
  private final Expression offset;
  private final Expression size;

  public IntervalFunction(QueryContext queryContext, Expression value, Expression offset, Expression size) {
    super(queryContext);
    this.value = value;
    this.offset = offset;
    this.size = size;
  }

  public Expression value() {
    return value;
  }

  public Expression offset() {
    return offset;
  }

  public Expression size() {
    return size;
  }

  @Override
  public Object evaluate(EvaluationContext context) throws EvaluationException {
    Object value = this.value.evaluate(context);
    Object offset = this.offset.evaluate(context);
    Object size = this.size.evaluate(context);

    if (value == null) {
      throw new EvaluationException(queryContext(), "value cannot be null");
    }

    if (!(value instanceof Number)) {
      throw new EvaluationException(queryContext(), "value must be a Number");
    }

    if (offset == null) {
      throw new EvaluationException(queryContext(), "offset cannot be null");
    }

    if (!(offset instanceof Number)) {
      throw new EvaluationException(queryContext(), "offset must be a Number");
    }

    if (size == null) {
      throw new EvaluationException(queryContext(), "size cannot be null");
    }

    if (!(size instanceof Number)) {
      throw new EvaluationException(queryContext(), "size must be a Number");
    }

    long valueNumber = ((Number) value).longValue();
    long offsetNumber = ((Number) offset).longValue();
    long sizeNumber = ((Number) size).longValue();

    return (LongMath.divide(valueNumber - offsetNumber, sizeNumber, RoundingMode.FLOOR) * sizeNumber) + offsetNumber;
  }
}
