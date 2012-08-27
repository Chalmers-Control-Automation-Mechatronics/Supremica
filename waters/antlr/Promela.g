// ANTLR3 Grammar for Promela
// (c) 2009, Benjamin Zeiss <zeiss@cs.uni-goettingen.de>
//
// Licensed under the Eclipse Public License (EPL)

grammar Promela;

options {
    output=AST;
    ASTLabelType=CommonTree;
    backtrack=true; 
    memoize=true;
}

/*
tokens {
	PROCTYPENODE = 'proctype-definition';
	//INLINENODE = 'inline-definition';
	INITNODE = 'init-definition';
	//NEVERNODE = 'never-definition';
	//TRACENODE = 'trace-definition';
  	//NOTRACENODE = 'notrace-definition';
	//UTYPENODE = 'utype-definition';
	//MTYPENODE = 'mtype-definition';
	//DECLARATIONSNODE = 'declarations';
	NAMEDEFNODE = 'name';
	CONFIGNODE = 'config';
	ARGUMENTSNODE = 'arguments';
	BEHAVIORNODE = 'behavior';
	STATEMENTNODE = 'statement';
	GUARDNODE = 'guard';
}
*/
tokens{
	PROCTYPENODE;
	INITNODE;
	TYPE;
	STATEMENT;
	MODULE_ROOT = 'Root';
	CONDITION;
	REC;
	SEN;
	MESSAGE_ARGUMENTS;
	PROCSTATE=	'proc_state';
	CHANSTATE=	'chan_state';
	VARDEFINITION;
	SEMI;
}

@header {
package net.sourceforge.waters.external.promela.parser;


import net.sourceforge.waters.external.promela.*;
import net.sourceforge.waters.external.promela.ast.*;

}

@lexer::header {
package net.sourceforge.waters.external.promela.parser;

import net.sourceforge.waters.external.promela.PromelaParserError;
}

@members {
  private ArrayList<PromelaParserError> errors = new ArrayList<PromelaParserError>();
  private Stack paraphrases = new Stack();
  private PromelaMType mMType = new PromelaMType("mtype");
  
  public ArrayList<PromelaParserError> getErrors() {
    return errors;
  }

  public void reportError(RecognitionException e) {
    String msg = super.getErrorMessage(e, tokenNames); 
    if (paraphrases.size() > 0) {
      String paraphrase = (String) paraphrases.peek();
      msg = msg + " " + paraphrase;
    }

    PromelaParserError error = new PromelaParserError(e.line, e.charPositionInLine, msg, true);

    if (!errors.contains(error))
      errors.add(error);
  }

  public boolean isSyntacticallyCorrect() {
    return errors.size() == 0;
  }
  
}

// ----------------------------------------------------------------------------------------
// Parser Rules
// ----------------------------------------------------------------------------------------

specRule
@init  { paraphrases.push("in specification"); }
@after { paraphrases.pop(); }
	:	moduleRule+ EOF
		-> ^(MODULE_ROOT<ModuleTreeNode>[mMType] moduleRule*)
	;

moduleRule
@init  { paraphrases.push("in module definition"); }
@after { paraphrases.pop(); }
	:	proctypeRule //-> ^(PROCTYPENODE proctypeRule)
		| inlineRule// -> ^(INLINENODE inlineRule)
		| initRule //-> ^(INITNODE initRule)
		| neverRule //-> ^(NEVERNODE neverRule)
		| traceRule //-> ^(TRACENODE traceRule)
    		| notraceRule //-> ^(NOTRACENODE notraceRule)
		| utypeRule //-> ^(UTYPENODE utypeRule)
		| mtypeRule //-> ^(MTYPENODE mtypeRule)
		| decl_lstRule //-> ^(DECLARATIONSNODE decl_lstRule)
		| channelRule
    | CSTATE STRING STRING (STRING)?
    | CTRACK STRING STRING
    | CCODEBLOCK
    | CCODEASSERTBLOCK
    | CEXPRBLOCK
    | CEXPRASSERTBLOCK
	;

traceRule
@init  { paraphrases.push("in trace block"); }
@after { paraphrases.pop(); }
	:	TRACE BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
	;

notraceRule
@init  { paraphrases.push("in notrace block"); }
@after { paraphrases.pop(); }
  : NOTRACE BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
  ;

neverRule
@init  { paraphrases.push("in never block"); }
@after { paraphrases.pop(); }
	:	NEVER BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
	;
