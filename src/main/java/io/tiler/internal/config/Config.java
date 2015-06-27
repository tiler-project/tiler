package io.tiler.internal.config;

public class Config {
  private static final String METRIC_NAMES_REDIS_KEY = "metricNames";
  private static final String METRIC_REDIS_KEY_PREFIX = "metrics:";
  private final int port;
  private final ApiConfig apiConfig;
  private final RedisConfig redisConfig;

  public Config(Integer port, ApiConfig apiConfig, RedisConfig redisConfig) {
    if (port == null) {
      port = 8080;
    }

    this.port = port;
    this.apiConfig = apiConfig;
    this.redisConfig = redisConfig;
  }

  public int port() {
    return port;
  }

  public ApiConfig api() {
    return apiConfig;
  }

  public RedisConfig redis() {
    return redisConfig;
  }

  public String getMetricNamesKey() {
    return redisConfig.keyPrefix() + METRIC_NAMES_REDIS_KEY;
  }

  public String getMetricKey(String metricName) {
    return redisConfig.keyPrefix() + METRIC_REDIS_KEY_PREFIX + metricName;
  }
}
