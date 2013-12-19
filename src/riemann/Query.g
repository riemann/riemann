grammar Query;

options {
	output=AST;
	ASTLabelType=CommonTree;
}

tokens {
	AND = 'and';
	OR = 'or';
	NOT = 'not';
	APPROXIMATELY = '=~';
	REGEX_MATCH = '~=';
	NOT_EQUAL = '!=';
	EQUAL = '=';
	LESSER = '<';
	LESSER_EQUAL = '<=';
	GREATER = '>';
	GREATER_EQUAL = '>=';
	TAGGED = 'tagged';
}

@header		{package riemann;}
@lexer::header 	{package riemann;}

expr	:	(or EOF) -> or;

or	:	and (WS* OR^ WS* and)*;

and	:	(not | primary) (WS* AND^ WS* (not | primary))*;

not	:	NOT^ WS* (not | primary);


primary	: 	(
			('(' or ')') -> ^(or)
			| simple -> simple
		);

fragment
simple	:	( t | f | nil
		| tagged
		| approximately
		| regex_match
		| lesser
		| lesser_equal
		| greater
		| greater_equal
		| not_equal
		| equal
		);

approximately
	:	field WS* APPROXIMATELY^ WS* value;
regex_match
	:	field WS* REGEX_MATCH^ WS* value;
lesser	:	field WS* LESSER^ WS* value;
lesser_equal
	:	field WS* LESSER_EQUAL^ WS* value;
greater	:	field WS* GREATER^ WS* value;
greater_equal
	:	field WS* GREATER_EQUAL^ WS* value;
not_equal
	:	field WS* NOT_EQUAL^ WS* value;	
equal	:	field WS* EQUAL^ WS* value;

tagged	:	TAGGED^ WS* String;

value	: 	(String | t | f | nil | INT | FLOAT);

t	:	'true';
f	:	'false';
nil	:	'null' | 'nil';

field	: ('host'
	| 'service'
	| 'state'
	| 'description'
	| 'metric_f'
	| 'metric'
	| 'ttl'
	| 'time'
	);

ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :	'-'? '0'..'9'+
    ;

FLOAT
    :   '-'? ('0'..'9')+ ('.' ('0'..'9')*)? EXPONENT?
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

String  :
//     '"' (EscapeSequence | FreeChar)* '"'
// Still don't understand why this doesn't work
    '"' ( EscapeSequence | ~('\u0000'..'\u001f' | '\\' | '\"' ) )* '"'
    ;

    fragment EscapeSequence
        :   '\\' (UnicodeEscape |'b'|'t'|'n'|'f'|'r'|'\"'|'\\')
        ;

fragment UnicodeEscape
    : 'u' HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigit
    : '0'..'9' | 'A'..'F' | 'a'..'f'
    ;