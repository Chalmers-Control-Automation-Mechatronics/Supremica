//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SelfloopSubsumptionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove selfloops that are
 * redundant by conflict equivalence.</P>
 *
 * @author Robi Malik
 */

public class SelfloopSubsumptionTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SelfloopSubsumptionTRSimplifier()
  {
  }

  SelfloopSubsumptionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_PREDECESSORS;
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, true, false);
    return setStatistics(stats);
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numStates = rel.getNumberOfStates();

    final BitSet removable = new BitSet(numStates);
    final TransitionIterator enabledIter =
      rel.createAllTransitionsReadOnlyIteratorByStatus(EventStatus.STATUS_ALWAYS_ENABLED);
    while (enabledIter.advance()) {
      final int s = enabledIter.getCurrentSourceState();
      final int t = enabledIter.getCurrentTargetState();
      if (s != t) {
        removable.set(s);
      }
    }

    final int lastEvent = rel.getNumberOfProperEvents() - 1;
    final TransitionIterator eventIter = rel.createPredecessorsReadOnlyIterator();
    eventIter.resetEvents(EventEncoding.NONTAU, lastEvent);
    final TransitionIterator tauIter = rel.createPredecessorsReadOnlyIterator();
    tauIter.resetEvent(EventEncoding.TAU);
    final TauClosure closure = rel.createPredecessorsTauClosure(0);
    final TransitionIterator closureIter = closure.createFullEventClosureIterator();
    final TIntArrayList selfloopEvents = new TIntArrayList(lastEvent);
    final TIntStack open = new TIntArrayStack();
    boolean removedSome = false;
    main:
    for (int current = removable.nextSetBit(0); current >= 0;
         current = removable.nextSetBit(current + 1)) {
      if (!rel.isInitial(current) && rel.isReachable(current)) {
        selfloopEvents.clear();
        eventIter.resetState(current);
        while (eventIter.advance()) {
          if (eventIter.getCurrentSourceState() == current) {
            selfloopEvents.add(eventIter.getCurrentEvent());
          } else {
            continue main;
          }
        }
        if (!selfloopEvents.isEmpty()) {
          open.push(current);
          while (open.size() > 0) {
            final int t = open.pop();
            tauIter.resetState(t);
            while (tauIter.advance()) {
              final int s = tauIter.getCurrentSourceState();
              closureIter.resetState(s);
              boolean found = true;
              for (int i = 0; i < selfloopEvents.size() && found; i++) {
                final int e = selfloopEvents.get(i);
                closureIter.resetEvent(e);
                found = false;
                while (closureIter.advance()) {
                  if (closureIter.getCurrentSourceState() == s) {
                    found = true;
                    break;
                  }
                }
              }
              if (!found) {
                if (rel.isInitial(s)) {
                  continue main;
                }
                eventIter.resetState(s);
                if (eventIter.advance()) {
                  continue main;
                }
                open.push(s);
              }
            }
          }
          for (int i = 0; i < selfloopEvents.size(); i++) {
            final int e = selfloopEvents.get(i);
            rel.removeTransition(current, e, current);
          }
          removedSome = true;
        }
      }
    }

    return removedSome;
  }

}

