

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
			String currName = currState.getName();
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