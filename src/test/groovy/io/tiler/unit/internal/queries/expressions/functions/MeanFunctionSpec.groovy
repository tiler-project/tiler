package io.tiler.unit.internal.queries.expressions.functions
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.MeanFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class MeanFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it calculates the mean of a list of numbers"() {
    given:
    def list = [1, 2, 3, 4]
    def function = new MeanFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    def result = function.evaluate(context)

    then:
    result == 2.5
  }

  def "it evaluates to NaN when provided with an empty list"() {
    given:
    def list = []
    def function = new MeanFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    def result = function.evaluate(context)

    then:
    result == Double.NaN
  }

  def "it validates the list is actually a list"() {
    given:
    def function = new MeanFunction(
      queryContext,
      new ConstantExpression(queryContext, 1))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must evaluate to a List"
  }

  def "it validates the items in the list are numbers"() {
    given:
    def list = ["text"]
    def function = new MeanFunction(
      queryContext,
      new ConstantExpression(queryContext, list))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ items in list must be numbers"
  }
}
