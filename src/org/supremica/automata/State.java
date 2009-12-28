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

import java.util.*;

import org.supremica.util.Args;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;

public class State
    implements StateProxy
{
    public final static double MIN_COST = 0;
    public final static double MAX_COST = Double.MAX_VALUE;
    public final static double UNDEF_COST = -1;
    public final static int UNDEF_POS = Integer.MIN_VALUE;
    private int index = -1;

    static final LabeledEvent acceptingProposition = new LabeledEvent(EventDeclProxy.DEFAULT_MARKING_NAME, true);
    static final LabeledEvent forbiddenProposition = new LabeledEvent(EventDeclProxy.DEFAULT_FORBIDDEN_NAME, true);

    /**
     * Name is the external identifier, i.e. the string appearing in Supremica
     */
    private String name;
    private boolean initial = false;
    private boolean accepting = false;
    private boolean forbidden = false;
    private boolean first = false;
    private boolean last = false;
    private boolean visited = false;
    private boolean selected = false;
    private double cost = UNDEF_COST;
    private State assocState = null;
    private StateSet stateSet = null;

    private Listeners listeners = null;

    /**
     * This is used to speed up set operations in the
     * AutomatonSynthesizerSingleFixpoint algorithm
     */
    public int sethelper;

    /** List of incoming transitions. */
    private ArcSet incomingArcs = new ArcSet();
    /** List of outgoing transitions. */
    private ArcSet outgoingArcs = new ArcSet();
    /** List of outgoing edges (multiple transitions on each edge). */
    private final MultiArcSet outgoingMultiArcs = new MultiArcSet();

    /**
     * Stores the cost accumulated from the initial state until this one.
     * The value depends normally (if synchronized automaton) on the path to this state.
     */
    protected double accumulatedCost = UNDEF_COST;

    /**
     * Creates a new state with a specified name.
     */
    public State(final String name)
    {
        Args.checkForContent(name);
        this.name = name;
    }

    /**
     * This copy constructor does only copy the states attributes.
     * The incoming and outgoing arcs are not copied.
     *
     *@param  otherState Description of the Parameter
     */
    public State(final State otherState)
    {
        this(otherState.name, otherState);
    }

    /**
     *
     * This copy constructor does only copy the states attributes.
     * The incoming and outgoing arcs are not copied.
     *
     * @param name the name of the new state
     * @param  otherState Description of the Parameter
     */
    public State(final String name, final State otherState)
    {
        this(name);

        index = otherState.index;
        initial = otherState.initial;
        accepting = otherState.accepting;
        forbidden = otherState.forbidden;
        first = otherState.first;
        last = otherState.last;
        cost = otherState.cost;
        stateSet = otherState.stateSet;
        visited = otherState.visited;
    }

    public State(final State state, final String name)
    {
        this(name);

        incomingArcs = state.incomingArcs;
        outgoingArcs = state.outgoingArcs;

        index = state.index;
        initial = state.initial;
        accepting = state.accepting;
        forbidden = state.forbidden;
        first = state.first;
        last = state.last;
        cost = state.cost;
        stateSet = state.stateSet;
        visited = state.visited;
    }

    public NamedProxy clone()
    {
        return new State(this);
    }

    /**
     * This is an ugly method that only is needed when dealing
     * with automataIndexForm. All methods that works with index
     * needs special initialisation that is not automatically done.
     * This method is not recommended for general use.
     */
    public void setIndex(final int index)
    {
        this.index = index;
    }

    /**
     * This is an ugly method that only is needed when dealing
     * with automataIndexForm. All methods that works with index
     * needs special initialisation that is not automatically done.
     * This method is not recommended for general use.
     */
    public int getIndex()
    {
        return index;
    }

    public int getSynchIndex()
    {
        return getIndex();
    }

    public boolean isFirst()
    {
        return first;
    }

    public void setFirst(final boolean first)
    {
        this.first = first;
    }

    public boolean isLast()
    {
        return last;
    }

    public void setLast(final boolean last)
    {
        this.last = last;
    }

    /**
     * Returns the name of this state.
     */
    public String getName()
    {
        if (name == null)
        {
            return "";
        }

        return name;
    }

    public void setName(final String name)
    {
        Args.checkForContent(name);
        this.name = name;
    }

    public String toString()
    {
        return "'" + getName() + "'";
    }

    public void setInitial(final boolean initial)
    {
        this.initial = initial;
    }

    public boolean isInitial()
    {
        return initial;
    }

    public void setAccepting(final boolean accepting)
    {
        this.accepting = accepting;
    }

    /**
     * Return true if this state is accepting.
     */
    public boolean isAccepting()
    {
        return accepting;
    }

    /**
     * Returns true if this state is accepting.
     *
     * @param considerEpsilonClosure If true, the method returns true also if any state
     * in the epsilon closure is accepting.
     */
    public boolean isAccepting(final boolean considerEpsilonClosure)
    {
        if (!considerEpsilonClosure)
        {
            return isAccepting();
        }
        else
        {
            return epsilonClosure(true).hasAcceptingState();
        }
    }

    public boolean isForbidden()
    {
        return forbidden;
    }

    public void setForbidden(final boolean forbidden)
    {
        this.forbidden = forbidden;

        // What has the forbiddenness to do with cost?!? /Hugo
        if (forbidden)
        {
            cost = State.MAX_COST;
        }
    }

    /**
     * This is only valid after setAssociatedState
     * has been called.
     */
    public State getAssociatedState()
    {
        return assocState;
    }

    /**
     * Set a state as the associated state.
     * This is used when computing the shortest
     * trace to a state.
     */
    public void setAssociatedState(final State assocState)
    {
        this.assocState = assocState;
    }

    public void setSelected(final boolean selected)
    {
        this.selected = selected;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public boolean equals(final Object other)
    {
        if (other instanceof State)
        {
            return name.equals(((State) other).name);
        }
        return false;
    }

    /**
     * True if this state and otherState have equal attributes considering
     * * accepting
     * * forbidden
     */
    public boolean hasEqualMarking(final State otherState)
    {
        return ((accepting == otherState.accepting) && (forbidden == otherState.forbidden));
    }

    public boolean equalState(final State otherState)
    {
        if (!getName().equals(otherState.getName()))
        {
            return false;
        }

        if (initial != otherState.initial)
        {
            return false;
        }

        if (accepting != otherState.accepting)
        {
            return false;
        }

        if (forbidden != otherState.forbidden)
        {
            return false;
        }

        if (first != otherState.first)
        {
            return false;
        }

        if (last != otherState.last)
        {
            return false;
        }

        if (cost != otherState.cost)
        {
            return false;
        }

        if (visited != otherState.visited)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    /**
     * Don't do this in public -- only for use by Automaton
     */
    void addIncomingArc(final Arc theArc)
    {
        incomingArcs.add(theArc);
    }

    /**
     * Don't do this in public -- only for use by Automaton
     */
    void addOutgoingArc(final Arc theArc)
    {
        outgoingArcs.add(theArc);

        MultiArc theArcSet = getArcSet(theArc);
        if (theArcSet == null)
        {
            // Did not find an arcset - generate one.
            theArcSet = new MultiArc(theArc.getFromState(), theArc.getToState());
            outgoingMultiArcs.add(theArcSet);
        }

        theArcSet.add(theArc);
    }

    /**
     * Don't do this in public -- only for use by Automaton
     */
    void removeIncomingArc(final Arc theArc)
    {
        incomingArcs.remove(theArc);
    }

    /**
     * Don't do this in public -- only for use by Automaton
     */
    void removeOutgoingArc(final Arc theArc)
    {
        outgoingArcs.remove(theArc);

        // Also remove the arc from the arcset
        final MultiArc theArcSet = getArcSet(theArc);
        theArcSet.remove(theArc);

        // Remove it if it is empty!
        if (theArcSet.size() == 0)
        {
            outgoingMultiArcs.remove(theArcSet);
        }
    }

    /**
     * Don't do this in public -- only for use by Automaton.
     */
    MultiArc getArcSet(final Arc theArc)
    {
        final State toState = theArc.getToState();

        for (final Iterator<MultiArc> arcSetIt = outgoingMultiArcIterator(); arcSetIt.hasNext(); )
        {
            final MultiArc currArcSet = arcSetIt.next();

            if (currArcSet.getToState() == toState)
            {
                assert (currArcSet.getFromState().equals(this));

                return currArcSet;
            }
        }

        // Couldn't find an arcset
        return null;
    }

    /**
     * Iterates over transitions out of this state (including self-loops).
     */
    public Iterator<Arc> outgoingArcsIterator()
    {
        return outgoingArcs.iterator();
    }

    /**
     * Returns the set of outgoing arcs from this state
     */
    public Set<Arc> getOutgoingArcs()
    {
        final Set<Arc> arcSet = new HashSet<Arc>();
        final Iterator<Arc> iterator = outgoingArcsIterator();
        while(iterator.hasNext())
        {
            arcSet.add(iterator.next());
        }
        return arcSet;
    }

    /**
     * Iterates over edges (all transitions from state A to state B
     * are associated with ONE AND THE SAME edge) out of this state
     * (including self-loops).
     */
    public Iterator<MultiArc> outgoingMultiArcIterator()
    {
        return outgoingMultiArcs.iterator();
    }

    /**
     * Use this iterator when you're planning to fiddle with the arcs.
     *
     * No, don't, there's no need, you can always go around it in better ways.
     * Such as... ?
     *
     * @deprecated
     */
    public Iterator<Arc> safeOutgoingArcsIterator()
    {
        return ((ArcSet) outgoingArcs.clone()).iterator();
    }

    /**
     * Returns true if there is an outgoing arc from this state that is equal to the
     * supplied arc.
     */
    public boolean containsOutgoingArc(final Arc arc)
    {
        // Had to do this ugly iteration since the equals()-method won't work properly
        for (final Iterator<Arc> arcIt = outgoingArcsIterator(); arcIt.hasNext(); )
        {
            if (arc.equals(arcIt.next()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if there is an incoming arc from this state that is equal to the
     * supplied arc.
     */
    public boolean containsIncomingArc(final Arc arc)
    {
        // Had to do this ugly iteration since the equals()-method won't work properly
        for (final Iterator<Arc> arcIt = incomingArcsIterator(); arcIt.hasNext(); )
        {
            if (arc.equals(arcIt.next()))
            {
                return true;
            }
        }

        return false;
    }

    public Iterator<Arc> incomingArcsIterator()
    {
        return incomingArcs.iterator();
    }

    public Iterator<State> nextStateIterator()
    {
        final StateSet nextStates = new StateSet();
        final Iterator<Arc> arcIt = outgoingArcsIterator();

        while (arcIt.hasNext())
        {
            nextStates.add(arcIt.next().getToState());
        }

        return nextStates.iterator();
    }

    public Iterator<State> previousStateIterator()
    {
        final StateSet previousStates = new StateSet();
        final Iterator<Arc> arcIt = incomingArcsIterator();

        while (arcIt.hasNext())
        {
            previousStates.add(arcIt.next().getFromState());
        }

        return previousStates.iterator();
    }

    /**
     * StateIterator for the states that can reach this state in one transition
     * along the event labeled eventLabel.
     */
    //public Iterator<State> previousStateIterator(LabeledEvent event)
    public Iterator<State> previousStateIterator(final String eventLabel)
    {
        final StateSet previousStates = new StateSet();
        final Iterator<Arc> arcIt = incomingArcsIterator();
        while (arcIt.hasNext())
        {
            final Arc arc = arcIt.next();
            if (arc.getEvent().equals(eventLabel))
            {
                previousStates.add(arc.getFromState());
            }
        }

        return previousStates.iterator();
    }

    public int nbrOfIncomingArcs()
    {
        return incomingArcs.size();
    }

    public int nbrOfIncomingUnobservableArcs()
    {
        int count = 0;

        for (final Iterator<Arc> it = incomingArcsIterator(); it.hasNext(); )
        {
            if (!it.next().getEvent().isObservable())
            {
                count++;
            }
        }

        return count;
    }

    public int nbrOfOutgoingArcs()
    {
        return outgoingArcs.size();
    }

    public int nbrOfOutgoingMultiArcs()
    {
        return outgoingMultiArcs.size();
    }

    public boolean isDeadlock()
    {
        return outgoingArcs.size() == 0;
    }

    public void setCost(final double cost)
    {
        this.cost = cost;
    }

    public double getCost()
    {
        return cost;
    }

    /**
     * Method used by e.g. AutomatonMinimizer for faster lookup of which equivalence class
     * a state belongs to.
     */
    public void setStateSet(final StateSet stateSet)    // setEquivalenceClass(Object equivClass)
    {
        // this.equivClass = equivClass;
        this.stateSet = stateSet;
    }

    /**
     * Method used by e.g. AutomatonMinimizer for faster lookup of which equivalence class
     * a state belongs to.
     */
    public StateSet getStateSet()    // Object getEquivalenceClass()
    {
        return stateSet;    // equivClass;
    }

    public void setVisited(final boolean visited)
    {
        this.visited = visited;
    }

    public boolean isVisited()
    {
        return visited;
    }

    public void removeArcs()
    {
        removeOutgoingArcs();
        removeIncomingArcs();
    }

    /**
     * Removes all outgoing arcs from this state.
     * @return the number of removed arcs.
     */
    public int removeOutgoingArcs()
    {
        final int count = outgoingArcs.size();
        final Object[] arcs = outgoingArcs.toArray();
        outgoingArcs.clear();
        for (final Object arc : arcs)
        {
            ((Arc) arc).clear();
        }
        return count;
    }

    /**
     * Removes all incoming arcs from this state.
     * @return the number of removed arcs.
     */
    public int removeIncomingArcs()
    {
        final int count = incomingArcs.size();
        final Object[] arcs = incomingArcs.toArray();
        incomingArcs.clear();
        for (final Object arc : arcs)
        {
            ((Arc) arc).clear();
        }
        return count;
    }

    /**
     * Follow the label "label" and return the next state (only one if many).
     * If such an event is not defined then return null.
     *
     *@param  label Description of the Parameter
     *@return  Description of the Return Value
     */
    public State nextState(final String label)
    {
        final Iterator<Arc> outgoingArcsIt = outgoingArcsIterator();

        while (outgoingArcsIt.hasNext())
        {
            final Arc currArc = outgoingArcsIt.next();

            if (currArc.getEvent().getLabel().equals(label))
            {
                return currArc.getToState();
            }
        }

        return null;
    }

    /**
     * Follow the event "theEvent" and return the next state (only one if many).
     * If "theEvent" is not enabled then return null.
     *
     *@param theEvent The event.
     */
    public State nextState(final LabeledEvent theEvent)
    {
        return nextState(theEvent.getLabel());

    }

    /**
     * Follow the event "theEvent" and return the set of states that may be reached.
     */
    public StateSet nextStates(final LabeledEvent theEvent)
    {
        return nextStates(theEvent, false);
    }

    /**
     * Follow the event "theEvent" and return the set of states that may be reached.
     * Optionally also consider the states reachable also after any number of
     * epsilon events before and/or after "theEvent".
     *
     * Note that at least "theEvent" MUST be executed.
     *
     * This method should not be called for epsilon events normally (it may be sensible
     * in some special case but remember that there may be many epsilon events in the same
     * automaton and that the epsilon closure considers all of them as the same but this
     * method may treat them as unique! See (@link #epsilonClosure).
     *
     * @param theEvent the event that must be executed.
     * @param considerEpsilonClosure if true, an arbitrary number of epsilon events may be
     * executed before and after theEvent, otherwise, only theEvent is executed.
     */
    public StateSet nextStates(final LabeledEvent theEvent, final boolean considerEpsilonClosure)
    {
        final StateSet states = new StateSet();

                /* Sometimes we want this!
                assert (!theEvent.isEpsilon());    // See above!
                 */

        // Do the stuff
        Iterator<Arc> outgoingArcsIt;
        if (considerEpsilonClosure)
        {
            outgoingArcsIt = epsilonClosure(true).outgoingArcsIterator();
        }
        else
        {
            outgoingArcsIt = outgoingArcsIterator();
        }

        while (outgoingArcsIt.hasNext())
        {
            final Arc currArc = outgoingArcsIt.next();

            if (currArc.getEvent().equals(theEvent))
            {
                if (considerEpsilonClosure)
                {
                    states.addAll(currArc.getToState().epsilonClosure(true));
                }
                else
                {
                    states.add(currArc.getToState());
                }
            }
        }

        return states;
    }

    /**
     * Calculates and returns (forward) epsilon closure as a StateSet. Optionally, the closure
     * does or does not necessarily include the state from which the closure is calculated.
     *
     * @param includeSelf if true, this State itself is included even if no epsilon transitions
     * leads to it (a loop), if false, at least one epsilon transition must be executed and this
     * State itself may not be in the returned set (if there is no loop).
     * @return the states that can be reached by executing at least one epsilon transition.
     */
    public StateSet epsilonClosure(final boolean includeSelf)
    {
        return epsilonClosure(includeSelf, true, true);
    }
    /**
     * Same as epsilonClosure(boolean includeSelf) but you can choose
     * whether you want silent controllable and/or silent
     * uncontrollable transitions.
     *
     * @param includeControllable if true, silent controllable transitions count for the closure.
     * @param includeUncontrollable if true, silent uncontrollable transitions count for the closure.
     */
    public StateSet epsilonClosure(final boolean includeSelf, final boolean includeControllable, final boolean includeUncontrollable)
    {
        final StateSet result = new StateSet();

        // Include self?
        if (includeSelf)
        {
            result.add(this);
        }

        // Examine states
        final LinkedList<State> statesToExamine = new LinkedList<State>();
        statesToExamine.add(this);
        while (statesToExamine.size() != 0)
        {
            final State currState = statesToExamine.removeFirst();

            for (final Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
            {
                final Arc currArc = arcIt.next();
                final State state = currArc.getToState();

                // Is this an epsilon event that we care about?
                if (!currArc.getEvent().isObservable() && !currArc.isSelfLoop() && !result.contains(state) &&
                    ((includeControllable && includeUncontrollable) ||
                    (includeControllable && currArc.getEvent().isControllable()) ||
                    (includeUncontrollable && !currArc.getEvent().isControllable())))
                {
                    statesToExamine.add(state);
                    result.add(state);
                }
            }
        }

        return result;
    }

    /**
     * Calculates and returns backwards epsilon closure as a StateSet. The closure includes the
     * state from which the closure is calculated.
     */
    public StateSet backwardsEpsilonClosure()
    {
        final StateSet result = new StateSet();

        result.add(this);

        final LinkedList<State> statesToExamine = new LinkedList<State>();

        statesToExamine.add(this);

        while (statesToExamine.size() != 0)
        {
            final State currState = statesToExamine.removeFirst();

            for (final Iterator<Arc> arcIt = currState.incomingArcsIterator(); arcIt.hasNext(); )
            {
                final Arc currArc = arcIt.next();
                final State state = currArc.getFromState();

                if (!currArc.getEvent().isObservable() &&!currArc.isSelfLoop() &&!result.contains(state))
                {
                    statesToExamine.add(state);
                    result.add(state);
                }
            }
        }

        return result;
    }


    /**
     * Returns true if this state has an outgoing arc for this event .
     */
    public boolean doesDefine(final LabeledEvent event)
    {
        return doesDefine(event, false);
    }

    /**
     * Returns true if an event is defined in this state. that is, has an outgoing arc for this event
     * If considerEpsilonClosure is true, it can be anywhere in the epsilon closure
     * of this state instead of just in this state.
     */
    private boolean doesDefine(final LabeledEvent event, final boolean considerEpsilonClosure)
    {
        Iterator<Arc> arc_it;

        if (considerEpsilonClosure)
        {
            arc_it = epsilonClosure(true).outgoingArcsIterator();
        }
        else
        {
            arc_it = outgoingArcsIterator();
        }

        while (arc_it.hasNext())
        {
            final Arc curr_arc = arc_it.next();

            if (curr_arc.getEvent().equals(event))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the alphabet of active events. Epsilon events are left out.
     */
    public Alphabet activeEvents(final boolean considerEpsilonClosure)
    {
        final Alphabet enabled = new Alphabet();
        Iterator<Arc> arcIt;

        if (considerEpsilonClosure)
        {
            arcIt = epsilonClosure(true).outgoingArcsIterator();
        }
        else
        {
            arcIt = outgoingArcsIterator();
        }

        while (arcIt.hasNext())
        {
            final LabeledEvent event = arcIt.next().getEvent();

            if (!enabled.contains(event) && event.isObservable())
            {
                enabled.addEvent(event);
            }
        }

        return enabled;
    }

    public Listeners getListeners()
    {
        if (listeners == null)
        {
            listeners = new Listeners(this);
        }

        return listeners;
    }

    protected void notifyListeners()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }

    /**
     * Returns the cost accumulated when this state is reached. Note that the
     * path to the state is of importance.
     */
    public double getAccumulatedCost()
    {
        return accumulatedCost;
    }

    public void initCosts()
    {
        accumulatedCost = 0;
    }

    /**
     * Updates the accumulated cost. This method is overloaded in CompositeState.
     */
    public void updateCosts(final State prevState)
    {
        updateCosts(prevState, prevState.getAccumulatedCost());
    }

    /**
     * This method updates explicitly the accumulatedCost. Normally, this version
     * of updateCosts() is only used from within Node.class.
     */
    public void updateCosts(final State prevState, final double accumulatedCost)
    {
        this.accumulatedCost = accumulatedCost + prevState.getCost();
    }

    ///////////////////////////////////
    // Comparable interface  methods //
    ///////////////////////////////////

    /**
     * Implementation of the Comparable interface. Compares the id of the states.
     */

    public int compareTo(final NamedProxy partner)
    {
        return name.compareTo(((State) partner).name);
    }

//    //////////////////
//    // Kripke stuff //
//    //////////////////
//
//    /**
//     * The set of labels in this state.
//     */
//    private Set<KripkeLabel> labels = null;
//
//    /**
//     * Returns the set of labels in this state.
//     */
//    public Set<KripkeLabel> getKripkeLabels()
//    {
//        if (labels == null)
//        {
//            // The default label set of a state is a set containing just one label (based on the name).
//            labels = new TreeSet<KripkeLabel>();
//            labels.add(new KripkeLabel(getName()));
//            return labels;
//        }
//        return labels;
//    }
//
//    /**
//     * Sets the set of labels in this state.
//     */
//    public void setKripkeLabels(Set<KripkeLabel> set)
//    {
//        labels = set;
//    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.Proxy
    public Class<StateProxy> getProxyInterface()
    {
        return StateProxy.class;
    }

    public Object acceptVisitor(final ProxyVisitor visitor)
        throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor =
            (ProductDESProxyVisitor) visitor;
        return desvisitor.visitStateProxy(this);
    }

    public boolean refequals(final NamedProxy partner)
    {
        return getName().equals(partner.getName());
    }

    public int refHashCode()
    {
        return getName().hashCode();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.AutomatonProxy
    public Collection<EventProxy> getPropositions()
    {
        final LinkedList<EventProxy> currPropositions = new LinkedList<EventProxy>();
        if (isAccepting() && !isForbidden())
        {
            currPropositions.add(acceptingProposition);
        }
        if (isForbidden())
        {
            currPropositions.add(forbiddenProposition);
        }
        return currPropositions;
    }

}
