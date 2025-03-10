//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-

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

import java.io.Serializable;
import java.util.Comparator;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.util.Args;


public class Arc
    implements TransitionProxy
{
    private static Logger logger = LogManager.getLogger(Arc.class);
    private LabeledEvent event;
    private final State fromState;
    private State toState;

    // TEMP-solution (use EFA instead)
    public static final double DEFAULT_PROBABILITY = 1;
    private double probability = DEFAULT_PROBABILITY;

    public Arc(final State from, final State to, final LabeledEvent event)
    {
        Args.checkForNull(from);
        Args.checkForNull(to);
        Args.checkForNull(event);

        this.fromState = from;
        this.toState = to;
        this.event = event;
    }

    public Arc(final Arc other)
    {
        this.event = other.event;
        this.fromState = other.fromState;
        this.toState = other.toState;
    }

    @Override
    public TransitionProxy clone()
    {
        try
        {
            return (Arc)super.clone();
        }
        catch (final CloneNotSupportedException ex)
        {
            logger.error(ex);
            return null;
        }
    }

    @Override
    public LabeledEvent getEvent()
    {
        return event;
    }

    public void setEvent(final LabeledEvent event)
    {
        this.event = event;
    }

    public State getToState()
    {
        return toState;
    }

    public void setToState(final State toState)
    {
        this.toState = toState;
    }

    @Override
    public State getTarget()
    {
        return getToState();
    }

    public State getFromState()
    {
        return fromState;
    }

    @Override
    public State getSource()
    {
        return getFromState();
    }

    public String getLabel()
    {
        return event.getLabel();
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
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Arc)
        {
            final Arc arc = (Arc) object;
            return (toState.equals(arc.getToState()) && fromState.equals(arc.getFromState()) && event.equals(arc.getEvent()));
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return toState.hashCode() + fromState.hashCode() + event.hashCode();
    }

    /**
     * Returns a string representation of the arc as an ordered triple,
     * with from-state, to-state and event.
     */
    @Override
    public String toString()
    {
        final StringBuilder sbuf = new StringBuilder();

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
     * Compares this arc to another arc. The event is compared first,
     * then the toState and last the fromState.
     */
    @Override
    public int compareTo(final TransitionProxy trans)
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
        implements Comparator<Arc>, Serializable
    {
		private static final long serialVersionUID = 1L;

		@Override
    public int compare(final Arc one, final Arc two)
        {
            return one.getEvent().compareTo(two.getEvent());
        }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.Proxy
    @Override
    public Class<TransitionProxy> getProxyInterface()
    {
        return TransitionProxy.class;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
        throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor =
            (ProductDESProxyVisitor) visitor;
        return desvisitor.visitTransitionProxy(this);
    }


    //#######################################################################
    //# TEMP-solution (use EFA instead)
    public Arc(final State from, final State to, final LabeledEvent event, final double probability)
        throws IllegalArgumentException
    {
        this(from, to, event);
        setProbability(probability);
    }

    public void setProbability(final double probability)
    {
        @SuppressWarnings("unused")
        final
		String label = getEvent().getName();
        this.probability = probability;
    }

    public double getProbability()
    {
	return probability;
    }

}
