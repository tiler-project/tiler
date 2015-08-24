package io.tiler.internal.queries;

public class QueryContext {
  private final String query;
  private final int line;
  private final int column;

  public QueryContext(String query, int line, int column) {
    this.query = query;
    this.line = line;
    this.column = column;
  }

  public String query() {
    return query;
  }

  public int line() {
    return line;
  }

  public int column() {
    return column;
  }
}
