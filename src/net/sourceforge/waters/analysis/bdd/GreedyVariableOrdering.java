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

package net.sourceforge.waters.analysis.bdd;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


class GreedyVariableOrdering
  extends AbstractCollection<AutomatonProxy>
{

  //#########################################################################
  //# Constructor
  GreedyVariableOrdering(final ProductDESProxy des,
                         final KindTranslator translator)
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    mNumPlants = 0;
    mNumSpecs = 0;
    for (final AutomatonProxy aut : automata) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
        mNumPlants++;
        break;
      case SPEC:
        mNumSpecs++;
        break;
      default:
        break;
      }
    }
    mAutomata = new ArrayList<AutomatonProxy>(mNumPlants + mNumSpecs);
    for (final AutomatonProxy aut : automata) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        mAutomata.add(aut);
        break;
      default:
        break;
      }
    }
    final Collection<EventProxy> events = des.getEvents();
    mNumEvents = events.size();
    mKindTranslator = translator;
  }


  //#########################################################################
  //# Interface java.util.Collection<AutomatonProxy>
  public int size()
  {
    return mNumPlants + mNumSpecs;
  }

  public Iterator<AutomatonProxy> iterator()
  {
    return new VariableOrderingIterator();
  }


  //#########################################################################
  //# Inner Class VariableOrderingIterator
  private class VariableOrderingIterator
  implements Iterator<AutomatonProxy>
  {

    //#######################################################################
    //# Constructor
    private VariableOrderingIterator()
    {
      mRemainingAutomata = new ArrayList<AutomatonProxy>(mAutomata);
      mEventMap = new HashMap<EventProxy,Integer>(mNumEvents);
      mCurrentLevel = 0;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return !mRemainingAutomata.isEmpty();
    }

    public AutomatonProxy next()
    {
      try {
        final AutomatonProxy aut0 = mRemainingAutomata.get(0);
        Score best = getScore(aut0);
        int bestindex = 0;
        int numremaining = mRemainingAutomata.size();
        for (int index = 1; index < numremaining; index++) {
          final AutomatonProxy aut = mRemainingAutomata.get(index);
          final Score score = getScore(aut);
          if (score.isBetterThan(best)) {
            best = score;
            bestindex = index;
          }
        }
        AutomatonProxy bestaut = mRemainingAutomata.remove(--numremaining);
        if (bestindex < numremaining) {
          bestaut = mRemainingAutomata.set(bestindex, bestaut);
        }
        for (final EventProxy event : bestaut.getEvents()) {
          mEventMap.put(event, mCurrentLevel);
        }
        mCurrentLevel++;
        return bestaut;
      } catch (final IndexOutOfBoundsException exception) {
        final NoSuchElementException rethrown = new NoSuchElementException();
        rethrown.initCause(exception);
        throw rethrown;
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("VariableOrderingIterator can't remove!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private Score getScore(final AutomatonProxy aut)
    {
      int numnewevents = 0;
      double sharedeventsweight = 0;
      for (final EventProxy event : aut.getEvents()) {
        final Integer level = mEventMap.get(event);
        if (level == null) {
          numnewevents++;
        } else {
          final int delta = mCurrentLevel - level;
          if (delta < 16) {
            sharedeventsweight += 2.0 / (1 << delta);
          }
        }
      }
      if (mCurrentLevel == 0) {
        numnewevents = -numnewevents;
      }
      return new Score(aut, numnewevents, sharedeventsweight);
    }

    //#######################################################################
    //# Data Members
    private final List<AutomatonProxy> mRemainingAutomata;
    private final Map<EventProxy,Integer> mEventMap;
    private int mCurrentLevel;

  }


  //#########################################################################
  //# Inner Class Score
  private class Score {

    //#######################################################################
    //# Constructor
    private Score(final AutomatonProxy aut,
                  final int numnewevents,
                  final double sharedeventsweight)
    {
      mAutomaton = aut;
      mNumNewEvents = numnewevents;
      mSharedEventsWeight = sharedeventsweight;
    }

    //#######################################################################
    //# Comparing
    private boolean isBetterThan(final Score score)
    {
      if (mNumNewEvents != score.mNumNewEvents) {
        return mNumNewEvents < score.mNumNewEvents;
      } else if (mSharedEventsWeight != score.mSharedEventsWeight) {
        return mSharedEventsWeight > score.mSharedEventsWeight;
      }
      final ComponentKind kind0 = mKindTranslator.getComponentKind(mAutomaton);
      final ComponentKind kind1 =
        mKindTranslator.getComponentKind(score.mAutomaton);
      if (kind0 != kind1) {
        return kind0 == ComponentKind.PLANT;
      }
      final int size0 = mAutomaton.getStates().size();
      final int size1 = score.mAutomaton.getStates().size();
      if (size0 != size1) {
        return size0 > size1;
      }
      final String name0 = mAutomaton.getName();
      final String name1 = score.mAutomaton.getName();
      return name0.compareTo(name1) < 0;
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private final int mNumNewEvents;
    private final double mSharedEventsWeight;

  }


  //#########################################################################
  //# Data Members
  private final List<AutomatonProxy> mAutomata;
  private int mNumPlants;
  private int mNumSpecs;
  private final int mNumEvents;
  private final KindTranslator mKindTranslator;

}
