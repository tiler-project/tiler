package io.tiler.unit.internal.queries.expressions.logical
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.logical.NotOperation
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.Specification

class LogicalUnaryOperationSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it performs a logical operation on the operand"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand)] as Object[])

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    operation    | operand | result
    NotOperation | true    | false
    NotOperation | false   | true
  }

  def "it validates the operand is a boolean"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operation    | operand | message
    NotOperation | ""      | "operand must be a 'java.lang.Boolean' but is actually a 'java.lang.String'"
  }

  def "it validates the operands are not null"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operation    | operand | message
    NotOperation | null    | "operand cannot be null"
  }
}
