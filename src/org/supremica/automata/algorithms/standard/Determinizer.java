package org.supremica.automata.algorithms.standard;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;
import org.supremica.automata.StateSets;


public class Determinizer
{
    private static Logger logger = LogManager.getLogger(Determinizer.class);
    private final Automaton automaton;
    private Automaton newAutomaton = null;

    // private Alphabet epsilons = null;
    private EpsilonTester epsilonTester = null;
    private final StateSets openStateSets = new StateSets();
    private final StateSets closedStateSets = new StateSets();
    @SuppressWarnings("unused")
	private final StateSet openStates = new StateSet();
    @SuppressWarnings("unused")
	private final StateSet closedStates = new StateSet();
    private boolean checkControlInconsistencies = false;
    private boolean resolveControlInconsistencies = false;
    private boolean isControlInconsistent = false;

    // Debug stuff
    @SuppressWarnings("unused")
	private final int tabs = 0;    // keeps track on num tabs to insert, for formatting output

    /**
     * For this automaton, determinize with respect to the events marked as isEpsilon()
     */
    public Determinizer(final Automaton automaton)
    {
        this.automaton = automaton;
        this.epsilonTester = new DefaultEpsilonTester();
        this.newAutomaton = createNewAutomaton();
    }

    /**
     * For this automaton, determinize with respect to the given events
     * If contains == true, the events are considered to be epsilons
     * If contains == false, the events are considered as non-epsilons (and all other epsilons)
     */
    public Determinizer(final Automaton automaton, final Alphabet events, final boolean contains)
    {
        this.automaton = automaton;
        this.epsilonTester = new AlphaEpsilonTester(events, contains);
        this.newAutomaton = createNewAutomaton();
    }

    /**
     * Determinize using a supplied epsilonTester for deciding which (and when...) events are
     * considered to be epsilons.
     */
    public Determinizer(final Automaton automaton, final EpsilonTester epsilonTester)
    {
        this.automaton = automaton;
        this.epsilonTester = epsilonTester;
        this.newAutomaton = createNewAutomaton();
    }

    public void checkControlInconsistencies(final boolean checkControlInconsistencies)
    {
        this.checkControlInconsistencies = checkControlInconsistencies;
    }

    public void resolveControlInconsistencies(final boolean resolveControlInconsistencies)
    {
        this.resolveControlInconsistencies = resolveControlInconsistencies;
    }

    public boolean isControlInconsistent()
    throws IllegalStateException
    {
        if (checkControlInconsistencies || resolveControlInconsistencies)
        {
            return isControlInconsistent;
        }
        else
        {
            throw new IllegalStateException("Only allowed to be called when checkControlInconsistencies or resolvedControlInconsistencies are set to true");
        }
    }

