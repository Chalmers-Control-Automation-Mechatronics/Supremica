//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinSync0
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;


/**
 * <P>An abstract base class to implement the <STRONG>MinSync</STRONG> and
 * <STRONG>MinSync</STRONG><SUP>&alpha;</SUP> candidate selection heuristics
 * for compositional model analysers of type {@link
 * AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>This base class contains support for heuristics that use a synchronous
 * product builder, and for accessing overflow candidates cached by the
 * analyser's preselection heuristic.</P>
 *
 * @author Robi Malik
 */

abstract class SelectionHeuristicMinSync0
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mSynchronousProductBuilder != null) {
      mSynchronousProductBuilder.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for SelectionHeuristic<TRCandidate>
  @Override
  public TRCandidate select(final Collection<? extends TRCandidate> candidates)
    throws AnalysisException
  {
    final TRCandidate candidate = super.select(candidates);
    if (isOverflowCandidate(candidate)) {
      return null;
    } else {
      return candidate;
    }
  }


  //#########################################################################
  //# Overrides for NumericSelectionHeuristic<TRCandidate>
  @Override
  public void setContext(final Object context)
  {
    super.setContext(context);
    mAnalyzer = (AbstractTRCompositionalAnalyzer) context;
    if (context != null) {
      final KindTranslator translator = mAnalyzer.getKindTranslator();
      mSynchronousProductBuilder =
        new TRSynchronousProductBuilder(null, translator);
      mSynchronousProductBuilder.setPruningDeadlocks
        (mAnalyzer.isPruningDeadlocks());
      mSynchronousProductBuilder.setNodeLimit
        (mAnalyzer.getInternalStateLimit());
      mSynchronousProductBuilder.setTransitionLimit
        (mAnalyzer.getInternalTransitionLimit());
      mSynchronousProductBuilder.setRemovingSelfloops(true);
      mSynchronousProductBuilder.setPruningDeadlocks
        (mAnalyzer.isPruningDeadlocks());
      mSynchronousProductBuilder.setDetailedOutputEnabled(false);
    } else {
      mSynchronousProductBuilder = null;
    }
  }


  //#########################################################################
  //# Simple Access
  AbstractTRCompositionalAnalyzer getAnalyzer()
  {
    return mAnalyzer;
  }

  TRSynchronousProductBuilder getSynchronousProductBuilder()
  {
    return mSynchronousProductBuilder;
  }


  //#########################################################################
  //# Auxiliary Methods
  void addOverflowCandidate(final TRCandidate candidate)
  {
    final TRPreselectionHeuristic cache = mAnalyzer.getPreselectionHeuristic();
    cache.addOverflowCandidate(candidate);
  }

  boolean isOverflowCandidate(final TRCandidate candidate)
  {
    final TRPreselectionHeuristic cache = mAnalyzer.getPreselectionHeuristic();
    return cache.isOverflowCandidate(candidate);
  }


  //#########################################################################
  //# Data Members
  private AbstractTRCompositionalAnalyzer mAnalyzer;
  private TRSynchronousProductBuilder mSynchronousProductBuilder;

}