//modified
proctypeRule
@init  { paraphrases.push("in proctype"); }
@after { paraphrases.pop(); }
	:	(activeRule)? PROCTYPE NAME PARENOPEN (decl_lstRule)? PARENCLOSE
		(priorityRule)? 
		(enablerRule)? 
		BLOCKBEGIN sequenceRule BLOCKEND 
		(SEMICOLON)* 
		-> ^(NAME<ProctypeTreeNode> NAME<NameTreeNode> ^(PROCTYPE<ProctypeStatementTreeNode> sequenceRule))
    ;

inlineRule
@init  { paraphrases.push("in inline definition"); }
@after { paraphrases.pop(); }
	:	INLINE NAME PARENOPEN (ivarRule (COMMA ivarRule)*)? PARENCLOSE BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
    ;

enablerRule
	:	PROVIDED PARENOPEN exprRule PARENCLOSE
	;

activeRule
	:	ACTIVE (ALTPARENOPEN constRule ALTPARENCLOSE)?
	;

mtypeRule
@init  { paraphrases.push("in mtype definition"); }
@after { paraphrases.pop(); }
	:	MTYPE (ASSIGN)? BLOCKBEGIN NAME (COMMA NAME)* BLOCKEND (SEMICOLON)*
	-> ^(MTYPE<TypeTreeNode> (NAME<NameTreeNode>)*  )
	;

utypeRule
@init  { paraphrases.push("in user type definition"); }
@after { paraphrases.pop(); }
	:	TYPEDEF NAME BLOCKBEGIN decl_lstRule BLOCKEND (SEMICOLON)* 
	;
//modified
initRule
@init  { paraphrases.push("in init definition"); }
@after { paraphrases.pop(); }
	:	INIT (priorityRule)? BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
		-> ^(INIT<InitialTreeNode> (priorityRule)? sequenceRule)
	;

priorityRule
	:	PRIORITY constRule
	;


//sequenceRule
//	:	// original: stepRule (';' stepRule)*
//		(stepRule (';' | '-' '>')? )*
//		(stepRule (SEMICOLON)* (isguard=ARROW )? )*
//		->(stepRule)*
//;

sequenceRule
    :   stepRule (SEMICOLON)* (isguard=ARROW )?
        (
        -> stepRule
        |
        ( stepRule (SEMICOLON)* (isguard=ARROW )? )+
        -> ^(STATEMENT<SemicolonTreeNode> (stepRule)*)
        )
 //       | stepRule (SEMICOLON)* (isguard=ARROW) (stepRule (SEMICOLON)* (isguard=ARROW)?)+
 //       -> ^(STATEMENT<DoConditionTreeNode> (stepRule)*)
    ;
stepRule 
	:	decl_lstRule
		| stmntRule (UNLESS stmntRule)?
    		| XR varrefRule (COMMA varrefRule)*
    		| XS varrefRule (COMMA varrefRule)*
	;

varrefRule
	:	NAME (ALTPARENOPEN any_exprRule ALTPARENCLOSE)? (DOT varrefRule)?
		-> ^(NAME<NameTreeNode> (any_exprRule)? (varrefRule)?)
	;
//modified
stmntRule
@init  { paraphrases.push("in statement"); }
@after { paraphrases.pop(); }
	:	    IF optionsRule FI
		-> ^(IF<ConditionTreeNode> optionsRule)
        | DO optionsRule OD
		-> ^(DO<DoConditionTreeNode> optionsRule)
        | ATOMIC BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
		->^(ATOMIC<InitialStatementTreeNode>  sequenceRule )		

        | DSTEP BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
        | BLOCKBEGIN sequenceRule BLOCKEND (SEMICOLON)*
        ->^(SEMI<SemicolonTreeNode> sequenceRule)
        | sendRule
        | receiveRule
        | assignRule
        | ELSE
        | BREAK -> ^(BREAK<BreakStatementTreeNode>)
        | GOTO NAME -> ^(NAME<GotoTreeNode>)
        | NAME COLON stmntRule -> ^(NAME<LabelTreeNode> stmntRule)
        | (PRINT|PRINTF) PARENOPEN STRING (COMMA arg_lstRule)? PARENCLOSE
		| NAME PARENOPEN arg_lstRule PARENCLOSE
        | ASSERT exprRule
        | exprRule
        | CCODEBLOCK
        | CCODEASSERTBLOCK
        | CEXPRBLOCK
        | CEXPRASSERTBLOCK
	;

