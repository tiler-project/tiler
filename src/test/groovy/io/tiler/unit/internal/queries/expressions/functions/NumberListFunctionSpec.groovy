package io.tiler.unit.internal.queries.expressions.functions
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.MaxFunction
import io.tiler.internal.queries.expressions.functions.MeanFunction
import io.tiler.internal.queries.expressions.functions.MinFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class NumberListFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it calculates the function of a list of numbers"() {
    given:
    def list = [2, 1, 4, 3]
    def function = functionClass.newInstance([
      queryContext,
      new ConstantExpression(queryContext, list)] as Object[])

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    functionClass | result
    MeanFunction  | 2.5
    MinFunction   | 1
    MaxFunction   | 4
  }

  def "it evaluates to NaN when provided with an empty list"() {
    given:
    def list = []
    def function = functionClass.newInstance([
      queryContext,
      new ConstantExpression(queryContext, list)] as Object[])

    when:
    def result = function.evaluate(context)

    then:
    result == Double.NaN

    where:
    functionClass << [MeanFunction, MinFunction, MaxFunction]
  }

  def "it validates the list is actually a list"() {
    given:
    def function = functionClass.newInstance([
      queryContext,
      new ConstantExpression(queryContext, 1)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ list must evaluate to a List"

    where:
    functionClass << [MeanFunction, MinFunction, MaxFunction]
  }

  def "it validates the items in the list are numbers"() {
    given:
    def list = ["text"]
    def function = functionClass.newInstance([
      queryContext,
      new ConstantExpression(queryContext, list)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ items in list must be numbers"

    where:
    functionClass << [MeanFunction, MinFunction, MaxFunction]
  }
}
