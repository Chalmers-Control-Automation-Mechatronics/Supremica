package net.sourceforge.waters.analysis.distributed.schemata;

import java.io.Serializable;

public class ProductDESSchema implements Serializable
{
  ProductDESSchema(String name, 
		   AutomatonSchema[] automata,
		   EventSchema[] events)
  {
    mName = name;
    mAutomata = automata;
    mEvents = events;
  }

  public String getName()
  {
    return mName;
  }

  public AutomatonSchema getAutomaton(int index)
  {
    return mAutomata[index];
  }

  public int getAutomataCount()
  {
    return mAutomata.length;
  }

  public EventSchema getEvent(int index)
  {
    return mEvents[index];
  }

  public int getEventCount()
  {
    return mEvents.length;
  }
  
  private final String mName;
  private final AutomatonSchema[] mAutomata;
  private final EventSchema[] mEvents;
}