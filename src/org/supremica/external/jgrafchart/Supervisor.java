
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
package org.supremica.external.jgrafchart;

import org.supremica.automata.*;
import org.supremica.gui.*;
import java.util.*;

public class Supervisor
{
	private static InitializedSupervisors supervisors = new InitializedSupervisors();

	public static boolean isEventEnabled(String supervisor, String event)
	{

		//System.out.println("isEventEnabled Supervisor: " + supervisor + " Event: " + event);
		return supervisors.isEventEnabled(supervisor, event);
	}

	public static boolean executeEvent(String supervisor, String event)
	{

		//System.out.println("executeEvent Supervisor: " + supervisor + " Event: " + event);
		return supervisors.executeEvent(supervisor, event);
	}

	public static boolean initializeSupervisor(String supervisor)
	{

		//System.out.println("initialize Supervisor: " + supervisor);
		return supervisors.initializeSupervisor(supervisor);
	}
}

class InitializedSupervisors
{

	// automatonName -> Automation
	protected HashMap supervisorMap = new HashMap();

	// automationName -> State
	protected HashMap stateMap = new HashMap();

	public InitializedSupervisors() {}

	public boolean isEventEnabled(String supervisor, String event)
	{
		Automaton currAutomaton = getAutomaton(supervisor);

		if (currAutomaton == null)
		{
			return false;
		}

		State currState = getState(supervisor);
		boolean isEnabled = currState.isEnabled(event);

		// System.out.println("isEnabled: " + event + " : " + isEnabled);
		return isEnabled;
	}

	public boolean executeEvent(String supervisor, String event)
	{
		Automaton currAutomaton = getAutomaton(supervisor);

		if (currAutomaton == null)
		{
			return false;
		}

		State currState = getState(supervisor);
		State nextState = currState.nextState(event);

		if (nextState == null)
		{
			System.err.println("Could not execute event: " + event);

			return false;
		}

		stateMap.remove(supervisor);
		stateMap.put(supervisor, nextState);

		return true;
	}

	public boolean initializeSupervisor(String supervisor)
	{
		if (hasAutomaton(supervisor))
		{

			// If the supervisor already is initialized the remove it
			// from the supervisor and stateMap
			supervisorMap.remove(supervisor);
			stateMap.remove(supervisor);
		}

		Gui theGui = ActionMan.getGui();
		VisualProjectContainer container = theGui.getVisualProjectContainer();
		Project activeProject = container.getActiveProject();
		Automaton currAutomaton = activeProject.getAutomaton(supervisor);
		State currState = currAutomaton.getInitialState();

		// Add automaton and state
		supervisorMap.put(supervisor, currAutomaton);
		stateMap.put(supervisor, currState);

		return true;
	}

	public boolean hasAutomaton(String supervisor)
	{
		return supervisorMap.containsKey(supervisor);
	}

	public Automaton getAutomaton(String supervisor)
	{
		Automaton currAutomaton = (Automaton) supervisorMap.get(supervisor);

		return currAutomaton;
	}

	public State getState(String supervisor)
	{
		State currState = (State) stateMap.get(supervisor);

		return currState;
	}
}
