package net.sourceforge.waters.analysis.compositional;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class EnabledEventsCache
{

  AutomatonProxy mAutToAbstract;
  List<AutomatonProxy> mAutomataToTest = new ArrayList<AutomatonProxy>();
  ListBufferTransitionRelation[] mTransitionRelations;
  TObjectByteHashMap<TIntHashSet> mEventSetCache;
  EventEncoding mEncoding;
  ProductDESProxyFactory mFactory;
int OMEGA;
  HashMap<EventProxy,Integer> eventsCounter = new HashMap<>();

  public EnabledEventsCache(final AutomatonProxy autToAbstract,
                            final Collection<AutomatonProxy> allAutomata,
                            final ProductDESProxyFactory factory,
                            final KindTranslator translator,
                            final int omega)
  {
    OMEGA = omega;
    mFactory = factory;
    final EventEncoding encoding =
      new EventEncoding(autToAbstract.getEvents(), translator);
    mAutToAbstract = autToAbstract;
    //Collect all automata that share events with the autToAbstract.
    for (final AutomatonProxy aut : allAutomata) {
      for (final EventProxy event : aut.getEvents()) {
        if(translator.getEventKind(event) != EventKind.PROPOSITION && encoding.getEventCode(event) >= 0)
        {
          mAutomataToTest.add(aut);
          break;
        }
      }
    }
    mTransitionRelations =
      new ListBufferTransitionRelation[mAutomataToTest.size()];

    //Find any tau events in allAutomata and add them to the encoding.
    for (final AutomatonProxy aut : allAutomata) {
      for (final EventProxy tauEvents : aut.getEvents()) {
        if (!eventsCounter.containsKey(tauEvents)) {
          //If we have not seen the event in previous automata, add it to the counter
          eventsCounter.put(tauEvents, 1);
        } else {
          //Increase it's count by one, and it is no longer tau.
          eventsCounter.put(tauEvents, eventsCounter.get(tauEvents) + 1);
        }
      }
    }
    for (final EventProxy tauEvents : eventsCounter.keySet()) {
      if (eventsCounter.get(tauEvents) == 1) {
        encoding.addSilentEvent(tauEvents);
      }
    }
    //The encoding has events from autToEncode, plus all tau events from any of the automata
    mEncoding = encoding;
  }

  /**
   *
   * Is at least one of the events in the set eventSet enabled on all states
   * in every other automata in the system?
   *
   * @param eventSet
   *          is a set of encoded events
   * @return true if the set is always enabled.
   * @throws OverflowException
   */
  public boolean IsAlwaysEnabled(final TIntHashSet eventSet) throws OverflowException
  {
    //Check the eventSet cache to see if we have already tested this set
    final int cacheValue = mEventSetCache.get(eventSet);
    if (cacheValue == 1)
      return true;
    else if (cacheValue == 2)
      return false;
    else if (cacheValue == 0) {

      outer: for (final AutomatonProxy aut : mAutomataToTest) {
        //If ALL events from eventSet are in the alphabet of aut
        final TIntIterator iter = eventSet.iterator();
        while(iter.hasNext())
        {
          final int e = iter.next();
          final EventProxy event = mEncoding.getProperEvent(e);
          if(!aut.getEvents().contains(event))
          {
            continue outer;
          }
        }

        //If all the events from eventSet are in this automaton, we then get
        //the transition relation to test if some of eventSet is enabled on every state.
        final int relIndex = mAutomataToTest.indexOf(aut);
        //The transition relations are cached for a single question.
        if (mTransitionRelations[relIndex] == null) {
          //Create a smaller version of the automaton with only relevant events
          final Set<StateProxy> fantasy = new THashSet<>();
          final ArrayList<TransitionProxy> newTrans = new ArrayList<>();
          for (final TransitionProxy tr : aut.getTransitions()) {

            //If the event is not tau and not in the alphabet of the autToAbstract
            if(mEncoding.getEventCode(tr.getEvent()) < 0)
            {
              //Then we will mark the state as a 'fantasy' state
              fantasy.add(tr.getSource());
            } else //We will add this transition to the smaller automaton.
            {
              newTrans.add(tr);
            }
          }
          //Create smaller automaton using eventset of autToAbstract
          //Same state set
          //transitions newTrans
          final Collection<EventProxy> newEvents = mEncoding.getEvents();
          final AutomatonProxy newAut = mFactory.createAutomatonProxy(aut.getName(), aut.getKind(), newEvents, aut.getStates(), newTrans);

          //Create a State encoding.
          final StateEncoding stateEncoding = new StateEncoding();

          //Create transition relation and store it.
          final ListBufferTransitionRelation newRel = new ListBufferTransitionRelation(newAut, mEncoding, stateEncoding, ListBufferTransitionRelation.CONFIG_SUCCESSORS);


          //Mark each fantasy state in the rel.
          for(final StateProxy s : fantasy)
          {
            newRel.setMarked(stateEncoding.getStateCode(s), OMEGA, true);
          }
        }
        //Test if every state has some of eventSet enabled.
        final TauClosure closure =
          mTransitionRelations[relIndex].createSuccessorsTauClosure(0);
        final TransitionIterator iterator = closure.createPreEventClosureIterator();

        states: for (int state = 0; state < mTransitionRelations[relIndex]
          .getNumberOfStates(); state++) {
          iterator.resetState(state);
          while (iterator.advance()) {
            if (eventSet.contains(iterator.getCurrentEvent())) {
              continue states;
            }
          }
          //If this state is a dump state then ignore it
          //If it has no outgoing transition, and is not marked OMEGA.
         if( mTransitionRelations[relIndex].isDeadlockState(state, OMEGA))
          {
            continue states;
          }
          //If this state has no transitions from the eventSet
          mEventSetCache.put(eventSet, (byte) 2);
          return false;
        }
      }
    //Otherwise every state in every automaton has some transitions from the eventSet
    mEventSetCache.put(eventSet, (byte) 1);
    return true;

    } else //The cache has somehow broken
    {
      return false;
    }
  }


  public EventEncoding getEventEncoding()
  {
    return mEncoding;
  }
}
