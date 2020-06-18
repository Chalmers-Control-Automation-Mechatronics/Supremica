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

import javax.swing.event.ListDataEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.LabeledEvent;
import org.supremica.properties.Config;

/**
 * Automatically execute controllable and uncontrollable events in the simulation.
 *
 * @author Knut &Aring;kesson
 */
class EventExecuter
    extends Thread

//      implements ListDataListener
{
    protected static Logger logger = LogManager.getLogger(EventExecuter.class);
    protected long sleepTime = 100;
    protected boolean doRun = true;
    protected boolean executeControllableEvents = false;
    protected boolean executeUncontrollableEvents = false;
    protected SimulatorEventListModel eventModel;
    protected SimulatorExecuter theExecuter;

    public EventExecuter(final SimulatorExecuter theExecuter, final SimulatorEventListModel eventModel)
    {

//              this.sleepTime = sleepTime;
        this.eventModel = eventModel;
        this.theExecuter = theExecuter;

//              eventModel.addListDataListener(this);
        sleepTime = Config.SIMULATION_CYCLE_TIME.getValue();

        System.out.println("sleepTime = " + sleepTime);    // DEBUG
    }

    @Override
    public void run()
    {
        while (doRun)
        {
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (final InterruptedException e)
            {}

            tryExecuteEvent();
        }
    }

//
    public void executeControllableEvents(final boolean executeControllableEvents)
    {
        this.executeControllableEvents = executeControllableEvents;

        //tryExecuteEvent();
    }

    public void executeUncontrollableEvents(final boolean executeUncontrollableEvents)
    {
        this.executeUncontrollableEvents = executeUncontrollableEvents;

        //tryExecuteEvent();
    }

    public void requestStop()
    {
        doRun = false;
    }

    protected void tryExecuteUncontrollableEvents()
    {
        eventModel.enterLock();

        final int nbrOfEvents = eventModel.getSize();

        for (int i = 0; i < nbrOfEvents; i++)
        {
            final LabeledEvent currEvent = eventModel.getEventAt(i);

            if (!currEvent.isControllable())
            {
                logger.info("Automatically executed event: " + currEvent.getLabel());

                if (!theExecuter.executeEvent(currEvent))
                {
                    logger.warn("Failed to execute event: " + currEvent.getLabel());
                }

                eventModel.exitLock();

                return;
            }
        }

        eventModel.exitLock();
    }

    protected void tryExecuteControllableEvents()
    {
        eventModel.enterLock();

        final int nbrOfEvents = eventModel.getSize();

        for (int i = 0; i < nbrOfEvents; i++)
        {
            final LabeledEvent currEvent = eventModel.getEventAt(i);

            if (currEvent.isControllable())
            {
                logger.info("Automatically executed event: " + currEvent.getLabel());

                if (!theExecuter.executeEvent(currEvent))
                {
                    logger.warn("Failed to execute event: " + currEvent.getLabel());
                }

                eventModel.exitLock();

                return;
            }
        }

        eventModel.exitLock();
    }

    protected void tryExecuteAnyEvents()
    {
        eventModel.enterLock();

        final int nbrOfEvents = eventModel.getSize();

        for (int i = 0; i < nbrOfEvents; i++)
        {
            final LabeledEvent currEvent = eventModel.getEventAt(i);

            logger.info("Automatically executed event: " + currEvent.getLabel());

            if (!theExecuter.executeEvent(currEvent))
            {
                logger.warn("Failed to execute event: " + currEvent.getLabel());
            }

            eventModel.exitLock();

            return;
        }

        eventModel.exitLock();
    }

    protected synchronized void tryExecuteEvent()
    {
        updateSignals();

/*
                                int nbrOfEvents = eventModel.getSize();
                                logger.debug("2.a ----------------------------");
                                eventModel.enterLock();
                                StringBuilder dum = new StringBuilder();
                                dum.append("Enabled events are: " );
                                for (int i = 0; i < nbrOfEvents; i++) dum.append(" " + eventModel.getEventAt(i).getLabel());
                                logger.debug(dum.toString() );
                                eventModel.exitLock();
 */
        if (executeUncontrollableEvents)
        {
            tryExecuteUncontrollableEvents();
        }

        updateSignals();

        if (executeControllableEvents)
        {
            tryExecuteControllableEvents();
        }
    }

    public void contentsChanged(final ListDataEvent e)
    {

        //tryExecuteEvent();
    }

    public void intervalAdded(final ListDataEvent e)
    {
        contentsChanged(e);
    }

    public void intervalRemoved(final ListDataEvent e)
    {
        contentsChanged(e);
    }

    protected void updateSignals()
    {
        eventModel.update();

        //theExecuter.updateSignals();
    }
}