/* Old version
 * assignRule
 * @init  { paraphrases.push("in assignment"); }
 * @after { paraphrases.pop(); }
 * 	:	varrefRule (ASSIGN any_exprRule | PLUSPLUS | MINUSMINUS)
 * 	;
 */

//New version -Ethan
assignRule
@init  { paraphrases.push("in assignment"); }
@after { paraphrases.pop(); }
  : varrefRule ASSIGN any_exprRule -> ^(ASSIGN<BinaryStatementTreeNode>["="] varrefRule any_exprRule)
  | varrefRule PLUSPLUS -> ^(PLUSPLUS<UnaryStatementTreeNode>["++"] varrefRule)
  | varrefRule MINUSMINUS -> ^(MINUSMINUS<UnaryStatementTreeNode>["--"] varrefRule)
  ;
  
  
//modified
receiveRule
@init  { paraphrases.push("in receive statement"); }
@after { paraphrases.pop(); }

  : 	varrefRule ((QUESTIONMARK) ( recv_argsRule))
    	-> ^(QUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule) 
 	| varrefRule ((QUESTIONMARK) ( ALTPARENOPEN recv_argsRule ALTPARENCLOSE))
	-> ^(QUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule)
	| varrefRule ((QUESTIONMARK) ( LESS recv_argsRule MORE))
	-> ^(QUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule)	
	
   	| varrefRule ((DOUBLEQUESTIONMARK) ( recv_argsRule))
	-> ^(DOUBLEQUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule) 
	| varrefRule ((DOUBLEQUESTIONMARK) (ALTPARENOPEN recv_argsRule ALTPARENCLOSE))
	-> ^(DOUBLEQUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule) 
	| varrefRule ((DOUBLEQUESTIONMARK) (LESS recv_argsRule MORE))
	-> ^(DOUBLEQUESTIONMARK<ReceiveTreeNode> varrefRule recv_argsRule) 
  ;

//modified
recv_argsRule
@init  { paraphrases.push("in receive arguments"); }
@after { paraphrases.pop(); }
	: recv_argRule (PARENOPEN recv_argListRule PARENCLOSE) 
		-> ^(MESSAGE_ARGUMENTS<MsgTreeNode> recv_argRule recv_argListRule)
  	| recv_argRule (COMMA recv_argRule)*
		-> ^(MESSAGE_ARGUMENTS<MsgTreeNode> recv_argRule recv_argRule*)
//	->  ^(recv_argRule ivarRule)
    ;

recv_argListRule
@init  { paraphrases.push("in receive argument"); }
@after { paraphrases.pop(); }
	:	recv_argRule (COMMA recv_argRule)*
		-> (recv_argRule)+
	;


recv_argRule
@init  { paraphrases.push("in receive argument"); }
@after { paraphrases.pop(); }
	:  varrefRule
        | (EVAL PARENOPEN varrefRule PARENCLOSE)
        | ((MINUS)? constRule)
	;
//modified
sendRule
@init  { paraphrases.push("in send statement"); }
@after { paraphrases.pop(); }
	:	varrefRule 
	    EXCLAMATIONMARK 
	    send_argsRule
	-> ^(EXCLAMATIONMARK<SendTreeNode> varrefRule send_argsRule)
	| varrefRule DOUBLEEXCLAMATIONMARK send_argsRule
	-> ^(DOUBLEEXCLAMATIONMARK<SendTreeNode> varrefRule send_argsRule)
	
  ;


//modified
send_argsRule
@init  { paraphrases.push("in send arguments"); }
@after { paraphrases.pop(); }
	:	(any_exprRule PARENOPEN arg_lstRule PARENCLOSE) 
		-> ^(MESSAGE_ARGUMENTS<MsgTreeNode> any_exprRule arg_lstRule)
	    | arg_lstRule
    ;

arg_lstRule
@init  { paraphrases.push("in send argument"); }
@after { paraphrases.pop(); }
	:	any_exprRule (COMMA any_exprRule)*
		-> (any_exprRule)+
	;

decl_lstRule
@init  { paraphrases.push("in declaration list"); }
@after { paraphrases.pop(); }
	:	// original: one_declRule (';' one_declRule)*
		(one_declRule (SEMICOLON)* )+
		->(one_declRule)*
	;

