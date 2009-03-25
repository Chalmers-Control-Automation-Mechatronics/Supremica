package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class TransitionSchema implements Serializable
{
  protected TransitionSchema()
  {
  }

  public int source;
  public int target;
  public int event;
}