package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.xsd.des.*;
import net.sourceforge.waters.xsd.module.LabelBlock;
import org.supremica.automata.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;

import java.util.*;

public class BBSDAbstraction {

    private static Logger logger = LoggerFactory.createLogger(BBSDAbstraction.class);

    public BBSDAbstraction() {}

    public Automaton calculateAbstraction(final Automaton aut, final HashSet<LabeledEvent> local_events) {
        Hashtable<State,Integer> state_labels = new Hashtable<State,Integer>();
        for (State s : aut.iterableStates()) state_labels.put(s,1);
        return calculateAbstraction(aut, local_events, state_labels);
    }

    public Automaton calculateAbstraction(final Automaton aut, final HashSet<LabeledEvent> local_events, final Hashtable<State,Integer> state_labels) {


        /*StringBuilder str = new StringBuilder();
        for (LabeledEvent e : local_events)
            str.append(e.getLabel());
        logger.info(" && " + str.toString());*/



        int blocks;

        Map<State, Integer> pi = new Hashtable<State, Integer>(state_labels),
                pi_zeros = new Hashtable<State, Integer>(),
                pi_temp; // pi_template for new test
        for (State s : aut.iterableStates()) pi_zeros.put(s, 0);

        // Debug info, output the initial partitioning
        /*for (State s : aut.iterableStates()) {
            logger.info(s.getName() + ", " + pi.get(s));
        }
        logger.info("- - - -");*/

        Set<State> reach_states = new HashSet<State>();
        Map<State, HashSet<BlockTransition>> blockTransitions = new Hashtable<State, HashSet<BlockTransition>>();



        /*
         *
         * INSERT TRANSITIONS TO DUMMY STATE FROM EACH STATE IN A LOCAL LOOP
         *
         */

        // Create a tau event that can be used instead of local events
        LabeledEvent localEvent = new LabeledEvent("tau_" + aut.getName());
        if (local_events.size() > 0) {
            aut.getAlphabet().addEvent(localEvent);
            for (Arc t : aut.iterableArcs())
                if (local_events.contains(t.getEvent()))
                    t.setEvent(localEvent);
            for (LabeledEvent e : local_events)
                aut.getAlphabet().removeEvent(e);
        }

        // Loop until pi = pi'
        while (true) {

            // Calculate local transitions
            Set<Arc> LB_trans = new HashSet<Arc>();
            for (Arc t : aut.iterableArcs())
                if (t.getEvent().equals(localEvent) && pi.get(t.getFromState()).equals(pi.get(t.getToState())))
                    LB_trans.add(t);

            for (State s : aut.iterableStates()) {
                reach_states.clear();
                blockTransitions.put(s, new HashSet<BlockTransition>());

                // Calculate reachability for each state
                Queue<State> q = new ArrayDeque<State>();
                State s2;
                q.add(s);
                while (!q.isEmpty()) {
                    s2 = q.poll();
                    for (Arc t : s2.getOutgoingArcs())
                        if (LB_trans.contains(t) && !reach_states.contains(t.getToState())) {
                            reach_states.add(t.getToState());
                            q.add(t.getToState());
                        }
                }

                // Calculate block transitions of each state
                for (Arc t : aut.iterableArcs())
                    if (!LB_trans.contains(t) && (t.getFromState() == s || reach_states.contains(t.getFromState())))
                        blockTransitions.get(s).add(new BlockTransition(pi.get(t.getFromState()), t.getEvent(), pi.get(t.getToState())));
            }

            // Update pi to create new partitioning
            pi_temp = new Hashtable<State, Integer>(pi_zeros);
            int k = 1;
            for (State s : aut.iterableStates()) {
                if (pi_temp.get(s) == 0) {
                    pi_temp.put(s, k);
                    for (State s2 : aut.iterableStates())
                        if (blockTransitions.get(s).equals(blockTransitions.get(s2)))
                            pi_temp.put(s2,k);
                    ++k;
                }
            }

            // Debug info - Output block transitions for each state
            /*StringBuilder sb;
            for (State s : aut.iterableStates()) {
                sb = new StringBuilder("&& " + s.getName() + "(" + pi.get(s) + "," + pi_temp.get(s) + ") : ");
                for (BlockTransition t : blockTransitions.get(s))
                    sb.append("(" + t.getLabel() + "," + t.getTargetBlock() + ") ");
                logger.info(sb.toString());
            }*/

            // Count the number of blocks in the latest partitioning
            blocks = k-1;

            // It's finished when pi' = pi (the partition did not change)
            if (pi.equals(pi_temp))
                break;
            else
                pi = pi_temp;

            // Debug info, output the new partition
            /*for (State s : aut.iterableStates()) {
                logger.info(s.getName() + ", " + pi.get(s));
            }
            logger.info("- - - -");*/
        }


        // Create the base for the result
        Automaton result = new Automaton("("+aut.getName()+")_prime");
        result.getAlphabet().addEvents(aut.getAlphabet());

        // Add one state for each block
        for (int i = 1; i <= blocks; ++i) {
            State newState = new State("B" + i);
            boolean initial = false;
            boolean forbidden = false;
            boolean accepting = true;
            for (State s2 : aut.iterableStates()) {
                if (pi.get(s2) == i) {
                    if (s2.isInitial())
                        initial = true;
                    if (s2.isForbidden())
                        forbidden = true;
                    else if (!s2.isAccepting())
                        accepting = false;
                }
            }
            newState.setInitial(initial);
            newState.setForbidden(forbidden);
            newState.setAccepting(accepting && !forbidden);
            result.addState(newState);
        }


        // Add all transitions that go between blocks
        int pi1, pi2;
        for (Arc t : aut.iterableArcs()) {
            pi1 = pi.get(t.getFromState());
            pi2 = pi.get(t.getToState());
            if (pi1 != pi2 || !t.getEvent().equals(localEvent)) {
                Arc t2 = new Arc(result.getStateWithName("B" + pi1), result.getStateWithName("B" + pi2), t.getEvent());
                if (!result.getStateWithName("B" + pi1).containsOutgoingArc(t2)) result.addArc(t2);
            }
        }

        /*
         *
         * REPLACE TRANSITIONS TO DUMMY STATE WITH SELFLOOPS
         *
         */

        return result;
    }

