//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.ic.doc.scenebeans.animation.Animation;
import uk.ac.ic.doc.scenebeans.event.AnimationEvent;
import uk.ac.ic.doc.scenebeans.event.AnimationListener;


class AnimationSignals
	implements AnimationListener
{
	private static Logger logger = LogManager.getLogger(AnimationSignals.class);
	@SuppressWarnings("unused")
	private final Animation theAnimation;
	private final HashMap<String, Boolean> theSignals = new HashMap<String, Boolean>();
	@SuppressWarnings("unused")
	private final LinkedList<?> observers = new LinkedList<Object>();

	public AnimationSignals(final Animation theAnimation)
	{
		this.theAnimation = theAnimation;

		theAnimation.addAnimationListener(this);

		final Set<?> theEvents = theAnimation.getEventNames();

		for (final Iterator<?> evIt = theEvents.iterator(); evIt.hasNext(); )
		{
			final String currEvent = (String) evIt.next();

			theSignals.put(currEvent, Boolean.TRUE);
		}
	}

	public void registerInterest(final SignalObserver observer)
	{

//              observers.add(observer);
	}

	@Override
  public synchronized void animationEvent(final AnimationEvent ev)
	{
		logger.info("AnimationEvent: " + ev.getName());

		final String currEvent = ev.getName();

		if (currEvent.charAt(0) == '~')
		{
			final String currSignal = currEvent.substring(1, currEvent.length());

			logger.info("Setting " + currSignal + " to FALSE");
			theSignals.put(currSignal, Boolean.FALSE);
		}
		else
		{
			logger.info("Setting " + currEvent + " to TRUE");
			theSignals.put(currEvent, Boolean.TRUE);
		}

		//logger.error("Calling notifyObservers");
		//notifyObservers();
		//logger.error("Finished Calling notifyObservers");
	}

/*
		public void notifyObservers()
		{
				for (Iterator obsIt = observers.iterator(); obsIt.hasNext();)
				{
						SignalObserver currObserver = (SignalObserver)obsIt.next();
						currObserver.signalUpdated();
				}
		}
*/
	public synchronized boolean isTrue(final String theSignal)
	{
		final Boolean currValue = theSignals.get(theSignal);

		if (currValue == null)
		{
			logger.error("Signal not found: " + theSignal);

			return false;
		}

		return currValue == Boolean.TRUE;
	}

/*
		public void updateSignals()
		{

		}
*/
}
