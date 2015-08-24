package io.tiler.unit.internal.queries.expressions.functions

import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.ConcatFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

class ConcatFunctionSpec extends Specification {
  def queryContext = new QueryContext("query", 1, 2)

  def "field expressions list is unmodifiable"() {
    when:
    def function = new ConcatFunction(queryContext, new ArrayList<>())

    then:
    function.parameters().getClass().canonicalName == "java.util.Collections.UnmodifiableRandomAccessList"
  }

  def "it concatenates strings"() {
    given:
    def parameters = [
      new ConstantExpression(queryContext, "one"),
      new ConstantExpression(queryContext, " "),
      new ConstantExpression(queryContext, "two"),
      new ConstantExpression(queryContext, " "),
      new ConstantExpression(queryContext, "three")]
    def context = new EvaluationContextBuilder().build()
    def function = new ConcatFunction(queryContext, parameters)

    when:
    def result = function.evaluate(context)

    then:
    result == "one two three"
  }

  def "it concatenates an empty list"() {
    given:
    def parameters = []
    def context = new EvaluationContextBuilder().build()
    def function = new ConcatFunction(queryContext, parameters)

    when:
    def result = function.evaluate(context)

    then:
    result == ""
  }
}
