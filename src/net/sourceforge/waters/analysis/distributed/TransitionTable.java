package net.sourceforge.waters.analysis.distributed;

import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.TransitionSchema;

/**
 * Represents transition information for an automaton. This uses a
 * technique similar to the one uesd by Jinjian Shi in the Waters
 * monolithic controllability checker, where the transition relation
 * for an automaton is represented as 
 * <code>(source, event) -&gt; target</code>.  
 * 
 * This allows fast lookups to check if an event is allowed from a
 * given state. 
 */
class TransitionTable
{
  public TransitionTable(ProductDESSchema des, int autId)
  {
    AutomatonSchema aut = des.getAutomaton(autId);

    //Set some convenient member variables for the array bounds.
    mStates = aut.getStateCount();
    mEvents = des.getEventCount();

    //This array is used to lookup target state based
    //on current state and event. The value -1 is used 
    //to indicate no transition.
    mTransitions = new int[mStates][mEvents];
    for (int i = 0; i < mStates; i++)
      for (int j = 0; j < mEvents; j++)
	{
	  mTransitions[i][j] = -1;
	}

    //Create the event alphabet for the automaton. This
    //is used to quickly check if an event is enabled
    //by an automaton.
    mAlphabet = new HashSet<Integer>();
    for (int i = 0; i < aut.getEventIdCount(); i++)
      {
	mAlphabet.add(aut.getEventId(i));
      }

    //Now build the transition table.
    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);
	mTransitions[t.getSource()][t.getEventId()] = t.getTarget();
      }
  }

  private final int mStates;
  private final int mEvents;
  private final int[][] mTransitions;
  private final Set<Integer> mAlphabet;
}