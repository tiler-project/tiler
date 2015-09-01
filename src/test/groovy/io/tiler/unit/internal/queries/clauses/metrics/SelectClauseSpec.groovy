package io.tiler.unit.internal.queries.clauses.metrics
import io.tiler.internal.queries.clauses.metrics.SelectClause
import spock.lang.Specification

class SelectClauseSpec extends Specification {
  def "named expressions map is unmodifiable"() {
    when:
    def pointClause = new SelectClause(new HashMap<>())

    then:
    pointClause.namedExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableMap"
  }
}
