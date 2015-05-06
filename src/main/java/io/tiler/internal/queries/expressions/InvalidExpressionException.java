package io.tiler.internal.queries.expressions;

public class InvalidExpressionException extends Exception {
  public InvalidExpressionException() {
  }

  public InvalidExpressionException(String message) {
    super(message);
  }

  public InvalidExpressionException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidExpressionException(Throwable cause) {
    super(cause);
  }
}
