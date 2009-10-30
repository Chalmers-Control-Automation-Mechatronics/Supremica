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
 * The Variable describes some state of a part of the machine and can have a finite number of states.
 *
 *
 * Created: Mon Apr  24 15:00:32 2006
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.controlsystemdata;

import java.util.List;
import java.util.LinkedList;

// Should the values really be Strings and not boolean, integers and so on. Strings may take to much time? 
// Could use hashvalue too.

public class VariableData
{
    private String name;
    private String initialValue;
    private List<String> values; 
    // The order for the values are not important but I allways iterate through all values.
    // Normally very few elements are used.

    public VariableData(String name)
    {
	this.name = name;
	values = new LinkedList<String>();
	initialValue = null;
    }

    public String getName()
    {
	return name;
    }
   
    public List<String> getValues()
    {
	return values;
    }
  
    public void addValue(String valueToAdd)
    {
	values.add(valueToAdd);
    }

    public void setInitialValue(String initialValue)
    {
	this.initialValue = initialValue;
    }

    public String getInitialValue()
    {
	return initialValue;
    }
}