/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
/*
 * @author Goran Cengic
 */
package org.supremica.functionblocks.model.interpreters.st;

import org.supremica.functionblocks.model.interpreters.st.abstractsyntax.*; 

public class Printer {

	java.io.PrintStream out;

	public Printer(java.io.PrintStream o) {out=o;}

	void indent(int d) 
	{
		for(int i=0; i<d; i++) 
			out.print(' ');
	}
	
	void say(String s) 
	{
		out.print(s);
	}
	
	void say(Integer i) 
	{
		out.print(i.toString());
	}
	
	void say(Boolean b) 
	{
		out.print(b.toString());
	}

	void say(Float f) 
	{
		out.print(f.toString());
	}

	void say(Double d) 
	{
		out.print(d.toString());
	}
	
	void sayln(String s) 
	{
		say(s); say("\n");
	}
	
	public void prExpression(Expression e, int d) 
	{
		//indent(d);
		prOrExpression(e.a, d);
	}

	public void prOrExpression(OrExpression e, int d) 
	{
		//indent(d);
		//sayln("OrExpression(");
		if (e instanceof BinaryOrExpression) 
		{
			prOrExpression(((BinaryOrExpression) e).a, d+2);
			indent(d+2);
			sayln("OR");
			prOrExpression(((BinaryOrExpression) e).b, d+2);
		}
		else if (e instanceof UnaryOrExpression)
		{
			prXorExpression(((UnaryOrExpression) e).a, d+2);		
		}
		//indent(d);
		//sayln(")");
	}

	public void prXorExpression(XorExpression e, int d) 
	{
		//indent(d);
		//sayln("XorExpression(");
		if (e instanceof BinaryXorExpression) 
		{
			prXorExpression(((BinaryXorExpression) e).a, d+2);
			prXorExpression(((BinaryXorExpression) e).b, d+2);
		}
		else if (e instanceof UnaryXorExpression)
		{
			prAndExpression(((UnaryXorExpression) e).a, d+2);		
		}
		//indent(d);
		//sayln(")");
	}

	public void prAndExpression(AndExpression e, int d) 
	{
		//indent(d);
		//sayln("AndExpression(");
		if (e instanceof BinaryAndExpression) 
		{
			prAndExpression(((BinaryAndExpression) e).a, d+2);
			prAndExpression(((BinaryAndExpression) e).b, d+2);
		}
		else if (e instanceof UnaryAndExpression)
		{
			prComparison(((UnaryAndExpression) e).a, d+2);		
		}
		//indent(d);
		//sayln(")");
	}

