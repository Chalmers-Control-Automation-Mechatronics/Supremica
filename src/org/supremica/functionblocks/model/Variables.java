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
/**
 * @author cengic
 */
package org.supremica.functionblocks.model;

import java.util.*;
import java.util.Iterator;

public class Variables
{
	
	private Map variables = new HashMap();

	public void addVariable(String name, Variable var)
	{
		variables.put(name,var);
	}

	public Variable getVariable(String name)
	{
		if(!variables.keySet().contains(name))
		{
			System.out.println("Variables: no variable " + name + " in this instance");
			System.out.println("\tContained Variables:" + toString());
			System.exit(0);
		}
		return (Variable) variables.get(name);
	}


	public Iterator iterator()
	{
		return variables.keySet().iterator();
	}

	public Object clone()
	{
		Variables newVars = new Variables();
		for (Iterator iter = iterator(); iter.hasNext();)
		{
			String curName = (String) iter.next();
			Variable curVar = getVariable(curName);
			newVars.addVariable(curName, (Variable) curVar.clone());
		}
		return newVars;
	}

	public String toString()
	{
		String thisVars = "[";
		for(Iterator iter = variables.keySet().iterator();iter.hasNext();)
		{
			String curName = (String )iter.next();
			if(iter.hasNext())
			{
				thisVars = thisVars + curName + " " + getVariable(curName).toString() + ",";
			}
			else
			{
				thisVars = thisVars + curName + " " + getVariable(curName).toString() + "]";
			}
		}
		return thisVars;
	}
}
