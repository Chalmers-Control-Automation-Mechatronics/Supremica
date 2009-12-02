package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.PropositionIcon;

import java.lang.Comparable;

public class AutomatonTableComparitor<K> implements Comparator<K>
{

  public AutomatonTableComparitor()
  {
    sortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    sortingMethods.add(new Pair<Boolean, Integer>(true, 1));
  }

  public void addNewSortingMethod(final int column)
  {
    Pair<Boolean, Integer> remove = null;
    for (final Pair<Boolean, Integer> method : sortingMethods)
    {
      if (method.getSecond() == column)
        remove = method;
    }
    if (remove != null)
    {
      sortingMethods.remove(remove);
      sortingMethods.add(0, new Pair<Boolean, Integer>(!remove.getFirst(), column));
    }
    else
      sortingMethods.add(0, new Pair<Boolean, Integer>(true, column));
  }

  @SuppressWarnings("unchecked")
  public int compare(final Object o1, final Object o2)
  {
    if (!checkValid(o1) || !checkValid(o2))
      throw new ClassCastException("Either o1 or o2 do not have the type array, and don't have the types {Icon, String, Icon, Icon, String");
    for (final Pair<Boolean, Integer> sortingMethod : sortingMethods)
    {
      final List arrayO1 = (List)o1;
      final List arrayO2 = (List)o2;
      Comparable<Object> compare01;
      Comparable<Object> compare02;
      try
      {
        compare01 = (Comparable<Object>)arrayO1.get(sortingMethod.getSecond());
        compare02 = (Comparable<Object>)arrayO2.get(sortingMethod.getSecond());
      }
      catch (final ClassCastException e)
      {
        compare01 = (Comparable<Object>)(Object)(arrayO1.get(sortingMethod.getSecond()).toString());
        compare02 = (Comparable<Object>)(Object)(arrayO2.get(sortingMethod.getSecond()).toString());
      }
      final int compared = compare01.compareTo(compare02);
      if (compared < 0)
        if (sortingMethod.getFirst())
          return -1;
        else return 1;
      else if (compared > 0)
        if (sortingMethod.getFirst())
          return 1;
        else
          return -1;
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  private boolean checkValid(final Object object)
  {
    final List<Object> array = (List<Object>)object;
    return (array.get(0).getClass()== ImageIcon.class
        && array.get(1).getClass() == String.class
        && array.get(2).getClass() == ImageIcon.class
        && array.get(3).getClass() == PropositionIcon.class
        && array.get(4).getClass() == String.class);
  }

  // ####################################################################
  // # Data Members

  public String toString()
  {
    return sortingMethods.toString();
  }

  private final ArrayList<Pair<Boolean, Integer>> sortingMethods;

}
