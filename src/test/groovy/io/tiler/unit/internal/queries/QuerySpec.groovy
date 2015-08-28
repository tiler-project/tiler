package io.tiler.unit.internal.queries

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryFactory
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import spock.lang.*

class QuerySpec extends Specification {
  def factory = new QueryFactory()

  def "it handles an evaluation exception"() {
    def queryText = "from metric.name\n" +
                    "where true && 1"
    def query = factory.parseQuery(queryText)
    def metrics = new JsonArray()
      .addObject(new JsonObject()
        .putString("name", "metric.name")
        .putArray("points", new JsonArray()
          .addObject(new JsonObject())))

    when:
    query.applyToMetrics(metrics)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 2:6\n" +
                 "where true && 1\n" +
                 "      ^ operand1 cannot be null"
  }
}
