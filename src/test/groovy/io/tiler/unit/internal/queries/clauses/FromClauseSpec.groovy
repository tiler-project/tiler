package io.tiler.unit.internal.queries.clauses
import io.tiler.internal.queries.clauses.FromClause
import spock.lang.*

class FromClauseSpec extends Specification {
  def "metric expressions list is unmodifiable"() {
    when:
    def fromClause = new FromClause(new ArrayList<>())

    then:
    fromClause.metricExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableRandomAccessList"
  }
}
