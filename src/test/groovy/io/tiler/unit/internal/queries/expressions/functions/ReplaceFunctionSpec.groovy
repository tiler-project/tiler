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
  @Shared validValue = new ConstantExpression(queryContext, "value")
  @Shared validRegex = new ConstantExpression(queryContext, Pattern.compile("regex"))
  @Shared validReplacement = new ConstantExpression(queryContext, "replacement")
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

  def "it validates the types of the parameters"() {
    given:
    def function = new ReplaceFunction(queryContext, value, regex, replacement)

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value << [new ConstantExpression(queryContext, 1), validValue, validValue]
    regex << [validRegex, new ConstantExpression(queryContext, 1), validRegex]
    replacement << [validReplacement, validReplacement, new ConstantExpression(queryContext, 1)]
    message << ["value must be a String", "regex must be a Pattern", "replacement must be a String"]
  }

  def "it validates the parameters are not null"() {
    given:
    def function = new ReplaceFunction(queryContext, value, regex, replacement)

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    value << [new ConstantExpression(queryContext, null), validValue, validValue]
    regex << [validRegex, new ConstantExpression(queryContext, null), validRegex]
    replacement << [validReplacement, validReplacement, new ConstantExpression(queryContext, null)]
    message << ["value cannot be null", "regex cannot be null", "replacement cannot be null"]
  }
}
