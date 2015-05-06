package io.tiler.internal;

import org.vertx.java.core.json.JsonObject;

public class RedisException extends Exception {
  public RedisException(JsonObject redisReply) {
    super(createMessage(redisReply));
  }

  private static String createMessage(JsonObject redisReply) {
    String message = redisReply.getString("status");

    if (redisReply.containsField("message")) {
      message += ": " + redisReply.getString("message");
    }
    return message;
  }
}
