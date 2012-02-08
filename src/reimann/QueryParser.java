// $ANTLR 3.2 Sep 23, 2009 14:05:07 src/reimann/Query.g 2012-02-07 15:10:23
package reimann;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class QueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "OR", "NOT", "APPROXIMATELY", "NOT_EQUAL", "EQUAL", "LESSER", "LESSER_EQUAL", "GREATER", "GREATER_EQUAL", "WS", "String", "INT", "FLOAT", "ID", "EXPONENT", "EscapeSequence", "UnicodeEscape", "HexDigit", "'('", "')'", "'true'", "'false'", "'null'", "'nil'", "'host'", "'service'", "'state'", "'description'", "'metric_f'", "'time'"
    };
    public static final int LESSER_EQUAL=11;
    public static final int EXPONENT=19;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int APPROXIMATELY=7;
    public static final int FLOAT=17;
    public static final int INT=16;
    public static final int NOT=6;
    public static final int ID=18;
    public static final int AND=4;
    public static final int EOF=-1;
    public static final int HexDigit=22;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=14;
    public static final int GREATER=12;
    public static final int LESSER=10;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int NOT_EQUAL=8;
    public static final int UnicodeEscape=21;
    public static final int EQUAL=9;
    public static final int OR=5;
    public static final int String=15;
    public static final int EscapeSequence=20;
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
    // src/reimann/Query.g:24:1: expr : ( or EOF ) -> or ;
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
            // src/reimann/Query.g:24:6: ( ( or EOF ) -> or )
            // src/reimann/Query.g:24:8: ( or EOF )
            {
            // src/reimann/Query.g:24:8: ( or EOF )
            // src/reimann/Query.g:24:9: or EOF
            {
            pushFollow(FOLLOW_or_in_expr129);
            or1=or();

            state._fsp--;

            stream_or.add(or1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_expr131);  
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
            // 24:17: -> or
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
    // src/reimann/Query.g:26:1: or : and ( ( WS )* OR ( WS )* and )* ;
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
            // src/reimann/Query.g:26:4: ( and ( ( WS )* OR ( WS )* and )* )
            // src/reimann/Query.g:26:6: and ( ( WS )* OR ( WS )* and )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_and_in_or144);
            and3=and();

            state._fsp--;

            adaptor.addChild(root_0, and3.getTree());
            // src/reimann/Query.g:26:10: ( ( WS )* OR ( WS )* and )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR||LA3_0==WS) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // src/reimann/Query.g:26:11: ( WS )* OR ( WS )* and
            	    {
            	    // src/reimann/Query.g:26:11: ( WS )*
            	    loop1:
            	    do {
            	        int alt1=2;
            	        int LA1_0 = input.LA(1);

            	        if ( (LA1_0==WS) ) {
            	            alt1=1;
            	        }


            	        switch (alt1) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:26:11: WS
            	    	    {
            	    	    WS4=(Token)match(input,WS,FOLLOW_WS_in_or147); 
            	    	    WS4_tree = (CommonTree)adaptor.create(WS4);
            	    	    adaptor.addChild(root_0, WS4_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop1;
            	        }
            	    } while (true);

            	    OR5=(Token)match(input,OR,FOLLOW_OR_in_or150); 
            	    OR5_tree = (CommonTree)adaptor.create(OR5);
            	    root_0 = (CommonTree)adaptor.becomeRoot(OR5_tree, root_0);

            	    // src/reimann/Query.g:26:19: ( WS )*
            	    loop2:
            	    do {
            	        int alt2=2;
            	        int LA2_0 = input.LA(1);

            	        if ( (LA2_0==WS) ) {
            	            alt2=1;
            	        }


            	        switch (alt2) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:26:19: WS
            	    	    {
            	    	    WS6=(Token)match(input,WS,FOLLOW_WS_in_or153); 
            	    	    WS6_tree = (CommonTree)adaptor.create(WS6);
            	    	    adaptor.addChild(root_0, WS6_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop2;
            	        }
            	    } while (true);

            	    pushFollow(FOLLOW_and_in_or156);
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
    // src/reimann/Query.g:28:1: and : ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* ;
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
            // src/reimann/Query.g:28:5: ( ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )* )
            // src/reimann/Query.g:28:7: ( not | primary ) ( ( WS )* AND ( WS )* ( not | primary ) )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:28:7: ( not | primary )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==NOT) ) {
                alt4=1;
            }
            else if ( (LA4_0==23||(LA4_0>=25 && LA4_0<=34)) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // src/reimann/Query.g:28:8: not
                    {
                    pushFollow(FOLLOW_not_in_and167);
                    not8=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not8.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:28:14: primary
                    {
                    pushFollow(FOLLOW_primary_in_and171);
                    primary9=primary();

                    state._fsp--;

                    adaptor.addChild(root_0, primary9.getTree());

                    }
                    break;

            }

            // src/reimann/Query.g:28:23: ( ( WS )* AND ( WS )* ( not | primary ) )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // src/reimann/Query.g:28:24: ( WS )* AND ( WS )* ( not | primary )
            	    {
            	    // src/reimann/Query.g:28:24: ( WS )*
            	    loop5:
            	    do {
            	        int alt5=2;
            	        int LA5_0 = input.LA(1);

            	        if ( (LA5_0==WS) ) {
            	            alt5=1;
            	        }


            	        switch (alt5) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:28:24: WS
            	    	    {
            	    	    WS10=(Token)match(input,WS,FOLLOW_WS_in_and175); 
            	    	    WS10_tree = (CommonTree)adaptor.create(WS10);
            	    	    adaptor.addChild(root_0, WS10_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop5;
            	        }
            	    } while (true);

            	    AND11=(Token)match(input,AND,FOLLOW_AND_in_and178); 
            	    AND11_tree = (CommonTree)adaptor.create(AND11);
            	    root_0 = (CommonTree)adaptor.becomeRoot(AND11_tree, root_0);

            	    // src/reimann/Query.g:28:33: ( WS )*
            	    loop6:
            	    do {
            	        int alt6=2;
            	        int LA6_0 = input.LA(1);

            	        if ( (LA6_0==WS) ) {
            	            alt6=1;
            	        }


            	        switch (alt6) {
            	    	case 1 :
            	    	    // src/reimann/Query.g:28:33: WS
            	    	    {
            	    	    WS12=(Token)match(input,WS,FOLLOW_WS_in_and181); 
            	    	    WS12_tree = (CommonTree)adaptor.create(WS12);
            	    	    adaptor.addChild(root_0, WS12_tree);


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop6;
            	        }
            	    } while (true);

            	    // src/reimann/Query.g:28:37: ( not | primary )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==NOT) ) {
            	        alt7=1;
            	    }
            	    else if ( (LA7_0==23||(LA7_0>=25 && LA7_0<=34)) ) {
            	        alt7=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // src/reimann/Query.g:28:38: not
            	            {
            	            pushFollow(FOLLOW_not_in_and185);
            	            not13=not();

            	            state._fsp--;

            	            adaptor.addChild(root_0, not13.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // src/reimann/Query.g:28:44: primary
            	            {
            	            pushFollow(FOLLOW_primary_in_and189);
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
    // src/reimann/Query.g:30:1: not : NOT ( WS )* ( not | primary ) ;
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
            // src/reimann/Query.g:30:5: ( NOT ( WS )* ( not | primary ) )
            // src/reimann/Query.g:30:7: NOT ( WS )* ( not | primary )
            {
            root_0 = (CommonTree)adaptor.nil();

            NOT15=(Token)match(input,NOT,FOLLOW_NOT_in_not200); 
            NOT15_tree = (CommonTree)adaptor.create(NOT15);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT15_tree, root_0);

            // src/reimann/Query.g:30:12: ( WS )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==WS) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // src/reimann/Query.g:30:12: WS
            	    {
            	    WS16=(Token)match(input,WS,FOLLOW_WS_in_not203); 
            	    WS16_tree = (CommonTree)adaptor.create(WS16);
            	    adaptor.addChild(root_0, WS16_tree);


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // src/reimann/Query.g:30:16: ( not | primary )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==NOT) ) {
                alt10=1;
            }
            else if ( (LA10_0==23||(LA10_0>=25 && LA10_0<=34)) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // src/reimann/Query.g:30:17: not
                    {
                    pushFollow(FOLLOW_not_in_not207);
                    not17=not();

                    state._fsp--;

                    adaptor.addChild(root_0, not17.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:30:23: primary
                    {
                    pushFollow(FOLLOW_primary_in_not211);
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
    // src/reimann/Query.g:33:1: primary : ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) ;
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
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleSubtreeStream stream_or=new RewriteRuleSubtreeStream(adaptor,"rule or");
        RewriteRuleSubtreeStream stream_simple=new RewriteRuleSubtreeStream(adaptor,"rule simple");
        try {
            // src/reimann/Query.g:33:9: ( ( ( '(' or ')' ) -> ^( or ) | simple -> simple ) )
            // src/reimann/Query.g:33:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            {
            // src/reimann/Query.g:33:12: ( ( '(' or ')' ) -> ^( or ) | simple -> simple )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==23) ) {
                alt11=1;
            }
            else if ( ((LA11_0>=25 && LA11_0<=34)) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // src/reimann/Query.g:34:4: ( '(' or ')' )
                    {
                    // src/reimann/Query.g:34:4: ( '(' or ')' )
                    // src/reimann/Query.g:34:5: '(' or ')'
                    {
                    char_literal19=(Token)match(input,23,FOLLOW_23_in_primary228);  
                    stream_23.add(char_literal19);

                    pushFollow(FOLLOW_or_in_primary230);
                    or20=or();

                    state._fsp--;

                    stream_or.add(or20.getTree());
                    char_literal21=(Token)match(input,24,FOLLOW_24_in_primary232);  
                    stream_24.add(char_literal21);


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
                    // 34:17: -> ^( or )
                    {
                        // src/reimann/Query.g:34:20: ^( or )
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
                    // src/reimann/Query.g:35:6: simple
                    {
                    pushFollow(FOLLOW_simple_in_primary246);
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
                    // 35:13: -> simple
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
    // src/reimann/Query.g:38:1: fragment simple : ( t | f | nil | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) ;
    public final QueryParser.simple_return simple() throws RecognitionException {
        QueryParser.simple_return retval = new QueryParser.simple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        QueryParser.t_return t23 = null;

        QueryParser.f_return f24 = null;

        QueryParser.nil_return nil25 = null;

        QueryParser.approximately_return approximately26 = null;

        QueryParser.lesser_return lesser27 = null;

        QueryParser.lesser_equal_return lesser_equal28 = null;

        QueryParser.greater_return greater29 = null;

        QueryParser.greater_equal_return greater_equal30 = null;

        QueryParser.not_equal_return not_equal31 = null;

        QueryParser.equal_return equal32 = null;



        try {
            // src/reimann/Query.g:39:8: ( ( t | f | nil | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal ) )
            // src/reimann/Query.g:39:10: ( t | f | nil | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:39:10: ( t | f | nil | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )
            int alt12=10;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // src/reimann/Query.g:39:12: t
                    {
                    pushFollow(FOLLOW_t_in_simple266);
                    t23=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t23.getTree());

                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:39:16: f
                    {
                    pushFollow(FOLLOW_f_in_simple270);
                    f24=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f24.getTree());

                    }
                    break;
                case 3 :
                    // src/reimann/Query.g:39:20: nil
                    {
                    pushFollow(FOLLOW_nil_in_simple274);
                    nil25=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil25.getTree());

                    }
                    break;
                case 4 :
                    // src/reimann/Query.g:40:5: approximately
                    {
                    pushFollow(FOLLOW_approximately_in_simple280);
                    approximately26=approximately();

                    state._fsp--;

                    adaptor.addChild(root_0, approximately26.getTree());

                    }
                    break;
                case 5 :
                    // src/reimann/Query.g:41:5: lesser
                    {
                    pushFollow(FOLLOW_lesser_in_simple286);
                    lesser27=lesser();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser27.getTree());

                    }
                    break;
                case 6 :
                    // src/reimann/Query.g:42:5: lesser_equal
                    {
                    pushFollow(FOLLOW_lesser_equal_in_simple292);
                    lesser_equal28=lesser_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, lesser_equal28.getTree());

                    }
                    break;
                case 7 :
                    // src/reimann/Query.g:43:5: greater
                    {
                    pushFollow(FOLLOW_greater_in_simple298);
                    greater29=greater();

                    state._fsp--;

                    adaptor.addChild(root_0, greater29.getTree());

                    }
                    break;
                case 8 :
                    // src/reimann/Query.g:44:5: greater_equal
                    {
                    pushFollow(FOLLOW_greater_equal_in_simple304);
                    greater_equal30=greater_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, greater_equal30.getTree());

                    }
                    break;
                case 9 :
                    // src/reimann/Query.g:45:5: not_equal
                    {
                    pushFollow(FOLLOW_not_equal_in_simple310);
                    not_equal31=not_equal();

                    state._fsp--;

                    adaptor.addChild(root_0, not_equal31.getTree());

                    }
                    break;
                case 10 :
                    // src/reimann/Query.g:46:5: equal
                    {
                    pushFollow(FOLLOW_equal_in_simple316);
                    equal32=equal();

                    state._fsp--;

                    adaptor.addChild(root_0, equal32.getTree());

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
    // src/reimann/Query.g:48:1: approximately : field ( WS )* APPROXIMATELY ( WS )* value ;
    public final QueryParser.approximately_return approximately() throws RecognitionException {
        QueryParser.approximately_return retval = new QueryParser.approximately_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS34=null;
        Token APPROXIMATELY35=null;
        Token WS36=null;
        QueryParser.field_return field33 = null;

        QueryParser.value_return value37 = null;


        CommonTree WS34_tree=null;
        CommonTree APPROXIMATELY35_tree=null;
        CommonTree WS36_tree=null;

        try {
            // src/reimann/Query.g:49:2: ( field ( WS )* APPROXIMATELY ( WS )* value )
            // src/reimann/Query.g:49:4: field ( WS )* APPROXIMATELY ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_approximately326);
            field33=field();

            state._fsp--;

            adaptor.addChild(root_0, field33.getTree());
            // src/reimann/Query.g:49:10: ( WS )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==WS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // src/reimann/Query.g:49:10: WS
            	    {
            	    WS34=(Token)match(input,WS,FOLLOW_WS_in_approximately328); 
            	    WS34_tree = (CommonTree)adaptor.create(WS34);
            	    adaptor.addChild(root_0, WS34_tree);


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            APPROXIMATELY35=(Token)match(input,APPROXIMATELY,FOLLOW_APPROXIMATELY_in_approximately331); 
            APPROXIMATELY35_tree = (CommonTree)adaptor.create(APPROXIMATELY35);
            root_0 = (CommonTree)adaptor.becomeRoot(APPROXIMATELY35_tree, root_0);

            // src/reimann/Query.g:49:29: ( WS )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==WS) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // src/reimann/Query.g:49:29: WS
            	    {
            	    WS36=(Token)match(input,WS,FOLLOW_WS_in_approximately334); 
            	    WS36_tree = (CommonTree)adaptor.create(WS36);
            	    adaptor.addChild(root_0, WS36_tree);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_approximately337);
            value37=value();

            state._fsp--;

            adaptor.addChild(root_0, value37.getTree());

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
    // src/reimann/Query.g:50:1: lesser : field ( WS )* LESSER ( WS )* value ;
    public final QueryParser.lesser_return lesser() throws RecognitionException {
        QueryParser.lesser_return retval = new QueryParser.lesser_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS39=null;
        Token LESSER40=null;
        Token WS41=null;
        QueryParser.field_return field38 = null;

        QueryParser.value_return value42 = null;


        CommonTree WS39_tree=null;
        CommonTree LESSER40_tree=null;
        CommonTree WS41_tree=null;

        try {
            // src/reimann/Query.g:50:8: ( field ( WS )* LESSER ( WS )* value )
            // src/reimann/Query.g:50:10: field ( WS )* LESSER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser344);
            field38=field();

            state._fsp--;

            adaptor.addChild(root_0, field38.getTree());
            // src/reimann/Query.g:50:16: ( WS )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==WS) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // src/reimann/Query.g:50:16: WS
            	    {
            	    WS39=(Token)match(input,WS,FOLLOW_WS_in_lesser346); 
            	    WS39_tree = (CommonTree)adaptor.create(WS39);
            	    adaptor.addChild(root_0, WS39_tree);


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            LESSER40=(Token)match(input,LESSER,FOLLOW_LESSER_in_lesser349); 
            LESSER40_tree = (CommonTree)adaptor.create(LESSER40);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER40_tree, root_0);

            // src/reimann/Query.g:50:28: ( WS )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==WS) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // src/reimann/Query.g:50:28: WS
            	    {
            	    WS41=(Token)match(input,WS,FOLLOW_WS_in_lesser352); 
            	    WS41_tree = (CommonTree)adaptor.create(WS41);
            	    adaptor.addChild(root_0, WS41_tree);


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser355);
            value42=value();

            state._fsp--;

            adaptor.addChild(root_0, value42.getTree());

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
    // src/reimann/Query.g:51:1: lesser_equal : field ( WS )* LESSER_EQUAL ( WS )* value ;
    public final QueryParser.lesser_equal_return lesser_equal() throws RecognitionException {
        QueryParser.lesser_equal_return retval = new QueryParser.lesser_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS44=null;
        Token LESSER_EQUAL45=null;
        Token WS46=null;
        QueryParser.field_return field43 = null;

        QueryParser.value_return value47 = null;


        CommonTree WS44_tree=null;
        CommonTree LESSER_EQUAL45_tree=null;
        CommonTree WS46_tree=null;

        try {
            // src/reimann/Query.g:52:2: ( field ( WS )* LESSER_EQUAL ( WS )* value )
            // src/reimann/Query.g:52:4: field ( WS )* LESSER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_lesser_equal363);
            field43=field();

            state._fsp--;

            adaptor.addChild(root_0, field43.getTree());
            // src/reimann/Query.g:52:10: ( WS )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==WS) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // src/reimann/Query.g:52:10: WS
            	    {
            	    WS44=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal365); 
            	    WS44_tree = (CommonTree)adaptor.create(WS44);
            	    adaptor.addChild(root_0, WS44_tree);


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            LESSER_EQUAL45=(Token)match(input,LESSER_EQUAL,FOLLOW_LESSER_EQUAL_in_lesser_equal368); 
            LESSER_EQUAL45_tree = (CommonTree)adaptor.create(LESSER_EQUAL45);
            root_0 = (CommonTree)adaptor.becomeRoot(LESSER_EQUAL45_tree, root_0);

            // src/reimann/Query.g:52:28: ( WS )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==WS) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // src/reimann/Query.g:52:28: WS
            	    {
            	    WS46=(Token)match(input,WS,FOLLOW_WS_in_lesser_equal371); 
            	    WS46_tree = (CommonTree)adaptor.create(WS46);
            	    adaptor.addChild(root_0, WS46_tree);


            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_lesser_equal374);
            value47=value();

            state._fsp--;

            adaptor.addChild(root_0, value47.getTree());

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
    // src/reimann/Query.g:53:1: greater : field ( WS )* GREATER ( WS )* value ;
    public final QueryParser.greater_return greater() throws RecognitionException {
        QueryParser.greater_return retval = new QueryParser.greater_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS49=null;
        Token GREATER50=null;
        Token WS51=null;
        QueryParser.field_return field48 = null;

        QueryParser.value_return value52 = null;


        CommonTree WS49_tree=null;
        CommonTree GREATER50_tree=null;
        CommonTree WS51_tree=null;

        try {
            // src/reimann/Query.g:53:9: ( field ( WS )* GREATER ( WS )* value )
            // src/reimann/Query.g:53:11: field ( WS )* GREATER ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater381);
            field48=field();

            state._fsp--;

            adaptor.addChild(root_0, field48.getTree());
            // src/reimann/Query.g:53:17: ( WS )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==WS) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // src/reimann/Query.g:53:17: WS
            	    {
            	    WS49=(Token)match(input,WS,FOLLOW_WS_in_greater383); 
            	    WS49_tree = (CommonTree)adaptor.create(WS49);
            	    adaptor.addChild(root_0, WS49_tree);


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            GREATER50=(Token)match(input,GREATER,FOLLOW_GREATER_in_greater386); 
            GREATER50_tree = (CommonTree)adaptor.create(GREATER50);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER50_tree, root_0);

            // src/reimann/Query.g:53:30: ( WS )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==WS) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // src/reimann/Query.g:53:30: WS
            	    {
            	    WS51=(Token)match(input,WS,FOLLOW_WS_in_greater389); 
            	    WS51_tree = (CommonTree)adaptor.create(WS51);
            	    adaptor.addChild(root_0, WS51_tree);


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater392);
            value52=value();

            state._fsp--;

            adaptor.addChild(root_0, value52.getTree());

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
    // src/reimann/Query.g:54:1: greater_equal : field ( WS )* GREATER_EQUAL ( WS )* value ;
    public final QueryParser.greater_equal_return greater_equal() throws RecognitionException {
        QueryParser.greater_equal_return retval = new QueryParser.greater_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS54=null;
        Token GREATER_EQUAL55=null;
        Token WS56=null;
        QueryParser.field_return field53 = null;

        QueryParser.value_return value57 = null;


        CommonTree WS54_tree=null;
        CommonTree GREATER_EQUAL55_tree=null;
        CommonTree WS56_tree=null;

        try {
            // src/reimann/Query.g:55:2: ( field ( WS )* GREATER_EQUAL ( WS )* value )
            // src/reimann/Query.g:55:4: field ( WS )* GREATER_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_greater_equal400);
            field53=field();

            state._fsp--;

            adaptor.addChild(root_0, field53.getTree());
            // src/reimann/Query.g:55:10: ( WS )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==WS) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // src/reimann/Query.g:55:10: WS
            	    {
            	    WS54=(Token)match(input,WS,FOLLOW_WS_in_greater_equal402); 
            	    WS54_tree = (CommonTree)adaptor.create(WS54);
            	    adaptor.addChild(root_0, WS54_tree);


            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);

            GREATER_EQUAL55=(Token)match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_greater_equal405); 
            GREATER_EQUAL55_tree = (CommonTree)adaptor.create(GREATER_EQUAL55);
            root_0 = (CommonTree)adaptor.becomeRoot(GREATER_EQUAL55_tree, root_0);

            // src/reimann/Query.g:55:29: ( WS )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==WS) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // src/reimann/Query.g:55:29: WS
            	    {
            	    WS56=(Token)match(input,WS,FOLLOW_WS_in_greater_equal408); 
            	    WS56_tree = (CommonTree)adaptor.create(WS56);
            	    adaptor.addChild(root_0, WS56_tree);


            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_greater_equal411);
            value57=value();

            state._fsp--;

            adaptor.addChild(root_0, value57.getTree());

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
    // src/reimann/Query.g:56:1: not_equal : field ( WS )* NOT_EQUAL ( WS )* value ;
    public final QueryParser.not_equal_return not_equal() throws RecognitionException {
        QueryParser.not_equal_return retval = new QueryParser.not_equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS59=null;
        Token NOT_EQUAL60=null;
        Token WS61=null;
        QueryParser.field_return field58 = null;

        QueryParser.value_return value62 = null;


        CommonTree WS59_tree=null;
        CommonTree NOT_EQUAL60_tree=null;
        CommonTree WS61_tree=null;

        try {
            // src/reimann/Query.g:57:2: ( field ( WS )* NOT_EQUAL ( WS )* value )
            // src/reimann/Query.g:57:4: field ( WS )* NOT_EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_not_equal419);
            field58=field();

            state._fsp--;

            adaptor.addChild(root_0, field58.getTree());
            // src/reimann/Query.g:57:10: ( WS )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==WS) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // src/reimann/Query.g:57:10: WS
            	    {
            	    WS59=(Token)match(input,WS,FOLLOW_WS_in_not_equal421); 
            	    WS59_tree = (CommonTree)adaptor.create(WS59);
            	    adaptor.addChild(root_0, WS59_tree);


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            NOT_EQUAL60=(Token)match(input,NOT_EQUAL,FOLLOW_NOT_EQUAL_in_not_equal424); 
            NOT_EQUAL60_tree = (CommonTree)adaptor.create(NOT_EQUAL60);
            root_0 = (CommonTree)adaptor.becomeRoot(NOT_EQUAL60_tree, root_0);

            // src/reimann/Query.g:57:25: ( WS )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==WS) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // src/reimann/Query.g:57:25: WS
            	    {
            	    WS61=(Token)match(input,WS,FOLLOW_WS_in_not_equal427); 
            	    WS61_tree = (CommonTree)adaptor.create(WS61);
            	    adaptor.addChild(root_0, WS61_tree);


            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_not_equal430);
            value62=value();

            state._fsp--;

            adaptor.addChild(root_0, value62.getTree());

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
    // src/reimann/Query.g:58:1: equal : field ( WS )* EQUAL ( WS )* value ;
    public final QueryParser.equal_return equal() throws RecognitionException {
        QueryParser.equal_return retval = new QueryParser.equal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WS64=null;
        Token EQUAL65=null;
        Token WS66=null;
        QueryParser.field_return field63 = null;

        QueryParser.value_return value67 = null;


        CommonTree WS64_tree=null;
        CommonTree EQUAL65_tree=null;
        CommonTree WS66_tree=null;

        try {
            // src/reimann/Query.g:58:7: ( field ( WS )* EQUAL ( WS )* value )
            // src/reimann/Query.g:58:9: field ( WS )* EQUAL ( WS )* value
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_field_in_equal438);
            field63=field();

            state._fsp--;

            adaptor.addChild(root_0, field63.getTree());
            // src/reimann/Query.g:58:15: ( WS )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==WS) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // src/reimann/Query.g:58:15: WS
            	    {
            	    WS64=(Token)match(input,WS,FOLLOW_WS_in_equal440); 
            	    WS64_tree = (CommonTree)adaptor.create(WS64);
            	    adaptor.addChild(root_0, WS64_tree);


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            EQUAL65=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_equal443); 
            EQUAL65_tree = (CommonTree)adaptor.create(EQUAL65);
            root_0 = (CommonTree)adaptor.becomeRoot(EQUAL65_tree, root_0);

            // src/reimann/Query.g:58:26: ( WS )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==WS) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // src/reimann/Query.g:58:26: WS
            	    {
            	    WS66=(Token)match(input,WS,FOLLOW_WS_in_equal446); 
            	    WS66_tree = (CommonTree)adaptor.create(WS66);
            	    adaptor.addChild(root_0, WS66_tree);


            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);

            pushFollow(FOLLOW_value_in_equal449);
            value67=value();

            state._fsp--;

            adaptor.addChild(root_0, value67.getTree());

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

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // src/reimann/Query.g:60:1: value : ( String | t | f | nil | INT | FLOAT ) ;
    public final QueryParser.value_return value() throws RecognitionException {
        QueryParser.value_return retval = new QueryParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token String68=null;
        Token INT72=null;
        Token FLOAT73=null;
        QueryParser.t_return t69 = null;

        QueryParser.f_return f70 = null;

        QueryParser.nil_return nil71 = null;


        CommonTree String68_tree=null;
        CommonTree INT72_tree=null;
        CommonTree FLOAT73_tree=null;

        try {
            // src/reimann/Query.g:60:7: ( ( String | t | f | nil | INT | FLOAT ) )
            // src/reimann/Query.g:60:10: ( String | t | f | nil | INT | FLOAT )
            {
            root_0 = (CommonTree)adaptor.nil();

            // src/reimann/Query.g:60:10: ( String | t | f | nil | INT | FLOAT )
            int alt27=6;
            switch ( input.LA(1) ) {
            case String:
                {
                alt27=1;
                }
                break;
            case 25:
                {
                alt27=2;
                }
                break;
            case 26:
                {
                alt27=3;
                }
                break;
            case 27:
            case 28:
                {
                alt27=4;
                }
                break;
            case INT:
                {
                alt27=5;
                }
                break;
            case FLOAT:
                {
                alt27=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // src/reimann/Query.g:60:11: String
                    {
                    String68=(Token)match(input,String,FOLLOW_String_in_value459); 
                    String68_tree = (CommonTree)adaptor.create(String68);
                    adaptor.addChild(root_0, String68_tree);


                    }
                    break;
                case 2 :
                    // src/reimann/Query.g:60:20: t
                    {
                    pushFollow(FOLLOW_t_in_value463);
                    t69=t();

                    state._fsp--;

                    adaptor.addChild(root_0, t69.getTree());

                    }
                    break;
                case 3 :
                    // src/reimann/Query.g:60:24: f
                    {
                    pushFollow(FOLLOW_f_in_value467);
                    f70=f();

                    state._fsp--;

                    adaptor.addChild(root_0, f70.getTree());

                    }
                    break;
                case 4 :
                    // src/reimann/Query.g:60:28: nil
                    {
                    pushFollow(FOLLOW_nil_in_value471);
                    nil71=nil();

                    state._fsp--;

                    adaptor.addChild(root_0, nil71.getTree());

                    }
                    break;
                case 5 :
                    // src/reimann/Query.g:60:34: INT
                    {
                    INT72=(Token)match(input,INT,FOLLOW_INT_in_value475); 
                    INT72_tree = (CommonTree)adaptor.create(INT72);
                    adaptor.addChild(root_0, INT72_tree);


                    }
                    break;
                case 6 :
                    // src/reimann/Query.g:60:40: FLOAT
                    {
                    FLOAT73=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_value479); 
                    FLOAT73_tree = (CommonTree)adaptor.create(FLOAT73);
                    adaptor.addChild(root_0, FLOAT73_tree);


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
    // src/reimann/Query.g:63:1: t : 'true' ;
    public final QueryParser.t_return t() throws RecognitionException {
        QueryParser.t_return retval = new QueryParser.t_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal74=null;

        CommonTree string_literal74_tree=null;

        try {
            // src/reimann/Query.g:63:3: ( 'true' )
            // src/reimann/Query.g:63:5: 'true'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal74=(Token)match(input,25,FOLLOW_25_in_t489); 
            string_literal74_tree = (CommonTree)adaptor.create(string_literal74);
            adaptor.addChild(root_0, string_literal74_tree);


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
    // src/reimann/Query.g:64:1: f : 'false' ;
    public final QueryParser.f_return f() throws RecognitionException {
        QueryParser.f_return retval = new QueryParser.f_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal75=null;

        CommonTree string_literal75_tree=null;

        try {
            // src/reimann/Query.g:64:3: ( 'false' )
            // src/reimann/Query.g:64:5: 'false'
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal75=(Token)match(input,26,FOLLOW_26_in_f496); 
            string_literal75_tree = (CommonTree)adaptor.create(string_literal75);
            adaptor.addChild(root_0, string_literal75_tree);


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
    // src/reimann/Query.g:65:1: nil : ( 'null' | 'nil' );
    public final QueryParser.nil_return nil() throws RecognitionException {
        QueryParser.nil_return retval = new QueryParser.nil_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set76=null;

        CommonTree set76_tree=null;

        try {
            // src/reimann/Query.g:65:5: ( 'null' | 'nil' )
            // src/reimann/Query.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set76=(Token)input.LT(1);
            if ( (input.LA(1)>=27 && input.LA(1)<=28) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set76));
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
    // src/reimann/Query.g:67:1: field : ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'time' ) ;
    public final QueryParser.field_return field() throws RecognitionException {
        QueryParser.field_return retval = new QueryParser.field_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set77=null;

        CommonTree set77_tree=null;

        try {
            // src/reimann/Query.g:67:7: ( ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'time' ) )
            // src/reimann/Query.g:67:9: ( 'host' | 'service' | 'state' | 'description' | 'metric_f' | 'time' )
            {
            root_0 = (CommonTree)adaptor.nil();

            set77=(Token)input.LT(1);
            if ( (input.LA(1)>=29 && input.LA(1)<=34) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set77));
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
        "\1\30\1\16\2\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA8_specialS =
        "\4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\3\1\2\10\uffff\1\1\11\uffff\1\2",
            "\1\3\1\2\10\uffff\1\1",
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
            return "()* loopback of 28:23: ( ( WS )* AND ( WS )* ( not | primary ) )*";
        }
    }
    static final String DFA12_eotS =
        "\15\uffff";
    static final String DFA12_eofS =
        "\15\uffff";
    static final String DFA12_minS =
        "\1\31\3\uffff\2\7\7\uffff";
    static final String DFA12_maxS =
        "\1\42\3\uffff\2\16\7\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\2\uffff\1\10\1\4\1\11\1\6\1\7\1\12\1\5";
    static final String DFA12_specialS =
        "\15\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\1\1\2\2\3\6\4",
            "",
            "",
            "",
            "\1\7\1\10\1\13\1\14\1\11\1\12\1\6\1\5",
            "\1\7\1\10\1\13\1\14\1\11\1\12\1\6\1\5",
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
            return "39:10: ( t | f | nil | approximately | lesser | lesser_equal | greater | greater_equal | not_equal | equal )";
        }
    }
 

    public static final BitSet FOLLOW_or_in_expr129 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_expr131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_in_or144 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_WS_in_or147 = new BitSet(new long[]{0x0000000000004020L});
    public static final BitSet FOLLOW_OR_in_or150 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_WS_in_or153 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_and_in_or156 = new BitSet(new long[]{0x0000000000004022L});
    public static final BitSet FOLLOW_not_in_and167 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_primary_in_and171 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_WS_in_and175 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_AND_in_and178 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_WS_in_and181 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_not_in_and185 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_primary_in_and189 = new BitSet(new long[]{0x0000000000004012L});
    public static final BitSet FOLLOW_NOT_in_not200 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_WS_in_not203 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_not_in_not207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primary_in_not211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_primary228 = new BitSet(new long[]{0x00000007FE804040L});
    public static final BitSet FOLLOW_or_in_primary230 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_primary232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_in_primary246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_simple266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_simple270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_simple274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_approximately_in_simple280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_in_simple286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lesser_equal_in_simple292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_in_simple298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_greater_equal_in_simple304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_not_equal_in_simple310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_equal_in_simple316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_approximately326 = new BitSet(new long[]{0x0000000000004080L});
    public static final BitSet FOLLOW_WS_in_approximately328 = new BitSet(new long[]{0x0000000000004080L});
    public static final BitSet FOLLOW_APPROXIMATELY_in_approximately331 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_approximately334 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_approximately337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser344 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_WS_in_lesser346 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_LESSER_in_lesser349 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_lesser352 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_lesser355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_lesser_equal363 = new BitSet(new long[]{0x0000000000004800L});
    public static final BitSet FOLLOW_WS_in_lesser_equal365 = new BitSet(new long[]{0x0000000000004800L});
    public static final BitSet FOLLOW_LESSER_EQUAL_in_lesser_equal368 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_lesser_equal371 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_lesser_equal374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater381 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_WS_in_greater383 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_GREATER_in_greater386 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_greater389 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_greater392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_greater_equal400 = new BitSet(new long[]{0x0000000000006000L});
    public static final BitSet FOLLOW_WS_in_greater_equal402 = new BitSet(new long[]{0x0000000000006000L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_greater_equal405 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_greater_equal408 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_greater_equal411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_not_equal419 = new BitSet(new long[]{0x0000000000004100L});
    public static final BitSet FOLLOW_WS_in_not_equal421 = new BitSet(new long[]{0x0000000000004100L});
    public static final BitSet FOLLOW_NOT_EQUAL_in_not_equal424 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_not_equal427 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_not_equal430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_equal438 = new BitSet(new long[]{0x0000000000004200L});
    public static final BitSet FOLLOW_WS_in_equal440 = new BitSet(new long[]{0x0000000000004200L});
    public static final BitSet FOLLOW_EQUAL_in_equal443 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_WS_in_equal446 = new BitSet(new long[]{0x000000001E03C000L});
    public static final BitSet FOLLOW_value_in_equal449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_value459 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_t_in_value463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_f_in_value467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nil_in_value471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_value475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_value479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_t489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_f496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nil0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_field515 = new BitSet(new long[]{0x0000000000000002L});

}