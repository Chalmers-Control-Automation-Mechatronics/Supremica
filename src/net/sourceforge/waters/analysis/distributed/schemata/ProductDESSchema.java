package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Formatter;
import java.util.Arrays;
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

  public String toString()
  {
    Formatter fmt = new Formatter();

    fmt.format("Model name: %s\n", mName);
    fmt.format("Events: %s\n", Arrays.deepToString(mEvents));
    fmt.format(" - Automata -\n");

    for (AutomatonSchema aut : mAutomata)
      {
	fmt.format(aut.toString());
      }

    return fmt.toString();
  }
  
  private final String mName;
  private final AutomatonSchema[] mAutomata;
  private final EventSchema[] mEvents;
}