package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class EventSchema implements Serializable
{
  protected EventSchema()
  {
  }

  public String name;
  public int kind;
  public boolean observable;

  public static final int CONTROLLABLE = 0;
  public static final int UNCONTROLLABLE = 1;
  public static final int PROPOSITION = 2;
}