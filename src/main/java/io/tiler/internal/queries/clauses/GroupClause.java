package io.tiler.internal.queries.clauses;

import io.tiler.core.json.JsonArrayIterable;
import io.tiler.core.json.JsonArrayUtils;
import io.tiler.internal.queries.expressions.fields.FieldExpression;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GroupClause {
  private final List<FieldExpression> fieldExpressions;

  public GroupClause(List<FieldExpression> fieldExpressions) {
    this.fieldExpressions = Collections.unmodifiableList(fieldExpressions);
  }

  public List<FieldExpression> fieldExpressions() {
    return fieldExpressions;
  }

  public JsonArray applyToMetrics(JsonArray metrics) {
    JsonArray groups = applyToPoints(metrics);
    JsonArray transformedMetrics = new JsonArray();

    for (JsonObject group : new JsonArrayIterable<JsonObject>(groups)) {
      transformedMetrics.addObject(new JsonObject()
        .mergeIn(group)
        .putArray("points", group.getArray("points")));
    }

    return transformedMetrics;
  }

  private JsonArray applyToPoints(JsonArray metrics) {
    HashMap<ArrayList<Object>, JsonObject> groups = new HashMap<>();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      for (JsonObject point : new JsonArrayIterable<JsonObject>(metric.getArray("points"))) {
        ArrayList<Object> groupKey = new ArrayList<>();

        for (FieldExpression fieldExpression : fieldExpressions) {
          String fieldName = fieldExpression.fieldName();
          groupKey.add(fieldName);
          groupKey.add(point.getValue(fieldName));
        }

        JsonObject group = groups.get(groupKey);
        JsonArray groupPoints;

        if (group == null) {
          groupPoints = new JsonArray();
          group = new JsonObject();

          for (FieldExpression fieldExpression : fieldExpressions) {
            String fieldName = fieldExpression.fieldName();
            group.putValue(fieldName, point.getValue(fieldName));
          }

          group.putArray("points", groupPoints);
          groups.put(groupKey, group);
        } else {
          groupPoints = group.getArray("points");
        }

        groupPoints.addObject(point);
      }
    }

    return JsonArrayUtils.convertToJsonArray(groups.values());
  }
}
