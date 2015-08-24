package io.tiler.unit.internal.queries.expressions.arithmetic

import io.tiler.internal.queries.EvaluationException
import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.arithmetic.AdditionOperation
import io.tiler.internal.queries.expressions.arithmetic.DivisionOperation
import io.tiler.internal.queries.expressions.arithmetic.MultiplicationOperation
import io.tiler.internal.queries.expressions.arithmetic.SubtractionOperation
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class ArithmeticOperationSpec extends Specification {
  @Shared validOperand = 1
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it performs arithmetic on operands 1 and 2"() {
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
    operation               | operand1 | operand2 | result
    MultiplicationOperation | 3        | 2        | 6
    MultiplicationOperation | 3.5      | 2        | 7
    MultiplicationOperation | 3        | 2.5      | 7.5
    MultiplicationOperation | 3.5      | 2.5      | 8.75
    DivisionOperation       | 3        | 2        | 1.5
    DivisionOperation       | 3.5      | 2        | 1.75
    DivisionOperation       | 3        | 2.5      | 1.2
    DivisionOperation       | 3.5      | 2.5      | 1.4
    AdditionOperation       | 3        | 2        | 5
    AdditionOperation       | 3.5      | 2        | 5.5
    AdditionOperation       | 3        | 2.5      | 5.5
    AdditionOperation       | 3.5      | 2.5      | 6
    SubtractionOperation    | 3        | 2        | 1
    SubtractionOperation    | 3.5      | 2        | 1.5
    SubtractionOperation    | 3        | 2.5      | 0.5
    SubtractionOperation    | 3.5      | 2.5      | 1
  }

  def "it validates the types of the operands"() {
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
    operation               | operand1     | operand2      | message
    MultiplicationOperation | ""           | validOperand  | "operand1 must be a Number"
    MultiplicationOperation | validOperand | ""            | "operand2 must be a Number"
    DivisionOperation       | ""           | validOperand  | "operand1 must be a Number"
    DivisionOperation       | validOperand | ""            | "operand2 must be a Number"
    AdditionOperation       | ""           | validOperand  | "operand1 must be a Number"
    AdditionOperation       | validOperand | ""            | "operand2 must be a Number"
    SubtractionOperation    | ""           | validOperand  | "operand1 must be a Number"
    SubtractionOperation    | validOperand | ""            | "operand2 must be a Number"
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
    operation               | operand1     | operand2      | message
    MultiplicationOperation | null         | validOperand  | "operand1 cannot be null"
    MultiplicationOperation | validOperand | null          | "operand2 cannot be null"
    DivisionOperation       | null         | validOperand  | "operand1 cannot be null"
    DivisionOperation       | validOperand | null          | "operand2 cannot be null"
    AdditionOperation       | null         | validOperand  | "operand1 cannot be null"
    AdditionOperation       | validOperand | null          | "operand2 cannot be null"
    SubtractionOperation    | null         | validOperand  | "operand1 cannot be null"
    SubtractionOperation    | validOperand | null          | "operand2 cannot be null"
  }
}
