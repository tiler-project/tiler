package io.tiler.internal.queries;

import org.antlr.v4.runtime.Token;

public class QueryError {
  private final int line;
  private final int column;
  private final String message;

  public QueryError(int line, int column, String message) {
    this.line = line;
    this.column = column;
    this.message = message;
  }

  public QueryError(Token token, String message) {
    this.line = token.getLine();
    this.column = token.getCharPositionInLine();
    this.message = message;
  }

  public int line() {
    return line;
  }

  public int column() {
    return column;
  }

  public String message() {
    return message;
  }
}
