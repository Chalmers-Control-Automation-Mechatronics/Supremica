//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TLongStack;
import gnu.trove.stack.array.TLongArrayStack;

/**
 * @author Robi Malik
 */

public class WatersIntPairStack
{
  //#######################################################################
  //# Access
  public void clear()
  {
    mStack.clear();
    if (!mVisited.isEmpty()) {
      mVisited = new TLongHashSet();
    }
  }

  public boolean isEmpty()
  {
    return mStack.size() == 0;
  }

  public long peek()
  {
    return mStack.peek();
  }

  public long pop()
  {
    return mStack.pop();
  }

  public void push(final int s1, final int s2)
  {
    final long pair = createPair(s1, s2);
    if (mVisited.add(pair)) {
      mStack.push(pair);
    }
  }


  //#######################################################################
  //# Pair Decomposition
  public static long createPair(final int s1, final int s2)
  {
    final long lo, hi;
    if (s1 < s2) {
      lo = s1;
      hi = s2;
    } else {
      lo = s2;
      hi = s1;
    }
    return lo | (hi << 32);
  }

  public static int getLo(final long pair)
  {
    return (int) (pair & 0xffffffffL);
  }

  public static int getHi(final long pair)
  {
    return (int) (pair >>> 32);
  }


  //#######################################################################
  //# Data Members
  private final TLongStack mStack = new TLongArrayStack();
  private TLongHashSet mVisited = new TLongHashSet();
}