//modified
one_declRule
@init  { paraphrases.push("in declaration"); boolean visible = true; PromelaType type=null;}
@after { paraphrases.pop(); }
	:	(visibleRule {visible = $visibleRule.visible;})? typenameRule {type = $typenameRule.type;} ivarRule (COMMA ivarRule)*
		-> ^(VARDEFINITION<VardefTreeNode>[visible, type] ivarRule+)
	;

optionsRule
@init  { paraphrases.push("in option"); }
@after { paraphrases.pop(); }
	:	COLONCOLON sequenceRule (COLONCOLON sequenceRule)*
		-> (sequenceRule)+
		//->  (sequenceRule sequenceRule*)
		//-> ^(CONDITION<ConditionTreeNode> sequenceRule sequenceRule*)
	;

ivarRule
	:	NAME (ALTPARENOPEN constRule ALTPARENCLOSE)? (ASSIGN (any_exprRule) )?
		-> ^(NAME<NameTreeNode> (constRule)? (any_exprRule)? )  //(ch_initRule)?
	//	| (ASSIGN (any_exprRule))?
	//|	NAME ASSIGN ALTPARENOPEN constRule ALTPARENCLOSE OF BLOCKBEGIN typeRule (COMMA typeRule)* BLOCKEND (SEMICOLON)*
	//	-> ^(NAME STATEMENT constRule typeRule*)
	;

/* Original version
any_exprRule
	:	'(' any_exprRule ')'
        | any_exprRule binaropRule any_exprRule
        | unaropRule any_exprRule
        | '(' any_exprRule '-' '>' any_exprRule ':' any_exprRule ')'
        | 'len' '(' varrefRule ')'
        | pollRule
        | varrefRule
        | constRule
        | 'timeout'
        | 'np_'
        | 'enabled' '(' any_exprRule ')'
        | 'pc_value' '(' any_exprRule ')'~
        | NAME '[' any_exprRule ']' '@' NAME
        | 'run' NAME '(' (arg_lstRule)? ')' (priorityRule)?
	;
*/
// changed a lot, very strange, coz Run A() is found here
/* Second version
any_exprRule 
@init  { paraphrases.push("in any expression"); }
@after { paraphrases.pop(); }
	:	(PARENOPEN (any_exprRule) PARENCLOSE ) (binaropRule any_exprRule)* 
	  | PARENOPEN PARENCLOSE 
    | varrefRule AT varrefRule
	  | unaropRule any_exprRule 
    | (unaropRule)? PARENOPEN receiveRule PARENCLOSE 
	  | PARENOPEN any_exprRule ARROW any_exprRule COLON any_exprRule PARENCLOSE 
	  | LEN PARENOPEN varrefRule PARENCLOSE 
	  | varrefRule 
	  | pollRule 
	  | constRule 
	  | TIMEOUT 
	  | NP 
	  | (ENABLED|PCVALUE) PARENOPEN any_exprRule PARENCLOSE 
	  | varrefRule ALTPARENOPEN any_exprRule ALTPARENCLOSE AT varrefRule
	  | RUN NAME PARENOPEN (arg_lstRule)? PARENCLOSE (priorityRule)? 
		-> ^(RUN<RunTreeNode> NAME<NameTreeNode> (arg_lstRule)? (priorityRule)?)
	;
*/
/*
//Current version -Ethan
any_exprRule
@init  { paraphrases.push("in any expression"); }
@after { paraphrases.pop(); }
  : RUN NAME PARENOPEN (arg_lstRule)? PARENCLOSE (priorityRule)? 
    -> ^(RUN<RunTreeNode> NAME<NameTreeNode> (arg_lstRule)? (priorityRule)?)
  | a_exprRule
  ;

//Lowest level, has a -> b : c
a_exprRule
  : PARENOPEN b_exprRule ARROW b_exprRule COLON any_exprRule PARENCLOSE
  | b_exprRule
  ;
*/
//Second level, has logical or
any_exprRule
  : c_exprRule (OROR c_exprRule)* -> c_exprRule (^(OROR<BinaryStatementTreeNode>["||"]) c_exprRule)*
  ;

//Third level, has logical and
c_exprRule
  : d_exprRule (ANDAND d_exprRule)* -> d_exprRule (^(ANDAND<BinaryStatementTreeNode>["&&"]) d_exprRule)*
  ;

