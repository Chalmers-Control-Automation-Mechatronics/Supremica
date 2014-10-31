//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SynthesisTransitionRemovalTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.DFSIntSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.OrderingInfo;
import net.sourceforge.waters.analysis.tr.TauClosure;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier to remove synthesis
 * redundant transitions.</P>
 *
 * This implementation assumes that the automaton is tau-uncontrollable
 * loop-free.
 *
 * <P><I>Reference.</I><BR>
 * S. Mohajerani, R. Malik and M. Fabian. Transition removal for compositional
 *  supervisor synthesis. CASE 2012</P>
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class SynthesisTransitionRemovalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  public SynthesisTransitionRemovalTRSimplifier()
  {
  }

  SynthesisTransitionRemovalTRSimplifier(final ListBufferTransitionRelation rel)
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
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

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
    final OrderingInfo info = rel.getOrderingInfo();
    final int firstLocalUnont = info.getFirstEventIndex
      (EventStatus.STATUS_LOCAL, ~EventStatus.STATUS_CONTROLLABLE);
    final int lastLocalUncont = info.getLastEventIndex
      (EventStatus.STATUS_LOCAL, ~EventStatus.STATUS_CONTROLLABLE);
    final TauClosure tauClosure = rel.createSuccessorsTauClosure
      (firstLocalUnont, lastLocalUncont, mTransitionLimit);

    final TransitionIterator iterCandidate =
      rel.createAllTransitionsModifyingIterator();
    final TransitionIterator iterFrom = rel.createSuccessorsReadOnlyIteratorByStatus
      (EventStatus.STATUS_LOCAL, ~EventStatus.STATUS_CONTROLLABLE);
    final TransitionIterator iterEvent = rel.createAnyReadOnlyIterator();
    final TransitionIterator iterUncontrollableTo = tauClosure.createIterator();
    mIteratorUncontrollableClosure =
      tauClosure.createFullEventClosureIterator();
    mIteratorControllableTo = rel.createSuccessorsReadOnlyIteratorByStatus
      (EventStatus.STATUS_LOCAL, EventStatus.STATUS_CONTROLLABLE);
    mIteratorUncontrollableTo = rel.createSuccessorsReadOnlyIteratorByStatus
      (~EventStatus.STATUS_CONTROLLABLE);
    boolean removedSome = false;
    trans:
    while (iterCandidate.advance()) {
      final DFSIntSearchSpace searchSpace = new DFSIntSearchSpace();
      checkAbort();
      final int e = iterCandidate.getCurrentEvent();
      final byte status = rel.getProperEventStatus(e);
      final boolean selflooped =
        (status & EventStatus.STATUS_OUTSIDE_ONLY_SELFLOOP) != 0 &&
        mUsingSpecialEvents && e != EventEncoding.TAU;
      final boolean controllable = EventStatus.isControllableEvent(status);
      final boolean local = EventStatus.isLocalEvent(status);
      final int from0 = iterCandidate.getCurrentFromState();
      final int to0 = iterCandidate.getCurrentToState();
      searchSpace.offer(from0);
      while (searchSpace.size() > 0) {
        final int p1 = searchSpace.poll();
        if (selflooped && p1 == to0) {
          iterCandidate.remove();
          removedSome = true;
          continue trans;
        }
        if (local) {
          if (!controllable) {
            if (p1 == to0) {
              iterCandidate.remove();
              removedSome = true;
              continue trans;
            }
          } else {
            if (isWeaklyControllablePath(from0, p1, to0)) {
              iterCandidate.remove();
              removedSome = true;
              continue trans;
            }
          }
        } else { // shared
          iterEvent.reset(p1, e);
          while (iterEvent.advance()) {
            final int p2 = iterEvent.getCurrentToState();
            if (p1 != from0 || p2 != to0) {
              if (!controllable) {
                iterUncontrollableTo.resetState(p2);
                while (iterUncontrollableTo.advance()) {
                  final int p3 = iterUncontrollableTo.getCurrentToState();
                  if (p3 == to0) {
                    iterCandidate.remove();
                    removedSome = true;
                    continue trans;
                  }
                }
              } else { // controllable
                if (isWeaklyControllablePath(-1, p2, to0)) {
                  iterCandidate.remove();
                  removedSome = true;
                  continue trans;
                }
              }
            }
          }
        }
        iterFrom.resetState(p1);
        while (iterFrom.advance()) {
          final int target = iterFrom.getCurrentTargetState();
          // for tau uncontrollable. check to see if this is the transition we are trying to delete.
          if (p1 != from0 || target != to0 || !local || controllable) {
            searchSpace.offer(target);
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
  //# Auxiliary Methods
  private boolean isWeaklyControllablePath(final int source0, final int source, final int target0)
  {

    if (source == target0) {
      return true;
    }
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final DFSIntSearchSpace searchSpace = new DFSIntSearchSpace();
    searchSpace.offer(source);
    outer:
    while (searchSpace.size() > 0) {
      final int current = searchSpace.poll();
      mIteratorUncontrollableTo.resetState(current);
      boolean foundEnd = false;
      int next = -1;
      inner:
      while (mIteratorUncontrollableTo.advance()) {
        final int target = mIteratorUncontrollableTo.getCurrentTargetState();
        final int event = mIteratorUncontrollableTo.getCurrentEvent();
        final byte status = rel.getProperEventStatus(event);
        if (EventStatus.isLocalEvent(status)) {
          if (target == current) {
            // nothing
          } else if (target == target0) {
            foundEnd = true;
          } else if (target == next) {
            // nothing
          } else if (next < 0) {
            next = target;
          } else {
            continue outer;
          }
        } else {
          mIteratorUncontrollableClosure.reset(target0, event);
          while (mIteratorUncontrollableClosure.advance()) {
            final int targetFrom0 =
              mIteratorUncontrollableClosure.getCurrentTargetState();
            if (targetFrom0 == target) {
              continue inner;
            }
          }
          continue outer;
        }
      }
      if (next >= 0) {
        searchSpace.offer(next);
      } else if (foundEnd) {
        return true;
      } else {
        mIteratorControllableTo.resetState(current);
        while (mIteratorControllableTo.advance()) {
          final int target = mIteratorControllableTo.getCurrentTargetState();
          // check to see if this is the transition we are trying to delete.
          if (source0 != current || target != target0) {
            if (target == target0) return true;
            searchSpace.offer(target);
          }
        }
      }
    }
    return false;
  }


  //#########################################################################
  //# Data Members
  private int mTransitionLimit = Integer.MAX_VALUE;
  private boolean mUsingSpecialEvents = true;
  private TransitionIterator mIteratorControllableTo;
  private TransitionIterator mIteratorUncontrollableTo;
  private TransitionIterator mIteratorUncontrollableClosure;

}

