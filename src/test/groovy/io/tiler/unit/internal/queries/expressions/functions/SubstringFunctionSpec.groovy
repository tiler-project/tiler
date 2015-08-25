package io.tiler.unit.internal.queries.expressions.functions

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.SubstringFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

class SubstringFunctionSpec extends Specification {
  @Shared queryContext = new QueryContext("query", 1, 2)
  @Shared validValue = ""
  @Shared validBeginIndex = 1
  @Shared validEndIndex = 1
  def context = new EvaluationContextBuilder().build()

  def "it returns part of a string"() {
    given:
    def function = new SubstringFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, beginIndex),
      new ConstantExpression(queryContext, endIndex))

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    value      | beginIndex      | endIndex      | result
    "test"     | 0               | 4             | "test"
    "test"     | 0               | 0             | ""
    "test"     | 4               | 4             | ""
    "test"     | 1               | 3             | "es"
  }

  def "it validates the parameters"() {
    given:
    def function = new SubstringFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, beginIndex),
      new ConstantExpression(queryContext, endIndex))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value      | beginIndex      | endIndex      | message
    1          | validBeginIndex | validEndIndex | "value must be a String"
    validValue | ""              | validEndIndex | "beginIndex must be an Integer"
    validValue | validBeginIndex | ""            | "endIndex must be an Integer"
    null       | validBeginIndex | validEndIndex | "value cannot be null"
    validValue | null            | validEndIndex | "beginIndex cannot be null"
    validValue | validBeginIndex | null          | "endIndex cannot be null"
    "test"     | -1              | validEndIndex | "beginIndex cannot be less than zero"
    "test"     | 0               | 5             | "endIndex cannot be greater than the length of value"
    "test"     | 2               | 1             | "endIndex cannot be less than beginIndex"
  }
}