//Fourth level, has bitwise or
d_exprRule
  : e_exprRule (OR e_exprRule)* -> e_exprRule (^(OR<BinaryStatementTreeNode>["|"]) e_exprRule)*
  ;

//Fifth level, has bitwise exclusive or
e_exprRule
  : f_exprRule (DACH f_exprRule)* -> f_exprRule (^(DACH<BinaryStatementTreeNode>["^"]) f_exprRule)*
  ;

//Sixth level, has bitwise and
f_exprRule
  : g_exprRule (AND g_exprRule)* -> g_exprRule (^(AND<BinaryStatementTreeNode>["&"]) g_exprRule)*
  ;

//Seventh level, has equals and not equals
g_exprRule
  : h_exprRule (e_ne_Rule h_exprRule)* -> h_exprRule (^(e_ne_Rule) h_exprRule)*
  ;
 
e_ne_Rule
  : EQUALS -> ^(EQUALS<BinaryStatementTreeNode>["=="])
  | NOTEQUALS -> ^(NOTEQUALS<BinaryStatementTreeNode>["!="])
  ;

//Eighth level, has <, >, <=, >=
h_exprRule
  : i_exprRule (l_m_lt_gt_Rule i_exprRule)* -> i_exprRule (^(l_m_lt_gt_Rule) i_exprRule)*
  ;

l_m_lt_gt_Rule
  : LESS -> ^(LESS<BinaryStatementTreeNode>["<"])
  | MORE -> ^(MORE<BinaryStatementTreeNode>[">"])
  | LESSTHAN -> ^(LESSTHAN<BinaryStatementTreeNode>["<="])
  | GREATERTHAN -> ^(GREATERTHAN<BinaryStatementTreeNode>[">="])
  ;

//Ninth level, has bit shifts (<< and >>)
//TODO
i_exprRule
  : j_exprRule
  ;

//Tenth level, has plus and minus
j_exprRule
  : k_exprRule ((PLUS | MINUS) k_exprRule)*
  ;

//Eleventh level, has times, divide and modulus
k_exprRule
  : l_exprRule (s_s_p_Rule l_exprRule)* -> l_exprRule (^(s_s_p_Rule) l_exprRule)*
  ;

s_s_p_Rule
  : STAR -> ^(STAR<BinaryStatementTreeNode>["*"])
  | SLASH -> ^(SLASH<BinaryStatementTreeNode>["/"])
  | PERCENT -> ^(PERCENT<BinaryStatementTreeNode>["modulus"])
  ;
//Didn't like the string "%" for some reason

//Twelfth level, has negate (boolean), complement and negative(integer)
//I think that the exclamation mark is mucking up the send on channel  tries to match !statement instead of ch!statement
l_exprRule
  : EXCLAMATIONMARK m_exprRule -> ^(EXCLAMATIONMARK<UnaryStatementTreeNode>["!"] m_exprRule)
  | TILDE m_exprRule -> ^(TILDE<UnaryStatementTreeNode>["~"] m_exprRule)
  | MINUS m_exprRule -> ^(MINUS<UnaryStatementTreeNode>["-"] m_exprRule)
  | m_exprRule
  ;

//Last level, has brackets, variables and constants
//TODO add in array access here | varrefRule ALTPARENOPEN any_exprRule ALTPARENCLOSE AT varrefRule
m_exprRule
  : PARENOPEN any_exprRule PARENCLOSE -> ^(any_exprRule)
  | PARENOPEN any_exprRule ARROW any_exprRule COLON any_exprRule PARENCLOSE
  | RUN NAME PARENOPEN (arg_lstRule)? PARENCLOSE (priorityRule)? 
    -> ^(RUN<RunTreeNode> NAME<NameTreeNode> (arg_lstRule)? (priorityRule)?)
  | constRule
  | varrefRule
  | LEN PARENOPEN varrefRule PARENCLOSE
  | TIMEOUT 
  | NP 
  | (ENABLED|PCVALUE) PARENOPEN any_exprRule PARENCLOSE
  ;
