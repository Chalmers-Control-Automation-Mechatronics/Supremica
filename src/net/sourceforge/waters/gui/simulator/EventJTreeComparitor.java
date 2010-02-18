package net.sourceforge.waters.gui.simulator;

import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class EventJTreeComparitor implements Comparator<EventProxy>
{

  public EventJTreeComparitor(final Simulation sim, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    mSim = sim;
    mSortingMethods = sortingMethods;
  }

  public int compare(final EventProxy o1, final EventProxy o2)
  {
    return isLowerThan((EventProxy)o1, (EventProxy)o2, mSortingMethods);
  }

  /**
   * Returns 0 if a and b are equal, 1 if a is lower in the tree than b, and -1 if a is higher than b
   */
  private int isLowerThan(final EventProxy a, final EventProxy b, final List<Pair<Boolean, Integer>> sortingMethods)
  {
    if (sortingMethods.size() == 0)
    {
      return 0;
    }
    final int sortingMethod = sortingMethods.get(0).getSecond();
    final boolean isAscending = sortingMethods.get(0).getFirst();
    int compare;
    switch (sortingMethod)
    {
    case 0:
      compare = sortByType(a, b);
      break;
    case 1:
      compare = sortByName(a, b);
      break;
    case 2:
      compare = sortByEnabled(a, b);
      break;
    default:
      throw new UnsupportedOperationException("Unsupported Sort Method");
    }
    if ((compare < 0 && isAscending) || (compare > 0 && !isAscending))
      return 1;
    if ((compare < 0 && !isAscending || compare > 0 && isAscending))
      return -1;
    return isLowerThan(a, b, sortingMethods.subList(1, sortingMethods.size()));
  }

  /**
   * Returns a POSITIVE NUMBER if a comes before b alphabetically, NEGATIVE number if b comes before a, and ZERO if they are equal
   * @param a
   * @param b
   * @return
   */
  private int sortByName(final EventProxy a, final EventProxy b)
  {
    return -a.getName().compareTo(b.getName());
  }

  /**
   * Returns a POSITIVE NUMBER if a is before b in the default setting. The default order is ENABLED, WARNING, DISABLED, BLOCKING
   * @param a
   * @param b
   * @return
   */
  private int sortByEnabled(final EventProxy a, final EventProxy b)
  {
    boolean aIsWarning = false;
    boolean bIsWarning = false;
    for (final Step step : mSim.getWarningProperties().keySet())
    {
      if (step.getEvent() == a)
        aIsWarning = true;

      if (step.getEvent() == b)
        bIsWarning = true;
    }
    final boolean aIsEnabled = mSim.getActiveEvents().contains(a) && !aIsWarning;
    final boolean bIsEnabled = mSim.getActiveEvents().contains(b) && !bIsWarning;
    final boolean aIsBlocking = mSim.getNonControllable(a).size() != 0;
    final boolean bIsBlocking = mSim.getNonControllable(b).size() != 0;
    final boolean aIsDisabled = !aIsEnabled && !aIsBlocking && !aIsWarning;
    final boolean bIsDisabled = !bIsEnabled && !bIsBlocking && !bIsWarning;
    if (aIsEnabled)
    {
      if (bIsEnabled)
        return 0;
      else if (bIsWarning || bIsDisabled || bIsBlocking)
        return 1;
    }
    else if (aIsWarning)
    {
      if (bIsEnabled)
        return -1;
      else if (bIsWarning)
        return 0;
      else if (bIsDisabled || bIsBlocking)
        return 1;
    }
    else if (aIsDisabled)
    {
      if (bIsEnabled || bIsWarning)
        return -1;
      else if (bIsDisabled)
        return 0;
      else if (bIsBlocking)
        return 1;
    }
    else if (aIsBlocking)
    {
      if (bIsEnabled || bIsWarning || bIsDisabled)
        return -1;
      else if (bIsBlocking)
        return 0;
    }
    throw new IllegalArgumentException("Either a or b is not blocking, warning, disabled, or enabled!");
  }

  /**
   * Returns a POSITIVE NUMBER if a is a controllable and b isn't, NEGATIVE if b is controllable and a isn't, and ZERO if they are the same
   * @param a
   * @param b
   * @return
   */
   private int sortByType(final EventProxy a, final EventProxy b)
   {
     if (a.getKind() == EventKind.CONTROLLABLE)
     {
       if (b.getKind() == EventKind.CONTROLLABLE)
         return 0;
       else
         return 1;
     }
     else
     {
       if (b.getKind() == EventKind.CONTROLLABLE)
         return -1;
       else
         return 0;
     }
   }

   private final Simulation mSim;
   private final List<Pair<Boolean,Integer>> mSortingMethods;
}
