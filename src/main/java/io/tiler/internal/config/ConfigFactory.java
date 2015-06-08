package io.tiler.internal.config;

import org.vertx.java.core.json.JsonObject;

public class ConfigFactory {
  public Config load(JsonObject config) {
    return new Config(
      config.getInteger("port"),
      getRedis(config));
  }

  private Redis getRedis(JsonObject config) {
    JsonObject redis = config.getObject("redis");

    if (redis == null) {
      return new Redis();
    }

    return new Redis(
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
