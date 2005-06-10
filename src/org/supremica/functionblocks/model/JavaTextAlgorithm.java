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

package org.supremica.functionblocks.model;

import bsh.Interpreter;

import java.util.Iterator;
import java.io.Reader;
import java.io.StringReader;

public class JavaTextAlgorithm extends Algorithm
{

	private Interpreter interpreter;

	private String algorithmText;

	public JavaTextAlgorithm(String n, String algText)
	{
		name = n;
		algorithmText = algText;
		interpreter = new Interpreter();
	}

	public void execute(Variables vars)
	{

		//set variables
		for (Iterator iter = vars.iterator();iter.hasNext();)
		{
			String curName = (String) iter.next();
			Variable curVariable = vars.getVariable(curName);
			if(curVariable.getType().equals("Local") || curVariable.getType().equals("DataInput") || curVariable.getType().equals("DataOutput"))
			{
				try
				{
					if (curVariable instanceof StringVariable)
					{
						StringVariable tmpVar = (StringVariable) vars.getVariable(curName); 
						interpreter.set(curName,tmpVar.getValue());
					}
					else if (curVariable instanceof IntegerVariable)
					{
						IntegerVariable tmpVar = (IntegerVariable) vars.getVariable(curName); 
						interpreter.set(curName,tmpVar.getValue().intValue());
					}
					else if (curVariable instanceof DoubleVariable)
					{
						DoubleVariable tmpVar = (DoubleVariable) vars.getVariable(curName); 
						interpreter.set(curName,tmpVar.getValue().doubleValue());
					}
					else if (curVariable instanceof FloatVariable)
					{
						FloatVariable tmpVar = (FloatVariable) vars.getVariable(curName); 
						interpreter.set(curName,tmpVar.getValue().floatValue());
					}
					else if (curVariable instanceof BooleanVariable)
					{
						BooleanVariable tmpVar = (BooleanVariable) vars.getVariable(curName); 
						interpreter.set(curName,tmpVar.getValue().booleanValue());
					}
				}
				catch (Exception e)
				{
					System.err.println(e);
				}
			}
		}
		
		//execute algorithm

		try
		{
			Reader algTextReader = new StringReader(algorithmText);
			interpreter.eval(algTextReader);
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
		
		//get variables
		for (Iterator iter = vars.iterator();iter.hasNext();)
		{
			String curName = (String) iter.next();
			Variable curVariable = vars.getVariable(curName);
			if(curVariable.getType().equals("Local") || curVariable.getType().equals("DataOutput"))
			{
				try
				{
					if (curVariable instanceof StringVariable)
					{
						StringVariable tmpVar = (StringVariable) vars.getVariable(curName); 
						String tmpValue = (String) interpreter.get(curName);
						((StringVariable) vars.getVariable(curName)).setValue(tmpValue);
					}
					else if (curVariable instanceof IntegerVariable)
					{
						IntegerVariable tmpVar = (IntegerVariable) vars.getVariable(curName); 
						int tmpValue = ((Integer) interpreter.get(curName)).intValue();
						((IntegerVariable) vars.getVariable(curName)).setValue(tmpValue);
					}
					else if (curVariable instanceof DoubleVariable)
					{
						DoubleVariable tmpVar = (DoubleVariable) vars.getVariable(curName); 
						double tmpValue = ((Double) interpreter.get(curName)).doubleValue();
						((DoubleVariable) vars.getVariable(curName)).setValue(tmpValue);
					}
					else if (curVariable instanceof FloatVariable)
					{
						FloatVariable tmpVar = (FloatVariable) vars.getVariable(curName); 
						float tmpValue = ((Float) interpreter.get(curName)).floatValue();
						((FloatVariable) vars.getVariable(curName)).setValue(tmpValue);
					}
					else if (curVariable instanceof BooleanVariable)
					{
						BooleanVariable tmpVar = (BooleanVariable) vars.getVariable(curName); 
						boolean tmpValue = ((Boolean) interpreter.get(curName)).booleanValue();
						((BooleanVariable) vars.getVariable(curName)).setValue(tmpValue);
					}
				}
				catch (Exception e)
				{
					System.err.println(e);
				}
			}
		}
		
	}
	
}
