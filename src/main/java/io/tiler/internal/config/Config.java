package io.tiler.internal.config;

public class Config {
  private static final String METRIC_NAMES_REDIS_KEY = "metricNames";
  private static final String METRIC_REDIS_KEY_PREFIX = "metrics:";
  private final Redis redis;

  public Config(Redis redis) {
    this.redis = redis;
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
