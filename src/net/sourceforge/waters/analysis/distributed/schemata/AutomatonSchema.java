package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Arrays;
import java.util.Formatter;
import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  AutomatonSchema(String name,
		  int[] eventIds,
		  StateSchema[] states,
		  int kind,
		  TransitionSchema[] transitions,
		  int id)
  {
    mName = name;
    mEventIds = eventIds;
    mStates = states;
    mKind = kind;
    mTransitions = transitions;
    mAutomatonId = id;
  }

  public String getName()
  {
    return mName;
  }

  public int getAutomatonId()
  {
    return mAutomatonId;
  }
  
  public int getEventId(int index)
  {
    return mEventIds[index];
  }

  public int getEventIdCount()
  {
    return mEventIds.length;
  }

  public StateSchema getState(int index)
  {
    return mStates[index];
  }

  public int getStateCount()
  {
    return mStates.length;
  }

  public int getKind()
  {
    return mKind;
  }

  public TransitionSchema getTransition(int index)
  {
    return mTransitions[index];
  }

  public int getTransitionCount()
  {
    return mTransitions.length;
  }

  public String toString()
  {
    Formatter fmt = new Formatter();
    fmt.format("Name: %s\n", mName);
    fmt.format("Id: %d\n", mAutomatonId);
    fmt.format("Events: %s\n", Arrays.toString(mEventIds));
    fmt.format("Transitions: %s\n", Arrays.deepToString(mTransitions));
    fmt.format("States: %s\n", Arrays.deepToString(mStates));
    fmt.format("Kind: %d\n", mKind);

    return fmt.toString();
  }

  /**
   * A simple predicate to check if an event is in the alphabet for
   * this automaton.
   * @param event to check for
   * @return true if event is in the alphabet for this automaton.
   */
  public boolean hasEvent(int event)
  {
    for (int eventid : mEventIds)
      {
	if (event == eventid)
	  return true;
      }

    return false;
  }

  private final String mName;
  private final int[] mEventIds;
  private final StateSchema[] mStates;
  private final int mKind;
  private final TransitionSchema[] mTransitions;
  private final int mAutomatonId;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}