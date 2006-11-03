
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

import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
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
    private String label = "";
    
    private boolean controllable = true;
    private boolean prioritized = true;
    private boolean observable = true;
    private boolean operatorIncrease = false;
    private boolean operatorReset = false;
    private boolean immediate = false;
    private boolean proposition = false;
    private int expansionPriority = -1;
    private int index = -1;
    
    public LabeledEvent()
    {}
    
    public LabeledEvent(String label)
    {
        this(label, false);
    }
    
    public LabeledEvent(String label, boolean isProposition)
    {
        this.label = label;
        this.proposition = isProposition;
    }
    
    public LabeledEvent(LabeledEvent e)
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
    
    public LabeledEvent(EventProxy e)
    {
        label = e.getName();
        EventKind watersKind = e.getKind();
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
    
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    public boolean isControllable()
    {
        return controllable;
    }
    
    public void setControllable(boolean controllable)
    {
        this.controllable = controllable;
    }
    
    public boolean isObservable()
    {
        return observable;
    }
    
    public void setObservable(boolean observable)
    {
        this.observable = observable;
    }
    
    public boolean isOperatorIncrease()
    {
        return operatorIncrease;
    }
    
    public void setOperatorIncrease(boolean operatorIncrease)
    {
        this.operatorIncrease = operatorIncrease;
    }
    
    public boolean isOperatorReset()
    {
        return operatorReset;
    }
    
    public void setOperatorReset(boolean operatorReset)
    {
        this.operatorReset = operatorReset;
    }
    
    public boolean isImmediate()
    {
        return immediate;
    }
    
    public void setImmediate(boolean immediate)
    {
        this.immediate = immediate;
    }
    
    public boolean isPrioritized()
    {
        return prioritized;
    }
    
    public void setPrioritized(boolean prioritized)
    {
        this.prioritized = prioritized;
    }
    
    public boolean isUnobservable()
    {
        return !observable;
    }
    
    public void setUnobservable(boolean unobservable)
    {
        this.observable = !unobservable;
    }
    
    public boolean isProposition()
    {
        return proposition;
    }
    
    public void setProposition(boolean proposition)
    {
        this.proposition = proposition;
    }
    
    public void setExpansionPriority(int expansionPriority)
    {
        this.expansionPriority = expansionPriority;
    }
    
    public int getExpansionPriority()
    {
        return expansionPriority;
    }
    
    public boolean equals(Object obj)
    {
        return equals((LabeledEvent) obj);
        
        // throw new RuntimeException("LabeledEvent::equals(Object), not expected to be called!");
        // System.err.println("equalsObject");
        // return this.label.equals(((LabeledEvent) obj).label);
    }
    
    // NOTE -- this one's experimental, while migrating to the real problem domain
    // The Java people have messed it all up with inheriting equals(Object)
    public boolean equals(LabeledEvent event)
    {
        return equals(event.getLabel());
    }
    
    public boolean equals(String label)
    {
        return this.label.equals(label);
    }
    
    public int hashCode()
    {
        return label.hashCode();
    }
    
    public int getSynchIndex()
    {
        return index;
    }
    
    void setSynchIndex(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return getSynchIndex();
    }
    
/*
        public int compareTo(Object event)
        {
                return label.compareTo(((LabeledEvent) event).label);
        }
 */
    public int compareTo(NamedProxy event)
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
    //# Methods that implements the EventProxy interface
    //# Some methods is defined above
    
    
    public String getName()
    {
        return getLabel();
    }
    
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
    
    public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
        return desvisitor.visitEventProxy(this);
    }
    
    public boolean equalsByContents(final Proxy partner)
    {
        LabeledEvent partnerEvent = (LabeledEvent)partner;
        return getName().equals(partnerEvent.getName()) && getKind().equals(partnerEvent.getKind()) && isObservable() == partnerEvent.isObservable();
    }
    
    public boolean equalsWithGeometry(final Proxy partner)
    {
        return equalsByContents(partner);
    }
    
    public int hashCodeByContents()
    {
        int result = refHashCode();
        result *= 5;
        result += getKind().hashCode();
        result *= 5;
        if (isObservable())
        {
            result++;
        }
        return result;
    }
    
    public int hashCodeWithGeometry()
    {
        return hashCodeByContents();
    }
    
    public boolean refequals(final NamedProxy partner)
    {
        return getName().equals(partner.getName());
    }
    
    public int refHashCode()
    {
        return getName().hashCode();
    }
}
