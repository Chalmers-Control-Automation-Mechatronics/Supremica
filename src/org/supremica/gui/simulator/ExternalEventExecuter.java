package org.supremica.gui.simulator;


import org.supremica.automata.*;
import org.supremica.log.*;

import java.util.*;

/**
 * @author Arash
 *
 * ExternalEventExecuter execute events by asking an external interface.
 * The external interface is assumed to be interfaced to a dynamic library called "executer".
 * For exemple, in MS windows, a DLL called "executer.dll" should be in the system PATH.
 *
 */

class ExternalEventExecuter extends EventExecuter
{

	static {
		System.loadLibrary("executer");
	}

	private class EventWrapper {
		public EventWrapper(int index_, LabeledEvent event_) { index = index_; event = event_; }
		public int index;
		public LabeledEvent event;
		public boolean external, pending;
	};

	private Stack pending_events; // what I really need is a queue :)
	private Automata theAutomata;
	private Alphabet theAlphabet;
	private int events_size;
	private EventWrapper[] events; // int --> EventWrapper
	private TreeMap events_map;    // Event --> EventWrapper


	public ExternalEventExecuter(SimulatorExecuter theExecuter, SimulatorEventListModel eventModel)
	{
		super(theExecuter, eventModel);

		// TEMP: slow down things
		// sleepTime = 5000;

		logger.info("Using external exectuer");

		theAutomata = eventModel.getAutomata();
		theAlphabet = eventModel.getAlphabet();
		/*
		try {
			theAlphabet = AlphabetHelpers.getUnionAlphabet(theAutomata, false, false);
		} catch(Exception i_dont_know_what_to_do_with_this) {
			i_dont_know_what_to_do_with_this.printStackTrace();
		}
		*/



		pending_events = new Stack();
		events_size = theAlphabet.size();
		events = new EventWrapper[events_size];
		events_map = new TreeMap();
		native_initialize(events_size);
		int i = 0;

		for (Iterator alphIt = theAlphabet.iterator(); alphIt.hasNext(); )
		{
			LabeledEvent currEvent = theAlphabet.getEventWithLabel( ((LabeledEvent) alphIt.next()).getLabel());
			events[i] = new EventWrapper(i,currEvent);
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
			catch (InterruptedException e)
			{

			}
			logger.debug("---- IN");
			eventModel.update();
			update_event_queue();
			logger.debug("---- OUT");
		}


		native_cleanup();

	}


	private void execute_event(EventWrapper ew)
	{
		logger.debug("About to execute " + ew.event.getLabel() + "   " + ew.index);
		if (!theExecuter.executeEvent(ew.event))
		{
			logger.warn("Failed to execute event: " + ew.event.getLabel());
			return;
		}

		native_fire(ew.index);
	}

	private void from_native_fire(int index)
	{
		if(index >= 0 && index < events_size) {
			synchronized(pending_events) {
				if(events[index].pending)
				{
					logger.warn("Pending event already in queue: " + events[index].event.getLabel());
				} else {
					events[index].pending = true;
				}
				pending_events.push(events[index]);
			}
		}
	}

	protected synchronized void update_event_queue()
	{



		logger.debug("1----------------------------");
		// first, try to execute the pending events:
		synchronized(pending_events) {
			if(!pending_events.empty()) {
				EventWrapper ew = (EventWrapper )pending_events.pop();

				if (!theExecuter.executeEvent(ew.event))
				{
					logger.warn("Failed to execute event: " + ew.event.getLabel());
				}
				ew.pending = false;
				return;

			}
		}






		// these are events that _we_ activate
		eventModel.enterLock();
		int nbrOfEvents = eventModel.getSize();

		logger.debug("2.a ----------------------------");

		StringBuffer dum = new StringBuffer();
		dum.append("Enabled events are: " );
		for (int i = 0; i < nbrOfEvents; i++) dum.append(" " + eventModel.getEventAt(i).getLabel());
		logger.debug(dum.toString() );


		logger.debug("2.b----------------------------");


		for (int i = 0; i < nbrOfEvents; i++)
		{
			LabeledEvent currEvent = eventModel.getEventAt(i);
			EventWrapper ew = (EventWrapper) events_map.get(currEvent);
			if (ew !=  null)
			{
				// _WEIRD_ á la Knut bug:
				// The objects are not the same a(altough its the same event), which makes
				// the simulator to _fail_ on executing this event, here is a _dirty_ hack to fix it
				// ew.event = currEvent;

				if(!ew.external) {
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

		logger.debug("3----------------------------");

	}



	// native models:

	  public native void native_initialize(int numberOfEvents);
	  public native void native_cleanup();
	  public native boolean native_register_event(int ID, String name);
	  public native void native_fire(int eventID);
	  public native void native_check_external_events();

}


