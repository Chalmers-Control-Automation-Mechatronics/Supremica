package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  AutomatonSchema(String name,
		  int[] eventIds,
		  StateSchema[] states,
		  int kind,
		  TransitionSchema[] transitions)
  {
    mName = name;
    mEventIds = eventIds;
    mStates = states;
    mKind = kind;
    mTransitions = transitions;
  }

  public String getName()
  {
    return mName;
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

  private final String mName;
  private final int[] mEventIds;
  private final StateSchema[] mStates;
  private final int mKind;
  private final TransitionSchema[] mTransitions;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}