package net.sourceforge.waters.analysis.abstraction;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

import java.util.*;

public class BBSDDiagnosabilityVerification {

    private static Logger logger = LoggerFactory.createLogger(BBSDDiagnosabilityVerification.class);

    private BBSDAbstraction bbsd = new BBSDAbstraction();

    public BBSDDiagnosabilityVerification(Automata theAutomata){

        Automaton result = new Automaton("BBSDDiagnosabilityVerification");
        final HashSet<LabeledEvent> local_events = new HashSet<LabeledEvent>();
        final Hashtable<State,Integer> state_labels = new Hashtable<State, Integer>();


        if (theAutomata.size() == 1) {
            javax.swing.JOptionPane.showMessageDialog(null, "Since you only selected ONE automaton, you have entered a debugging mode. If this was not intended, please select 2+ automatons and try again.");

            Automaton aut = theAutomata.getFirstAutomaton();
            local_events.clear();
            for (LabeledEvent e : aut.iterableEvents())
                if (e.getLabel().equals("tau"))
                    local_events.add(e);
            for (State s : aut.iterableStates())
                state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
            result = bbsd.calculateAbstraction(aut, local_events, state_labels);
        }
        else {

            final Automata remaining = new Automata(theAutomata);
            Automata current;

            AutomataSynchronizer theSynchronizer;
            try
            {
                for (Automaton aut : theAutomata) {
                    remaining.removeAutomaton(aut.getName());


                    // Calculate local events for G_i
                    //logger.info("&&&&&&&&&&&&&&&");

                /*StringBuilder str = new StringBuilder();
                for (LabeledEvent e : local_events)
                    str.append(e.getLabel());
                logger.info(" && " + str.toString());*/

                    // Calculate (G_N'||G_F')
                    Automaton currentGv = calculateGv(aut);

                    /*
                     * Calculate G_v_prime
                     */

                    local_events.clear();
                    for (LabeledEvent e : currentGv.iterableEvents())
                        if (!remaining.getUnionAlphabet().contains(e) && !result.getAlphabet().contains(e))
                            local_events.add(e);
                    state_labels.clear();
                    for (State s : currentGv.iterableStates())
                        state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
                    currentGv = bbsd.calculateAbstraction(currentGv, local_events, state_labels);

                    if (result.nbrOfStates() > 0) {
                        current = new Automata();
                        current.addAutomaton(result);
                        current.addAutomaton(currentGv);
                        theSynchronizer = new AutomataSynchronizer(current, new SynchronizationOptions(), Config.SYNTHESIS_SUP_AS_PLANT.get());
                        theSynchronizer.execute();
                        result = theSynchronizer.getAutomaton();
                    }
                    else
                        result = currentGv;

                    // Make a last abstraction of G_v
                    local_events.clear();
                    for (LabeledEvent e : result.iterableEvents())
                        if (!remaining.getUnionAlphabet().contains(e))
                            local_events.add(e);
                    for (State s : result.iterableStates())
                        state_labels.put(s, ((s.isForbidden()) ? 2 : 1));
                    result = bbsd.calculateAbstraction(result, local_events, state_labels);

                }
            }
            catch (Exception e)
            {
                logger.error("Error in BBSDDiagnosabilityVerification.java: " + e);
            }
        }

        // Create a tau event that can be used instead of local events
        LabeledEvent localEvent = new LabeledEvent("tau");
        boolean addTau = false;
        for (Arc t : result.iterableArcs())
            if (t.getEvent().getLabel().startsWith("tau"))
                t.setEvent(localEvent);
        for (LabeledEvent e : result.iterableEvents())
            if (e.getLabel().startsWith("tau")) {
                result.getAlphabet().removeEvent(e);
                addTau = true;
            }
        if (addTau) result.getAlphabet().addEvent(localEvent);

        // Print debug data for abstraction
        for (State s : result.iterableStates())
            logger.info("Name: " + s.getName() + ", forbidden: " + s.isForbidden() + ", accepting: " + s.isAccepting() + ", initial: " + s.isInitial());
        logger.info("- - - -");
        for (Arc t : result.iterableArcs())
            logger.info(t.getFromState() + "," + t.getLabel() + "," + t.getToState());
        logger.info("- - - -");
        for (LabeledEvent e : result.iterableEvents())
            logger.info(e.getLabel());
        // The correct value, the number of states in the synchronization

    }

    private Automaton calculateGv(Automaton aut) {

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
        Automata temp = separateNormalAndFailure(aut);
        Automaton autN = temp.getAutomaton("G_N");
        Automaton autF = temp.getAutomaton("G_F");
        autN.setName(aut.getName()+"_N");
        autF.setName(aut.getName()+"_F");



        /*
         * Perform abstraction on G_N and  G_F
         */

        // Calculate local events
        final HashSet<LabeledEvent> local_events = new HashSet<LabeledEvent>();
        for (LabeledEvent e : aut.iterableEvents())
            if (e.isUnobservable())
                local_events.add(e);

        // Perform abstraction on autN
        autN = bbsd.calculateAbstraction(autN, local_events);

        // Calculate state labels
        state_labels.clear();
        for (State s : autF.iterableStates())
            state_labels.put(s, ((s.isForbidden()) ? 2 : 1));

        // Perform abstraction
        autF = bbsd.calculateAbstraction(autF, local_events, state_labels);


        /*
         * Create G_V = G_N || G_F
         */
        Automata automata = new Automata(autN);
        automata.addAutomaton(autF);
        Automaton G_v = new Automaton("temp");


        if (autN.nbrOfStates() > 0) {
            AutomataSynchronizer theSynchronizer;
            try {
                theSynchronizer = new AutomataSynchronizer(automata, new SynchronizationOptions(), Config.SYNTHESIS_SUP_AS_PLANT.get());
                theSynchronizer.execute();
                G_v = theSynchronizer.getAutomaton();
            } catch (Exception e) {
                logger.error("Error in BBSDDiagnosabilityVerification.java calculateGv(): " + e);
            }
        }
        else
            G_v = autF;

        return G_v;
    }

    private Automata separateNormalAndFailure(final Automaton original) {

        Automata result = new Automata();
        Automaton autN = new Automaton("G_N");
        Automaton autF = new Automaton("G_F");
        autN.getAlphabet().addEvents(new Alphabet(original.getAlphabet()));
        autF.getAlphabet().addEvents(new Alphabet(original.getAlphabet()));
        result.addAutomaton(autN);
        result.addAutomaton(autF);


        State s1, s2; Arc t2;
        s1 = original.getInitialState();

        Queue<State> q = new ArrayDeque<State>();
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
            for (Arc t : s1.getOutgoingArcs()) {

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

    private boolean isFailureEvent(LabeledEvent e) {
        return e.getName().toLowerCase().startsWith("f");
    }

}