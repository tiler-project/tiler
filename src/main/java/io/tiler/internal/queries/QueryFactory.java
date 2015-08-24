package io.tiler.internal.queries;

import io.tiler.internal.queries.grammar.QueryLexer;
import io.tiler.internal.queries.grammar.QueryParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public class QueryFactory {
  public Query parseQuery(String query) throws InvalidQueryException {
    ANTLRInputStream input = new ANTLRInputStream(query);
    ErrorListener errorListener = new ErrorListener();
    QueryLexer lexer = new QueryLexer(input);
    lexer.removeErrorListeners();
    lexer.addErrorListener(errorListener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    QueryParser parser = new QueryParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    ParseTree tree = parser.query();

    if (errorListener.errors.size() > 0) {
      throw new InvalidQueryException(query, errorListener.errors);
    }

    QueryListenerImpl listener = new QueryListenerImpl(query);
    ParseTreeWalker.DEFAULT.walk(listener, tree);

    if (listener.errors().size() > 0) {
      throw new InvalidQueryException(query, listener.errors());
    }

    return listener.query();
  }

  private class ErrorListener extends BaseErrorListener {
    private List<QueryError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
      QueryError queryError = new QueryError(line, charPositionInLine, msg);
      errors.add(queryError);
    }
  }
}
