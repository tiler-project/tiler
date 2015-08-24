package io.tiler.internal.queries.expressions.comparisons;

import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class GreaterThanOrEqualsOperation extends ComparisonOperation {
  public GreaterThanOrEqualsOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  protected boolean evaluateComparisonResult(int comparisonResult) {
    return comparisonResult >= 0;
  }
}
