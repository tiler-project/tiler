package io.tiler.unit.internal.queries
import io.tiler.internal.queries.AggregateClause
import spock.lang.*

class AggregateClauseSpec extends Specification {
  def "field expressions map is unmodifiable"() {
    when:
    def aggregateClause = new AggregateClause(new HashMap<>())

    then:
    aggregateClause.namedAggregateExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableMap"
  }
}
