//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinSync
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.List;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * <P>The <STRONG>MinSync</STRONG><SUP>&alpha;</SUP> candidate selection
 * heuristic for compositional model analysers of type {@link
 * AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinSync</STRONG><SUP>&alpha;</SUP> heuristic computes the
 * synchronous composition of each candidate and gives preference to the
 * candidate with the fewest precondition-marked states in the synchronous
 * composition.</P>
 *
 * <P>The implementation attempts to improve performance using state limits,
 * aborting the synchronous product computation as soon as the number of
 * precondition-marked states encountered exceeds the running minimum.</P>
 *
 * <P><I>Reference:</I><BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinSyncA
  extends NumericSelectionHeuristic<TRCandidate>
  implements TRSynchronousProductBuilder.StateCallback
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
  //# Overrides for NumericSelectionHeuristic<TRCandidate>
  @Override
  public void setContext(final Object context)
  {
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
      mSynchronousProductBuilder.setDetailedOutputEnabled(false);
      mSynchronousProductBuilder.setRemovingSelfloops(true);
    } else {
      mSynchronousProductBuilder = null;
    }
  }

  @Override
  public SelectionHeuristic<TRCandidate> createDecisiveHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<TRCandidate>[] array = new SelectionHeuristic[] {
      this,
      AbstractTRCompositionalAnalyzer.SEL_MaxL,
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinE,
      AbstractTRCompositionalAnalyzer.SEL_MinS
    };
    final ChainSelectionHeuristic<TRCandidate> chain =
      new ChainSelectionHeuristic<TRCandidate>(array);
    chain.setPreOrder(AbstractTRCompositionalAnalyzer.SEL_MinS0a);
    return chain;
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    try {
      mCount = 0;
      final ProductDESProxyFactory factory = mAnalyzer.getFactory();
      final ProductDESProxy des = candidate.createProductDESProxy(factory);
      final EventEncoding syncEncoding =
        candidate.createSyncEventEncoding();
      mSynchronousProductBuilder.setModel(des);
      mSynchronousProductBuilder.setEventEncoding(syncEncoding);
      mSynchronousProductBuilder.run();
      if (mCount < mCurrentMinimum) {
        mCurrentMinimum = mCount;
      }
      return mCount;
    } catch (final OutOfMemoryError error) {
      final Logger logger = mAnalyzer.getLogger();
      logger.debug("<out of memory>");
      return Double.POSITIVE_INFINITY;
    } catch (final OverflowException overflow) {
      return Double.POSITIVE_INFINITY;
    } catch (final AnalysisException exception) {
      throw exception.getRuntimeException();
    } finally {
      mMarkingInfo = null;
      final CompositionalAnalysisResult stats = mAnalyzer.getAnalysisResult();
      final SynchronousProductResult result =
        mSynchronousProductBuilder.getAnalysisResult();
      stats.addSynchronousProductAnalysisResult(result);
    }
  }

  @Override
  protected void reset()
  {
    super.reset();
    mCurrentMinimum = Integer.MAX_VALUE;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.analysis.monolithic.
  //# TRSynchronousProductBuilder.StateCallBack
  @Override
  public boolean newState(final int[] decoded)
    throws OverflowException
  {
    if (mMarkingInfo == null) {
      final EventEncoding enc = mSynchronousProductBuilder.getEventEncoding();
      final EventProxy prop = enc.getProposition
        (AbstractTRCompositionalAnalyzer.PRECONDITION_MARKING);
      mMarkingInfo = mSynchronousProductBuilder.createMarkingInfo(prop);
    }
    if (mSynchronousProductBuilder.isMarked(mMarkingInfo, decoded)) {
      mCount += mAlphaWeight;
    } else {
      mCount += mNonAlphaWeight;
    }
    if (mCount >= mCurrentMinimum) {
      throw new OverflowException(OverflowKind.NODE, mCurrentMinimum);
    }
    return true;
  }


  //#########################################################################
  //# Data Members
  private final int mAlphaWeight = 1;
  private final int mNonAlphaWeight = 0;
  private AbstractTRCompositionalAnalyzer mAnalyzer;
  private TRSynchronousProductBuilder mSynchronousProductBuilder;
  private List<TRSynchronousProductBuilder.MarkingInfo> mMarkingInfo;
  private int mCurrentMinimum = Integer.MAX_VALUE;
  private int mCount = 0;

}