    public void execute()
    {
        // Clear the caches - is this necessary?
        for (final Iterator<State> state_it = automaton.stateIterator();
        state_it.hasNext(); )
        {
            final State state = state_it.next();

            state.setStateSet(null);
        }

        logger.debug("Executing Determinizer(" + automaton.getName() + ") " + epsilonTester.showWhatYouGot());

                /* This is how it goes - a variant of van Noords

                let Qinit = epsilonClosure(initialState)
                put Qinit on openStates

                while openStates is not empty
                {
                        get Q1 from openStates

                        for all non-epsilon events e
                        {
                            let Q2 = eventClosure(Q1, e) [the state set Q2 reached from Q1 via e, one-step]
                                let Q2c = epsilonClosure(Q2)
                                if(Q2c != empty)
                                {
                                        add Q2c to openStates
                                        add arc <Q1, e, Q2c>
                                }
                        }

                        put Q1 on closedStates
                }

                and that's how it goes */

        // This should be the one and only initial state!
        final StateSet initset = epsilonClosure(automaton.getInitialState());

        // put it on openStateSets (if not already seen)
        add(initset);

        // Create initial state
        final State init = initset.getSingleStateRepresentation();
        newAutomaton.addState(init);
        newAutomaton.setInitialState(init);

        // Loop as long as there are StateSet:s in openStateSets
        while (!openStateSets.isEmpty())
        {
            // Note that the getSingleStateRepresentation call ALWAYS returns a new representation
            // i.e. (if you look at the implementation ni StateSet) it never reuses the same
            // representation, the local singleStateRepresentation is never null!! (I think.)
            final StateSet Q1 = openStateSets.get();    // This is vanNoords T
            final State T = newAutomaton.addStateChecked(Q1.getSingleStateRepresentation());

            // for each event not to be disregarded, calc the closure, create the arc
            final Iterator<LabeledEvent> it = automaton.getAlphabet().iterator();
            while (it.hasNext())
            {
                final LabeledEvent e = it.next();
                if (!epsilonTester.isThisEpsilon(e))
                {
                    // From this set, via this event, calc the reached state
                    final StateSet Q2 = eventClosure(Q1, e);    // this is vanNoords U
                    final StateSet Q2c = epsilonClosure(Q2);

                    if (!Q2c.isEmpty())
                    {
                        if (checkControlInconsistencies)
                        {
                            final Alphabet inconsistentEvents = automaton.getControlInconsistentEvents(Q2c);

                            if (inconsistentEvents.size() > 0)
                            {
                                logger.debug("In states " + Q2c.toString() + " the following events are inconsistent " + inconsistentEvents.toString());

                                isControlInconsistent = true;
                            }
                        }

                        if (resolveControlInconsistencies)
                        {
                            final Alphabet inconsistentEvents = automaton.resolveControlInconsistencies(Q2c);

                            if (inconsistentEvents.size() > 0)
                            {
                                logger.debug("In states " + Q2c.toString() + " the following events are inconsistent (resolved) " + inconsistentEvents.toString());

                                isControlInconsistent = true;
                            }
                        }

                        add(Q2c);

                        final State U = newAutomaton.addStateChecked(Q2c.getSingleStateRepresentation());

                        newAutomaton.addArc(new Arc(T, U, e));
                    }
                }
            }

            openStateSets.remove(Q1);
            closedStateSets.add(Q1);    // "mark" this set as done
        }

        // Give the new automaton an appropriate comment
        if (automaton.getName() != "")
        {
            newAutomaton.setComment("detm(" + automaton.getName() + ")");
        }
        else
        {
            newAutomaton.setComment("detm(" + automaton.getComment() + ")");
        }
    }

    public Automaton getNewAutomaton()
    {
        return newAutomaton;
    }

    @SuppressWarnings("deprecation")
	private Automaton createNewAutomaton()
    {
        final Automaton aut = new Automaton();
        aut.setType(automaton.getType());

        // Add all events except the epsilons
        final Iterator<LabeledEvent> alphait = automaton.getAlphabet().iterator();
        while (alphait.hasNext())
        {
            final LabeledEvent event = alphait.next();

            if (!epsilonTester.isThisEpsilon(event))
            {
                try    // we know it can't throw, still we have to do this
                {
                    aut.getAlphabet().addEvent(event);
                }
                catch (final Exception excp)
                {
                    logger.error(excp);
                    logger.debug(excp.getStackTrace());
                }
            }
        }

        return aut;
    }

    private void add(final StateSet states)
    {
        if (!openStateSets.contains(states) &&!closedStateSets.contains(states))
        {
            // printTabs();
            // System.out.println("adding " + states.toString());
            openStateSets.add(states);

            // never mind marking and forbidden for now
        }
    }

    // Calc the (one-step) event "closure" for this event from these states
    // Note that the original states are not necessarily included
    private StateSet eventClosure(final StateSet states, final LabeledEvent event)
    {
        debugPrint("(evCSsE) eventClosure(" + states.toString() + ", " + event.getLabel() + ")", true);

        final StateSet closure = new StateSet();

        // System.out.println("(evCSsE) closure instantiated");
        /* State */
        final Iterator<State> stateit = states.iterator();

        // System.out.println("(evCSsE) stateit created");
        while (stateit.hasNext())
        {
            // System.out.println("stateit hasNext(), yes");
            final State state = stateit.next();

            // System.out.println("(evCSsE) Calling eventTransition(" + state.getName() + ", " + event.getLabel() + ")");
            final StateSet ss = eventTransition(state, event);

            // System.out.println("(evCSsE) Performing the union");
            closure.addAll(ss);
        }

        debugPrint("(evCSsE) return closure " + closure.toString() + ")", false);

        return closure;
    }

