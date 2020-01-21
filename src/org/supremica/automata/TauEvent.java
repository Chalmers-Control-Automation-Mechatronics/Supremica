
package org.supremica.automata;

import java.util.HashMap;

import net.sourceforge.waters.model.des.EventProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.properties.Config;


/**
 * <P>A TauEvent always preserves controllability and is never a proposition.
 * A TauEvent keeps track of the original LabeledEvent that it replaced.</P>
 *
 * <P><STRONG>NOTE:</STRONG> Experimental still.</P>
 * <P><STRONG>NOTE:</STRONG> I have NO idea about {@link EventProxy},
 * TauEvent is only to be used in Analyser.</P> *
 *
 * @author Martin Fabian, Sahar Mohajerani
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

    private static Logger logger = LogManager.getLogger(TauEvent.class);
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
      if(Config.MINIMIZATION_USE_TAU_EVENT_MAP.getValue())
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

    @Override
    public TauEvent clone()
    {
      return new TauEvent(this);
    }

    public  LabeledEvent getOriginalEvent()
    {
      return orig_event;
    }

    @Override
    public  boolean isTauEvent()
    {
      return tauEvent;  // should not this always return true?
    }
}