/*
: (PARENOPEN (any_exprRule) PARENCLOSE ) (binaropRule any_exprRule)* 
    | PARENOPEN PARENCLOSE 
    | varrefRule AT varrefRule
    | unaropRule any_exprRule 
    | (unaropRule)? PARENOPEN receiveRule PARENCLOSE 
    | PARENOPEN any_exprRule ARROW any_exprRule COLON any_exprRule PARENCLOSE 
    | LEN PARENOPEN varrefRule PARENCLOSE 
    | varrefRule 
    | pollRule 
    | constRule 
    | TIMEOUT 
    | NP 
    | (ENABLED|PCVALUE) PARENOPEN any_exprRule PARENCLOSE 
    | varrefRule ALTPARENOPEN any_exprRule ALTPARENCLOSE AT varrefRule
    | RUN NAME PARENOPEN (arg_lstRule)? PARENCLOSE (priorityRule)? 
    -> ^(RUN<RunTreeNode> NAME<NameTreeNode> (arg_lstRule)? (priorityRule)?)
*/
pollRule
@init  { paraphrases.push("in poll statement"); }
@after { paraphrases.pop(); }
	:	varrefRule QUESTIONMARK (ALTPARENOPEN recv_argsRule ALTPARENCLOSE | QUESTIONMARK ALTPARENOPEN recv_argsRule ALTPARENCLOSE)
	;
//modified
//ch_initRule
//	:	ALTPARENOPEN constRule ALTPARENCLOSE OF BLOCKBEGIN typenameRule (COMMA typenameRule)* BLOCKEND (SEMICOLON)*
//		-> ^(STATEMENT constRule typenameRule* )
//	;
	
typenameRule returns [PromelaType type]
	:	BIT {$type = PromelaIntRange.BIT;}
	| BOOL {$type = PromelaIntRange.BIT;}
	| BYTE {$type = PromelaIntRange.BYTE;}
	| SHORT {$type = PromelaIntRange.SHORT;}
	| INT {$type = PromelaIntRange.INT;}
	| MTYPE {$type = mMType;}
	| unameRule
		
	;
typeRule
	: 	CHAN
	;

channelRule
@init  { paraphrases.push("in channel definition"); }
@after { paraphrases.pop(); }
	:	CHAN NAME (ASSIGN)?  ALTPARENOPEN constRule ALTPARENCLOSE OF BLOCKBEGIN typenameRule (COMMA typenameRule)* BLOCKEND (SEMICOLON)*
		-> ^(CHAN<ChannelTreeNode> NAME<NameTreeNode> ^( CHAN<ChannelStatementTreeNode> constRule typenameRule*) )
	;													 


unameRule
	:	NAME
	;
	
visibleRule returns [boolean visible]
	:	HIDDEN { $visible = false; }
	   | SHOW { $visible = true; }
	;
	
constRule
	:	TRUE | FALSE | SKIP -> ^(SKIP<SkipTreeNode>)
		| NUMBER -> ^(NUMBER <ConstantTreeNode>)
		| CHARLITERAL
	;

/*
exprRule
	:	any_exprRule
        | '(' exprRule ')'
        | exprRule andorRule exprRule
        | chanpollRule '(' varrefRule ')'
    ;
*/

exprRule // original above, left recursion automatically removed with antlrworks
@init  { paraphrases.push("in expression"); }
@after { paraphrases.pop(); }
	:	(
      any_exprRule 
      | (PARENOPEN exprRule PARENCLOSE)
      | (chanpollRule PARENOPEN varrefRule PARENCLOSE)
    ) 
    (andorRule exprRule)*
    ;

andorRule
	:	ANDAND | OROR
	;

chanpollRule
	:	FULL | EMPTY | NFULL | NEMPTY
	;

binaropRule
	:	(PLUS | MINUS | STAR | SLASH | PERCENT | AND | DACH | OR
        | MORE | LESS | GREATERTHAN | LESSTHAN | EQUALS | NOTEQUALS
        | LESSLESS | GREATERGREATER | andorRule)
    ;

unaropRule
	:	TILDE | MINUS | EXCLAMATIONMARK
	;

// ----------------------------------------------------------------------------------------
// Lexer Rules
// ----------------------------------------------------------------------------------------

