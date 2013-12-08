package net.sourceforge.waters.analysis.po;

import net.sourceforge.waters.analysis.tr.WatersIntComparator;

public class PartialOrderDependencyComparator implements WatersIntComparator
{
  public PartialOrderDependencyComparator(final int[] dependencyWeightings){
    mDependencyWeightings = dependencyWeightings;
  }
  @Override
  public int compare(final int val1, final int val2)
  {
    if (mDependencyWeightings[val1] < mDependencyWeightings[val2])
      return -1;
    else if (mDependencyWeightings[val1] == mDependencyWeightings[val2])
      return 0;
    else
      return 1;
  }

  int[] mDependencyWeightings;

}
