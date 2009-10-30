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
        public EventWrapper(int index_, LabeledEvent event_)
        {
            index = index_;
            event = event_;
        }
        
        public int index;
        public LabeledEvent event;
        public boolean external, pending;
    }
    ;
    
    private Stack<EventWrapper> pending_events;    // what I really need is a queue :)
    @SuppressWarnings("unused")
	private Automata theAutomata;
    private Alphabet theAlphabet;
    private int events_size;
    private EventWrapper[] events;    // int --> EventWrapper
    private TreeMap<LabeledEvent, EventWrapper> events_map;    // Event --> EventWrapper
    
    public ExternalEventExecuter(SimulatorExecuter theExecuter, SimulatorEventListModel eventModel)
    {
        super(theExecuter, eventModel);
        
        logger.info("Using external exectuer");
        
        theAutomata = eventModel.getAutomata();
        theAlphabet = eventModel.getAlphabet();
        pending_events = new Stack<EventWrapper>();
        events_size = theAlphabet.size();
        events = new EventWrapper[events_size];
        events_map = new TreeMap<LabeledEvent, EventWrapper>();
        
        native_initialize(events_size);
        
        int i = 0;
        
        for (Iterator<?> alphIt = theAlphabet.iterator(); alphIt.hasNext(); )
        {
            LabeledEvent currEvent = theAlphabet.getEvent(((LabeledEvent) alphIt.next()).getLabel());
            
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
            catch (InterruptedException e)
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
    
    @SuppressWarnings("unused")
	private void from_native_fire(int index)
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
    
    protected synchronized void update_event_queue()
    {
        
        // first, try to execute the pending events:
        synchronized (pending_events)
        {
            if (!pending_events.empty())
            {
                EventWrapper ew = pending_events.pop();
                
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
        
        for (int i = 0; i < nbrOfEvents; i++)
        {
            LabeledEvent currEvent = eventModel.getEventAt(i);
            EventWrapper ew = events_map.get(currEvent);
            
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
