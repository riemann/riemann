grammar Query;

// Kind of a simplified, restructured variant of the Clojure/Java lexers with a
// way different structure.
//
// https://github.com/antlr/grammars-v4/blob/master/clojure/Clojure.g4

predicate
  : primary
  | 'not' predicate
  | predicate 'and' predicate
  | predicate 'or' predicate
  ;

primary
  : '(' predicate ')'
  | simple
  ;

simple
  : tagged
  | equal
  | not_equal
  | lesser
  | greater
  | lesser_equal
  | greater_equal
  | like
  | regex_match
  | field
  | value
  ;

tagged        : 'tagged' string ;
equal         : value '=' value ;
not_equal     : value '!=' value ;
lesser        : value '<' number ;
greater       : value '>' number ;
lesser_equal  : value '<=' number ;
greater_equal : value '>=' number ;
like          : value '=~' string ;
regex_match   : value '~=' string ;

// Values -----------------------------------------------------------

value
  : true
  | false
  | nil
  | number
  | string
  | field
  ;

field : NAME ;
string : STRING ;
true : TRUE ;
false : FALSE ;
nil : NIL ;

number
    : float
    | bign
    | long
    ;

float : FLOAT;
bign : BIGN;
long : LONG;


// Lexers -------------------------------------------------------------

STRING : '"' ( ~'"' | '\\' '"' )* '"' ;

FLOAT
    : '-'? [0-9]+ FLOAT_TAIL
    | '-'? 'Infinity'
    | '-'? 'NaN'
    ;

fragment
FLOAT_TAIL
    : FLOAT_DECIMAL FLOAT_EXP
    | FLOAT_DECIMAL
    | FLOAT_EXP
    ;

fragment
FLOAT_DECIMAL
    : '.' [0-9]+
    ;

fragment
FLOAT_EXP
    : [eE] '-'? [0-9]+
    ;
fragment
HEXD: [0-9a-fA-F] ;
HEX: '0' [xX] HEXD+ ;
LONG: '-'? [0-9]+[lL]?;
BIGN: '-'? [0-9]+[nN];

CHAR_U
    : '\\' 'u'[0-9D-Fd-f] HEXD HEXD HEXD ;
CHAR_NAMED
    : '\\' ( 'newline'
           | 'return'
           | 'space'
           | 'tab'
           | 'formfeed'
           | 'backspace' ) ;
CHAR_ANY
    : '\\' . ;

NIL : 'nil'
    | 'null' ;

TRUE : 'true' ;
FALSE : 'false' ;

// Names

NAME: SYMBOL_HEAD SYMBOL_REST* (':' SYMBOL_REST+)* ;

fragment
SYMBOL_HEAD
    : ~('0' .. '9'
        | '^' | '`' | '\'' | '"' | '#' | '~' | '@' | ':' | '/' | '%' | '(' | ')' | '[' | ']' | '{' | '}' // FIXME: could be one group
        | [ \n\r\t\,] // FIXME: could be WS
        )
    ;

fragment
SYMBOL_REST
    : SYMBOL_HEAD
    | '0'..'9'
    | '.'
    ;


// Whitespace

WS : [ \n\r\t\,] -> channel(HIDDEN) ;
