package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Formatter;
import java.io.Serializable;

public class EventSchema implements Serializable
{
  EventSchema(final String name, final int kind, final boolean observable)
  {
    mName = name;
    mKind = kind;
    mObservable = observable;
  }

  public String getName()
  {
    return mName;
  }

  public int getKind()
  {
    return mKind;
  }

  public boolean getObservable()
  {
    return mObservable;
  }

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      return fmt.format("(%s, %d, %b)", mName, mKind, mObservable).toString();
    } finally {
      fmt.close();
    }
  }

  private final String mName;
  private final int mKind;
  private final boolean mObservable;


  //#########################################################################
  //# Class Constants
  public static final int CONTROLLABLE = 0;
  public static final int UNCONTROLLABLE = 1;
  public static final int PROPOSITION = 2;

  private static final long serialVersionUID = 1L;

}