package org.supremica.automata.algorithms.bbsd;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.properties.Config;


public class BBSDDiagnosabilityVerification {

    private static Logger logger = LogManager.getLogger(BBSDDiagnosabilityVerification.class);

    private final BBSDAbstraction bbsd = new BBSDAbstraction();

    private static Automaton result;

    /*private final ActionTimer timer1 = new ActionTimer(); // TOTAL
    private final ActionTimer timer2 = new ActionTimer(); // SEPARATION INTO Gv_N and Gv_F
    private final ActionTimer timer3 = new ActionTimer(); // CALCULATE GV
    private final ActionTimer timer4 = new ActionTimer(); // SYNCHRONISATION
    private final ActionTimer timer5 = new ActionTimer();*/

    public BBSDDiagnosabilityVerification(final Automata theAutomata) {

        /*timer1.reset();
        timer2.reset();
        timer3.reset();
        timer4.reset();
        timer5.reset();
        BBSDAbstraction.timer.reset();*/

        //timer1.start();

        final HashSet<LabeledEvent> local_events = new HashSet<LabeledEvent>();
        final Hashtable<State, Integer> state_labels = new Hashtable<State, Integer>();

        if (theAutomata.size() == 1 && !theAutomata.getFirstAutomaton().getAlphabet().contains("f")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Since you only selected ONE automaton, you have entered a debugging mode. If this was not intended, please select 2+ automatons and try again.");

            final Automaton aut = new Automaton(theAutomata.getFirstAutomaton());
            local_events.clear();
            for (final LabeledEvent e : aut.iterableEvents())
                if (e.isUnobservable() || e.getLabel().equals("tau"))
                    local_events.add(e);
            for (final State s : aut.iterableStates())
                state_labels.put(s, ((s.isForbidden()) ? 2 : 1));

            result = new Automaton("BBSDDiagnosabilityVerification");
            result = bbsd.calculateAbstraction(aut, local_events, state_labels);
        } else {
            final Automata remaining = new Automata(theAutomata);
            Automata current;

            result = new Automaton("BBSDDiagnosabilityVerification");

            AutomataSynchronizer theSynchronizer;
            try {
                for (final Automaton aut : theAutomata) {
                    remaining.removeAutomaton(aut.getName());


                    // Calculate local events for G_i
                    //logger.info("&&&&&&&&&&&&&&&");

                /*StringBuilder str = new StringBuilder();
                for (LabeledEvent e : local_events)
                    str.append(e.getLabel());
                logger.info(" && " + str.toString());*/

                    // Calculate (G_N'||G_F')

                    // timer3.start();
                    Automaton currentGv = calculateGv(aut);
                    // timer3.stop();

                    /*
                     * Calculate G_v_prime
                     */
                    local_events.clear();
                    for (final LabeledEvent e : currentGv.iterableEvents())
                        if (e.isUnobservable() || (!remaining.getUnionAlphabet().contains(e) && !result.getAlphabet().contains(e)))
                            local_events.add(e);
                    state_labels.clear();
                    for (final State s : currentGv.iterableStates())
                        state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
                    currentGv = bbsd.calculateAbstraction(currentGv, local_events, state_labels);

                    if (result.nbrOfStates() > 0) {
                        current = new Automata();
                        current.addAutomaton(result);
                        current.addAutomaton(currentGv);
                        // timer4.start();
                        theSynchronizer = new AutomataSynchronizer(current, new SynchronizationOptions(), Config.SYNTHESIS_SUP_AS_PLANT.getValue());
                        theSynchronizer.execute();
                        // timer4.stop();
                        result = theSynchronizer.getAutomaton();
                    } else
                        result = currentGv;

                    // Make a last abstraction of G_v
                    local_events.clear();
                    for (final LabeledEvent e : result.iterableEvents())
                        if (e.isUnobservable() || !remaining.getUnionAlphabet().contains(e))
                            local_events.add(e);
                    for (final State s : result.iterableStates())
                        state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
                    result = bbsd.calculateAbstraction(result, local_events, state_labels);
                }
            } catch (final Exception e) {
                logger.error("Error in BBSDDiagnosabilityVerification.java: " + e);
            }
        }

        // Create a tau event that can be used instead of local events
        final LabeledEvent localEvent = new LabeledEvent("tau");
        boolean addTau = false;
        for (final Arc t : result.iterableArcs()) {
            if (t.getEvent().getLabel().startsWith("tau")) {
                t.setEvent(localEvent);
                addTau = true;
            }
        }
        for (final LabeledEvent e : result.iterableEvents())
            if (e.getLabel().startsWith("tau"))
                result.getAlphabet().removeEvent(e);
        if (addTau) result.getAlphabet().addEvent(localEvent);

        // Print debug data for abstraction
        /*for (State s : result.iterableStates())
            logger.info("Name: " + s.getName() + ", forbidden: " + s.isForbidden() + ", accepting: " + s.isAccepting() + ", initial: " + s.isInitial());
        logger.info("- - - -");
        for (Arc t : result.iterableArcs())
            logger.info(t.getFromState() + "," + t.getLabel() + "," + t.getToState());
        logger.info("- - - -");
        for (LabeledEvent e : result.iterableEvents())
            logger.info(e.getLabel());*/
        // The correct value, the number of states in the synchronization
        // timer1.stop();
        /*logger.info("TOT: \t" + timer1.toString());
        logger.info("ABS: \t" + BBSDAbstraction.timer.toString());
        logger.info("SEP: \t" + timer2.toString());
        logger.info("3: \t" + timer3.toString());
        logger.info("4: \t" + timer4.toString());
        //logger.info("5: \t" + timer5.toString());*/
    }

