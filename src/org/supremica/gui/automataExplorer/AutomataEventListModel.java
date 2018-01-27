//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.automataExplorer;

import javax.swing.AbstractListModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;


public class AutomataEventListModel
        extends AbstractListModel<Object>
{
 	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(AutomataEventListModel.class);

    private int[] currState;

    // / private ArrayList currArcs = new ArrayList();
    private int[] events;
    private int eventAmount = 0;
    private final boolean forward;
    private final Automata theAutomata;
    @SuppressWarnings("unused")
	private final Alphabet theAlphabet;
    @SuppressWarnings("unused")
	private boolean showState = false;
    private final AutomataSynchronizerHelper helper;

    public AutomataEventListModel(final AutomataSynchronizerHelper helper, final boolean forward)
    {
        this.forward = forward;
        this.helper = helper;
        this.theAutomata = helper.getAutomata();
        //this.theAlphabet = helper.getAutomaton().getAlphabet();
        this.theAlphabet = theAutomata.getUnionAlphabet();
    }

    public void setCurrState(final int[] currState)
    {
        this.currState = currState;

        update();
    }

    public void setShowStateId(final boolean showState)
    {
        this.showState = showState;
    }

    public void update()
    {
        final AutomataSynchronizerExecuter onlineSynchronizer = helper.getCoExecuter();

        if (forward)
        {
            events = onlineSynchronizer.getOutgoingEvents(currState);
        }
        else
        {
            events = onlineSynchronizer.getIncomingEvents(currState);
        }

        eventAmount = 0;

        while (events[eventAmount] != Integer.MAX_VALUE)
        {
            eventAmount++;
        }

        fireContentsChanged(this, 0, eventAmount - 1);

        /*
         *  Iterator arcIt;
         *  if (forward)
         *  {
         *  arcIt = currState.outgoingArcsIterator();
         *  }
         *  else
         *  {
         *  arcIt = currState.incomingArcsIterator();
         *  }
         *  currArcs.clear();
         *  while (arcIt.hasNext())
         *  {
         *  Arc currArc = (Arc)arcIt.next();
         *  currArcs.add(currArc);
         *  }
         *  fireContentsChanged(this, 0, currArcs.size() - 1);
         */
    }

    @Override
    public int getSize()
    {
        return eventAmount;
    }

    @Override
    public Object getElementAt(final int index)
    {
        org.supremica.automata.LabeledEvent currEvent;

        try
        {
            currEvent = helper.getIndexMap().getEventAt(events[index]);
            //currEvent = theAlphabet.getEventWithIndex(events[index]);
        }
        catch (final Exception e)
        {
            logger.error(e);
            //System.err.println("Error: Could not find event in alphabet!\n");

            return null;
        }

        final StringBuilder responseString = new StringBuilder();

        if (!currEvent.isControllable())
        {
            responseString.append("!");
        }

        responseString.append(currEvent.getLabel());

                /*
                 *  if (showState)
                 *  {
                 *  int[] currState;
                 *  if (forward)
                 *  {
                 *  currState = currArc.getToState();
                 *  }
                 *  else
                 *  {
                 *  currState = currArc.getFromState();
                 *  }
                 *  responseString.append(" [state name: " + currState.getName() + "]");
                 *  }
                 */
        return responseString.toString();
    }

    public int[] getStateAt(final int[] currState, final int index)
    {
        //AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();
        final AutomataSynchronizerExecuter onlineSynchronizer = helper.getCoExecuter();

        return onlineSynchronizer.doTransition(currState, events[index]);

                /*
                 *  Arc currArc = (Arc)currArcs.get(index);
                 *  State newState;
                 *  if (forward)
                 *  {
                 *  newState = currArc.getToState();
                 *  }
                 *  else
                 *  {
                 *  newState = currArc.getFromState();
                 *  }
                 *  return newState;
                 */
    }
}
