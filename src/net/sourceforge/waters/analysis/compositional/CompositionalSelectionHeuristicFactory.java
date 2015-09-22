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

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.log4j.Logger;


/**
 * <P>A collection of selection heuristics for compositional model
 * analysers.</P>
 *
 * <P>The selection heuristic factory declares constants {@link #MaxC},
 * {@link #MaxL}, {@link #MinE}, {@link #MinF}, {@link #MinS}, and
 * {@link #MinSync}, which can be passed to the {@link
 * AbstractCompositionalModelAnalyzer#setSelectionHeuristic(SelectionHeuristicCreator)
 * setSelectionHeuristic()} method of a {@link AbstractCompositionalModelAnalyzer}.</P>
 *
 * <P>The selection heuristic factory also provides the functionality to
 * associate heuristics with names for use by the command line tool or
 * other user interfaces.</P>
 *
 * <P>Technically, the factory only provides selection heuristic
 * <I>creators</I> ({@link SelectionHeuristicCreator}), from which the
 * actual heuristics are obtained by calling the {@link
 * SelectionHeuristicCreator#createBaseHeuristic() createBaseHeuristic()}
 * and  {@link SelectionHeuristicCreator#createChainHeuristic()
 * createChainHeuristic()} methods.</P>
 *
 * @author Robi Malik
 */

