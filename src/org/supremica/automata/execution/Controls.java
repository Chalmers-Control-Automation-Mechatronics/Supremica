
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

public class Controls
{
	private Set theControls = null;
	private Map labelToControlMap = null;


	public Controls()
	{
		theControls = new HashSet();
		labelToControlMap = new HashMap();
	}

	public Controls(Controls otherControls)
	{
		theControls = new HashSet((int) (otherControls.size() * 1.5));
		labelToControlMap = new HashMap((int) (otherControls.size() * 1.5));

		for (Iterator conIt = otherControls.iterator(); conIt.hasNext(); )
		{
			Control currControl = (Control) conIt.next();
			Control newControl = new Control(currControl);

			addControl(newControl);
		}
	}

	public void addControls(Controls otherControls)
	{
		for (Iterator conIt = otherControls.iterator(); conIt.hasNext(); )
		{
			Control currControl = (Control) conIt.next();
			Control newControl = new Control(currControl);

			addControl(newControl);
		}
	}

	public boolean addControl(Control theControl)
	{
		if (theControl == null)
		{
			return false;
		}
		if (labelToControlMap.containsKey(theControl.getLabel()))
		{
			return false;
		}
		theControls.add(theControl);
		labelToControlMap.put(theControl.getLabel(), theControl);
		return true;
	}

	public void removeControl(Control theControl)
	{
		theControls.remove(theControl);
		labelToControlMap.remove(theControl.getLabel());
	}

	public boolean hasControl(String label)
	{
		return labelToControlMap.containsKey(label);
	}

	public Control getControl(String label)
	{
		return (Control) labelToControlMap.get(label);
	}

	public Iterator iterator()
	{
		return theControls.iterator();
	}

	public int size()
	{
		return theControls.size();
	}

	public void clear()
	{
		theControls.clear();
		labelToControlMap.clear();
	}
}
