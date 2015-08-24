package io.tiler.unit.internal.queries

import io.tiler.internal.queries.GroupClause
import spock.lang.*

class GroupClauseSpec extends Specification {
  def "field expressions list is unmodifiable"() {
    when:
    def groupClause = new GroupClause(new ArrayList<>())

    then:
    groupClause.fieldExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableRandomAccessList"
  }
}
