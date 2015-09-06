package io.tiler.internal.queries.clauses.metrics;

import io.tiler.internal.queries.EvaluationException;
import org.vertx.java.core.json.JsonArray;

import java.time.Clock;

public class MetricClauses {
  private final SelectClause selectClause;
  private final SortClause sortClause;

  public MetricClauses(SelectClause selectClause, SortClause sortClause) {
    this.selectClause = selectClause;
    this.sortClause = sortClause;
  }

  public boolean hasSelectClause() {
    return selectClause != null;
  }

  public boolean hasSortClause() {
    return sortClause != null;
  }

  public SelectClause selectClause() {
    return selectClause;
  }

  public SortClause sortClause() {
    return sortClause;
  }

  public JsonArray applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    if (hasSelectClause()) {
      metrics = selectClause.applyToMetrics(clock, metrics);
    }

    if (hasSortClause()) {
      metrics = sortClause.applyToMetrics(clock, metrics);
    }

    return metrics;
  }
}
