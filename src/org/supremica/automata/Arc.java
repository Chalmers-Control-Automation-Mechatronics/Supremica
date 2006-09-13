
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import org.supremica.log.*;
import java.util.Comparator;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.plain.base.AbstractNamedElement;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.TransitionProxy;

public class Arc
	implements TransitionProxy
{
	private static Logger logger = LoggerFactory.createLogger(Arc.class);
	private LabeledEvent event;
	private State fromState;
	private State toState;
//	private ArcListeners listeners = null;

	// Internal use for graphical representation -- should be in GraphicalArc?
	private double outgoingAngle = 0;
	private double incomingAngle = 0;
	private int dxControlPointBegin = 0;
	private int dyControlPointBegin = 0;
	private int dxControlPointEnd = 0;
	private int dyControlPointEnd = 0;
	private int beginX = -1;
	private int beginY = -1;
	private int endX = -1;
	private int endY = -1;

	/**
	 * This variable indicates which (initial) automata are "the brains behind"
	 * this Arc. It has only a reasonable value if synchronization is performed.
	 */
	private boolean[] firingAutomata = null;

	private Arc(State from, State to)
		throws IllegalArgumentException
	{
		if (from == null)
		{
			throw new IllegalArgumentException("State from must be non null");
		}

		if (to == null)
		{
			throw new IllegalArgumentException("State to must be non null");
		}

		this.fromState = from;
		this.toState = to;

		/* I think these should be in Automaton.java... maybe we're not actually going to add this arc, right?
		from.addOutgoingArc(this);
		to.addIncomingArc(this);
		*/
	}

	public Arc(State from, State to, LabeledEvent event)
		throws IllegalArgumentException
	{
		this(from, to);

		this.event = event;
	}

	public Arc(Arc other)
	{
		this.event = other.event;
		this.fromState = other.fromState;
		this.toState = other.toState;
	}


	public TransitionProxy clone()
	{
		return new Arc(this);
	}

	public LabeledEvent getEvent()
	{
		return event;
	}

	public void setEvent(LabeledEvent event)
	{
		this.event = event;
	}

	public State getToState()
	{
		return toState;
	}

	public State getTarget()
	{
		return getToState();
	}

	public void setToState(State toState)
	{
		this.toState = toState;
	}

	public State getFromState()
	{
		return fromState;
	}

	public State getSource()
	{
		return getFromState();
	}

	public void setFromState(State fromState)
	{
		this.fromState = fromState;
	}

	public String getLabel()
	{
		return event.getLabel();
	}

	public void reverse()
	{
		// swap the states
		State tmpState = getToState();

		toState = getFromState();
		fromState = tmpState;
	}

	/**
	 * True if the from- and the to-state are the same, otherwise false.
	 */
	public boolean isSelfLoop()
	{
		return fromState == toState;
	}

	public void clear()
	{
		if (fromState != null)
		{
			fromState.removeOutgoingArc(this);
		}

		if (toState != null)
		{
			toState.removeIncomingArc(this);
		}

		// eventId = null;
		event = null;
		fromState = null;
		toState = null;

		// Removed notification here because it was extremely costly
		// when running the AutomatonPurge. Should we really have notifiers
		// attached to the arcs, isn't it enough if we have it to the automaton?

		// Later: Hmm - This can be removed because then the arcset in the automaton will
		// not be updated and thus an error might occur.

		// Hugo: What do you mean? Can you or can't you remove it!?

		// notifyListeners(ArcListeners.MODE_ARC_REMOVED, this);
	}

	// This does not belong here, does it?
	// Should have a "GraphicalArc" that includes (by inheritance?) Arc
	public void computeDefaultDisplayParameters()
	{
		int x1 = fromState.getX();
		int y1 = fromState.getY();
		int x2 = toState.getX();
		int y2 = toState.getY();

		outgoingAngle = angle(x1, y1, x2, y2);
		incomingAngle = angle(x2, y2, x1, y1);
	}

	public void computeStartAndEndParameters()
	{
		int x1 = fromState.getX();
		int y1 = fromState.getY();
		int r1 = fromState.getRadius();
		int x2 = toState.getX();
		int y2 = toState.getY();
		int r2 = toState.getRadius();

		beginX = x1 + (int) (r1 * Math.cos(outgoingAngle));
		beginY = y1 + (int) (r1 * Math.sin(outgoingAngle));
		endX = x2 + (int) (r2 * Math.cos(incomingAngle));
		endY = y2 + (int) (r2 * Math.sin(incomingAngle));
	}

	public int getBeginX()
	{
		return beginX;
	}

	public int getBeginY()
	{
		return beginY;
	}

	public int getEndX()
	{
		return endX;
	}

	public int getEndY()
	{
		return endY;
	}

/*
	public Listeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new ArcListeners(this);
		}

		return listeners;
	}

	private void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}

	private void notifyListeners(int mode, Object o)
	{
		if (listeners != null)
		{
			listeners.notifyListeners(mode, o);
		}
	}
*/

	public static double angle(int x0, int y0, int x1, int y1)
	{
		double angle = 0;
		double xDist = x1 - x0;
		double yDist = y1 - y0;

		if (xDist == 0)
		{
			if (yDist >= 0)
			{
				angle = Math.PI / 2;
			}
			else
			{
				angle = -1 * Math.PI / 2;
			}
		}
		else if (xDist > 0)
		{
			angle = Math.atan(yDist / xDist);

			if (yDist < 0)
			{
				angle = 2 * Math.PI + angle;
			}
		}
		else
		{
			xDist = -1 * xDist;
			angle = Math.PI - Math.atan(yDist / xDist);
		}

		return angle;
	}

	/**
	 *      Returns a boolean vector where true means that the corresponding automata
	 *      is (one of the) "responsible(s)" for the transition.
	 *
	 *      @return firingAutomata
	 */
	public boolean[] getFiringAutomata()
	{
		return firingAutomata;
	}

	public boolean equals(Arc obj)
	{
		if (obj == null)
		{
			return (this == null); // Hehe
		}

		Arc arc = (Arc) obj;
		return (toState.equals(arc.getToState()) && fromState.equals(arc.getFromState()) && event.equals(arc.getEvent()));
	}

	/**
	 * Returns a string representation of the arc as an ordered triple,
	 * with from-state, to-state and event.
	 */
	public String toString()
	{
		StringBuffer sbuf = new StringBuffer();

		sbuf.append("<");
		sbuf.append(getFromState());
		sbuf.append(", ");
		sbuf.append(getEvent());
		sbuf.append(", ");
		sbuf.append(getToState());
		sbuf.append(">");

		return sbuf.toString();
	}

	/**
	 * Initializes the array of firingAutomata
	 *
	 * @param size
	 */
	private void initFiringAutomata(int size)
	{
		firingAutomata = new boolean[size];

		for (int i = 0; i < size; i++)
		{
			firingAutomata[i] = false;
		}
	}

	/**
	 * Compares this arc to another arc. The event is compared first,
	 * then the toState and last the fromState.
	 */
	public int compareTo(TransitionProxy trans)
	{
		final int compsource = getSource().compareTo(trans.getSource());
		if (compsource != 0)
		{
			return compsource;
		}
		final int compevent = getEvent().compareTo(trans.getEvent());
		if (compevent != 0)
		{
			return compevent;
		}
		return getTarget().compareTo(trans.getTarget());
	}

	/**
	 * Comparator for comparing two arcs based on their events alone.
	 */
	public static class EventComparator
		implements Comparator<Arc>
	{
		public int compare(Arc one, Arc two)
		{
			return one.getEvent().compareTo(two.getEvent());
		}
	}

	public Object acceptVisitor(final ProxyVisitor visitor)
		throws VisitorException
	{
		final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
		return desvisitor.visitTransitionProxy(this);
	}

	public boolean equalsByContents(final Proxy partner)
	{
		Arc partnerArc = (Arc)partner;
		return getSource().refequals(partnerArc.getSource()) && getTarget().refequals(partnerArc.getTarget()) && getEvent().refequals(partnerArc.getEvent());
    }

	public boolean equalsWithGeometry(final Proxy partner)
	{
		return equalsByContents(partner);
	}

	public int hashCodeByContents()
	{
	return getSource().refHashCode() + 5 * getEvent().refHashCode() + 25 * getTarget().refHashCode();
	}

	public int hashCodeWithGeometry()
	{
		return hashCodeByContents();
	}


}
