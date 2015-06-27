package io.tiler.internal.config;

public class ApiConfig {
  private boolean readOnly;

  public ApiConfig(Boolean readOnly) {
    if (readOnly == null) {
      // Default to read-only to reduce the default attack surface
      readOnly = true;
    }

    this.readOnly = readOnly;
  }

  public ApiConfig() {
    this(null);
  }

  public boolean readOnly() {
    return readOnly;
  }
}
