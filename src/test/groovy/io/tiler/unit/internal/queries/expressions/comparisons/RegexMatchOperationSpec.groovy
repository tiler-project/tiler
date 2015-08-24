package io.tiler.unit.internal.queries.expressions.comparisons

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.comparisons.RegexMatchOperation
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import org.antlr.v4.runtime.Token
import spock.lang.*

import java.util.regex.Pattern

class RegexMatchOperationSpec extends Specification {
  @Shared validOperand1 = ""
  @Shared validOperand2 = Pattern.compile("")
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it matches a string against a regex pattern"() {
    given:
    def function = new RegexMatchOperation(
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2))

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    operand1 | operand2 | result
    "abc"    | ~/b/     | true
    "abc"    | ~/d/     | false
    "abc"    | ~/^a/    | true
    "abc"    | ~/c$/    | true
    "abc"    | ~/^b/    | false
    "abc"    | ~/b$/    | false
  }

  def "it validates the types of the operands"() {
    given:
    def function = new RegexMatchOperation(
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operand1      | operand2      | message
    1             | validOperand2 | "operand1 must be a String"
    validOperand1 | ""            | "operand2 must be a regular expression Pattern"
  }

  def "it validates the operands are not null"() {
    given:
    def function = new RegexMatchOperation(
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2))

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operand1      | operand2      | message
    null          | validOperand2 | "operand1 cannot be null"
    validOperand1 | null          | "operand2 cannot be null"
  }
}
