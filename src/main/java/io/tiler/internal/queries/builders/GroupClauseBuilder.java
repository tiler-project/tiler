package io.tiler.internal.queries.builders;

import io.tiler.internal.queries.GroupClause;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.fields.FieldExpression;

import java.util.ArrayList;

public class GroupClauseBuilder {
  private ArrayList<FieldExpression> fieldExpressions = new ArrayList<>();

  public GroupClauseBuilder fieldExpression(FieldExpression fieldExpression) {
    fieldExpressions.add(fieldExpression);
    return this;
  }

  public GroupClause build() {
    return new GroupClause(fieldExpressions);
  }
}
