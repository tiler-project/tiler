package io.tiler.internal.queries.clauses.points;

import io.tiler.internal.queries.EvaluationException;
import org.vertx.java.core.json.JsonArray;

import java.time.Clock;

public class PointClauses {
  private final SelectClause selectClause;
  private final SortClause sortClause;

  public PointClauses(SelectClause selectClause, SortClause sortClause) {
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

  public void applyToMetrics(Clock clock, JsonArray metrics) throws EvaluationException {
    if (hasSelectClause()) {
      selectClause.applyToMetrics(clock, metrics);
    }

    if (hasSortClause()) {
      sortClause.applyToMetrics(clock, metrics);
    }
  }
}
