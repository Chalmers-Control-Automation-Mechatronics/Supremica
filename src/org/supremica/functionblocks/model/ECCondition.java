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
package org.supremica.functionblocks.model;

import java.lang.Exception;
import java.io.StringReader;
import java.io.Reader;

import java_cup.runtime.Scanner;

import org.supremica.functionblocks.model.interpreters.st.Lexer;
import org.supremica.functionblocks.model.interpreters.st.Parser;
import org.supremica.functionblocks.model.interpreters.st.Evaluator;
import org.supremica.functionblocks.model.interpreters.st.abstractsyntax.Expression;

public class ECCondition
{

	private String condition = "";

	private Expression abstractSyntax = null;

	private ECCondition() {}

	public ECCondition(String condition)
	{
		set(condition);
	}

	public void set(String cond)
	{

		//System.out.println("ECCondition.set(" + cond + ")");

		condition = cond;

		StringReader stringReader = new StringReader(condition);

		Lexer lexer = new Lexer((Reader) stringReader);

		Parser parser = new Parser((Scanner) lexer);

		try
		{
			abstractSyntax = (Expression) parser.parse().value;
		}
		catch(Exception e)
		{

		}

	}

	public String get()
	{
		return condition;
	}

	public Object evaluate(Variables vars)
	{

		//System.out.println("ECCondition.evaluate(): Evaluating \"" + condition + "\" with vars ...");
		//System.out.print(vars.toString());
		try
		{
			return (new Evaluator(vars)).evalExpression(abstractSyntax);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			System.exit(0);
		}
		return null;
	}

}
