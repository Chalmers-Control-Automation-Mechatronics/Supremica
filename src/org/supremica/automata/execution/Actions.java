
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
import org.supremica.automata.LabeledEvent;

public class Actions
{
	private Map labelToActionMap = new TreeMap();

	public Actions()
	{
	}

	public Actions(Actions otherActions)
	{
		for (Iterator actIt = otherActions.iterator(); actIt.hasNext(); )
		{
			Action currAction = (Action) actIt.next();
			Action newAction = new Action(currAction);

			addAction(newAction);
		}
	}

	public void addActions(Actions otherActions)
	{
		for (Iterator actIt = otherActions.iterator(); actIt.hasNext(); )
		{
			Action currAction = (Action) actIt.next();
			Action newAction = new Action(currAction);

			addAction(newAction);
		}
	}

	public boolean addAction(Action theAction)
	{
		if (theAction == null)
		{
			return false;
		}
		if (labelToActionMap.containsKey(theAction.getLabel()))
		{
			return false;
		}
		labelToActionMap.put(theAction.getLabel(), theAction);
		return true;
	}

	public void removeAction(Action theAction)
	{
		labelToActionMap.remove(theAction.getLabel());
	}

	public boolean hasAction(String label)
	{
		return labelToActionMap.containsKey(label);
	}

	public Action getAction(String label)
	{
		return (Action) labelToActionMap.get(label);
	}

	public Action getAction(LabeledEvent event)
	{
		return (Action) labelToActionMap.get(event.getLabel());
	}

	public Iterator iterator()
	{
		return labelToActionMap.values().iterator();
	}

	public int size()
	{
		return labelToActionMap.size();
	}

	public void clear()
	{
		labelToActionMap.clear();
	}
}
