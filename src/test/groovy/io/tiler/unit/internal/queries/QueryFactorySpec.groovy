package io.tiler.unit.internal.queries

import io.tiler.internal.queries.InvalidQueryException
import io.tiler.internal.queries.QueryFactory
import io.tiler.internal.queries.clauses.SortDirection
import io.tiler.internal.queries.clauses.SortExpression
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
import io.tiler.internal.queries.clauses.RegexMetricExpression
import io.tiler.internal.queries.clauses.SimpleMetricExpression
import io.tiler.internal.queries.expressions.aggregations.IntervalFunction
import io.tiler.internal.queries.expressions.arithmetic.SubtractionOperation
import io.tiler.internal.queries.expressions.comparisons.EqualsOperation
import io.tiler.internal.queries.expressions.comparisons.GreaterThanOrEqualsOperation
import io.tiler.internal.queries.expressions.fields.FieldExpression
import io.tiler.internal.queries.expressions.functions.SubstringFunction
import io.tiler.internal.queries.expressions.functions.SumFunction
import io.tiler.internal.queries.expressions.logical.AndOperation
import io.tiler.internal.queries.expressions.logical.OrOperation
import spock.lang.*

import java.util.regex.Pattern

class QueryFactorySpec extends Specification {
  def factory = new QueryFactory()

  def getClauseFromQuery(query, clauseName) {
    switch (clauseName) {
      case "aggregate":
        query.aggregateClause()
        break;
      case "metric":
        query.metricClauses().selectClause()
        break;
      case "point":
        query.pointClauses().selectClause()
        break;
      default:
        throw new RuntimeException("Unexpected clause '" + clauseName + "'")
    }
  }

  def getSortClauseFromQuery(query, clauseName) {
    switch (clauseName) {
      case "metric":
        query.metricClauses().sortClause()
        break;
      case "point":
        query.pointClauses().sortClause()
        break;
      default:
        throw new RuntimeException("Unexpected clause '" + clauseName + "'")
    }
  }

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

