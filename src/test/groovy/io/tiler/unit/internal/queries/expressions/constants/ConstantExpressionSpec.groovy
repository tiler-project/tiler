package io.tiler.unit.internal.queries.expressions.constants

import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class ConstantExpressionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)

  def "it returns its value when evaluated"() {
    given:
    def context = new EvaluationContextBuilder().build()
    def expression = new ConstantExpression(queryContext, 1L)

    when:
    def result = expression.evaluate(context)

    then:
    result == 1L
  }
}
