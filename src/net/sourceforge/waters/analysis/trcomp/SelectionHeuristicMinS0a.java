//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinS0a
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP><SUB>0</SUB> candidate
 * selection heuristic for compositional model analysers of type {@link
 * AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP> heuristic is of interest
 * when verifying the generalised nonblocking property. It estimates the
 * number of states of the abstracted synchronous composition of candidates
 * and chooses the candidate with the smallest estimate. The estimate is
 * obtained by multiplying the product of the numbers of reachable
 * precondition-marked states of the candidate's automata (excluding dump
 * states).</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinS0a
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
      AbstractTRCompositionalAnalyzer.SEL_MinS,
      AbstractTRCompositionalAnalyzer.SEL_MaxL,
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinE
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final int alpha = AbstractTRCompositionalAnalyzer.PRECONDITION_MARKING;
    double result = 1.0;
    for (final TRAutomatonProxy aut : candidate.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      int numStates;
      if (rel.isPropositionUsed(alpha)) {
        numStates = rel.getNumberOfMarkings(alpha, false);
      } else {
        numStates = rel.getNumberOfReachableStates();
      }
      final int dump = rel.getDumpStateIndex();
      if (rel.isReachable(dump) && rel.isMarked(dump, alpha)) {
        numStates--;
      }
      result *= numStates;

    }
    return result;
  }

}
