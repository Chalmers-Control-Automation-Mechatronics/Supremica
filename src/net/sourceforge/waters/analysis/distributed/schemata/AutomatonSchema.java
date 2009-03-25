package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  protected AutomatonSchema()
  {
  }

  public String name;
  public int[] events;
  public StateSchema[] states;
  public int kind;
  public TransitionSchema[] transitions;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}