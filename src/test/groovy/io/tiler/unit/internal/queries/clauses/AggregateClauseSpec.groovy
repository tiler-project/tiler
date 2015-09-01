package io.tiler.unit.internal.queries.clauses
import io.tiler.internal.queries.clauses.AggregateClause
import spock.lang.*

class AggregateClauseSpec extends Specification {
  def "field expressions map is unmodifiable"() {
    when:
    def aggregateClause = new AggregateClause(new HashMap<>())

    then:
    aggregateClause.namedExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableMap"
  }
}
