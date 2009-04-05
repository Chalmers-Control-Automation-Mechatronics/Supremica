package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class TransitionSchema implements Serializable
{
  TransitionSchema(int source, int target, int eventId)
  {
    mSource = source;
    mTarget = target;
    mEventId = eventId;
  }

  public int getSource()
  {
    return mSource;
  }

  public int getTarget()
  {
    return mTarget;
  }

  public int getEventId()
  {
    return mEventId;
  }

  private final int mSource;
  private final int mTarget;
  private final int mEventId;
}