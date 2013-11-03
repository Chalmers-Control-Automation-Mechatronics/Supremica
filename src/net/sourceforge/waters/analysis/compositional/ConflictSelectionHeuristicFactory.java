//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictSelectionHeuristicFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import org.apache.log4j.Logger;


/**
 * A collection of selection heuristics used by compositional conflict
 * checkers.
 *
 * @see CompositionalSelectionHeuristicFactory
 * @author Robi Malik
 */

public class ConflictSelectionHeuristicFactory
  extends CompositionalSelectionHeuristicFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static ConflictSelectionHeuristicFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static ConflictSelectionHeuristicFactory INSTANCE =
      new ConflictSelectionHeuristicFactory();
  }


  //#######################################################################
  //# Constructors
  protected ConflictSelectionHeuristicFactory()
  {
    register(MinSa);
    register(MinSyncA);
  }


  //#########################################################################
  //# Selection Heuristic Creators
  /**
   * The selection heuristic that chooses the candidate with the minimum
   * estimated number of precondition-marked states in the synchronous
   * product.
   */
  public static final SelectionHeuristicCreator MinSa =
    new SelectionHeuristicCreator("MinSa")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinSAlpha();
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      return CompositionalSelectionHeuristicFactory.createChainHeuristic
        (MinSa, MinS, MaxL, MaxC, MinE);
    }
  };

  /**
   * The selection heuristic that chooses the candidate with the minimum
   * number of precondition-marked states in the synchronous product.
   */
  public static final SelectionHeuristicCreator MinSyncA =
    new SelectionHeuristicCreator("MinSyncA")
  {
    @Override
    SelectionHeuristic<Candidate> createBaseHeuristic()
    {
      return new SelectionHeuristicMinSyncAlpha(1, 0);
    }

    @Override
    SelectionHeuristic<Candidate> createChainHeuristic()
    {
      @SuppressWarnings("unchecked")
      final SelectionHeuristic<Candidate>[] heuristics =
        new SelectionHeuristic[5];
      heuristics[0] = MinSyncA.createBaseHeuristic();
      heuristics[1] = MinS.createBaseHeuristic();
      heuristics[2] = MaxL.createBaseHeuristic();
      heuristics[3] = MaxC.createBaseHeuristic();
      heuristics[4] = MinE.createBaseHeuristic();
      final ChainSelectionHeuristic<Candidate> chain =
        new ChainSelectionHeuristic<>(heuristics);
      final SelectionHeuristic<Candidate> minSa = MinSa.createBaseHeuristic();
      chain.setPreOrder(minSa);
      return chain;
    }
  };


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSAlpha
  public static class SelectionHeuristicMinSAlpha
    extends NumericSelectionHeuristic<Candidate>
  {
    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (CompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        final CompositionalConflictChecker.AutomatonInfo info =
          mChecker.getAutomatonInfo(aut);
        product *= info.getNumberOfPreconditionMarkedStates();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }

    //#######################################################################
    //# Data Members
    private CompositionalConflictChecker mChecker;
  }


  //#########################################################################
  //# Inner Class SelectionHeuristicMinSyncAlpha
  public static class SelectionHeuristicMinSyncAlpha
    extends NumericSelectionHeuristic<Candidate>
    implements MonolithicSynchronousProductBuilder.StateCallback
  {
    //#######################################################################
    //# Constructor
    private SelectionHeuristicMinSyncAlpha(final int alphaWeight,
                                           final int nonAlphaWeight)
    {
      mAlphaWeight = alphaWeight;
      mNonAlphaWeight = nonAlphaWeight;
      mCurrentMinimum = Integer.MAX_VALUE;
    }

    //#######################################################################
    //# Interface for java.lang.Cloneable
    @Override
    public SelectionHeuristicMinSyncAlpha clone()
    {
      return new SelectionHeuristicMinSyncAlpha(mAlphaWeight, mNonAlphaWeight);
    }

    //#######################################################################
    //# Overrides for AbstractNumericSelectionHeuristic<Candidate>
    @Override
    public void setContext(final Object context)
    {
      mChecker = (CompositionalConflictChecker) context;
    }

    @Override
    public double getHeuristicValue(final Candidate candidate)
    {
      final MonolithicSynchronousProductBuilder syncBuilder =
        mChecker.getSynchronousProductBuilder();
      final int limit = mChecker.getCurrentInternalStateLimit();
      syncBuilder.setNodeLimit(limit);
      syncBuilder.setConstructsResult(false);
      syncBuilder.setStateCallback(this);
      final List<AutomatonProxy> automata = candidate.getAutomata();
      final ProductDESProxy des = mChecker.createProductDESProxy(automata);
      syncBuilder.setModel(des);
      try {
        mCount = 0;
        syncBuilder.run();
        if (mCount < mCurrentMinimum) {
          mCurrentMinimum = mCount;
        }
      } catch (final OutOfMemoryError error) {
        final Logger logger = mChecker.getLogger();
        logger.debug("<out of memory>");
        return Double.POSITIVE_INFINITY;
      } catch (final OverflowException overflow) {
        return Double.POSITIVE_INFINITY;
      } catch (final AnalysisException exception) {
        throw exception.getRuntimeException();
      } finally {
        final CompositionalVerificationResult stats =
          mChecker.getAnalysisResult();
        final AutomatonResult result = syncBuilder.getAnalysisResult();
        stats.addSynchronousProductAnalysisResult(result);
      }
      return mCount;
    }

    @Override
    public void reset()
    {
      super.reset();
      mCurrentMinimum = Integer.MAX_VALUE;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.monolithic.
    //# MonolithicSynchronousProductBuilder.StateCounter
    @Override
    public boolean newState(final int[] tuple)
      throws OverflowException
    {
      final MonolithicSynchronousProductBuilder syncBuilder =
        mChecker.getSynchronousProductBuilder();
      boolean alpha = true;
      for (int a = 0; a < tuple.length; a++) {
        final List<EventProxy> props =
          syncBuilder.getStateMarking(a, tuple[a]);
        if (props.isEmpty()) {
          alpha = false;
          break;
        }
      }
      if (alpha) {
        mCount += mAlphaWeight;
      } else {
        mCount += mNonAlphaWeight;
      }
      if (mCount >= mCurrentMinimum) {
        throw new OverflowException(OverflowKind.NODE, mCurrentMinimum);
      }
      return true;
    }

    @Override
    public void recordStatistics(final AutomatonResult result)
    {
      result.setPeakNumberOfNodes(mCount);
    }

    //#######################################################################
    //# Data Members
    private final int mAlphaWeight;
    private final int mNonAlphaWeight;
    private CompositionalConflictChecker mChecker;
    private int mCount;
    private int mCurrentMinimum;
  }

}