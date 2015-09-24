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

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.compositional.EnabledEventsCompositionalConflictChecker.EnabledEventsEventInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * A collection of selection heuristics that involve enabled events.
 *
 * @see CompositionalSelectionHeuristicFactory
 * @author Colin Pilbrow, Robi Malik
 */

public class EnabledEventsSelectionHeuristicFactory
  extends ConflictSelectionHeuristicFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static EnabledEventsSelectionHeuristicFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static EnabledEventsSelectionHeuristicFactory INSTANCE =
      new EnabledEventsSelectionHeuristicFactory();
  }


  //#######################################################################
  //# Constructors
  protected EnabledEventsSelectionHeuristicFactory()
  {
    register(MaxLE);
    register(MaxSp);
    register(MinSE);
    register(MinSSp);
  }


  //#########################################################################
  //# Selection Heuristic Creators
  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of local and always enabled events.
   */
  public static final SelectionHeuristicCreator MaxLE =
    new SelectionHeuristicCreator("MaxLE")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMaxLE();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MaxLE, MaxL, MaxC, MinE, MinS);
    }
  };

  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of local, always enabled, and selfloop-only events.
   */
  public static final SelectionHeuristicCreator MaxSp =
    new SelectionHeuristicCreator("MaxSp")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMaxSp();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MaxSp, MaxL, MaxC, MinE, MinS);
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product, while taking
   * into account always enabled events.
   */
  public static final SelectionHeuristicCreator MinSE =
    new SelectionHeuristicCreator("MinSE")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinSE();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinSE, MinS, MaxL, MaxC, MinE);
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product, while taking
   * into account always enabled events and selfloop-only events.
   */
  public static final SelectionHeuristicCreator MinSSp =
    new SelectionHeuristicCreator("MinSSp")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinSSp();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinSSp, MinS, MaxL, MaxC, MinE);
    }
  };


  //#########################################################################
  //# Inner Class SelectionHeuristicMaxLE
  public static class SelectionHeuristicMaxLE
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (EnabledEventsCompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      int alwaysEnabledEvents = 0;
      final List<AutomatonProxy> automataList = candidate.getAutomata();
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = mChecker.getEventInfo(event);
        if (info != null && // not a proposition
            info.getDisablingAutomata() != null &&
            automataList.containsAll(info.getDisablingAutomata())) {
          alwaysEnabledEvents++;
        }
      }
      return - (candidate.getLocalEventCount() + 0.5 * alwaysEnabledEvents) /
               candidate.getNumberOfEvents();
    }

    //#######################################################################
    //# Data Members
    private EnabledEventsCompositionalConflictChecker mChecker;
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMaxSp
  public static class SelectionHeuristicMaxSp
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (EnabledEventsCompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      final double totalEvents = candidate.getNumberOfEvents();
      int numSpecial = 0;
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = mChecker.getEventInfo(event);
        if (info != null) { // not a proposition
          if (info.isSingleDisablingCandidate(candidate)) {
            numSpecial++;
          }
          if (info.isOnlyNonSelfLoopCandidate(candidate)) {
            numSpecial++;
          }
        }
      }
      return - 0.5 * numSpecial / totalEvents;
    }

    //#######################################################################
    //# Data Members
    private EnabledEventsCompositionalConflictChecker mChecker;
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSE
  public static class SelectionHeuristicMinSE
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (EnabledEventsCompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      int numAlwaysEnabled = 0;
      final List<AutomatonProxy> automata = candidate.getAutomata();
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = mChecker.getEventInfo(event);
        if (info != null && // not a proposition
            info.getDisablingAutomata() != null &&
            automata.containsAll(info.getDisablingAutomata())) {
          numAlwaysEnabled++;
        }
      }
      return product *
             (totalEvents - localEvents - 0.5 * numAlwaysEnabled) /
             totalEvents;
    }

    //#######################################################################
    //# Data Members
    private EnabledEventsCompositionalConflictChecker mChecker;
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSSp
  public static class SelectionHeuristicMinSSp
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (EnabledEventsCompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      int numSpecial = 0;
      for (final EventProxy event : candidate.getOrderedEvents()) {
        final EnabledEventsEventInfo info = mChecker.getEventInfo(event);
        if (info != null) { // not a proposition
          if (info.isSingleDisablingCandidate(candidate)) {
            numSpecial++;
          }
          if (info.isOnlyNonSelfLoopCandidate(candidate)) {
            numSpecial++;
          }
        }
      }
      return product * (totalEvents - 0.5 * numSpecial) / totalEvents;
    }

    //#######################################################################
    //# Data Members
    private EnabledEventsCompositionalConflictChecker mChecker;
  }

}
