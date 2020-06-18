//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.simulator;

import java.util.Iterator;

import javax.swing.AbstractListModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.execution.Condition;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Controls;

public class SimulatorEventListModel
    extends AbstractListModel<Object>
    implements SignalObserver
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(SimulatorEventListModel.class);

    // private int[] currState;
    private final int[] events;
    private int eventAmount = 0;
    private final Automata theAutomata;
    @SuppressWarnings("unused")
	private final Project theProject;
    private final Controls theControls;
    private final Alphabet theAlphabet;
    @SuppressWarnings("unused")
	private boolean showState = false;
    private final boolean showDisabledEvents = false;
    private final AutomataSynchronizerHelper helper;

//      private AnimationSignals theSignals;
    private final SimulatorExecuter theExecuter;
    protected boolean isLocked = false;

    public SimulatorEventListModel(final SimulatorExecuter theExecuter, final AutomataSynchronizerHelper helper, final Project theProject)
    {
        this.helper = helper;
        this.theAutomata = helper.getAutomata();
        this.theProject = theProject;
        this.theControls = theProject.getControls();
        this.theAlphabet = helper.getUnionAlphabet();

//              this.showDisabledEvents = showDisabledEvents;
//              this.theSignals = theSignals;
        this.theExecuter = theExecuter;
        events = new int[helper.getNbrOfEvents() + 1];

//              theExecuter.registerSignalObserver(this);
//              theSignals.registerInterest(this);
    }

//      public void setCurrState(int[] currState)
//      {
//              this.currState = currState;
//
//              update();
//      }
    public void setShowStateId(final boolean showState)
    {
        this.showState = showState;
    }

    public void update()
    {
        enterLock();

        //logger.info("SimulatorEventListModel.update");
        //AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();
        final AutomataSynchronizerExecuter onlineSynchronizer = helper.getCoExecuter();
        final int[] extEvents = onlineSynchronizer.getOutgoingEvents(theExecuter.getCurrentState());

        System.arraycopy(extEvents, 0, events, 0, events.length);

        if (!showDisabledEvents)
        {
            int currEventIndex = 0;
            int nbrOfEvents = 0;

            while (extEvents[currEventIndex] != Integer.MAX_VALUE)
            {
                LabeledEvent currEvent;

                try
                {
                    currEvent = helper.getIndexMap().getEventAt(events[currEventIndex]);
                    //currEvent = theAlphabet.getEventWithIndex(events[currEventIndex]);
                }
                catch (final Exception ex)
                {

                    //logger.error("Exception in SimulatorEventListModel.update");
                    logger.debug(ex.getStackTrace());

                    return;
                }

                if (theControls.hasControl(currEvent.getLabel()))
                {

                    //logger.info("hasControl: " + currEvent.getLabel());
                    final Control currControl = theControls.getControl(currEvent.getLabel());
                    boolean conditionsFulfilled = true;

                    for (final Iterator<Condition> condIt = currControl.conditionIterator();
                    condIt.hasNext(); )
                    {
                        final Condition condition = condIt.next();

                        conditionsFulfilled = conditionsFulfilled && theExecuter.isTrue(condition);
                    }

                    if (conditionsFulfilled)
                    {
                        events[nbrOfEvents] = events[currEventIndex];

                        nbrOfEvents++;
                    }
                    else
                    {

                        //logger.info("hasControl, event disabled: " + events[currEventIndex] + " " + currEvent.getLabel());
                    }
                }
                else
                {
                    events[nbrOfEvents] = events[currEventIndex];

                    nbrOfEvents++;
                }

                currEventIndex++;
            }

            events[nbrOfEvents] = Integer.MAX_VALUE;
        }

        eventAmount = 0;

        while (events[eventAmount] != Integer.MAX_VALUE)
        {
            eventAmount++;
        }

        logger.debug("Before fireContentsChanged");
        fireContentsChanged(this, 0, eventAmount - 1);
        logger.debug("After fireContentsChanged");
        exitLock();
    }

    public Automata getAutomata()
    {
        return theAutomata;
    }

    public Alphabet getAlphabet()
    {
        return theAlphabet;
    }

    public synchronized void enterLock()
    {
        try
        {
            while (isLocked)
            {
                wait();
            }

            setLock(true);
        }
        catch (final InterruptedException e)
        {
            logger.error("enterLock interrupted");
        }
    }

    public synchronized void exitLock()
    {
        setLock(false);
        notifyAll();
    }

    public synchronized boolean isLocked()
    {
        return isLocked;
    }

    private synchronized void setLock(final boolean doLock)
    {
        this.isLocked = doLock;
    }

    @Override
    public int getSize()
    {
        return eventAmount;
    }

    @Override
    public Object getElementAt(final int index)
    {
        LabeledEvent currEvent;

        try
        {
            //currEvent = theAlphabet.getEventWithIndex(events[index]);
            currEvent = helper.getIndexMap().getEventAt(events[index]);
        }
        catch (final Exception ex)
        {
            logger.error("Error: Could not find event in alphabet.", ex);
            logger.debug(ex.getStackTrace());

            return null;
        }

        final StringBuilder responseString = new StringBuilder();

        if (!currEvent.isControllable())
        {
            responseString.append("!");
        }

        responseString.append(currEvent.getLabel());

        return responseString.toString();
    }

    public LabeledEvent getEventAt(final int index)
    {
        LabeledEvent currEvent;

        try
        {
            //currEvent = theAlphabet.getEventWithIndex(events[index]);
            currEvent = helper.getIndexMap().getEventAt(events[index]);
        }
        catch (final Exception ex)
        {
            logger.error("Error: Could not find event in alphabet.", ex);
            logger.debug(ex.getStackTrace());

            return null;
        }

        return currEvent;
    }

//      public int[] getStateAt(int index)
//      {
//              //System.err.println("getStateAt: " + index);
//              AutomataOnlineSynchronizer onlineSynchronizer = helper.getCoExecuter();
//
//              return onlineSynchronizer.doTransition(events[index]);
//      }
//
//      public synchronized boolean executeEvent(LabeledEvent theEvent)
//      {
//              theExecuter.executeEvent(theEvent);
//              return true;
//      }
    @Override
    public void signalUpdated()
    {
        update();
    }
}
