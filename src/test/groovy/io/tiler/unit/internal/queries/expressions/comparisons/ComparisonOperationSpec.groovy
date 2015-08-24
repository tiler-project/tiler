package io.tiler.unit.internal.queries.expressions.comparisons
import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.comparisons.EqualsOperation
import io.tiler.internal.queries.expressions.comparisons.GreaterThanOperation
import io.tiler.internal.queries.expressions.comparisons.GreaterThanOrEqualsOperation
import io.tiler.internal.queries.expressions.comparisons.LessThanOperation
import io.tiler.internal.queries.expressions.comparisons.LessThanOrEqualsOperation
import io.tiler.internal.queries.expressions.comparisons.NotEqualsOperation
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class ComparisonOperationSpec extends Specification {
  @Shared validOperand = 1
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it compares numbers"() {
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
    operation                    | operand1 | operand2 | result
    EqualsOperation              | 2        | 1        | false
    EqualsOperation              | 2        | 2        | true
    EqualsOperation              | 2        | 3        | false
    GreaterThanOrEqualsOperation | 2        | 1        | true
    GreaterThanOrEqualsOperation | 2        | 2        | true
    GreaterThanOrEqualsOperation | 2        | 3        | false
  }

  def "it compares booleans"() {
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
    operation                    | operand1 | operand2 | result
    EqualsOperation              | false    | false    | true
    EqualsOperation              | false    | true     | false
    EqualsOperation              | true     | false    | false
    EqualsOperation              | true     | true     | true
    GreaterThanOrEqualsOperation | false    | false    | true
    GreaterThanOrEqualsOperation | false    | true     | false
    GreaterThanOrEqualsOperation | true     | false    | true
    GreaterThanOrEqualsOperation | true     | true     | true
  }

  def "it compares strings"() {
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
    operation                    | operand1 | operand2 | result
    EqualsOperation              | "b"      | "a"      | false
    EqualsOperation              | "b"      | "b"      | true
    EqualsOperation              | "b"      | "c"      | false
    GreaterThanOrEqualsOperation | "b"      | "a"      | true
    GreaterThanOrEqualsOperation | "b"      | "b"      | true
    GreaterThanOrEqualsOperation | "b"      | "c"      | false
  }

  def "it validates the operands are comparable"() {
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
    operation                    | operand1 | operand2 | message
    EqualsOperation              | 1        | ""       | "Expressions are not comparable 'java.lang.Integer', 'java.lang.String'"
    GreaterThanOrEqualsOperation | 1        | ""       | "Expressions are not comparable 'java.lang.Integer', 'java.lang.String'"
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
    operation                    | operand1     | operand2     | message
    EqualsOperation              | null         | validOperand | "operand1 cannot be null"
    EqualsOperation              | validOperand | null         | "operand2 cannot be null"
    NotEqualsOperation           | null         | validOperand | "operand1 cannot be null"
    NotEqualsOperation           | validOperand | null         | "operand2 cannot be null"
    LessThanOperation            | null         | validOperand | "operand1 cannot be null"
    LessThanOperation            | validOperand | null         | "operand2 cannot be null"
    GreaterThanOperation         | null         | validOperand | "operand1 cannot be null"
    GreaterThanOperation         | validOperand | null         | "operand2 cannot be null"
    LessThanOrEqualsOperation    | null         | validOperand | "operand1 cannot be null"
    LessThanOrEqualsOperation    | validOperand | null         | "operand2 cannot be null"
    GreaterThanOrEqualsOperation | null         | validOperand | "operand1 cannot be null"
    GreaterThanOrEqualsOperation | validOperand | null         | "operand2 cannot be null"
  }
}
