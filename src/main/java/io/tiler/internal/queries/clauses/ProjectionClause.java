package io.tiler.internal.queries.clauses;

import io.tiler.internal.queries.EvaluationContext;
import io.tiler.internal.queries.EvaluationException;
import io.tiler.internal.queries.expressions.Expression;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.util.Collections;
import java.util.Map;

public class ProjectionClause {
  private final Map<String, Expression> namedExpressions;

  public ProjectionClause(Map<String, Expression> namedExpressions) {
    this.namedExpressions = Collections.unmodifiableMap(namedExpressions);
  }

  public Map<String, Expression> namedExpressions() {
    return namedExpressions;
  }

  protected JsonObject applyToFields(JsonObject fields) throws EvaluationException {
    EvaluationContext context = new EvaluationContext(Clock.systemUTC(), fields);
    JsonObject transformedJsonObject = new JsonObject();

    for (Map.Entry<String, Expression> namedExpression : namedExpressions().entrySet()) {
      String name = namedExpression.getKey();
      Object value = namedExpression.getValue().evaluate(context);
      transformedJsonObject.putValue(name, value);
    }

    return transformedJsonObject;
  }
}
