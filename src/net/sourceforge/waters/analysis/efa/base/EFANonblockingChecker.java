//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFANonblockingChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.base;

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFANonblockingChecker extends AbstractEFAAlgorithm
{

  //#########################################################################
  //# Invocation
  public boolean run(final AbstractEFATransitionRelation<?> tr)
    throws AnalysisAbortException
  {
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    if (rel.getNumberOfPropositions() == 0) {
      return true;
    }
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_PREDECESSORS);
    final TIntStack stack = new TIntArrayStack();
    final int numStates = rel.getNumberOfStates();
    final boolean[] coReachable = new boolean[numStates];
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    for (int s=0; s < numStates; s++) {
      if (rel.isMarked(s, 0) && !coReachable[s]) {
        coReachable[s] = true;
        stack.push(s);
        while (stack.size() > 0) {
          final int target = stack.pop();
          iter.resetState(target);
          while (iter.advance()) {
            final int source = iter.getCurrentSourceState();
            if (!coReachable[source]) {
              coReachable[source] = true;
              stack.push(source);
            }
            checkAbort();
          }
        }
      }
    }
    for (int s=0; s < numStates; s++) {
      if (!coReachable[s]) {
        return false;
      }
    }
    return true;
  }

}
