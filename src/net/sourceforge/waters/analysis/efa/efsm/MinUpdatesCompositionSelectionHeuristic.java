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

import java.util.List;
import java.util.Set;

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
  extends CompositionSelectionHeuristic
{

  //#########################################################################
  //# Invocation
  @Override
  public double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    final Set<ConstraintList> updates = new THashSet<ConstraintList>();
    for (final EFSMTransitionRelation efsmTR : candidate) {
      final EFSMEventEncoding efsmEncoding = efsmTR.getEventEncoding();
      for (int e = EventEncoding.NONTAU; e < efsmEncoding.size(); e++) {
        updates.add(efsmEncoding.getUpdate(e));
      }
    }
    return updates.size();
  }

}
