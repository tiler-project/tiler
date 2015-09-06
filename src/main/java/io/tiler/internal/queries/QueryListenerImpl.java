package io.tiler.internal.queries;

import io.tiler.core.regex.InvalidPatternOptionsException;
import io.tiler.core.regex.PatternOptionsParser;
import io.tiler.core.time.TimePeriodParser;
import io.tiler.internal.queries.builders.*;
import io.tiler.internal.queries.clauses.SortDirection;
import io.tiler.internal.queries.clauses.SortExpression;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.aggregations.AllFunction;
import io.tiler.internal.queries.expressions.aggregations.IntervalFunction;
import io.tiler.internal.queries.expressions.arithmetic.AdditionOperation;
import io.tiler.internal.queries.expressions.arithmetic.DivisionOperation;
import io.tiler.internal.queries.expressions.arithmetic.MultiplicationOperation;
import io.tiler.internal.queries.expressions.arithmetic.SubtractionOperation;
import io.tiler.internal.queries.expressions.comparisons.*;
import io.tiler.internal.queries.expressions.constants.ConstantExpression;
import io.tiler.internal.queries.expressions.fields.FieldExpression;
import io.tiler.internal.queries.expressions.functions.*;
import io.tiler.internal.queries.expressions.logical.AndOperation;
import io.tiler.internal.queries.expressions.logical.NotOperation;
import io.tiler.internal.queries.expressions.logical.OrOperation;
import io.tiler.internal.queries.grammar.QueryLexer;
import io.tiler.internal.queries.grammar.QueryListener;
import io.tiler.internal.queries.grammar.QueryParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class QueryListenerImpl implements QueryListener {
  private static final PatternOptionsParser patternOptionsParser = new PatternOptionsParser();

  private ArrayList<QueryError> errors = new ArrayList<>();

  private Query query;
  private QueryBuilder queryBuilder;
  private HashMap<ParserRuleContext, Expression> expressions = new HashMap<>();
  private HashMap<ParserRuleContext, NamedExpression> namedExpressions = new HashMap<>();
  private HashMap<ParserRuleContext, SortExpression> sortExpressions = new HashMap<>();
  private String queryText;

  public QueryListenerImpl(String queryText) {
    this.queryText = queryText;
  }

  private QueryContext createQueryContext(ParserRuleContext ctx) {
    return createQueryContext(ctx.getStart());
  }

  private QueryContext createQueryContext(Token start) {
    return new QueryContext(queryText, start.getLine(), start.getCharPositionInLine());
  }

  private Pattern convertRegexTextToPattern(Token token) throws InvalidPatternOptionsException {
    String text = token.getText();

    if (text.length() < 1 || !text.substring(0, 1).equals("/")) {
      throw new RuntimeException("Regex literal does not start with a forward slash");
    }

    int index = 1;
    int length = text.length();
    StringBuilder patternBuilder = new StringBuilder(text.length() - 2);
    String options;

    while (true) {
      char firstChar = text.charAt(index);

      if (firstChar == '/') {
        options = text.substring(index + 1);
        break;
      }
      if (firstChar == '\\') {
        if (index + 1 == length) {
          throw new RuntimeException("Regex literal does not end with a forward slash");
        }

        char secondChar = text.charAt(index + 1);

        if (secondChar == '/') {
          patternBuilder.append(secondChar);
        }
        else {
          patternBuilder.append(firstChar);
          patternBuilder.append(secondChar);
        }

        index += 2;
      }
      else {
        patternBuilder.append(firstChar);
        index++;
      }

      if (index >= length) {
        options = "";
        break;
      }
    }

    return Pattern.compile(patternBuilder.toString(), patternOptionsParser.parsePatternOptions(options));
  }

  private String stringLiteralToString(String text) {
    if (text.length() < 1) {
      throw new RuntimeException("String literal does not start with a single or double quote symbol");
    }

    char firstChar = text.charAt(0);

    if (firstChar != '\'' && firstChar != '"') {

    }

    if (text.length() < 2 || text.charAt(text.length() - 1) != firstChar) {
      throw new RuntimeException("String literal does not end with a " + (firstChar == '\'' ? "single" : "double") + " quote symbol");
    }

    text = text.substring(1, text.length() - 1);

    return StringEscapeUtils.unescapeJava(text);
  }

  public Query query() {
    return query;
  }

  public List<QueryError> errors() {
    return Collections.unmodifiableList(errors);
  }

  @Override
  public void enterQuery(QueryParser.QueryContext ctx) {
    queryBuilder = new QueryBuilder();
  }

  @Override
  public void exitQuery(QueryParser.QueryContext ctx) {
    query = queryBuilder.build();
  }

  @Override
  public void enterFromClause(QueryParser.FromClauseContext ctx) {

  }

  @Override
  public void exitFromClause(QueryParser.FromClauseContext ctx) {
    FromClauseBuilder builder = new FromClauseBuilder();

    for (QueryParser.MetricExprContext expr : ctx.exprs) {
      if (expr.ID() != null) {
        builder.metricExpression(expr.ID().getText());
      }
      else {
        try {
          builder.metricExpression(convertRegexTextToPattern(expr.REGEX().getSymbol()));
        } catch (InvalidPatternOptionsException e) {
          errors.add(new QueryError(ctx.getStart(), "Invalid options in regex literal. " + e.getMessage()));
        }
      }
    }

    queryBuilder.fromClause(builder.build());
  }

  @Override
  public void enterWhereClause(QueryParser.WhereClauseContext ctx) {

  }

  @Override
  public void exitWhereClause(QueryParser.WhereClauseContext ctx) {
    WhereClauseBuilder builder = new WhereClauseBuilder();
    builder.expression(expressions.get(ctx.expr()));
    queryBuilder.whereClause(builder.build());
  }

  @Override
  public void enterGroupClause(QueryParser.GroupClauseContext ctx) {

  }

  @Override
  public void exitGroupClause(QueryParser.GroupClauseContext ctx) {
    GroupClauseBuilder builder = new GroupClauseBuilder();

    for (Token field : ctx.fields) {
      builder.fieldExpression(new FieldExpression(createQueryContext(ctx), field.getText()));
    }

    queryBuilder.groupClause(builder.build());
  }

  @Override
  public void enterAggregateClause(QueryParser.AggregateClauseContext ctx) {

  }

  @Override
  public void exitAggregateClause(QueryParser.AggregateClauseContext ctx) {
    AggregateClauseBuilder builder = new AggregateClauseBuilder();

    for (QueryParser.NamedExprContext namedExpr : ctx.namedExprs) {
      NamedExpression namedExpression = namedExpressions.get(namedExpr);
      builder.namedExpression(namedExpression.name(), namedExpression.expression());
    }

    queryBuilder.aggregateClause(builder.build());
  }

  @Override
  public void enterPointSelectClause(QueryParser.PointSelectClauseContext ctx) {

  }

  @Override
  public void exitPointSelectClause(QueryParser.PointSelectClauseContext ctx) {
    PointSelectClauseBuilder builder = new PointSelectClauseBuilder();

    for (QueryParser.NamedExprContext namedExpr : ctx.namedExprs) {
      NamedExpression namedExpression = namedExpressions.get(namedExpr);
      builder.namedExpression(namedExpression.name(), namedExpression.expression());
    }

    queryBuilder.pointClauses().selectClause(builder.build());
  }

  @Override
  public void enterPointSortClause(QueryParser.PointSortClauseContext ctx) {

  }

  @Override
  public void exitPointSortClause(QueryParser.PointSortClauseContext ctx) {
    PointSortClauseBuilder builder = new PointSortClauseBuilder();

    for (QueryParser.SortExprContext sortExpr : ctx.sortExprs) {
      SortExpression sortExpression = sortExpressions.get(sortExpr);
      builder.sortExpression(sortExpression);
    }

    queryBuilder.pointClauses().sortClause(builder.build());
  }

  @Override
  public void enterMetricSelectClause(QueryParser.MetricSelectClauseContext ctx) {

  }

  @Override
  public void exitMetricSelectClause(QueryParser.MetricSelectClauseContext ctx) {
    MetricSelectClauseBuilder builder = new MetricSelectClauseBuilder();

    for (QueryParser.NamedExprContext namedExpr : ctx.namedExprs) {
      NamedExpression namedExpression = namedExpressions.get(namedExpr);
      builder.namedExpression(namedExpression.name(), namedExpression.expression());
    }

    queryBuilder.metricClauses().selectClause(builder.build());
  }

  @Override
  public void enterMetricSortClause(QueryParser.MetricSortClauseContext ctx) {

  }

  @Override
  public void exitMetricSortClause(QueryParser.MetricSortClauseContext ctx) {
    MetricSortClauseBuilder builder = new MetricSortClauseBuilder();

    for (QueryParser.SortExprContext sortExpr : ctx.sortExprs) {
      SortExpression sortExpression = sortExpressions.get(sortExpr);
      builder.sortExpression(sortExpression);
    }

    queryBuilder.metricClauses().sortClause(builder.build());
  }

  @Override
  public void enterField(QueryParser.FieldContext ctx) {

  }

  @Override
  public void exitField(QueryParser.FieldContext ctx) {
    FieldExpression expression = new FieldExpression(createQueryContext(ctx), ctx.getText());
    expressions.put(ctx, expression);
  }

  @Override
  public void enterUnaryOp(QueryParser.UnaryOpContext ctx) {

  }

  @Override
  public void exitUnaryOp(QueryParser.UnaryOpContext ctx) {
    Expression expression;
    Expression operand = expressions.get(ctx.expr());

    switch (ctx.op.getType()) {
      case QueryLexer.EXCLAMATION_MARK:
        expression = new NotOperation(createQueryContext(ctx), operand);
        break;
      default:
        throw new RuntimeException("Unexpected unary operator '" + ctx.op.getText() + "'");
    }

    expressions.put(ctx, expression);
  }

  @Override
  public void enterFunc(QueryParser.FuncContext ctx) {

  }

  private boolean validParameterCount(QueryParser.FuncContext ctx, int count) {
    if (ctx.expr().size() != count) {
      errors.add(new QueryError(ctx.getStart(), "Function needs " + count + " parameter" + (count != 1 ? "s" : "") + " but actually has " + ctx.expr().size()));
      return false;
    }

    return true;
  }

  private boolean validMinParameterCount(QueryParser.FuncContext ctx, int minCount) {
    if (ctx.expr().size() < minCount) {
      errors.add(new QueryError(ctx.getStart(), "Function needs at least " + minCount + " parameter" + (minCount != 1 ? "s" : "") + " but actually has " + ctx.expr().size()));
      return false;
    }

    return true;
  }

  @Override
  public void exitFunc(QueryParser.FuncContext ctx) {
    Function function;

    switch (ctx.func.getText()) {
      case "interval":
        if (!validParameterCount(ctx, 3)) {
          return;
        }

        function = new IntervalFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)),
          expressions.get(ctx.expr().get(1)),
          expressions.get(ctx.expr().get(2)));
        break;
      case "all":
        if (!validParameterCount(ctx, 0)) {
          return;
        }

        function = new AllFunction(
          createQueryContext(ctx));
        break;
      case "now":
        if (!validParameterCount(ctx, 0)) {
          return;
        }

        function = new NowFunction(
          createQueryContext(ctx));
        break;
      case "replace":
        if (!validParameterCount(ctx, 3)) {
          return;
        }

        function = new ReplaceFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)),
          expressions.get(ctx.expr().get(1)),
          expressions.get(ctx.expr().get(2)));
        break;
      case "substring":
        if (!validParameterCount(ctx, 3)) {
          return;
        }

        function = new SubstringFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)),
          expressions.get(ctx.expr().get(1)),
          expressions.get(ctx.expr().get(2)));
        break;
      case "concat":
        if (!validMinParameterCount(ctx, 1)) {
          return;
        }

        ArrayList<Expression> parameters = new ArrayList<>();

        for (QueryParser.ExprContext parameter : ctx.expr()) {
          parameters.add(expressions.get(parameter));
        }

        function = new ConcatFunction(
          createQueryContext(ctx),
          parameters);
        break;
      case "mean":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new MeanFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      case "min":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new MinFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      case "max":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new MaxFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      case "sum":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new SumFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      case "first":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new FirstFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      case "last":
        if (!validParameterCount(ctx, 1)) {
          return;
        }

        function = new LastFunction(
          createQueryContext(ctx),
          expressions.get(ctx.expr().get(0)));
        break;
      default:
        errors.add(new QueryError(ctx.func, "Unknown function '" + ctx.func.getText() + "'"));
        return;
    }

    expressions.put(ctx, function);
  }

  @Override
  public void enterConstant(QueryParser.ConstantContext ctx) {

  }

  @Override
  public void exitConstant(QueryParser.ConstantContext ctx) {
    Expression expression;

    switch (ctx.constant.getType()) {
      case QueryLexer.BOOLEAN:
        expression = new ConstantExpression<>(createQueryContext(ctx), Boolean.parseBoolean(ctx.getText()));
        break;
      case QueryLexer.INTEGER:
        long value = Long.parseLong(ctx.getText());

        if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
          expression = new ConstantExpression<>(createQueryContext(ctx), (int) value);
        }
        else {
          expression = new ConstantExpression<>(createQueryContext(ctx), value);
        }

        break;
      case QueryLexer.STRING:
        expression = new ConstantExpression<>(createQueryContext(ctx), stringLiteralToString(ctx.getText()));
        break;
      case QueryLexer.TIME_PERIOD:
        expression = new ConstantExpression<>(
          createQueryContext(ctx),
          TimePeriodParser.parseTimePeriodToMicroseconds(ctx.getText()));
        break;
      case QueryLexer.REGEX:
        expression = createRegexConstantExpression(ctx.constant);

        break;
      default:
        throw new RuntimeException("Unexpected constant '" + ctx.getText() + "'");
    }

    expressions.put(ctx, expression);
  }

  private Expression createRegexConstantExpression(Token regexToken) {
    Expression expression;
    try {
      expression = new ConstantExpression<>(createQueryContext(regexToken), convertRegexTextToPattern(regexToken));
    } catch (InvalidPatternOptionsException e) {
      errors.add(new QueryError(regexToken, "Invalid options in regex literal. " + e.getMessage()));
      expression = null;
    } return expression;
  }

  @Override
  public void enterParentheses(QueryParser.ParenthesesContext ctx) {

  }

  @Override
  public void exitParentheses(QueryParser.ParenthesesContext ctx) {
    expressions.put(ctx, expressions.get(ctx.expr()));
  }

  @Override
  public void enterBinaryOp(QueryParser.BinaryOpContext ctx) {

  }

  @Override
  public void exitBinaryOp(QueryParser.BinaryOpContext ctx) {
    Expression expression;
    Expression operand1 = expressions.get(ctx.expr(0));
    Expression operand2 = expressions.get(ctx.expr(1));

    switch (ctx.op.getType()) {
      case QueryLexer.ASTERISK:
        expression = new MultiplicationOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.FORWARD_SLASH:
        expression = new DivisionOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.PLUS:
        expression = new AdditionOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.MINUS:
        expression = new SubtractionOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.LESS_THAN:
        expression = new LessThanOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.GREATER_THAN:
        expression = new GreaterThanOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.LESS_THAN_OR_EQUALS:
        expression = new LessThanOrEqualsOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.GREATER_THAN_OR_EQUALS:
        expression = new GreaterThanOrEqualsOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.EQUALS:
        expression = new EqualsOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.NOT_EQUALS:
        expression = new NotEqualsOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.MATCHES:
        expression = new RegexMatchOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.AND:
        expression = new AndOperation(createQueryContext(ctx), operand1, operand2);
        break;
      case QueryLexer.OR:
        expression = new OrOperation(createQueryContext(ctx), operand1, operand2);
        break;
      default:
        throw new RuntimeException("Unexpected binary operator '" + ctx.op.getText() + "'");
    }

    expressions.put(ctx, expression);
  }

  @Override
  public void enterMetricExpr(QueryParser.MetricExprContext ctx) {

  }

  @Override
  public void exitMetricExpr(QueryParser.MetricExprContext ctx) {

  }

  @Override
  public void enterNamedExpr(QueryParser.NamedExprContext ctx) {

  }

  @Override
  public void exitNamedExpr(QueryParser.NamedExprContext ctx) {
    String name = ctx.ID().getText();
    Expression expression;

    if (ctx.expr() == null) {
      expression = new FieldExpression(createQueryContext(ctx), name);
    } else {
      expression = expressions.get(ctx.expr());
    }

    NamedExpression namedExpression = new NamedExpression(name, expression);
    namedExpressions.put(ctx, namedExpression);
  }

  @Override
  public void enterSortExpr(QueryParser.SortExprContext ctx) {
  }

  @Override
  public void exitSortExpr(QueryParser.SortExprContext ctx) {
    Expression expression = expressions.get(ctx.expr());
    SortDirection sortDirection;

    if (ctx.sortDirection == null) {
      sortDirection = SortDirection.Ascending;
    }
    else {
      switch (ctx.sortDirection.getText()) {
        case "asc":
          sortDirection = SortDirection.Ascending;
          break;
        case "desc":
          sortDirection = SortDirection.Descending;
          break;
        default:
          throw new RuntimeException("Unexpected sort direction '" + ctx.sortDirection.getText() + "'");
      }
    }

    SortExpression sortExpression = new SortExpression(
      createQueryContext(ctx),
      expression,
      sortDirection);
    sortExpressions.put(ctx, sortExpression);
  }

  @Override
  public void visitTerminal(TerminalNode terminalNode) {

  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    errors.add(new QueryError(errorNode.getSymbol(), errorNode.getText()));
  }

  @Override
  public void enterEveryRule(ParserRuleContext parserRuleContext) {

  }

  @Override
  public void exitEveryRule(ParserRuleContext parserRuleContext) {

  }

  private class NamedExpression {
    private final String name;
    private final Expression expression;

    public NamedExpression(String name, Expression expression) {
      this.name = name;
      this.expression = expression;
    }

    public String name() {
      return name;
    }

    public Expression expression() {
      return expression;
    }
  }
}
