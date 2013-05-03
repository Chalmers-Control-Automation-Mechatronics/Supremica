package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Arrays;
import java.util.Formatter;
import java.io.Serializable;

public class StateSchema implements Serializable
{
  StateSchema(final String name,
	      final boolean initial,
	      final int[] propositions)
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

  public int getPropositionId(final int index)
  {
    return mPropositionIds[index];
  }

  public int getPropositionIdCount()
  {
    return mPropositionIds.length;
  }

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      return fmt.format("(%s, %b, %s)", mName, mInitial,
                        Arrays.toString(mPropositionIds)).toString();
    } finally {
      fmt.close();
    }
  }

  private final String mName;
  private final boolean mInitial;
  private final int[] mPropositionIds;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}