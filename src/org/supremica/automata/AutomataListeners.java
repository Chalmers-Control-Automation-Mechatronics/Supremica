
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

import java.util.Iterator;


public class AutomataListeners
	extends Listeners
{
	public static final int MODE_AUTOMATON_ADDED = 1;
	public static final int MODE_AUTOMATON_REMOVED = 2;
	public static final int MODE_AUTOMATON_RENAMED = 3;
	public static final int MODE_ACTIONS_OR_CONTROLS_CHANGED = 4;

	public AutomataListeners(final Automata owner)
	{
		super(owner);
	}

	public void notifyListeners(final int mode, final Automaton a)
	{

		// logger.debug("AutomataListeners.notifyListeners Start");
		if (batchUpdate)
		{
			updateNeeded = true;
		}
		else
		{
			if (listeners != null)
			{

				// logger.debug("AutomataListeners.notifyListeners notifying");
				for (final Iterator<Listener> listenerIt = listeners.iterator();
						listenerIt.hasNext(); )
				{
					final AutomataListener currListener = (AutomataListener) listenerIt.next();

					if (mode == MODE_AUTOMATON_ADDED)
					{
						currListener.automatonAdded((Automata) owner, a);
					}
					else if (mode == MODE_AUTOMATON_REMOVED)
					{
						currListener.automatonRemoved((Automata) owner, a);
					}
					else if (mode == MODE_AUTOMATON_RENAMED)
					{
						currListener.automatonRenamed((Automata) owner, a);
					}
					else if (mode == MODE_ACTIONS_OR_CONTROLS_CHANGED)
					{
						currListener.actionsOrControlsChanged((Automata) owner);
					}
				}
			}

			updateNeeded = false;
		}
	}
}
