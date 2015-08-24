package io.tiler.internal.queries;

public class EvaluationException extends Exception {
  private final QueryContext queryContext;

  public EvaluationException(QueryContext queryContext, String message) {
    super(QueryErrorFormatter.format(queryContext.query(), new QueryError(queryContext.line(), queryContext.column(), message)));
    this.queryContext = queryContext;
  }

  public QueryContext getQueryContext() {
    return queryContext;
  }
}
