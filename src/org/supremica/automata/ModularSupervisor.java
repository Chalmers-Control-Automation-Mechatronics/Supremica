
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
package org.supremica.automata;

import java.lang.String;

import org.supremica.util.SupremicaException;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 * A modular supervisor, examining which events are enabled through
 * "online synchronization" of the modular supervisor model.
 */
public class ModularSupervisor
	implements Supervisor
{
	/** The current global state. */
	State[] currentGlobalState;

	/** The system model. */
	Automata model;

	/**
	 * Creates a modular supervisor. 
	 *
	 * @param model is an Automata containing all the modules in
	 * the supervisor.
	 */
	public ModularSupervisor(Automata model)
		throws SupremicaException
	{
		if (!model.isDeterministic())
		{
			throw new SupremicaException("The supervisor is not deterministic.");
		}

		if (!model.hasInitialState())
		{
			throw new SupremicaException("The supervisor has no initial state, supervision is not possible.");
		}

		// Set the current global state to the initial state
		currentGlobalState = new State[model.size()];
		for (Automaton aut : model)
		{
			currentGlobalState[model.getAutomatonIndex(aut)] = aut.getInitialState();
		}

		// Set system model
		this.model = model;
	}

   	//////////////////////////////////
	// Supervisor interface methods //
	//////////////////////////////////
	
	public synchronized boolean isEnabled(LabeledEvent event)
	{
		// Try executing the event
		// Save the current global state
		State[] currentStateSave = (State[]) currentGlobalState.clone();
		try 
		{
			// Try executing the event
			executeEvent(event);
			// Restore order
			currentGlobalState = currentStateSave;
			// It went well--the event must have been enabled!
			return true;
		}
		catch (EventDisabledException ex)
		{
			// Restore order
			currentGlobalState = currentStateSave;
			// The event is disabled!
			return false;
		}
	}

	public synchronized void executeEvent(LabeledEvent event)
		throws EventDisabledException
	{
		for (Automaton aut : model)
		{
			// Get automaton index
			int index = model.getAutomatonIndex(aut);

			// If the event is included in the alphabet, change state!
			if (aut.getAlphabet().contains(event))
			{				
				// Supposes that the system is deterministic!
				State nextState = currentGlobalState[index].nextState(event);
				if (nextState == null)
				{
					throw new EventDisabledException();
				}
				currentGlobalState[index] = nextState;
			}
		}
	}
}
