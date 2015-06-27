package io.tiler.internal.config;

import org.vertx.java.core.json.JsonObject;

public class RedisConfig {
  private final String address;
  private final String host;
  private final Integer port;
  private final String encoding;
  private final Boolean binary;
  private final String auth;
  private final Integer select;
  private final String keyPrefix;

  public RedisConfig() {
    this(null, null, null, null, null, null, null, null);
  }

  public RedisConfig(String address, String host, Integer port, String encoding, Boolean binary, String auth, Integer select, String keyPrefix) {
    if (address == null) {
      address = "io.tiler.redis";
    }

    if (host == null) {
      host = "localhost";
    }

    if (port == null) {
      port = 6379;
    }

    if (encoding == null) {
      encoding = "UTF-8";
    }

    if (keyPrefix == null) {
      keyPrefix = "io.tiler:";
    }

    this.address = address;
    this.host = host;
    this.port = port;
    this.encoding = encoding;
    this.binary = binary;
    this.auth = auth;
    this.select = select;
    this.keyPrefix = keyPrefix;
  }

  public JsonObject toRedisModuleConfig() {
    return new JsonObject()
      .putString("address", address())
      .putString("host", host())
      .putNumber("port", port())
      .putString("encoding", encoding())
      .putBoolean("binary", binary())
      .putString("auth", auth())
      .putNumber("select", select());
  }

  public String address() {
    return address;
  }

  public String host() {
    return host;
  }

  public Integer port() {
    return port;
  }

  public String encoding() {
    return encoding;
  }

  public Boolean binary() {
    return binary;
  }

  public String auth() {
    return auth;
  }

  public Integer select() {
    return select;
  }

  public String keyPrefix() {
    return keyPrefix;
  }
}
