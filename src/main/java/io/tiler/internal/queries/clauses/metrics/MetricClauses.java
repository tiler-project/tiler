package io.tiler.internal.queries.clauses.metrics;

import io.tiler.internal.queries.EvaluationException;
import org.vertx.java.core.json.JsonArray;

public class MetricClauses {
  private final io.tiler.internal.queries.clauses.metrics.SelectClause selectClause;
  private final io.tiler.internal.queries.clauses.metrics.SortClause sortClause;

  public MetricClauses(io.tiler.internal.queries.clauses.metrics.SelectClause selectClause, SortClause sortClause) {
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

  public void applyToMetrics(JsonArray metrics) throws EvaluationException {
    if (hasSelectClause()) {
      selectClause.applyToMetrics(metrics);
    }
  }
}
