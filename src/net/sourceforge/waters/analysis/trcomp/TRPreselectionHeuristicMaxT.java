//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRPreselectionHeuristicMaxT
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * <P>The <STRONG>MaxT</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MaxT</STRONG> preselection identifies the automaton with
 * the most transitions and forms pairs consisting of this automaton
 * every other automaton in the model with which it shares at least one
 * event.</P>

 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

class TRPreselectionHeuristicMaxT
  extends TRPreselectionHeuristicPairsConstrained
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.trcomp.TRPreselectionHeuristicPairsConstrained
  @Override
  double computeHeuristicValue(final TRAutomatonProxy aut)
  {
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    return -rel.getNumberOfTransitions();
  }

}
