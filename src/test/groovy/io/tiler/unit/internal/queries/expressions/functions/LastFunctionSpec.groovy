package io.tiler.unit.internal.queries.expressions.functions

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.LastFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.Specification

class LastFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it returns the last item in a list"() {
    given:
    def list = ["1", "2", "3"]
    def function = new LastFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    def result = function.evaluate(context)

    then:
    result == "3"
  }

  def "it validates the list is actually a list"() {
    given:
    def function = new LastFunction(
      queryContext,
      new ConstantExpression(queryContext, 1))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must evaluate to a List"
  }

  def "it validates the list is not empty"() {
    given:
    def list = []
    def function = new LastFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must not be empty"
  }
}
