grammar Query;
query : fromClause
        whereClause?
        groupClause?
        aggregateClause?
        (metricSelectClause metricSortClause?)?
        (pointSelectClause pointSortClause?)? ;

fromClause : FROM exprs+=metricExpr (',' exprs+=metricExpr)* ;
whereClause : WHERE expr ;
groupClause : GROUP fields+=ID (',' fields+=ID)* ;
aggregateClause : AGGREGATE namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
pointSelectClause : POINT namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
pointSortClause : SORT sortExprs+=sortExpr (',' sortExprs+=sortExpr)* ;
metricSelectClause : METRIC namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
metricSortClause : SORT sortExprs+=sortExpr (',' sortExprs+=sortExpr)* ;

expr : ID                                                                                     # Field
     | constant=(BOOLEAN | INTEGER | STRING | TIME_PERIOD | REGEX)                            # Constant
     | func=ID '(' (exprs+=expr (',' exprs+=expr)*)? ')'                                      # Func
     | op=EXCLAMATION_MARK expr                                                               # UnaryOp
     | expr op=(ASTERISK | FORWARD_SLASH) expr                                                # BinaryOp
     | expr op=(PLUS | MINUS) expr                                                            # BinaryOp
     | expr op=(LESS_THAN | GREATER_THAN | LESS_THAN_OR_EQUALS | GREATER_THAN_OR_EQUALS) expr # BinaryOp
     | expr op=(EQUALS | NOT_EQUALS) expr                                                     # BinaryOp
     | expr op=REGEX_FIND expr                                                                # BinaryOp
     | expr op=(AND | OR) expr                                                                # BinaryOp
     | '(' expr ')'                                                                           # Parentheses
     ;
metricExpr : ID
           | REGEX ;
namedExpr : ID
          | expr AS ID ;
sortExpr : expr sortDirection=(ASC | DESC)?;

FROM : 'from' ;
WHERE : 'where' ;
GROUP : 'group' ;
AGGREGATE : 'aggregate' ;
POINT : 'point' ;
METRIC : 'metric' ;
SORT : 'sort' ;
AS : 'as' ;
ASC : 'asc' ;
DESC : 'desc' ;

EXCLAMATION_MARK : '!' ;
ASTERISK : '*' ;
FORWARD_SLASH : '/' ;
PLUS : '+' ;
MINUS : '-' ;
LESS_THAN : '<' ;
GREATER_THAN : '>' ;
LESS_THAN_OR_EQUALS : '<=' ;
GREATER_THAN_OR_EQUALS : '>=' ;
EQUALS : '==' ;
NOT_EQUALS : '!=' ;
REGEX_FIND : '~=' ;
AND : '&&' ;
OR : '||' ;

BOOLEAN : ('true'|'false') ;
INTEGER : ('0'|[1-9][0-9]*|'-0'|'-'[1-9][0-9]*) ;
STRING : '\'' ('\\\''|~['])* '\''
       | '"' ('\\"'|~["])* '"';
TIME_PERIOD : [1-9][0-9]*[usmhdw] ;
REGEX : '/' ('\\/'|~[/])* '/' [dixmsuU]* ;

ID : [a-zA-Z_] [a-zA-Z_\-0-9]* ('.' [a-zA-Z_] [a-zA-Z_\-0-9]*)* ;

WS : [ \t\r\n]+ -> skip ;
