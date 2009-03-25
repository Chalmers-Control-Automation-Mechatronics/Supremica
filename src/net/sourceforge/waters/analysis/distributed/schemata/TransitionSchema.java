package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class TransitionSchema implements Serializable
{
  protected TransitionSchema()
  {
  }

  public final int source;
  public final int target;
  public final int event;
}