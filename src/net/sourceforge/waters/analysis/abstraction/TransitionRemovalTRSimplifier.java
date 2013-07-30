//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TransitionRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove observation equivalence
 * redundant transitions.</P>
 *
 * <P><I>Reference.</I><BR>
 * Jaana Eloranta. Minimizing the Number of Transitions with Respect to
 * Observation Equivalence. BIT, <STRONG>31</STRONG>(4), 397-419, 1991.</P>
 *
 * @author Robi Malik
 */

public class TransitionRemovalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public TransitionRemovalTRSimplifier()
  {
  }

  TransitionRemovalTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  /**
   * Sets whether special events are to be considered in abstraction.
   * If enabled, events marked as selfloop-only in all other automata
   * will be treated specially. For such events, it is possible to assume
   * implicit selfloops on all states of the automaton being simplified,
   * potentially giving better state reduction.
   */
  public void setUsingSpecialEvents(final boolean enable)
  {
    mUsingSpecialEvents = enable;
  }

  /**
   * Returns whether special events are considered in abstraction.
   * @see #setUsingSpecialEvents(boolean)
   */
  public boolean isUsingSpecialEvents()
  {
    return mUsingSpecialEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public boolean isPartitioning()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int config = rel.getConfiguration();
    final TauClosure tauClosure;
    if ((config & ListBufferTransitionRelation.CONFIG_PREDECESSORS) != 0) {
      tauClosure = rel.createPredecessorsTauClosure(mTransitionLimit);
    } else {
      tauClosure = rel.createSuccessorsTauClosure(mTransitionLimit);
    }

    final int tau = EventEncoding.TAU;
    final TransitionIterator iter0 =
      rel.createAllTransitionsModifyingIterator();
    final TransitionIterator iter1 = tauClosure.createIterator();
    final TransitionIterator iter2 = rel.createAnyReadOnlyIterator();
    final TransitionIterator iter3 = tauClosure.createIterator();
    boolean removedSome = false;
    trans:
    while (iter0.advance()) {
      final int e = iter0.getCurrentEvent();
      final byte status = rel.getProperEventStatus(e);
      if ((status & EventEncoding.STATUS_UNUSED) != 0) {
        continue;
      }
      checkAbort();
      final boolean selflooped =
        (status & EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP) != 0 &&
        mUsingSpecialEvents && e != EventEncoding.TAU;
      final int from0 = iter0.getCurrentFromState();
      final int to0 = iter0.getCurrentToState();
      iter1.resetState(from0);
      while (iter1.advance()) {
        final int p1 = iter1.getCurrentToState();
        if (selflooped && p1 == to0) {
          iter0.remove();
          removedSome = true;
          continue trans;
        }
        iter2.reset(p1, e);
        while (iter2.advance()) {
          final int p2 = iter2.getCurrentToState();
          if (e == tau) {
            if (p1 != from0 && p2 == to0) {
              iter0.remove();
              removedSome = true;
              continue trans;
            }
          } else {
            if (p1 != from0 || p2 != to0) {
              iter3.resetState(p2);
              while (iter3.advance()) {
                final int p3 = iter3.getCurrentToState();
                if (p3 == to0) {
                  iter0.remove();
                  removedSome = true;
                  continue trans;
                }
              }
            }
          }
        }
      }
    }

    return removedSome;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, false, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;
  private boolean mUsingSpecialEvents = true;

}

