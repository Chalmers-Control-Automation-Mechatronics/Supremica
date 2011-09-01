/************************* TauEvent.java ************************************
 * Legalities, blah, blah, blah... you know the drill
 * NOTE: Experimental still
 * NOTE: I have NO idea about EventProxy, TauEvent is only to be used in Analyzer
 *
 * A TauEvent always preserves controllability and is never a proposition
 * A TauEvent keeps track of the original LabeledEvent that it replaced
 */

package org.supremica.automata;

import org.supremica.automata.LabeledEvent;
import org.supremica.properties.Config;

/**
 * @author Fabian
 */
public class TauEvent extends LabeledEvent
{
    private static final boolean NO_PROPOSITION = false;
    private static final String DELIMITER = "_";
    	// Should probably have another char than "_" as delimiter...
	// We must agree on some chars that cannot occur in user-specified event (or state) labels
	// but that can be used by labels generated automatically from within Supremica

    private static int count = 1; // class global counter
    LabeledEvent orig_event = null; // keeps track of the replaced event

    private static String makeLabel(final boolean isControllable)
    {
	if(isControllable)
	    return Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count++;
	else
	    return Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.getAsString() + DELIMITER + count++;
    }

    public TauEvent(final LabeledEvent event)
    {
	super(makeLabel(event.isControllable()), NO_PROPOSITION);
	this.orig_event = event;
	this.controllable = event.controllable;	// always preserve controllability
        this.prioritized = false;		// always non-prioritize
        this.observable = false;		// always un-observable
    }

    private TauEvent(final TauEvent tevent)
    {
	// do not copy! - so we make this contructor private
	super(tevent);	// This line just to keep the compiler happy
    }

    public LabeledEvent getOriginalEvent()
    {
	return orig_event;
    }
}