    private Automaton calculateGv(final Automaton aut) {

        /*
         * Create first abstraction
         */
        // Calculate state labels
        final Hashtable<State,Integer> state_labels = new Hashtable<State, Integer>();
        /*for (State s : aut.iterableStates())
            state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
        Automaton aut_prime = bbsd.calculateAbstraction(aut, local_events, state_labels);*/


        /*
         * Separate the automaton into G_F and G_N
         */
        // timer2.start();
        final Automata temp = separateNormalAndFailure(aut);
        // timer2.stop();
        Automaton autN = temp.getAutomaton("G_N");
        Automaton autF = temp.getAutomaton("G_F");
        autN.setName(aut.getName()+"_N");
        autF.setName(aut.getName()+"_F");



        /*
         * Perform abstraction on G_N and  G_F
         */

        // Calculate local events
        final HashSet<LabeledEvent> local_events = new HashSet<LabeledEvent>();
        for (final LabeledEvent e : aut.iterableEvents())
            if (e.isUnobservable())
                local_events.add(e);

        // Perform abstraction on autN
        autN = bbsd.calculateAbstraction(autN, local_events);

        // Calculate state labels
        state_labels.clear();
        for (final State s : autF.iterableStates())
            state_labels.put(s, ((s.isForbidden()) ? 2 : 1));

        // Perform abstraction on autF
        autF = bbsd.calculateAbstraction(autF, local_events, state_labels);


        /*
         * Create G_V = G_N || G_F
         */
        final Automata automata = new Automata(autN);
        automata.addAutomaton(autF);
        Automaton G_v = new Automaton("temp");


        if (autN.nbrOfStates() > 0) {
            AutomataSynchronizer theSynchronizer;
            try {
                // timer3.stop();
                // timer4.start();
                theSynchronizer = new AutomataSynchronizer(automata, new SynchronizationOptions(), Config.SYNTHESIS_SUP_AS_PLANT.getValue());
                theSynchronizer.execute();
                // timer4.stop();
                // timer3.start();
                G_v = theSynchronizer.getAutomaton();
            } catch (final Exception e) {
                logger.error("Error in BBSDDiagnosabilityVerification.java calculateGv(): " + e);
            }
        }
        else
            G_v = autF;

        return G_v;
    }

    private Automata separateNormalAndFailure(final Automaton original) {

        final Automata result = new Automata();
        final Automaton autN = new Automaton("G_N");
        final Automaton autF = new Automaton("G_F");
        autN.getAlphabet().addEvents(new Alphabet(original.getAlphabet()));
        autF.getAlphabet().addEvents(new Alphabet(original.getAlphabet()));
        result.addAutomaton(autN);
        result.addAutomaton(autF);


        State s1, s2; Arc t2;
        s1 = original.getInitialState();

        final Queue<State> q = new ArrayDeque<State>();
        q.add(s1);

        s2 = new State(original.getInitialState());
        s2.setName(s2.getName() + ((!s2.isForbidden()) ? "_N" : "_F"));
        autF.addState(s2);
        if (!s1.isForbidden())
            autN.addState(new State(original.getInitialState()));

        int autF_status;
        LabeledEvent autN_event, autF_event;
        while (!q.isEmpty()) {
            s1 = q.poll();
            for (final Arc t : s1.getOutgoingArcs()) {

                // Add to G_N iff s1 is an N state && the event is not a failure event && the target is not forbidden
                if (autN.containsState(s1) && !isFailureEvent(t.getEvent()) && !t.getToState().isForbidden()) {
                    autN_event = autN.getAlphabet().getEvent(t.getLabel());

                    s2 = new State(t.getToState());
                    s2.setInitial(false);
                    s2 = autN.addStateChecked(s2);
                    t2 = new Arc(autN.getStateWithName(s1.getName()),s2,autN_event);
                    if (!s2.containsIncomingArc(t2)) autN.addArc(t2);
                }

                // if G_F contains N-state for the source, add a new transition from this state
                autF_status = autF.nbrOfStates();
                autF_event = autF.getAlphabet().getEvent(t.getLabel());
                if (autF.containsStateWithName(s1.getName() + "_N")) {
                    s2 = new State(t.getToState());

                    if (isFailureEvent(t.getEvent()) || t.getToState().isForbidden()){
                        s2.setInitial(false);
                        s2.setName(s2.getName() + "_F");
                        s2.setForbidden(true);
                    }
                    else
                        s2.setName(s2.getName() + "_N");

                    s2 = autF.addStateChecked(s2);
                    t2 = new Arc(autF.getStateWithName(s1.getName() + "_N"),s2,autF_event);
                    if (!s2.containsIncomingArc(t2)) autF.addArc(t2);
                }

                // if G_F contains F-state for the source, add a new transition from this state
                if (autF.containsStateWithName(s1.getName() + "_F")) {
                    s2 = new State(t.getToState());
                    s2.setInitial(false);
                    s2.setName(s2.getName() + "_F");
                    s2.setForbidden(true);
                    s2 = autF.addStateChecked(s2);
                    t2 = new Arc(autF.getStateWithName(s1.getName() + "_F"),s2,autF_event);
                    if (!s2.containsIncomingArc(t2)) autF.addArc(t2);
                }

                // if G_F has changed, add a new state to the queue
                if (autF_status != autF.nbrOfStates()) {
                    q.add(t.getToState());
                }
            }
        }

        return result;
    }

    private boolean isFailureEvent(final LabeledEvent e) {
        return e.getName().toLowerCase().startsWith("f");
    }

    public static Automata getFinalAutomata() {
        return new Automata(result);
    }
    public static boolean getResult() {
        return !result.getComment().equals("not");
    }

}