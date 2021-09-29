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

import net.sourceforge.waters.model.des.EventProxy;


public class ForbiddenEvent
	extends LabeledEvent
{
	public ForbiddenEvent(final String label)
	{
		super(label);
	}

	public ForbiddenEvent(final LabeledEvent event)
	{
		super(event);
	}

	public ForbiddenEvent(final EventProxy event_proxy)
	{
		super(event_proxy);
	}

	@Override
	public boolean isForbidden()
	{
		return true;
	}
}