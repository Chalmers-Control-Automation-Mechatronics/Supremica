//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

import org.supremica.automata.*;

import java.util.*;

/**
 * @author Arash
 *
 * ExternalEventExecuter execute events by asking an external interface.
 * The external interface is assumed to be interfaced to a dynamic library called "executer".
 * For exemple, in MS windows, a DLL called "executer.dll" should be in the system PATH.
 *
 */
class ExternalEventExecuter
    extends EventExecuter
{
    static
    {
        System.loadLibrary("executer");
    }

    private class EventWrapper
    {
        public EventWrapper(final int index_, final LabeledEvent event_)
        {
            index = index_;
            event = event_;
        }

        public int index;
        public LabeledEvent event;
        public boolean external;
        // pending;
    }
    ;

    private final Stack<EventWrapper> pending_events;    // what I really need is a queue :)
    /*
	@SuppressWarnings("unused")
    private final Automata theAutomata;
    */
    private final Alphabet theAlphabet;
    private final int events_size;
    private final EventWrapper[] events;    // int --> EventWrapper
    private final TreeMap<LabeledEvent, EventWrapper> events_map;    // Event --> EventWrapper

    public ExternalEventExecuter(final SimulatorExecuter theExecuter, final SimulatorEventListModel eventModel)
    {
        super(theExecuter, eventModel);

        logger.info("Using external exectuer");

        //theAutomata = eventModel.getAutomata();
        theAlphabet = eventModel.getAlphabet();
        pending_events = new Stack<EventWrapper>();
        events_size = theAlphabet.size();
        events = new EventWrapper[events_size];
        events_map = new TreeMap<LabeledEvent, EventWrapper>();

        native_initialize(events_size);

        int i = 0;

        for (final Iterator<?> alphIt = theAlphabet.iterator(); alphIt.hasNext(); )
        {
            final LabeledEvent currEvent = theAlphabet.getEvent(((LabeledEvent) alphIt.next()).getLabel());

            events[i] = new EventWrapper(i, currEvent);
            events[i].external = native_register_event(i, currEvent.getLabel());

            events_map.put(currEvent, events[i]);

                        /*
                        // check map sanity:
                        EventWrapper ew = (EventWrapper) events_map.get(currEvent);
                        if(ew.event != currEvent) logger.fatal("ew.event != currEvent");
                        if(ew.index != i) logger.fatal("ew.index != i");
                         */
            i++;
        }

        // XXX: the thread is started from the base class. what if it starts executing before we have initialized the DLL?
    }

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

            // get enabled events
            eventModel.update();

            // update the list of pending events:
            native_check_external_events();

            // do some event messageing...
            update_event_queue();
        }

        native_cleanup();
    }

    private void execute_event(final EventWrapper ew)
    {
        logger.debug("About to execute " + ew.event.getLabel() + "   " + ew.index);

        if (!theExecuter.executeEvent(ew.event))
        {
            logger.warn("Failed to execute event: " + ew.event.getLabel());

            return;
        }

        native_fire(ew.index);
    }

    /*
    @SuppressWarnings("unused")
    private void from_native_fire(final int index)
    {
        if ((index >= 0) && (index < events_size))
        {
            synchronized (pending_events)
            {
                if (events[index].pending)
                {
                    logger.warn("Pending event already in queue: " + events[index].event.getLabel());
                }
                else
                {
                    events[index].pending = true;
                }

                pending_events.push(events[index]);
            }
        }
    }
    */

    protected synchronized void update_event_queue()
    {

        // first, try to execute the pending events:
        synchronized (pending_events)
        {
            if (!pending_events.empty())
            {
                final EventWrapper ew = pending_events.pop();

                if (!theExecuter.executeEvent(ew.event))
                {
                    logger.warn("Failed to execute event: " + ew.event.getLabel());
                }

                //ew.pending = false;

                return;
            }
        }

        // these are events that _we_ activate
        eventModel.enterLock();

        final int nbrOfEvents = eventModel.getSize();

        for (int i = 0; i < nbrOfEvents; i++)
        {
            final LabeledEvent currEvent = eventModel.getEventAt(i);
            final EventWrapper ew = events_map.get(currEvent);

            if (ew != null)
            {
                if (!ew.external)
                {
                    execute_event(ew);
                    eventModel.exitLock();

                    return;
                }
            }
            else
            {
                logger.warn("Could not find event: " + currEvent.getLabel());
            }
        }

        eventModel.exitLock();
    }

    // native models:
    public native void native_initialize(int numberOfEvents);

    public native void native_cleanup();

    public native boolean native_register_event(int ID, String name);

    public native void native_fire(int eventID);

    public native void native_check_external_events();
}