    // For this state, calc the states reached in one step on the given events
    // Note that the original state is _not_necessarily_ included (no implicit selfloops, that is)
    @SuppressWarnings("unused")
	private StateSet eventTransition(final State state, final Alphabet alpha)
    {
        debugPrint("(evTSA) eventTransition(" + state.getName() + ", " + alpha.toString() + ")", true);

        final StateSet stateset = new StateSet();
        /* LabeledEvent */
        final Iterator<LabeledEvent> eventit = alpha.iterator();

        while (eventit.hasNext())
        {
            stateset.addAll(eventTransition(state, eventit.next()));
        }

        debugPrint("(evTSA) return " + stateset.toString() + ")", false);

        return stateset;
    }

    // For this specific state, calc the states (might be more than one!) reached via this event
    // Note that the original state is _not_necessarily_ included (no implicit selfloops, that is)
    private StateSet eventTransition(final State state, final LabeledEvent event)
    {
        debugPrint("(evTSE) eventTransition(" + state.getName() + ", " + event.getLabel() + ")", true);

        final StateSet stateset = new StateSet();

        for (final Iterator<Arc> it = state.outgoingArcsIterator(); it.hasNext(); )
        {
            final Arc arc = it.next();

            if (arc.getEvent().equals(event))
            {
                stateset.add(arc.getToState());

                // printTabs();
                // System.out.println("(eTSE) Added state " + arc.getToState().getName() + ", stateset = " + stateset.toString());
            }
        }

        debugPrint("(evTSE) return " + stateset.toString() + ")", false);

        return stateset;
    }

    // Calc the epsilon closure for this set of states
    // For each state, calc its closure and return the union of the lot
    private StateSet epsilonClosure(final StateSet states)
    {
        debugPrint("(eCSS) epsilonClosure(" + states.toString() + ")", true);

        final StateSet closure = new StateSet();

        /* StateSet. */
        for (final Iterator<State> stateit = states.iterator(); stateit.hasNext(); )
        {
            closure.addAll(epsilonClosure(stateit.next()));
        }

        debugPrint("(eCSS) return closure" + closure.toString() + ")", false);

        return closure;
    }

    /**
     * Calc the epsilon closure for this particular state
     */
    public StateSet epsilonClosure(final State state)
    {
        debugPrint("(eCS) epsilonClosure(" + state.getName() + ")", true);

        if (state.getStateSet() != null)    // if already calculated and cached, return it
        {
            final StateSet closure = state.getStateSet();

            debugPrint("(eCS) return cached closure" + closure.toString() + ")", false);

            return state.getStateSet();
        }

        final StateSet closure = new StateSet();
        StateSet tempset = new StateSet();

        // The original state is always included in the closure
        closure.add(state);

        // And we begin calculating from that state
        tempset.add(state);

        // No states are ever removed, so we can use the size to determine when fixpoint reached
        int nextsize = closure.size();    // we use the size to determine fixpoint
        int prevsize = 0;    // no states are removed, so same size means we're finished
        if (prevsize != nextsize)
        {
            do
            {
                prevsize = nextsize;
                tempset = epsilonTransition(tempset);    // call eTSS

                closure.addAll(tempset);

                nextsize = closure.size();
            }
            while (prevsize != nextsize);
        }

        state.setStateSet(closure);    // cache the result
        debugPrint("(eCS) return closure" + closure.toString() + ")", false);

        return closure;
    }

    // Calc the one-step epsilon transitions for this set of states
    // Note that the original states are _not_necessarily_ included,
    // That is, there are no implicit epilon moves to the same state,
    // this has to be taken care of above this level
    private StateSet epsilonTransition(final StateSet states)
    {
        debugPrint("(eTSS) epsilonTransition(" + states.toString() + ")", true);

        final StateSet stateset = new StateSet();    // empty set

        // Iterate over the states and create the union of their respective closures
        for (final Iterator<State> it = states.iterator(); it.hasNext(); )
        {
            stateset.addAll(epsilonTransition(it.next()));
        }

        debugPrint("(eTSS) return " + stateset.toString() + ")", false);

        return stateset;
    }

