package org.supremica.automata.algorithms.bbsd;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


public class BBSDAbstraction
{
    // Create a dummy state that is used for local loops
    final State dummy = new State("SelfLoopDummyState");

    public BBSDAbstraction() {}

    public Automaton calculateAbstraction(final Automaton aut, final HashSet<LabeledEvent> local_events) {
        final Map<State,Integer> state_labels = new Hashtable<>();
        for (final State s : aut.iterableStates()) state_labels.put(s,1);
        return calculateAbstraction(aut, local_events, (Hashtable<State,Integer>)state_labels);
    }

    public Automaton calculateAbstraction(final Automaton aut, final HashSet<LabeledEvent> local_events, final Hashtable<State,Integer> state_labels) {

        //timer.start();
        //logger.info(" - - -");
        //logger.info("States: " + aut.nbrOfStates() + ", Events: " + aut.nbrOfEvents() + ", Transitions: " + aut.nbrOfTransitions() + ",\t(name: " + aut.getName() + ")");
        /*StringBuilder str = new StringBuilder();
        for (LabeledEvent e : local_events)
            str.append(e.getLabel());
        logger.info(" && " + str.toString());*/

        int blocks;

        Map<State, Integer> pi = new Hashtable<>(state_labels); // pi_template for new test
        final Map<State, Integer> pi_zeros = new Hashtable<>();
        Map<State, Integer> pi_temp;
        for (final State s : aut.iterableStates()) pi_zeros.put(s, 0);

        // Debug info, output the initial partitioning
        /*for (State s : aut.iterableStates()) {
            logger.info(s.getName() + ", " + pi.get(s));
        }
        logger.info("- - - -");*/

        final Map<State, HashSet<BlockTransition>> blockTransitions = new Hashtable<>();

        // Create a tau event that can be used instead of local events
        final LabeledEvent localEvent = new LabeledEvent("tau_" + aut.getName());
        if (local_events.size() > 0) {
            aut.getAlphabet().addEvent(localEvent);
            for (final Arc t : aut.iterableArcs())
                if (local_events.contains(t.getEvent()))
                    t.setEvent(localEvent);
            for (final LabeledEvent e : local_events)
                aut.getAlphabet().removeEvent(e);
        }

        final Map<State, HashSet<State>> coreachable = new Hashtable<>();

        // Loop until pi = pi'
        while (true) {
            // logger.info("1: " + timer.toString());

            // Calculate local transitions
            final Set<Arc> LB_trans = new HashSet<>();
            for (final Arc t : aut.iterableArcs())
                if (t.getEvent().equals(localEvent) && !t.getToState().equals(dummy) && pi.get(t.getFromState()).equals(pi.get(t.getToState())))
                    LB_trans.add(t);


            // logger.info("2: " + timer.toString());

            for (final State s : aut.iterableStates()) {
                blockTransitions.put(s, new HashSet<>());
                coreachable.put(s, new HashSet<>());
            }
            boolean coreachability_finished = false;
            while(!coreachability_finished) {
                coreachability_finished = true;
                for (final Arc t : LB_trans) {
                    final int pi1 = pi.get(t.getToState());
                    for (final State s : aut.iterableStates()) {
                        if (pi1 == pi.get(s)) {
                            final Set<State> ss = coreachable.get(s);
                            if (t.getToState() == s || ss.contains(t.getToState())) {
                                if (t.getFromState() == s) {
                                    aut.addArc(new Arc(s, dummy, localEvent));
                                }
                                if (!ss.contains(t.getFromState())) {
                                    coreachable.get(s).add(t.getFromState());
                                    coreachability_finished = false;
                                }
                            }
                        }
                    }
                }

                /*for (final State s1 : aut.iterableStates()) {
                    StringBuilder str = new StringBuilder();
                    str.append(s1.getName()+ ": ");
                    for (final State s2 : coreachable.get(s1)) {
                        str.append(s2.getName() + ", ");
                    }
                    logger.info(" && " + str.toString());
                }*/

            }

            // logger.info("3: " + timer.toString());


            for (final Arc t : aut.iterableArcs()) {
                if (!LB_trans.contains(t)) {
                    final State s1 = t.getFromState();
                    final BlockTransition bt = new BlockTransition(pi.get(s1), t.getEvent(), ((t.getToState().equals(dummy)) ? 0 : pi.get(t.getToState())));
                    blockTransitions.get(s1).add(bt);
                    for (final State s2 : coreachable.get(s1))
                        blockTransitions.get(s2).add(bt);
                }
            }

            // logger.info("4: " + timer.toString());

            // Update pi to create new partitioning
            pi_temp = new Hashtable<>(pi_zeros);
            int k = 1;
            for (final State s : aut.iterableStates()) {
                if (pi_temp.get(s) == 0) {
                    pi_temp.put(s, k);
                    for (final State s2 : aut.iterableStates())
                        if (blockTransitions.get(s).equals(blockTransitions.get(s2)))
                            pi_temp.put(s2,k);
                    ++k;
                }
            }

            // logger.info("5: " + timer.toString());

            // Debug info - Output block transitions for each state
            /*StringBuilder sb;
            for (State s : aut.iterableStates()) {
                sb = new StringBuilder("&& " + s.getName() + "(" + pi.get(s) + "," + pi_temp.get(s) + ") : ");
                for (BlockTransition t : blockTransitions.get(s))
                    sb.append("(" + t.getSourceBlock() + "," + t.getLabel() + "," + t.getTargetBlock() + ") ");
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
        final Automaton result = new Automaton("("+aut.getName()+")_prime");
        result.setType(AutomatonType.PLANT);
        result.getAlphabet().addEvents(aut.getAlphabet());

        // Add one state for each block
        // logger.info("6: " + timer.toString());

        final boolean[] initial = new boolean[blocks];
        final boolean[] forbidden = new boolean[blocks];
        final boolean[] notAccepting = new boolean[blocks];

        for (final State s2 : aut.iterableStates()) {
            if (s2.isInitial())
                initial[pi.get(s2)-1] = true;
            if (s2.isForbidden())
                forbidden[pi.get(s2)-1] = true;
            else if (!s2.isAccepting())
                notAccepting[pi.get(s2)-1] = true;
        }

        // logger.info("7: " + timer.toString());

        for (int i = 1; i <= blocks; ++i) {
            final State newState = new State("B" + i);
            newState.setInitial(initial[i-1]);
            newState.setForbidden(forbidden[i-1]);
            newState.setAccepting(!notAccepting[i-1] && !forbidden[i-1]);
            result.addState(newState);
        }


        // Add all transitions that go between blocks
        int pi1, pi2;
        for (final Arc t : aut.iterableArcs()) {
            Arc t2 = null;
            pi1 = pi.get(t.getFromState());
            final State s1 = result.getStateWithName("B" + pi1);
            if (t.getToState().equals(dummy)) {
                t2 = new Arc(s1, s1, t.getEvent());
                if (s1.isForbidden())
                    result.setComment("not");
            }
            else {
                pi2 = pi.get(t.getToState());
                final State s2 = result.getStateWithName("B" + pi2);
                if (pi1 != pi2 || !t.getEvent().equals(localEvent))
                    t2 = new Arc(s1, s2, t.getEvent());
            }
            if (t2 != null && !s1.containsOutgoingArc(t2)) result.addArc(t2);
        }
        // logger.info("8: " + timer.toString());

        // timer.stop();

        //logger.info("States: " + result.nbrOfStates() + ", Events: " + result.nbrOfEvents() + ", Transitions: " + result.nbrOfTransitions() + ",\t(name: " + result.getName() + ")");
        //logger.info(" - - -");
        //timer.stop();

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

        @Override
        public String toString()
        {
            return "{" + from + "," + event.toString() + "," + to + "}";
        }

        @Override
        public boolean equals(final Object o) {

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
            final BlockTransition c = (BlockTransition) o;

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