package io.tiler.unit.internal.queries.expressions.aggregations
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.aggregations.IntervalFunction
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class IntervalFunctionSpec extends Specification {
  @Shared validValue = 1
  @Shared validOffset = 1
  @Shared validSize = 1
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it calculates the interval of a value"() {
    given:
    def function = new IntervalFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, offset),
      new ConstantExpression(queryContext, size))

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    value | offset | size | result
    -1    | 0      | 1    | -1
    0     | 0      | 1    | 0
    1     | 0      | 1    | 1
    -2    | 0      | 2    | -2
    -1    | 0      | 2    | -2
    0     | 0      | 2    | 0
    1     | 0      | 2    | 0
    2     | 0      | 2    | 2
    -2    | 1      | 2    | -3
    -1    | 1      | 2    | -1
    0     | 1      | 2    | -1
    1     | 1      | 2    | 1
    2     | 1      | 2    | 1
    3     | 1      | 2    | 3
  }

  def "it validates the types of the parameters"() {
    given:
    def function = new IntervalFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, offset),
      new ConstantExpression(queryContext, size))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value      | offset      | size      | message
    ""         | validOffset | validSize | "value must be a Number"
    validValue | ""          | validSize | "offset must be a Number"
    validValue | validOffset | ""        | "size must be a Number"
  }

  def "it validates the parameters are not null"() {
    given:
    def function = new IntervalFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, offset),
      new ConstantExpression(queryContext, size))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value      | offset      | size      | message
    null       | validOffset | validSize | "value cannot be null"
    validValue | null        | validSize | "offset cannot be null"
    validValue | validOffset | null      | "size cannot be null"
  }
}
