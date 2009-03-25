package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class StateSchema implements Serializable
{
  protected StateSchema()
  {
  }

  public final String name;
  public final boolean initial;
  public final int[] propositions;
}