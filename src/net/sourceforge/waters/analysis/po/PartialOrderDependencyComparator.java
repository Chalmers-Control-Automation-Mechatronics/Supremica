package net.sourceforge.waters.analysis.po;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.WatersIntComparator;

public class PartialOrderDependencyComparator implements WatersIntComparator
{
  public PartialOrderDependencyComparator(final int[] dependencyWeightings){
    mDependencyWeightings = dependencyWeightings;
    clearBits();
  }

  public void clearBits(){
    mToVisitedState = new BitSet(mDependencyWeightings.length);
  }

  public void reachesVisitedState(final int event){
    mToVisitedState.set(event);
  }

  @Override
  public int compare(final int val1, final int val2)
  {
    if(mToVisitedState.get(val1) == mToVisitedState.get(val2)){
      if (mDependencyWeightings[val1] < mDependencyWeightings[val2])
        return -1;
      else if (mDependencyWeightings[val1] == mDependencyWeightings[val2])
        return 0;
      else
        return 1;
    }
    else if (mToVisitedState.get(val1)){
      return -1;
    }
    else{
      return 1;
    }
  }

  private final int[] mDependencyWeightings;
  private BitSet mToVisitedState;
}
