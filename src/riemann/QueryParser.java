// $ANTLR 3.2 debian-10 src/riemann/Query.g 2014-12-13 12:59:06
package riemann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class QueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "OR", "NOT", "APPROXIMATELY", "REGEX_MATCH", "NOT_EQUAL", "EQUAL", "LESSER", "LESSER_EQUAL", "GREATER", "GREATER_EQUAL", "TAGGED", "LIMIT", "WS", "String", "INT", "FLOAT", "ID", "EXPONENT", "EscapeSequence", "UnicodeEscape", "HexDigit", "'('", "')'", "'true'", "'false'", "'null'", "'nil'", "'host'", "'service'", "'state'", "'description'", "'metric_f'", "'metric'", "'ttl'", "'time'"
    };
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
    public static final int WS=17;
    public static final int GREATER=13;
    public static final int LESSER=11;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int NOT_EQUAL=9;
    public static final int T__38=38;
    public static final int TAGGED=15;
    public static final int T__39=39;
    public static final int UnicodeEscape=24;
    public static final int EQUAL=10;
    public static final int OR=5;
    public static final int String=18;
    public static final int EscapeSequence=23;
    public static final int GREATER_EQUAL=14;
    public static final int REGEX_MATCH=8;

    // delegates
    // delegators


        public QueryParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public QueryParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return QueryParser.tokenNames; }
    public String getGrammarFileName() { return "src/riemann/Query.g"; }


    public static class expr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expr"
    // src/riemann/Query.g:27:1: expr : ( or EOF ) -> or ;
    public final QueryParser.expr_return expr() throws RecognitionException {
        QueryParser.expr_return retval = new QueryParser.expr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        QueryParser.or_return or1 = null;


        CommonTree EOF2_tree=null;
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        try {
            // src/riemann/Query.g:27:6: ( ( or EOF ) -> or )
            // src/riemann/Query.g:27:8: ( or EOF )
            {
            // src/riemann/Query.g:27:8: ( or EOF )
            // src/riemann/Query.g:27:9: or EOF
            {
            pushFollow(FOLLOW_or_in_expr153);
            or1=or();

            state._fsp--;

            stream_or.add(or1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_expr155);  
            stream_EOF.add(EOF2);


            }



            // AST REWRITE
            // elements: or
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 27:17: -> or
            {
                adaptor.addChild(root_0, stream_or.nextTree());

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expr"

    public static class or_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "or"
    // src/riemann/Query.g:29:1: or : and ( ( WS )* OR ( WS )* and )* ;
    public final QueryParser.or_return or() throws RecognitionException {
        QueryParser.or_return retval = new QueryParser.or_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS4=null;
        Token OR5=null;
        Token WS6=null;
        QueryParser.and_return and3 = null;

        QueryParser.and_return and7 = null;


        CommonTree WS4_tree=null;
        CommonTree OR5_tree=null;
        CommonTree WS6_tree=null;

        try {
            // src/riemann/Query.g:29:4: ( and ( ( WS )* OR ( WS )* and )* )
            // src/riemann/Query.g:29:6: and ( ( WS )* OR ( WS )* and )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_and_in_or168);
            and3=and();

            state._fsp--;

            adaptor.addChild(root_0, and3.getTree());
            // src/riemann/Query.g:29:10: ( ( WS )* OR ( WS )* and )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR||LA3_0==WS) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/riemann/Query.g:29:11: ( WS )* OR ( WS )* and
            	    {
            	    // src/riemann/Query.g:29:11: ( WS )*
            	    loop1:
            	    do {
            	        int alt1=2;
            	        int LA1_0 = input.LA(1);

            	        if ( (LA1_0==WS) ) {
            	            alt1=1;
            	        }


            	        switch (alt1) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:29:11: WS
            	    	    {
            	    	    WS4=(Token)match(input,WS,FOLLOW_WS_in_or171); 
            	    	    WS4_tree = (CommonTree)adaptor.create(WS4);
            	    	    adaptor.addChild(root_0, WS4_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop1;
            	        }
            	    } while (true);

            	    OR5=(Token)match(input,OR,FOLLOW_OR_in_or174); 
            	    OR5_tree = (CommonTree)adaptor.create(OR5);
            	    root_0 = (CommonTree)adaptor.becomeRoot(OR5_tree, root_0);

            	    // src/riemann/Query.g:29:19: ( WS )*
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( (LA2_0==WS) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:29:19: WS
            	    	    {
            	    	    WS6=(Token)match(input,WS,FOLLOW_WS_in_or177); 
            	    	    WS6_tree = (CommonTree)adaptor.create(WS6);
            	    	    adaptor.addChild(root_0, WS6_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop2;
            	        }
            	    } while (true);

            	    pushFollow(FOLLOW_and_in_or180);
            	    and7=and();

            	    state._fsp--;

            	    adaptor.addChild(root_0, and7.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "or"

    public static class and_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and"
    // src/riemann/Query.g:31:1: and : ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* ;
    public final QueryParser.and_return and() throws RecognitionException {
        QueryParser.and_return retval = new QueryParser.and_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS10=null;
        Token AND11=null;
        Token WS12=null;
        QueryParser.not_return not8 = null;

        QueryParser.primary_return primary9 = null;

        QueryParser.not_return not13 = null;

        QueryParser.primary_return primary14 = null;


        CommonTree WS10_tree=null;
        CommonTree AND11_tree=null;
        CommonTree WS12_tree=null;

        try {
            // src/riemann/Query.g:31:5: ( ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* )
            // src/riemann/Query.g:31:7: ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:31:7: ( not | primary )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==NOT) ) {
                alt4=1;
            }
            else if ( ((LA4_0>=TAGGED && LA4_0<=LIMIT)||LA4_0==26||(LA4_0>=28 && LA4_0<=39)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // src/riemann/Query.g:31:8: not
                    {
                    pushFollow(FOLLOW_not_in_and191);
                    not8=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not8.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:31:14: primary
                    {
                    pushFollow(FOLLOW_primary_in_and195);
                    primary9=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary9.getTree());

                    }
                    break;

            }

            // src/riemann/Query.g:31:23: ( ( WS )* AND ( WS )* ( not | primary ) )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // src/riemann/Query.g:31:24: ( WS )* AND ( WS )* ( not | primary )
            	    {
            	    // src/riemann/Query.g:31:24: ( WS )*
            	    loop5:
            	    do {
            	        int alt5=2;
            	        int LA5_0 = input.LA(1);

            	        if ( (LA5_0==WS) ) {
            	            alt5=1;
            	        }


            	        switch (alt5) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:31:24: WS
            	    	    {
            	    	    WS10=(Token)match(input,WS,FOLLOW_WS_in_and199); 
            	    	    WS10_tree = (CommonTree)adaptor.create(WS10);
            	    	    adaptor.addChild(root_0, WS10_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop5;
            	        }
            	    } while (true);

            	    AND11=(Token)match(input,AND,FOLLOW_AND_in_and202); 
            	    AND11_tree = (CommonTree)adaptor.create(AND11);
            	    root_0 = (CommonTree)adaptor.becomeRoot(AND11_tree, root_0);

            	    // src/riemann/Query.g:31:33: ( WS )*
            	    loop6:
            	    do {
            	        int alt6=2;
            	        int LA6_0 = input.LA(1);

            	        if ( (LA6_0==WS) ) {
            	            alt6=1;
            	        }


            	        switch (alt6) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:31:33: WS
            	    	    {
            	    	    WS12=(Token)match(input,WS,FOLLOW_WS_in_and205); 
            	    	    WS12_tree = (CommonTree)adaptor.create(WS12);
            	    	    adaptor.addChild(root_0, WS12_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop6;
            	        }
            	    } while (true);

            	    // src/riemann/Query.g:31:37: ( not | primary )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==NOT) ) {
            	        alt7=1;
            	    }
            	    else if ( ((LA7_0>=TAGGED && LA7_0<=LIMIT)||LA7_0==26||(LA7_0>=28 && LA7_0<=39)) ) {
            	        alt7=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // src/riemann/Query.g:31:38: not
            	            {
            	            pushFollow(FOLLOW_not_in_and209);
            	            not13=not();

            	            state._fsp--;

            	            adaptor.addChild(root_0, not13.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // src/riemann/Query.g:31:44: primary
            	            {
            	            pushFollow(FOLLOW_primary_in_and213);
            	            primary14=primary();

            	            state._fsp--;

            	            adaptor.addChild(root_0, primary14.getTree());

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and"

    public static class not_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not"
    // src/riemann/Query.g:33:1: not : NOT ( WS )* ( not | primary ) ;
    public final QueryParser.not_return not() throws RecognitionException {
        QueryParser.not_return retval = new QueryParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT15=null;
        Token WS16=null;
        QueryParser.not_return not17 = null;

        QueryParser.primary_return primary18 = null;


        CommonTree NOT15_tree=null;
        CommonTree WS16_tree=null;

        try {
            // src/riemann/Query.g:33:5: ( NOT ( WS )* ( not | primary ) )
            // src/riemann/Query.g:33:7: NOT ( WS )* ( not | primary )
            {
            root_0 = (CommonTree)adaptor.nil();

            NOT15=(Token)match(input,NOT,FOLLOW_NOT_in_not224); 
            NOT15_tree = (CommonTree)adaptor.create(NOT15);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT15_tree, root_0);

            // src/riemann/Query.g:33:12: ( WS )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==WS) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // src/riemann/Query.g:33:12: WS
            	    {
            	    WS16=(Token)match(input,WS,FOLLOW_WS_in_not227); 
            	    WS16_tree = (CommonTree)adaptor.create(WS16);
            	    adaptor.addChild(root_0, WS16_tree);


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // src/riemann/Query.g:33:16: ( not | primary )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==NOT) ) {
                alt10=1;
            }
            else if ( ((LA10_0>=TAGGED && LA10_0<=LIMIT)||LA10_0==26||(LA10_0>=28 && LA10_0<=39)) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // src/riemann/Query.g:33:17: not
                    {
                    pushFollow(FOLLOW_not_in_not231);
                    not17=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not17.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:33:23: primary
                    {
                    pushFollow(FOLLOW_primary_in_not235);
                    primary18=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary18.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not"

    public static class primary_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primary"
    // src/riemann/Query.g:36:1: primary : ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) ;
    public final QueryParser.primary_return primary() throws RecognitionException {
        QueryParser.primary_return retval = new QueryParser.primary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal19=null;
        Token char_literal21=null;
        QueryParser.or_return or20 = null;

        QueryParser.simple_return simple22 = null;


        CommonTree char_literal19_tree=null;
        CommonTree char_literal21_tree=null;
        RewriteRuleTokenStream stream_26=new RewriteRuleTokenStream(adaptor,"token 26");
        RewriteRuleTokenStream stream_27=new RewriteRuleTokenStream(adaptor,"token 27");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_simple=new RewriteRuleSubtreeStream(adaptor,"rule simple");
        try {
            // src/riemann/Query.g:36:9: ( ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) )
            // src/riemann/Query.g:36:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            {
            // src/riemann/Query.g:36:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==26) ) {
                alt11=1;
            }
            else if ( ((LA11_0>=TAGGED && LA11_0<=LIMIT)||(LA11_0>=28 && LA11_0<=39)) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // src/riemann/Query.g:37:4: ( '(' or ')' )
                    {
                    // src/riemann/Query.g:37:4: ( '(' or ')' )
                    // src/riemann/Query.g:37:5: '(' or ')'
                    {
                    char_literal19=(Token)match(input,26,FOLLOW_26_in_primary252);  
                    stream_26.add(char_literal19);

                    pushFollow(FOLLOW_or_in_primary254);
                    or20=or();

                    state._fsp--;

                    stream_or.add(or20.getTree());
                    char_literal21=(Token)match(input,27,FOLLOW_27_in_primary256);  
                    stream_27.add(char_literal21);


                    }



                    // AST REWRITE
                    // elements: or
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 37:17: -> ^( or )
                    {
                        // src/riemann/Query.g:37:20: ^( or )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_or.nextNode(), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:38:6: simple
                    {
                    pushFollow(FOLLOW_simple_in_primary270);
                    simple22=simple();

                    state._fsp--;

                    stream_simple.add(simple22.getTree());


                    // AST REWRITE
                    // elements: simple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 38:13: -> simple
                    {
                        adaptor.addChild(root_0, stream_simple.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primary"

    public static class simple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "simple"
    // src/riemann/Query.g:41:1: fragment simple : ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal | limit ) ;
    public final QueryParser.simple_return simple() throws RecognitionException {
        QueryParser.simple_return retval = new QueryParser.simple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        QueryParser.t_return t23 = null;

        QueryParser.f_return f24 = null;

        QueryParser.nil_return nil25 = null;

        QueryParser.tagged_return tagged26 = null;

        QueryParser.approximately_return approximately27 = null;

        QueryParser.regex_match_return regex_match28 = null;

        QueryParser.lesser_return lesser29 = null;

        QueryParser.lesser_equal_return lesser_equal30 = null;

        QueryParser.greater_return greater31 = null;

        QueryParser.greater_equal_return greater_equal32 = null;

        QueryParser.not_equal_return not_equal33 = null;

        QueryParser.equal_return equal34 = null;

        QueryParser.limit_return limit35 = null;



        try {
            // src/riemann/Query.g:42:8: ( ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal | limit ) )
            // src/riemann/Query.g:42:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal | limit )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:42:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal | limit )
            int alt12=13;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // src/riemann/Query.g:42:12: t
                    {
                    pushFollow(FOLLOW_t_in_simple290);
                    t23=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t23.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:42:16: f
                    {
                    pushFollow(FOLLOW_f_in_simple294);
                    f24=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f24.getTree());

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:42:20: nil
                    {
                    pushFollow(FOLLOW_nil_in_simple298);
                    nil25=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil25.getTree());

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:43:5: tagged
                    {
                    pushFollow(FOLLOW_tagged_in_simple304);
                    tagged26=tagged();

                    state._fsp--;

                    adaptor.addChild(root_0, tagged26.getTree());

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:44:5: approximately
                    {
                    pushFollow(FOLLOW_approximately_in_simple310);
                    approximately27=approximately();

                    state._fsp--;

                    adaptor.addChild(root_0, approximately27.getTree());

                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:45:5: regex_match
                    {
                    pushFollow(FOLLOW_regex_match_in_simple316);
                    regex_match28=regex_match();

                    state._fsp--;

                    adaptor.addChild(root_0, regex_match28.getTree());

                    }
                    break;
                case 7 :
                    // src/riemann/Query.g:46:5: lesser
                    {
                    pushFollow(FOLLOW_lesser_in_simple322);
                    lesser29=lesser();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser29.getTree());

                    }
                    break;
                case 8 :
                    // src/riemann/Query.g:47:5: lesser_equal
                    {
                    pushFollow(FOLLOW_lesser_equal_in_simple328);
                    lesser_equal30=lesser_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser_equal30.getTree());

                    }
                    break;
                case 9 :
                    // src/riemann/Query.g:48:5: greater
                    {
                    pushFollow(FOLLOW_greater_in_simple334);
                    greater31=greater();

                    state._fsp--;

                    adaptor.addChild(root_0, greater31.getTree());

                    }
                    break;
                case 10 :
                    // src/riemann/Query.g:49:5: greater_equal
                    {
                    pushFollow(FOLLOW_greater_equal_in_simple340);
                    greater_equal32=greater_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, greater_equal32.getTree());

                    }
                    break;
                case 11 :
                    // src/riemann/Query.g:50:5: not_equal
                    {
                    pushFollow(FOLLOW_not_equal_in_simple346);
                    not_equal33=not_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, not_equal33.getTree());

                    }
                    break;
                case 12 :
                    // src/riemann/Query.g:51:5: equal
                    {
                    pushFollow(FOLLOW_equal_in_simple352);
                    equal34=equal();

                    state._fsp--;

                    adaptor.addChild(root_0, equal34.getTree());

                    }
                    break;
                case 13 :
                    // src/riemann/Query.g:52:5: limit
                    {
                    pushFollow(FOLLOW_limit_in_simple358);
                    limit35=limit();

                    state._fsp--;

                    adaptor.addChild(root_0, limit35.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "simple"

    public static class approximately_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "approximately"
    // src/riemann/Query.g:55:1: approximately : field ( WS )* APPROXIMATELY ( WS )* value ;
    public final QueryParser.approximately_return approximately() throws RecognitionException {
        QueryParser.approximately_return retval = new QueryParser.approximately_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS37=null;
        Token APPROXIMATELY38=null;
        Token WS39=null;
        QueryParser.field_return field36 = null;

        QueryParser.value_return value40 = null;


        CommonTree WS37_tree=null;
        CommonTree APPROXIMATELY38_tree=null;
        CommonTree WS39_tree=null;

        try {
            // src/riemann/Query.g:56:2: ( field ( WS )* APPROXIMATELY ( WS )* value )
            // src/riemann/Query.g:56:4: field ( WS )* APPROXIMATELY ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_approximately371);
            field36=field();

            state._fsp--;

            adaptor.addChild(root_0, field36.getTree());
            // src/riemann/Query.g:56:10: ( WS )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==WS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // src/riemann/Query.g:56:10: WS
            	    {
            	    WS37=(Token)match(input,WS,FOLLOW_WS_in_approximately373); 
            	    WS37_tree = (CommonTree)adaptor.create(WS37);
            	    adaptor.addChild(root_0, WS37_tree);


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            APPROXIMATELY38=(Token)match(input,APPROXIMATELY,FOLLOW_APPROXIMATELY_in_approximately376); 
            APPROXIMATELY38_tree = (CommonTree)adaptor.create(APPROXIMATELY38);
            root_0 = (CommonTree)adaptor.becomeRoot(APPROXIMATELY38_tree, root_0);

            // src/riemann/Query.g:56:29: ( WS )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==WS) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // src/riemann/Query.g:56:29: WS
            	    {
            	    WS39=(Token)match(input,WS,FOLLOW_WS_in_approximately379); 
            	    WS39_tree = (CommonTree)adaptor.create(WS39);
            	    adaptor.addChild(root_0, WS39_tree);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_approximately382);
            value40=value();

            state._fsp--;

            adaptor.addChild(root_0, value40.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "approximately"

    public static class regex_match_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "regex_match"
    // src/riemann/Query.g:57:1: regex_match : field ( WS )* REGEX_MATCH ( WS )* value ;
    public final QueryParser.regex_match_return regex_match() throws RecognitionException {
        QueryParser.regex_match_return retval = new QueryParser.regex_match_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS42=null;
        Token REGEX_MATCH43=null;
        Token WS44=null;
        QueryParser.field_return field41 = null;

        QueryParser.value_return value45 = null;


        CommonTree WS42_tree=null;
        CommonTree REGEX_MATCH43_tree=null;
        CommonTree WS44_tree=null;

        try {
            // src/riemann/Query.g:58:2: ( field ( WS )* REGEX_MATCH ( WS )* value )
            // src/riemann/Query.g:58:4: field ( WS )* REGEX_MATCH ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_regex_match390);
            field41=field();

            state._fsp--;

            adaptor.addChild(root_0, field41.getTree());
            // src/riemann/Query.g:58:10: ( WS )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==WS) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // src/riemann/Query.g:58:10: WS
            	    {
            	    WS42=(Token)match(input,WS,FOLLOW_WS_in_regex_match392); 
            	    WS42_tree = (CommonTree)adaptor.create(WS42);
            	    adaptor.addChild(root_0, WS42_tree);


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            REGEX_MATCH43=(Token)match(input,REGEX_MATCH,FOLLOW_REGEX_MATCH_in_regex_match395); 
            REGEX_MATCH43_tree = (CommonTree)adaptor.create(REGEX_MATCH43);
            root_0 = (CommonTree)adaptor.becomeRoot(REGEX_MATCH43_tree, root_0);

            // src/riemann/Query.g:58:27: ( WS )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==WS) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // src/riemann/Query.g:58:27: WS
            	    {
            	    WS44=(Token)match(input,WS,FOLLOW_WS_in_regex_match398); 
            	    WS44_tree = (CommonTree)adaptor.create(WS44);
            	    adaptor.addChild(root_0, WS44_tree);


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_regex_match401);
            value45=value();

            state._fsp--;

            adaptor.addChild(root_0, value45.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "regex_match"

    public static class lesser_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lesser"
    // src/riemann/Query.g:59:1: lesser : field ( WS )* LESSER ( WS )* value ;
    public final QueryParser.lesser_return lesser() throws RecognitionException {
        QueryParser.lesser_return retval = new QueryParser.lesser_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS47=null;
        Token LESSER48=null;
        Token WS49=null;
        QueryParser.field_return field46 = null;

        QueryParser.value_return value50 = null;


        CommonTree WS47_tree=null;
        CommonTree LESSER48_tree=null;
        CommonTree WS49_tree=null;

        try {
            // src/riemann/Query.g:59:8: ( field ( WS )* LESSER ( WS )* value )
            // src/riemann/Query.g:59:10: field ( WS )* LESSER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser408);
            field46=field();

            state._fsp--;

            adaptor.addChild(root_0, field46.getTree());
            // src/riemann/Query.g:59:16: ( WS )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==WS) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src/riemann/Query.g:59:16: WS
            	    {
            	    WS47=(Token)match(input,WS,FOLLOW_WS_in_lesser410); 
            	    WS47_tree = (CommonTree)adaptor.create(WS47);
            	    adaptor.addChild(root_0, WS47_tree);


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            LESSER48=(Token)match(input,LESSER,FOLLOW_LESSER_in_lesser413); 
            LESSER48_tree = (CommonTree)adaptor.create(LESSER48);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER48_tree, root_0);

            // src/riemann/Query.g:59:28: ( WS )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==WS) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src/riemann/Query.g:59:28: WS
            	    {
            	    WS49=(Token)match(input,WS,FOLLOW_WS_in_lesser416); 
            	    WS49_tree = (CommonTree)adaptor.create(WS49);
            	    adaptor.addChild(root_0, WS49_tree);


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser419);
            value50=value();

            state._fsp--;

            adaptor.addChild(root_0, value50.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lesser"

    public static class lesser_equal_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lesser_equal"
    // src/riemann/Query.g:60:1: lesser_equal : field ( WS )* LESSER_EQUAL ( WS )* value ;
    public final QueryParser.lesser_equal_return lesser_equal() throws RecognitionException {
        QueryParser.lesser_equal_return retval = new QueryParser.lesser_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS52=null;
        Token LESSER_EQUAL53=null;
        Token WS54=null;
        QueryParser.field_return field51 = null;

        QueryParser.value_return value55 = null;


        CommonTree WS52_tree=null;
        CommonTree LESSER_EQUAL53_tree=null;
        CommonTree WS54_tree=null;

        try {
            // src/riemann/Query.g:61:2: ( field ( WS )* LESSER_EQUAL ( WS )* value )
            // src/riemann/Query.g:61:4: field ( WS )* LESSER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser_equal427);
            field51=field();

            state._fsp--;

            adaptor.addChild(root_0, field51.getTree());
            // src/riemann/Query.g:61:10: ( WS )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==WS) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // src/riemann/Query.g:61:10: WS
            	    {
            	    WS52=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal429); 
            	    WS52_tree = (CommonTree)adaptor.create(WS52);
            	    adaptor.addChild(root_0, WS52_tree);


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            LESSER_EQUAL53=(Token)match(input,LESSER_EQUAL,FOLLOW_LESSER_EQUAL_in_lesser_equal432); 
            LESSER_EQUAL53_tree = (CommonTree)adaptor.create(LESSER_EQUAL53);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER_EQUAL53_tree, root_0);

            // src/riemann/Query.g:61:28: ( WS )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==WS) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // src/riemann/Query.g:61:28: WS
            	    {
            	    WS54=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal435); 
            	    WS54_tree = (CommonTree)adaptor.create(WS54);
            	    adaptor.addChild(root_0, WS54_tree);


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser_equal438);
            value55=value();

            state._fsp--;

            adaptor.addChild(root_0, value55.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lesser_equal"

    public static class greater_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "greater"
    // src/riemann/Query.g:62:1: greater : field ( WS )* GREATER ( WS )* value ;
    public final QueryParser.greater_return greater() throws RecognitionException {
        QueryParser.greater_return retval = new QueryParser.greater_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS57=null;
        Token GREATER58=null;
        Token WS59=null;
        QueryParser.field_return field56 = null;

        QueryParser.value_return value60 = null;


        CommonTree WS57_tree=null;
        CommonTree GREATER58_tree=null;
        CommonTree WS59_tree=null;

        try {
            // src/riemann/Query.g:62:9: ( field ( WS )* GREATER ( WS )* value )
            // src/riemann/Query.g:62:11: field ( WS )* GREATER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater445);
            field56=field();

            state._fsp--;

            adaptor.addChild(root_0, field56.getTree());
            // src/riemann/Query.g:62:17: ( WS )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==WS) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // src/riemann/Query.g:62:17: WS
            	    {
            	    WS57=(Token)match(input,WS,FOLLOW_WS_in_greater447); 
            	    WS57_tree = (CommonTree)adaptor.create(WS57);
            	    adaptor.addChild(root_0, WS57_tree);


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            GREATER58=(Token)match(input,GREATER,FOLLOW_GREATER_in_greater450); 
            GREATER58_tree = (CommonTree)adaptor.create(GREATER58);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER58_tree, root_0);

            // src/riemann/Query.g:62:30: ( WS )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==WS) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // src/riemann/Query.g:62:30: WS
            	    {
            	    WS59=(Token)match(input,WS,FOLLOW_WS_in_greater453); 
            	    WS59_tree = (CommonTree)adaptor.create(WS59);
            	    adaptor.addChild(root_0, WS59_tree);


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater456);
            value60=value();

            state._fsp--;

            adaptor.addChild(root_0, value60.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "greater"

    public static class greater_equal_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "greater_equal"
    // src/riemann/Query.g:63:1: greater_equal : field ( WS )* GREATER_EQUAL ( WS )* value ;
    public final QueryParser.greater_equal_return greater_equal() throws RecognitionException {
        QueryParser.greater_equal_return retval = new QueryParser.greater_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS62=null;
        Token GREATER_EQUAL63=null;
        Token WS64=null;
        QueryParser.field_return field61 = null;

        QueryParser.value_return value65 = null;


        CommonTree WS62_tree=null;
        CommonTree GREATER_EQUAL63_tree=null;
        CommonTree WS64_tree=null;

        try {
            // src/riemann/Query.g:64:2: ( field ( WS )* GREATER_EQUAL ( WS )* value )
            // src/riemann/Query.g:64:4: field ( WS )* GREATER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater_equal464);
            field61=field();

            state._fsp--;

            adaptor.addChild(root_0, field61.getTree());
            // src/riemann/Query.g:64:10: ( WS )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==WS) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // src/riemann/Query.g:64:10: WS
            	    {
            	    WS62=(Token)match(input,WS,FOLLOW_WS_in_greater_equal466); 
            	    WS62_tree = (CommonTree)adaptor.create(WS62);
            	    adaptor.addChild(root_0, WS62_tree);


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            GREATER_EQUAL63=(Token)match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_greater_equal469); 
            GREATER_EQUAL63_tree = (CommonTree)adaptor.create(GREATER_EQUAL63);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER_EQUAL63_tree, root_0);

            // src/riemann/Query.g:64:29: ( WS )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==WS) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // src/riemann/Query.g:64:29: WS
            	    {
            	    WS64=(Token)match(input,WS,FOLLOW_WS_in_greater_equal472); 
            	    WS64_tree = (CommonTree)adaptor.create(WS64);
            	    adaptor.addChild(root_0, WS64_tree);


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater_equal475);
            value65=value();

            state._fsp--;

            adaptor.addChild(root_0, value65.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "greater_equal"

    public static class not_equal_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not_equal"
    // src/riemann/Query.g:65:1: not_equal : field ( WS )* NOT_EQUAL ( WS )* value ;
    public final QueryParser.not_equal_return not_equal() throws RecognitionException {
        QueryParser.not_equal_return retval = new QueryParser.not_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS67=null;
        Token NOT_EQUAL68=null;
        Token WS69=null;
        QueryParser.field_return field66 = null;

        QueryParser.value_return value70 = null;


        CommonTree WS67_tree=null;
        CommonTree NOT_EQUAL68_tree=null;
        CommonTree WS69_tree=null;

        try {
            // src/riemann/Query.g:66:2: ( field ( WS )* NOT_EQUAL ( WS )* value )
            // src/riemann/Query.g:66:4: field ( WS )* NOT_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_not_equal483);
            field66=field();

            state._fsp--;

            adaptor.addChild(root_0, field66.getTree());
            // src/riemann/Query.g:66:10: ( WS )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==WS) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // src/riemann/Query.g:66:10: WS
            	    {
            	    WS67=(Token)match(input,WS,FOLLOW_WS_in_not_equal485); 
            	    WS67_tree = (CommonTree)adaptor.create(WS67);
            	    adaptor.addChild(root_0, WS67_tree);


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            NOT_EQUAL68=(Token)match(input,NOT_EQUAL,FOLLOW_NOT_EQUAL_in_not_equal488); 
            NOT_EQUAL68_tree = (CommonTree)adaptor.create(NOT_EQUAL68);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT_EQUAL68_tree, root_0);

            // src/riemann/Query.g:66:25: ( WS )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==WS) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // src/riemann/Query.g:66:25: WS
            	    {
            	    WS69=(Token)match(input,WS,FOLLOW_WS_in_not_equal491); 
            	    WS69_tree = (CommonTree)adaptor.create(WS69);
            	    adaptor.addChild(root_0, WS69_tree);


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_not_equal494);
            value70=value();

            state._fsp--;

            adaptor.addChild(root_0, value70.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not_equal"

    public static class equal_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "equal"
    // src/riemann/Query.g:67:1: equal : field ( WS )* EQUAL ( WS )* value ;
    public final QueryParser.equal_return equal() throws RecognitionException {
        QueryParser.equal_return retval = new QueryParser.equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS72=null;
        Token EQUAL73=null;
        Token WS74=null;
        QueryParser.field_return field71 = null;

        QueryParser.value_return value75 = null;


        CommonTree WS72_tree=null;
        CommonTree EQUAL73_tree=null;
        CommonTree WS74_tree=null;

        try {
            // src/riemann/Query.g:67:7: ( field ( WS )* EQUAL ( WS )* value )
            // src/riemann/Query.g:67:9: field ( WS )* EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_equal502);
            field71=field();

            state._fsp--;

            adaptor.addChild(root_0, field71.getTree());
            // src/riemann/Query.g:67:15: ( WS )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==WS) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // src/riemann/Query.g:67:15: WS
            	    {
            	    WS72=(Token)match(input,WS,FOLLOW_WS_in_equal504); 
            	    WS72_tree = (CommonTree)adaptor.create(WS72);
            	    adaptor.addChild(root_0, WS72_tree);


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);

            EQUAL73=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_equal507); 
            EQUAL73_tree = (CommonTree)adaptor.create(EQUAL73);
            root_0 = (CommonTree)adaptor.becomeRoot(EQUAL73_tree, root_0);

            // src/riemann/Query.g:67:26: ( WS )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==WS) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // src/riemann/Query.g:67:26: WS
            	    {
            	    WS74=(Token)match(input,WS,FOLLOW_WS_in_equal510); 
            	    WS74_tree = (CommonTree)adaptor.create(WS74);
            	    adaptor.addChild(root_0, WS74_tree);


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_equal513);
            value75=value();

            state._fsp--;

            adaptor.addChild(root_0, value75.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "equal"

    public static class tagged_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tagged"
    // src/riemann/Query.g:69:1: tagged : TAGGED ( WS )* String ;
    public final QueryParser.tagged_return tagged() throws RecognitionException {
        QueryParser.tagged_return retval = new QueryParser.tagged_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TAGGED76=null;
        Token WS77=null;
        Token String78=null;

        CommonTree TAGGED76_tree=null;
        CommonTree WS77_tree=null;
        CommonTree String78_tree=null;

        try {
            // src/riemann/Query.g:69:8: ( TAGGED ( WS )* String )
            // src/riemann/Query.g:69:10: TAGGED ( WS )* String
            {
            root_0 = (CommonTree)adaptor.nil();

            TAGGED76=(Token)match(input,TAGGED,FOLLOW_TAGGED_in_tagged521); 
            TAGGED76_tree = (CommonTree)adaptor.create(TAGGED76);
            root_0 = (CommonTree)adaptor.becomeRoot(TAGGED76_tree, root_0);

            // src/riemann/Query.g:69:18: ( WS )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==WS) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // src/riemann/Query.g:69:18: WS
            	    {
            	    WS77=(Token)match(input,WS,FOLLOW_WS_in_tagged524); 
            	    WS77_tree = (CommonTree)adaptor.create(WS77);
            	    adaptor.addChild(root_0, WS77_tree);


            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);

            String78=(Token)match(input,String,FOLLOW_String_in_tagged527); 
            String78_tree = (CommonTree)adaptor.create(String78);
            adaptor.addChild(root_0, String78_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tagged"

    public static class limit_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "limit"
    // src/riemann/Query.g:71:1: limit : LIMIT ( WS )* INT ( WS )* primary ;
    public final QueryParser.limit_return limit() throws RecognitionException {
        QueryParser.limit_return retval = new QueryParser.limit_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LIMIT79=null;
        Token WS80=null;
        Token INT81=null;
        Token WS82=null;
        QueryParser.primary_return primary83 = null;


        CommonTree LIMIT79_tree=null;
        CommonTree WS80_tree=null;
        CommonTree INT81_tree=null;
        CommonTree WS82_tree=null;

        try {
            // src/riemann/Query.g:71:7: ( LIMIT ( WS )* INT ( WS )* primary )
            // src/riemann/Query.g:71:9: LIMIT ( WS )* INT ( WS )* primary
            {
            root_0 = (CommonTree)adaptor.nil();

            LIMIT79=(Token)match(input,LIMIT,FOLLOW_LIMIT_in_limit535); 
            LIMIT79_tree = (CommonTree)adaptor.create(LIMIT79);
            root_0 = (CommonTree)adaptor.becomeRoot(LIMIT79_tree, root_0);

            // src/riemann/Query.g:71:16: ( WS )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==WS) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // src/riemann/Query.g:71:16: WS
            	    {
            	    WS80=(Token)match(input,WS,FOLLOW_WS_in_limit538); 
            	    WS80_tree = (CommonTree)adaptor.create(WS80);
            	    adaptor.addChild(root_0, WS80_tree);


            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);

            INT81=(Token)match(input,INT,FOLLOW_INT_in_limit541); 
            INT81_tree = (CommonTree)adaptor.create(INT81);
            adaptor.addChild(root_0, INT81_tree);

            // src/riemann/Query.g:71:24: ( WS )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==WS) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // src/riemann/Query.g:71:24: WS
            	    {
            	    WS82=(Token)match(input,WS,FOLLOW_WS_in_limit543); 
            	    WS82_tree = (CommonTree)adaptor.create(WS82);
            	    adaptor.addChild(root_0, WS82_tree);


            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);

            pushFollow(FOLLOW_primary_in_limit546);
            primary83=primary();

            state._fsp--;

            adaptor.addChild(root_0, primary83.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "limit"

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // src/riemann/Query.g:73:1: value : ( String | t | f | nil | INT | FLOAT ) ;
    public final QueryParser.value_return value() throws RecognitionException {
        QueryParser.value_return retval = new QueryParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token String84=null;
        Token INT88=null;
        Token FLOAT89=null;
        QueryParser.t_return t85 = null;

        QueryParser.f_return f86 = null;

        QueryParser.nil_return nil87 = null;


        CommonTree String84_tree=null;
        CommonTree INT88_tree=null;
        CommonTree FLOAT89_tree=null;

        try {
            // src/riemann/Query.g:73:7: ( ( String | t | f | nil | INT | FLOAT ) )
            // src/riemann/Query.g:73:10: ( String | t | f | nil | INT | FLOAT )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:73:10: ( String | t | f | nil | INT | FLOAT )
            int alt32=6;
            switch ( input.LA(1) ) {
            case String:
                {
                alt32=1;
                }
                break;
            case 28:
                {
                alt32=2;
                }
                break;
            case 29:
                {
                alt32=3;
                }
                break;
            case 30:
            case 31:
                {
                alt32=4;
                }
                break;
            case INT:
                {
                alt32=5;
                }
                break;
            case FLOAT:
                {
                alt32=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // src/riemann/Query.g:73:11: String
                    {
                    String84=(Token)match(input,String,FOLLOW_String_in_value556); 
                    String84_tree = (CommonTree)adaptor.create(String84);
                    adaptor.addChild(root_0, String84_tree);


                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:73:20: t
                    {
                    pushFollow(FOLLOW_t_in_value560);
                    t85=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t85.getTree());

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:73:24: f
                    {
                    pushFollow(FOLLOW_f_in_value564);
                    f86=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f86.getTree());

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:73:28: nil
                    {
                    pushFollow(FOLLOW_nil_in_value568);
                    nil87=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil87.getTree());

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:73:34: INT
                    {
                    INT88=(Token)match(input,INT,FOLLOW_INT_in_value572); 
                    INT88_tree = (CommonTree)adaptor.create(INT88);
                    adaptor.addChild(root_0, INT88_tree);


                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:73:40: FLOAT
                    {
                    FLOAT89=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_value576); 
                    FLOAT89_tree = (CommonTree)adaptor.create(FLOAT89);
                    adaptor.addChild(root_0, FLOAT89_tree);


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "value"

    public static class t_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "t"
    // src/riemann/Query.g:75:1: t : 'true' ;
    public final QueryParser.t_return t() throws RecognitionException {
        QueryParser.t_return retval = new QueryParser.t_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal90=null;

        CommonTree string_literal90_tree=null;

        try {
            // src/riemann/Query.g:75:3: ( 'true' )
            // src/riemann/Query.g:75:5: 'true'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal90=(Token)match(input,28,FOLLOW_28_in_t585); 
            string_literal90_tree = (CommonTree)adaptor.create(string_literal90);
            adaptor.addChild(root_0, string_literal90_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "t"

    public static class f_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "f"
    // src/riemann/Query.g:76:1: f : 'false' ;
    public final QueryParser.f_return f() throws RecognitionException {
        QueryParser.f_return retval = new QueryParser.f_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal91=null;

        CommonTree string_literal91_tree=null;

        try {
            // src/riemann/Query.g:76:3: ( 'false' )
            // src/riemann/Query.g:76:5: 'false'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal91=(Token)match(input,29,FOLLOW_29_in_f592); 
            string_literal91_tree = (CommonTree)adaptor.create(string_literal91);
            adaptor.addChild(root_0, string_literal91_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "f"

    public static class nil_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nil"
    // src/riemann/Query.g:77:1: nil : ( 'null' | 'nil' );
    public final QueryParser.nil_return nil() throws RecognitionException {
        QueryParser.nil_return retval = new QueryParser.nil_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set92=null;

        CommonTree set92_tree=null;

        try {
            // src/riemann/Query.g:77:5: ( 'null' | 'nil' )
            // src/riemann/Query.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set92=(Token)input.LT(1);
            if ( (input.LA(1)>=30 && input.LA(1)<=31) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set92));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nil"

    public static class field_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "field"
    // src/riemann/Query.g:79:1: field : ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' ) ;
    public final QueryParser.field_return field() throws RecognitionException {
        QueryParser.field_return retval = new QueryParser.field_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set93=null;

        CommonTree set93_tree=null;

        try {
            // src/riemann/Query.g:79:7: ( ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' ) )
            // src/riemann/Query.g:79:9: ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set93=(Token)input.LT(1);
            if ( (input.LA(1)>=32 && input.LA(1)<=39) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set93));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "field"

    // Delegated rules


    protected DFA8 dfa8 = new DFA8(this);
    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA8_eotS =
        "\4\uffff";
    static final String DFA8_eofS =
        "\1\2\3\uffff";
    static final String DFA8_minS =
        "\2\4\2\uffff";
    static final String DFA8_maxS =
        "\1\33\1\21\2\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA8_specialS =
        "\4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\3\1\2\13\uffff\1\1\11\uffff\1\2",
            "\1\3\1\2\13\uffff\1\1",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "()* loopback of 31:23: ( ( WS )* AND ( WS )* ( not | primary ) )*";
        }
    }
    static final String DFA12_eotS =
        "\20\uffff";
    static final String DFA12_eofS =
        "\20\uffff";
    static final String DFA12_minS =
        "\1\17\4\uffff\1\7\1\uffff\1\7\10\uffff";
    static final String DFA12_maxS =
        "\1\47\4\uffff\1\21\1\uffff\1\21\10\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\uffff\1\15\1\uffff\1\14\1\7\1\13\1\11"+
        "\1\6\1\10\1\5\1\12";
    static final String DFA12_specialS =
        "\20\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\4\1\6\13\uffff\1\1\1\2\2\3\10\5",
            "",
            "",
            "",
            "",
            "\1\16\1\14\1\12\1\10\1\11\1\15\1\13\1\17\2\uffff\1\7",
            "",
            "\1\16\1\14\1\12\1\10\1\11\1\15\1\13\1\17\2\uffff\1\7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "42:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal | limit )";
        }
    }
 

    public static final BitSet FOLLOW_or_in_expr153 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_expr155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_or168 = new BitSet(new long[]{0x0000000000020022L});
    public static final BitSet FOLLOW_WS_in_or171 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_OR_in_or174 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_WS_in_or177 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_and_in_or180 = new BitSet(new long[]{0x0000000000020022L});
    public static final BitSet FOLLOW_not_in_and191 = new BitSet(new long[]{0x0000000000020012L});
    public static final BitSet FOLLOW_primary_in_and195 = new BitSet(new long[]{0x0000000000020012L});
    public static final BitSet FOLLOW_WS_in_and199 = new BitSet(new long[]{0x0000000000020010L});
    public static final BitSet FOLLOW_AND_in_and202 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_WS_in_and205 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_not_in_and209 = new BitSet(new long[]{0x0000000000020012L});
    public static final BitSet FOLLOW_primary_in_and213 = new BitSet(new long[]{0x0000000000020012L});
    public static final BitSet FOLLOW_NOT_in_not224 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_WS_in_not227 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_not_in_not231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_not235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_primary252 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_or_in_primary254 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_primary256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_in_primary270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_simple290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_simple294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_simple298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tagged_in_simple304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_approximately_in_simple310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regex_match_in_simple316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_in_simple322 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_equal_in_simple328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_in_simple334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_equal_in_simple340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equal_in_simple346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equal_in_simple352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_limit_in_simple358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_approximately371 = new BitSet(new long[]{0x0000000000020080L});
    public static final BitSet FOLLOW_WS_in_approximately373 = new BitSet(new long[]{0x0000000000020080L});
    public static final BitSet FOLLOW_APPROXIMATELY_in_approximately376 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_approximately379 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_approximately382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_regex_match390 = new BitSet(new long[]{0x0000000000020100L});
    public static final BitSet FOLLOW_WS_in_regex_match392 = new BitSet(new long[]{0x0000000000020100L});
    public static final BitSet FOLLOW_REGEX_MATCH_in_regex_match395 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_regex_match398 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_regex_match401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser408 = new BitSet(new long[]{0x0000000000020800L});
    public static final BitSet FOLLOW_WS_in_lesser410 = new BitSet(new long[]{0x0000000000020800L});
    public static final BitSet FOLLOW_LESSER_in_lesser413 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_lesser416 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_lesser419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser_equal427 = new BitSet(new long[]{0x0000000000021000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal429 = new BitSet(new long[]{0x0000000000021000L});
    public static final BitSet FOLLOW_LESSER_EQUAL_in_lesser_equal432 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal435 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_lesser_equal438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater445 = new BitSet(new long[]{0x0000000000022000L});
    public static final BitSet FOLLOW_WS_in_greater447 = new BitSet(new long[]{0x0000000000022000L});
    public static final BitSet FOLLOW_GREATER_in_greater450 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_greater453 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_greater456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater_equal464 = new BitSet(new long[]{0x0000000000024000L});
    public static final BitSet FOLLOW_WS_in_greater_equal466 = new BitSet(new long[]{0x0000000000024000L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_greater_equal469 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_greater_equal472 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_greater_equal475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_not_equal483 = new BitSet(new long[]{0x0000000000020200L});
    public static final BitSet FOLLOW_WS_in_not_equal485 = new BitSet(new long[]{0x0000000000020200L});
    public static final BitSet FOLLOW_NOT_EQUAL_in_not_equal488 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_not_equal491 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_not_equal494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_equal502 = new BitSet(new long[]{0x0000000000020400L});
    public static final BitSet FOLLOW_WS_in_equal504 = new BitSet(new long[]{0x0000000000020400L});
    public static final BitSet FOLLOW_EQUAL_in_equal507 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_WS_in_equal510 = new BitSet(new long[]{0x00000000F01E0000L});
    public static final BitSet FOLLOW_value_in_equal513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TAGGED_in_tagged521 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_WS_in_tagged524 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_String_in_tagged527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIMIT_in_limit535 = new BitSet(new long[]{0x00000000000A0000L});
    public static final BitSet FOLLOW_WS_in_limit538 = new BitSet(new long[]{0x00000000000A0000L});
    public static final BitSet FOLLOW_INT_in_limit541 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_WS_in_limit543 = new BitSet(new long[]{0x000000FFF4038040L});
    public static final BitSet FOLLOW_primary_in_limit546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_value556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_value560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_value564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_value568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_value572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_value576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_t585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_f592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nil0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_field611 = new BitSet(new long[]{0x0000000000000002L});

}