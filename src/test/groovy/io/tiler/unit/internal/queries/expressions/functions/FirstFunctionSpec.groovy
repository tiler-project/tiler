package io.tiler.unit.internal.queries.expressions.functions
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.FirstFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import org.vertx.java.core.json.JsonArray
import spock.lang.Specification

class FirstFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it returns the first item in a list"() {
    given:
    def list = new JsonArray(["1", "2", "3"])
    def function = new FirstFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    def result = function.evaluate(context)

    then:
    result == "1"
  }

  def "it validates the list is actually a list"() {
    given:
    def function = new FirstFunction(
      queryContext,
      new ConstantExpression(queryContext, 1))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must evaluate to a JsonArray"
  }

  def "it validates the list is not empty"() {
    given:
    def list = new JsonArray([])
    def function = new FirstFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must not be empty"
  }
}
