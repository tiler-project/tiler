package io.tiler.internal.queries;

import org.vertx.java.core.json.JsonObject;

import java.time.Clock;

public class EvaluationContext {
  private final Clock clock;
  private final JsonObject fields;

  public EvaluationContext(Clock clock, JsonObject fields) {
    this.clock = clock;
    this.fields = fields;
  }

  public Clock clock() {
    return clock;
  }

  public JsonObject fields() {
    return fields;
  }
}
