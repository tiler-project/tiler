package io.tiler.unit.internal.queries.expressions.functions

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.ReplaceFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

import java.util.regex.Pattern

class ReplaceFunctionSpec extends Specification {
  @Shared queryContext = new QueryContext("query", 1, 2)
  @Shared validValue = "value"
  @Shared validRegex = Pattern.compile("regex")
  @Shared validReplacement = "replacement"
  def context = new EvaluationContextBuilder().build()

  def "it replaces a value in a string"() {
    given:
    def function = new ReplaceFunction(
      queryContext,
      new ConstantExpression(queryContext, "one three"),
      new ConstantExpression(queryContext, Pattern.compile("one")),
      new ConstantExpression(queryContext, "two"))

    when:
    def result = function.evaluate(context)

    then:
    result == "two three"
  }

  def "it replaces multiple occurrences of value in a string"() {
    given:
    def function = new ReplaceFunction(
      queryContext,
      new ConstantExpression(queryContext, "one one"),
      new ConstantExpression(queryContext, Pattern.compile("one")),
      new ConstantExpression(queryContext, "two"))

    when:
    def result = function.evaluate(context)

    then:
    result == "two two"
  }

  def "it handles the value to replace not being in a string"() {
    given:
    def function = new ReplaceFunction(
      queryContext,
      new ConstantExpression(queryContext, "three three"),
      new ConstantExpression(queryContext, Pattern.compile("one")),
      new ConstantExpression(queryContext, "two"))

    when:
    def result = function.evaluate(context)

    then:
    result == "three three"
  }

  def "it validates the parameters"() {
    given:
    def function = new ReplaceFunction(
      queryContext,
      new ConstantExpression(queryContext, value),
      new ConstantExpression(queryContext, regex),
      new ConstantExpression(queryContext, replacement))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value      | regex      | replacement      | message
    1          | validRegex | validReplacement | "value must be a String"
    validValue | 1          | validReplacement | "regex must be a Pattern"
    validValue | validRegex | 1                | "replacement must be a String"
    null       | validRegex | validReplacement | "value cannot be null"
    validValue | null       | validReplacement | "regex cannot be null"
    validValue | validRegex | null             | "replacement cannot be null"
  }
}
