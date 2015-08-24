package io.tiler.unit.internal.queries

import io.tiler.internal.queries.InvalidQueryException
import io.tiler.internal.queries.QueryFactory
import io.tiler.internal.queries.expressions.aggregations.AllFunction
import io.tiler.internal.queries.expressions.arithmetic.AdditionOperation
import io.tiler.internal.queries.expressions.arithmetic.DivisionOperation
import io.tiler.internal.queries.expressions.arithmetic.MultiplicationOperation
import io.tiler.internal.queries.expressions.comparisons.GreaterThanOperation
import io.tiler.internal.queries.expressions.comparisons.LessThanOperation
import io.tiler.internal.queries.expressions.comparisons.LessThanOrEqualsOperation
import io.tiler.internal.queries.expressions.comparisons.NotEqualsOperation
import io.tiler.internal.queries.expressions.comparisons.RegexMatchOperation
import io.tiler.internal.queries.expressions.constants.ConstantExpression
import io.tiler.internal.queries.expressions.functions.ConcatFunction
import io.tiler.internal.queries.expressions.functions.FirstFunction
import io.tiler.internal.queries.expressions.functions.LastFunction
import io.tiler.internal.queries.expressions.functions.MaxFunction
import io.tiler.internal.queries.expressions.functions.MeanFunction
import io.tiler.internal.queries.expressions.functions.MinFunction
import io.tiler.internal.queries.expressions.functions.NowFunction
import io.tiler.internal.queries.expressions.functions.ReplaceFunction
import io.tiler.internal.queries.RegexMetricExpression
import io.tiler.internal.queries.SimpleMetricExpression
import io.tiler.internal.queries.expressions.aggregations.IntervalFunction
import io.tiler.internal.queries.expressions.arithmetic.SubtractionOperation
import io.tiler.internal.queries.expressions.comparisons.EqualsOperation
import io.tiler.internal.queries.expressions.comparisons.GreaterThanOrEqualsOperation
import io.tiler.internal.queries.expressions.fields.FieldExpression
import io.tiler.internal.queries.expressions.logical.AndOperation
import io.tiler.internal.queries.expressions.logical.OrOperation
import spock.lang.*

import java.util.regex.Pattern

class QueryFactorySpec extends Specification {
  def factory = new QueryFactory()

  def "from clause with a metric name"() {
    def queryText = "from metric.name"

    when:
    def query = factory.parseQuery(queryText)

    then:
    query.fromClause().metricExpressions().size() == 1
    def metricExpression = query.fromClause().metricExpressions()[0]
    metricExpression instanceof SimpleMetricExpression
    metricExpression.metricName() == "metric.name"
  }

  def "from clause with a metric name regex"() {
    def queryText = "from ${regex}"

    when:
    def query = factory.parseQuery(queryText)

    then:
    query.fromClause().metricExpressions().size() == 1
    def metricExpression = query.fromClause().metricExpressions()[0]
    metricExpression instanceof RegexMetricExpression
    metricExpression.pattern().pattern() == pattern
    metricExpression.pattern().flags() == flag
    metricExpression.pattern().matcher(matches).find() == true

    where:
    regex << ['/^metric\\.name$/', '/^metric\\.name$/i', '/^metric\\/name$/']
    pattern << ['^metric\\.name$', '^metric\\.name$', '^metric/name$']
    flag << [0, Pattern.CASE_INSENSITIVE, 0]
    matches << ['metric.name', 'METRIC.NAME', 'metric/name']
  }

  def "from clause with an invalid metric name regex"() {
    def queryText = "from /metric\\.name/ii"

    when:
    factory.parseQuery(queryText)

    then:
    def e = thrown(InvalidQueryException)
    e.errors.size() == 1
    def error = e.errors[0]
    error.message() == "Invalid options in regex literal. Same option 'i' specified multiple times"
  }

