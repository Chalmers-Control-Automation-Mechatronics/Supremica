/********************* ForbiddenEvent **********************/
// Owner: MF
/** 
 * Implementing forbidden partial states by self-looped 
 * "forbidden" events requires a child of LabeledEvent.
 * This implements that child.
 *
 * We need to guarantee that each newly created forbidden
 * event has a label that is unique over all
 **/
package org.supremica.automata;

import org.supremica.automata.LabeledEvent;
import net.sourceforge.waters.model.des.EventProxy;

public class ForbiddenEvent
	extends LabeledEvent
{
//	public ForbiddenEvent()
//	{
//		super();
//	}
	
	public ForbiddenEvent(String label)
	{
		super(label);
	}
	
	public ForbiddenEvent(LabeledEvent event)
	{
		super(event);
	}
	
	public ForbiddenEvent(EventProxy event_proxy)
	{
		super(event_proxy);
	}
	
	public boolean isForbidden()
	{
		return true;
	}
}