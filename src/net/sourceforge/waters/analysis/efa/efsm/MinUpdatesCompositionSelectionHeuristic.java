//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinUpdatesCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * The &quot;minimum updates&quot; composition selection
 * heuristic for EFSMs. This heuristic gives preference to composition
 * candidates with the smallest possible number of distinct updates appearing
 * in the EFSMs to be composed.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class MinUpdatesCompositionSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMPair>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMPair candidate)
  {
    final Set<ConstraintList> updates = new THashSet<ConstraintList>();
    final EFSMEventEncoding encoding1 =
      candidate.getFirst().getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < encoding1.size(); e++) {
      updates.add(encoding1.getUpdate(e));
    }
    final EFSMEventEncoding encoding2 =
      candidate.getSecond().getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < encoding2.size(); e++) {
      updates.add(encoding2.getUpdate(e));
    }
    return updates.size();
  }

}
