package io.tiler.unit.internal.queries.clauses.points

import io.tiler.internal.queries.QueryContext
import io.tiler.internal.queries.clauses.SortDirection
import io.tiler.internal.queries.clauses.SortExpression
import io.tiler.internal.queries.clauses.points.SortClause
import io.tiler.internal.queries.expressions.arithmetic.SubtractionOperation
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.fields.FieldExpression
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SortClauseSpec extends Specification {
  def Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));
  def queryContext = new QueryContext("query", 1, 2)

  def createArrayOf1Metric(JsonArray points) {
    return new JsonArray()
      .addObject(new JsonObject()
        .putArray("points", points))
  }
  
  def "it sorts the points based on one field"() {
    given:
    def points = new JsonArray()
      .addObject(new JsonObject()
        .putNumber("fieldName", 3))
      .addObject(new JsonObject()
        .putNumber("fieldName", 2))
      .addObject(new JsonObject()
        .putNumber("fieldName", 1))
    def metrics = createArrayOf1Metric(points)

    when:
    def sortClause = new SortClause([
      new SortExpression(
        queryContext,
        new FieldExpression(queryContext, "fieldName"),
        SortDirection.Ascending)])

    then:
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName == "fieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending

    when:
    sortClause.applyToMetrics(clock, metrics)

    then:
    metrics.size() == 1
    metrics[0].getArray("points").size() == 3
    metrics[0].getArray("points")[0].getNumber("fieldName") == 1
    metrics[0].getArray("points")[1].getNumber("fieldName") == 2
    metrics[0].getArray("points")[2].getNumber("fieldName") == 3
  }

  def "it sorts the points based on two fields"() {
    given:
    def points = new JsonArray()
      .addObject(new JsonObject()
        .putNumber("fieldName", 1)
        .putString("fieldName2", "c"))
      .addObject(new JsonObject()
        .putNumber("fieldName", 1)
        .putString("fieldName2", "b"))
      .addObject(new JsonObject()
        .putNumber("fieldName", 1)
        .putString("fieldName2", "a"))
    def metrics = createArrayOf1Metric(points)

    when:
    def sortClause = new SortClause([
      new SortExpression(
        queryContext,
        new FieldExpression(queryContext, "fieldName"),
        SortDirection.Ascending),
      new SortExpression(
        queryContext,
        new FieldExpression(queryContext, "fieldName2"),
        SortDirection.Ascending)])

    then:
    sortClause.sortExpressions().size() == 2
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName == "fieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending
    sortClause.sortExpressions()[1].expression() instanceof FieldExpression
    sortClause.sortExpressions()[1].expression().fieldName == "fieldName2"
    sortClause.sortExpressions()[1].sortDirection() == SortDirection.Ascending

    when:
    sortClause.applyToMetrics(clock, metrics)

    then:
    metrics.size() == 1
    metrics[0].getArray("points").size() == 3
    metrics[0].getArray("points")[0].getNumber("fieldName") == 1
    metrics[0].getArray("points")[0].getString("fieldName2") == "a"
    metrics[0].getArray("points")[1].getNumber("fieldName") == 1
    metrics[0].getArray("points")[1].getString("fieldName2") == "b"
    metrics[0].getArray("points")[2].getNumber("fieldName") == 1
    metrics[0].getArray("points")[2].getString("fieldName2") == "c"
  }

  def "it sorts the points in based on a field in descending order"() {
    given:
    def points = new JsonArray()
      .addObject(new JsonObject()
        .putNumber("fieldName", 1))
      .addObject(new JsonObject()
        .putNumber("fieldName", 2))
      .addObject(new JsonObject()
        .putNumber("fieldName", 3))
    def metrics = createArrayOf1Metric(points)

    when:
    def sortClause = new SortClause([
      new SortExpression(
        queryContext,
        new FieldExpression(queryContext, "fieldName"),
        SortDirection.Descending)])

    then:
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName == "fieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Descending

    when:
    sortClause.applyToMetrics(clock, metrics)

    then:
    metrics.size() == 1
    metrics[0].getArray("points").size() == 3
    metrics[0].getArray("points")[0].getNumber("fieldName") == 3
    metrics[0].getArray("points")[1].getNumber("fieldName") == 2
    metrics[0].getArray("points")[2].getNumber("fieldName") == 1
  }

  def "it sorts the points based on an expression"() {
    given:
    def points = new JsonArray()
      .addObject(new JsonObject()
        .putNumber("fieldName", 1))
      .addObject(new JsonObject()
        .putNumber("fieldName", 2))
      .addObject(new JsonObject()
        .putNumber("fieldName", 3))
    def metrics = createArrayOf1Metric(points)

    when:
    def sortClause = new SortClause([
      new SortExpression(
        queryContext,
        new SubtractionOperation(
          queryContext,
          new ConstantExpression(queryContext, 10),
          new FieldExpression(queryContext, "fieldName")),
        SortDirection.Ascending)])

    then:
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0].expression() instanceof SubtractionOperation
    sortClause.sortExpressions()[0].expression().operand1() instanceof ConstantExpression
    sortClause.sortExpressions()[0].expression().operand1().value() == 10
    sortClause.sortExpressions()[0].expression().operand2() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().operand2().fieldName == "fieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending

    when:
    sortClause.applyToMetrics(clock, metrics)

    then:
    metrics.size() == 1
    metrics[0].getArray("points").size() == 3
    metrics[0].getArray("points")[0].getNumber("fieldName") == 3
    metrics[0].getArray("points")[1].getNumber("fieldName") == 2
    metrics[0].getArray("points")[2].getNumber("fieldName") == 1
  }

  def "sort expressions list is unmodifiable"() {
    when:
    def sortClause = new SortClause(new ArrayList<>())

    then:
    sortClause.sortExpressions().getClass().canonicalName == "java.util.Collections.UnmodifiableRandomAccessList"
  }
}
