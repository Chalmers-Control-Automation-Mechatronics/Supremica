
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
package org.supremica.gui.simulator;

import org.supremica.automata.algorithms.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Project;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.execution.Controls;
import org.supremica.automata.execution.Control;
import org.supremica.automata.execution.Condition;

public class SimulatorEventListModel
    extends AbstractListModel<Object>
    implements SignalObserver
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(SimulatorEventListModel.class);

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

    public int getSize()
    {
        return eventAmount;
    }

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

        final StringBuffer responseString = new StringBuffer();

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
    public void signalUpdated()
    {
        update();
    }
}