  def "from clause with multiple metrics  "() {
    def queryText = "from metric.name, /^metric\\.name2\$/"

    when:
    def query = factory.parseQuery(queryText)

    then:
    def metricExpressions = query.fromClause().metricExpressions()
    metricExpressions.size() == 2
    metricExpressions[0] instanceof SimpleMetricExpression
    metricExpressions[0].metricName() == "metric.name"
    metricExpressions[1] instanceof RegexMetricExpression
    metricExpressions[1].pattern().pattern() == '^metric\\.name2$'
    metricExpressions[1].pattern().flags() == 0
  }

  def "where clause with a comparison operation"() {
    def queryText = """
      from metric.name
      where fieldName == 1
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof EqualsOperation
    expression.operand1() instanceof FieldExpression
    expression.operand1().fieldName() == "fieldName"
    expression.operand2() instanceof ConstantExpression
    expression.operand2().value() == 1
  }

  def "where clause with a regex match operation"() {
    def queryText = """
      from metric.name
      where fieldName ~= /value/i
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof RegexMatchOperation
    expression.operand1() instanceof FieldExpression
    expression.operand1().fieldName() == "fieldName"
    expression.operand2() instanceof ConstantExpression
    expression.operand2().value() instanceof Pattern
    expression.operand2().value().pattern() == "value"
    expression.operand2().value().flags() == Pattern.CASE_INSENSITIVE
  }

  def "where clause with an logical operation"() {
    def queryText = """
      from metric.name
      where fieldName == 1 and fieldName2 == 2
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof AndOperation
    expression.operand1() instanceof EqualsOperation
    expression.operand1().operand1() instanceof FieldExpression
    expression.operand1().operand1().fieldName() == "fieldName"
    expression.operand1().operand2() instanceof ConstantExpression
    expression.operand1().operand2().value() == 1
    expression.operand2() instanceof EqualsOperation
    expression.operand2().operand1() instanceof FieldExpression
    expression.operand2().operand1().fieldName() == "fieldName2"
    expression.operand2().operand2() instanceof ConstantExpression
    expression.operand2().operand2().value() == 2
  }

  def "expression with a binary operation"() {
    def queryText = """
      from metric.name
      where fieldName ${operator} 1
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    operationClass.isInstance(expression)
    def operand1 = expression.operand1()
    operand1 instanceof FieldExpression
    operand1.fieldName() == "fieldName"
    def operand2 = expression.operand2()
    operand2 instanceof ConstantExpression
    operand2.value() == 1

    where:
    operator | operationClass
    "*"      | MultiplicationOperation
    "/"      | DivisionOperation
    "+"      | AdditionOperation
    "-"      | SubtractionOperation
    "<"      | LessThanOperation
    ">"      | GreaterThanOperation
    "<="     | LessThanOrEqualsOperation
    ">="     | GreaterThanOrEqualsOperation
    "=="     | EqualsOperation
    "!="     | NotEqualsOperation
    "and"    | AndOperation
    "or"     | OrOperation
  }

  def "expression with a constant"() {
    def queryText = """
      from metric.name
      where fieldName == ${constant}
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof EqualsOperation
    def operand1 = expression.operand1()
    operand1 instanceof FieldExpression
    operand1.fieldName() == "fieldName"
    def operand2 = expression.operand2()
    operand2 instanceof ConstantExpression
    operand2.value() == constantValue

    where:
    constant   | constantValue
    "1"        | 1
    "'text'"   | "text"
    "\"text\"" | "text"
    "1s"       | 1000000
  }

  def "expression with parentheses"() {
    def queryText = """
      from metric.name
      where fieldName and (fieldName2 and fieldName3)
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    query.whereClause().expression() instanceof AndOperation
    query.whereClause().expression().operand1() instanceof FieldExpression
    query.whereClause().expression().operand1().fieldName() == "fieldName"
    query.whereClause().expression().operand2() instanceof AndOperation
    query.whereClause().expression().operand2().operand1() instanceof FieldExpression
    query.whereClause().expression().operand2().operand1().fieldName() == "fieldName2"
    query.whereClause().expression().operand2().operand2() instanceof FieldExpression
    query.whereClause().expression().operand2().operand2().fieldName() == "fieldName3"
  }

