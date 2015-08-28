grammar Query;
query : fromClause whereClause? groupClause? aggregateClause? metricClause? pointClause? ;

fromClause : FROM exprs+=metricExpr (',' exprs+=metricExpr)* ;
whereClause : WHERE expr ;
groupClause : GROUP fields+=ID (',' fields+=ID)* ;
aggregateClause : AGGREGATE namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
pointClause : POINT namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
metricClause : METRIC namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;

expr : ID                                                                                     # Field
     | constant=(INTEGER | STRING | TIME_PERIOD | REGEX)                                      # Constant
     | func=ID '(' (exprs+=expr (',' exprs+=expr)*)? ')'                                      # Func
     | expr op=(ASTERISK | FORWARD_SLASH) expr                                                # BinaryOp
     | expr op=(PLUS | MINUS) expr                                                            # BinaryOp
     | expr op=(LESS_THAN | GREATER_THAN | LESS_THAN_OR_EQUALS | GREATER_THAN_OR_EQUALS) expr # BinaryOp
     | expr op=(EQUALS | NOT_EQUALS) expr                                                     # BinaryOp
     | expr op=MATCHES expr                                                                   # BinaryOp
     | expr op=(AND | OR) expr                                                                # BinaryOp
     | '(' expr ')'                                                                           # Parentheses
     ;
metricExpr : ID
           | REGEX ;
namedExpr : ID
          | expr AS ID ;

FROM : 'from' ;
WHERE : 'where' ;
GROUP : 'group' ;
AGGREGATE : 'aggregate' ;
POINT : 'point' ;
METRIC : 'metric' ;
AS : 'as' ;

FORWARD_SLASH : '/' ;
ASTERISK : '*' ;
PLUS : '+' ;
MINUS : '-' ;
LESS_THAN : '<' ;
GREATER_THAN : '>' ;
LESS_THAN_OR_EQUALS : '<=' ;
GREATER_THAN_OR_EQUALS : '>=' ;
EQUALS : '==' ;
NOT_EQUALS : '!=' ;
MATCHES : '~=' ;
AND : '&&' ;
OR : '||' ;

REGEX : '/' ('\\/'|~[/])* '/' [dixmsuU]* ;
TIME_PERIOD : [1-9][0-9]*[usmhdw] ;
INTEGER : ('0'|[1-9][0-9]*|'-0'|'-'[1-9][0-9]*) ;
ID : [a-zA-Z_] [a-zA-Z_\-0-9]* ('.' [a-zA-Z_] [a-zA-Z_\-0-9]*)* ;
STRING : '\'' ('\\\''|~['])* '\''
       | '"' ('\\"'|~["])* '"';

WS : [ \t\r\n]+ -> skip ;
