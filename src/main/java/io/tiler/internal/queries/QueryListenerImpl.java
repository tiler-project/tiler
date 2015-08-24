package io.tiler.internal.queries;

import io.tiler.core.regex.InvalidPatternOptionsException;
import io.tiler.core.regex.PatternOptionsParser;
import io.tiler.core.time.TimePeriodParser;
import io.tiler.internal.queries.builders.*;
import io.tiler.internal.queries.expressions.Expression;
import io.tiler.internal.queries.expressions.aggregations.AggregateExpression;
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

    for (int i = 0, count = ctx.exprs.size(); i < count; i++) {
      AggregateExpression expression = (AggregateExpression) expressions.get(ctx.exprs.get(i));
      builder.namedAggregateExpression(ctx.names.get(i).getText(), expression);
    }

    queryBuilder.aggregateClause(builder.build());
  }

  @Override
  public void enterPointClause(QueryParser.PointClauseContext ctx) {

  }

  @Override
  public void exitPointClause(QueryParser.PointClauseContext ctx) {
    PointClauseBuilder builder = new PointClauseBuilder();

    for (QueryParser.NamedExprContext namedExpr : ctx.namedExprs) {
      NamedExpression namedExpression = namedExpressions.get(namedExpr);
      builder.namedExpression(namedExpression.name(), namedExpression.expression());
    }

    queryBuilder.pointClause(builder.build());
  }

  @Override
  public void enterMetricClause(QueryParser.MetricClauseContext ctx) {

  }

  @Override
  public void exitMetricClause(QueryParser.MetricClauseContext ctx) {
    MetricClauseBuilder builder = new MetricClauseBuilder();

    for (QueryParser.NamedExprContext namedExpr : ctx.namedExprs) {
      NamedExpression namedExpression = namedExpressions.get(namedExpr);
      builder.namedExpression(namedExpression.name(), namedExpression.expression());
    }

    queryBuilder.metricClause(builder.build());
  }

  @Override
  public void enterIntervalFuncExpr(QueryParser.IntervalFuncExprContext ctx) {

  }

  @Override
  public void exitIntervalFuncExpr(QueryParser.IntervalFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.intervalFunc()));
  }

  @Override
  public void enterAllFuncExpr(QueryParser.AllFuncExprContext ctx) {

  }

  @Override
  public void exitAllFuncExpr(QueryParser.AllFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.allFunc()));
  }

  @Override
  public void enterIntervalFunc(QueryParser.IntervalFuncContext ctx) {

  }

  @Override
  public void exitIntervalFunc(QueryParser.IntervalFuncContext ctx) {
    IntervalFunction function = new IntervalFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value),
      expressions.get(ctx.offset),
      expressions.get(ctx.size));
    expressions.put(ctx, function);
  }

  @Override
  public void enterAllFunc(QueryParser.AllFuncContext ctx) {

  }

  @Override
  public void exitAllFunc(QueryParser.AllFuncContext ctx) {
    AllFunction function = new AllFunction(
      createQueryContext(ctx));
    expressions.put(ctx, function);
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
  public void enterConcatFuncExpr(QueryParser.ConcatFuncExprContext ctx) {

  }

  @Override
  public void exitConcatFuncExpr(QueryParser.ConcatFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.concatFunc()));
  }

  @Override
  public void enterFirstFuncExpr(QueryParser.FirstFuncExprContext ctx) {

  }

  @Override
  public void exitFirstFuncExpr(QueryParser.FirstFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.firstFunc()));
  }

  @Override
  public void enterNowFuncExpr(QueryParser.NowFuncExprContext ctx) {

  }

  @Override
  public void exitNowFuncExpr(QueryParser.NowFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.nowFunc()));
  }

  @Override
  public void enterConstant(QueryParser.ConstantContext ctx) {

  }

  @Override
  public void exitConstant(QueryParser.ConstantContext ctx) {
    Expression expression;

    switch (ctx.constant.getType()) {
      case QueryLexer.INT:
        expression = new ConstantExpression<>(createQueryContext(ctx), Long.parseLong(ctx.getText()));
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

  @Override
  public void enterLastFuncExpr(QueryParser.LastFuncExprContext ctx) {

  }

  @Override
  public void exitLastFuncExpr(QueryParser.LastFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.lastFunc()));
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
  public void enterMeanFuncExpr(QueryParser.MeanFuncExprContext ctx) {

  }

  @Override
  public void exitMeanFuncExpr(QueryParser.MeanFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.meanFunc()));
  }

  @Override
  public void enterMaxFuncExpr(QueryParser.MaxFuncExprContext ctx) {

  }

  @Override
  public void exitMaxFuncExpr(QueryParser.MaxFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.maxFunc()));
  }

  @Override
  public void enterReplaceFuncExpr(QueryParser.ReplaceFuncExprContext ctx) {

  }

  @Override
  public void exitReplaceFuncExpr(QueryParser.ReplaceFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.replaceFunc()));
  }

  @Override
  public void enterMinFuncExpr(QueryParser.MinFuncExprContext ctx) {

  }

  @Override
  public void exitMinFuncExpr(QueryParser.MinFuncExprContext ctx) {
    expressions.put(ctx, expressions.get(ctx.minFunc()));
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
        throw new RuntimeException("Unexpected operator '" + ctx.op.getText() + "'");
    }

    expressions.put(ctx, expression);
  }

  @Override
  public void enterNowFunc(QueryParser.NowFuncContext ctx) {

  }

  @Override
  public void exitNowFunc(QueryParser.NowFuncContext ctx) {
    expressions.put(ctx, new NowFunction(createQueryContext(ctx)));
  }

  @Override
  public void enterReplaceFunc(QueryParser.ReplaceFuncContext ctx) {

  }

  @Override
  public void exitReplaceFunc(QueryParser.ReplaceFuncContext ctx) {
    ReplaceFunction function = new ReplaceFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value),
      expressions.get(ctx.regex),
      expressions.get(ctx.replacement));
    expressions.put(ctx, function);
  }

  @Override
  public void enterMeanFunc(QueryParser.MeanFuncContext ctx) {

  }

  @Override
  public void exitMeanFunc(QueryParser.MeanFuncContext ctx) {
    MeanFunction function = new MeanFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value));
    expressions.put(ctx, function);
  }

  @Override
  public void enterMinFunc(QueryParser.MinFuncContext ctx) {

  }

  @Override
  public void exitMinFunc(QueryParser.MinFuncContext ctx) {
    MinFunction function = new MinFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value));
    expressions.put(ctx, function);
  }

  @Override
  public void enterMaxFunc(QueryParser.MaxFuncContext ctx) {

  }

  @Override
  public void exitMaxFunc(QueryParser.MaxFuncContext ctx) {
    MaxFunction function = new MaxFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value));
    expressions.put(ctx, function);
  }

  @Override
  public void enterFirstFunc(QueryParser.FirstFuncContext ctx) {

  }

  @Override
  public void exitFirstFunc(QueryParser.FirstFuncContext ctx) {
    FirstFunction function = new FirstFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value));
    expressions.put(ctx, function);
  }

  @Override
  public void enterLastFunc(QueryParser.LastFuncContext ctx) {

  }

  @Override
  public void exitLastFunc(QueryParser.LastFuncContext ctx) {
    LastFunction function = new LastFunction(
      createQueryContext(ctx),
      expressions.get(ctx.value));
    expressions.put(ctx, function);
  }

  @Override
  public void enterConcatFunc(QueryParser.ConcatFuncContext ctx) {

  }

  @Override
  public void exitConcatFunc(QueryParser.ConcatFuncContext ctx) {
    ArrayList<Expression> parameters = new ArrayList<>();

    for (QueryParser.ExprContext param : ctx.params) {
      parameters.add(expressions.get(param));
    }

    ConcatFunction function = new ConcatFunction(createQueryContext(ctx), parameters);
    expressions.put(ctx, function);
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
