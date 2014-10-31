//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ObserverProjectionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;

/**
 * <P>An implementation of the natural observer algorithm.</P>
 *
 * <P>This simplifier determines a coarsest causal reporter map satisfying the
 * observer property [1] for the input automaton and set of observable events,
 * and repartitions accordingly. However, if the natural projection is not an
 * observer, i.e., the quotient automaton has transitions with unobservable
 * events ({@link EventEncoding#TAU}) and/or it is nondeterministic w.r.t.
 * observable events, the polynomial-time extention algorithm in [2] is used to
 * resolve it. While the algorithm does not guarantee to find a minimal set of
 * observable events, it is in practice often reasonably small.
 * </P>
 * <I>Reference:</I>
 * <p/>
 * [1] K. C. Wong and W. M. Wonham, On the Computation of Observers in
 * Discrete-Event Systems, Discrete Event Dynamic Systems, vol. 14, no. 1, pp.
 * 55�107, 2004.
 * <p/>
 * [2] L. Feng and W. M. Wonham, On the Computation of Natural Observers in
 * Discrete-Event Systems, Discrete Event Dynamic Systems, vol. 20, no. 1, pp.
 * 63�102, 2008.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class NaturalObserverTRSimplifier
 extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructor
  /**
   * The constructor of the the natural observer simplifier.
   * <p/>
   * @param rel       The original transition relation
   * @param obsEvents Set of observable events. This set will be modified
   *                  iteratively s.t. the corresponding natural projection
   *                  becomes an observer. The end result can be retrieved by
   *                  {@link #getObservableEvents()} method.
   * <p/>
   * @throws OverflowException
   */
  public NaturalObserverTRSimplifier(final ListBufferTransitionRelation rel,
                                     final int[] obsEvents)
   throws OverflowException
  {
    super(null);
    mRel = rel;
    mRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mObsEvents = new TIntHashSet(obsEvents);
    mUnObsEvents = getUnObsEvents();
    final ListBufferTransitionRelation prjRel = project();
    super.setTransitionRelation(prjRel);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence(
     ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
    mBisimulator.setAppliesPartitionAutomatically(false);
    mBisimulator.setStatistics(null);
  }

  /**
   * The constructor of the the natural observer simplifier. In this, the local
   * events (i.e. events with local status {@link EventStatus#STATUS_LOCAL})
   * are considered as unobservable events hence will be projected.
   * <p/>
   * @param rel The original transition relation where the local events are
   *            considered as unobservable events hence will be projected.
   * <p/>
   * @throws OverflowException
   */
  public NaturalObserverTRSimplifier(final ListBufferTransitionRelation rel)
   throws OverflowException
  {
    super(null);
    mRel = rel;
    mRel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    mObsEvents = new TIntHashSet();
    mUnObsEvents = new TIntHashSet();
    findLocalEvents();
    final ListBufferTransitionRelation prjRel = project();
    super.setTransitionRelation(prjRel);
    mBisimulator = new ObservationEquivalenceTRSimplifier();
    mBisimulator.setEquivalence(
     ObservationEquivalenceTRSimplifier.Equivalence.OBSERVATION_EQUIVALENCE);
    mBisimulator.setAppliesPartitionAutomatically(false);
    mBisimulator.setStatistics(null);
  }

  //#########################################################################
  //# Configuration
  /**
   * Gets the the set of unobservable events.
   */
  public TIntHashSet getUnObservableEvents()
  {
    return mUnObsEvents;
  }

  /**
   * Gets the the set of observable events. If this method is called after
   * {@link NaturalObserverTRSimplifier#run()}, it returns the set of observable
   * events s.t. the natural projection is an observer.
   */
  public TIntHashSet getObservableEvents()
  {
    return mObsEvents;
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   * <p/>
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public void setTransitionRemovalMode(
   final ObservationEquivalenceTRSimplifier.TransitionRemoval mode)
  {
    mBisimulator.setTransitionRemovalMode(mode);
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * <p/>
   * @see ObservationEquivalenceTRSimplifier.TransitionRemoval
   */
  public ObservationEquivalenceTRSimplifier.TransitionRemoval getTransitionRemovalMode()
  {
    return mBisimulator.getTransitionRemovalMode();
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * <p/>
   * @param limit The new transition limit, or {@link Integer#MAX_VALUE} to
   *              allow an unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mBisimulator.setTransitionLimit(limit);
  }

  /**
   * Gets the transition limit.
   * <p/>
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mBisimulator.getTransitionLimit();
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
  public boolean isPartitioning()
  {
    return true;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return true;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
     new TRSimplifierStatistics(this, true, true);
    return setStatistics(stats);
  }

  @Override
  public void reset()
  {
    mBisimulator.reset();
    super.reset();
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  protected boolean runSimplifier()
   throws AnalysisException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numTransBefore = rel.getNumberOfTransitions();
    mBisimulator.setTransitionRelation(rel);
    while (true) {
      checkAbort();
      final boolean modified = mBisimulator.run();
      if (!modified && rel.getNumberOfTransitions() == numTransBefore) {
        return false;
      }
      mPartition = mBisimulator.getResultPartition();
      if (mPartition == null) {
        break;
      } else if (!extendEvent()) {
        break;
      }
      final ListBufferTransitionRelation prjRel = project();
      prjRel.reconfigure(mBisimulator.getPreferredInputConfiguration());
      super.setTransitionRelation(prjRel);
      mBisimulator.setTransitionRelation(prjRel);
    }
    setResultPartition(mPartition);
    return true;
  }

  //#########################################################################
  //# Auxiliary Methods
  private boolean extendEvent()
  {
    boolean modified = false;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    TransitionIterator iter;
    // State to partition map
    final int[] pmap = mPartition.getStateToClass();
    // Enlargement from the unobservable events in the quotient automaton
    final TIntHashSet B = new TIntHashSet();
    iter = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (int source = 0; source < rel.getNumberOfStates(); source++) {
      if (rel.isReachable(source)) {
        final int sourceClass = pmap[source];
        iter.reset(source, EventEncoding.TAU);
        // If there is an outgoing transition with tau event
        while (iter.advance()) {
          final int target = iter.getCurrentTargetState();
          final int targetClass = pmap[target];
          // If it is not a selfloop
          if (sourceClass != targetClass) {
            // Finding the correponding unobservable event on the 
            // original transition relation
            mIter.resetState(source);
            while (mIter.advance()) {
              if (mIter.getCurrentTargetState() == target) {
                B.add(mIter.getCurrentEvent());
              }
            }
          }
        }
      }
    }
    if (!B.isEmpty()) {
      modified = true;
    }
    // Creating the quotient automaton
    rel.merge(mPartition);
    /**
     * First, check if the quotient automaton is nondeterministic, i.e., cosets
     * with nondeterministic observable outgoing transitions. N is a mapping
     * from a quotient state to set of observable events that lead to more than
     * one quotient state.
     */
    final TIntObjectHashMap<TIntHashSet> N =
     new TIntObjectHashMap<>(rel.getNumberOfStates());
    iter = rel.createSuccessorsReadOnlyIterator();
    for (final int event : mObsEvents.toArray()) {
      for (int source = 0; source < rel.getNumberOfStates(); source++) {
        if (rel.isReachable(source)) {
          int targets = 1;
          iter.reset(source, event);
          while (iter.advance()) {
            // If form the source state there are more than one outgoing 
            // transitions labeled by this event
            if (targets++ > 1) {
              final TIntHashSet key = N.get(source);
              if (key != null) {
                key.add(event);
              } else {
                final TIntHashSet ndEvents = new TIntHashSet();
                ndEvents.add(event);
                N.put(source, ndEvents);
              }
            }
          }
        }
      }
    }
    // If it is nondeterministic then fix it by making hidden (unobservable) 
    // events in the cosets to observable
    if (!N.isEmpty()) {
      modified = true;
      // The set of unobservable events in the cosets
      final TIntHashSet H = new TIntHashSet();
      for (final int coset : N.keys()) {
        final TIntHashSet hiddenEvents = getHiddenEvents(coset);
        H.addAll(hiddenEvents);
      }
      // Add all events in B and H (B union H) to the set of observable events
      final TIntHashSet BH = setUnion(B, H);
      addAllObsEvent(BH);
      // The set of events which are in H but not in B
      final TIntHashSet H_B = setMinus(H, B);

      // Check each event
      for (final int e : H_B.toArray()) {
        addUnObsEvent(e);
        for (final int coset : N.keys()) {
          if (!split(coset, N.get(coset))) {
            addObsEvent(e);
            break;
          }
        }
      }
    } else {
      addAllObsEvent(B);
    }
    return modified;
  }

  private TIntHashSet getHiddenEvents(final int pNbr)
  {
    final TIntHashSet hiddenEvents = new TIntHashSet();
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (final int state : mPartition.getStates(pNbr)) {
      mIter.resetState(state);
      while (mIter.advance()) {
        if (mUnObsEvents.contains(mIter.getCurrentEvent())) {
          hiddenEvents.add(mIter.getCurrentEvent());
        }
      }
    }
    return hiddenEvents;
  }

  private boolean split(final int y, final TIntHashSet ndEvents)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    for (final int ndEvent : ndEvents.toArray()) {
      final TIntArrayList ys = new TIntArrayList();
      for (int source = 0; source < rel.getNumberOfStates(); source++) {
        if (rel.isReachable(source)) {
          iter.reset(source, ndEvent);
          while (iter.advance()) {
            ys.add(iter.getCurrentTargetState());
          }
        }
      }
      if (!nopath(y, ndEvent, ys)) {
        return false;
      }
    }
    return true;
  }

  private boolean nopath(final int y, final int ndEvent,
                         final TIntArrayList ys)
  {
    final TIntObjectHashMap<TIntHashSet> yMap = new TIntObjectHashMap<>();
    for (final int ysCode : ys.toArray()) {
      yMap.put(ysCode, new TIntHashSet());
    }
    final int[] pmap = mPartition.getStateToClass();
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (final int yNode : mPartition.getStates(y)) {
      mIter.reset(yNode, ndEvent);
      while (mIter.advance()) {
        final int st = mIter.getCurrentTargetState();
        yMap.get(pmap[st]).add(yNode);
      }
    }

    final ArrayList<TIntHashSet> Es = new ArrayList<>(yMap.valueCollection());
    TIntHashSet Ei, Ej;
    for (int i = 0; i < Es.size(); i++) {
      for (int j = i + 1; j < Es.size(); j++) {
        Ei = findEquivalentStates(Es.get(i));
        Ej = Es.get(j);

        if (!setIntersection(Ei, Ej).isEmpty()) {
          return false;
        }
        Ei = Es.get(i);
        Ej = findEquivalentStates(Es.get(j));
        if (!setIntersection(Ei, Ej).isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }

  private TIntHashSet findEquivalentStates(final TIntHashSet states)
  {
    final TIntHashSet eqStates = new TIntHashSet();
    for (final int state : states.toArray()) {
      final TIntHashSet eqsts = findEquivalentStates(state);
      eqStates.addAll(eqsts.toArray());
    }
    return eqStates;
  }

  private TIntHashSet findEquivalentStates(final int state)
  {
    final TIntHashSet eqStates = new TIntHashSet();
    final TIntStack stk = new TIntArrayStack();
    eqStates.add(state);
    stk.push(state);
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    while (stk.size() > 0) {
      final int currstate = stk.pop();
      for (final int e : mUnObsEvents.toArray()) {
        mIter.reset(currstate, e);
        while (mIter.advance()) {
          final int st = mIter.getCurrentTargetState();
          if (eqStates.add(st)) {
            stk.push(st);
          }
        }
      }
    }
    return eqStates;
  }

  private TIntHashSet getUnObsEvents()
  {
    final TIntHashSet unObsEvents = new TIntHashSet();
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (int source = 0; source < mRel.getNumberOfStates(); source++) {
      if (mRel.isReachable(source)) {
        mIter.resetState(source);
        while (mIter.advance()) {
          final int event = mIter.getCurrentEvent();
          if (!mObsEvents.contains(event)) {
            unObsEvents.add(event);
          }
        }
      }
    }
    return unObsEvents;
  }

  private void findLocalEvents()
  {
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (int source = 0; source < mRel.getNumberOfStates(); source++) {
      if (mRel.isReachable(source)) {
        mIter.resetState(source);
        while (mIter.advance()) {
          final int event = mIter.getCurrentEvent();
          if (mRel.getProperEventStatus(event) != EventStatus.STATUS_LOCAL) {
            mObsEvents.add(event);
          } else {
            mUnObsEvents.add(event);
          }
        }
      }
    }
  }

  private ListBufferTransitionRelation project()
   throws OverflowException
  {
    final ListBufferTransitionRelation rel =
     new ListBufferTransitionRelation(mRel.getName(), mRel.getKind(), mRel
     .getNumberOfProperEvents(), mRel.getNumberOfPropositions(), mRel
     .getNumberOfStates(), ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    boolean init = false;
    final TransitionIterator mIter = mRel.createSuccessorsReadOnlyIterator();
    for (int source = 0; source < mRel.getNumberOfStates(); source++) {
      if (mRel.isReachable(source)) {
        if (!init && mRel.isInitial(source)) {
          rel.setInitial(source, true);
          init = true;
        }
        rel.setAllMarkings(source, mRel.getAllMarkings(source));
        mIter.resetState(source);
        while (mIter.advance()) {
          rel.addTransition(source, mIter.getCurrentEvent(),
                            mIter.getCurrentTargetState());
        }
      }
    }
    final TransitionIterator iter = rel.createSuccessorsModifyingIterator();
    for (int source = 0; source < rel.getNumberOfStates(); source++) {
      if (rel.isReachable(source)) {
        iter.resetState(source);
        final TIntArrayList targets = new TIntArrayList();
        while (iter.advance()) {
          final int event = iter.getCurrentEvent();
          if (!mObsEvents.contains(event)) {
            targets.add(iter.getCurrentTargetState());
            iter.remove();
          }
        }
        if (!targets.isEmpty()) {
          rel.addTransitions(source, EventEncoding.TAU, targets);
        }
      }
    }
    return rel;
  }

  private void addObsEvent(final int event)
  {
    mObsEvents.add(event);
    mUnObsEvents.remove(event);
  }

  private void addUnObsEvent(final int event)
  {
    mUnObsEvents.add(event);
    mObsEvents.remove(event);
  }

  private void addAllObsEvent(final TIntHashSet events)
  {
    mObsEvents.addAll(events.toArray());
    mUnObsEvents.removeAll(events.toArray());
  }

  private TIntHashSet setUnion(final TIntHashSet x, final TIntHashSet y)
  {
    final TIntHashSet result = new TIntHashSet();
    result.addAll(x.toArray());
    result.addAll(y.toArray());
    return result;
  }

  private TIntHashSet setIntersection(final TIntHashSet x, final TIntHashSet y)
  {
    if (x == null || y == null || x.isEmpty() || y.isEmpty()) {
      return new TIntHashSet();
    }

    final TIntHashSet result = new TIntHashSet(x.toArray());
    result.retainAll(y.toArray());
    return result;
  }

  private TIntHashSet setMinus(final TIntHashSet x, final TIntHashSet y)
  {
    if (x == null || x.isEmpty()) {
      return new TIntHashSet();
    } else if (y == null || y.isEmpty()) {
      return new TIntHashSet(x.toArray());
    }

    final TIntHashSet result = new TIntHashSet();
    for (final int n : x.toArray()) {
      if (!y.contains(n)) {
        result.add(n);
      }
    }
    return result;
  }

  //#########################################################################
  //# Data Members
  private final ObservationEquivalenceTRSimplifier mBisimulator;
  private final ListBufferTransitionRelation mRel;
  private TIntHashSet mUnObsEvents = null;
  private TIntHashSet mObsEvents = null;
  private TRPartition mPartition = null;
}
