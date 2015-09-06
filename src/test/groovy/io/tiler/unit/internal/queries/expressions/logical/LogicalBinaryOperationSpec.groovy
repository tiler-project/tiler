package io.tiler.unit.internal.queries.expressions.logical
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.Expression
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.logical.AndOperation
import io.tiler.internal.queries.expressions.logical.OrOperation
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.Specification

class LogicalBinaryOperationSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it performs a logical operation on the operands"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2)] as Object[])

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result

    where:
    operation    | operand1 | operand2 | result
    AndOperation | false    | false    | false
    AndOperation | false    | true     | false
    AndOperation | true     | false    | false
    AndOperation | true     | true     | true
    OrOperation  | false    | false    | false
    OrOperation  | false    | true     | true
    OrOperation  | true     | false    | true
    OrOperation  | true     | true     | true
  }

  def "it performs short-circuit evaluation"() {
    given:
    def operand2Expression = Mock(Expression)
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand1),
      operand2Expression] as Object[])

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == result
    0 * operand2Expression.evaluate(_)

    where:
    operation    | operand1 | result
    AndOperation | false    | false
    OrOperation  | true     | true
  }

  def "it validates the operands are booleans"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operation    | operand1 | operand2 | message
    AndOperation | ""       | true     | "operand1 must be a 'java.lang.Boolean' but is actually a 'java.lang.String'"
    AndOperation | true     | ""       | "operand2 must be a 'java.lang.Boolean' but is actually a 'java.lang.String'"
    OrOperation  | ""       | false    | "operand1 must be a 'java.lang.Boolean' but is actually a 'java.lang.String'"
    OrOperation  | false    | ""       | "operand2 must be a 'java.lang.Boolean' but is actually a 'java.lang.String'"
  }

  def "it validates the operands are not null"() {
    given:
    def function = operation.newInstance([
      queryContext,
      new ConstantExpression(queryContext, operand1),
      new ConstantExpression(queryContext, operand2)] as Object[])

    when:
    function.evaluate(context)

    then:
    def e = thrown(EvaluationException)
    e.message == "Line 1:2\nquery\n  ^ $message"

    where:
    operation    | operand1 | operand2 | message
    AndOperation | null     | true     | "operand1 cannot be null"
    AndOperation | true     | null     | "operand2 cannot be null"
    OrOperation  | null     | false    | "operand1 cannot be null"
    OrOperation  | false    | null     | "operand2 cannot be null"
  }
}
