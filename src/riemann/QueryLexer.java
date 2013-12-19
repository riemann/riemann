// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/riemann/Query.g 2013-12-02 00:16:46
package riemann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QueryLexer extends Lexer {
    public static final int LESSER_EQUAL=12;
    public static final int EXPONENT=21;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int APPROXIMATELY=7;
    public static final int FLOAT=19;
    public static final int INT=18;
    public static final int NOT=6;
    public static final int ID=20;
    public static final int AND=4;
    public static final int EOF=-1;
    public static final int HexDigit=24;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int LESSER=11;
    public static final int GREATER=13;
    public static final int WS=16;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int NOT_EQUAL=9;
    public static final int TAGGED=15;
    public static final int UnicodeEscape=23;
    public static final int EQUAL=10;
    public static final int OR=5;
    public static final int String=17;
    public static final int GREATER_EQUAL=14;
    public static final int EscapeSequence=22;
    public static final int REGEX_MATCH=8;

    // delegates
    // delegators

    public QueryLexer() {;} 
    public QueryLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public QueryLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "src/riemann/Query.g"; }

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:5:5: ( 'and' )
            // src/riemann/Query.g:5:7: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:6:4: ( 'or' )
            // src/riemann/Query.g:6:6: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:7:5: ( 'not' )
            // src/riemann/Query.g:7:7: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "APPROXIMATELY"
    public final void mAPPROXIMATELY() throws RecognitionException {
        try {
            int _type = APPROXIMATELY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:8:15: ( '=~' )
            // src/riemann/Query.g:8:17: '=~'
            {
            match("=~"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "APPROXIMATELY"

    // $ANTLR start "REGEX_MATCH"
    public final void mREGEX_MATCH() throws RecognitionException {
        try {
            int _type = REGEX_MATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:9:13: ( '~=' )
            // src/riemann/Query.g:9:15: '~='
            {
            match("~="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REGEX_MATCH"

    // $ANTLR start "NOT_EQUAL"
    public final void mNOT_EQUAL() throws RecognitionException {
        try {
            int _type = NOT_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:10:11: ( '!=' )
            // src/riemann/Query.g:10:13: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT_EQUAL"

    // $ANTLR start "EQUAL"
    public final void mEQUAL() throws RecognitionException {
        try {
            int _type = EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:11:7: ( '=' )
            // src/riemann/Query.g:11:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUAL"

    // $ANTLR start "LESSER"
    public final void mLESSER() throws RecognitionException {
        try {
            int _type = LESSER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:12:8: ( '<' )
            // src/riemann/Query.g:12:10: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSER"

    // $ANTLR start "LESSER_EQUAL"
    public final void mLESSER_EQUAL() throws RecognitionException {
        try {
            int _type = LESSER_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:13:14: ( '<=' )
            // src/riemann/Query.g:13:16: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSER_EQUAL"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:14:9: ( '>' )
            // src/riemann/Query.g:14:11: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "GREATER_EQUAL"
    public final void mGREATER_EQUAL() throws RecognitionException {
        try {
            int _type = GREATER_EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:15:15: ( '>=' )
            // src/riemann/Query.g:15:17: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER_EQUAL"

    // $ANTLR start "TAGGED"
    public final void mTAGGED() throws RecognitionException {
        try {
            int _type = TAGGED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:16:8: ( 'tagged' )
            // src/riemann/Query.g:16:10: 'tagged'
            {
            match("tagged"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TAGGED"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:17:7: ( '(' )
            // src/riemann/Query.g:17:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:18:7: ( ')' )
            // src/riemann/Query.g:18:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:19:7: ( 'true' )
            // src/riemann/Query.g:19:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:20:7: ( 'false' )
            // src/riemann/Query.g:20:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:21:7: ( 'null' )
            // src/riemann/Query.g:21:9: 'null'
            {
            match("null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:22:7: ( 'nil' )
            // src/riemann/Query.g:22:9: 'nil'
            {
            match("nil"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:23:7: ( 'host' )
            // src/riemann/Query.g:23:9: 'host'
            {
            match("host"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:24:7: ( 'service' )
            // src/riemann/Query.g:24:9: 'service'
            {
            match("service"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:25:7: ( 'state' )
            // src/riemann/Query.g:25:9: 'state'
            {
            match("state"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:26:7: ( 'description' )
            // src/riemann/Query.g:26:9: 'description'
            {
            match("description"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:27:7: ( 'metric_f' )
            // src/riemann/Query.g:27:9: 'metric_f'
            {
            match("metric_f"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:28:7: ( 'metric' )
            // src/riemann/Query.g:28:9: 'metric'
            {
            match("metric"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:29:7: ( 'ttl' )
            // src/riemann/Query.g:29:9: 'ttl'
            {
            match("ttl"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:30:7: ( 'time' )
            // src/riemann/Query.g:30:9: 'time'
            {
            match("time"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:85:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // src/riemann/Query.g:85:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // src/riemann/Query.g:85:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // src/riemann/Query.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:88:5: ( ( '-' )? ( '0' .. '9' )+ )
            // src/riemann/Query.g:88:7: ( '-' )? ( '0' .. '9' )+
            {
            // src/riemann/Query.g:88:7: ( '-' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // src/riemann/Query.g:88:7: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // src/riemann/Query.g:88:12: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/riemann/Query.g:88:12: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:92:5: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )* )? ( EXPONENT )? )
            // src/riemann/Query.g:92:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )* )? ( EXPONENT )?
            {
            // src/riemann/Query.g:92:9: ( '-' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='-') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // src/riemann/Query.g:92:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // src/riemann/Query.g:92:14: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // src/riemann/Query.g:92:15: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);

            // src/riemann/Query.g:92:26: ( '.' ( '0' .. '9' )* )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='.') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // src/riemann/Query.g:92:27: '.' ( '0' .. '9' )*
                    {
                    match('.'); 
                    // src/riemann/Query.g:92:31: ( '0' .. '9' )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // src/riemann/Query.g:92:32: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);


                    }
                    break;

            }

            // src/riemann/Query.g:92:45: ( EXPONENT )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='E'||LA8_0=='e') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // src/riemann/Query.g:92:45: EXPONENT
                    {
                    mEXPONENT(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:95:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // src/riemann/Query.g:95:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "EXPONENT"
    public final void mEXPONENT() throws RecognitionException {
        try {
            // src/riemann/Query.g:103:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // src/riemann/Query.g:103:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // src/riemann/Query.g:103:22: ( '+' | '-' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='+'||LA9_0=='-') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // src/riemann/Query.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // src/riemann/Query.g:103:33: ( '0' .. '9' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='0' && LA10_0<='9')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // src/riemann/Query.g:103:34: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "EXPONENT"

    // $ANTLR start "String"
    public final void mString() throws RecognitionException {
        try {
            int _type = String;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:105:9: ( '\"' ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )* '\"' )
            // src/riemann/Query.g:108:5: '\"' ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )* '\"'
            {
            match('\"'); 
            // src/riemann/Query.g:108:9: ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )*
            loop11:
            do {
                int alt11=3;
                int LA11_0 = input.LA(1);

                if ( (LA11_0=='\\') ) {
                    alt11=1;
                }
                else if ( ((LA11_0>=' ' && LA11_0<='!')||(LA11_0>='#' && LA11_0<='[')||(LA11_0>=']' && LA11_0<='\uFFFF')) ) {
                    alt11=2;
                }


                switch (alt11) {
            	case 1 :
            	    // src/riemann/Query.g:108:11: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // src/riemann/Query.g:108:28: ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' )
            	    {
            	    if ( (input.LA(1)>=' ' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "String"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // src/riemann/Query.g:112:9: ( '\\\\' ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' ) )
            // src/riemann/Query.g:112:13: '\\\\' ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' )
            {
            match('\\'); 
            // src/riemann/Query.g:112:18: ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' )
            int alt12=8;
            switch ( input.LA(1) ) {
            case 'u':
                {
                alt12=1;
                }
                break;
            case 'b':
                {
                alt12=2;
                }
                break;
            case 't':
                {
                alt12=3;
                }
                break;
            case 'n':
                {
                alt12=4;
                }
                break;
            case 'f':
                {
                alt12=5;
                }
                break;
            case 'r':
                {
                alt12=6;
                }
                break;
            case '\"':
                {
                alt12=7;
                }
                break;
            case '\\':
                {
                alt12=8;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // src/riemann/Query.g:112:19: UnicodeEscape
                    {
                    mUnicodeEscape(); 

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:112:34: 'b'
                    {
                    match('b'); 

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:112:38: 't'
                    {
                    match('t'); 

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:112:42: 'n'
                    {
                    match('n'); 

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:112:46: 'f'
                    {
                    match('f'); 

                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:112:50: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 7 :
                    // src/riemann/Query.g:112:54: '\\\"'
                    {
                    match('\"'); 

                    }
                    break;
                case 8 :
                    // src/riemann/Query.g:112:59: '\\\\'
                    {
                    match('\\'); 

                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "UnicodeEscape"
    public final void mUnicodeEscape() throws RecognitionException {
        try {
            // src/riemann/Query.g:116:5: ( 'u' HexDigit HexDigit HexDigit HexDigit )
            // src/riemann/Query.g:116:7: 'u' HexDigit HexDigit HexDigit HexDigit
            {
            match('u'); 
            mHexDigit(); 
            mHexDigit(); 
            mHexDigit(); 
            mHexDigit(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "UnicodeEscape"

    // $ANTLR start "HexDigit"
    public final void mHexDigit() throws RecognitionException {
        try {
            // src/riemann/Query.g:120:5: ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )
            // src/riemann/Query.g:
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HexDigit"

    public void mTokens() throws RecognitionException {
        // src/riemann/Query.g:1:8: ( AND | OR | NOT | APPROXIMATELY | REGEX_MATCH | NOT_EQUAL | EQUAL | LESSER | LESSER_EQUAL | GREATER | GREATER_EQUAL | TAGGED | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | ID | INT | FLOAT | WS | String )
        int alt13=31;
        alt13 = dfa13.predict(input);
        switch (alt13) {
            case 1 :
                // src/riemann/Query.g:1:10: AND
                {
                mAND(); 

                }
                break;
            case 2 :
                // src/riemann/Query.g:1:14: OR
                {
                mOR(); 

                }
                break;
            case 3 :
                // src/riemann/Query.g:1:17: NOT
                {
                mNOT(); 

                }
                break;
            case 4 :
                // src/riemann/Query.g:1:21: APPROXIMATELY
                {
                mAPPROXIMATELY(); 

                }
                break;
            case 5 :
                // src/riemann/Query.g:1:35: REGEX_MATCH
                {
                mREGEX_MATCH(); 

                }
                break;
            case 6 :
                // src/riemann/Query.g:1:47: NOT_EQUAL
                {
                mNOT_EQUAL(); 

                }
                break;
            case 7 :
                // src/riemann/Query.g:1:57: EQUAL
                {
                mEQUAL(); 

                }
                break;
            case 8 :
                // src/riemann/Query.g:1:63: LESSER
                {
                mLESSER(); 

                }
                break;
            case 9 :
                // src/riemann/Query.g:1:70: LESSER_EQUAL
                {
                mLESSER_EQUAL(); 

                }
                break;
            case 10 :
                // src/riemann/Query.g:1:83: GREATER
                {
                mGREATER(); 

                }
                break;
            case 11 :
                // src/riemann/Query.g:1:91: GREATER_EQUAL
                {
                mGREATER_EQUAL(); 

                }
                break;
            case 12 :
                // src/riemann/Query.g:1:105: TAGGED
                {
                mTAGGED(); 

                }
                break;
            case 13 :
                // src/riemann/Query.g:1:112: T__25
                {
                mT__25(); 

                }
                break;
            case 14 :
                // src/riemann/Query.g:1:118: T__26
                {
                mT__26(); 

                }
                break;
            case 15 :
                // src/riemann/Query.g:1:124: T__27
                {
                mT__27(); 

                }
                break;
            case 16 :
                // src/riemann/Query.g:1:130: T__28
                {
                mT__28(); 

                }
                break;
            case 17 :
                // src/riemann/Query.g:1:136: T__29
                {
                mT__29(); 

                }
                break;
            case 18 :
                // src/riemann/Query.g:1:142: T__30
                {
                mT__30(); 

                }
                break;
            case 19 :
                // src/riemann/Query.g:1:148: T__31
                {
                mT__31(); 

                }
                break;
            case 20 :
                // src/riemann/Query.g:1:154: T__32
                {
                mT__32(); 

                }
                break;
            case 21 :
                // src/riemann/Query.g:1:160: T__33
                {
                mT__33(); 

                }
                break;
            case 22 :
                // src/riemann/Query.g:1:166: T__34
                {
                mT__34(); 

                }
                break;
            case 23 :
                // src/riemann/Query.g:1:172: T__35
                {
                mT__35(); 

                }
                break;
            case 24 :
                // src/riemann/Query.g:1:178: T__36
                {
                mT__36(); 

                }
                break;
            case 25 :
                // src/riemann/Query.g:1:184: T__37
                {
                mT__37(); 

                }
                break;
            case 26 :
                // src/riemann/Query.g:1:190: T__38
                {
                mT__38(); 

                }
                break;
            case 27 :
                // src/riemann/Query.g:1:196: ID
                {
                mID(); 

                }
                break;
            case 28 :
                // src/riemann/Query.g:1:199: INT
                {
                mINT(); 

                }
                break;
            case 29 :
                // src/riemann/Query.g:1:203: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 30 :
                // src/riemann/Query.g:1:209: WS
                {
                mWS(); 

                }
                break;
            case 31 :
                // src/riemann/Query.g:1:212: String
                {
                mString(); 

                }
                break;

        }

    }


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\1\uffff\3\21\1\34\2\uffff\1\36\1\40\1\21\2\uffff\5\21\2\uffff\1"+
        "\54\2\uffff\1\21\1\56\3\21\6\uffff\12\21\2\uffff\1\74\1\uffff\1"+
        "\75\1\21\1\77\2\21\1\102\7\21\2\uffff\1\112\1\uffff\1\21\1\114\1"+
        "\uffff\1\115\1\21\1\117\4\21\1\uffff\1\21\2\uffff\1\125\1\uffff"+
        "\1\21\1\127\2\21\1\132\1\uffff\1\21\1\uffff\1\21\1\136\1\uffff\1"+
        "\137\2\21\2\uffff\1\21\1\143\1\21\1\uffff\1\21\1\146\1\uffff";
    static final String DFA13_eofS =
        "\147\uffff";
    static final String DFA13_minS =
        "\1\11\1\156\1\162\1\151\1\176\2\uffff\2\75\1\141\2\uffff\1\141\1"+
        "\157\3\145\1\uffff\1\60\1\56\2\uffff\1\144\1\60\1\164\2\154\6\uffff"+
        "\1\147\1\165\1\154\1\155\1\154\1\163\1\162\1\141\1\163\1\164\2\uffff"+
        "\1\60\1\uffff\1\60\1\154\1\60\1\147\1\145\1\60\1\145\1\163\1\164"+
        "\1\166\1\164\1\143\1\162\2\uffff\1\60\1\uffff\1\145\1\60\1\uffff"+
        "\1\60\1\145\1\60\1\151\1\145\1\162\1\151\1\uffff\1\144\2\uffff\1"+
        "\60\1\uffff\1\143\1\60\1\151\1\143\1\60\1\uffff\1\145\1\uffff\1"+
        "\160\1\60\1\uffff\1\60\1\164\1\146\2\uffff\1\151\1\60\1\157\1\uffff"+
        "\1\156\1\60\1\uffff";
    static final String DFA13_maxS =
        "\1\176\1\156\1\162\1\165\1\176\2\uffff\2\75\1\164\2\uffff\1\141"+
        "\1\157\1\164\2\145\1\uffff\1\71\1\145\2\uffff\1\144\1\172\1\164"+
        "\2\154\6\uffff\1\147\1\165\1\154\1\155\1\154\1\163\1\162\1\141\1"+
        "\163\1\164\2\uffff\1\172\1\uffff\1\172\1\154\1\172\1\147\1\145\1"+
        "\172\1\145\1\163\1\164\1\166\1\164\1\143\1\162\2\uffff\1\172\1\uffff"+
        "\1\145\1\172\1\uffff\1\172\1\145\1\172\1\151\1\145\1\162\1\151\1"+
        "\uffff\1\144\2\uffff\1\172\1\uffff\1\143\1\172\1\151\1\143\1\172"+
        "\1\uffff\1\145\1\uffff\1\160\1\172\1\uffff\1\172\1\164\1\146\2\uffff"+
        "\1\151\1\172\1\157\1\uffff\1\156\1\172\1\uffff";
    static final String DFA13_acceptS =
        "\5\uffff\1\5\1\6\3\uffff\1\15\1\16\5\uffff\1\33\2\uffff\1\36\1\37"+
        "\5\uffff\1\4\1\7\1\11\1\10\1\13\1\12\12\uffff\1\35\1\34\1\uffff"+
        "\1\2\15\uffff\1\1\1\3\1\uffff\1\22\2\uffff\1\31\7\uffff\1\21\1\uffff"+
        "\1\17\1\32\1\uffff\1\23\5\uffff\1\20\1\uffff\1\25\2\uffff\1\14\3"+
        "\uffff\1\30\1\24\3\uffff\1\27\2\uffff\1\26";
    static final String DFA13_specialS =
        "\147\uffff}>";
    static final String[] DFA13_transitionS = {
            "\2\24\2\uffff\1\24\22\uffff\1\24\1\6\1\25\5\uffff\1\12\1\13"+
            "\3\uffff\1\22\2\uffff\12\23\2\uffff\1\7\1\4\1\10\2\uffff\32"+
            "\21\4\uffff\1\21\1\uffff\1\1\2\21\1\17\1\21\1\14\1\21\1\15\4"+
            "\21\1\20\1\3\1\2\3\21\1\16\1\11\6\21\3\uffff\1\5",
            "\1\26",
            "\1\27",
            "\1\32\5\uffff\1\30\5\uffff\1\31",
            "\1\33",
            "",
            "",
            "\1\35",
            "\1\37",
            "\1\41\7\uffff\1\44\10\uffff\1\42\1\uffff\1\43",
            "",
            "",
            "\1\45",
            "\1\46",
            "\1\47\16\uffff\1\50",
            "\1\51",
            "\1\52",
            "",
            "\12\23",
            "\1\53\1\uffff\12\23\13\uffff\1\53\37\uffff\1\53",
            "",
            "",
            "\1\55",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\57",
            "\1\60",
            "\1\61",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\62",
            "\1\63",
            "\1\64",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\76",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\100",
            "\1\101",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\1\113",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\116",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\120",
            "\1\121",
            "\1\122",
            "\1\123",
            "",
            "\1\124",
            "",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\1\126",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\130",
            "\1\131",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\1\133",
            "",
            "\1\134",
            "\12\21\7\uffff\32\21\4\uffff\1\135\1\uffff\32\21",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\140",
            "\1\141",
            "",
            "",
            "\1\142",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\144",
            "",
            "\1\145",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( AND | OR | NOT | APPROXIMATELY | REGEX_MATCH | NOT_EQUAL | EQUAL | LESSER | LESSER_EQUAL | GREATER | GREATER_EQUAL | TAGGED | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | ID | INT | FLOAT | WS | String );";
        }
    }
 

}