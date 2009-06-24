package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.TransitionSchema;

/**
 * Represents transition information for an automaton. This uses a
 * technique similar to the one uesd by Jinjian Shi in the Waters
 * monolithic controllability checker.
 *
 * This is a two dimensional array [events][source state] -&gt; target
 * state
 *
 * This implementation can be used to check if an event is in the
 * alphabet for an automaton; if the event is not in the alphabet then
 * the second dimension of the array can be omitted (array[event] ==
 * null).
 * 
 * This allows fast lookups to check if an event is allowed from a
 * given state. 
 */
class TransitionTable
{
  /**
   * Build a transition table for a given automaton from a model schematic.
   * @param des The model to use
   * @param autId The id of the automaton to use.
   */
  public TransitionTable(ProductDESSchema des, int autId)
  {
    AutomatonSchema aut = des.getAutomaton(autId);
    mAutomatonIndex = autId;
    mModel = des;

    //Set some convenient member variables for the array bounds.
    mStates = aut.getStateCount();
    mEvents = des.getEventCount();

    //Only events in the alphabet will have a state dimension created
    //for them. All state transitions will be set to -1 initially, to 
    //indicate a transition on that event from that state is not possible.
    mTransitions = new int[mEvents][];
    for (int i = 0; i < aut.getEventIdCount(); i++)
      {
	int event = aut.getEventId(i);
	mTransitions[event] = new int[mStates];
	for (int j = 0; j < mStates; j++)
	  {
	    mTransitions[event][j] = -1;
	  }
      }

    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	TransitionSchema t = aut.getTransition(i);
	mTransitions[t.getEventId()][t.getSource()] = t.getTarget();
      }
  }

  /**
   * Check if an event is in the alphabet for this automaton.
   */
  public boolean isInAlphabet(int event)
  {
    return mTransitions[event] != null;
  }


  /**
   * Get the successor state, given the current state for the
   * automaton and an event.
   *
   * If the event is not in the alphabet for the automaton, then the
   * current state will be returned (implicit self-loop). If there is
   * no successor (the event is disabled from the current state), then
   * -1 will be returned.
   * @param state The current state index
   * @param event Event that occurred.
   * @return The target state if the event is enabled, or -1 if disabled.
   */
  public int getSuccessorState(int state, int event)
  { 
    try
      {
	if (!isInAlphabet(event))
	  return state;
	else
	  return mTransitions[event][state];
      }
    catch (IndexOutOfBoundsException e)
      {
	System.err.println(e);
	System.err.format("automaton %d state %d event %d\n", mAutomatonIndex, state, event);
	System.err.println(mModel.getAutomaton(mAutomatonIndex));
	System.err.println(this);
	throw e;
      }
  }

  /**
   * Get the index of the automaton this transition relation
   * corresponds to.
   */
  public int getAutomatonIndex()
  {
    return mAutomatonIndex;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < mEvents; i++)
      {
	sb.append(i);
	sb.append(":");
	sb.append(Arrays.toString(mTransitions[i]));
	sb.append("\n");
      }

    return sb.toString();
  }

  private final ProductDESSchema mModel;
  private final int mAutomatonIndex;
  private final int mStates;
  private final int mEvents;
  private final int[][] mTransitions;
}