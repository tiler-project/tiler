package io.tiler.internal.queries.expressions.fields;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.QueryContext;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonArray;

public class FieldExpression extends Expression {
  private final String fieldName;

  public FieldExpression(QueryContext queryContext, String fieldName) {
    super(queryContext);
    this.fieldName = fieldName;
  }

  public String fieldName() {
    return fieldName;
  }

  @Override
  public Object evaluate(EvaluationContext context) {
    Object fieldValue = context.fields().getValue(fieldName);

    if (fieldValue instanceof JsonArray) {
      fieldValue = ((JsonArray) fieldValue).toList();
    }

    return fieldValue;
  }
}