  def "expression with a string constant containing escaped characters"() {
    def queryText = """
      from metric.name
      where fieldName == ${quoteSymbol}text${otherQuoteSymbol}\\t\\b\\n\\r\\f\\'\\"a\\\\text${quoteSymbol}
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof EqualsOperation
    expression.operand1() instanceof FieldExpression
    expression.operand1().fieldName() == "fieldName"
    expression.operand2() instanceof ConstantExpression
    expression.operand2().value() == "text${otherQuoteSymbol}\t\b\n\r\f\'\"a\\text"

    where:
    quoteSymbol << ["'", "\""]
    otherQuoteSymbol << ["\"", "'"]
  }

  def "group clause with one field"() {
    def queryText = """
      from metric.name
      group fieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def groupClauseFieldExpressions = query.groupClause().fieldExpressions()
    groupClauseFieldExpressions.size() == 1
    groupClauseFieldExpressions[0].fieldName() == "fieldName"
  }

  def "group clause with multiple fields"() {
    def queryText = """
      from metric.name
      group fieldName, fieldName2
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def groupClauseFieldExpressions = query.groupClause().fieldExpressions()
    groupClauseFieldExpressions.size() == 2
    groupClauseFieldExpressions[0].fieldName() == "fieldName"
    groupClauseFieldExpressions[1].fieldName() == "fieldName2"
  }

  def "aggregate clause with an interval aggregation"() {
    def queryText = """
      from metric.name
      aggregate interval(fieldName, 0, 1000) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedAggregateExpressions = query.aggregateClause().namedAggregateExpressions()
    namedAggregateExpressions.size() == 1
    def aggregateExpression = namedAggregateExpressions["newFieldName"]
    aggregateExpression instanceof IntervalFunction
    aggregateExpression.value() instanceof FieldExpression
    aggregateExpression.value().fieldName == "fieldName"
    aggregateExpression.offset() instanceof ConstantExpression
    aggregateExpression.offset().value() == 0
    aggregateExpression.size() instanceof ConstantExpression
    aggregateExpression.size().value() == 1000
  }

  def "aggregate clause with an all aggregation"() {
    def queryText = """
      from metric.name
      aggregate all() as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedAggregateExpressions = query.aggregateClause().namedAggregateExpressions()
    namedAggregateExpressions.size() == 1
    def aggregateExpression = namedAggregateExpressions["newFieldName"]
    aggregateExpression instanceof AllFunction
  }

  def "aggregate clause with multiple aggregations"() {
    def queryText = """
      from metric.name
      aggregate interval(fieldName, 0, 1000) as newFieldName,
        interval(fieldName2, 1, 1001) as newFieldName2
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedAggregateExpressions = query.aggregateClause().namedAggregateExpressions()
    namedAggregateExpressions.size() == 2
    def aggregateExpression = namedAggregateExpressions["newFieldName"]
    aggregateExpression instanceof IntervalFunction
    aggregateExpression.value() instanceof FieldExpression
    aggregateExpression.value().fieldName == "fieldName"
    aggregateExpression.offset() instanceof ConstantExpression
    aggregateExpression.offset().value() == 0
    aggregateExpression.size() instanceof ConstantExpression
    aggregateExpression.size().value() == 1000
    def aggregateExpression2 = namedAggregateExpressions["newFieldName2"]
    aggregateExpression2 instanceof IntervalFunction
    aggregateExpression2.value() instanceof FieldExpression
    aggregateExpression2.value().fieldName == "fieldName2"
    aggregateExpression2.offset() instanceof ConstantExpression
    aggregateExpression2.offset().value() == 1
    aggregateExpression2.size() instanceof ConstantExpression
    aggregateExpression2.size().value() == 1001
  }

  def "metric clause with an explicitly named field"() {
    def queryText = """
      from metric.name
      metric fieldName as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.metricClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["newFieldName"]
    expression instanceof FieldExpression
    expression.fieldName() == "fieldName"
  }

  def "metric clause with an complex expression"() {
    def queryText = """
      from metric.name
      metric concat(fieldName, ", ", fieldName2) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.metricClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["newFieldName"]
    expression instanceof ConcatFunction
    def parameters = expression.parameters();
    parameters.size() == 3
    parameters[0] instanceof FieldExpression
    parameters[0].fieldName() == "fieldName"
    parameters[1] instanceof ConstantExpression
    parameters[1].value() == ", "
    parameters[2] instanceof FieldExpression
    parameters[2 ].fieldName() == "fieldName2"
  }

