//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;

public class FindBlockingStates
{
  public FindBlockingStates(final ListBufferTransitionRelation automaton,
                            final int marking)
  {
    mAutomaton = automaton;
    mBlockingStates = new TIntHashSet();
    if (marking != -1) {
      final TIntArrayList reaches = new TIntArrayList();
      for (int s = 0; s < mAutomaton.getNumberOfStates(); s++) {
        if (!mAutomaton.isMarked(s, marking)) {
          mBlockingStates.add(s);
        } else {
          reaches.add(s);
        }
      }
      mAutomaton.reconfigure(ListBufferTransitionRelation.CONFIG_ALL);
      while (!reaches.isEmpty()) {
        final int state = reaches.removeAt(reaches.size() - 1);
        final TransitionIterator ti = mAutomaton.createPredecessorsReadOnlyIterator(state);
        while (ti.advance()) {
          if (mBlockingStates.remove(ti.getCurrentSourceState())) {
            reaches.add(ti.getCurrentSourceState());
          }
        }
      }
    }
  }

  public TIntHashSet getBlockingStates()
  {
    return mBlockingStates;
  }

  private final ListBufferTransitionRelation mAutomaton;
  private final TIntHashSet mBlockingStates;
}
