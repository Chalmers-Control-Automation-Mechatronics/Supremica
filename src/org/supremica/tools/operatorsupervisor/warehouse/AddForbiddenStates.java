/*
 * JDESCO - Discrete Event System Controller synthesizer.
 * Copyright (C) 2000  Knut Åkesson, ka@s2.chalmers.se
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.supremica.tools.operatorsupervisor.warehouse;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.IO.*;

import java.util.*;
import java.io.*;

public class AddForbiddenStates
{
	public static void forbidStates(Automaton theAutomaton)
	{
		Iterator stateIt = theAutomaton.stateIterator();
		while(stateIt.hasNext())
		{
			State currState = (State)stateIt.next();
			if (currState.isInitial())
			{
				currState.setAccepting(true);
			}
			String currName = currState.getId();
			int length = currName.length();
			if ((length % 2) == 0)
			{
				int mid = length / 2;
				boolean matches = currName.regionMatches(0, currName, mid, mid);
				if (matches)
				{
					currState.setForbidden(true);
				}
			}
		}
	}


	public static void main(String args[])
		throws Exception
	{

		// Get filename
		if (args.length == 0)
   		{
			System.out.println("Usage: AddForbiddenStates file.xml");
			return;
   		}

		ProjectBuildFromXml builder = new ProjectBuildFromXml();

   		Project theAutomata = builder.build(new File(args[0]));
   		Iterator autIt = theAutomata.iterator();
   		while (autIt.hasNext())
   		{
   			Automaton currAutomaton = (Automaton)autIt.next();
   			forbidStates(currAutomaton);
   		}

		PrintWriter pw = new PrintWriter(System.out);

		AutomataToXml serializer = new AutomataToXml(theAutomata);
		serializer.serialize(pw);
	}
}