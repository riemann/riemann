// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/reimann/Query.g 2012-02-12 01:00:05
package reimann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class QueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "OR", "NOT", "APPROXIMATELY", "NOT_EQUAL", "EQUAL", "LESSER", "LESSER_EQUAL", "GREATER", "GREATER_EQUAL", "TAGGED", "WS", "String", "INT", "FLOAT", "ID", "EXPONENT", "EscapeSequence", "UnicodeEscape", "HexDigit", "'('", "')'", "'true'", "'false'", "'null'", "'nil'", "'host'", "'service'", "'state'", "'description'", "'metric'", "'metric_f'", "'time'"
    };
    public static final int LESSER_EQUAL=11;
    public static final int EXPONENT=20;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int APPROXIMATELY=7;
    public static final int FLOAT=18;
    public static final int INT=17;
    public static final int NOT=6;
    public static final int ID=19;
    public static final int AND=4;
    public static final int EOF=-1;
    public static final int HexDigit=23;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=15;
    public static final int GREATER=12;
    public static final int LESSER=10;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int NOT_EQUAL=8;
    public static final int TAGGED=14;
    public static final int UnicodeEscape=22;
    public static final int EQUAL=9;
    public static final int OR=5;
    public static final int String=16;
    public static final int EscapeSequence=21;
    public static final int GREATER_EQUAL=13;

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
    public String getGrammarFileName() { return "src/reimann/Query.g"; }


    public static class expr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expr"
    // src/reimann/Query.g:25:1: expr : ( or EOF ) -> or ;
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
            // src/reimann/Query.g:25:6: ( ( or EOF ) -> or )
            // src/reimann/Query.g:25:8: ( or EOF )
            {
            // src/reimann/Query.g:25:8: ( or EOF )
            // src/reimann/Query.g:25:9: or EOF
            {
            pushFollow(FOLLOW_or_in_expr137);
            or1=or();

            state._fsp--;

            stream_or.add(or1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_expr139);  
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
            // 25:17: -> or
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
    // src/reimann/Query.g:27:1: or : and ( ( WS )* OR ( WS )* and )* ;
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
            // src/reimann/Query.g:27:4: ( and ( ( WS )* OR ( WS )* and )* )
            // src/reimann/Query.g:27:6: and ( ( WS )* OR ( WS )* and )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_and_in_or152);
            and3=and();

            state._fsp--;

            adaptor.addChild(root_0, and3.getTree());
            // src/reimann/Query.g:27:10: ( ( WS )* OR ( WS )* and )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR||LA3_0==WS) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/reimann/Query.g:27:11: ( WS )* OR ( WS )* and
            	    {
            	    // src/reimann/Query.g:27:11: ( WS )*
            	    loop1:
            	    do {
            	        int alt1=2;
            	        int LA1_0 = input.LA(1);

            	        if ( (LA1_0==WS) ) {
            	            alt1=1;
            	        }


            	        switch (alt1) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:27:11: WS
            	    	    {
            	    	    WS4=(Token)match(input,WS,FOLLOW_WS_in_or155); 
            	    	    WS4_tree = (CommonTree)adaptor.create(WS4);
            	    	    adaptor.addChild(root_0, WS4_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop1;
            	        }
            	    } while (true);

            	    OR5=(Token)match(input,OR,FOLLOW_OR_in_or158); 
            	    OR5_tree = (CommonTree)adaptor.create(OR5);
            	    root_0 = (CommonTree)adaptor.becomeRoot(OR5_tree, root_0);

            	    // src/reimann/Query.g:27:19: ( WS )*
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( (LA2_0==WS) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:27:19: WS
            	    	    {
            	    	    WS6=(Token)match(input,WS,FOLLOW_WS_in_or161); 
            	    	    WS6_tree = (CommonTree)adaptor.create(WS6);
            	    	    adaptor.addChild(root_0, WS6_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop2;
            	        }
            	    } while (true);

            	    pushFollow(FOLLOW_and_in_or164);
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
    // src/reimann/Query.g:29:1: and : ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* ;
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
            // src/reimann/Query.g:29:5: ( ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* )
            // src/reimann/Query.g:29:7: ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:29:7: ( not | primary )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==NOT) ) {
                alt4=1;
            }
            else if ( (LA4_0==TAGGED||LA4_0==24||(LA4_0>=26 && LA4_0<=36)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // src/reimann/Query.g:29:8: not
                    {
                    pushFollow(FOLLOW_not_in_and175);
                    not8=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not8.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:29:14: primary
                    {
                    pushFollow(FOLLOW_primary_in_and179);
                    primary9=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary9.getTree());

                    }
                    break;

            }

            // src/reimann/Query.g:29:23: ( ( WS )* AND ( WS )* ( not | primary ) )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // src/reimann/Query.g:29:24: ( WS )* AND ( WS )* ( not | primary )
            	    {
            	    // src/reimann/Query.g:29:24: ( WS )*
            	    loop5:
            	    do {
            	        int alt5=2;
            	        int LA5_0 = input.LA(1);

            	        if ( (LA5_0==WS) ) {
            	            alt5=1;
            	        }


            	        switch (alt5) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:29:24: WS
            	    	    {
            	    	    WS10=(Token)match(input,WS,FOLLOW_WS_in_and183); 
            	    	    WS10_tree = (CommonTree)adaptor.create(WS10);
            	    	    adaptor.addChild(root_0, WS10_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop5;
            	        }
            	    } while (true);

            	    AND11=(Token)match(input,AND,FOLLOW_AND_in_and186); 
            	    AND11_tree = (CommonTree)adaptor.create(AND11);
            	    root_0 = (CommonTree)adaptor.becomeRoot(AND11_tree, root_0);

            	    // src/reimann/Query.g:29:33: ( WS )*
            	    loop6:
            	    do {
            	        int alt6=2;
            	        int LA6_0 = input.LA(1);

            	        if ( (LA6_0==WS) ) {
            	            alt6=1;
            	        }


            	        switch (alt6) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:29:33: WS
            	    	    {
            	    	    WS12=(Token)match(input,WS,FOLLOW_WS_in_and189); 
            	    	    WS12_tree = (CommonTree)adaptor.create(WS12);
            	    	    adaptor.addChild(root_0, WS12_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop6;
            	        }
            	    } while (true);

            	    // src/reimann/Query.g:29:37: ( not | primary )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==NOT) ) {
            	        alt7=1;
            	    }
            	    else if ( (LA7_0==TAGGED||LA7_0==24||(LA7_0>=26 && LA7_0<=36)) ) {
            	        alt7=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // src/reimann/Query.g:29:38: not
            	            {
            	            pushFollow(FOLLOW_not_in_and193);
            	            not13=not();

            	            state._fsp--;

            	            adaptor.addChild(root_0, not13.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // src/reimann/Query.g:29:44: primary
            	            {
            	            pushFollow(FOLLOW_primary_in_and197);
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
    // src/reimann/Query.g:31:1: not : NOT ( WS )* ( not | primary ) ;
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
            // src/reimann/Query.g:31:5: ( NOT ( WS )* ( not | primary ) )
            // src/reimann/Query.g:31:7: NOT ( WS )* ( not | primary )
            {
            root_0 = (CommonTree)adaptor.nil();

            NOT15=(Token)match(input,NOT,FOLLOW_NOT_in_not208); 
            NOT15_tree = (CommonTree)adaptor.create(NOT15);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT15_tree, root_0);

            // src/reimann/Query.g:31:12: ( WS )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==WS) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // src/reimann/Query.g:31:12: WS
            	    {
            	    WS16=(Token)match(input,WS,FOLLOW_WS_in_not211); 
            	    WS16_tree = (CommonTree)adaptor.create(WS16);
            	    adaptor.addChild(root_0, WS16_tree);


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // src/reimann/Query.g:31:16: ( not | primary )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==NOT) ) {
                alt10=1;
            }
            else if ( (LA10_0==TAGGED||LA10_0==24||(LA10_0>=26 && LA10_0<=36)) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // src/reimann/Query.g:31:17: not
                    {
                    pushFollow(FOLLOW_not_in_not215);
                    not17=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not17.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:31:23: primary
                    {
                    pushFollow(FOLLOW_primary_in_not219);
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
    // src/reimann/Query.g:34:1: primary : ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) ;
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
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_simple=new RewriteRuleSubtreeStream(adaptor,"rule simple");
        try {
            // src/reimann/Query.g:34:9: ( ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) )
            // src/reimann/Query.g:34:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            {
            // src/reimann/Query.g:34:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==24) ) {
                alt11=1;
            }
            else if ( (LA11_0==TAGGED||(LA11_0>=26 && LA11_0<=36)) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // src/reimann/Query.g:35:4: ( '(' or ')' )
                    {
                    // src/reimann/Query.g:35:4: ( '(' or ')' )
                    // src/reimann/Query.g:35:5: '(' or ')'
                    {
                    char_literal19=(Token)match(input,24,FOLLOW_24_in_primary236);  
                    stream_24.add(char_literal19);

                    pushFollow(FOLLOW_or_in_primary238);
                    or20=or();

                    state._fsp--;

                    stream_or.add(or20.getTree());
                    char_literal21=(Token)match(input,25,FOLLOW_25_in_primary240);  
                    stream_25.add(char_literal21);


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
                    // 35:17: -> ^( or )
                    {
                        // src/reimann/Query.g:35:20: ^( or )
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
                    // src/reimann/Query.g:36:6: simple
                    {
                    pushFollow(FOLLOW_simple_in_primary254);
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
                    // 36:13: -> simple
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
    // src/reimann/Query.g:39:1: fragment simple : ( t | f | nil | tagged | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) ;
    public final QueryParser.simple_return simple() throws RecognitionException {
        QueryParser.simple_return retval = new QueryParser.simple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        QueryParser.t_return t23 = null;

        QueryParser.f_return f24 = null;

        QueryParser.nil_return nil25 = null;

        QueryParser.tagged_return tagged26 = null;

        QueryParser.approximately_return approximately27 = null;

        QueryParser.lesser_return lesser28 = null;

        QueryParser.lesser_equal_return lesser_equal29 = null;

        QueryParser.greater_return greater30 = null;

        QueryParser.greater_equal_return greater_equal31 = null;

        QueryParser.not_equal_return not_equal32 = null;

        QueryParser.equal_return equal33 = null;



        try {
            // src/reimann/Query.g:40:8: ( ( t | f | nil | tagged | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) )
            // src/reimann/Query.g:40:10: ( t | f | nil | tagged | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:40:10: ( t | f | nil | tagged | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            int alt12=11;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // src/reimann/Query.g:40:12: t
                    {
                    pushFollow(FOLLOW_t_in_simple274);
                    t23=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t23.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:40:16: f
                    {
                    pushFollow(FOLLOW_f_in_simple278);
                    f24=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f24.getTree());

                    }
                    break;
                case 3 :
                    // src/reimann/Query.g:40:20: nil
                    {
                    pushFollow(FOLLOW_nil_in_simple282);
                    nil25=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil25.getTree());

                    }
                    break;
                case 4 :
                    // src/reimann/Query.g:41:5: tagged
                    {
                    pushFollow(FOLLOW_tagged_in_simple288);
                    tagged26=tagged();

                    state._fsp--;

                    adaptor.addChild(root_0, tagged26.getTree());

                    }
                    break;
                case 5 :
                    // src/reimann/Query.g:42:5: approximately
                    {
                    pushFollow(FOLLOW_approximately_in_simple294);
                    approximately27=approximately();

                    state._fsp--;

                    adaptor.addChild(root_0, approximately27.getTree());

                    }
                    break;
                case 6 :
                    // src/reimann/Query.g:43:5: lesser
                    {
                    pushFollow(FOLLOW_lesser_in_simple300);
                    lesser28=lesser();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser28.getTree());

                    }
                    break;
                case 7 :
                    // src/reimann/Query.g:44:5: lesser_equal
                    {
                    pushFollow(FOLLOW_lesser_equal_in_simple306);
                    lesser_equal29=lesser_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser_equal29.getTree());

                    }
                    break;
                case 8 :
                    // src/reimann/Query.g:45:5: greater
                    {
                    pushFollow(FOLLOW_greater_in_simple312);
                    greater30=greater();

                    state._fsp--;

                    adaptor.addChild(root_0, greater30.getTree());

                    }
                    break;
                case 9 :
                    // src/reimann/Query.g:46:5: greater_equal
                    {
                    pushFollow(FOLLOW_greater_equal_in_simple318);
                    greater_equal31=greater_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, greater_equal31.getTree());

                    }
                    break;
                case 10 :
                    // src/reimann/Query.g:47:5: not_equal
                    {
                    pushFollow(FOLLOW_not_equal_in_simple324);
                    not_equal32=not_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, not_equal32.getTree());

                    }
                    break;
                case 11 :
                    // src/reimann/Query.g:48:5: equal
                    {
                    pushFollow(FOLLOW_equal_in_simple330);
                    equal33=equal();

                    state._fsp--;

                    adaptor.addChild(root_0, equal33.getTree());

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
    // src/reimann/Query.g:51:1: approximately : field ( WS )* APPROXIMATELY ( WS )* value ;
    public final QueryParser.approximately_return approximately() throws RecognitionException {
        QueryParser.approximately_return retval = new QueryParser.approximately_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS35=null;
        Token APPROXIMATELY36=null;
        Token WS37=null;
        QueryParser.field_return field34 = null;

        QueryParser.value_return value38 = null;


        CommonTree WS35_tree=null;
        CommonTree APPROXIMATELY36_tree=null;
        CommonTree WS37_tree=null;

        try {
            // src/reimann/Query.g:52:2: ( field ( WS )* APPROXIMATELY ( WS )* value )
            // src/reimann/Query.g:52:4: field ( WS )* APPROXIMATELY ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_approximately343);
            field34=field();

            state._fsp--;

            adaptor.addChild(root_0, field34.getTree());
            // src/reimann/Query.g:52:10: ( WS )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==WS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // src/reimann/Query.g:52:10: WS
            	    {
            	    WS35=(Token)match(input,WS,FOLLOW_WS_in_approximately345); 
            	    WS35_tree = (CommonTree)adaptor.create(WS35);
            	    adaptor.addChild(root_0, WS35_tree);


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            APPROXIMATELY36=(Token)match(input,APPROXIMATELY,FOLLOW_APPROXIMATELY_in_approximately348); 
            APPROXIMATELY36_tree = (CommonTree)adaptor.create(APPROXIMATELY36);
            root_0 = (CommonTree)adaptor.becomeRoot(APPROXIMATELY36_tree, root_0);

            // src/reimann/Query.g:52:29: ( WS )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==WS) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // src/reimann/Query.g:52:29: WS
            	    {
            	    WS37=(Token)match(input,WS,FOLLOW_WS_in_approximately351); 
            	    WS37_tree = (CommonTree)adaptor.create(WS37);
            	    adaptor.addChild(root_0, WS37_tree);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_approximately354);
            value38=value();

            state._fsp--;

            adaptor.addChild(root_0, value38.getTree());

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

    public static class lesser_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lesser"
    // src/reimann/Query.g:53:1: lesser : field ( WS )* LESSER ( WS )* value ;
    public final QueryParser.lesser_return lesser() throws RecognitionException {
        QueryParser.lesser_return retval = new QueryParser.lesser_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS40=null;
        Token LESSER41=null;
        Token WS42=null;
        QueryParser.field_return field39 = null;

        QueryParser.value_return value43 = null;


        CommonTree WS40_tree=null;
        CommonTree LESSER41_tree=null;
        CommonTree WS42_tree=null;

        try {
            // src/reimann/Query.g:53:8: ( field ( WS )* LESSER ( WS )* value )
            // src/reimann/Query.g:53:10: field ( WS )* LESSER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser361);
            field39=field();

            state._fsp--;

            adaptor.addChild(root_0, field39.getTree());
            // src/reimann/Query.g:53:16: ( WS )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==WS) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // src/reimann/Query.g:53:16: WS
            	    {
            	    WS40=(Token)match(input,WS,FOLLOW_WS_in_lesser363); 
            	    WS40_tree = (CommonTree)adaptor.create(WS40);
            	    adaptor.addChild(root_0, WS40_tree);


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            LESSER41=(Token)match(input,LESSER,FOLLOW_LESSER_in_lesser366); 
            LESSER41_tree = (CommonTree)adaptor.create(LESSER41);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER41_tree, root_0);

            // src/reimann/Query.g:53:28: ( WS )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==WS) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // src/reimann/Query.g:53:28: WS
            	    {
            	    WS42=(Token)match(input,WS,FOLLOW_WS_in_lesser369); 
            	    WS42_tree = (CommonTree)adaptor.create(WS42);
            	    adaptor.addChild(root_0, WS42_tree);


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser372);
            value43=value();

            state._fsp--;

            adaptor.addChild(root_0, value43.getTree());

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
    // src/reimann/Query.g:54:1: lesser_equal : field ( WS )* LESSER_EQUAL ( WS )* value ;
    public final QueryParser.lesser_equal_return lesser_equal() throws RecognitionException {
        QueryParser.lesser_equal_return retval = new QueryParser.lesser_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS45=null;
        Token LESSER_EQUAL46=null;
        Token WS47=null;
        QueryParser.field_return field44 = null;

        QueryParser.value_return value48 = null;


        CommonTree WS45_tree=null;
        CommonTree LESSER_EQUAL46_tree=null;
        CommonTree WS47_tree=null;

        try {
            // src/reimann/Query.g:55:2: ( field ( WS )* LESSER_EQUAL ( WS )* value )
            // src/reimann/Query.g:55:4: field ( WS )* LESSER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser_equal380);
            field44=field();

            state._fsp--;

            adaptor.addChild(root_0, field44.getTree());
            // src/reimann/Query.g:55:10: ( WS )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==WS) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src/reimann/Query.g:55:10: WS
            	    {
            	    WS45=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal382); 
            	    WS45_tree = (CommonTree)adaptor.create(WS45);
            	    adaptor.addChild(root_0, WS45_tree);


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            LESSER_EQUAL46=(Token)match(input,LESSER_EQUAL,FOLLOW_LESSER_EQUAL_in_lesser_equal385); 
            LESSER_EQUAL46_tree = (CommonTree)adaptor.create(LESSER_EQUAL46);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER_EQUAL46_tree, root_0);

            // src/reimann/Query.g:55:28: ( WS )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==WS) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src/reimann/Query.g:55:28: WS
            	    {
            	    WS47=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal388); 
            	    WS47_tree = (CommonTree)adaptor.create(WS47);
            	    adaptor.addChild(root_0, WS47_tree);


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser_equal391);
            value48=value();

            state._fsp--;

            adaptor.addChild(root_0, value48.getTree());

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
    // src/reimann/Query.g:56:1: greater : field ( WS )* GREATER ( WS )* value ;
    public final QueryParser.greater_return greater() throws RecognitionException {
        QueryParser.greater_return retval = new QueryParser.greater_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS50=null;
        Token GREATER51=null;
        Token WS52=null;
        QueryParser.field_return field49 = null;

        QueryParser.value_return value53 = null;


        CommonTree WS50_tree=null;
        CommonTree GREATER51_tree=null;
        CommonTree WS52_tree=null;

        try {
            // src/reimann/Query.g:56:9: ( field ( WS )* GREATER ( WS )* value )
            // src/reimann/Query.g:56:11: field ( WS )* GREATER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater398);
            field49=field();

            state._fsp--;

            adaptor.addChild(root_0, field49.getTree());
            // src/reimann/Query.g:56:17: ( WS )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==WS) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // src/reimann/Query.g:56:17: WS
            	    {
            	    WS50=(Token)match(input,WS,FOLLOW_WS_in_greater400); 
            	    WS50_tree = (CommonTree)adaptor.create(WS50);
            	    adaptor.addChild(root_0, WS50_tree);


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            GREATER51=(Token)match(input,GREATER,FOLLOW_GREATER_in_greater403); 
            GREATER51_tree = (CommonTree)adaptor.create(GREATER51);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER51_tree, root_0);

            // src/reimann/Query.g:56:30: ( WS )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==WS) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // src/reimann/Query.g:56:30: WS
            	    {
            	    WS52=(Token)match(input,WS,FOLLOW_WS_in_greater406); 
            	    WS52_tree = (CommonTree)adaptor.create(WS52);
            	    adaptor.addChild(root_0, WS52_tree);


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater409);
            value53=value();

            state._fsp--;

            adaptor.addChild(root_0, value53.getTree());

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
    // src/reimann/Query.g:57:1: greater_equal : field ( WS )* GREATER_EQUAL ( WS )* value ;
    public final QueryParser.greater_equal_return greater_equal() throws RecognitionException {
        QueryParser.greater_equal_return retval = new QueryParser.greater_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS55=null;
        Token GREATER_EQUAL56=null;
        Token WS57=null;
        QueryParser.field_return field54 = null;

        QueryParser.value_return value58 = null;


        CommonTree WS55_tree=null;
        CommonTree GREATER_EQUAL56_tree=null;
        CommonTree WS57_tree=null;

        try {
            // src/reimann/Query.g:58:2: ( field ( WS )* GREATER_EQUAL ( WS )* value )
            // src/reimann/Query.g:58:4: field ( WS )* GREATER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater_equal417);
            field54=field();

            state._fsp--;

            adaptor.addChild(root_0, field54.getTree());
            // src/reimann/Query.g:58:10: ( WS )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==WS) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // src/reimann/Query.g:58:10: WS
            	    {
            	    WS55=(Token)match(input,WS,FOLLOW_WS_in_greater_equal419); 
            	    WS55_tree = (CommonTree)adaptor.create(WS55);
            	    adaptor.addChild(root_0, WS55_tree);


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            GREATER_EQUAL56=(Token)match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_greater_equal422); 
            GREATER_EQUAL56_tree = (CommonTree)adaptor.create(GREATER_EQUAL56);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER_EQUAL56_tree, root_0);

            // src/reimann/Query.g:58:29: ( WS )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==WS) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // src/reimann/Query.g:58:29: WS
            	    {
            	    WS57=(Token)match(input,WS,FOLLOW_WS_in_greater_equal425); 
            	    WS57_tree = (CommonTree)adaptor.create(WS57);
            	    adaptor.addChild(root_0, WS57_tree);


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater_equal428);
            value58=value();

            state._fsp--;

            adaptor.addChild(root_0, value58.getTree());

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
    // src/reimann/Query.g:59:1: not_equal : field ( WS )* NOT_EQUAL ( WS )* value ;
    public final QueryParser.not_equal_return not_equal() throws RecognitionException {
        QueryParser.not_equal_return retval = new QueryParser.not_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS60=null;
        Token NOT_EQUAL61=null;
        Token WS62=null;
        QueryParser.field_return field59 = null;

        QueryParser.value_return value63 = null;


        CommonTree WS60_tree=null;
        CommonTree NOT_EQUAL61_tree=null;
        CommonTree WS62_tree=null;

        try {
            // src/reimann/Query.g:60:2: ( field ( WS )* NOT_EQUAL ( WS )* value )
            // src/reimann/Query.g:60:4: field ( WS )* NOT_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_not_equal436);
            field59=field();

            state._fsp--;

            adaptor.addChild(root_0, field59.getTree());
            // src/reimann/Query.g:60:10: ( WS )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==WS) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // src/reimann/Query.g:60:10: WS
            	    {
            	    WS60=(Token)match(input,WS,FOLLOW_WS_in_not_equal438); 
            	    WS60_tree = (CommonTree)adaptor.create(WS60);
            	    adaptor.addChild(root_0, WS60_tree);


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            NOT_EQUAL61=(Token)match(input,NOT_EQUAL,FOLLOW_NOT_EQUAL_in_not_equal441); 
            NOT_EQUAL61_tree = (CommonTree)adaptor.create(NOT_EQUAL61);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT_EQUAL61_tree, root_0);

            // src/reimann/Query.g:60:25: ( WS )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==WS) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // src/reimann/Query.g:60:25: WS
            	    {
            	    WS62=(Token)match(input,WS,FOLLOW_WS_in_not_equal444); 
            	    WS62_tree = (CommonTree)adaptor.create(WS62);
            	    adaptor.addChild(root_0, WS62_tree);


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_not_equal447);
            value63=value();

            state._fsp--;

            adaptor.addChild(root_0, value63.getTree());

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
    // src/reimann/Query.g:61:1: equal : field ( WS )* EQUAL ( WS )* value ;
    public final QueryParser.equal_return equal() throws RecognitionException {
        QueryParser.equal_return retval = new QueryParser.equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS65=null;
        Token EQUAL66=null;
        Token WS67=null;
        QueryParser.field_return field64 = null;

        QueryParser.value_return value68 = null;


        CommonTree WS65_tree=null;
        CommonTree EQUAL66_tree=null;
        CommonTree WS67_tree=null;

        try {
            // src/reimann/Query.g:61:7: ( field ( WS )* EQUAL ( WS )* value )
            // src/reimann/Query.g:61:9: field ( WS )* EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_equal455);
            field64=field();

            state._fsp--;

            adaptor.addChild(root_0, field64.getTree());
            // src/reimann/Query.g:61:15: ( WS )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==WS) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // src/reimann/Query.g:61:15: WS
            	    {
            	    WS65=(Token)match(input,WS,FOLLOW_WS_in_equal457); 
            	    WS65_tree = (CommonTree)adaptor.create(WS65);
            	    adaptor.addChild(root_0, WS65_tree);


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            EQUAL66=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_equal460); 
            EQUAL66_tree = (CommonTree)adaptor.create(EQUAL66);
            root_0 = (CommonTree)adaptor.becomeRoot(EQUAL66_tree, root_0);

            // src/reimann/Query.g:61:26: ( WS )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==WS) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // src/reimann/Query.g:61:26: WS
            	    {
            	    WS67=(Token)match(input,WS,FOLLOW_WS_in_equal463); 
            	    WS67_tree = (CommonTree)adaptor.create(WS67);
            	    adaptor.addChild(root_0, WS67_tree);


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_equal466);
            value68=value();

            state._fsp--;

            adaptor.addChild(root_0, value68.getTree());

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
    // src/reimann/Query.g:63:1: tagged : TAGGED ( WS )* String ;
    public final QueryParser.tagged_return tagged() throws RecognitionException {
        QueryParser.tagged_return retval = new QueryParser.tagged_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TAGGED69=null;
        Token WS70=null;
        Token String71=null;

        CommonTree TAGGED69_tree=null;
        CommonTree WS70_tree=null;
        CommonTree String71_tree=null;

        try {
            // src/reimann/Query.g:63:8: ( TAGGED ( WS )* String )
            // src/reimann/Query.g:63:10: TAGGED ( WS )* String
            {
            root_0 = (CommonTree)adaptor.nil();

            TAGGED69=(Token)match(input,TAGGED,FOLLOW_TAGGED_in_tagged474); 
            TAGGED69_tree = (CommonTree)adaptor.create(TAGGED69);
            root_0 = (CommonTree)adaptor.becomeRoot(TAGGED69_tree, root_0);

            // src/reimann/Query.g:63:18: ( WS )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==WS) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // src/reimann/Query.g:63:18: WS
            	    {
            	    WS70=(Token)match(input,WS,FOLLOW_WS_in_tagged477); 
            	    WS70_tree = (CommonTree)adaptor.create(WS70);
            	    adaptor.addChild(root_0, WS70_tree);


            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);

            String71=(Token)match(input,String,FOLLOW_String_in_tagged480); 
            String71_tree = (CommonTree)adaptor.create(String71);
            adaptor.addChild(root_0, String71_tree);


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
    // src/reimann/Query.g:65:1: value : ( String | t | f | nil | INT | FLOAT ) ;
    public final QueryParser.value_return value() throws RecognitionException {
        QueryParser.value_return retval = new QueryParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token String72=null;
        Token INT76=null;
        Token FLOAT77=null;
        QueryParser.t_return t73 = null;

        QueryParser.f_return f74 = null;

        QueryParser.nil_return nil75 = null;


        CommonTree String72_tree=null;
        CommonTree INT76_tree=null;
        CommonTree FLOAT77_tree=null;

        try {
            // src/reimann/Query.g:65:7: ( ( String | t | f | nil | INT | FLOAT ) )
            // src/reimann/Query.g:65:10: ( String | t | f | nil | INT | FLOAT )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:65:10: ( String | t | f | nil | INT | FLOAT )
            int alt28=6;
            switch ( input.LA(1) ) {
            case String:
                {
                alt28=1;
                }
                break;
            case 26:
                {
                alt28=2;
                }
                break;
            case 27:
                {
                alt28=3;
                }
                break;
            case 28:
            case 29:
                {
                alt28=4;
                }
                break;
            case INT:
                {
                alt28=5;
                }
                break;
            case FLOAT:
                {
                alt28=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }

            switch (alt28) {
                case 1 :
                    // src/reimann/Query.g:65:11: String
                    {
                    String72=(Token)match(input,String,FOLLOW_String_in_value490); 
                    String72_tree = (CommonTree)adaptor.create(String72);
                    adaptor.addChild(root_0, String72_tree);


                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:65:20: t
                    {
                    pushFollow(FOLLOW_t_in_value494);
                    t73=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t73.getTree());

                    }
                    break;
                case 3 :
                    // src/reimann/Query.g:65:24: f
                    {
                    pushFollow(FOLLOW_f_in_value498);
                    f74=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f74.getTree());

                    }
                    break;
                case 4 :
                    // src/reimann/Query.g:65:28: nil
                    {
                    pushFollow(FOLLOW_nil_in_value502);
                    nil75=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil75.getTree());

                    }
                    break;
                case 5 :
                    // src/reimann/Query.g:65:34: INT
                    {
                    INT76=(Token)match(input,INT,FOLLOW_INT_in_value506); 
                    INT76_tree = (CommonTree)adaptor.create(INT76);
                    adaptor.addChild(root_0, INT76_tree);


                    }
                    break;
                case 6 :
                    // src/reimann/Query.g:65:40: FLOAT
                    {
                    FLOAT77=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_value510); 
                    FLOAT77_tree = (CommonTree)adaptor.create(FLOAT77);
                    adaptor.addChild(root_0, FLOAT77_tree);


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
    // src/reimann/Query.g:67:1: t : 'true' ;
    public final QueryParser.t_return t() throws RecognitionException {
        QueryParser.t_return retval = new QueryParser.t_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal78=null;

        CommonTree string_literal78_tree=null;

        try {
            // src/reimann/Query.g:67:3: ( 'true' )
            // src/reimann/Query.g:67:5: 'true'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal78=(Token)match(input,26,FOLLOW_26_in_t519); 
            string_literal78_tree = (CommonTree)adaptor.create(string_literal78);
            adaptor.addChild(root_0, string_literal78_tree);


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
    // src/reimann/Query.g:68:1: f : 'false' ;
    public final QueryParser.f_return f() throws RecognitionException {
        QueryParser.f_return retval = new QueryParser.f_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal79=null;

        CommonTree string_literal79_tree=null;

        try {
            // src/reimann/Query.g:68:3: ( 'false' )
            // src/reimann/Query.g:68:5: 'false'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal79=(Token)match(input,27,FOLLOW_27_in_f526); 
            string_literal79_tree = (CommonTree)adaptor.create(string_literal79);
            adaptor.addChild(root_0, string_literal79_tree);


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
    // src/reimann/Query.g:69:1: nil : ( 'null' | 'nil' );
    public final QueryParser.nil_return nil() throws RecognitionException {
        QueryParser.nil_return retval = new QueryParser.nil_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set80=null;

        CommonTree set80_tree=null;

        try {
            // src/reimann/Query.g:69:5: ( 'null' | 'nil' )
            // src/reimann/Query.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set80=(Token)input.LT(1);
            if ( (input.LA(1)>=28 && input.LA(1)<=29) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set80));
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
    // src/reimann/Query.g:71:1: field : ( 'host' | 'service' | 'state' | 'description' | 'metric' | 'metric_f' | 'time' ) ;
    public final QueryParser.field_return field() throws RecognitionException {
        QueryParser.field_return retval = new QueryParser.field_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set81=null;

        CommonTree set81_tree=null;

        try {
            // src/reimann/Query.g:71:7: ( ( 'host' | 'service' | 'state' | 'description' | 'metric' | 'metric_f' | 'time' ) )
            // src/reimann/Query.g:71:9: ( 'host' | 'service' | 'state' | 'description' | 'metric' | 'metric_f' | 'time' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set81=(Token)input.LT(1);
            if ( (input.LA(1)>=30 && input.LA(1)<=36) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set81));
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
        "\1\31\1\17\2\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA8_specialS =
        "\4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\3\1\2\11\uffff\1\1\11\uffff\1\2",
            "\1\3\1\2\11\uffff\1\1",
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
            return "()* loopback of 29:23: ( ( WS )* AND ( WS )* ( not | primary ) )*";
        }
    }
    static final String DFA12_eotS =
        "\16\uffff";
    static final String DFA12_eofS =
        "\16\uffff";
    static final String DFA12_minS =
        "\1\16\4\uffff\2\7\7\uffff";
    static final String DFA12_maxS =
        "\1\44\4\uffff\2\17\7\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\2\uffff\1\10\1\11\1\6\1\13\1\5\1\7\1\12";
    static final String DFA12_specialS =
        "\16\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\4\13\uffff\1\1\1\2\2\3\7\5",
            "",
            "",
            "",
            "",
            "\1\13\1\15\1\12\1\11\1\14\1\7\1\10\1\uffff\1\6",
            "\1\13\1\15\1\12\1\11\1\14\1\7\1\10\1\uffff\1\6",
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
            return "40:10: ( t | f | nil | tagged | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )";
        }
    }
 

    public static final BitSet FOLLOW_or_in_expr137 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_expr139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_or152 = new BitSet(new long[]{0x0000000000008022L});
    public static final BitSet FOLLOW_WS_in_or155 = new BitSet(new long[]{0x0000000000008020L});
    public static final BitSet FOLLOW_OR_in_or158 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_WS_in_or161 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_and_in_or164 = new BitSet(new long[]{0x0000000000008022L});
    public static final BitSet FOLLOW_not_in_and175 = new BitSet(new long[]{0x0000000000008012L});
    public static final BitSet FOLLOW_primary_in_and179 = new BitSet(new long[]{0x0000000000008012L});
    public static final BitSet FOLLOW_WS_in_and183 = new BitSet(new long[]{0x0000000000008010L});
    public static final BitSet FOLLOW_AND_in_and186 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_WS_in_and189 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_not_in_and193 = new BitSet(new long[]{0x0000000000008012L});
    public static final BitSet FOLLOW_primary_in_and197 = new BitSet(new long[]{0x0000000000008012L});
    public static final BitSet FOLLOW_NOT_in_not208 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_WS_in_not211 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_not_in_not215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_not219 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_primary236 = new BitSet(new long[]{0x0000001FFD00C040L});
    public static final BitSet FOLLOW_or_in_primary238 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_primary240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_in_primary254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_simple274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_simple278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_simple282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tagged_in_simple288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_approximately_in_simple294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_in_simple300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_equal_in_simple306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_in_simple312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_equal_in_simple318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equal_in_simple324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equal_in_simple330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_approximately343 = new BitSet(new long[]{0x0000000000008080L});
    public static final BitSet FOLLOW_WS_in_approximately345 = new BitSet(new long[]{0x0000000000008080L});
    public static final BitSet FOLLOW_APPROXIMATELY_in_approximately348 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_approximately351 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_approximately354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser361 = new BitSet(new long[]{0x0000000000008400L});
    public static final BitSet FOLLOW_WS_in_lesser363 = new BitSet(new long[]{0x0000000000008400L});
    public static final BitSet FOLLOW_LESSER_in_lesser366 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_lesser369 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_lesser372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser_equal380 = new BitSet(new long[]{0x0000000000008800L});
    public static final BitSet FOLLOW_WS_in_lesser_equal382 = new BitSet(new long[]{0x0000000000008800L});
    public static final BitSet FOLLOW_LESSER_EQUAL_in_lesser_equal385 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal388 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_lesser_equal391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater398 = new BitSet(new long[]{0x0000000000009000L});
    public static final BitSet FOLLOW_WS_in_greater400 = new BitSet(new long[]{0x0000000000009000L});
    public static final BitSet FOLLOW_GREATER_in_greater403 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_greater406 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_greater409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater_equal417 = new BitSet(new long[]{0x000000000000A000L});
    public static final BitSet FOLLOW_WS_in_greater_equal419 = new BitSet(new long[]{0x000000000000A000L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_greater_equal422 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_greater_equal425 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_greater_equal428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_not_equal436 = new BitSet(new long[]{0x0000000000008100L});
    public static final BitSet FOLLOW_WS_in_not_equal438 = new BitSet(new long[]{0x0000000000008100L});
    public static final BitSet FOLLOW_NOT_EQUAL_in_not_equal441 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_not_equal444 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_not_equal447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_equal455 = new BitSet(new long[]{0x0000000000008200L});
    public static final BitSet FOLLOW_WS_in_equal457 = new BitSet(new long[]{0x0000000000008200L});
    public static final BitSet FOLLOW_EQUAL_in_equal460 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_WS_in_equal463 = new BitSet(new long[]{0x000000003C078000L});
    public static final BitSet FOLLOW_value_in_equal466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TAGGED_in_tagged474 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_WS_in_tagged477 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_String_in_tagged480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_value490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_value494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_value498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_value502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_value506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_value510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_t519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_f526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nil0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_field545 = new BitSet(new long[]{0x0000000000000002L});

}