NEWLINE:'\r'? '\n' {skip();};
WS: (' '|'\t'|'\n'|'\r')+ {skip();} ;
fragment WSNOSKIP: (' '|'\t'|'\n'|'\r')*;
ARROW:	'->';
OROR:	'||';
ANDAND:	'&&';
LESSTHAN:	'<=';
GREATERTHAN:	'>=';
EQUALS:	'==';
NOTEQUALS:	'!=';
LESSLESS:	'<<';
GREATERGREATER:	'>>';
COLONCOLON:	'::';
PLUSPLUS:	'++';
MINUSMINUS:	'--';
INLINE:	'inline';
PROVIDED:	'provided';
PROCTYPE:	'proctype';
NEVER:	'never';
FULL:	'full';
EMPTY:	'empty';
NFULL:	'nfull';
NEMPTY:	'nempty';
TRUE:	'true';
FALSE:	'false';
SKIP:	'skip';
HIDDEN:	'hidden';
SHOW:	'show';
BIT	:	'bit';
BOOL:	'bool';
BYTE:	'byte';
SHORT:	 'short';
INT:	'int';
MTYPE:	 'mtype';
CHAN:	'chan';
OF:		'of';	
RUN:	'run';
ACTIVE:	'active';
PCVALUE:	'pc_value';
LEN:	'len';
TIMEOUT:	'timeout';
NP:	'np_';
ENABLED:	'enabled';
EVAL:	'eval';
ATOMIC:	'atomic';
DSTEP:	'd_step';
ELSE:	'else';
BREAK:	'break';
GOTO:	'goto';
PRINT:	'print';
PRINTF:	'printf';
ASSERT:	'assert';
UNLESS:	'unless';
fragment CCODE:  'c_code';
fragment CEXPR:  'c_expr';
fragment CASSERT: 'c_assert';
CCODEBLOCK: CCODE WSNOSKIP NESTED_ACTION;
CCODEASSERTBLOCK: CCODE WSNOSKIP CASSERT WSNOSKIP NESTED_ACTION;
CEXPRBLOCK: CEXPR WSNOSKIP NESTED_ACTION;
CEXPRASSERTBLOCK: CEXPR WSNOSKIP CASSERT WSNOSKIP NESTED_ACTION;
CDECL:  'c_decl';
CSTATE: 'c_state';
CTRACK: 'c_track';
IF	:	'if';
FI	:	'fi';
DO	:	'do';
OD	:	'od';
XR	:	'xr';
XS	:	'xs';
INIT:	'init';
PRIORITY:	'priority';
TYPEDEF:	'typedef';
TRACE:		'trace';
NOTRACE:    'notrace';
DOUBLEEXCLAMATIONMARK:  '!!';
DOUBLEQUESTIONMARK: '??';
QUESTIONMARK:	'?';
DOT	:	'.';
ASSIGN:	'=';
COMMA:	',';
PLUS:	'+';
MINUS:	'-';
STAR:	'*';
SLASH:	'/';
OR:		'|';
DACH:	'^'; // what the hell is this called?
PERCENT: '%';	
AND:	'&';
LESS:	'<';
MORE:	'>';
TILDE:	'~';
EXCLAMATIONMARK:	'!';
PARENOPEN:	'(';
PARENCLOSE:	')';
ALTPARENOPEN:	'[';
ALTPARENCLOSE:	']';
AT:	'@';
COLON:	':';
SEMICOLON:	';';
NAME: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;
NUMBER: '0'..'9'+ ;
STRING:	('"' ~('"')* '"');
CHARLITERAL: ('\'' ~('\'')* '\'');


COMMENT
	:	'/*'
		(options {greedy=false;} : . )* 
		'*/'
            { skip(); }
	;

LINE_COMMENT
    :	'//' ~('\n'|'\r')*  ('\r\n' | '\r' | '\n') { skip(); }
    |	'//' ~('\n'|'\r')*	// a line comment could appear at the end of the file without CR/LF
		{ skip(); }
    ;

fragment
NESTED_ACTION:
    BLOCKBEGIN
    (   options {greedy=false; k=2; }
    :   NESTED_ACTION
    |   COMMENT
    |   LINE_COMMENT
    |   ACTION_STRING_LITERAL
    |   ACTION_CHAR_LITERAL
    | .
    )*
    BLOCKEND
   ;

fragment ACTION_CHAR_LITERAL
    :   
        '\'' (ACTION_ESC|~('\\'|'\'')) '\''
    ;

fragment ACTION_STRING_LITERAL
    :   
        '"' (ACTION_ESC|~('\\'|'"'))* '"'
    ;

fragment ACTION_ESC
    :   '\\\''
    |   '\\' '"' // ANTLR doesn't like: '\\"'
    |   '\\' ~('\''|'"')
    ;

BLOCKBEGIN: '{';
BLOCKEND:   '}';
    
