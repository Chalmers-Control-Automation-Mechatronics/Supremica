//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRPreselectionHeuristicMaxS
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * <P>The <STRONG>MaxS</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MaxS</STRONG> preselection identifies the automaton with
 * the most reachable states and forms pairs consisting of this automaton
 * every other automaton in the model with which it shares at least one
 * event.</P>
 *
 * @author Robi Malik
 */

class TRPreselectionHeuristicMaxS
  extends TRPreselectionHeuristicPairsConstrained
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.trcomp.TRPreselectionHeuristicPairsConstrained
  @Override
  double computeHeuristicValue(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    final int dumpIndex = rel.getDumpStateIndex();
    int numStates = rel.getNumberOfReachableStates();
    if (rel.isReachable(dumpIndex)) {
      numStates--;
    }
    return -numStates;
  }

}