  def "metric clause with an implicitly named field"() {
    def queryText = """
      from metric.name
      metric fieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.metricClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["fieldName"]
    expression instanceof FieldExpression
    expression.fieldName() == "fieldName"
  }

  def "point clause with an implicitly named field"() {
    def queryText = """
      from metric.name
      point fieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.pointClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["fieldName"]
    expression instanceof FieldExpression
    expression.fieldName() == "fieldName"
  }

  def "point clause with an explicitly named field"() {
    def queryText = """
      from metric.name
      point fieldName as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.pointClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["newFieldName"]
    expression instanceof FieldExpression
    expression.fieldName() == "fieldName"
  }

  def "point clause with an complex expression"() {
    def queryText = """
      from metric.name
      point concat(fieldName, ", ", fieldName2) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = query.pointClause().namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["newFieldName"]
    expression instanceof ConcatFunction
    def parameters = expression.parameters();
    parameters.size() == 3
    parameters[0] instanceof FieldExpression
    parameters[0].fieldName() == "fieldName"
    parameters[1] instanceof ConstantExpression
    parameters[1].value() == ", "
    parameters[2] instanceof FieldExpression
    parameters[2 ].fieldName() == "fieldName2"
  }

  def "now function"() {
    def queryText = """
      from metric.name
      point now() as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.pointClause().namedExpressions()["newFieldName"]
    expression instanceof NowFunction
  }

  def "replace function"() {
    def queryText = """
      from metric.name
      point replace("one two", /one/i, "two") as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.pointClause().namedExpressions()["newFieldName"]
    expression instanceof ReplaceFunction
    expression.value() instanceof ConstantExpression
    expression.value().value() == "one two"
    expression.regex() instanceof ConstantExpression
    expression.regex().value().pattern() == "one"
    expression.regex().value().flags() == Pattern.CASE_INSENSITIVE
    expression.replacement() instanceof ConstantExpression
    expression.replacement().value() == "two"
  }

  def "simple number list function"() {
    def queryText = """
      from metric.name
      point $functionName(fieldName) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.pointClause().namedExpressions()["newFieldName"]
    expression.class == functionClass
    expression.list() instanceof FieldExpression
    expression.list().fieldName() == "fieldName"

    where:
    functionName | functionClass
    "mean"       | MeanFunction
    "min"        | MinFunction
    "max"        | MaxFunction
    "first"      | FirstFunction
    "last"       | LastFunction
  }

  def "concat function with one parameter"() {
    def queryText = """
      from metric.name
      point concat(fieldName) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.pointClause().namedExpressions()["newFieldName"]
    expression instanceof ConcatFunction
    expression.parameters().size() == 1
    expression.parameters()[0] instanceof FieldExpression
    expression.parameters()[0].fieldName() == "fieldName"
  }

