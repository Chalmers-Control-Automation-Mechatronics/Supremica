//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The <STRONG>MaxC</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalModelAnalyzer}.</P>
 *
 * <P>The <STRONG>MaxC</STRONG> selection heuristic gives preference to
 * candidate with the highest proportion of common events (i.e., events shared
 * by all automata of the candidate) over its total number of events
 * (excluding {@link EventEncoding#TAU}).</P>
 *
 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMaxC
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public SelectionHeuristic<TRCandidate> createDecisiveHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<TRCandidate>[] chain = new SelectionHeuristic[] {
      this,
      AbstractTRCompositionalModelAnalyzer.SEL_MaxL,
      AbstractTRCompositionalModelAnalyzer.SEL_MinE,
      AbstractTRCompositionalModelAnalyzer.SEL_MinS
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final EventEncoding enc = candidate.getEventEncoding();
    int numCommon = 0;
    int numEvents = 0;
    for (final EventProxy event : enc.getUsedEvents()) {
      numEvents++;
      numCommon++;
      for (final TRAutomatonProxy aut : candidate.getAutomata()) {
        final EventEncoding autEnc = aut.getEventEncoding();
        final int e = autEnc.getEventCode(event);
        if (e < 0) {
          numCommon--;
        } else {
          final byte status = autEnc.getProperEventStatus(e);
          if (!EventStatus.isUsedEvent(status)) {
            numCommon--;
          }
        }
      }
    }
    if (numEvents == 0) {
      return -1.0;
    } else {
      return - (double) numCommon / numEvents;
    }
  }

}
