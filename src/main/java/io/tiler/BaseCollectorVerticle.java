package io.tiler;

import org.simondean.vertx.async.DefaultAsyncResult;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.List;

public class BaseCollectorVerticle extends Verticle {
  private static final long TWO_MINUTES_IN_MILLISECONDS = 2 * 60 * 1000;

  protected void getExistingMetrics(List<String> metricNames, AsyncResultHandler<JsonArray> handler) {
    JsonObject message = new JsonObject()
      .putString("type", "getMetrics")
      .putObject("body", new JsonObject()
        .putArray("metricNames", new JsonArray(metricNames)));

    vertx.eventBus().sendWithTimeout("io.tiler", message, TWO_MINUTES_IN_MILLISECONDS, (AsyncResult<Message<JsonObject>> result) -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result.cause()));
        return;
      }

      JsonObject responseMessage = result.result().body();
      JsonObject responseBody = responseMessage.getObject("body");

      if (responseBody.containsField("error")) {
        String errorMessage = responseMessage.getObject("error").getString("message");
        container.logger().error("Failed to get metrics: " + errorMessage);
        handler.handle(DefaultAsyncResult.fail(new RuntimeException(errorMessage)));
        return;
      }

      handler.handle(DefaultAsyncResult.succeed(responseBody.getArray("metrics")));
    });
  }

  protected void saveMetrics(JsonArray metrics) {
    container.logger().info("Sending metrics to event bus");
    JsonObject message = new JsonObject()
      .putString("type", "publishMetrics")
      .putObject("body", new JsonObject()
        .putArray("metrics", metrics));
    vertx.eventBus().send("io.tiler", message);
  }

  protected long currentTimeInMicroseconds() {
    return System.currentTimeMillis() * 1000;
  }
}
