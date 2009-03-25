package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class ProductDESSchema implements Serializable
{
  protected ProductDESSchema()
  {
  }

  public final String name;
  public final AutomatonSchema[] automata;
  public final EventSchema[] events;
}