  def "concat function with multiple parameters"() {
    def queryText = """
      from metric.name
      point concat(fieldName, ", ", fieldName2) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.pointClause().namedExpressions()["newFieldName"]
    expression instanceof ConcatFunction
    expression.parameters().size() == 3
    expression.parameters()[0] instanceof FieldExpression
    expression.parameters()[0].fieldName() == "fieldName"
    expression.parameters()[1] instanceof ConstantExpression
    expression.parameters()[1].value() == ", "
    expression.parameters()[2] instanceof FieldExpression
    expression.parameters()[2].fieldName() == "fieldName2"
  }

  def "all clauses"() {
    def queryText = """
      from metric.name
      where fieldName == 1
      group fieldName
      aggregate interval(fieldName, 0, 1000) as newFieldName
      metric fieldName
      point fieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    query.fromClause().metricExpressions().size() == 1
    query.fromClause().metricExpressions()[0] instanceof SimpleMetricExpression
    query.fromClause().metricExpressions()[0].metricName() == "metric.name"
    query.whereClause().expression() instanceof EqualsOperation
    query.whereClause().expression().operand1() instanceof FieldExpression
    query.whereClause().expression().operand1().fieldName() == "fieldName"
    query.whereClause().expression().operand2() instanceof ConstantExpression
    query.whereClause().expression().operand2().value() == 1
    query.groupClause().fieldExpressions().size() == 1
    query.groupClause().fieldExpressions()[0].fieldName() == "fieldName"
    query.aggregateClause().namedAggregateExpressions().size() == 1
    query.aggregateClause().namedAggregateExpressions()["newFieldName"] instanceof IntervalFunction
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].value() instanceof FieldExpression
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].value().fieldName == "fieldName"
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].offset() instanceof ConstantExpression
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].offset().value() == 0
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].size() instanceof ConstantExpression
    query.aggregateClause().namedAggregateExpressions()["newFieldName"].size().value() == 1000
    query.metricClause().namedExpressions().size() == 1
    query.metricClause().namedExpressions()["fieldName"] instanceof FieldExpression
    query.metricClause().namedExpressions()["fieldName"].fieldName() == "fieldName"
    query.pointClause().namedExpressions().size() == 1
    query.pointClause().namedExpressions()["fieldName"] instanceof FieldExpression
    query.pointClause().namedExpressions()["fieldName"].fieldName() == "fieldName"
  }

  def "it handles the order of precedence of operators correctly"() {
    def queryText = """
      from metric.name
      $whereClauseText
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    query.whereClause().expression().class == outerExpressionClass

    where:
    whereClauseText        | outerExpressionClass
    // Level 1
    "where 1 * 2 / 3"      | DivisionOperation
    "where 1 / 2 * 3"      | MultiplicationOperation
    // Levels 1 and 2
    "where 1 * 2 + 3"      | AdditionOperation
    "where 1 + 2 * 3"      | AdditionOperation
    // Level 2
    "where 1 + 2 - 3"      | SubtractionOperation
    "where 1 - 2 + 3"      | AdditionOperation
    // Levels 2 and 3
    "where 1 + 2 < 3"      | LessThanOperation
    "where 1 < 2 + 3"      | LessThanOperation
    // Level 3
    "where 1 < 2 > 3"      | GreaterThanOperation
    "where 1 > 2 < 3"      | LessThanOperation
    "where 1 > 2 <= 3"     | LessThanOrEqualsOperation
    "where 1 <= 2 > 3"     | GreaterThanOperation
    "where 1 <= 2 >= 3"    | GreaterThanOrEqualsOperation
    "where 1 >= 2 <= 3"    | LessThanOrEqualsOperation
    // Levels 3 and 4
    "where 1 < 2 == 3"     | EqualsOperation
    "where 1 == 2 < 3"     | EqualsOperation
    // Level 4
    "where 1 == 2 != 3"    | NotEqualsOperation
    "where 1 != 2 == 3"    | EqualsOperation
    // Levels 4 and 5
    "where 1 == 2 ~= /3/"  | RegexMatchOperation
    "where 1 ~= /2/ == 3"  | RegexMatchOperation
    // Only one operator in Level 5
    // Levels 5 and 6
    "where 1 ~= /2/ and 3" | AndOperation
    "where 1 and 2 ~= /3/" | AndOperation
    // Level 6
    "where 1 and 2 or 3"   | OrOperation
    "where 1 or 2 and 3"   | AndOperation
  }

  def "invalid query"() {
    def queryText = """
      from metric.name metric.name2
    """

    when:
    factory.parseQuery(queryText)

    then:
    def e = thrown(InvalidQueryException)
    e.message == "Line 2:23\n" +
                 "      from metric.name metric.name2\n" +
                 "                       ^ extraneous input 'metric.name2' expecting {<EOF>, ',', 'where', 'group', 'aggregate', 'point', 'metric'}"
    e.errors.size() == 1
    e.errors[0].line() == 2
    e.errors[0].column() == 23
    e.errors[0].message() == "extraneous input 'metric.name2' expecting {<EOF>, ',', 'where', 'group', 'aggregate', 'point', 'metric'}"
  }
}
