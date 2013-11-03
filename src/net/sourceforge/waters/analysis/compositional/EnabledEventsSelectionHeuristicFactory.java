//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   EnabledEventsSelectionHeuristicFactory
//###########################################################################
//# $Id$
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
    register(MinSE);
  }


  //#########################################################################
  //# Selection Heuristic Creators
  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of local and always enabled events.
   */
  public static final SelectionHeuristicCreator MaxLE = new SelectionHeuristicCreator("MaxLE")
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
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product, while taking
   * into account always enabled events.
   */
  public static final SelectionHeuristicCreator MinSE = new SelectionHeuristicCreator("MinSE")
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


  //#########################################################################
  //# Inner Class SelectionHeuristicMaxLE
  public static class SelectionHeuristicMaxLE
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
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
  //# Inner Class SelectionHeuristicMinSE
  public static class SelectionHeuristicMinSE
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
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
      return product *
             (totalEvents - localEvents - 0.5 * alwaysEnabledEvents) /
             totalEvents;
    }

    //#######################################################################
    //# Data Members
    private EnabledEventsCompositionalConflictChecker mChecker;
  }

}