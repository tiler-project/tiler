package io.tiler.unit.internal.queries.expressions.builders;

import io.tiler.internal.queries.EvaluationContext;
import org.vertx.java.core.json.JsonObject;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class EvaluationContextBuilder {
  private Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));
  private JsonObject fields = new JsonObject();

  public EvaluationContextBuilder clock(Clock clock) {
    this.clock = clock;
    return this;
  }

  public EvaluationContextBuilder field(String fieldName, Object fieldValue) {
    fields.putValue(fieldName, fieldValue);
    return this;
  }

  public EvaluationContext build() {
    return new EvaluationContext(clock, fields);
  }
}
