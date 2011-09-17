/************************* TauEvent.java ************************************
 * Legalities, blah, blah, blah... you know the drill
 * NOTE: Experimental still
 * NOTE: I have NO idea about EventProxy, TauEvent is only to be used in Analyzer
 *
 * A TauEvent always preserves controllability and is never a proposition
 * A TauEvent keeps track of the original LabeledEvent that it replaced
 */

package org.supremica.automata;

import java.util.HashMap;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


/**
 * @author Fabian and Sahar
 */
public class TauEvent extends LabeledEvent
{
    private static final boolean NO_PROPOSITION = false;
    private static final String DELIMITER = "_";
    // Should probably have another char than "_" as delimiter...
    // We must agree on some chars that cannot occur in user-specified event (or state) labels
    // but that can be used by labels generated automatically from within Supremica

    LabeledEvent orig_event = null; // keeps track of the replaced event

    //*** static stuff ***

    private static Logger logger = LoggerFactory.createLogger(TauEvent.class);
    private static int count = 1; // class global counter
    static HashMap <String,LabeledEvent> toOriginalMap = new HashMap <String,LabeledEvent>();

    private static String makeLabel(final boolean isControllable, final LabeledEvent event)
    {
        count++;

      if(isControllable)
      {
          toOriginalMap.put(Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count, event);
          return Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count++;    // should really increment count twice on each call?
      }
      else
      {
          toOriginalMap.put(Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count, event);
          return Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count++;
      }
    }

    public static LabeledEvent getOriginalEvent(final LabeledEvent ev) // Because of this change made here (LabeledEvent instead of String as parameter)
    {                                                                   // we can easily change to not use the map but return ev.getOriginalEvent() instead
      if(Config.MINIMIZATION_USE_TAUEVENT_MAP.isTrue())
        return toOriginalMap.get(ev.getLabel());
      else // don't use TauEvent map
      {
        final LabeledEvent event =  ((TauEvent)ev).getOriginalEvent();
        logger.info("TauEvent not using hash-map to get original event " + event.getLabel());
        return event;
      }
    }

    //*** non-static stuff ***

    public TauEvent(final LabeledEvent event)
    {
      super(makeLabel(event.isControllable(), event), NO_PROPOSITION);
      this.orig_event = event;
      this.controllable = event.controllable;	// always preserve controllability
      this.prioritized = false;		// always non-prioritize
      this.observable = false;     // always un-observable
      this.tauEvent= true;          //and always a tau-event

    }

    private TauEvent(final TauEvent tevent)
    {
      // do not copy, use clone()! - so we make this constructor private
      super(tevent);
      this.orig_event = tevent.orig_event;
    }

    public TauEvent clone()
    {
      return new TauEvent(this);
    }

    public  LabeledEvent getOriginalEvent()
    {
      return orig_event;
    }

    public  boolean isTauEvent()
    {
      return tauEvent;  // should not this always return true?
    }
}