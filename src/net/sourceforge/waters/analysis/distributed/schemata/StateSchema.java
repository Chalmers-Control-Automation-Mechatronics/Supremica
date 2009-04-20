package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Arrays;
import java.util.Formatter;
import java.io.Serializable;

public class StateSchema implements Serializable
{
  StateSchema(String name, 
	      boolean initial, 
	      int[] propositions)
  {
    mName = name;
    mInitial = initial;
    mPropositionIds = propositions;
  }

  public String getName()
  {
    return mName;
  }

  public boolean getInitial()
  {
    return mInitial;
  }

  public int getPropositionId(int index)
  {
    return mPropositionIds[index];
  }

  public int getPropositionIdCount()
  {
    return mPropositionIds.length;
  }

  public String toString()
  {
    Formatter fmt = new Formatter();
    return fmt.format("(%s, %b, %s)", mName, mInitial, Arrays.toString(mPropositionIds)).toString();
  }

  private final String mName;
  private final boolean mInitial;
  private final int[] mPropositionIds;
}