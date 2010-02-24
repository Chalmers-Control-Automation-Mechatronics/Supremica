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
  public List<int[]> mStateTuples;
  public AutomatonProxy[] mAutomaton;
  public Map<Integer,StateProxy[]> mAutToStateMap;

  public List<StateProxy> mStates = new ArrayList<StateProxy>();

  public StateMap(final int numAutomata)
  {
    mAutomaton = new AutomatonProxy[numAutomata];
    mAutToStateMap = new HashMap<Integer,StateProxy[]>(numAutomata);
    mStateTuples = new ArrayList<int[]>();
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
