package io.tiler.internal.queries.expressions;

import org.vertx.java.core.json.JsonObject;

public class JsonHelper {
  public static String getFirstFieldNameFromJsonObject(JsonObject jsonObject) {
    for (String fieldName : jsonObject.getFieldNames()) {
      return fieldName;
    }

    return null;
  }
}
