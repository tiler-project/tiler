package io.tiler.unit.internal.queries.expressions.functions
import io.tiler.internal.queries.expressions.functions.NowFunction
import io.tiler.unit.internal.queries.expressions.builders.EvaluationContextBuilder
import spock.lang.*

import java.time.Clock

class NowFunctionSpec extends Specification {
  def "it returns the current time in UTC in microseconds"() {
    given:
    def clock = Mock(Clock)
    def context = new EvaluationContextBuilder()
      .clock(clock)
      .build()
    def function = new NowFunction()

    when:
    def result = function.evaluate(context)

    then:
    1 * clock.millis() >> 1234
    result == 1234 * 1000
  }
}