    /*
     * CONTAINER CLASS FOR TRANSITIONS BETWEEN BLOCKS
     */
    class BlockTransition {

        //#################################################################################
        //# Constructor
        public BlockTransition(final Integer from, final LabeledEvent event, final Integer to)
        {
            this.from = from;
            this.event = event;
            this.to = to;
        }


        // #################################################################################
        // # Simple Access
        public Integer getSourceBlock()
        {
            return from;
        }

        public LabeledEvent getEvent()
        {
            return event;
        }

        public String getLabel()
        {
            return event.getLabel();
        }

        public Integer getTargetBlock()
        {
            return to;
        }

        public String toString()
        {
            return "{" + from + "," + event.toString() + "," + to + "}";
        }

        @Override
        public boolean equals(Object o) {

            // If the object is compared with itself then return true
            if (o == this) {
                return true;
            }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
            if (!(o instanceof BlockTransition)) {
                return false;
            }

            // typecast o to Complex so that we can compare data members
            BlockTransition c = (BlockTransition) o;

            // Compare the data members and return accordingly
            return Integer.compare(from, c.getSourceBlock()) == 0 && event.equals(c.getEvent()) && Integer.compare(to, c.getTargetBlock()) == 0;
        }

        @Override
        public int hashCode() {
            return from+to+event.getLabel().hashCode();
        }


        //#################################################################################
        //# Data Members
        private final Integer from;
        private final LabeledEvent event;
        private final Integer to;
    }

}