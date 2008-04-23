/*
 * This RAC package is external to the Supremica tool and developed by 
 * Oscar Ljungkrantz. Contact:
 *
 * Oscar Ljungkrantz 
 * oscar.ljungkrantz@chalmers.se
 * +46 (0)708-706278
 * SWEDEN
 *
 * for technical discussions about RACs (Reusable Automation Components).
 * For questions about Supremica license or other technical discussions,
 * contact Supremica according to License Agreement below.
 */

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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
 * The IntVariable class is used to represent an int variable 
 * (input, output or internal) in SMV.
 *
 *
 * Created: Mon Apr 14 16:05:12 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac.verificationModel.smv;

import java.util.Set;
import java.util.LinkedHashSet;

public class StringVariable extends Variable
{
    private Set<String> possibleValues;

    public StringVariable(String name)
    {
	super(name);
	possibleValues = new LinkedHashSet<String>(4); //initial capacity 4 and default load factor (0.75)
    }

    public String getVariableDeclaration()
    {
	String s = name + " : {";
	for (String value : possibleValues)
	{
	    s += value + ", ";
	}
	s = s.substring(0, s.length()-2) + "}" ; // delete the last ", " and add ending
	return s;
    }

    // Takes string with possible values, separated by a comma (,), and adds each value to the set
    public void setPossibleValues(String posValues)
    {
	int start = 0;
	while(start < posValues.length())
	{
	    int end = posValues.indexOf(",",start); // end = -1 if not found
	    if (end < 0)
	    {
		end = posValues.length();
	    }
	    possibleValues.add(posValues.substring(start, end).trim());
	    start = end + 1;
	}
    }

}
