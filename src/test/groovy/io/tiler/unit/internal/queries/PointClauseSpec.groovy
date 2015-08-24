package io.tiler.unit.internal.queries

import io.tiler.internal.queries.PointClause
import spock.lang.*

class PointClauseSpec extends Specification {
  def "named expressions map is unmodifiable"() {
    when:
    def pointClause = new PointClause(new HashMap<>())

    then:
    pointClause.namedExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableMap"
  }
}
