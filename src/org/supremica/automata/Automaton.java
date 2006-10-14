
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

import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.log.*;
import org.supremica.util.SupremicaIterator;
import org.supremica.properties.Config;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.plain.base.AbstractNamedElement;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;

public class Automaton
    implements AutomatonProxy, Iterable<State>
{
    private static Logger logger = LoggerFactory.createLogger(Automaton.class);

    /**
     * The name of the automaton.
     */
    private String name;

    /**
     * A temporary name, used as a suggestion for a name to the gui when
     * adding a new automaton, so that the gui can avoid giving two automata
     * the same name.
     */
    private String comment;

    // private List theStates = new LinkedList();
    private final StateSet theStates = new StateSet();
    private final Alphabet alphabet;
    //private Map idStateMap;    // Want fast lookup on both id and index (but not name?)
    private int index = -1;
    private Map<Integer,State> indexStateMap;
    //private ArcSet theArcs;
    private State initialState;
    private boolean isDisabled = false;
    private AutomatonType type = AutomatonType.SPECIFICATION;
    private int uniqueStateIndex = 0;

    // Graphical stuff
    private boolean hasLayout = false;
    private int width = -1;
    private int height = -1;

    private AutomatonListeners listeners = null;

    private AutomatonProxy correspondingAutomatonProxy = null;

    /**
     * Creates an empty automaton.
     */
    public Automaton()
    {
        alphabet = new Alphabet();
        indexStateMap = new HashMap<Integer,State>();
    }

    /**
     * Creates an empty automaton with a specified name.
     */
    public Automaton(String name)
    {
        this();

        setName(name);
    }

    /**
     * Creates an automaton that is a copy of another.
     */
    public Automaton(Automaton orgAut)
    {
        this();
        beginTransaction();

        // Deep copy of alphabet...
        alphabet.union(new Alphabet(orgAut.getAlphabet()));

        type = orgAut.type;
        name = new String(orgAut.name == null ? "" : orgAut.name);
        comment = new String(orgAut.comment == null ? "" : orgAut.comment);

        // Create all states
        for (State orgState : orgAut.iterableStates())
        {
            State newState = new State(orgState);

            addState(newState);
        }

        for (Arc arc : orgAut.iterableArcs())
        {
            // We can use indices which is much faster, since the indices can not have changed!
            //State fromState = getStateWithName(arc.getFromState().getName());
            //State toState = getStateWithName(arc.getToState().getName());
            State fromState = getStateWithIndex(arc.getFromState().getIndex());
            State toState = getStateWithIndex(arc.getToState().getIndex());
            LabeledEvent event = alphabet.getEvent(arc.getEvent().getLabel());
            Arc newArc = new Arc(fromState, toState, event);

            addArc(newArc);
        }

        endTransaction();
    }

    public AutomatonProxy clone()
    {
        return new Automaton(this);
    }

    /**
     * Sets the type of this automaton (e.g. AutomatonType.PLANT, AutomatonType.SPECIFICATION).
     *
     * @see AutomatonType
     */
    public void setType(AutomatonType type)
    throws IllegalArgumentException
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type must be non-null");
        }

        this.type = type;
    }

    /**
     * Returns the type of this automaton.
     */
    public AutomatonType getType()
    {
        if (type == null)
        {
            return AutomatonType.UNDEFINED;
        }

        return type;
    }

    public boolean isSupervisor()
    {
        return type == AutomatonType.SUPERVISOR;
    }

    public boolean isSpecification()
    {
        return type == AutomatonType.SPECIFICATION;
    }

    public boolean isPlant()
    {
        return type == AutomatonType.PLANT;
    }

    public boolean isUndefined()
    {
        return type == AutomatonType.UNDEFINED;
    }

    /**
     * Sets the name of this automaton.
     */
    public void setName(String name)
    throws IllegalArgumentException
    {
                /*
                if (name == null)
                {
                                throw new IllegalArgumentException("Name must be non-null");
                }
                 */
        String oldName = this.name;

        this.name = name;

        notifyListeners(AutomatonListeners.MODE_AUTOMATON_RENAMED, oldName);
    }

    /**
     * Returns the name of the automaton, or, if there is no name, returns the comment.
     */
    public String getName()
    {
        if ((name == null) || (name.equals("") && !getComment().equals("")))
        {
            // This solved some ugly problems...
            // but this isn't all that beautiful either... /hguo
            if (comment != null)
            {
                return getComment();
            }
            else
            {
                logger.error("Error in Automaton.java. Automaton with empty name and comment detected!");
            }

            return "";
        }

        return name;
    }

    /**
     * Returns true if the automaton has a name (not comment) that is not null or empty.
     */
    public boolean hasName()
    {
        return !((name == null) || (name == ""));
    }

    /**
     * Returns the comment of the automaton.
     */
    public String getComment()
    {
        if (comment == null)
        {
            return "";
        }

        return comment;
    }

    /**
     * Sets the comment of the automaton.
     */
    public void setComment(String comment)
    throws IllegalArgumentException
    {
        if (comment == null)
        {
            throw new IllegalArgumentException("Comment must be non-null");
        }

        this.comment = comment;
    }

    public void setDisabled(boolean isDisabled)
    {
        this.isDisabled = isDisabled;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }

    //  When is the slow and beautiful fixx due? When EXACTLY does the indices "become different"?
    /**
     * Quick and ugly fixx, see bug report
     * When working with the state-indices, sometimes, somehow the indices in
     * indexStateMap become different from the indices stored in the State:s
     * This func iterates over the states and rebuilds the map
     * Used by ModifiedAstar
     */
    public void remapStateIndices()
    {
        indexStateMap.clear();
        for (State state : iterableStates())
        {
            indexStateMap.put(new Integer(state.getIndex()), state);
        }
    }

    /**
     * Logs a sorted list of the state indices.
     */
    private void checkStateIndices()
    {
        TreeSet<Integer> sort = new TreeSet<Integer>();

        for (State state : iterableStates())
        {
            sort.add(new Integer(state.getIndex()));
        }

        for (Integer i : sort )
        {
            logger.error(i.toString());
        }
    }

    /**
     * Adds a state to this automaton. If this is supposed to be the initial state, make
     * sure that the state is set to be initial BEFORE adding it with this method.
     *
     * @return 1 if the state is already in the automaton, otherwise returns 0.
     */
    public int addState(State state)
    throws IllegalArgumentException
    {
        if (state == null)
        {
            throw new IllegalArgumentException("State must be non-null");
        }

        if (theStates.contains(state))
        {
            logger.warn("Automaton " + this + " already contains the state " + state + ".");
            return 1;
        }

        theStates.add(state);

        if (state.getIndex() == -1)
        {
            state.setIndex(getUniqueStateIndex());
        }

        //idStateMap.put(state.getId(), state);
        indexStateMap.put(new Integer(state.getIndex()), state);

        if (state.isInitial())
        {
            this.initialState = state;
        }

        notifyListeners(AutomatonListeners.MODE_STATE_ADDED, state);

        return 0;
    }

    /**
     * Returns a list of control inconsisten events among the given
     * states are consistent.  We have an inconsistency if an event is
     * explicitly disabled in one state and explicitly enabled in
     * another.  To represent the explicitly disabled events they must
     * exist from the state but they are assumed to end in an
     * forbidden state.  The states in stateset must be contained in
     * the current automaton.
     */
    public Alphabet getControlInconsistentEvents(StateSet stateset)
    {
        Alphabet explicitlyForbiddenEvents = new Alphabet();

        // We start by computing the set of explicitly
        // forbidden events
        for (State currState : iterableStates())
        {
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator();
            arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                State toState = currArc.getToState();

                if (toState.isForbidden())
                {
                    LabeledEvent currEvent = currArc.getEvent();

                    if (!explicitlyForbiddenEvents.contains(currEvent))
                    {
                        try
                        {
                            explicitlyForbiddenEvents.addEvent(currEvent);
                        }
                        catch (Exception ex)
                        {
                            logger.error("Could not add event in getControlInconsistentStates", ex);
                        }
                    }
                }
            }
        }

        // We continuing by iterating over the
        // all explicitly allowed events and check
        // that those are not in the list of explicitly
        // forbidden events
        Alphabet controlInconsistentEvents = new Alphabet();

        for (State currState : iterableStates())
        {

            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator();
            arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                State toState = currArc.getToState();

                if (!toState.isForbidden())

                {
                    LabeledEvent currEvent = currArc.getEvent();

                    if (explicitlyForbiddenEvents.contains(currEvent))
                    {
                        try
                        {
                            controlInconsistentEvents.addEvent(currEvent);
                        }
                        catch (Exception ex)
                        {
                            logger.error("Could not add event in getControlInconsistentStates", ex);
                        }
                    }
                }
            }
        }

        return controlInconsistentEvents;
    }

    /**
     * Sets an explicitly allowed arc (event) to point to a forbidden
     * state if that events is involved in a control
     * inconsistency. See getControlInconsistentEvents.
     */
    public Alphabet resolveControlInconsistencies(StateSet stateset)
    {
        Alphabet explicitlyForbiddenEvents = new Alphabet();
        Map eventToStateMap = new HashMap();

        // We start by computing the set of explicitly
        // forbidden events
        for (State currState : iterableStates())
        {
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator();
            arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                State toState = currArc.getToState();

                if (toState.isForbidden())
                {
                    LabeledEvent currEvent = currArc.getEvent();

                    if (!explicitlyForbiddenEvents.contains(currEvent))
                    {
                        try
                        {
                            explicitlyForbiddenEvents.addEvent(currEvent);
                            eventToStateMap.put(currEvent, toState);
                        }
                        catch (Exception ex)
                        {
                            logger.error("Could not add event in resolveControlInconsistencies", ex);
                        }
                    }
                }
            }
        }

        // We continuing by iterating over the
        // all explicitly allowed events and check
        // that those are not in the list of explicitly
        // forbidden events
        Alphabet controlInconsistentEvents = new Alphabet();

        for (State currState : iterableStates())
        {
            for (Iterator<Arc> arcIt = currState.outgoingArcsIterator();
            arcIt.hasNext(); )
            {
                Arc currArc = arcIt.next();
                State toState = currArc.getToState();

                if (!toState.isForbidden())
                {
                    LabeledEvent currEvent = currArc.getEvent();

                    if (explicitlyForbiddenEvents.contains(currEvent))
                    {
                        try
                        {
                            State forbiddenState = (State) eventToStateMap.get(currEvent);

                            currArc.setToState(forbiddenState);
                            controlInconsistentEvents.addEvent(currEvent);
                        }
                        catch (Exception ex)
                        {
                            logger.error("Could not add event in getControlInconsistentStates", ex);
                        }
                    }
                }
            }
        }

        return controlInconsistentEvents;
    }

    /**
     * An event is redundant if it is self-looped in all states and
     *  * The event is controllable or
     *  * The event is uncontrollable and the automaton is a spec/sup
     *
     * Only works for deterministic automata.
     *
     * @return the Alphabet of redundant events.
     */
    public Alphabet getRedundantEvents()
    {
        assert(isDeterministic());

        Alphabet result = new Alphabet();

        loop: for (Iterator<LabeledEvent> evIt = eventIterator(); evIt.hasNext(); )
        {
            LabeledEvent event = evIt.next();
            for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
            {
                State state = stIt.next();
                if (!state.doesDefine(event))
                {
                    continue loop;
                }
                for (Iterator<MultiArc> multiArcIt = state.outgoingMultiArcIterator(); multiArcIt.hasNext(); )
                {
                    MultiArc multiArc = multiArcIt.next();
                    if (multiArc.contains(event) && !multiArc.isSelfLoop())
                    {
                        continue loop;
                    }
                }
            }
            // Only add if the event is defined in all states and is
            // selflooped in the transition where it is defined.
            if (!(isPlant() && !event.isControllable()))
            {
                result.addEvent(event);
            }
        }

        return result;
    }

    /**
     * If a state with this id (and/or name?) already exists, return the existing state
     * Else, add this state and return it
     */
    public State addStateChecked(State state)
    throws IllegalArgumentException
    {
        if (state == null)
        {
            throw new IllegalArgumentException("State must be non-null");
        }

        State existing = getStateWithName(state.getName());
        if (existing != null)
        {
            return existing;
        }

        // else, add it as usual
        addState(state);

        return state;
    }

    public void removeState(State state)
    throws IllegalArgumentException
    {
        if (state == null)
        {
            throw new IllegalArgumentException("State must be non-null");
        }

        if (state == initialState)
        {
            initialState = null;
        }

        theStates.remove(state);
        state.removeArcs();
        //String id = state.getId();
        //idStateMap.remove(id);
        int index = state.getIndex();
        indexStateMap.remove(new Integer(index));
        notifyListeners(AutomatonListeners.MODE_STATE_REMOVED, state);
    }

    public boolean hasInitialState()
    {
        return initialState != null;
    }

    public State getInitialState()
    {
        return initialState;
    }

    /**
     * Returns true if this is the null automaton, i.e. this
     * automaton does not have an initial state.
     */ // is this the correct def of "null automaton"? what about empty state-set? //MF
    public boolean isNullAutomaton()
    {
        return !hasInitialState();
    }

    // This is a fixx, for now - see bug report
    public void setInitialState(State state)
    throws IllegalArgumentException
    {
        if (state == null)
        {
            throw new IllegalArgumentException("State must be non-null");
        }

        State oldinit = getInitialState();
        State newinit = getStateWithName(state.getName());

        if (newinit == null)
        {
            throw new IllegalStateException("No such state, " + state);
        }

        newinit.setInitial(true);

        initialState = newinit;

        if (oldinit != null)
        {
            oldinit.setInitial(false);
        }
    }

    /**
     * Returns a uniquely named (and id'ed) state.
     * Passing null or empty prefix sets prefix to 'q'
     * The new state is not added to the state set
     */
    public State createUniqueState(String prefix)
    {
        StringBuffer name = null;

        if ((prefix == null) || prefix.equals(""))
        {
            name = new StringBuffer("r");
        }
        else
        {
            name = new StringBuffer(prefix);
        }

        while (containsStateWithName(name.toString()))
        {
            name.append(uniqueStateIndex++);
        }

        return new State(name.toString());
    }

    public State createUniqueState()
    {
        return createUniqueState("p");
    }

    /**
     * Returns true if it finds an accepting state, else returns false
     * Iterates over all states _only_if_ no accepting states exist (or only
     * the last one is accepting)
     */
    public boolean hasAcceptingState()
    {
        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            if (currState.isAccepting())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if it finds a nonaccepting state, else returns false
     * Iterates over all states _only_if_ no accepting states exist (or only
     * the last one is accepting)
     */
    public boolean hasNonacceptingState()
    {
        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            if (!currState.isAccepting())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * True if automaton has at least one self loop.
     */
    public boolean hasSelfLoop()
    {
        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();

            if (currArc.isSelfLoop())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Examines if the automaton is deterministic.
     * @return true if automaton is deterministic.
     */
    public boolean isDeterministic()
    {
        HashSet foundEvents = new HashSet();

        for (Iterator<State> stIt = stateIterator();	stIt.hasNext(); )
        {
            State currState = stIt.next();

            foundEvents.clear();

            for (Iterator<Arc> evIt = currState.outgoingArcsIterator(); evIt.hasNext(); )
            {
                LabeledEvent currEvent = evIt.next().getEvent();

                // Epsilon event?
                if (!currEvent.isObservable())
                {
                    return false;
                }

                // Has this event been seen in another transition from this state?
                boolean newElement = foundEvents.add(currEvent.getLabel());
                if (!newElement)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isAllEventsPrioritized()
    {
        return alphabet.isAllEventsPrioritized();
    }

    public boolean isAllEventsObservable()
    {
        return alphabet.isAllEventsObservable();
    }

    /**
     * When adding an arc, both the two states associated with the
     * arc _must_ already be contained in the automaton, otherwise the
     * behavior is undefined.
     *
     * What about the event? //MF
     */
    public void addArc(Arc arc)
    throws IllegalArgumentException
    {
        if (arc == null)
        {
            throw new IllegalArgumentException("Arc must be non-null");
        }

        // Add the arc to the individual states
        arc.getFromState().addOutgoingArc(arc);
        arc.getToState().addIncomingArc(arc);

//		arc.getListeners().addListener(this);
//		theArcs.addArc(arc);
//		notifyListeners(AutomatonListeners.MODE_ARC_ADDED, arc);
    }

    public void removeArc(Arc arc)
    throws IllegalArgumentException
    {
        if (arc == null)
        {
            throw new IllegalArgumentException("Arc must be non-null");
        }

        arc.clear();
        //theArcs.removeArc(arc);
        //notifyListeners(AutomatonListeners.MODE_ARC_REMOVED, arc);
    }

        /*
        public void removeArcs(ArcSet arcSet)
        {
                try
                {
                        for (Iterator<Arc> arcIt = arcSet.iterator(); arcIt.hasNext(); )
                        {
                                removeArc(arcIt.next());
                        }
                }
                catch (Exception ex)
                {
                        logger.error("Error in Automaton.java when removing arcs.");
                }
        }
         */

    public boolean containsState(State state)
    throws IllegalArgumentException
    {
        if (state == null)
        {
            throw new IllegalArgumentException("State must be non-null");
        }

        //return idStateMap.containsKey(state.getId());
        return theStates.contains(state);
    }

    /**
     * Gets the states in this automaton.
     */
    public StateSet getStateSet()
    {
        return theStates;
    }

    public Set<StateProxy> getStates()
    {
        return getStateSet().getWatersStates();
    }


    public IterableStates iterableStates()
    {
        return new IterableStates(theStates);
    }

    public IterableEvents iterableEvents()
    {
        return new IterableEvents(alphabet);
    }

    public IterableArcs iterableArcs()
    {
        return new IterableArcs(theStates);
    }

    public Iterator<Arc> arcIterator()
    {
        return theStates.outgoingArcsIterator();
    }

    public Collection<TransitionProxy> getTransitions()
    {
        LinkedList<TransitionProxy> transitions = new LinkedList<TransitionProxy>();

        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();
            transitions.add(currArc);
        }

        return transitions;
    }

/*
        public List<Arc> getArcs()
        {
                List<Arc> arcList = new LinkedList<Arc>();

                return Collections.unmodifiableList(arcList);
        }
 */

    // Note, searches on id - only call this with states in this automaton
        /*
        public State getState(State state)
                throws IllegalArgumentException
        {
                if (state == null)
                {
                        throw new IllegalArgumentException("State must be non-null");
                }

                //return (State) idStateMap.get(state.getId());
        }
         */

    /**
     * Given this state, which belongs to this stateset, return a unique id-string
     */
        /*
        public String getUniqueStateId(State state)
        {
                // prereq: state is in theStates:
                return state.getId();    // at the moment do the simplest thing
        }
         */

    /**
     * True if a state with the name exists, otherwise false.
     */
    public boolean containsStateWithName(String name)
    throws IllegalArgumentException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name must be non-null");
        }

        State theState = getStateWithName(name);

        return theState != null;
    }

    /**
     * Returns the state with the asked for name if it exists, otherwise null.
     */
    public State getStateWithName(String name)
    throws IllegalArgumentException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name must be non-null");
        }

        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            State currState = stIt.next();

            if (currState.getName().equals(name))
            {
                return currState;
            }
        }

        return null;
    }

        /*
        private boolean containsStateWithId(String id)
        {
                return idStateMap.containsKey(id);
        }
         */

        /*
        private State getStateWithId(String id)
        {
                return (State) idStateMap.get(id);
        }
         */

    // The index stuff should be exclusive to AutomataIndexForm, but how to manage that?

    /**
     * This is an ugly method that is only needed when dealing
     * with automataIndexForm. All methods that works with index
     * needs special initialisation that is not automatically done.
     * This method is not recommended for general use.
     */
    public boolean containsStateWithIndex(int index)
    {
        return indexStateMap.containsKey(new Integer(index));
    }

    /**
     * This is an ugly method that only is needed when dealing
     * with automataIndexForm. All methods that works with index
     * needs special initialisation that is not automatically done.
     * This method is not recommended for general use.
     */
    public State getStateWithIndex(int index)
    {
        return (State) indexStateMap.get(new Integer(index));
    }

    // end index stuff

    /**
     * Returns an iterator to all states in this automaton
     * that has an event with eventLabel as an outoing event.
     */
    public Iterator<State> statesThatEnableEventIterator(String eventLabel)
    throws IllegalStateException
    {
        if (eventLabel == null)
        {
            throw new IllegalArgumentException("EventLabel must be non-null");
        }

        Iterator<State> stIt = new InternalStateIterator(eventLabel, true);
        return stIt;
    }

    /**
     * Returns true if the event with label eventLabel is prioritized in this
     * automaton. If the event is not included in this automaton or is not
     * prioritized then it returns false.
     */ // should throw exception if the event is not in this alphabet? //MF
    public boolean isEventPrioritized(String eventLabel)
    throws IllegalArgumentException
    {
        if (eventLabel == null)
        {
            throw new IllegalArgumentException("EventLabel must be non-null");
        }

        if (alphabet.contains(eventLabel))
        {
            LabeledEvent thisEvent = alphabet.getEvent(eventLabel);

            return thisEvent.isPrioritized();
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns number of states in this automaton.
     */
    public int nbrOfStates()
    {
        return theStates.size();
    }

    /**
     * Returns number of events in this automaton's alphabet.
     */
    public int nbrOfEvents()
    {
        return alphabet.size();
    }

    /**
     * Returns number of transitions in this automaton.
     */
    public int nbrOfTransitions()
    {
        int amount = 0;
        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();
            amount++;
        }

        return amount;

                /*
                // Calculate the sum of the outgoing arcs in the states
                int amount = 0;
                for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
                {
                        amount += stIt.next().nbrOfOutgoingArcs();
                }

                return amount;
                 */
    }

    /**
     * Returns number of transitions in this automaton that are associated with epsilon-events.
     */
    public int nbrOfEpsilonTransitions()
    {
        int amount = 0;

        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();

            if (!currArc.getEvent().isObservable())
            {
                amount++;
            }
        }

        return amount;
    }

    /**
     * Amount of selfloops in automaton.
     */
    public int nbrOfSelfLoops()
    {
        int amount = 0;

        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();

            if (currArc.isSelfLoop())
            {
                amount++;
            }
        }

        return amount;
    }

    /**
     * Amount of selfloops of the events in anAlphabet
     */
    public int nbrOfSelfLoops(Alphabet anAlphabet)
    {
        int amount = 0;

        for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();

            if (currArc.isSelfLoop() && anAlphabet.contains(currArc.getEvent()))
            {
                amount++;
            }
        }

        return amount;
    }

    public int nbrOfAcceptingStates()
    {
        int nbrOfAcceptingStates = 0;
        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            State currState = stIt.next();

            if (currState.isAccepting())
            {
                nbrOfAcceptingStates++;
            }
        }

        return nbrOfAcceptingStates;
    }

    public int nbrOfForbiddenStates()
    {
        int nbrOfForbiddenStates = 0;
        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            State currState = stIt.next();

            if (currState.isForbidden())
            {
                nbrOfForbiddenStates++;
            }
        }

        return nbrOfForbiddenStates;
    }

    public int nbrOfAcceptingAndForbiddenStates()
    {
        int nbrOfAcceptingAndForbiddenStates = 0;
        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            State currState = stIt.next();

            if (currState.isAccepting() && currState.isForbidden())
            {
                nbrOfAcceptingAndForbiddenStates++;
            }
        }

        return nbrOfAcceptingAndForbiddenStates;
    }

    /**
     * Returns depth of automaton.
     */
    public int depth()
    {
        int depth = 0;
        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            // Measure the shortest trace to the state.
            State currState = stIt.next();
            int stateDepth;

            try
            {
                stateDepth = getTrace(currState).size();
            }
            catch (Exception ex)
            {
                logger.error(ex);

                return Integer.MAX_VALUE;
            }

            if (stateDepth > depth)
            {
                depth = stateDepth;
            }
        }

        return depth;
    }

    /**
     * Returns sum of depths of transitions of events in anAlphabet.
     */
    public int depthSum(Alphabet anAlphabet)
    {
        int depthSum = 0;
        for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
        {
            // Measure the shortest trace to the state.
            State currState = stIt.next();
            int stateDepth;

            try
            {
                stateDepth = getTrace(currState).size();
            }
            catch (Exception ex)
            {
                logger.error(ex);

                return Integer.MAX_VALUE;
            }

            // Calculate sum of
            for (Iterator<Arc> arcIterator = currState.outgoingArcsIterator();
            arcIterator.hasNext(); )
            {
                if (anAlphabet.contains(arcIterator.next().getEvent()))
                {
                    depthSum += stateDepth;
                }
            }
        }

        return depthSum;
    }

    /**
     * Returns an iterator over the states.
     */
    public Iterator<State> stateIterator()
    {
        return theStates.iterator();
    }

    /**
     * Returns the state iterator.
     */
    public Iterator<State> iterator()
    {
        return stateIterator();
    }

    /**
     * Use this iterator instead of stateIterator when you add or
     * remove states in this automaton.
     */
    public Iterator<State> safeStateIterator()
    {
        return (new StateSet(theStates)).iterator();
    }

    public Iterator<LabeledEvent> eventIterator()
    {
        return alphabet.iterator();
    }

    public Collection eventCollection()
    {
        return alphabet.values();
    }

    public Alphabet getAlphabet()
    {
        return alphabet;
    }

    public Set<EventProxy> getEvents()
    {
        return getAlphabet().getWatersEventsWithPropositions();
    }

    /**
     * In some situation, for example in the dot output
     * not all state identities can be accepeted. For example
     * dot does not handle dots in the statename.
     * This method resets all state identities to valid names
     */
    public void normalizeStateIdentities()
    {
        setStateIndices();

        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            currState.setName("n" + currState.getIndex());
        }

        remapStateIndices();
    }

    public void setIndices()
    {
        setIndices(0);
    }

    void setIndices(int automatonIndex)
    {
        index = automatonIndex;

        alphabet.setIndices();
        setStateIndices();
    }

    void setIndices(int automatonIndex, Alphabet otherAlphabet)
    {
        index = automatonIndex;

        alphabet.setIndices(otherAlphabet);
        setStateIndices();
    }

    private void setStateIndices()
    {
        int i = 0;

        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            currState.setIndex(i++);
        }
    }

    void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public int getSynchIndex()
    {
        return getIndex();
    }

    /**
     * Don't do this in public
     */
    private int getUniqueStateIndex()
    {
        while (containsStateWithIndex(uniqueStateIndex))
        {
            uniqueStateIndex++;
        }

        return uniqueStateIndex;
    }

    public void clearVisitedStates()
    {
        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            currState.setVisited(false);
        }
    }

    public void clearSelectedStates()
    {
        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            currState.setSelected(false);
        }
    }

    private void removeAssociatedStateFromUnvisitedStates()
    {
        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            if (!currState.isVisited())
            {
                currState.setAssociatedState(null);
            }
        }
    }

    // Do these really belong here? I think not. Better use a wrapper //MF
    /**
     * Returns the shortest trace from the initial state to toState.
     */
    public LabelTrace getTrace(State toState)
    throws Exception, IllegalArgumentException, IllegalStateException
    {
        if (toState == null)
        {
            throw new IllegalArgumentException("Automaton.getTrace: toState must be non-null");
        }

        if (!hasInitialState())
        {
            throw new IllegalStateException("There is no initial state");
        }

        return getTrace(getInitialState(), toState);
    }

    /**
     * Returns the shortest trace from fromState to toState,
     */
    public LabelTrace getTrace(State fromState, State toState)
    throws Exception, IllegalArgumentException
    {
        if (fromState == null)
        {
            throw new IllegalArgumentException("Automaton.getTrace: fromState must be non-null");
        }

        if (toState == null)
        {
            throw new IllegalArgumentException("Automaton.getTrace: toState must be non-null");
        }

        computeShortestPath(fromState);

        LabelTrace theTrace = new LabelTrace();

        // Remove the associated state from all states that
        // is not on the way between fromState and toState.
        State thisState = toState;

        thisState.setVisited(true);

        State prevState = thisState.getAssociatedState();

        while (prevState != null)
        {
            LabeledEvent currEvent = getLabeledEvent(prevState, thisState);

            if (currEvent == null)
            {
                throw new SupremicaException("Could not find an arc from " + prevState.getName() + " to " + thisState.getName());
            }

            theTrace.addFirst(currEvent.getLabel());

            thisState = prevState;
            prevState = prevState.getAssociatedState();
        }

        reverseAssociatedState(toState);

        return theTrace;
    }

    private void computeShortestPath(State fromState)
    throws IllegalArgumentException
    {
        if (fromState == null)
        {
            throw new IllegalArgumentException("Automaton.getTrace: fromState is null");
        }

        clearVisitedStates();

        // This implements a breath first search
        LinkedList openStates = new LinkedList();

        fromState.setAssociatedState(null);
        openStates.addLast(fromState);
        fromState.setVisited(true);

        while (openStates.size() > 0)
        {
            State currState = (State) openStates.removeFirst();

            for (Iterator arcIt = currState.outgoingArcsIterator();
            arcIt.hasNext(); )
            {
                Arc currArc = (Arc) arcIt.next();
                State currToState = currArc.getToState();

                if (!currToState.isVisited())
                {
                    currToState.setAssociatedState(currState);
                    currToState.setVisited(true);
                    openStates.addLast(currToState);
                }
            }
        }
    }

    /**
     * In computeShortestPath the associatedStates are backwards. I.e. they
     * point from the to states towards the initial state. After the computation
     * we want the arcs in the opposite direction.
     */
    private void reverseAssociatedState(State toState)
    {
        clearVisitedStates();
        reverseAssociatedState(toState, null);
        removeAssociatedStateFromUnvisitedStates();
    }

    private void reverseAssociatedState(State currState, State nextState)
    {
        if (currState != null)
        {
            currState.setVisited(true);
        }

        if (currState.getAssociatedState() == null)
        {
            currState.setAssociatedState(nextState);

            return;
        }

        reverseAssociatedState(currState.getAssociatedState(), currState);
        currState.setAssociatedState(nextState);
    }
    // End path-computing stuff that does not really belong here //MF

    /**
     * Change the marking of all states in the automaon.
     */
    public void setAllStatesAccepting()
    {
        setAllStatesAccepting(false);
    }
    public void setAllStatesAccepting(boolean keepForbidden)
    {
        beginTransaction();

        for (Iterator<State> stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = stateIt.next();

            if (keepForbidden)
            {
                if (!currState.isForbidden())
                {
                    currState.setAccepting(true);
                }
            }
            else
            {
                currState.setAccepting(true);
                currState.setForbidden(false);
            }
        }

        invalidate();
        endTransaction();
    }

    /**
     * Makes all accepting states non-accepting
     * and all non-accepting states accepting.
     */ 
    public void invertMarking()
    {
        for (State currState : this)
        {
            currState.setAccepting(!currState.isAccepting());
        }
    }

    /**
     * Returns an event on an arc that starts in fromState and ends in toState.
     * If no such event exists, null is returned.
     */
    public LabeledEvent getLabeledEvent(State fromState, State toState)
    throws Exception
    {
        for (Iterator<Arc> arcIt = fromState.outgoingArcsIterator();
        arcIt.hasNext(); )
        {
            Arc currArc = arcIt.next();
            State currToState = currArc.getToState();

            if (currToState == toState)
            {
                return currArc.getEvent();
            }
        }

        return null;
    }

    /**
     * Hides (makes epsilon) the supplied events.
     */
    public void hide(Alphabet alpha, boolean preserveControllability)
    {
        // Don't hide nothing!
        if ((alpha == null) || (alpha.size() == 0))
        {
            return;
        }

        // Remove the hidden events from alphabet
        getAlphabet().minus(alpha);

        // Do we care about controllability?
        if (!preserveControllability)
        {
            // Get/create silent event tau
            String silentName = Config.MINIMIZATION_SILENT_EVENT_NAME.get();
            LabeledEvent tau = getAlphabet().getEvent(silentName);
            if (tau == null)
            {
                tau = new LabeledEvent(silentName);
                tau.setUnobservable(true);
                getAlphabet().addEvent(tau);
            }
            else
            {
                if (tau.isObservable())
                {
                    logger.error("The event name " + silentName +
                        " is reserved and must be unobservable!");
                    return;
                }
            }

            // Modify arcs
            for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
            {
                Arc arc = arcIt.next();

                // Hide this one?
                if (alpha.contains(arc.getEvent()))
                {
                    arc.setEvent(tau);
                }
            }
        }
        else
        {
            // Get/create silent events tau_c and tau_u
            // tau_c
            String silentCName = Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.get();
            LabeledEvent tau_c = getAlphabet().getEvent(silentCName);
            if (tau_c == null)
            {
                tau_c = new LabeledEvent(silentCName);
                tau_c.setUnobservable(true);
                tau_c.setControllable(true);
                getAlphabet().addEvent(tau_c);
            }
            else
            {
                if (tau_c.isObservable() || !tau_c.isControllable())
                {
                    logger.error("The event name " + silentCName +
                        " is reserved and must be controllable and unobservable!");
                    return;
                }
            }
            // tau_u
            String silentUName = Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.get();
            LabeledEvent tau_u = getAlphabet().getEvent(silentUName);
            if (tau_u == null)
            {
                tau_u = new LabeledEvent(silentUName);
                tau_u.setUnobservable(true);
                tau_u.setControllable(false);
                getAlphabet().addEvent(tau_u);
            }
            else
            {
                if (tau_u.isObservable() || tau_u.isControllable())
                {
                    logger.error("The event name " + silentUName +
                        " is reserved and must be uncontrollable and unobservable!");
                    return;
                }
            }

            // Modify arcs
            for (Iterator<Arc> arcIt = arcIterator(); arcIt.hasNext(); )
            {
                Arc arc = arcIt.next();

                // Hide this one?
                if (alpha.contains(arc.getEvent()))
                {
                    if (arc.getEvent().isControllable())
                        arc.setEvent(tau_c);
                    else
                        arc.setEvent(tau_u);
                }
            }
        }
    }

    public void removeAllStates()
    {
        beginTransaction();
        theStates.clear();
        indexStateMap.clear();
        theStates.clear();
        //theArcs.clear();

        initialState = null;

        if (listeners != null)
        {
            listeners.setUpdateNeeded(true);
        }

        endTransaction();
    }

    public State getState(int x, int y)
    {
        Iterator stateIt = stateIterator();

        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();

            if (currState.contains(x, y))
            {
                return currState;
            }
        }

        return null;
    }

    // What does layout, height, width etc has to do with automaton? this does not belong here! wrap! //MF
    public boolean hasLayout()
    {
        return hasLayout;
    }

    public void setHasLayout(boolean hasLayout)
    {
        this.hasLayout = hasLayout;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public boolean validLayout()
    {
        if ((width < 0) || (height < 0))
        {
            return false;
        }

        for (Iterator stateIt = stateIterator(); stateIt.hasNext(); )
        {
            State currState = (State) stateIt.next();

            if (!currState.validLayout())
            {
                return false;
            }
        }

        return true;
    }
    // End layout stuff that does not belong in this interface

    public int nbrOfControllableEvents()
    {
        return alphabet.nbrOfControllableEvents();
    }

    public int nbrOfPrioritizedEvents()
    {
        return alphabet.nbrOfPrioritizedEvents();
    }

    public int nbrOfImmediateEvents()
    {
        return alphabet.nbrOfImmediateEvents();
    }

    public int nbrOfEpsilonEvents()
    {
        return alphabet.nbrOfUnobservableEvents();
    }

    public int nbrOfUnobservableEvents()
    {
        return alphabet.nbrOfUnobservableEvents();
    }

    /**
     * Returns true if there are no obvious differences between this
     * automaton and the other. Note, that this method only compares the
     * number of states and transitions, etc. This method does not guarantee
     * that the two automata generates the same language.
     *
     * Shouldn't there really be a CompareAutomata class?
     * This class should have methods like isIsomorphic() and languageEqual()
     * // yes, definitively there should be a CompareAutomata class //MF
     */
    public boolean equalAutomaton(Automaton other)
    {
        // Should type, name, comment really be considered?
        if (!getName().equals(other.getName()))
        {
            return false;
        }

        if (getType() != other.getType())
        {
            return false;
        }

        if (!getComment().equals(other.getComment()))
        {
            return false;
        }

        // The following stuff seems useful to consider
        if (hasAcceptingState() != other.hasAcceptingState())
        {
            return false;
        }
        if (hasSelfLoop() != other.hasSelfLoop())
        {
            return false;
        }
        if (isDeterministic() != other.isDeterministic())
        {
            return false;
        }
        if (isAllEventsPrioritized() != other.isAllEventsPrioritized())
        {
            return false;
        }
        if (nbrOfAcceptingStates() != other.nbrOfAcceptingStates())
        {
            return false;
        }
        if (nbrOfForbiddenStates() != other.nbrOfForbiddenStates())
        {
            return false;
        }
        if (nbrOfAcceptingAndForbiddenStates() != other.nbrOfAcceptingAndForbiddenStates())
        {
            return false;
        }
        if (!alphabet.equalAlphabet(other.alphabet))
        {
            return false;
        }

        return true;
    }

    public void setCorrespondingAutomatonProxy(AutomatonProxy correspondingAutomatonProxy)
    {
        this.correspondingAutomatonProxy = correspondingAutomatonProxy;
    }

    public boolean hasCorrespondingAutomatonProxy()
    {
        return correspondingAutomatonProxy != null;
    }

    public AutomatonProxy getCorrespondingAutomatonProxy()
    {
        return correspondingAutomatonProxy;
    }


    public Listeners getListeners()
    {
        if (listeners == null)
        {
            listeners = new AutomatonListeners(this);
        }

        return listeners;
    }

    public void addListener(AutomatonListener listener)
    {
        Listeners currListeners = getListeners();

        currListeners.addListener(listener);
    }

    private void notifyListeners(int mode, Object o)
    {
        if (listeners != null)
        {
            listeners.notifyListeners(mode, o);
        }
    }

    private void notifyListeners()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }

    public void invalidate()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }

    public void beginTransaction()
    {
        // System.err.println("beginTransaction");
        if (listeners != null)
        {
            listeners.beginTransaction();
        }
    }

    public void endTransaction()
    {
        // System.err.println("endTransaction");
        if (listeners != null)
        {
            listeners.endTransaction();
        }
    }

    public void updated(Object o)
    {}

    public int hashCode()
    {
        // Generate hascode from name, or if that's null, from the comment
        if (name != null)
        {
            return name.hashCode();
        }
        else if (comment != null)
        {
            return comment.hashCode();
        }

        logger.error("Error in Automaton.java. Some automaton has both name and comment empty! This is NOT recommended!");

        return 0;
    }

    // These are unary Automaton operators, they do belong here //MF
    // So move them somewhere else! That's how things get done around here... :o) //Hugo

    /**
     * Saturate the automaton with arcs on events not defined for a
     * state, going to a uniquely named dump state.
     *
     * This unary operation makes the automaton "complete" in the
     * computer science sense.
     */
    public boolean saturateDump()
    {
        return saturateDump(getAlphabet());
    }

    /**
     * Saturate with dump-state for a certain sub-alphabet.
     * We assume that alpha is a subset of the alphabet of the automaton.
     */
    public boolean saturateDump(Alphabet alpha)
    {
        assert(alpha.isSubsetOf(getAlphabet()));

        boolean done_something = false; // keep track so we don't add dump-states over and over

        // Create uniquely named dump state
        State dump = createUniqueState("dump");
        addState(dump);
        // saturate, else something will always be done
        saturate(dump, alpha, dump);

        for (Iterator<State> state_it = safeStateIterator(); state_it.hasNext(); )
        {
            State state = state_it.next();
            done_something |= saturate(state, alpha, dump);	// saturate to the dump-state
        }
        
        // Remove all outgoing arcs from the dump-state.
        dump.removeOutgoingArcs();

        // Need to remove dump state including its self-loop arcs?
        if (done_something == false)
        {
            removeState(dump);
        }

        return done_something;
    }

    /**
     * Saturate the automaton with self-loops on events not defined
     * for a state.  This unary operation makes the automaton
     * "complete" in the sense.that all events are defined for all
     * states
     */
    public boolean saturateLoop()
    {
        return saturateLoop(getAlphabet());
    }

    /**
     * Saturate with self-loops for a certain sub-alphabet.  Beware,
     * we assume that alpha is a subset of the alphabet of the
     * automaton
     */
    public boolean saturateLoop(Alphabet alpha)
    {
        boolean done_something = false;

        Iterator<State> state_it = safeStateIterator();
        while(state_it.hasNext())
        {
            State state = (State)state_it.next(); // Why doesn't a Iterator<State> return a State?
            done_something |= saturate(state, alpha, state);	// saturate with self-loops
        }

        return done_something;
    }

    /**
     * Add an arc <from_state, event, to-state> for each event in
     * alpha that is not defined in from_state. Returns true if
     * anything added.
     *
     * Note that this one costs you |Q|x|A|x|T|, where Q is the
     * state-set, A the given alphabet and T the transitions from the
     * certain state (plus the ones we're adding while we work!)
     * ... at the moment -- there *must* be a better way!
     */
    private boolean saturate(State from_state, Alphabet alpha, State to_state)
    {
        boolean done_something = false;
        Iterator<LabeledEvent> event_it = alpha.iterator();
        while(event_it.hasNext())
        {
            LabeledEvent event = event_it.next();
            if(from_state.doesDefine(event) == false) // this event not defined for this state, add it
            {
                addArc(new Arc(from_state, to_state, event));
                done_something = true;
            }
        }
        return done_something;
    }

    /**
     * Returns the alphabet of obviously "inadequate" events (there
     * may (but rarely are) be more). With "adequate" as defined in
     * "On the set of certain conflicts of a given language" by Robi
     * Malik.
     *
     * An inadequate event is, for example, one that is self-looped in
     * all states of an automaton. Controllable inadequate events can
     * always be removed from the alphabet, uncontrollable self-looped
     * events can safely be removed from specification/supervisors and
     * from plants IF the event is present in some other plant of the
     * system.
     */
    public Alphabet getInadequateEvents()
    {
        Alphabet inadequate = new Alphabet();

        // For all events...
        eventLoop: for (Iterator<LabeledEvent> evIt = eventIterator(); evIt.hasNext(); )
        {
            LabeledEvent event = evIt.next();
            if (isPlant() && !event.isControllable())
            {
                continue eventLoop;
            }
            // ... and all states ...
            stateLoop: for (Iterator<State> stIt = stateIterator(); stIt.hasNext(); )
            {
                State state = stIt.next();
                // ... there must be a self-loop ...
                for (Iterator<Arc> arcIt = state.outgoingArcsIterator(); arcIt.hasNext(); )
                {
                    Arc arc = arcIt.next();
                    if (arc.isSelfLoop() && arc.getEvent().equals(event))
                    {
                        continue stateLoop;
                    }
                }
                // ... or the event is NOT inadequate!
                continue eventLoop;
            }
            inadequate.addEvent(event);
        }
        return inadequate;
    }

    public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
    {
        final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
        return desvisitor.visitAutomatonProxy(this);
    }

    public boolean equalsByContents(final Proxy partner)
    {
        Automaton partnerAutomaton = (Automaton)partner;
        if (getName().equals(partnerAutomaton.getName()))
        {
            return
                getKind().equals(partnerAutomaton.getKind()) &&
                EqualCollection.isEqualSetByContents(getEvents(), partnerAutomaton.getEvents()) &&
                EqualCollection.isEqualSetByContents(getStates(), partnerAutomaton.getStates()) &&
                EqualCollection.isEqualSetByContents(getTransitions(), partnerAutomaton.getTransitions());
        }
        else
        {
            return false;
        }
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
        result += EqualCollection.getSetHashCodeByContents(getEvents());
        result *= 5;
        result += EqualCollection.getSetHashCodeByContents(getStates());
        result *= 5;
        result += EqualCollection.getSetHashCodeByContents(getTransitions());
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

    public int compareTo(NamedProxy partner)
    {
        return getName().compareTo(((Automaton) partner).getName());
    }


    public static void main(String[] args)
    {
        Automaton theAutomaton = new Automaton();
        State stateZero = new State("zero");

        theAutomaton.addState(stateZero);

        State st = theAutomaton.getStateWithIndex(0);    // should be stateZero (not?)

        if (st == null)
        {
            System.err.println("st == null");
        }
        else
        {
            System.err.println("st != null");
        }
    }

    class InternalEventIterator
        implements Iterator<LabeledEvent>
    {
        private final Iterator arcIt;

        public InternalEventIterator(Iterator arcIt)
        {
            this.arcIt = arcIt;
        }

        public boolean hasNext()
        {
            return arcIt.hasNext();
        }

        public LabeledEvent next()
        {
            Arc nextArc = (Arc) arcIt.next();

            // String eventId = nextArc.getEventId();
            LabeledEvent nextEvent = null;

            try
            {
                nextEvent = nextArc.getEvent();    // eventId);
            }
            catch (Exception ex)
            {
                logger.error("Automaton::InternalEventIterator.next: Error in getEvent", ex);
                logger.debug(ex.getStackTrace());
            }

            return nextEvent;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    class InternalStateIterator
        implements Iterator<State>
    {
        private final Iterator<Arc> arcIt;
        private State currState = null;
        private String eventLabel;
        private boolean outgoing;

        public InternalStateIterator(String eventLabel, boolean outgoing)
        {
            this.eventLabel = eventLabel;
            this.outgoing = outgoing;
            this.arcIt = arcIterator();
            this.currState = null;

            findNext();
        }

        public boolean hasNext()
        {
            return currState != null;
        }

        public State next()
        {
            State returnState = currState;

            findNext();

            return returnState;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private void findNext()
        {
            while (arcIt.hasNext())
            {
                Arc currArc = arcIt.next();
                String currLabel = currArc.getLabel();

                if (eventLabel.equals(currLabel))
                {
                    if (outgoing)
                    {
                        currState = currArc.getFromState();

                        return;
                    }
                    else
                    {
                        currState = currArc.getToState();

                        return;
                    }
                }
            }

            currState = null;
        }
    }

    // These are useful for debugging (etc)
    public String toDebugString()
    {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append(getName());
        sbuf.append("::");

        for (Iterator it = arcIterator(); it.hasNext(); )
        {
            Arc arc = (Arc) it.next();

            sbuf.append(arc.toString());
        }

        return sbuf.toString();
    }

    public String toString()
    {
        return "'" + getName() + "'";
    }

    // toCode writes Java code - Note, the names are used as-is, which means there may be blanks in variabel names!
    public String toCode()
    {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append("Automaton " + getName() + " = new Automaton(\"" + getName() + "\");");
        sbuf.append("\t\t{\t\t\t" + getName() + ".setType(AutomatonType." + getType().toString() + ");\n");

        for (Iterator<State> sit = stateIterator(); sit.hasNext(); )
        {
            State state = sit.next();

            sbuf.append("State " + state.getName() + " = new State(\"" + state.getName() + "\");");
            sbuf.append("\t" + state.getName() + ".setCost(" + state.getCost() + ");");
            sbuf.append("\t" + getName() + ".addState(" + state.getName() + ");");

            if (state.isInitial())
            {
                sbuf.append("\t" + getName() + ".setInitialState(" + state.getName() + ");");
            }

            if (state.isAccepting())
            {
                sbuf.append("\t" + state.getName() + ".setAccepting(true);");
            }

            sbuf.append("\n");
        }

        for (Iterator<LabeledEvent> eit = getAlphabet().iterator(); eit.hasNext(); )
        {
            LabeledEvent ev = eit.next();

            sbuf.append("LabeledEvent " + ev.getLabel() + " = new LabeledEvent(\"" + ev.getLabel() + "\");");
            sbuf.append("\t" + getName() + ".getAlphabet().addEvent(" + ev.getLabel() + ");\n");
        }

        for (Iterator<Arc> ait = arcIterator(); ait.hasNext(); )
        {
            Arc arc = ait.next();

            sbuf.append(getName() + ".addArc(new Arc(" + arc.getFromState().getName() + ", " + arc.getToState().getName() + ", " + arc.getEvent().getLabel() + "));\n");
        }

        sbuf.append("}\n");

        return sbuf.toString();
    }

    public ComponentKind getKind()
    {
        return AutomatonType.toKind(type);
    }

}