	public void prComparison(Comparison e, int d) 
	{
		//indent(d);
		//sayln("Comparison(");
		if (e instanceof Eq)
		{ 
			//indent(d+2);
			//sayln("Eq(");
			prAddExpression(((Eq) e).a, d+4);
			prAddExpression(((Eq) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof Neq)
		{ 
			//indent(d+2);
			//sayln("Neq(");
			prAddExpression(((Neq) e).a, d+4);
			prAddExpression(((Neq) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof Less)
		{ 
			//indent(d+2);
			//sayln("Less(");
			prAddExpression(((Less) e).a, d+4);
			prAddExpression(((Less) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof LessEq)
		{ 
			//indent(d+2);
			//sayln("LessEq(");
			prAddExpression(((LessEq) e).a, d+4);
			prAddExpression(((LessEq) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof More)
		{ 
			//indent(d+2);
			//sayln("More(");
			prAddExpression(((More) e).a, d+4);
			prAddExpression(((More) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof MoreEq)
		{ 
			//indent(d+2);
			//sayln("MoreEq(");
			prAddExpression(((MoreEq) e).a, d+4);
			prAddExpression(((MoreEq) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryComparison)
		{ 
			//indent(d+2);
			//sayln("UnaryComparison(");
			prAddExpression(((UnaryComparison) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

	public void prAddExpression(AddExpression e, int d) 
	{
		//indent(d);
		//sayln("AddExpression(");
		if (e instanceof Plus)
		{ 
			//indent(d+2);
			//sayln("Plus(");
			prAddExpression(((Plus) e).a, d+4);
			indent(d+2);
			sayln("+");
			prAddExpression(((Plus) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof Minus)
		{ 
			//indent(d+2);
			//sayln("Minus(");
			prAddExpression(((Minus) e).a, d+4);
			indent(d+2);
			sayln("-");
			prAddExpression(((Minus) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryAddExpression)
		{ 
			//indent(d+2);
			//sayln("UnaryAddExpression(");
			prTerm(((UnaryAddExpression) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

	public void prTerm(Term e, int d) 
	{
		//indent(d);
		//sayln("Term(");
		if (e instanceof Times)
		{ 
			//indent(d+2);
			//sayln("Times(");
			prTerm(((Times) e).a, d+4);
			prTerm(((Times) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof Div)
		{ 
			//indent(d+2);
			//sayln("Div(");
			prTerm(((Div) e).a, d+4);
			prTerm(((Div) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof Mod)
		{ 
			//indent(d+2);
			//sayln("Mod(");
			prTerm(((Mod) e).a, d+4);
			prTerm(((Mod) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryTerm)
		{ 
			//indent(d+2);
			//sayln("UnaryTerm(");
			prPowerExpression(((UnaryTerm) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

	public void prPowerExpression(PowerExpression e, int d) 
	{
		//indent(d);
		//sayln("PowerExpression(");
		if (e instanceof BinaryPowerExpression)
		{ 
			//indent(d+2);
			//sayln("BinaryPowerExpression(");
			prPowerExpression(((BinaryPowerExpression) e).a, d+4);
			prPowerExpression(((BinaryPowerExpression) e).b, d+4); 
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryPowerExpression)
		{ 
			//indent(d+2);
			//sayln("UnaryPowerExpression(");
			prUnaryExpression(((UnaryPowerExpression) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

	public void prUnaryExpression(UnaryExpression e, int d) 
	{
		//indent(d);
		//sayln("UnaryExpression(");
		if (e instanceof UnaryPrimaryExpression)
		{ 
			//indent(d+2);
			//sayln("UnaryPrimaryExpression(");
			prPrimaryExpression(((UnaryPrimaryExpression) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryExpressionExpression)
		{ 
			//indent(d+2);
			//sayln("UnaryExpressionExpression(");
			prExpression(((UnaryExpressionExpression) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryMinus)
		{ 
			//indent(d+2);
			//sayln("UnaryMinus(");
			prUnaryExpression(((UnaryMinus) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof UnaryNot)
		{ 
			//indent(d+2);
			//sayln("UnaryNot(");
			prUnaryExpression(((UnaryNot) e).a, d+4);
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

	public void prPrimaryExpression(PrimaryExpression e, int d) 
	{
		//indent(d);
		//sayln("PrimaryExpression(");
		if (e instanceof PrimaryIdent)
		{ 
			//indent(d+2);
			//sayln("PrimaryIdent(");
			indent(d+4);
			sayln(((PrimaryIdent) e).a);
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof PrimaryString)
		{ 
			//indent(d+2);
			//sayln("PrimaryString(");
			indent(d+4);
			sayln(((PrimaryString) e).a);
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof PrimaryInt)
		{ 
			//indent(d+2);
			//sayln("PrimaryInt(");
			indent(d+4);
			say(((PrimaryInt) e).a);sayln("");
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof PrimaryFloat)
		{ 
			//indent(d+2);
			//sayln("PrimaryFloat(");
			indent(d+4);
			say(((PrimaryFloat) e).a);sayln("");
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof PrimaryDouble)
		{ 
			//indent(d+2);
			//sayln("PrimaryFloat(");
			indent(d+4);
			say(((PrimaryDouble) e).a);sayln("");
			//indent(d+2);
			//sayln(")");			
		}
		else if (e instanceof PrimaryBool)
		{ 
			//indent(d+2);
			//sayln("PrimaryBool(");
			indent(d+4);
			say(((PrimaryBool) e).a);sayln("");
			//indent(d+2);
			//sayln(")");			
		}
		//indent(d);
		//sayln(")");
	}

}



