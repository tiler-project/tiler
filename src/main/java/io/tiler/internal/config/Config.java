package io.tiler.internal.config;

public class Config {
  private static final String METRIC_NAMES_REDIS_KEY = "metricNames";
  private static final String METRIC_REDIS_KEY_PREFIX = "metrics:";
  private final int port;
  private final Redis redis;

  public Config(Integer port, Redis redis) {
    if (port == null) {
      port = 8080;
    }

    this.port = port;
    this.redis = redis;
  }

  public int port() {
    return port;
  }

  public Redis redis() {
    return redis;
  }

  public String getMetricNamesKey() {
    return redis.keyPrefix() + METRIC_NAMES_REDIS_KEY;
  }

  public String getMetricKey(String metricName) {
    return redis.keyPrefix() + METRIC_REDIS_KEY_PREFIX + metricName;
  }
}
