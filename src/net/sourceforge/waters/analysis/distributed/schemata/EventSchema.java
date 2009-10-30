package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Formatter;
import java.io.Serializable;

public class EventSchema implements Serializable
{
  EventSchema(String name, int kind, boolean observable)
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

  public String toString()
  {
    Formatter fmt = new Formatter();
    return fmt.format("(%s, %d, %b)", mName, mKind, mObservable).toString();
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