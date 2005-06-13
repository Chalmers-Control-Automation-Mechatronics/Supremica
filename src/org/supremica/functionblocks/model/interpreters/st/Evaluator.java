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
 * @author cengic
 */
package org.supremica.functionblocks.model.interpreters.st;
import org.supremica.functionblocks.model.interpreters.st.abstractsyntax.*;

import org.supremica.functionblocks.model.Variables;
import org.supremica.functionblocks.model.Variable;
import org.supremica.functionblocks.model.StringVariable;
import org.supremica.functionblocks.model.IntegerVariable;
import org.supremica.functionblocks.model.DoubleVariable;
import org.supremica.functionblocks.model.FloatVariable;
import org.supremica.functionblocks.model.BooleanVariable;

public class Evaluator {

	private Variables vars = null;
	private Printer printer = new Printer(System.out);

	public Evaluator(Variables v) 
	{ 
		vars=v; 
	}

	public Object evalExpression(Expression e) throws Exception
	{
		return evalOrExpression(e.a);
	}

	public Object evalOrExpression(OrExpression e) throws Exception
	{
		if (e instanceof BinaryOrExpression) 
		{
			Object a = evalOrExpression(((BinaryOrExpression) e).a);
			Object b = evalOrExpression(((BinaryOrExpression) e).b);
			if (a instanceof Boolean && b instanceof Boolean)
			{
				return new Boolean(((Boolean) a).booleanValue() || ((Boolean) b).booleanValue());
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prOrExpression(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryOrExpression)
		{
			return evalXorExpression(((UnaryOrExpression) e).a);
		}
		System.out.println("Eval: Can not determine the child type of OrExpression: ");
		printer.prOrExpression(e,4);
		throw new Exception();
	}

	public Object evalXorExpression(XorExpression e) throws Exception
	{
		if (e instanceof BinaryXorExpression) 
		{
			Object a = evalXorExpression(((BinaryXorExpression) e).a);
			Object b = evalXorExpression(((BinaryXorExpression) e).b);
			if (a instanceof Boolean && b instanceof Boolean)
			{
				return new Boolean((!((Boolean) a).booleanValue() && ((Boolean) b).booleanValue()) || (((Boolean) a).booleanValue() && !((Boolean) b).booleanValue()));
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prXorExpression(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryXorExpression)
		{
			return evalAndExpression(((UnaryXorExpression) e).a);
		}
		System.out.println("Eval: Can not determine the child type of XorExpression: ");
		printer.prXorExpression(e,4);
		throw new Exception();
	}

	public Object evalAndExpression(AndExpression e) throws Exception
	{
		if (e instanceof BinaryAndExpression) 
		{
			Object a = evalAndExpression(((BinaryAndExpression) e).a);
			Object b = evalAndExpression(((BinaryAndExpression) e).b);
			if (a instanceof Boolean && b instanceof Boolean)
			{
				return new Boolean(((Boolean) a).booleanValue() && ((Boolean) b).booleanValue());
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prAndExpression(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryAndExpression)
		{
			return evalComparison(((UnaryAndExpression) e).a);
		}
		System.out.println("Eval: Can not determine the child type of AndExpression: ");
		printer.prAndExpression(e,4);
		throw new Exception();
	}

	public Object evalComparison(Comparison e) throws Exception
	{
		if (e instanceof Eq)
		{ 
			Object a = evalAddExpression(((Eq) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((Eq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() == ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() == ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((Eq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() == ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() == ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else if (a instanceof String)
			{
				Object b = evalAddExpression(((Eq) e).b);
				if(b instanceof String)
				{
					return new Boolean(((String) a).equals(((String) b).toString()));
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof Neq)
		{ 
			Object a = evalAddExpression(((Neq) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((Neq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() != ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() != ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((Neq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() != ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() != ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else if (a instanceof String)
			{
				Object b = evalAddExpression(((Neq) e).b);
				if(b instanceof String)
				{
					return new Boolean( ! ((String) a).equals(((String) b).toString()));
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof Less)
		{ 
			Object a = evalAddExpression(((Less) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((Less) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() < ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() < ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((Less) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() < ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() < ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof LessEq)
		{ 
			Object a = evalAddExpression(((LessEq) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((LessEq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() <= ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() <= ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((LessEq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() <= ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() <= ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof More)
		{ 
			Object a = evalAddExpression(((More) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((More) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() > ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() > ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((More) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() > ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() > ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof MoreEq)
		{ 
			Object a = evalAddExpression(((MoreEq) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((MoreEq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Integer) a).intValue() >= ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Integer) a).intValue() >= ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((MoreEq) e).b);
				if(b instanceof Integer)
				{
					return new Boolean(((Double) a).doubleValue() >= ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Boolean(((Double) a).doubleValue() >= ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prComparison(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prComparison(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryComparison)
		{ 
			return evalAddExpression(((UnaryComparison) e).a);
		}
		System.out.println("Eval: Can not determine the child type of Comparison: ");
		printer.prComparison(e,4);
		throw new Exception();
	}

	public Object evalAddExpression(AddExpression e) throws Exception
	{
		if (e instanceof Plus)
		{ 
			Object a = evalAddExpression(((Plus) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((Plus) e).b);
				if(b instanceof Integer)
				{
					return new Integer(((Integer) a).intValue() + ((Integer) b).intValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prAddExpression(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((Plus) e).b);
				if(b instanceof Double)
				{
					return new Double(((Double) a).doubleValue() + ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prAddExpression(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prAddExpression(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof Minus)
		{ 
			Object a = evalAddExpression(((Minus) e).a);
			if (a instanceof Integer)
			{
				Object b = evalAddExpression(((Minus) e).b);
				if(b instanceof Integer)
				{
					return new Integer(((Integer) a).intValue() - ((Integer) b).intValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prAddExpression(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalAddExpression(((Minus) e).b);
				if(b instanceof Double)
				{
					return new Double(((Double) a).doubleValue() - ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prAddExpression(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prAddExpression(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryAddExpression)
		{ 
			return evalTerm(((UnaryAddExpression) e).a);
		}
		System.out.println("Eval: Can not determine the child type of AddExpression: ");
		printer.prAddExpression(e,4);
		throw new Exception();
	}

	public Object evalTerm(Term e) throws Exception
	{
		if (e instanceof Times)
		{
			Object a = evalTerm(((Times) e).a);
			if (a instanceof Integer)
			{
				Object b = evalTerm(((Times) e).b);
				if(b instanceof Integer)
				{
					return new Integer(((Integer) a).intValue() * ((Integer) b).intValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalTerm(((Times) e).b);
				if(b instanceof Double)
				{
					return new Double(((Double) a).doubleValue() * ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prTerm(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof Div)
		{ 
			Object a = evalTerm(((Div) e).a);
			if (a instanceof Integer)
			{
				Object b = evalTerm(((Div) e).b);
				if(b instanceof Integer)
				{
					return new Integer(((Integer) a).intValue() / ((Integer) b).intValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalTerm(((Div) e).b);
				if(b instanceof Double)
				{
					return new Double(((Double) a).doubleValue() / ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prTerm(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof Mod)
		{ 
			Object a = evalTerm(((Mod) e).a);
			if (a instanceof Integer)
			{
				Object b = evalTerm(((Mod) e).b);
				if(b instanceof Integer)
				{
					return new Integer(((Integer) a).intValue() % ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Double(((Integer) a).intValue() % ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}
			}
			else if (a instanceof Double)
			{
				Object b = evalTerm(((Mod) e).b);
				if(b instanceof Integer)
				{
					return new Double(((Double) a).doubleValue() % ((Integer) b).intValue());
				}
				else if(b instanceof Double)
				{
					return new Double(((Double) a).doubleValue() % ((Double) b).doubleValue());
				}
				else
				{
					System.out.println("Eval: Incompatible types in :");
					printer.prTerm(e,4);
					throw new Exception();
				}				
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prTerm(e,4);
				throw new Exception();
			}				
		}
		else if (e instanceof UnaryTerm)
		{ 
			return evalPowerExpression(((UnaryTerm) e).a);
		}
		System.out.println("Eval: Can not determine the child type of Term: ");
		printer.prTerm(e,4);
		throw new Exception();
	}

	public Object evalPowerExpression(PowerExpression e) throws Exception
	{
		if (e instanceof BinaryPowerExpression)
		{
			System.out.println("Eval: Power operation is not implemented yet!");
			throw new Exception();	
		}
		else if (e instanceof UnaryPowerExpression)
		{
			return evalUnaryExpression(((UnaryPowerExpression) e).a);
		}
		System.out.println("Eval: Can not determine the child type of PowerExpression: ");
		printer.prPowerExpression(e,4);
		throw new Exception();
	}

	public Object evalUnaryExpression(UnaryExpression e) throws Exception
	{
		if (e instanceof UnaryPrimaryExpression)
		{
			return evalPrimaryExpression(((UnaryPrimaryExpression) e).a);
		}
		else if (e instanceof UnaryExpressionExpression)
		{
			return evalExpression(((UnaryExpressionExpression) e).a);
		}
		else if (e instanceof UnaryMinus)
		{ 
			Object a = evalUnaryExpression(((UnaryMinus) e).a);
			if (a instanceof Integer)
			{
				return new Integer(-((Integer) a).intValue());
			}
			else if (a instanceof Double)
			{
				return new Double(-((Double) a).doubleValue());
			}
			else if (a instanceof Float)
			{
				return new Float(-((Float) a).floatValue());
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prUnaryExpression(e,4);
				throw new Exception();
			}							
		}
		else if (e instanceof UnaryNot)
		{ 
			Object a = evalUnaryExpression(((UnaryNot) e).a);
			if (a instanceof Boolean)
			{
				return new Boolean( ! ((Boolean) a).booleanValue());
			}
			else
			{
				System.out.println("Eval: Incompatible type in :");
				printer.prUnaryExpression(e,4);
				throw new Exception();
			}
		}
		System.out.println("Eval: Can not determine the child type of UnaryExpression: ");
		printer.prUnaryExpression(e,4);
		throw new Exception();
	}

	public Object evalPrimaryExpression(PrimaryExpression e) throws Exception
	{
		if (e instanceof PrimaryIdent)
		{
			Variable var = vars.getVariable(((PrimaryIdent) e).a);
			if (var instanceof BooleanVariable)
			{
				return ((BooleanVariable) var).getValue();
			}
			else if (var instanceof IntegerVariable)
			{
				return ((IntegerVariable) var).getValue();			
			}
			else if (var instanceof DoubleVariable)
			{
				return ((DoubleVariable) var).getValue();			
			}
			else if (var instanceof FloatVariable)
			{
				return ((FloatVariable) var).getValue();			
			}
			else if (var instanceof StringVariable)
			{
				return ((StringVariable) var).getValue();			
			}
		}
		else if (e instanceof PrimaryString)
		{
			return ((PrimaryString) e).a;
		}
		else if (e instanceof PrimaryInt)
		{ 
			return ((PrimaryInt) e).a;
		}
		else if (e instanceof PrimaryDouble)
		{ 
			return ((PrimaryDouble) e).a;
		}
		else if (e instanceof PrimaryFloat)
		{ 
			return ((PrimaryFloat) e).a;
		}
		else if (e instanceof PrimaryBool)
		{ 
			return ((PrimaryBool) e).a;
		}
		System.out.println("Eval: Can not determine the child type of PrimaryExpression: ");
		printer.prPrimaryExpression(e,4);
		throw new Exception();
	}
	
}



