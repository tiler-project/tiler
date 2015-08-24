package io.tiler.internal.queries;

import java.util.List;

public class InvalidQueryException extends Exception {
  private final String query;
  private final List<QueryError> errors;

  public InvalidQueryException(String query, List<QueryError> errors) {
    super(QueryErrorFormatter.format(query, errors));
    this.query = query;
    this.errors = errors;
  }

  public String getQuery() {
    return query;
  }

  public List<QueryError> getErrors() {
    return errors;
  }
}
