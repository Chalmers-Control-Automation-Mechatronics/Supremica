package org.supremica.gui.simulator;

import org.supremica.gui.*;
import org.supremica.log.*;
import java.util.*;
import uk.ac.ic.doc.scenebeans.event.AnimationListener;
import uk.ac.ic.doc.scenebeans.event.AnimationEvent;
import uk.ac.ic.doc.scenebeans.animation.Animation;

class AnimationSignals
	implements AnimationListener
{
	private static Logger logger = LoggerFactory.createLogger(AnimationSignals.class);

	private Animation theAnimation;
	private HashMap theSignals = new HashMap();
	private LinkedList observers = new LinkedList();

	public AnimationSignals(Animation theAnimation)
	{
		this.theAnimation = theAnimation;
		theAnimation.addAnimationListener(this);
		Set theEvents = theAnimation.getEventNames();
		for (Iterator evIt = theEvents.iterator(); evIt.hasNext();)
		{
			String currEvent = (String)evIt.next();
			theSignals.put(currEvent, Boolean.TRUE);
		}
	}

	public void registerInterest(SignalObserver observer)
	{
		observers.add(observer);
	}

	public void animationEvent(AnimationEvent ev)
	{
		String currEvent = ev.getName();
		if (currEvent.charAt(0) == '~')
		{
			theSignals.put(currEvent.substring(1, currEvent.length()), Boolean.FALSE);
		}
		else
		{
			theSignals.put(currEvent, Boolean.TRUE);
		}

	}

	private void notifyObservers()
	{
		for (Iterator obsIt = observers.iterator(); obsIt.hasNext();)
		{
			SignalObserver currObserver = (SignalObserver)obsIt.next();
			currObserver.signalUpdated();
		}
	}

	public boolean isTrue(String theSignal)
	{
		Boolean currValue = (Boolean)theSignals.get(theSignal);

		if (currValue == null)
		{
			logger.error("Signal not found: " + theSignal);
			return false;
		}
		return currValue == Boolean.TRUE;
	}
}
	