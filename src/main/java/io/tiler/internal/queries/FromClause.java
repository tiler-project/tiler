package io.tiler.internal.queries;

import io.tiler.core.json.JsonArrayIterable;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FromClause {
  private List<MetricExpression> metricExpressions;
  private final boolean containsAtLeastOnePattern;

  public FromClause(List<MetricExpression> metricExpressions) {
    this.metricExpressions = Collections.unmodifiableList(metricExpressions);
    boolean containsAtLeastOnePattern = false;

    for (MetricExpression item : metricExpressions) {
      if (item instanceof RegexMetricExpression) {
        containsAtLeastOnePattern = true;
        break;
      }
    }

    this.containsAtLeastOnePattern = containsAtLeastOnePattern;
  }

  public List<MetricExpression> metricExpressions() {
    return metricExpressions;
  }

  public boolean isPotentiallyMissingAnyMetrics(Set<String> metricNames) {
    if (containsAtLeastOnePattern) {
      return true;
    }

    for (MetricExpression item : metricExpressions) {
      SimpleMetricExpression item2 = (SimpleMetricExpression)item;

      if (!metricNames.contains(item2.metricName())) {
        return true;
      }
    }

    return false;
  }

  public boolean matchesMetricName(String metricName) {
    for (Object item : metricExpressions) {
      if (item instanceof SimpleMetricExpression) {
        SimpleMetricExpression item2 = (SimpleMetricExpression) item;

        if (metricName.equals(item2.metricName())) {
          return true;
        }
      } else {
        RegexMetricExpression item2 = (RegexMetricExpression) item;

        if (item2.pattern().matcher(metricName).find()) {
          return true;
        }
      }
    }

    return false;
  }

  public JsonArray findMatchingMetrics(JsonArray metrics) {
    JsonArray matchingMetrics = new JsonArray();

    for (JsonObject metric : new JsonArrayIterable<JsonObject>(metrics)) {
      if (matchesMetricName(metric.getString("name"))) {
        matchingMetrics.addObject(metric);
      }
    }

    return matchingMetrics;
  }
}