public class CompositionalSelectionHeuristicFactory
  extends ListedEnumFactory<SelectionHeuristicCreator>
{

  //#########################################################################
  //# Singleton Pattern
  public static CompositionalSelectionHeuristicFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static CompositionalSelectionHeuristicFactory INSTANCE =
      new CompositionalSelectionHeuristicFactory();
  }


  //#########################################################################
  //# Constructors
  protected CompositionalSelectionHeuristicFactory()
  {
    register(MaxL);
    register(MaxC);
    register(MinE);
    register(MinF);
    register(MinS);
    register(MinSync);
  }


  //#######################################################################
  //# Migration
  protected SelectionHeuristicCreator getEnumValue(final SelectionHeuristicCreator method)
  {
    final String name = method.toString();
    return getEnumValue(name);
  }


  //#######################################################################
  //# Chain Construction
  /**
   * Creates a chain heuristic based on the given selecting heuristic
   * creators. The returned heuristic applies the base heuristic obtained
   * from the given heuristic creators in the specified order.
   */
  public static ChainSelectionHeuristic<Candidate> createChainHeuristic
    (final SelectionHeuristicCreator... methods)
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<Candidate>[] heuristics =
    new SelectionHeuristic[methods.length];
    for (int i = 0; i < methods.length; i++) {
      final SelectionHeuristicCreator method = methods[i];
      heuristics[i] = method.createBaseHeuristic();
    }
    return new ChainSelectionHeuristic<>(heuristics);
  }


  //#########################################################################
  //# Selection Heuristic Creators
  /**
   * The selection heuristic that chooses the candidate with the highest
   * proportion of common events.
   * An event is considered as common if it is used by at least two
   * automata of the candidate.
   */
  public static final SelectionHeuristicCreator MaxC =
    new SelectionHeuristicCreator("MaxC")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMaxC();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MaxC, MaxL, MinE, MinS);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the highest
   * proportion of local events.
   */
  public static final SelectionHeuristicCreator MaxL =
    new SelectionHeuristicCreator("MaxL")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMaxL();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MaxL, MaxC, MinE, MinS);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the smallest
   * alphabet extension. The alphabet extension is given by the quotient
   * of the number of events of a candidate divided by the largest number of
   * events of a single automaton of the candidate.
   */
  public static final SelectionHeuristicCreator MinE =
    new SelectionHeuristicCreator("MinE")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinE();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinE, MaxL, MaxC, MinS);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the minimum
   * frontier, i.e., the smallest number of automata that share events
   * with the automata of this candidate.
   */
  public static final SelectionHeuristicCreator MinF =
    new SelectionHeuristicCreator("MinF")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinF();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinF, MinSync, MaxL, MaxC, MinE);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the minimum
   * estimated number of states in the synchronous product.
   */
  public static final SelectionHeuristicCreator MinS =
    new SelectionHeuristicCreator("MinS")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinS();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinS, MaxL, MaxC, MinE);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the minimum
   * actual number of states in the synchronous product.
   */
  public static final SelectionHeuristicCreator MinSync =
    new SelectionHeuristicCreator("MinSync")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinSync();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      @SuppressWarnings("unchecked")
      final SelectionHeuristic<Candidate>[] heuristics =
        new SelectionHeuristic[4];
      heuristics[0] = MinSync.createBaseHeuristic();
      heuristics[1] = MaxL.createBaseHeuristic();
      heuristics[2] = MaxC.createBaseHeuristic();
      heuristics[3] = MinE.createBaseHeuristic();
      final ChainSelectionHeuristic<Candidate> chain =
        new ChainSelectionHeuristic<>(heuristics);
      final SelectionHeuristic<Candidate> minS = MinS.createBaseHeuristic();
      chain.setPreOrder(minS);
      return chain;
    }
  };


  //#########################################################################
  //# Inner Class SelectionHeuristicMaxC
  public static class SelectionHeuristicMaxC
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getCommonEventCount() /
               candidate.getNumberOfEvents();
    }
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMaxL
  public static class SelectionHeuristicMaxL
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getLocalEventCount() /
               candidate.getNumberOfEvents();
    }
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinE
  public static class SelectionHeuristicMinE
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      final int unionAlphabetSize = candidate.getNumberOfEvents();
      int largestAlphabetSize = 0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        final int size = Candidate.countEvents(aut);
        if (size > largestAlphabetSize) {
          largestAlphabetSize = size;
        }
      }
      return (double) unionAlphabetSize / (double) largestAlphabetSize;
    }
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinF
  public static class SelectionHeuristicMinF
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mAnalyzer = (AbstractCompositionalModelAnalyzer) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      final Collection<AutomatonProxy> automata = candidate.getAutomata();
      final Collection<AutomatonProxy> frontier = new THashSet<AutomatonProxy>();
      final Collection<EventProxy> local = candidate.getLocalEvents();
      final Collection<EventProxy> shared = new THashSet<EventProxy>();
      for (final AutomatonProxy aut : automata) {
        for (final EventProxy event : aut.getEvents()) {
          final AbstractCompositionalModelAnalyzer.EventInfo info =
            mAnalyzer.getEventInfo(event);
          if (info != null && !local.contains(event) && shared.add(event)) {
            for (final AutomatonProxy other : info.getSortedAutomataList()) {
              if (!automata.contains(other)) {
                frontier.add(other);
              }
            }
          }
        }
      }
      return frontier.size();
    }

    //#######################################################################
    //# Data Members
    private AbstractCompositionalModelAnalyzer mAnalyzer;
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinS
  public static class SelectionHeuristicMinS
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSync
  public static class SelectionHeuristicMinSync
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for NumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mAnalyzer = (AbstractCompositionalModelAnalyzer) context;
      mStateLimit = mAnalyzer.getCurrentInternalStateLimit();
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      final MonolithicSynchronousProductBuilder syncBuilder =
        mAnalyzer.getSynchronousProductBuilder();
      syncBuilder.setNodeLimit(mStateLimit);
      syncBuilder.setDetailedOutputEnabled(false);
      syncBuilder.setStateCallback(null);
      final List<EventProxy> empty = Collections.emptyList();
      syncBuilder.setPropositions(empty);
      final List<AutomatonProxy> automata = candidate.getAutomata();
      final ProductDESProxy des = mAnalyzer.createProductDESProxy(automata);
      syncBuilder.setModel(des);
      try {
        syncBuilder.run();
        final AnalysisResult result = syncBuilder.getAnalysisResult();
        final double dsize = result.getTotalNumberOfStates();
        final int size = (int) Math.round(dsize);
        if (size < mStateLimit) {
          mStateLimit = size;
        }
        return dsize;
      } catch (final OutOfMemoryError error) {
        final Logger logger = mAnalyzer.getLogger();
        logger.debug("<out of memory>");
        return Double.POSITIVE_INFINITY;
      } catch (final OverflowException overflow) {
        return Double.POSITIVE_INFINITY;
      } catch (final AnalysisException exception) {
        throw exception.getRuntimeException();
      } finally {
        final CompositionalAnalysisResult stats = mAnalyzer.getAnalysisResult();
        final SynchronousProductResult result = syncBuilder.getAnalysisResult();
        stats.addSynchronousProductAnalysisResult(result);
      }
    }

    @Override
    protected void reset()
    {
      super.reset();
      mStateLimit = mAnalyzer.getCurrentInternalStateLimit();
    }

    //#######################################################################
    //# Data Members
    private AbstractCompositionalModelAnalyzer mAnalyzer;
    private int mStateLimit;
  }

}







