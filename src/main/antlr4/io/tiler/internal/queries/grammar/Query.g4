grammar Query;
query : fromClause whereClause? groupClause? aggregateClause? metricClause? pointClause? ;

fromClause : FROM exprs+=metricExpr (',' exprs+=metricExpr)* ;
whereClause : WHERE expr ;
groupClause : GROUP fields+=ID (',' fields+=ID)* ;
aggregateClause : AGGREGATE exprs+=aggregateExpr AS names+=ID (',' exprs+=aggregateExpr AS names+=ID)* ;
pointClause : POINT namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;
metricClause : METRIC namedExprs+=namedExpr (',' namedExprs+=namedExpr)* ;

aggregateExpr : func=intervalFunc # IntervalFuncExpr
              | func=allFunc      # AllFuncExpr
              ;
intervalFunc : INTERVAL '(' value=expr ',' offset=expr ',' size=expr ')' ;
allFunc : ALL '()' ;
expr : ID                                                                                     # Field
     | constant=INT                                                                           # Constant
     | constant=STRING                                                                        # Constant
     | constant=TIME_PERIOD                                                                   # Constant
     | constant=REGEX                                                                         # Constant
     | func=nowFunc                                                                           # NowFuncExpr
     | func=replaceFunc                                                                       # ReplaceFuncExpr
     | func=substringFunc                                                                     # SubstringFuncExpr
     | func=meanFunc                                                                          # MeanFuncExpr
     | func=minFunc                                                                           # MinFuncExpr
     | func=maxFunc                                                                           # MaxFuncExpr
     | func=sumFunc                                                                           # SumFuncExpr
     | func=firstFunc                                                                         # FirstFuncExpr
     | func=lastFunc                                                                          # LastFuncExpr
     | func=concatFunc                                                                        # ConcatFuncExpr
     | expr op=(ASTERISK | FORWARD_SLASH) expr                                                # BinaryOp
     | expr op=(PLUS | MINUS) expr                                                            # BinaryOp
     | expr op=(LESS_THAN | GREATER_THAN | LESS_THAN_OR_EQUALS | GREATER_THAN_OR_EQUALS) expr # BinaryOp
     | expr op=(EQUALS | NOT_EQUALS) expr                                                     # BinaryOp
     | expr op=MATCHES expr                                                                   # BinaryOp
     | expr op=(AND | OR) expr                                                                # BinaryOp
     | '(' expr ')'                                                                           # Parentheses
     ;
nowFunc : NOW '()' ;
replaceFunc : REPLACE '(' value=expr ',' regex=expr ',' replacement=expr ')' ;
substringFunc : SUBSTRING '(' value=expr ',' beginIndex=expr ',' endIndex=expr ')' ;
meanFunc : MEAN '(' value=expr ')' ;
minFunc : MIN '(' value=expr ')' ;
maxFunc : MAX '(' value=expr ')' ;
sumFunc : SUM '(' value=expr ')' ;
firstFunc : FIRST '(' value=expr ')' ;
lastFunc : LAST '(' value=expr ')' ;
concatFunc : CONCAT '(' params+=expr (',' params+=expr)* ')' ;
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
INTERVAL : 'interval' ;
ALL : 'all' ;
NOW : 'now' ;
REPLACE : 'replace' ;
SUBSTRING : 'substring' ;
MEAN : 'mean' ;
MIN : 'min' ;
MAX : 'max' ;
SUM : 'sum' ;
FIRST : 'first' ;
LAST : 'last' ;
CONCAT : 'concat' ;
AND : 'and' ;
OR : 'or' ;
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

REGEX : '/' ('\\/'|~[/])* '/' [dixmsuU]* ;
TIME_PERIOD : [1-9][0-9]*[usmhdw] ;
INT : ('0'|[1-9][0-9]*) ;
ID : [a-zA-Z_] [a-zA-Z_\-0-9]* ('.' [a-zA-Z_] [a-zA-Z_\-0-9]*)* ;
STRING : '\'' ('\\\''|~['])* '\''
       | '"' ('\\"'|~["])* '"';

WS : [ \t\r\n]+ -> skip ;
