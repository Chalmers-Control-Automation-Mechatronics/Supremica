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

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.monolithic.TRAbstractSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;


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
      mSynchronousProductBuilder =
        mAnalyzer.createSynchronousProductBuilder();
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

  TRAbstractSynchronousProductBuilder getSynchronousProductBuilder()
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
  private TRAbstractSynchronousProductBuilder mSynchronousProductBuilder;

}
