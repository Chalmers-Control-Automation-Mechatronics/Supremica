//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OnlySilentOutgoingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntArrayList;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Only Silent Outgoing Rule</I>.</P>
 *
 * <P>The <I>Only Silent Outgoing Rule</I> removes all states&nbsp;<I>x</I>
 * that do not have the alpha or default marking, if they only have outgoing
 * tau transitions. The incoming transitions to&nbsp;<I>x</I> are redirected
 * to all the (tau) successor states of&nbsp;<I>x</I>.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class OnlySilentOutgoingTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public OnlySilentOutgoingTRSimplifier()
  {
  }

  public OnlySilentOutgoingTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_ALL;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.op.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
  throws AnalysisException
  {
    final int alphaID = getPreconditionMarkingID();
    final int omegaID = getDefaultMarkingID();
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if (!rel.isUsedEvent(tauID)) {
      return false;
    }
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final TIntArrayList targets = new TIntArrayList();
    final int numStates = rel.getNumberOfStates();
    boolean modified = false;

    main:
    for (int source = 0; source < numStates; source++) {
      if (rel.isReachable(source) &&
          !rel.isMarked(source, omegaID) &&
          (alphaID < 0 || !rel.isMarked(source, alphaID))) {
        checkAbort();
        iter.resetState(source);
        while (iter.advance()) {
          final int eventID = iter.getCurrentEvent();
          if (eventID == tauID) {
            final int target = iter.getCurrentTargetState();
            targets.add(target);
          } else {
            targets.clear();
            continue main;
          }
        }
        if (!targets.isEmpty()) {
          rel.removeOutgoingTransitions(source, tauID);
          for (int i = 0; i < targets.size(); i++) {
            final int target = targets.get(i);
            rel.copyIncomingTransitions(source, target);
          }
          rel.removeIncomingTransitions(source);
          rel.setReachable(source, false);
          modified = true;
          targets.clear();
        }
      }
    }
    applyResultPartitionAutomatically();
    return modified;
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.removeTauSelfLoops();
    rel.removeProperSelfLoopEvents();
  }

}
