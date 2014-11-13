//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinS
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * @author Robi Malik
 */

public class SelectionHeuristicMinS
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  protected double getHeuristicValue(final TRCandidate candidate)
  {
    double numStates = 1.0;
    for (final TRAutomatonProxy aut : candidate.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      numStates *= rel.getNumberOfReachableStates();
    }
    int numEvents = 0;
    int numSharedEvents = 0;
    final EventEncoding enc = candidate.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < enc.getNumberOfProperEvents(); e++) {
      final byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        numEvents++;
        if (!EventStatus.isLocalEvent(status)) {
          numSharedEvents++;
        }
      }
    }
    if (numEvents == 0) {
      return 1.0;
    } else {
      return numStates * numSharedEvents / numEvents;
    }
  }

}
