
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
package org.supremica.automata.execution;

import java.util.*;
import org.supremica.automata.execution.expressions.*;

public class Control
{
	private String label = null;
	private List conditions = null;
	private Expression expr = null;
	private boolean invert = false;

	public Control(String label)
	{
		this(label, false);
	}

	public Control(String label, boolean invert)
	{
		this.label = label;
		this.invert = invert;
		conditions = new LinkedList();
		expr = new ConstVariable(false);
	}

	public Control(String label, boolean invert, Condition condition)
	{
		this(label, invert);

		addCondition(condition);
	}

	public Control(Control otherControl)
	{
		this(otherControl.label, otherControl.invert);

		conditions = new LinkedList(otherControl.conditions);
		expr = new ConstVariable(true);
	}

	public String getLabel()
	{
		return label;
	}

	public void addCondition(Condition condition)
	{
		conditions.add(condition);
	}

	public void removeCondition(Condition condition)
	{
		conditions.remove(condition);
	}

	public Iterator conditionIterator()
	{
		return conditions.iterator();
	}

	/** This should be in conditions instead, Remove this later **/

/*
		public boolean doInvert()
		{
				return invert;
		}
*/

	/** This should be in conditions instead, Remove this later **/

/*
		public void setInvert(boolean invert)
		{
				this.invert = invert;
		}
*/
	public boolean equals(Object other)
	{
		if (!(other instanceof Control))
		{
			return false;
		}

		Control otherControl = (Control) other;

		return label.equals(otherControl.label) && conditions.equals(otherControl.conditions);
	}

	public int hashCode()
	{
		return label.hashCode();
	}
}
