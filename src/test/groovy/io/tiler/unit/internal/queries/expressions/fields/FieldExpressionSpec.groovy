package io.tiler.unit.internal.queries.expressions.fields

import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.fields.FieldExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import org.vertx.java.core.json.JsonArray
import spock.lang.*

class FieldExpressionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)

  def "it returns the value of the field from the context when evaluated"() {
    given:
    def context = new EvaluationContextBuilder()
      .field("fieldName", "fieldValue")
      .build()
    def expression = new FieldExpression(queryContext, "fieldName")

    when:
    def result = expression.evaluate(context)

    then:
    result == "fieldValue"
  }

  def "it does not convert a JsonArray based field value to List"() {
    def jsonArray = new JsonArray().addString("fieldValue1").addString("fieldValue2")
    given:
    def context = new EvaluationContextBuilder()
      .field("fieldName", jsonArray)
      .build()
    def expression = new FieldExpression(queryContext, "fieldName")

    when:
    def result = expression.evaluate(context)

    then:
    result.class == JsonArray
    result == jsonArray
    result.size() == 2
    result[0] == "fieldValue1"
    result[1] == "fieldValue2"
  }
}
