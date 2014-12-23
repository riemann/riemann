// $ANTLR 3.2 debian-10 src/riemann/Query.g 2014-12-13 12:59:07
package riemann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QueryLexer extends Lexer {
    public static final int LESSER_EQUAL=12;
    public static final int EXPONENT=22;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int LIMIT=16;
    public static final int APPROXIMATELY=7;
    public static final int FLOAT=20;
    public static final int INT=19;
    public static final int NOT=6;
    public static final int ID=21;
    public static final int AND=4;
    public static final int EOF=-1;
    public static final int HexDigit=25;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int LESSER=11;
    public static final int GREATER=13;
    public static final int WS=17;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int NOT_EQUAL=9;
    public static final int T__39=39;
    public static final int TAGGED=15;
    public static final int UnicodeEscape=24;
    public static final int EQUAL=10;
    public static final int OR=5;
    public static final int String=18;
    public static final int GREATER_EQUAL=14;
    public static final int EscapeSequence=23;
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

    // $ANTLR start "LIMIT"
    public final void mLIMIT() throws RecognitionException {
        try {
            int _type = LIMIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:17:7: ( 'limit' )
            // src/riemann/Query.g:17:9: 'limit'
            {
            match("limit"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LIMIT"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:18:7: ( '(' )
            // src/riemann/Query.g:18:9: '('
            {
            match('('); 

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
            // src/riemann/Query.g:19:7: ( ')' )
            // src/riemann/Query.g:19:9: ')'
            {
            match(')'); 

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
            // src/riemann/Query.g:20:7: ( 'true' )
            // src/riemann/Query.g:20:9: 'true'
            {
            match("true"); 


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
            // src/riemann/Query.g:21:7: ( 'false' )
            // src/riemann/Query.g:21:9: 'false'
            {
            match("false"); 


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
            // src/riemann/Query.g:22:7: ( 'null' )
            // src/riemann/Query.g:22:9: 'null'
            {
            match("null"); 


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
            // src/riemann/Query.g:23:7: ( 'nil' )
            // src/riemann/Query.g:23:9: 'nil'
            {
            match("nil"); 


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
            // src/riemann/Query.g:24:7: ( 'host' )
            // src/riemann/Query.g:24:9: 'host'
            {
            match("host"); 


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
            // src/riemann/Query.g:25:7: ( 'service' )
            // src/riemann/Query.g:25:9: 'service'
            {
            match("service"); 


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
            // src/riemann/Query.g:26:7: ( 'state' )
            // src/riemann/Query.g:26:9: 'state'
            {
            match("state"); 


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
            // src/riemann/Query.g:27:7: ( 'description' )
            // src/riemann/Query.g:27:9: 'description'
            {
            match("description"); 


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
            // src/riemann/Query.g:28:7: ( 'metric_f' )
            // src/riemann/Query.g:28:9: 'metric_f'
            {
            match("metric_f"); 


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
            // src/riemann/Query.g:29:7: ( 'metric' )
            // src/riemann/Query.g:29:9: 'metric'
            {
            match("metric"); 


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
            // src/riemann/Query.g:30:7: ( 'ttl' )
            // src/riemann/Query.g:30:9: 'ttl'
            {
            match("ttl"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:31:7: ( 'time' )
            // src/riemann/Query.g:31:9: 'time'
            {
            match("time"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // src/riemann/Query.g:89:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // src/riemann/Query.g:89:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // src/riemann/Query.g:89:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
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
            // src/riemann/Query.g:92:5: ( ( '-' )? ( '0' .. '9' )+ )
            // src/riemann/Query.g:92:7: ( '-' )? ( '0' .. '9' )+
            {
            // src/riemann/Query.g:92:7: ( '-' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // src/riemann/Query.g:92:7: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // src/riemann/Query.g:92:12: ( '0' .. '9' )+
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
            	    // src/riemann/Query.g:92:12: '0' .. '9'
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
            // src/riemann/Query.g:96:5: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )* )? ( EXPONENT )? )
            // src/riemann/Query.g:96:9: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )* )? ( EXPONENT )?
            {
            // src/riemann/Query.g:96:9: ( '-' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='-') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // src/riemann/Query.g:96:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // src/riemann/Query.g:96:14: ( '0' .. '9' )+
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
            	    // src/riemann/Query.g:96:15: '0' .. '9'
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

            // src/riemann/Query.g:96:26: ( '.' ( '0' .. '9' )* )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='.') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // src/riemann/Query.g:96:27: '.' ( '0' .. '9' )*
                    {
                    match('.'); 
                    // src/riemann/Query.g:96:31: ( '0' .. '9' )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // src/riemann/Query.g:96:32: '0' .. '9'
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

            // src/riemann/Query.g:96:45: ( EXPONENT )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='E'||LA8_0=='e') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // src/riemann/Query.g:96:45: EXPONENT
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
            // src/riemann/Query.g:99:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // src/riemann/Query.g:99:9: ( ' ' | '\\t' | '\\r' | '\\n' )
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
            // src/riemann/Query.g:107:10: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )
            // src/riemann/Query.g:107:12: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // src/riemann/Query.g:107:22: ( '+' | '-' )?
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

            // src/riemann/Query.g:107:33: ( '0' .. '9' )+
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
            	    // src/riemann/Query.g:107:34: '0' .. '9'
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
            // src/riemann/Query.g:109:9: ( '\"' ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )* '\"' )
            // src/riemann/Query.g:112:5: '\"' ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )* '\"'
            {
            match('\"'); 
            // src/riemann/Query.g:112:9: ( EscapeSequence | ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' ) )*
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
            	    // src/riemann/Query.g:112:11: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // src/riemann/Query.g:112:28: ~ ( '\\u0000' .. '\\u001f' | '\\\\' | '\\\"' )
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
            // src/riemann/Query.g:116:9: ( '\\\\' ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' ) )
            // src/riemann/Query.g:116:13: '\\\\' ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' )
            {
            match('\\'); 
            // src/riemann/Query.g:116:18: ( UnicodeEscape | 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\\\' )
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
                    // src/riemann/Query.g:116:19: UnicodeEscape
                    {
                    mUnicodeEscape(); 

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:116:34: 'b'
                    {
                    match('b'); 

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:116:38: 't'
                    {
                    match('t'); 

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:116:42: 'n'
                    {
                    match('n'); 

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:116:46: 'f'
                    {
                    match('f'); 

                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:116:50: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 7 :
                    // src/riemann/Query.g:116:54: '\\\"'
                    {
                    match('\"'); 

                    }
                    break;
                case 8 :
                    // src/riemann/Query.g:116:59: '\\\\'
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
            // src/riemann/Query.g:120:5: ( 'u' HexDigit HexDigit HexDigit HexDigit )
            // src/riemann/Query.g:120:7: 'u' HexDigit HexDigit HexDigit HexDigit
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
            // src/riemann/Query.g:124:5: ( '0' .. '9' | 'A' .. 'F' | 'a' .. 'f' )
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
        // src/riemann/Query.g:1:8: ( AND | OR | NOT | APPROXIMATELY | REGEX_MATCH | NOT_EQUAL | EQUAL | LESSER | LESSER_EQUAL | GREATER | GREATER_EQUAL | TAGGED | LIMIT | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | ID | INT | FLOAT | WS | String )
        int alt13=32;
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
                // src/riemann/Query.g:1:112: LIMIT
                {
                mLIMIT(); 

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
                // src/riemann/Query.g:1:196: T__39
                {
                mT__39(); 

                }
                break;
            case 28 :
                // src/riemann/Query.g:1:202: ID
                {
                mID(); 

                }
                break;
            case 29 :
                // src/riemann/Query.g:1:205: INT
                {
                mINT(); 

                }
                break;
            case 30 :
                // src/riemann/Query.g:1:209: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 31 :
                // src/riemann/Query.g:1:215: WS
                {
                mWS(); 

                }
                break;
            case 32 :
                // src/riemann/Query.g:1:218: String
                {
                mString(); 

                }
                break;

        }

    }


    protected DFA13 dfa13 = new DFA13(this);
    static final String DFA13_eotS =
        "\1\uffff\3\22\1\35\2\uffff\1\37\1\41\2\22\2\uffff\5\22\2\uffff\1"+
        "\55\2\uffff\1\22\1\60\3\22\6\uffff\13\22\2\uffff\1\77\1\uffff\1"+
        "\100\1\22\1\102\2\22\1\105\10\22\2\uffff\1\116\1\uffff\1\22\1\120"+
        "\1\uffff\1\121\2\22\1\124\4\22\1\uffff\1\22\2\uffff\1\132\1\133"+
        "\1\uffff\1\22\1\135\2\22\1\140\2\uffff\1\22\1\uffff\1\22\1\144\1"+
        "\uffff\1\145\2\22\2\uffff\1\22\1\151\1\22\1\uffff\1\22\1\154\1\uffff";
    static final String DFA13_eofS =
        "\155\uffff";
    static final String DFA13_minS =
        "\1\11\1\156\1\162\1\151\1\176\2\uffff\2\75\1\141\1\151\2\uffff\1"+
        "\141\1\157\3\145\1\uffff\1\60\1\56\2\uffff\1\144\1\60\1\164\2\154"+
        "\6\uffff\1\147\1\165\1\154\2\155\1\154\1\163\1\162\1\141\1\163\1"+
        "\164\2\uffff\1\60\1\uffff\1\60\1\154\1\60\1\147\1\145\1\60\1\145"+
        "\1\151\1\163\1\164\1\166\1\164\1\143\1\162\2\uffff\1\60\1\uffff"+
        "\1\145\1\60\1\uffff\1\60\1\164\1\145\1\60\1\151\1\145\1\162\1\151"+
        "\1\uffff\1\144\2\uffff\2\60\1\uffff\1\143\1\60\1\151\1\143\1\60"+
        "\2\uffff\1\145\1\uffff\1\160\1\60\1\uffff\1\60\1\164\1\146\2\uffff"+
        "\1\151\1\60\1\157\1\uffff\1\156\1\60\1\uffff";
    static final String DFA13_maxS =
        "\1\176\1\156\1\162\1\165\1\176\2\uffff\2\75\1\164\1\151\2\uffff"+
        "\1\141\1\157\1\164\2\145\1\uffff\1\71\1\145\2\uffff\1\144\1\172"+
        "\1\164\2\154\6\uffff\1\147\1\165\1\154\2\155\1\154\1\163\1\162\1"+
        "\141\1\163\1\164\2\uffff\1\172\1\uffff\1\172\1\154\1\172\1\147\1"+
        "\145\1\172\1\145\1\151\1\163\1\164\1\166\1\164\1\143\1\162\2\uffff"+
        "\1\172\1\uffff\1\145\1\172\1\uffff\1\172\1\164\1\145\1\172\1\151"+
        "\1\145\1\162\1\151\1\uffff\1\144\2\uffff\2\172\1\uffff\1\143\1\172"+
        "\1\151\1\143\1\172\2\uffff\1\145\1\uffff\1\160\1\172\1\uffff\1\172"+
        "\1\164\1\146\2\uffff\1\151\1\172\1\157\1\uffff\1\156\1\172\1\uffff";
    static final String DFA13_acceptS =
        "\5\uffff\1\5\1\6\4\uffff\1\16\1\17\5\uffff\1\34\2\uffff\1\37\1\40"+
        "\5\uffff\1\4\1\7\1\11\1\10\1\13\1\12\13\uffff\1\35\1\36\1\uffff"+
        "\1\2\16\uffff\1\1\1\3\1\uffff\1\23\2\uffff\1\32\10\uffff\1\22\1"+
        "\uffff\1\20\1\33\2\uffff\1\24\5\uffff\1\15\1\21\1\uffff\1\26\2\uffff"+
        "\1\14\3\uffff\1\31\1\25\3\uffff\1\30\2\uffff\1\27";
    static final String DFA13_specialS =
        "\155\uffff}>";
    static final String[] DFA13_transitionS = {
            "\2\25\2\uffff\1\25\22\uffff\1\25\1\6\1\26\5\uffff\1\13\1\14"+
            "\3\uffff\1\23\2\uffff\12\24\2\uffff\1\7\1\4\1\10\2\uffff\32"+
            "\22\4\uffff\1\22\1\uffff\1\1\2\22\1\20\1\22\1\15\1\22\1\16\3"+
            "\22\1\12\1\21\1\3\1\2\3\22\1\17\1\11\6\22\3\uffff\1\5",
            "\1\27",
            "\1\30",
            "\1\33\5\uffff\1\31\5\uffff\1\32",
            "\1\34",
            "",
            "",
            "\1\36",
            "\1\40",
            "\1\42\7\uffff\1\45\10\uffff\1\43\1\uffff\1\44",
            "\1\46",
            "",
            "",
            "\1\47",
            "\1\50",
            "\1\51\16\uffff\1\52",
            "\1\53",
            "\1\54",
            "",
            "\12\24",
            "\1\56\1\uffff\12\24\13\uffff\1\56\37\uffff\1\56",
            "",
            "",
            "\1\57",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\61",
            "\1\62",
            "\1\63",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\64",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\101",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\103",
            "\1\104",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "",
            "\1\117",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\122",
            "\1\123",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\130",
            "",
            "\1\131",
            "",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "",
            "\1\134",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\136",
            "\1\137",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "",
            "",
            "\1\141",
            "",
            "\1\142",
            "\12\22\7\uffff\32\22\4\uffff\1\143\1\uffff\32\22",
            "",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\146",
            "\1\147",
            "",
            "",
            "\1\150",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
            "\1\152",
            "",
            "\1\153",
            "\12\22\7\uffff\32\22\4\uffff\1\22\1\uffff\32\22",
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
            return "1:1: Tokens : ( AND | OR | NOT | APPROXIMATELY | REGEX_MATCH | NOT_EQUAL | EQUAL | LESSER | LESSER_EQUAL | GREATER | GREATER_EQUAL | TAGGED | LIMIT | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | ID | INT | FLOAT | WS | String );";
        }
    }
 

}