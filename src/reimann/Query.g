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
	NOT_EQUAL = '!=';
	EQUAL = '=';
	LESSER = '<';
	LESSER_EQUAL = '<=';
	GREATER = '>';
	GREATER_EQUAL = '>=';
}

@header		{package reimann;}
@lexer::header 	{package reimann;}

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
		| approximately
		| lesser
		| lesser_equal
		| greater
		| greater_equal
		| not_equal
		| equal);

approximately
	:	field WS* APPROXIMATELY^ WS* value;
lesser	:	field WS* LESSER^ WS* value;
lesser_equal
	:	field WS* LESSER_EQUAL^ WS* value;
greater	:	field WS* GREATER^ WS* value;
greater_equal
	:	field WS* GREATER_EQUAL^ WS* value;
not_equal
	:	field WS* NOT_EQUAL^ WS* value;	
equal	:	field WS* EQUAL^ WS* value;

value	: 	(String | t | f | nil | INT | FLOAT);


t	:	'true';
f	:	'false';
nil	:	'null' | 'nil';

field	: ('host' 
	| 'service'
	| 'state'
	| 'description'
	| 'metric_f'
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