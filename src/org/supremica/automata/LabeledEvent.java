//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-

/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.Collections;
import java.util.Map;

import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;

public class LabeledEvent
    implements EventProxy
{
    /** The label is what shows in the dot-figures, this is the
     * global identifier of an event, appearing in the alphabet. */
    private final String label;

    private boolean controllable = true;
    private boolean prioritized = true;
    private boolean observable = true;
    private boolean operatorIncrease = false;
    private boolean operatorReset = false;
    private boolean immediate = false;
    private boolean proposition = false;
    private int expansionPriority = -1;
    private int index = -1;

//    public LabeledEvent()
//    {}

    public LabeledEvent(final String label)
    {
        this(label, false);
    }

    public LabeledEvent(final String label, final boolean isProposition)
    {
        this.label = label;
        this.proposition = isProposition;
    }

    public LabeledEvent(final LabeledEvent e)
    {
        label = e.label;
        controllable = e.controllable;
        prioritized = e.prioritized;
        observable = e.observable;
        operatorIncrease = e.operatorIncrease;
        operatorReset = e.operatorReset;
        immediate = e.immediate;
        proposition = e.proposition;
        index = e.index;
    }

    public LabeledEvent(final LabeledEvent e, final String newLabel)
    {
        controllable = e.controllable;
        prioritized = e.prioritized;
        observable = e.observable;
        operatorIncrease = e.operatorIncrease;
        operatorReset = e.operatorReset;
        immediate = e.immediate;
        proposition = e.proposition;
        index = e.index;
        label = newLabel;
    }

    public LabeledEvent(final EventProxy e)
    {
        label = e.getName();
        final EventKind watersKind = e.getKind();
        if (watersKind == EventKind.CONTROLLABLE)
        {
            controllable = true;
        }
        if (watersKind == EventKind.UNCONTROLLABLE)
        {
            controllable = false;
        }
        if (watersKind == EventKind.PROPOSITION)
        {
            proposition = true;
        }

        observable = e.isObservable();
    }

    public LabeledEvent clone()
    {
        return new LabeledEvent(this);
    }

    public String toString()
    {
        return "'" + label + "'";
    }

    public String getLabel()
    {
        return label;
    }

//    /**
//     * @deprecated
//     * There is no reason why setlabel should not be immutable
//     */
////    @Deprecated
////    public void setLabel(String label)
////    {
////        this.label = label;
////    }

    public boolean isControllable()
    {
        return controllable;
    }

    public void setControllable(final boolean controllable)
    {
        this.controllable = controllable;
    }

    public boolean isObservable()
    {
        return observable;
    }

    public void setObservable(final boolean observable)
    {
        this.observable = observable;
    }

    public boolean isOperatorIncrease()
    {
        return operatorIncrease;
    }

    public void setOperatorIncrease(final boolean operatorIncrease)
    {
        this.operatorIncrease = operatorIncrease;
    }

    public boolean isOperatorReset()
    {
        return operatorReset;
    }

    public void setOperatorReset(final boolean operatorReset)
    {
        this.operatorReset = operatorReset;
    }

    public boolean isImmediate()
    {
        return immediate;
    }

    public void setImmediate(final boolean immediate)
    {
        this.immediate = immediate;
    }

    public boolean isPrioritized()
    {
        return prioritized;
    }

    public void setPrioritized(final boolean prioritized)
    {
        this.prioritized = prioritized;
    }

    public boolean isUnobservable()
    {
        return !observable;
    }

    public void setUnobservable(final boolean unobservable)
    {
        this.observable = !unobservable;
    }

    public boolean isProposition()
    {
        return proposition;
    }

    public void setProposition(final boolean proposition)
    {
        this.proposition = proposition;
    }

    public void setExpansionPriority(final int expansionPriority)
    {
        this.expansionPriority = expansionPriority;
    }

    public int getExpansionPriority()
    {
        return expansionPriority;
    }

    public boolean equals(final Object other)
    {
        if (other instanceof LabeledEvent)
        {
            return equals(((LabeledEvent)other).label);
        }
        return false;
    }

    public boolean equals(final String label)
    {
        return this.label.equals(label);
    }

    public int hashCode()
    {
        return label.hashCode();
    }

    public int getIndex()
    {
        return index;
    }

    void setIndex(final int index)
    {
        this.index = index;
    }

/*
        public int compareTo(Object event)
        {
                return label.compareTo(((LabeledEvent) event).label);
        }
 */
    public int compareTo(final NamedProxy event)
    {
        return label.compareTo(((LabeledEvent) event).label);
    }

    /**
     * Ordinary LabeledEvents are not forbidden, but their children
     * ForbiddenEvents are. This is overridden in ForbiddenEvent to
     * always return true.
     **/
    public boolean isForbidden()
    {
        return false;
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.base.Proxy
    public Class<EventProxy> getProxyInterface()
    {
        return EventProxy.class;
    }

    public boolean refequals(final NamedProxy partner)
    {
        return getName().equals(partner.getName());
    }

    public int refHashCode()
    {
        return getName().hashCode();
    }

    public Object acceptVisitor(final ProxyVisitor visitor)
        throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor =
            (ProductDESProxyVisitor) visitor;
        return desvisitor.visitEventProxy(this);
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.base.NamedProxy
    public String getName()
    {
        return getLabel();
    }


    //#########################################################################
    //# Interface net.sourceforge.waters.model.des.EventProxy
    //# (Some EventProxy methods are defined above.)
    public EventKind getKind()
    {
        if (proposition)
        {
            return EventKind.PROPOSITION;
        }
        if (controllable)
        {
            return EventKind.CONTROLLABLE;
        }
        return EventKind.UNCONTROLLABLE;
    }

    public Map<String,String> getAttributes()
    {
      return Collections.emptyMap();
    }

}
