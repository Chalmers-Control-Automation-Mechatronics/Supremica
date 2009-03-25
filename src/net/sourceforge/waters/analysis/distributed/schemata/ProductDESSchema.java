package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class ProductDESSchema implements Serializable
{
  protected ProductDESSchema()
  {
  }

  public String name;
  public AutomatonSchema[] automata;
  public EventSchema[] events;
}