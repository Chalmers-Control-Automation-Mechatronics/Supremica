package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;

/**
 * A controller ID is used to uniquely identify a controller instance to
 * nodes, for example, to clean up any allocated worker objects.
 * 
 * ControllerID implements the equals/hashCode contract in a way that works
 * correctly when the object is serialised, that is to say it does not depend
 * on reference equality.
 *
 * The constructor for this class is package-local; instances should only be
 * created by the server.
 *
 * @author Sam Douglas
 */
public final class ControllerID implements Serializable
{
  ControllerID(String id)
  {
    mId = id;
  }

  public boolean equals(Object o)
  {
    if (o == null) return false;
    if (!(o instanceof ControllerID)) return false;
    return mId.equals(((ControllerID)o).mId);
  }
  public int hashCode()
  {
    return mId.hashCode();
  }

  public String toString()
  {
    return mId;
  }

  private final String mId;
}