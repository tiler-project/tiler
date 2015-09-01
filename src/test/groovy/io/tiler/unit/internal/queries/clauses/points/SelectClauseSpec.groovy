package io.tiler.unit.internal.queries.clauses.points

import io.tiler.internal.queries.clauses.points.SelectClause
import spock.lang.*

class SelectClauseSpec extends Specification {
  def "named expressions map is unmodifiable"() {
    when:
    def pointClause = new SelectClause(new HashMap<>())

    then:
    pointClause.namedExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableMap"
  }
}
