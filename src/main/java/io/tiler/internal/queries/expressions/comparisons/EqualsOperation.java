package io.tiler.internal.queries.expressions.comparisons;

import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;

public class EqualsOperation extends ComparisonOperation {
  public EqualsOperation(QueryContext queryContext, Expression operand1, Expression operand2) {
    super(queryContext, operand1, operand2);
  }

  @Override
  protected boolean evaluateComparisonResult(int comparisonResult) {
    return comparisonResult == 0;
  }
}
