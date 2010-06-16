package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.base.Pair;

import java.lang.Comparable;


class AutomatonTableComparator implements Comparator<Object>
{

  //#########################################################################
  //# Constructor
  AutomatonTableComparator()
  {
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    mSortingMethods.add(new Pair<Boolean, Integer>(true, 1));
  }


  //#########################################################################
  //# Simple Access
  void addNewSortingMethod(final int column)
  {
    Pair<Boolean, Integer> remove = null;
    for (final Pair<Boolean, Integer> method : mSortingMethods)
    {
      if (method.getSecond() == column)
        remove = method;
    }
    if (remove != null)
    {
      mSortingMethods.remove(remove);
      mSortingMethods.add(0, new Pair<Boolean, Integer>(!remove.getFirst(), column));
    }
    else
      mSortingMethods.add(0, new Pair<Boolean, Integer>(true, column));
  }

  public String toString()
  {
    return mSortingMethods.toString();
  }


  //#########################################################################
  //# Interface java.util.Comparator<Object>
  @SuppressWarnings("unchecked")
  public int compare(final Object o1, final Object o2)
  {
    final List<?> list1 = (List<?>) o1;
    final List<?> list2 = (List<?>) o2;
    for (final Pair<Boolean,Integer> sortingMethod : mSortingMethods) {
      final boolean ascending = sortingMethod.getFirst();
      final int index = sortingMethod.getSecond();
      final Object item1 = list1.get(index);
      final Object item2 = list2.get(index);
      final int comp;
      if (item1 == null && item2 != null) {
        return ascending ? 1 : -1;
      } else if (item2 == null) {
        return ascending ? -1 : 1;
      } else if (item1 instanceof Comparable<?>) {
        final Comparable<Object> comp1 = (Comparable<Object>) item1;
        comp = comp1.compareTo(item2);
      } else {
        final String comp1 = item1.toString();
        final String comp2 = item2.toString();
        comp = comp1.compareTo(comp2);
      }
      if (comp != 0) {
        return ascending ? comp : -comp;
      }
    }
    return 0;
  }


  //#########################################################################
  //# Data Members
  private final List<Pair<Boolean,Integer>> mSortingMethods;

}
