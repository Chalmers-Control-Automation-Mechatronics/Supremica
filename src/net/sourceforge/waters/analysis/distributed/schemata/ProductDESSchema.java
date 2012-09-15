package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Formatter;
import java.util.Arrays;
import java.io.Serializable;

public class ProductDESSchema implements Serializable
{
  ProductDESSchema(final String name,
		   final AutomatonSchema[] automata,
		   final EventSchema[] events)
  {
    mName = name;
    mAutomata = automata;
    mEvents = events;
  }

  public String getName()
  {
    return mName;
  }

  public AutomatonSchema getAutomaton(final int index)
  {
    return mAutomata[index];
  }

  public int getAutomataCount()
  {
    return mAutomata.length;
  }

  public AutomatonSchema[] getAutomata()
  {
    return mAutomata.clone();
  }

  public EventSchema getEvent(final int index)
  {
    return mEvents[index];
  }

  public int getEventCount()
  {
    return mEvents.length;
  }

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      fmt.format("Model name: %s\n", mName);
      fmt.format("Events: %s\n", Arrays.deepToString(mEvents));
      fmt.format(" - Automata -\n");
      for (final AutomatonSchema aut : mAutomata) {
        fmt.format(aut.toString());
      }
      return fmt.toString();
    } finally {
      fmt.close();
    }
  }

  private final String mName;
  private final AutomatonSchema[] mAutomata;
  private final EventSchema[] mEvents;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}