package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Formatter;
import java.io.Serializable;

public class TransitionSchema implements Serializable
{
  TransitionSchema(final int source, final int target, final int eventId)
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

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      return fmt.format("%d -%d-> %d", mSource, mEventId, mTarget).toString();
    } finally {
      fmt.close();
    }
  }

  private final int mSource;
  private final int mTarget;
  private final int mEventId;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}