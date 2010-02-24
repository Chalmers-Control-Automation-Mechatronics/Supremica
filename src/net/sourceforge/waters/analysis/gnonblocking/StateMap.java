package net.sourceforge.waters.analysis.gnonblocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * Provides a way of storing the original input automata/states and the changes
 * resulting in the output automata.
 *
 * @author Rachel Francis
 */
public class StateMap
{
  private final List<int[]> mStateTuples;
  private final AutomatonProxy[] mAutomaton;
  private final Map<Integer,StateProxy[]> mAutToStateMap;

  public List<StateProxy> mStates = new ArrayList<StateProxy>();

  public StateMap(final int numAutomata)
  {
    mAutomaton = new AutomatonProxy[numAutomata];
    mAutToStateMap = new HashMap<Integer,StateProxy[]>(numAutomata);
    mStateTuples = new ArrayList<int[]>();
  }

  public void addStateTuple(final int[] stateTuple)
  {
    mStateTuples.add(stateTuple);
  }

  public int[] getStateTuple(final int index)
  {
    return mStateTuples.get(index);
  }

  public void addAutomaton(final int index, final AutomatonProxy aut)
  {
    mAutomaton[index] = aut;
  }

  public void addStatesToAutomaton(final int id, final StateProxy[] states)
  {
    mAutToStateMap.put(id, states);
  }

  public StateProxy getState(final int[] stateTuple,
                             final AutomatonProxy automaton)
  {
    int autID = -1;
    for (final AutomatonProxy aut : mAutomaton) {
      autID++;
      if (aut == automaton) {
        break;
      }
    }
    final int stateID = stateTuple[autID];
    final StateProxy originalState = mAutToStateMap.get(autID)[stateID];
    return originalState;
  }
}
