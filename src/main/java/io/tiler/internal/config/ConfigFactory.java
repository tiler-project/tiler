package io.tiler.internal.config;

import org.vertx.java.core.json.JsonObject;

public class ConfigFactory {
  public Config load(JsonObject config) {
    return new Config(
      config.getInteger("port"),
      getApiConfig(config),
      getRedisConfig(config));
  }

  private ApiConfig getApiConfig(JsonObject config) {
    JsonObject api = config.getObject("api");

    if (api == null) {
      return new ApiConfig();
    }

    return new ApiConfig(
      api.getBoolean("readOnly"));
  }

  private RedisConfig getRedisConfig(JsonObject config) {
    JsonObject redis = config.getObject("redis");

    if (redis == null) {
      return new RedisConfig();
    }

    return new RedisConfig(
      redis.getString("address"),
      redis.getString("host"),
      redis.getInteger("port"),
      redis.getString("encoding"),
      redis.getBoolean("binary"),
      redis.getString("auth"),
      redis.getInteger("select"),
      redis.getString("keyPrefix"));
  }
}