    // calc the one-step epsilon transitions for this specific state
    private StateSet epsilonTransition(final State state)
    {
        debugPrint("(eTS) epsilonTransition(" + state.getName() + ")", true);

        final StateSet stateset = new StateSet();

        for (final Iterator<Arc> it = state.outgoingArcsIterator(); it.hasNext(); )
        {
            try
            {
                final Arc arc = it.next();

                if (epsilonTester.isThisEpsilon(arc.getEvent()))    // automaton.getEvent(arc).isEpsilon())
                {
                    stateset.add(arc.getToState());
                }
            }
            // Note this is a (ugly?) workaround, since I know that getEvent _cannot_ throw above
            // I just got the arc from a state of the automaton. Thus, getEvent must be able to
            // perform correctly, else the automaton is broken and we are lost anyway.
            catch (final Exception excp)
            {
                logger.debug(excp.getStackTrace());

                throw new RuntimeException(excp);
            }
        }

        debugPrint("(eTS) return " + stateset.toString() + ")", false);

        return stateset;
    }

    // Walk the state of the automaton, and null out its stateclass (helps the GC)
    public void cleanUp()
    {
        for (final Iterator<State> stateit = automaton.stateIterator();
        stateit.hasNext(); )
        {
            final State state = stateit.next();

            state.setStateSet(null);
        }
    }

    // Walk the state of the automaton, and null out its stateclass (helps the GC)
    public void cleanUpDebug()
    {
        /* State */
        for (final Iterator<State> stateit = automaton.stateIterator(); stateit.hasNext(); )
        {
            final State state = stateit.next();

            // System.out.print(state.getName());
            final StateSet stateset = state.getStateSet();

            if (stateset != null)
            {
                System.out.println(": " + stateset.toString());
            }
            else
            {
                System.out.println(": <null state class>");
            }

            state.setStateSet(null);
        }
    }

    // To test stuff, no GUI
    public static void main(final String[] args)
    {
        //logger.setLogToConsole(true);

        final Automaton automaton = new Automaton("Determinizer test");
        final State q0 = new State("q0");

        automaton.addState(q0);
        automaton.setInitialState(q0);

        final State q1 = new State("q1");

        automaton.addState(q1);

        final State q2 = new State("q2");

        automaton.addState(q2);

        final State q3 = new State("q3");

        automaton.addState(q3);

        final State q4 = new State("q4");

        automaton.addState(q4);

        final State q5 = new State("q5");

        automaton.addState(q5);
        q5.setAccepting(true);

        final LabeledEvent a = new LabeledEvent("a");

        automaton.getAlphabet().addEvent(a);

        final LabeledEvent b = new LabeledEvent("b");

        automaton.getAlphabet().addEvent(b);
        b.setUnobservable(true);

        final LabeledEvent c = new LabeledEvent("c");

        automaton.getAlphabet().addEvent(c);
        c.setUnobservable(true);

        final LabeledEvent d = new LabeledEvent("d");

        automaton.getAlphabet().addEvent(d);

        final Alphabet epsilons = new Alphabet();

        epsilons.addEvent(b);
        epsilons.addEvent(c);
        automaton.addArc(new Arc(q0, q1, a));
        automaton.addArc(new Arc(q0, q1, b));
        automaton.addArc(new Arc(q1, q2, b));
        automaton.addArc(new Arc(q1, q3, c));
        automaton.addArc(new Arc(q2, q4, d));
        automaton.addArc(new Arc(q3, q5, d));

/*
                                automaton.addArc(new Arc(q0, q0, a));
                                automaton.addArc(new Arc(q0, q1, c));
                                automaton.addArc(new Arc(q1, q2, a));

 */
        final Determinizer detm = new Determinizer(automaton);

        detm.execute();
        detm.cleanUpDebug();

        final org.supremica.automata.IO.AutomatonToDsx todsx = new org.supremica.automata.IO.AutomatonToDsx(detm.getNewAutomaton());

        try
        {
            todsx.serialize(new java.io.PrintWriter(System.out));
        }
        catch (final Exception excp)
        {
            logger.error(excp);
            logger.debug(excp.getStackTrace());
        }
    }

    private void debugPrint(final String str, final boolean enter)
    {

                /*      if(enter)
                                                ++tabs;

                                printTabs();
                                System.out.println(str);

                                if(!enter)
                                                --tabs;
                 */

        //logger.debug(str);
    }

    // end debug stuff
}
