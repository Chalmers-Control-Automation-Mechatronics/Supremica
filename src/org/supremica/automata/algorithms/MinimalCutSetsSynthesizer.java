package org.supremica.automata.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringJoiner;

import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.properties.Config;

/**
 * Zenuity Hackfest 2019
 *
 * Thread that does the calculation of minimal cut sets
 * in automaton-scale in the monolithic way.
 *
 * @author zhefei
 * @since 12/05/2019
 */

public class MinimalCutSetsSynthesizer
  implements Abortable
{

  private static Logger logger =
    LogManager.getLogger(MinimalCutSetsSynthesizer.class);

  private final Automata theAutomata;
  private boolean abortRequested = false;
  private Automaton syncAut;

  public MinimalCutSetsSynthesizer(final Automata theAutomata)
  {
    this.theAutomata = theAutomata;
  }

  public void execute() {
    // Synchronize automata
    final SynchronizationOptions synchronizationOptions =
      new SynchronizationOptions();
    final AutomataSynchronizer synchronizer =
      new AutomataSynchronizer(theAutomata,
                               synchronizationOptions,
                               Config.SYNTHESIS_SUP_AS_PLANT.get());
    synchronizer.execute();
    this.syncAut = synchronizer.getAutomaton();
    logger.info("Calculating the minimal cut sets...");
    computeMinimalCutSets(syncAut);
  }

  private void computeMinimalCutSets(final Automaton aut) {
    logger.debug("Algorithm 3...");
    final Automaton syncAut = aut;
    final State initialState = syncAut.getInitialState();
    final Queue<State> queue = new LinkedList<State>();
    queue.add(initialState);
    // set of events that belong to any minimal cut set
    final HashSet<LabeledEvent> redundantEvents = new HashSet<>();
    final HashMap<State, HashSet<LabeledEvent>> state2eventMap =
      new HashMap<>();
    final HashSet<HashSet<LabeledEvent>> minCutSets =
      new HashSet<HashSet<LabeledEvent>>();
    State currState = null;

    final HashSet<LabeledEvent> unContrlEvents = new HashSet<LabeledEvent>();
    for (final LabeledEvent uc : aut.getAlphabet().getUncontrollableAlphabet()) {
      unContrlEvents.add(uc);
    }

    final HashSet<State> explored = new HashSet<>();

    while(!queue.isEmpty()) {
      currState = queue.remove();

      if (currState == initialState) {
        for (final Arc arc : currState.getOutgoingArcs()) {
          final State target = arc.getTarget();
          final LabeledEvent event = arc.getEvent();
          // check if target is marked
          if (target.isAccepting()) {
            // event is a minimal cut set
            final HashSet<LabeledEvent> singleton = new HashSet<LabeledEvent>();
            singleton.add(event);
            minCutSets.add(singleton);
            redundantEvents.add(event);
            logger.debug(event.getName() + " is single MCS");
          } else {
            queue.add(target);
            final HashSet<LabeledEvent> eventSet =
              new HashSet<LabeledEvent>();
            eventSet.add(event);
            state2eventMap.put(target, eventSet);
            logger.debug(event.getName() + " does not lead to marked state");
          }
        }
        logger.debug("");
        explored.add(currState);
      } else {
        // currState is not marked and is not visited and it does not pass redundant events
        final HashSet<LabeledEvent> currEvents =
          new HashSet<>(state2eventMap.get(currState));
        final int currEventsSize = currEvents.size();
        currEvents.removeAll(redundantEvents);
        if (currEventsSize != currEvents.size())
          continue;
        for (final Arc arc : currState.getOutgoingArcs()) {
          final State target = arc.getTarget();
          final LabeledEvent event = arc.getEvent();
          if(redundantEvents.contains(event)) {
            logger.debug(event.getName() + " is redundant");
            continue;
          }
          if(explored.contains(target)) {
            logger.debug(currState.getName() + " is visited");
            continue;
          }
          if(target.isAccepting()) {
            logger.debug(event.getName() + " is MCS event");
            // combine event with the events (all controllable) of currState
            // since event is always uncontrollable, we takes events
            minCutSets.add(state2eventMap.get(currState));
            logger.debug("The minimal cut sets:");
            int index = 1;
            for (final HashSet<LabeledEvent> mcs : minCutSets) {
              logger.debug("Minimal cut set " + index + ":");
              for (final LabeledEvent e : mcs) {
                logger.debug(e.getName());
              }
              index++;
            }
            // add events into the redundant event set
            redundantEvents.addAll(state2eventMap.get(currState));
          } else {
            logger.debug(event.getName() + " does not lead to marked state");
            logger.debug(currState.getName() + " is current state");
            // currState is unvisited, and is not marked
            // put target in the queue
            queue.add(target);
            // combine events of currState with event if it is controllable
            // put the combined events in the map with target as the key
            final HashSet<LabeledEvent> targetEvents =
              new HashSet<>(state2eventMap.get(currState));
            if(event.isControllable())
              targetEvents.add(event);
            state2eventMap.put(target, targetEvents);
          }

        }
        logger.debug("");
        explored.add(currState);
      }
    }

    // print minimal cut sets
    int index = 1;
    final String PREFIX = "MinimalCutSet_";
    for (final HashSet<LabeledEvent> mSet : minCutSets) {
      logger.info(PREFIX + index + ":");
      for (final LabeledEvent e : mSet) {
        logger.info(e.getName());
      }
      index++;
    }
  }

  @Override
  public void requestAbort()
  {
    abortRequested = true;
    logger.debug("MinimalCutSetsSynthesizer requested to stop.");
  }

  @Override
  public boolean isAborting()
  {
    return abortRequested;
  }

  @Override
  public void resetAbort()
  {
    abortRequested = false;
  }

  void debugQueue(final Queue<State> queue) {
    final StringJoiner sb = new StringJoiner(",");
    for (final State s : queue) {
      sb.add(s.getName());
    }
    logger.debug("Queue is: " + sb.toString());
  }
}
