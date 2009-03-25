package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  protected AutomatonSchema()
  {
  }

  public final String name;
  public final int[] events;
  public final StateSchema[] states;
  public final int kind;
  public final TransitionSchema[] transition;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}