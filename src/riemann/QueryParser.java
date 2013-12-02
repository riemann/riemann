// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/riemann/Query.g 2013-12-02 00:16:45
package riemann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class QueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "OR", "NOT", "APPROXIMATELY", "REGEX_MATCH", "NOT_EQUAL", "EQUAL", "LESSER", "LESSER_EQUAL", "GREATER", "GREATER_EQUAL", "TAGGED", "WS", "String", "INT", "FLOAT", "ID", "EXPONENT", "EscapeSequence", "UnicodeEscape", "HexDigit", "'('", "')'", "'true'", "'false'", "'null'", "'nil'", "'host'", "'service'", "'state'", "'description'", "'metric_f'", "'metric'", "'ttl'", "'time'"
    };
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
    public static final int WS=16;
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
    public static final int UnicodeEscape=23;
    public static final int EQUAL=10;
    public static final int OR=5;
    public static final int String=17;
    public static final int EscapeSequence=22;
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
    // src/riemann/Query.g:26:1: expr : ( or EOF ) -> or ;
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
            // src/riemann/Query.g:26:6: ( ( or EOF ) -> or )
            // src/riemann/Query.g:26:8: ( or EOF )
            {
            // src/riemann/Query.g:26:8: ( or EOF )
            // src/riemann/Query.g:26:9: or EOF
            {
            pushFollow(FOLLOW_or_in_expr145);
            or1=or();

            state._fsp--;

            stream_or.add(or1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_expr147);  
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
            // 26:17: -> or
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
    // src/riemann/Query.g:28:1: or : and ( ( WS )* OR ( WS )* and )* ;
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
            // src/riemann/Query.g:28:4: ( and ( ( WS )* OR ( WS )* and )* )
            // src/riemann/Query.g:28:6: and ( ( WS )* OR ( WS )* and )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_and_in_or160);
            and3=and();

            state._fsp--;

            adaptor.addChild(root_0, and3.getTree());
            // src/riemann/Query.g:28:10: ( ( WS )* OR ( WS )* and )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR||LA3_0==WS) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/riemann/Query.g:28:11: ( WS )* OR ( WS )* and
            	    {
            	    // src/riemann/Query.g:28:11: ( WS )*
            	    loop1:
            	    do {
            	        int alt1=2;
            	        int LA1_0 = input.LA(1);

            	        if ( (LA1_0==WS) ) {
            	            alt1=1;
            	        }


            	        switch (alt1) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:28:11: WS
            	    	    {
            	    	    WS4=(Token)match(input,WS,FOLLOW_WS_in_or163); 
            	    	    WS4_tree = (CommonTree)adaptor.create(WS4);
            	    	    adaptor.addChild(root_0, WS4_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop1;
            	        }
            	    } while (true);

            	    OR5=(Token)match(input,OR,FOLLOW_OR_in_or166); 
            	    OR5_tree = (CommonTree)adaptor.create(OR5);
            	    root_0 = (CommonTree)adaptor.becomeRoot(OR5_tree, root_0);

            	    // src/riemann/Query.g:28:19: ( WS )*
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( (LA2_0==WS) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:28:19: WS
            	    	    {
            	    	    WS6=(Token)match(input,WS,FOLLOW_WS_in_or169); 
            	    	    WS6_tree = (CommonTree)adaptor.create(WS6);
            	    	    adaptor.addChild(root_0, WS6_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop2;
            	        }
            	    } while (true);

            	    pushFollow(FOLLOW_and_in_or172);
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
    // src/riemann/Query.g:30:1: and : ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* ;
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
            // src/riemann/Query.g:30:5: ( ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* )
            // src/riemann/Query.g:30:7: ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:30:7: ( not | primary )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==NOT) ) {
                alt4=1;
            }
            else if ( (LA4_0==TAGGED||LA4_0==25||(LA4_0>=27 && LA4_0<=38)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // src/riemann/Query.g:30:8: not
                    {
                    pushFollow(FOLLOW_not_in_and183);
                    not8=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not8.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:30:14: primary
                    {
                    pushFollow(FOLLOW_primary_in_and187);
                    primary9=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary9.getTree());

                    }
                    break;

            }

            // src/riemann/Query.g:30:23: ( ( WS )* AND ( WS )* ( not | primary ) )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // src/riemann/Query.g:30:24: ( WS )* AND ( WS )* ( not | primary )
            	    {
            	    // src/riemann/Query.g:30:24: ( WS )*
            	    loop5:
            	    do {
            	        int alt5=2;
            	        int LA5_0 = input.LA(1);

            	        if ( (LA5_0==WS) ) {
            	            alt5=1;
            	        }


            	        switch (alt5) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:30:24: WS
            	    	    {
            	    	    WS10=(Token)match(input,WS,FOLLOW_WS_in_and191); 
            	    	    WS10_tree = (CommonTree)adaptor.create(WS10);
            	    	    adaptor.addChild(root_0, WS10_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop5;
            	        }
            	    } while (true);

            	    AND11=(Token)match(input,AND,FOLLOW_AND_in_and194); 
            	    AND11_tree = (CommonTree)adaptor.create(AND11);
            	    root_0 = (CommonTree)adaptor.becomeRoot(AND11_tree, root_0);

            	    // src/riemann/Query.g:30:33: ( WS )*
            	    loop6:
            	    do {
            	        int alt6=2;
            	        int LA6_0 = input.LA(1);

            	        if ( (LA6_0==WS) ) {
            	            alt6=1;
            	        }


            	        switch (alt6) {
            	    	case 1 :
            	    	    // src/riemann/Query.g:30:33: WS
            	    	    {
            	    	    WS12=(Token)match(input,WS,FOLLOW_WS_in_and197); 
            	    	    WS12_tree = (CommonTree)adaptor.create(WS12);
            	    	    adaptor.addChild(root_0, WS12_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop6;
            	        }
            	    } while (true);

            	    // src/riemann/Query.g:30:37: ( not | primary )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==NOT) ) {
            	        alt7=1;
            	    }
            	    else if ( (LA7_0==TAGGED||LA7_0==25||(LA7_0>=27 && LA7_0<=38)) ) {
            	        alt7=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // src/riemann/Query.g:30:38: not
            	            {
            	            pushFollow(FOLLOW_not_in_and201);
            	            not13=not();

            	            state._fsp--;

            	            adaptor.addChild(root_0, not13.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // src/riemann/Query.g:30:44: primary
            	            {
            	            pushFollow(FOLLOW_primary_in_and205);
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
    // src/riemann/Query.g:32:1: not : NOT ( WS )* ( not | primary ) ;
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
            // src/riemann/Query.g:32:5: ( NOT ( WS )* ( not | primary ) )
            // src/riemann/Query.g:32:7: NOT ( WS )* ( not | primary )
            {
            root_0 = (CommonTree)adaptor.nil();

            NOT15=(Token)match(input,NOT,FOLLOW_NOT_in_not216); 
            NOT15_tree = (CommonTree)adaptor.create(NOT15);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT15_tree, root_0);

            // src/riemann/Query.g:32:12: ( WS )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==WS) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // src/riemann/Query.g:32:12: WS
            	    {
            	    WS16=(Token)match(input,WS,FOLLOW_WS_in_not219); 
            	    WS16_tree = (CommonTree)adaptor.create(WS16);
            	    adaptor.addChild(root_0, WS16_tree);


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // src/riemann/Query.g:32:16: ( not | primary )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==NOT) ) {
                alt10=1;
            }
            else if ( (LA10_0==TAGGED||LA10_0==25||(LA10_0>=27 && LA10_0<=38)) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // src/riemann/Query.g:32:17: not
                    {
                    pushFollow(FOLLOW_not_in_not223);
                    not17=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not17.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:32:23: primary
                    {
                    pushFollow(FOLLOW_primary_in_not227);
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
    // src/riemann/Query.g:35:1: primary : ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) ;
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
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
        RewriteRuleTokenStream stream_26=new RewriteRuleTokenStream(adaptor,"token 26");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_simple=new RewriteRuleSubtreeStream(adaptor,"rule simple");
        try {
            // src/riemann/Query.g:35:9: ( ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) )
            // src/riemann/Query.g:35:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            {
            // src/riemann/Query.g:35:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==25) ) {
                alt11=1;
            }
            else if ( (LA11_0==TAGGED||(LA11_0>=27 && LA11_0<=38)) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // src/riemann/Query.g:36:4: ( '(' or ')' )
                    {
                    // src/riemann/Query.g:36:4: ( '(' or ')' )
                    // src/riemann/Query.g:36:5: '(' or ')'
                    {
                    char_literal19=(Token)match(input,25,FOLLOW_25_in_primary244);  
                    stream_25.add(char_literal19);

                    pushFollow(FOLLOW_or_in_primary246);
                    or20=or();

                    state._fsp--;

                    stream_or.add(or20.getTree());
                    char_literal21=(Token)match(input,26,FOLLOW_26_in_primary248);  
                    stream_26.add(char_literal21);


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
                    // 36:17: -> ^( or )
                    {
                        // src/riemann/Query.g:36:20: ^( or )
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
                    // src/riemann/Query.g:37:6: simple
                    {
                    pushFollow(FOLLOW_simple_in_primary262);
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
                    // 37:13: -> simple
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
    // src/riemann/Query.g:40:1: fragment simple : ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) ;
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



        try {
            // src/riemann/Query.g:41:8: ( ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) )
            // src/riemann/Query.g:41:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:41:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            int alt12=12;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // src/riemann/Query.g:41:12: t
                    {
                    pushFollow(FOLLOW_t_in_simple282);
                    t23=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t23.getTree());

                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:41:16: f
                    {
                    pushFollow(FOLLOW_f_in_simple286);
                    f24=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f24.getTree());

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:41:20: nil
                    {
                    pushFollow(FOLLOW_nil_in_simple290);
                    nil25=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil25.getTree());

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:42:5: tagged
                    {
                    pushFollow(FOLLOW_tagged_in_simple296);
                    tagged26=tagged();

                    state._fsp--;

                    adaptor.addChild(root_0, tagged26.getTree());

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:43:5: approximately
                    {
                    pushFollow(FOLLOW_approximately_in_simple302);
                    approximately27=approximately();

                    state._fsp--;

                    adaptor.addChild(root_0, approximately27.getTree());

                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:44:5: regex_match
                    {
                    pushFollow(FOLLOW_regex_match_in_simple308);
                    regex_match28=regex_match();

                    state._fsp--;

                    adaptor.addChild(root_0, regex_match28.getTree());

                    }
                    break;
                case 7 :
                    // src/riemann/Query.g:45:5: lesser
                    {
                    pushFollow(FOLLOW_lesser_in_simple314);
                    lesser29=lesser();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser29.getTree());

                    }
                    break;
                case 8 :
                    // src/riemann/Query.g:46:5: lesser_equal
                    {
                    pushFollow(FOLLOW_lesser_equal_in_simple320);
                    lesser_equal30=lesser_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser_equal30.getTree());

                    }
                    break;
                case 9 :
                    // src/riemann/Query.g:47:5: greater
                    {
                    pushFollow(FOLLOW_greater_in_simple326);
                    greater31=greater();

                    state._fsp--;

                    adaptor.addChild(root_0, greater31.getTree());

                    }
                    break;
                case 10 :
                    // src/riemann/Query.g:48:5: greater_equal
                    {
                    pushFollow(FOLLOW_greater_equal_in_simple332);
                    greater_equal32=greater_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, greater_equal32.getTree());

                    }
                    break;
                case 11 :
                    // src/riemann/Query.g:49:5: not_equal
                    {
                    pushFollow(FOLLOW_not_equal_in_simple338);
                    not_equal33=not_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, not_equal33.getTree());

                    }
                    break;
                case 12 :
                    // src/riemann/Query.g:50:5: equal
                    {
                    pushFollow(FOLLOW_equal_in_simple344);
                    equal34=equal();

                    state._fsp--;

                    adaptor.addChild(root_0, equal34.getTree());

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
    // src/riemann/Query.g:53:1: approximately : field ( WS )* APPROXIMATELY ( WS )* value ;
    public final QueryParser.approximately_return approximately() throws RecognitionException {
        QueryParser.approximately_return retval = new QueryParser.approximately_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS36=null;
        Token APPROXIMATELY37=null;
        Token WS38=null;
        QueryParser.field_return field35 = null;

        QueryParser.value_return value39 = null;


        CommonTree WS36_tree=null;
        CommonTree APPROXIMATELY37_tree=null;
        CommonTree WS38_tree=null;

        try {
            // src/riemann/Query.g:54:2: ( field ( WS )* APPROXIMATELY ( WS )* value )
            // src/riemann/Query.g:54:4: field ( WS )* APPROXIMATELY ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_approximately357);
            field35=field();

            state._fsp--;

            adaptor.addChild(root_0, field35.getTree());
            // src/riemann/Query.g:54:10: ( WS )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==WS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // src/riemann/Query.g:54:10: WS
            	    {
            	    WS36=(Token)match(input,WS,FOLLOW_WS_in_approximately359); 
            	    WS36_tree = (CommonTree)adaptor.create(WS36);
            	    adaptor.addChild(root_0, WS36_tree);


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            APPROXIMATELY37=(Token)match(input,APPROXIMATELY,FOLLOW_APPROXIMATELY_in_approximately362); 
            APPROXIMATELY37_tree = (CommonTree)adaptor.create(APPROXIMATELY37);
            root_0 = (CommonTree)adaptor.becomeRoot(APPROXIMATELY37_tree, root_0);

            // src/riemann/Query.g:54:29: ( WS )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==WS) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // src/riemann/Query.g:54:29: WS
            	    {
            	    WS38=(Token)match(input,WS,FOLLOW_WS_in_approximately365); 
            	    WS38_tree = (CommonTree)adaptor.create(WS38);
            	    adaptor.addChild(root_0, WS38_tree);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_approximately368);
            value39=value();

            state._fsp--;

            adaptor.addChild(root_0, value39.getTree());

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
    // src/riemann/Query.g:55:1: regex_match : field ( WS )* REGEX_MATCH ( WS )* value ;
    public final QueryParser.regex_match_return regex_match() throws RecognitionException {
        QueryParser.regex_match_return retval = new QueryParser.regex_match_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS41=null;
        Token REGEX_MATCH42=null;
        Token WS43=null;
        QueryParser.field_return field40 = null;

        QueryParser.value_return value44 = null;


        CommonTree WS41_tree=null;
        CommonTree REGEX_MATCH42_tree=null;
        CommonTree WS43_tree=null;

        try {
            // src/riemann/Query.g:56:2: ( field ( WS )* REGEX_MATCH ( WS )* value )
            // src/riemann/Query.g:56:4: field ( WS )* REGEX_MATCH ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_regex_match376);
            field40=field();

            state._fsp--;

            adaptor.addChild(root_0, field40.getTree());
            // src/riemann/Query.g:56:10: ( WS )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==WS) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // src/riemann/Query.g:56:10: WS
            	    {
            	    WS41=(Token)match(input,WS,FOLLOW_WS_in_regex_match378); 
            	    WS41_tree = (CommonTree)adaptor.create(WS41);
            	    adaptor.addChild(root_0, WS41_tree);


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            REGEX_MATCH42=(Token)match(input,REGEX_MATCH,FOLLOW_REGEX_MATCH_in_regex_match381); 
            REGEX_MATCH42_tree = (CommonTree)adaptor.create(REGEX_MATCH42);
            root_0 = (CommonTree)adaptor.becomeRoot(REGEX_MATCH42_tree, root_0);

            // src/riemann/Query.g:56:27: ( WS )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==WS) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // src/riemann/Query.g:56:27: WS
            	    {
            	    WS43=(Token)match(input,WS,FOLLOW_WS_in_regex_match384); 
            	    WS43_tree = (CommonTree)adaptor.create(WS43);
            	    adaptor.addChild(root_0, WS43_tree);


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_regex_match387);
            value44=value();

            state._fsp--;

            adaptor.addChild(root_0, value44.getTree());

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
    // src/riemann/Query.g:57:1: lesser : field ( WS )* LESSER ( WS )* value ;
    public final QueryParser.lesser_return lesser() throws RecognitionException {
        QueryParser.lesser_return retval = new QueryParser.lesser_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS46=null;
        Token LESSER47=null;
        Token WS48=null;
        QueryParser.field_return field45 = null;

        QueryParser.value_return value49 = null;


        CommonTree WS46_tree=null;
        CommonTree LESSER47_tree=null;
        CommonTree WS48_tree=null;

        try {
            // src/riemann/Query.g:57:8: ( field ( WS )* LESSER ( WS )* value )
            // src/riemann/Query.g:57:10: field ( WS )* LESSER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser394);
            field45=field();

            state._fsp--;

            adaptor.addChild(root_0, field45.getTree());
            // src/riemann/Query.g:57:16: ( WS )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==WS) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src/riemann/Query.g:57:16: WS
            	    {
            	    WS46=(Token)match(input,WS,FOLLOW_WS_in_lesser396); 
            	    WS46_tree = (CommonTree)adaptor.create(WS46);
            	    adaptor.addChild(root_0, WS46_tree);


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            LESSER47=(Token)match(input,LESSER,FOLLOW_LESSER_in_lesser399); 
            LESSER47_tree = (CommonTree)adaptor.create(LESSER47);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER47_tree, root_0);

            // src/riemann/Query.g:57:28: ( WS )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==WS) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src/riemann/Query.g:57:28: WS
            	    {
            	    WS48=(Token)match(input,WS,FOLLOW_WS_in_lesser402); 
            	    WS48_tree = (CommonTree)adaptor.create(WS48);
            	    adaptor.addChild(root_0, WS48_tree);


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser405);
            value49=value();

            state._fsp--;

            adaptor.addChild(root_0, value49.getTree());

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
    // src/riemann/Query.g:58:1: lesser_equal : field ( WS )* LESSER_EQUAL ( WS )* value ;
    public final QueryParser.lesser_equal_return lesser_equal() throws RecognitionException {
        QueryParser.lesser_equal_return retval = new QueryParser.lesser_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS51=null;
        Token LESSER_EQUAL52=null;
        Token WS53=null;
        QueryParser.field_return field50 = null;

        QueryParser.value_return value54 = null;


        CommonTree WS51_tree=null;
        CommonTree LESSER_EQUAL52_tree=null;
        CommonTree WS53_tree=null;

        try {
            // src/riemann/Query.g:59:2: ( field ( WS )* LESSER_EQUAL ( WS )* value )
            // src/riemann/Query.g:59:4: field ( WS )* LESSER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser_equal413);
            field50=field();

            state._fsp--;

            adaptor.addChild(root_0, field50.getTree());
            // src/riemann/Query.g:59:10: ( WS )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==WS) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // src/riemann/Query.g:59:10: WS
            	    {
            	    WS51=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal415); 
            	    WS51_tree = (CommonTree)adaptor.create(WS51);
            	    adaptor.addChild(root_0, WS51_tree);


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            LESSER_EQUAL52=(Token)match(input,LESSER_EQUAL,FOLLOW_LESSER_EQUAL_in_lesser_equal418); 
            LESSER_EQUAL52_tree = (CommonTree)adaptor.create(LESSER_EQUAL52);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER_EQUAL52_tree, root_0);

            // src/riemann/Query.g:59:28: ( WS )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==WS) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // src/riemann/Query.g:59:28: WS
            	    {
            	    WS53=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal421); 
            	    WS53_tree = (CommonTree)adaptor.create(WS53);
            	    adaptor.addChild(root_0, WS53_tree);


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser_equal424);
            value54=value();

            state._fsp--;

            adaptor.addChild(root_0, value54.getTree());

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
    // src/riemann/Query.g:60:1: greater : field ( WS )* GREATER ( WS )* value ;
    public final QueryParser.greater_return greater() throws RecognitionException {
        QueryParser.greater_return retval = new QueryParser.greater_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS56=null;
        Token GREATER57=null;
        Token WS58=null;
        QueryParser.field_return field55 = null;

        QueryParser.value_return value59 = null;


        CommonTree WS56_tree=null;
        CommonTree GREATER57_tree=null;
        CommonTree WS58_tree=null;

        try {
            // src/riemann/Query.g:60:9: ( field ( WS )* GREATER ( WS )* value )
            // src/riemann/Query.g:60:11: field ( WS )* GREATER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater431);
            field55=field();

            state._fsp--;

            adaptor.addChild(root_0, field55.getTree());
            // src/riemann/Query.g:60:17: ( WS )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==WS) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // src/riemann/Query.g:60:17: WS
            	    {
            	    WS56=(Token)match(input,WS,FOLLOW_WS_in_greater433); 
            	    WS56_tree = (CommonTree)adaptor.create(WS56);
            	    adaptor.addChild(root_0, WS56_tree);


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            GREATER57=(Token)match(input,GREATER,FOLLOW_GREATER_in_greater436); 
            GREATER57_tree = (CommonTree)adaptor.create(GREATER57);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER57_tree, root_0);

            // src/riemann/Query.g:60:30: ( WS )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==WS) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // src/riemann/Query.g:60:30: WS
            	    {
            	    WS58=(Token)match(input,WS,FOLLOW_WS_in_greater439); 
            	    WS58_tree = (CommonTree)adaptor.create(WS58);
            	    adaptor.addChild(root_0, WS58_tree);


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater442);
            value59=value();

            state._fsp--;

            adaptor.addChild(root_0, value59.getTree());

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
    // src/riemann/Query.g:61:1: greater_equal : field ( WS )* GREATER_EQUAL ( WS )* value ;
    public final QueryParser.greater_equal_return greater_equal() throws RecognitionException {
        QueryParser.greater_equal_return retval = new QueryParser.greater_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS61=null;
        Token GREATER_EQUAL62=null;
        Token WS63=null;
        QueryParser.field_return field60 = null;

        QueryParser.value_return value64 = null;


        CommonTree WS61_tree=null;
        CommonTree GREATER_EQUAL62_tree=null;
        CommonTree WS63_tree=null;

        try {
            // src/riemann/Query.g:62:2: ( field ( WS )* GREATER_EQUAL ( WS )* value )
            // src/riemann/Query.g:62:4: field ( WS )* GREATER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater_equal450);
            field60=field();

            state._fsp--;

            adaptor.addChild(root_0, field60.getTree());
            // src/riemann/Query.g:62:10: ( WS )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==WS) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // src/riemann/Query.g:62:10: WS
            	    {
            	    WS61=(Token)match(input,WS,FOLLOW_WS_in_greater_equal452); 
            	    WS61_tree = (CommonTree)adaptor.create(WS61);
            	    adaptor.addChild(root_0, WS61_tree);


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            GREATER_EQUAL62=(Token)match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_greater_equal455); 
            GREATER_EQUAL62_tree = (CommonTree)adaptor.create(GREATER_EQUAL62);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER_EQUAL62_tree, root_0);

            // src/riemann/Query.g:62:29: ( WS )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==WS) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // src/riemann/Query.g:62:29: WS
            	    {
            	    WS63=(Token)match(input,WS,FOLLOW_WS_in_greater_equal458); 
            	    WS63_tree = (CommonTree)adaptor.create(WS63);
            	    adaptor.addChild(root_0, WS63_tree);


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater_equal461);
            value64=value();

            state._fsp--;

            adaptor.addChild(root_0, value64.getTree());

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
    // src/riemann/Query.g:63:1: not_equal : field ( WS )* NOT_EQUAL ( WS )* value ;
    public final QueryParser.not_equal_return not_equal() throws RecognitionException {
        QueryParser.not_equal_return retval = new QueryParser.not_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS66=null;
        Token NOT_EQUAL67=null;
        Token WS68=null;
        QueryParser.field_return field65 = null;

        QueryParser.value_return value69 = null;


        CommonTree WS66_tree=null;
        CommonTree NOT_EQUAL67_tree=null;
        CommonTree WS68_tree=null;

        try {
            // src/riemann/Query.g:64:2: ( field ( WS )* NOT_EQUAL ( WS )* value )
            // src/riemann/Query.g:64:4: field ( WS )* NOT_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_not_equal469);
            field65=field();

            state._fsp--;

            adaptor.addChild(root_0, field65.getTree());
            // src/riemann/Query.g:64:10: ( WS )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==WS) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // src/riemann/Query.g:64:10: WS
            	    {
            	    WS66=(Token)match(input,WS,FOLLOW_WS_in_not_equal471); 
            	    WS66_tree = (CommonTree)adaptor.create(WS66);
            	    adaptor.addChild(root_0, WS66_tree);


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            NOT_EQUAL67=(Token)match(input,NOT_EQUAL,FOLLOW_NOT_EQUAL_in_not_equal474); 
            NOT_EQUAL67_tree = (CommonTree)adaptor.create(NOT_EQUAL67);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT_EQUAL67_tree, root_0);

            // src/riemann/Query.g:64:25: ( WS )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==WS) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // src/riemann/Query.g:64:25: WS
            	    {
            	    WS68=(Token)match(input,WS,FOLLOW_WS_in_not_equal477); 
            	    WS68_tree = (CommonTree)adaptor.create(WS68);
            	    adaptor.addChild(root_0, WS68_tree);


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_not_equal480);
            value69=value();

            state._fsp--;

            adaptor.addChild(root_0, value69.getTree());

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
    // src/riemann/Query.g:65:1: equal : field ( WS )* EQUAL ( WS )* value ;
    public final QueryParser.equal_return equal() throws RecognitionException {
        QueryParser.equal_return retval = new QueryParser.equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS71=null;
        Token EQUAL72=null;
        Token WS73=null;
        QueryParser.field_return field70 = null;

        QueryParser.value_return value74 = null;


        CommonTree WS71_tree=null;
        CommonTree EQUAL72_tree=null;
        CommonTree WS73_tree=null;

        try {
            // src/riemann/Query.g:65:7: ( field ( WS )* EQUAL ( WS )* value )
            // src/riemann/Query.g:65:9: field ( WS )* EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_equal488);
            field70=field();

            state._fsp--;

            adaptor.addChild(root_0, field70.getTree());
            // src/riemann/Query.g:65:15: ( WS )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==WS) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // src/riemann/Query.g:65:15: WS
            	    {
            	    WS71=(Token)match(input,WS,FOLLOW_WS_in_equal490); 
            	    WS71_tree = (CommonTree)adaptor.create(WS71);
            	    adaptor.addChild(root_0, WS71_tree);


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);

            EQUAL72=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_equal493); 
            EQUAL72_tree = (CommonTree)adaptor.create(EQUAL72);
            root_0 = (CommonTree)adaptor.becomeRoot(EQUAL72_tree, root_0);

            // src/riemann/Query.g:65:26: ( WS )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==WS) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // src/riemann/Query.g:65:26: WS
            	    {
            	    WS73=(Token)match(input,WS,FOLLOW_WS_in_equal496); 
            	    WS73_tree = (CommonTree)adaptor.create(WS73);
            	    adaptor.addChild(root_0, WS73_tree);


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_equal499);
            value74=value();

            state._fsp--;

            adaptor.addChild(root_0, value74.getTree());

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
    // src/riemann/Query.g:67:1: tagged : TAGGED ( WS )* String ;
    public final QueryParser.tagged_return tagged() throws RecognitionException {
        QueryParser.tagged_return retval = new QueryParser.tagged_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TAGGED75=null;
        Token WS76=null;
        Token String77=null;

        CommonTree TAGGED75_tree=null;
        CommonTree WS76_tree=null;
        CommonTree String77_tree=null;

        try {
            // src/riemann/Query.g:67:8: ( TAGGED ( WS )* String )
            // src/riemann/Query.g:67:10: TAGGED ( WS )* String
            {
            root_0 = (CommonTree)adaptor.nil();

            TAGGED75=(Token)match(input,TAGGED,FOLLOW_TAGGED_in_tagged507); 
            TAGGED75_tree = (CommonTree)adaptor.create(TAGGED75);
            root_0 = (CommonTree)adaptor.becomeRoot(TAGGED75_tree, root_0);

            // src/riemann/Query.g:67:18: ( WS )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==WS) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // src/riemann/Query.g:67:18: WS
            	    {
            	    WS76=(Token)match(input,WS,FOLLOW_WS_in_tagged510); 
            	    WS76_tree = (CommonTree)adaptor.create(WS76);
            	    adaptor.addChild(root_0, WS76_tree);


            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);

            String77=(Token)match(input,String,FOLLOW_String_in_tagged513); 
            String77_tree = (CommonTree)adaptor.create(String77);
            adaptor.addChild(root_0, String77_tree);


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

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // src/riemann/Query.g:69:1: value : ( String | t | f | nil | INT | FLOAT ) ;
    public final QueryParser.value_return value() throws RecognitionException {
        QueryParser.value_return retval = new QueryParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token String78=null;
        Token INT82=null;
        Token FLOAT83=null;
        QueryParser.t_return t79 = null;

        QueryParser.f_return f80 = null;

        QueryParser.nil_return nil81 = null;


        CommonTree String78_tree=null;
        CommonTree INT82_tree=null;
        CommonTree FLOAT83_tree=null;

        try {
            // src/riemann/Query.g:69:7: ( ( String | t | f | nil | INT | FLOAT ) )
            // src/riemann/Query.g:69:10: ( String | t | f | nil | INT | FLOAT )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/riemann/Query.g:69:10: ( String | t | f | nil | INT | FLOAT )
            int alt30=6;
            switch ( input.LA(1) ) {
            case String:
                {
                alt30=1;
                }
                break;
            case 27:
                {
                alt30=2;
                }
                break;
            case 28:
                {
                alt30=3;
                }
                break;
            case 29:
            case 30:
                {
                alt30=4;
                }
                break;
            case INT:
                {
                alt30=5;
                }
                break;
            case FLOAT:
                {
                alt30=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // src/riemann/Query.g:69:11: String
                    {
                    String78=(Token)match(input,String,FOLLOW_String_in_value523); 
                    String78_tree = (CommonTree)adaptor.create(String78);
                    adaptor.addChild(root_0, String78_tree);


                    }
                    break;
                case 2 :
                    // src/riemann/Query.g:69:20: t
                    {
                    pushFollow(FOLLOW_t_in_value527);
                    t79=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t79.getTree());

                    }
                    break;
                case 3 :
                    // src/riemann/Query.g:69:24: f
                    {
                    pushFollow(FOLLOW_f_in_value531);
                    f80=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f80.getTree());

                    }
                    break;
                case 4 :
                    // src/riemann/Query.g:69:28: nil
                    {
                    pushFollow(FOLLOW_nil_in_value535);
                    nil81=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil81.getTree());

                    }
                    break;
                case 5 :
                    // src/riemann/Query.g:69:34: INT
                    {
                    INT82=(Token)match(input,INT,FOLLOW_INT_in_value539); 
                    INT82_tree = (CommonTree)adaptor.create(INT82);
                    adaptor.addChild(root_0, INT82_tree);


                    }
                    break;
                case 6 :
                    // src/riemann/Query.g:69:40: FLOAT
                    {
                    FLOAT83=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_value543); 
                    FLOAT83_tree = (CommonTree)adaptor.create(FLOAT83);
                    adaptor.addChild(root_0, FLOAT83_tree);


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
    // src/riemann/Query.g:71:1: t : 'true' ;
    public final QueryParser.t_return t() throws RecognitionException {
        QueryParser.t_return retval = new QueryParser.t_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal84=null;

        CommonTree string_literal84_tree=null;

        try {
            // src/riemann/Query.g:71:3: ( 'true' )
            // src/riemann/Query.g:71:5: 'true'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal84=(Token)match(input,27,FOLLOW_27_in_t552); 
            string_literal84_tree = (CommonTree)adaptor.create(string_literal84);
            adaptor.addChild(root_0, string_literal84_tree);


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
    // src/riemann/Query.g:72:1: f : 'false' ;
    public final QueryParser.f_return f() throws RecognitionException {
        QueryParser.f_return retval = new QueryParser.f_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal85=null;

        CommonTree string_literal85_tree=null;

        try {
            // src/riemann/Query.g:72:3: ( 'false' )
            // src/riemann/Query.g:72:5: 'false'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal85=(Token)match(input,28,FOLLOW_28_in_f559); 
            string_literal85_tree = (CommonTree)adaptor.create(string_literal85);
            adaptor.addChild(root_0, string_literal85_tree);


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
    // src/riemann/Query.g:73:1: nil : ( 'null' | 'nil' );
    public final QueryParser.nil_return nil() throws RecognitionException {
        QueryParser.nil_return retval = new QueryParser.nil_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set86=null;

        CommonTree set86_tree=null;

        try {
            // src/riemann/Query.g:73:5: ( 'null' | 'nil' )
            // src/riemann/Query.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set86=(Token)input.LT(1);
            if ( (input.LA(1)>=29 && input.LA(1)<=30) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set86));
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
    // src/riemann/Query.g:75:1: field : ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' ) ;
    public final QueryParser.field_return field() throws RecognitionException {
        QueryParser.field_return retval = new QueryParser.field_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set87=null;

        CommonTree set87_tree=null;

        try {
            // src/riemann/Query.g:75:7: ( ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' ) )
            // src/riemann/Query.g:75:9: ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'metric' | 'ttl' | 'time' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set87=(Token)input.LT(1);
            if ( (input.LA(1)>=31 && input.LA(1)<=38) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set87));
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
        "\1\32\1\20\2\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA8_specialS =
        "\4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\3\1\2\12\uffff\1\1\11\uffff\1\2",
            "\1\3\1\2\12\uffff\1\1",
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
            return "()* loopback of 30:23: ( ( WS )* AND ( WS )* ( not | primary ) )*";
        }
    }
    static final String DFA12_eotS =
        "\17\uffff";
    static final String DFA12_eofS =
        "\17\uffff";
    static final String DFA12_minS =
        "\1\17\4\uffff\2\7\10\uffff";
    static final String DFA12_maxS =
        "\1\46\4\uffff\2\20\10\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\2\uffff\1\11\1\6\1\13\1\12\1\10\1\14\1"+
        "\7\1\5";
    static final String DFA12_specialS =
        "\17\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\4\13\uffff\1\1\1\2\2\3\10\5",
            "",
            "",
            "",
            "",
            "\1\16\1\10\1\11\1\14\1\15\1\13\1\7\1\12\1\uffff\1\6",
            "\1\16\1\10\1\11\1\14\1\15\1\13\1\7\1\12\1\uffff\1\6",
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
            return "41:10: ( t | f | nil | tagged | approximately | regex_match | lesser | lesser_equal | greater | greater_equal | not_equal | equal )";
        }
    }
 

    public static final BitSet FOLLOW_or_in_expr145 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_expr147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_or160 = new BitSet(new long[]{0x0000000000010022L});
    public static final BitSet FOLLOW_WS_in_or163 = new BitSet(new long[]{0x0000000000010020L});
    public static final BitSet FOLLOW_OR_in_or166 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_WS_in_or169 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_and_in_or172 = new BitSet(new long[]{0x0000000000010022L});
    public static final BitSet FOLLOW_not_in_and183 = new BitSet(new long[]{0x0000000000010012L});
    public static final BitSet FOLLOW_primary_in_and187 = new BitSet(new long[]{0x0000000000010012L});
    public static final BitSet FOLLOW_WS_in_and191 = new BitSet(new long[]{0x0000000000010010L});
    public static final BitSet FOLLOW_AND_in_and194 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_WS_in_and197 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_not_in_and201 = new BitSet(new long[]{0x0000000000010012L});
    public static final BitSet FOLLOW_primary_in_and205 = new BitSet(new long[]{0x0000000000010012L});
    public static final BitSet FOLLOW_NOT_in_not216 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_WS_in_not219 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_not_in_not223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_not227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_primary244 = new BitSet(new long[]{0x0000007FFA018040L});
    public static final BitSet FOLLOW_or_in_primary246 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_primary248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_in_primary262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_simple282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_simple286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_simple290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tagged_in_simple296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_approximately_in_simple302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_regex_match_in_simple308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_in_simple314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_equal_in_simple320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_in_simple326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_equal_in_simple332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equal_in_simple338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equal_in_simple344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_approximately357 = new BitSet(new long[]{0x0000000000010080L});
    public static final BitSet FOLLOW_WS_in_approximately359 = new BitSet(new long[]{0x0000000000010080L});
    public static final BitSet FOLLOW_APPROXIMATELY_in_approximately362 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_approximately365 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_approximately368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_regex_match376 = new BitSet(new long[]{0x0000000000010100L});
    public static final BitSet FOLLOW_WS_in_regex_match378 = new BitSet(new long[]{0x0000000000010100L});
    public static final BitSet FOLLOW_REGEX_MATCH_in_regex_match381 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_regex_match384 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_regex_match387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser394 = new BitSet(new long[]{0x0000000000010800L});
    public static final BitSet FOLLOW_WS_in_lesser396 = new BitSet(new long[]{0x0000000000010800L});
    public static final BitSet FOLLOW_LESSER_in_lesser399 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_lesser402 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_lesser405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser_equal413 = new BitSet(new long[]{0x0000000000011000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal415 = new BitSet(new long[]{0x0000000000011000L});
    public static final BitSet FOLLOW_LESSER_EQUAL_in_lesser_equal418 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal421 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_lesser_equal424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater431 = new BitSet(new long[]{0x0000000000012000L});
    public static final BitSet FOLLOW_WS_in_greater433 = new BitSet(new long[]{0x0000000000012000L});
    public static final BitSet FOLLOW_GREATER_in_greater436 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_greater439 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_greater442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater_equal450 = new BitSet(new long[]{0x0000000000014000L});
    public static final BitSet FOLLOW_WS_in_greater_equal452 = new BitSet(new long[]{0x0000000000014000L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_greater_equal455 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_greater_equal458 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_greater_equal461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_not_equal469 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_WS_in_not_equal471 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_NOT_EQUAL_in_not_equal474 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_not_equal477 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_not_equal480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_equal488 = new BitSet(new long[]{0x0000000000010400L});
    public static final BitSet FOLLOW_WS_in_equal490 = new BitSet(new long[]{0x0000000000010400L});
    public static final BitSet FOLLOW_EQUAL_in_equal493 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_WS_in_equal496 = new BitSet(new long[]{0x00000000780F0000L});
    public static final BitSet FOLLOW_value_in_equal499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TAGGED_in_tagged507 = new BitSet(new long[]{0x0000000000030000L});
    public static final BitSet FOLLOW_WS_in_tagged510 = new BitSet(new long[]{0x0000000000030000L});
    public static final BitSet FOLLOW_String_in_tagged513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_value523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_value527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_value531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_value535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_value539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_value543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_t552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_f559 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nil0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_field578 = new BitSet(new long[]{0x0000000000000002L});

}