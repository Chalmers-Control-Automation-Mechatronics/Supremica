
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;
import java.io.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;


/**
 * Models an event trace. Instead of storing the
 * event we do store only the label. This is done
 * because we might want to use the same trace in
 * several automata. Since the events is different
 * for different automata we do only store the labels.
 **/
public class LabelTrace
{
	private LinkedList theTrace;

	public LabelTrace()
	{
		theTrace = new LinkedList();
	}

	public void addFirst(String label)
	{
		theTrace.addFirst(label);
	}

	public void addLast(String label)
	{
		theTrace.addLast(label);
	}

	public String getFirst()
		throws NoSuchElementException
	{
		return (String)theTrace.getFirst();
	}

	public String getLast()
		throws NoSuchElementException
	{
		return (String)theTrace.getLast();
	}

	public String removeFirst()
		throws NoSuchElementException
	{
		return (String)theTrace.removeFirst();
	}

	public String removeLast()
		throws NoSuchElementException
	{
		return (String)theTrace.removeLast();
	}

	public int size()
	{
		return theTrace.size();
	}

	public void clear()
	{
		theTrace.clear();
	}

	public Iterator iterator()
	{
		return theTrace.iterator();
	}

	public String toString()
	{
		StringBuffer traceDesc = new StringBuffer();
		for (Iterator eIt = iterator(); eIt.hasNext();)
		{
			String currLabel = (String) eIt.next();
			traceDesc.append("\"" + currLabel + "\"");
			if (eIt.hasNext())
			{
				traceDesc.append("->");
			}
		}
		return traceDesc.toString();
	}

}
