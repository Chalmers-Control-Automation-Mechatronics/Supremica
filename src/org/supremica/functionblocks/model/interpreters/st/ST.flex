/* The scanner definition for the IEC 61131-3 ST language */
/* Author: cengic */	

package org.supremica.functionblocks.model.interpreters.st;

import org.supremica.functionblocks.model.interpreters.st.Symbols;
import java_cup.runtime.Symbol;

%%

%class Lexer
//%public

%{
  private int comment_count = 0;
%} 

%full
%cupsym Symbols
%cup
%state COMMENT

ALPHA=[A-Za-z]
DIGIT=[0-9]
NONNEWLINE_WHITE_SPACE_CHAR=[\ \t\b\012]
NEWLINE=\r | \n | \r\n
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
STRING_TEXT=( \\\" | [^\n\r\"] | \\{WHITE_SPACE_CHAR}+\\ )*
COMMENT_TEXT=( [^*/\n] | [^*\n]"/"[^*\n] | [^/\n]"*"[^/\n] | "*"[^/\n] | "/"[^*\n] )*
IDENT = {ALPHA} ( {ALPHA} | {DIGIT} | _ )*

%% 

<YYINITIAL> {

  "OR"			{ return new Symbol(Symbols.OR); }
  "XOR"			{ return new Symbol(Symbols.XOR); }
  "&"			{ return new Symbol(Symbols.AND); }
  "AND"			{ return new Symbol(Symbols.AND); }	

  "=" 			{ return new Symbol(Symbols.EQ); }
  "<>" 			{ return new Symbol(Symbols.NEQ); }

  "<"  			{ return new Symbol(Symbols.LESS); }
  "<=" 			{ return new Symbol(Symbols.LESSEQ); }
  ">"  			{ return new Symbol(Symbols.MORE); }
  ">=" 			{ return new Symbol(Symbols.MOREEQ); }

  "+" 			{ return new Symbol(Symbols.PLUS); }
  "-" 			{ return new Symbol(Symbols.MINUS); }

  "*" 			{ return new Symbol(Symbols.TIMES); }
  "/" 			{ return new Symbol(Symbols.DIV); }
  "MOD"			{ return new Symbol(Symbols.MOD); }

  "NOT"			{ return new Symbol(Symbols.NOT); }

  "(" 			{ return new Symbol(Symbols.LPAREN); }
  ")" 			{ return new Symbol(Symbols.RPAREN); }

  \"{STRING_TEXT}\" 	{ return new Symbol(Symbols.STRING,yytext()); }
  
/*  \"{STRING_TEXT} 	{ return new Symbol(Symbols.OR); } */
  
  {DIGIT}+ 		{ return new Symbol(Symbols.NUMBER , new Integer(yytext())); }  

  "TRUE"		{ return new Symbol(Symbols.BOOL,new Boolean(true)); }
  "FALSE"		{ return new Symbol(Symbols.BOOL,new Boolean(false)); }

  {IDENT} 		{ return new Symbol(Symbols.IDENT,yytext()); }  

  {NEWLINE} 		{ }

  {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

  . 			{ System.out.println("Illegal character: <" + yytext() + ">");}

}

<COMMENT> {
  "(*" 			{ comment_count++; }
  "*)" 			{ if (--comment_count == 0) yybegin(YYINITIAL); }
  {COMMENT_TEXT} 	{ }
}
