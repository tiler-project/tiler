package io.tiler.internal.queries;

public class InvalidQueryException extends Exception {
  public InvalidQueryException() {
  }

  public InvalidQueryException(String message) {
    super(message);
  }

  public InvalidQueryException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidQueryException(Throwable cause) {
    super(cause);
  }
}
