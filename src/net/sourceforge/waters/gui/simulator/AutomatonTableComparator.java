//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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
