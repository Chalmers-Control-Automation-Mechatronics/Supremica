package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class StateSchema implements Serializable
{
  protected StateSchema()
  {
  }

  public String name;
  public boolean initial;
  public int[] propositions;
}