  def "where clause with a integer or long constant"() {
    def queryText = """
      from metric.name
      where fieldName == $constantText
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = query.whereClause().expression()
    expression instanceof EqualsOperation
    expression.operand1() instanceof FieldExpression
    expression.operand1().fieldName() == "fieldName"
    expression.operand2() instanceof ConstantExpression
    expression.operand2().value().class == constantType
    expression.operand2().value() == constantValue

    where:
    constantText                                | constantValue                  | constantType
    "1"                                         | 1                              | Integer
    "0"                                         | 0                              | Integer
    "-1"                                        | -1                             | Integer
    Integer.MAX_VALUE.toString()                | Integer.MAX_VALUE              | Integer
    Integer.MIN_VALUE.toString()                | Integer.MIN_VALUE              | Integer
    (Integer.MAX_VALUE.toLong() + 1).toString() | Integer.MAX_VALUE.toLong() + 1 | Long
    (Integer.MIN_VALUE.toLong() - 1).toString() | Integer.MIN_VALUE.toLong() - 1 | Long
    Long.MAX_VALUE.toString()                   | Long.MAX_VALUE                 | Long
    Long.MIN_VALUE.toString()                   | Long.MIN_VALUE                 | Long
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
      where fieldName == 1 && fieldName2 == 2
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
    "&&"     | AndOperation
    "||"     | OrOperation
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
      where fieldName && (fieldName2 && fieldName3)
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
    quoteSymbol | otherQuoteSymbol
    "'"         | "\""
    "\""        | "'"
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

  def "implicitly named field"() {
    def queryText = """
      from metric.name
      $clauseName fieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 1
    namedExpressions["fieldName"] instanceof FieldExpression
    namedExpressions["fieldName"].fieldName() == "fieldName"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "explicitly named field"() {
    def queryText = """
      from metric.name
      $clauseName fieldName as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 1
    def expression = namedExpressions["newFieldName"]
    expression instanceof FieldExpression
    expression.fieldName() == "fieldName"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "multiple expressions"() {
    def queryText = """
      from metric.name
      $clauseName all() as newFieldName, concat(fieldName, ", ", fieldName2) as newFieldName2
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 2
    namedExpressions["newFieldName"] instanceof AllFunction
    namedExpressions["newFieldName2"] instanceof ConcatFunction
    namedExpressions["newFieldName2"].parameters().size() == 3
    namedExpressions["newFieldName2"].parameters()[0] instanceof FieldExpression
    namedExpressions["newFieldName2"].parameters()[0].fieldName() == "fieldName"
    namedExpressions["newFieldName2"].parameters()[1] instanceof ConstantExpression
    namedExpressions["newFieldName2"].parameters()[1].value() == ", "
    namedExpressions["newFieldName2"].parameters()[2] instanceof FieldExpression
    namedExpressions["newFieldName2"].parameters()[2].fieldName() == "fieldName2"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "nested functions"() {
    def queryText = """
      from metric.name
      $clauseName concat(last(fieldName), " - ", substring(last(fieldName2), 0, 10)) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 1
    namedExpressions["newFieldName"] instanceof ConcatFunction
    namedExpressions["newFieldName"].parameters().size() == 3
    namedExpressions["newFieldName"].parameters()[0] instanceof LastFunction
    namedExpressions["newFieldName"].parameters()[0].list() instanceof FieldExpression
    namedExpressions["newFieldName"].parameters()[0].list().fieldName() == "fieldName"
    namedExpressions["newFieldName"].parameters()[1] instanceof ConstantExpression
    namedExpressions["newFieldName"].parameters()[1].value() == " - "
    namedExpressions["newFieldName"].parameters()[2] instanceof SubstringFunction
    namedExpressions["newFieldName"].parameters()[2].value() instanceof LastFunction
    namedExpressions["newFieldName"].parameters()[2].value().list() instanceof FieldExpression
    namedExpressions["newFieldName"].parameters()[2].value().list().fieldName() == "fieldName2"
    namedExpressions["newFieldName"].parameters()[2].beginIndex() instanceof ConstantExpression
    namedExpressions["newFieldName"].parameters()[2].beginIndex().value() == 0
    namedExpressions["newFieldName"].parameters()[2].endIndex() instanceof ConstantExpression
    namedExpressions["newFieldName"].parameters()[2].endIndex().value() == 10

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "interval function"() {
    def queryText = """
      from metric.name
      $clauseName interval(fieldName, 0, 1000) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 1
    def aggregateExpression = namedExpressions["newFieldName"]
    aggregateExpression instanceof IntervalFunction
    aggregateExpression.value() instanceof FieldExpression
    aggregateExpression.value().fieldName == "fieldName"
    aggregateExpression.offset() instanceof ConstantExpression
    aggregateExpression.offset().value() == 0
    aggregateExpression.size() instanceof ConstantExpression
    aggregateExpression.size().value() == 1000

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "all function"() {
    def queryText = """
      from metric.name
      $clauseName all() as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def namedExpressions = getClauseFromQuery(query, clauseName).namedExpressions()
    namedExpressions.size() == 1
    def aggregateExpression = namedExpressions["newFieldName"]
    aggregateExpression instanceof AllFunction

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "concat function with one parameter"() {
    def queryText = """
      from metric.name
      $clauseName concat(fieldName) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression instanceof ConcatFunction
    expression.parameters().size() == 1
    expression.parameters()[0] instanceof FieldExpression
    expression.parameters()[0].fieldName() == "fieldName"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "concat function with multiple parameters"() {
    def queryText = """
      from metric.name
      $clauseName concat(fieldName, ", ", fieldName2) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression instanceof ConcatFunction
    expression.parameters().size() == 3
    expression.parameters()[0] instanceof FieldExpression
    expression.parameters()[0].fieldName() == "fieldName"
    expression.parameters()[1] instanceof ConstantExpression
    expression.parameters()[1].value() == ", "
    expression.parameters()[2] instanceof FieldExpression
    expression.parameters()[2].fieldName() == "fieldName2"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "now function"() {
    def queryText = """
      from metric.name
      $clauseName now() as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression instanceof NowFunction

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "replace function"() {
    def queryText = """
      from metric.name
      $clauseName replace("one two", /one/i, "two") as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression instanceof ReplaceFunction
    expression.value() instanceof ConstantExpression
    expression.value().value() == "one two"
    expression.regex() instanceof ConstantExpression
    expression.regex().value().pattern() == "one"
    expression.regex().value().flags() == Pattern.CASE_INSENSITIVE
    expression.replacement() instanceof ConstantExpression
    expression.replacement().value() == "two"

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "simple number list function"() {
    def queryText = """
      from metric.name
      $clauseName $functionName(fieldName) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression.class == functionClass
    expression.list() instanceof FieldExpression
    expression.list().fieldName() == "fieldName"

    where:
    clauseName   | functionName | functionClass
    "aggregate"  | "mean"       | MeanFunction
    "aggregate"  | "min"        | MinFunction
    "aggregate"  | "max"        | MaxFunction
    "aggregate"  | "sum"        | SumFunction
    "aggregate"  | "first"      | FirstFunction
    "aggregate"  | "last"       | LastFunction
    "metric"     | "mean"       | MeanFunction
    "metric"     | "min"        | MinFunction
    "metric"     | "max"        | MaxFunction
    "metric"     | "sum"        | SumFunction
    "metric"     | "first"      | FirstFunction
    "metric"     | "last"       | LastFunction
    "point"      | "mean"       | MeanFunction
    "point"      | "min"        | MinFunction
    "point"      | "max"        | MaxFunction
    "point"      | "sum"        | SumFunction
    "point"      | "first"      | FirstFunction
    "point"      | "last"       | LastFunction
  }

  def "substring function"() {
    def queryText = """
      from metric.name
      $clauseName substring(fieldName, 1, 2) as newFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def expression = getClauseFromQuery(query, clauseName).namedExpressions()["newFieldName"]
    expression instanceof SubstringFunction
    expression.value() instanceof FieldExpression
    expression.value().fieldName() == "fieldName"
    expression.beginIndex() instanceof ConstantExpression
    expression.beginIndex().value() == 1
    expression.endIndex() instanceof ConstantExpression
    expression.endIndex().value() == 2

    where:
    clauseName << ["aggregate", "metric", "point"]
  }

  def "sort clause"() {
    def queryText = """
      from metric.name
      $clauseName fieldName
      sort sortFieldName
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def selectClause = getClauseFromQuery(query, clauseName)
    selectClause.namedExpressions().size() == 1
    selectClause.namedExpressions()["fieldName"] instanceof FieldExpression
    selectClause.namedExpressions()["fieldName"].fieldName() == "fieldName"
    def sortClause = getSortClauseFromQuery(query, clauseName)
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0] instanceof SortExpression
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName() == "sortFieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending

    where:
    clauseName << ["metric", "point"]
  }

  def "sort clause with multiple expressions"() {
    def queryText = """
      from metric.name
      $clauseName fieldName
      sort sortFieldName, sortFieldName2
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def selectClause = getClauseFromQuery(query, clauseName)
    selectClause.namedExpressions().size() == 1
    selectClause.namedExpressions()["fieldName"] instanceof FieldExpression
    selectClause.namedExpressions()["fieldName"].fieldName() == "fieldName"
    def sortClause = getSortClauseFromQuery(query, clauseName)
    sortClause.sortExpressions().size() == 2
    sortClause.sortExpressions()[0] instanceof SortExpression
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName() == "sortFieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending
    sortClause.sortExpressions()[1] instanceof SortExpression
    sortClause.sortExpressions()[1].expression() instanceof FieldExpression
    sortClause.sortExpressions()[1].expression().fieldName() == "sortFieldName2"
    sortClause.sortExpressions()[1].sortDirection() == SortDirection.Ascending

    where:
    clauseName << ["metric", "point"]
  }

  def "sort clause with explicit ascending sort order"() {
    def queryText = """
      from metric.name
      $clauseName fieldName
      sort sortFieldName asc
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def selectClause = getClauseFromQuery(query, clauseName)
    selectClause.namedExpressions().size() == 1
    selectClause.namedExpressions()["fieldName"] instanceof FieldExpression
    selectClause.namedExpressions()["fieldName"].fieldName() == "fieldName"
    def sortClause = getSortClauseFromQuery(query, clauseName)
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0] instanceof SortExpression
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName() == "sortFieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Ascending

    where:
    clauseName << ["metric", "point"]
  }

  def "sort clause with explicit descending sort order"() {
    def queryText = """
      from metric.name
      $clauseName fieldName
      sort sortFieldName desc
    """

    when:
    def query = factory.parseQuery(queryText)

    then:
    def selectClause = getClauseFromQuery(query, clauseName)
    selectClause.namedExpressions().size() == 1
    selectClause.namedExpressions()["fieldName"] instanceof FieldExpression
    selectClause.namedExpressions()["fieldName"].fieldName() == "fieldName"
    def sortClause = getSortClauseFromQuery(query, clauseName)
    sortClause.sortExpressions().size() == 1
    sortClause.sortExpressions()[0] instanceof SortExpression
    sortClause.sortExpressions()[0].expression() instanceof FieldExpression
    sortClause.sortExpressions()[0].expression().fieldName() == "sortFieldName"
    sortClause.sortExpressions()[0].sortDirection() == SortDirection.Descending

    where:
    clauseName << ["metric", "point"]
  }

  def "all clauses"() {
    def queryText = """
      from metric.name
      where fieldName == 1
      group fieldName
      aggregate all() as all
      metric fieldName
      sort fieldName
      point fieldName
      sort fieldName
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
    query.aggregateClause().namedExpressions().size() == 1
    query.aggregateClause().namedExpressions()["all"] instanceof AllFunction
    query.metricClauses().selectClause().namedExpressions().size() == 1
    query.metricClauses().selectClause().namedExpressions()["fieldName"] instanceof FieldExpression
    query.metricClauses().selectClause().namedExpressions()["fieldName"].fieldName() == "fieldName"
    query.metricClauses().sortClause().sortExpressions().size() == 1
    query.metricClauses().sortClause().sortExpressions()[0] instanceof SortExpression
    query.metricClauses().sortClause().sortExpressions()[0].expression() instanceof FieldExpression
    query.metricClauses().sortClause().sortExpressions()[0].expression().fieldName() == "fieldName"
    query.metricClauses().sortClause().sortExpressions()[0].sortDirection() == SortDirection.Ascending
    query.pointClauses().selectClause().namedExpressions().size() == 1
    query.pointClauses().selectClause().namedExpressions()["fieldName"] instanceof FieldExpression
    query.pointClauses().selectClause().namedExpressions()["fieldName"].fieldName() == "fieldName"
    query.pointClauses().sortClause().sortExpressions().size() == 1
    query.pointClauses().sortClause().sortExpressions()[0] instanceof SortExpression
    query.pointClauses().sortClause().sortExpressions()[0].expression() instanceof FieldExpression
    query.pointClauses().sortClause().sortExpressions()[0].expression().fieldName() == "fieldName"
    query.pointClauses().sortClause().sortExpressions()[0].sortDirection() == SortDirection.Ascending
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
    "where 1 ~= /2/ && 3"  | AndOperation
    "where 1 && 2 ~= /3/"  | AndOperation
    // Level 6
    "where 1 && 2 || 3"    | OrOperation
    "where 1 || 2 && 3"    | AndOperation
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
