/* The scanner definition for the IEC 61131-3 ST language */
/* Author: cengic */	

package org.supremica.functionblocks.model.interpreters.st;

import org.supremica.functionblocks.model.interpreters.st.Symbols;
import java_cup.runtime.Symbol;

%%

%class Lexer
%public

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

  "OR"			{ /*System.out.println("OR");*/ return new Symbol(Symbols.OR); }
  "XOR"			{ /*System.out.println("XOR");*/ return new Symbol(Symbols.XOR); }
  "&"			{ /*System.out.println("AND");*/ return new Symbol(Symbols.AND); }
  "AND"			{ /*System.out.println("AND");*/ return new Symbol(Symbols.AND); }	

  "=" 			{ /*System.out.println("EQ");*/ return new Symbol(Symbols.EQ); }
  "<>" 			{ /*System.out.println("NEQ");*/ return new Symbol(Symbols.NEQ); }

  "<"  			{ /*System.out.println("LESS");*/ return new Symbol(Symbols.LESS); }
  "<=" 			{ /*System.out.println("LESSEQ");*/ return new Symbol(Symbols.LESSEQ); }
  ">"  			{ /*System.out.println("MORE");*/ return new Symbol(Symbols.MORE); }
  ">=" 			{ /*System.out.println("MOREEQ");*/ return new Symbol(Symbols.MOREEQ); }

  "+" 			{ /*System.out.println("PLUS");*/ return new Symbol(Symbols.PLUS); }
  "-" 			{ /*System.out.println("MINUS");*/ return new Symbol(Symbols.MINUS); }

  "*" 			{ /*System.out.println("TIMES");*/ return new Symbol(Symbols.TIMES); }
  "/" 			{ /*System.out.println("DIV");*/ return new Symbol(Symbols.DIV); }
  "MOD"			{ /*System.out.println("MOD");*/ return new Symbol(Symbols.MOD); }

  "NOT"			{ /*System.out.println("NOT");*/ return new Symbol(Symbols.NOT); }

  "(" 			{ /*System.out.println("LPAREN");*/ return new Symbol(Symbols.LPAREN); }
  ")" 			{ /*System.out.println("RPAREN");*/ return new Symbol(Symbols.RPAREN); }

  \"{STRING_TEXT}\" 	{ /*System.out.println("STRING:" + yytext());*/ return new Symbol(Symbols.STRING,yytext().substring(1,yylength()-1)); } 

  {DIGIT}+ 		{ /*System.out.println("INT:" + yytext());*/ return new Symbol(Symbols.INT , new Integer(yytext())); }  

{DIGIT}+"."{DIGIT}+	{ /*System.out.println("DOUBLE:" + yytext());*/ return new Symbol(Symbols.DOUBLE , new Double(yytext())); }
{DIGIT}+"."{DIGIT}+"e"{DIGIT}+ { /*System.out.println("DOUBLE:" + yytext());*/ return new Symbol(Symbols.DOUBLE , new Double(yytext())); }

{DIGIT}+"."{DIGIT}+"F" 	{ /*System.out.println("FLOAT:" + yytext());*/ return new Symbol(Symbols.FLOAT , new Float(yytext())); }

  "TRUE"		{ /*System.out.println("TRUE");*/ return new Symbol(Symbols.BOOL,new Boolean(true)); }
  "FALSE"		{ /*System.out.println("FALSE");*/ return new Symbol(Symbols.BOOL,new Boolean(false)); }

  {IDENT} 		{ /*System.out.println("IDENT:" + yytext());*/ return new Symbol(Symbols.IDENT,yytext()); }  

  {NEWLINE} 		{ }

  {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

  . 			{ System.out.println("Illegal character: <" + yytext() + ">"); }

}

<COMMENT> {
  "(*" 			{ comment_count++; }
  "*)" 			{ if (--comment_count == 0) yybegin(YYINITIAL); }
  {COMMENT_TEXT} 	{ }
}
