package io.tiler.regex;

public class InvalidPatternOptionsException extends Exception {
  public InvalidPatternOptionsException() {
  }

  public InvalidPatternOptionsException(String message) {
    super(message);
  }

  public InvalidPatternOptionsException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidPatternOptionsException(Throwable cause) {
    super(cause);
  }
}
