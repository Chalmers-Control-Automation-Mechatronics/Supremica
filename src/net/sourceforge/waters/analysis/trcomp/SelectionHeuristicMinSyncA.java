//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.CompositionalAnalysisResult;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRAbstractSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.Logger;


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
  extends SelectionHeuristicMinSync0
  implements TRSynchronousProductBuilder.StateCallback
{

  //#########################################################################
  //# Overrides for NumericSelectionHeuristic<TRCandidate>
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
    if (isOverflowCandidate(candidate)) {
      return Double.POSITIVE_INFINITY;
    }
    final AbstractTRCompositionalAnalyzer analyzer = getAnalyzer();
    final TRAbstractSynchronousProductBuilder syncBuilder =
      getSynchronousProductBuilder();
    try {
      mCount = 0;
      final ProductDESProxyFactory factory = analyzer.getFactory();
      final ProductDESProxy des = candidate.createProductDESProxy(factory);
      final EventEncoding syncEncoding =
        candidate.createSyncEventEncoding();
      syncBuilder.setModel(des);
      syncBuilder.setEventEncoding(syncEncoding);
      syncBuilder.run();
      if (mCount < mCurrentMinimum) {
        mCurrentMinimum = mCount;
      }
      return mCount;
    } catch (final OutOfMemoryError error) {
      final Logger logger = analyzer.getLogger();
      logger.debug("<out of memory>");
      addOverflowCandidate(candidate);
      return Double.POSITIVE_INFINITY;
    } catch (final OverflowException overflow) {
      if (mCurrentMinimum == Integer.MAX_VALUE) {
        addOverflowCandidate(candidate);
      }
      return Double.POSITIVE_INFINITY;
    } catch (final AnalysisException exception) {
      throw exception.getRuntimeException();
    } finally {
      mMarkingInfo = null;
      final CompositionalAnalysisResult stats = analyzer.getAnalysisResult();
      final SynchronousProductResult result = syncBuilder.getAnalysisResult();
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
    final TRAbstractSynchronousProductBuilder syncBuilder =
      getSynchronousProductBuilder();
    if (mMarkingInfo == null) {
      final EventEncoding enc = syncBuilder.getEventEncoding();
      final EventProxy prop = enc.getProposition
        (AbstractTRCompositionalAnalyzer.PRECONDITION_MARKING);
      mMarkingInfo = syncBuilder.getMarkingInfo(prop);
    }
    if (mMarkingInfo.isMarkedStateDecoded(decoded)) {
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
  private TRSynchronousProductBuilder.MarkingInfo mMarkingInfo;
  private int mCurrentMinimum = Integer.MAX_VALUE;
  private int mCount = 0;

}
