package io.tiler.unit.internal.queries.expressions.aggregations

import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.aggregations.AllFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.Specification

class AllFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)
  def context = new EvaluationContextBuilder().build()

  def "it always returns true"() {
    given:
    def function = new AllFunction(
      queryContext)

    when:
    def actualResult = function.evaluate(context)

    then:
    actualResult == true
  }
}
