package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Arrays;
import java.util.Formatter;
import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  AutomatonSchema(final String name,
		  final int[] eventIds,
		  final StateSchema[] states,
		  final int kind,
		  final TransitionSchema[] transitions,
		  final int id)
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

  public int getEventId(final int index)
  {
    return mEventIds[index];
  }

  public int getEventIdCount()
  {
    return mEventIds.length;
  }

  public StateSchema getState(final int index)
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

  public TransitionSchema getTransition(final int index)
  {
    return mTransitions[index];
  }

  public int getTransitionCount()
  {
    return mTransitions.length;
  }

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      fmt.format("Name: %s\n", mName);
      fmt.format("Id: %d\n", mAutomatonId);
      fmt.format("Events: %s\n", Arrays.toString(mEventIds));
      fmt.format("Transitions: %s\n", Arrays.deepToString(mTransitions));
      fmt.format("States: %s\n", Arrays.deepToString(mStates));
      fmt.format("Kind: %d\n", mKind);
      return fmt.toString();
    } finally {
      fmt.close();
    }
  }

  /**
   * A simple predicate to check if an event is in the alphabet for
   * this automaton.
   * @param event to check for
   * @return true if event is in the alphabet for this automaton.
   */
  public boolean hasEvent(final int event)
  {
    for (final int eventid : mEventIds)
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

  private static final long serialVersionUID = 1L;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}