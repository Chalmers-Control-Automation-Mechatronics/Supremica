//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OPSearchAutomatonSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntStack;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongIntHashMap;
import gnu.trove.TLongLongHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.NondeterministicDESException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An implementation of the OP-search algorithm by Patr&iacute;cia Pena et.al.
 *
 * @author Robi Malik
 */

public class OPSearchAutomatonSimplifier
  extends AbstractAutomatonBuilder
{

  //#########################################################################
  //# Constructors
  public OPSearchAutomatonSimplifier(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public OPSearchAutomatonSimplifier(final ProductDESProxy model,
                                     final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public OPSearchAutomatonSimplifier(final AutomatonProxy aut,
                                     final Collection<EventProxy> hidden,
                                     final ProductDESProxyFactory factory)
  {
    super(aut, factory);
    mHiddenEvents = new THashSet<EventProxy>(hidden);
  }


  //#########################################################################
  //# Configuration
  /**
   * Specifies the set of silent or unobservable events to be used for
   * simplification.
   */
  public void setHiddenEvents(final Collection<EventProxy> hidden)
  {
    mHiddenEvents = new THashSet<EventProxy>(hidden);
  }

  /**
   * Gets the set of silent or unobservable events to be used for
   * simplification.
   */
  public Collection<EventProxy> getHiddenEvents()
  {
    return mHiddenEvents;
  }

  /**
   * Specifies the set of propositions that distinguish states.
   * If non-null, only the propositions in the given collection will
   * be considered as relevant. Otherwise, if the collection is
   * <CODE>null</CODE> all propositions in the automaton will be
   * considered.
   */
  public void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = new THashSet<EventProxy>(props);
  }

  /**
   * Gets the set of propositions that distinguish states.
   * @see #setPropositions(Collection) setPropositions()
   */
  public Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      // TODO Auto-generated method stub
      final boolean op = mListBuffer.isEmpty(mPredecessorsOfDead);
      if (!op) {
        return false;
      }
      if (hasSilentTransitions()) {
        final AutomatonProxy aut = createOutputAutomaton();
        return setAutomatonResult(aut);
      } else {
        return setAutomatonResult(mInputAutomaton);
      }
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalsyser
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mListBuffer = new IntListBuffer();
    mReadOnlyIterator = mListBuffer.createReadOnlyIterator();
    mAltReadOnlyIterator = mListBuffer.createReadOnlyIterator();
    setUpAutomatonEncoding();
    setUpStronglyConnectedComponents();
    setUpVerifier();
  }

  protected void tearDown()
  {
    super.tearDown();
    mInputAutomaton = null;
    mListBuffer = null;
    mReadOnlyIterator = null;
    mAltReadOnlyIterator = null;
    mEvents = null;
    mEventMap = null;
    mOriginalStates = null;
    mObservableSucessor = null;
    mUnobservableTauSuccessors = null;
    mObservableTauSuccessors = null;
    mTarjan = null;
    mComponentOfState = null;
    mVerifierStatePairs = null;
    mVerifierStateMap = null;
    mVerifierPredecessors = null;
    mBFSIntList1 = null;
    mBFSIntList2 = null;
    mBFSIntVisited = null;
    mBFSLongList1 = null;
    mBFSLongList2 = null;
    mBFSLongVisited = null;
  }


  //#########################################################################
  //# OP-Verifier Algorithm
  private void setUpAutomatonEncoding()
    throws AnalysisException
  {
    mInputAutomaton = getInputAutomaton();

    final Collection<EventProxy> events = mInputAutomaton.getEvents();
    int numEvents = 0;
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        if (mPropositions == null || mPropositions.contains(event)) {
          numEvents++;
        }
      } else {
        if (!mHiddenEvents.contains(event)) {
          numEvents++;
        }
      }
    }
    mEvents = new EventProxy[numEvents];
    mEventMap = new TObjectIntHashMap<EventProxy>(numEvents);
    mUnobservableTau = numEvents++;
    mObservableTau = numEvents++;
    int next = 0;
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.PROPOSITION) {
        if (mPropositions == null || mPropositions.contains(event)) {
          mEvents[next] = event;
          mEventMap.put(event, next);
          next++;
        }
      } else if (!mHiddenEvents.contains(event)) {
        mEvents[next] = event;
        mEventMap.put(event, next);
        next++;
      } else {
        mEventMap.put(event, mUnobservableTau);
      }
    }

    final Collection<StateProxy> states = mInputAutomaton.getStates();
    final int numStates = states.size();
    final TObjectIntHashMap<StateProxy> stateMap =
      new TObjectIntHashMap<StateProxy>(numStates);
    mOriginalStates = new StateProxy[numStates];
    next = 1;
    for (final StateProxy state : states) {
      final int code;
      if (!state.isInitial()) {
        code = next++;
      } else if (mOriginalStates[0] == null) {
        code = 0;
      } else {
        throw new NondeterministicDESException(mInputAutomaton, state);
      }
      mOriginalStates[code] = state;
      stateMap.put(state, code);
    }

    mObservableSucessor = new int[numStates][numEvents];
    for (next = 0; next < numStates; next++) {
      Arrays.fill(mObservableSucessor[next], NO_TRANSITION);
    }
    mUnobservableTauSuccessors = new int[numStates];
    Arrays.fill(mUnobservableTauSuccessors, IntListBuffer.NULL);
    mObservableTauSuccessors = new int[numStates];
    Arrays.fill(mObservableTauSuccessors, IntListBuffer.NULL);
    for (final StateProxy state : states) {
      final Collection<EventProxy> props = state.getPropositions();
      if (!props.isEmpty()) {
        final int stateCode = stateMap.get(state);
        for (final EventProxy prop : props) {
          if (mPropositions == null || mPropositions.contains(prop)) {
            final int propCode = mEventMap.get(prop);
            setUpTransition(mInputAutomaton, state, prop,
                            stateCode, propCode, DUMP_STATE);
          }
        }
      }
    }
    for (final TransitionProxy trans : mInputAutomaton.getTransitions()) {
      final StateProxy source = trans.getSource();
      final int sourceCode = stateMap.get(source);
      final StateProxy target = trans.getTarget();
      final int targetCode = stateMap.get(target);
      final EventProxy event = trans.getEvent();
      final int eventCode = mEventMap.get(event);
      setUpTransition(mInputAutomaton, source, event,
                      sourceCode, eventCode, targetCode);
    }
  }

  private void setUpTransition(final AutomatonProxy aut,
                               final StateProxy source,
                               final EventProxy event,
                               final int sourceCode,
                               final int eventCode,
                               final int targetCode)
    throws NondeterministicDESException
  {
    if (eventCode == mUnobservableTau) {
      if (sourceCode != targetCode) {
        int list = mUnobservableTauSuccessors[sourceCode];
        if (list == IntListBuffer.NULL) {
          mUnobservableTauSuccessors[sourceCode] = list =
            mListBuffer.createList();
        }
        if (!mListBuffer.contains(list, targetCode)) {
          mListBuffer.prepend(list, targetCode);
        }
      }
    } else if (mObservableSucessor[sourceCode][eventCode] == NO_TRANSITION) {
      mObservableSucessor[sourceCode][eventCode] = targetCode;
    } else if (mObservableSucessor[sourceCode][eventCode] != targetCode){
      throw new NondeterministicDESException(aut, source, event);
    }
  }

  private void setUpStronglyConnectedComponents()
  {
    final int numStates = mOriginalStates.length;
    mTarjan = new Tarjan(numStates);
    mComponentOfState = new StronglyConnectedComponent[numStates];
    mTarjan.findStronglyConnectedComponents();
  }

  private void setUpVerifier()
  {
    final int numStates = mOriginalStates.length;
    mVerifierStatePairs = new TLongArrayList(numStates);
    mVerifierStateMap = new TLongIntHashMap(numStates);
    mVerifierPredecessors = new TIntArrayList(numStates);
    mPredecessorsOfDead = mListBuffer.createList();
    for (int s = 0; s < numStates; s++) {
      expandVerifierPairSingleton(s);
    }
    for (int pindex = 0; pindex < mVerifierStatePairs.size(); pindex++) {
      final long pair = mVerifierStatePairs.get(pindex);
      final int pcode = pindex + mOriginalStates.length;
      expandVerifierPairEncoded(pcode, pair);
    }
  }

  private void expandVerifierPairSingleton(final int code)
  {
    final StronglyConnectedComponent comp = mComponentOfState[code];
    if (comp == null) {
      expandVerifierPairTagged(code, code, code);
    } else {
      final int root = comp.getRootIndex();
      expandVerifierPairTagged(root, root, root);
    }
  }

  private void expandVerifierPairEncoded(final int pcode, final long pair)
  {
    final int code1 = (int) (pair & 0xffffffff);
    final int code2 = (int) (pair >> 32);
    expandVerifierPairTagged(pcode, code1, code2);
  }

  private void expandVerifierPairTagged(final int pcode,
                                        final int code1,
                                        final int code2)
  {
    final StronglyConnectedComponent comp1 = mComponentOfState[code1];
    final int tausucc1 = mUnobservableTauSuccessors[code1];
    final boolean entau1 = comp1 == null ?
                           tausucc1 != IntListBuffer.NULL :
                           comp1.isEnabledEvent(mUnobservableTau);
    final StronglyConnectedComponent comp2 = mComponentOfState[code2];
    final int tausucc2 = mUnobservableTauSuccessors[code2];
    final boolean entau2 = comp2 == null ?
                           tausucc2 != IntListBuffer.NULL :
                           comp2.isEnabledEvent(mUnobservableTau);
    // Proper event transitions ...
    for (int e = 0; e < mUnobservableTau; e++) {
      int esucc1 = 0, esucc2 = 0;
      final boolean en1, en2;
      if (comp1 == null) {
        esucc1 = mObservableSucessor[code1][e];
        en1 = esucc1 != NO_TRANSITION;
      } else {
        en1 = comp1.isEnabledEvent(e);
      }
      if (comp2 == null) {
        esucc2 = mObservableSucessor[code2][e];
        en2 = esucc2 != NO_TRANSITION;
      } else {
        en2 = comp2.isEnabledEvent(e);
      }
      if (en1 && en2) {
        if (comp1 == null) {
          if (comp2 == null) {
            enqueueSuccessor(pcode, esucc1, esucc2);
          } else {
            enqueueSuccessors(pcode, e, esucc1, comp2);
          }
        } else {
          if (comp2 == null) {
            enqueueSuccessors(pcode, e, esucc2, comp1);
          } else if (comp1 == comp2) {
            enqueueSuccessors(pcode, e, comp1);
          } else {
            comp1.iterate();
            while (mReadOnlyIterator.advance()) {
              final int member1 = mReadOnlyIterator.getCurrentData();
              final int succ1 = mObservableSucessor[e][member1];
              if (succ1 != NO_TRANSITION) {
                enqueueSuccessors(pcode, e, succ1, comp2);
              }
            }
          }
        }
      } else if (en1 && !entau2 || en2 && !entau1) {
        mListBuffer.prepend(mPredecessorsOfDead, pcode);
      }
    }
    // Silent event transitions ...
    if (entau1) {
      enqueueTauSuccessors(pcode, comp1, code1, code2);
    }
    if (entau2 && code1 != code2) {
      enqueueTauSuccessors(pcode, comp2, code2, code1);
    }
  }

  private void enqueueSuccessors(final int pcode,
                                 final int e,
                                 final StronglyConnectedComponent comp)
  {
    comp.iterate();
    while (mReadOnlyIterator.advance()) {
      final int source1 = mReadOnlyIterator.getCurrentData();
      final int succ1 = mObservableSucessor[source1][e];
      if (succ1 != NO_TRANSITION) {
        mAltReadOnlyIterator.reset(mReadOnlyIterator);
        while (mAltReadOnlyIterator.advance()) {
          final int source2 = mAltReadOnlyIterator.getCurrentData();
          final int succ2 = mObservableSucessor[source2][e];
          if (succ2 != NO_TRANSITION) {
            enqueueSuccessor(pcode, succ1, succ2);
          }
        }
      }
    }
  }

  private void enqueueSuccessors(final int pcode,
                                 final int e,
                                 final int esucc1,
                                 final StronglyConnectedComponent comp2)
  {
    comp2.iterate();
    while (mReadOnlyIterator.advance()) {
      final int member2 = mReadOnlyIterator.getCurrentData();
      final int succ2 = mObservableSucessor[member2][e];
      if (succ2 != NO_TRANSITION) {
        enqueueSuccessor(pcode, esucc1, succ2);
      }
    }
  }

  private void enqueueTauSuccessors(final int pcode,
                                    final StronglyConnectedComponent comp1,
                                    final int code1,
                                    final int code2)
  {
    if (comp1 == null) {
      enqueueTauSuccessors(pcode, code1, code2);
    } else {
      comp1.iterate();
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        enqueueTauSuccessors(pcode, state, code2);
      }
    }
  }

  private void enqueueTauSuccessors(final int pcode,
                                    final int code1,
                                    final int code2)
  {
    final int list1 = mUnobservableTauSuccessors[code1];
    mReadOnlyIterator.reset(list1);
    while (mReadOnlyIterator.advance()) {
      final int succ1 = mReadOnlyIterator.getCurrentData();
      enqueueSuccessor(pcode, succ1, code2);
    }
  }

  private void enqueueSuccessor(final int from, final int to1, final int to2)
  {
    if (to1 != to2) {
      final StronglyConnectedComponent comp1 = mComponentOfState[to1];
      final int root1 = comp1 == null ? to1 : comp1.getRootIndex();
      final StronglyConnectedComponent comp2 = mComponentOfState[to2];
      final int root2 = comp2 == null ? to2 : comp2.getRootIndex();
      if (root1 != root2) {
        final long pair = getPair(root1, root2);
        final int lookup = mVerifierStateMap.get(pair);
        final int pindex;
        if (lookup > 0) {
          pindex = lookup - mOriginalStates.length;
        } else {
          pindex = mVerifierStateMap.size();
          final int pcode = pindex + mOriginalStates.length;
          mVerifierStatePairs.add(pair);
          mVerifierStateMap.put(pair, pcode);
          mVerifierPredecessors.add(IntListBuffer.NULL);
        }
        int list = mVerifierPredecessors.get(pindex);
        if (list == IntListBuffer.NULL) {
          list = mListBuffer.createList();
          mVerifierPredecessors.set(pindex, list);
        }
        mListBuffer.prependUnique(list, from);
      }
    }
  }


  //#########################################################################
  //# OP-Search Algorithm
  @SuppressWarnings("unused")
  private void doOPSearchStep()
  {
    final long trans = findOPSearchTransition();
    final int source = (int) (trans & 0xffffffff);
    final int target = (int) (trans >> 32);
    final int unobsList = mUnobservableTauSuccessors[source];
    mListBuffer.remove(unobsList, target);
    final int obsList = mObservableTauSuccessors[source];
    mListBuffer.prependUnique(obsList, target);
    final StronglyConnectedComponent comp = mComponentOfState[source];
    if (comp == mComponentOfState[target]) {
      mTarjan.split(comp);
    }
    // rebuild verifier ...
  }


  /**
   * @return Found transition encoded as <CODE>source | (target << 32)</CODE>.
   */
  private long findOPSearchTransition()
  {
    if (mBFSIntList1 == null) {
      mBFSIntList1 = new TIntArrayList();
      mBFSIntList2 = new TIntArrayList();
      mBFSIntVisited = new TIntHashSet();
      mBFSLongList1 = new TLongArrayList();
      mBFSLongVisited = new TLongLongHashMap();
    }
    TIntArrayList current = mBFSIntList1;
    TIntArrayList next = mBFSIntList2;
    boolean found = false;
    int state = 0;
    long pair = 0;
    mAltReadOnlyIterator.reset(mPredecessorsOfDead);
    while (mAltReadOnlyIterator.advance()) {
      state = mAltReadOnlyIterator.getCurrentData();
      final int list = mVerifierPredecessors.get(state);
      mReadOnlyIterator.reset(list);
      while (mReadOnlyIterator.advance()) {
        final int pred = mReadOnlyIterator.getCurrentData();
        pair = mVerifierStatePairs.get(pred);
        if ((pair & 0xffffffff) == (pair >> 32)) {
          found = true;
          break;
        } else if (mBFSIntVisited.add(pred)) {
          next.add(pred);
        }
      }
    }
    if (!found) {
      outer:
      while (true) {
        final TIntArrayList tmp = next;
        next = current;
        current = tmp;
        final int len = current.size();
        for (int i = 0; i < len; i++) {
          state = current.get(i);
          final int list = mVerifierPredecessors.get(state);
          mReadOnlyIterator.reset(list);
          while (mReadOnlyIterator.advance()) {
            final int pred = mReadOnlyIterator.getCurrentData();
            pair = mVerifierStatePairs.get(pred);
            if ((pair & 0xffffffff) == (pair >> 32)) {
              break outer;
            } else if (mBFSIntVisited.add(pred)) {
              next.add(pred);
            }
          }
        }
        current.clear();
      }
    }
    current.clear();
    next.clear();
    mBFSIntVisited.clear();
    // found: pair -> state

    // pair is start/start; state is end1/end2
    // We must search the strongly connected component of start
    // to find a link to strongly connected components end1/end2.
    // The search only uses states in start/start or end1/end2.
    // The first step of the shortest path found gets selected.
    int start = (int) (pair & 0xffffffff);
    final int startroot = getRootIndex(start);
    final StronglyConnectedComponent comp = mComponentOfState[start];
    final long targetpair = mVerifierStatePairs.get(state);
    long foundtrans = 0;
    if (comp == null) {
      foundtrans = searchTauSuccessors(startroot, start, start,
                                       targetpair, mBFSLongList1);
    } else {
      comp.iterate();
      while (mReadOnlyIterator.advance() && foundtrans == 0) {
        start = mReadOnlyIterator.getCurrentData();
        foundtrans = searchTauSuccessors(startroot, start, start,
                                         targetpair, mBFSLongList1);
      }
    }
    if (foundtrans == 0) {
      if (mBFSLongList2 == null) {
        mBFSLongList2 = new TLongArrayList();
      }
      TLongArrayList currentpairs = mBFSLongList1;
      TLongArrayList nextpairs = mBFSLongList2;
      outer:
      while (true) {
        final int len = currentpairs.size();
        for (int i = 0; i < len; i++) {
          pair = currentpairs.get(i);
          final int state1 = (int) (pair & 0xffffffff);
          final int state2 = (int) (pair >> 32);
          foundtrans = searchTauSuccessors(startroot, state1, state2,
                                           targetpair, nextpairs);
          if (foundtrans != 0) {
            break outer;
          }
          foundtrans = searchTauSuccessors(startroot, state2, state1,
                                           targetpair, nextpairs);
          if (foundtrans != 0) {
            break outer;
          }
          foundtrans = searchProperSuccessors(startroot, state1, state2,
                                              targetpair, nextpairs);
          if (foundtrans != 0) {
            break outer;
          }
        }
        final TLongArrayList tmp = nextpairs;
        nextpairs = currentpairs;
        currentpairs = tmp;
      }
      mBFSLongList2.clear();
    }
    mBFSLongList1.clear();
    mBFSLongVisited.clear();
    return foundtrans;
  }

  /**
   * @return Found transition encoded as <CODE>source | (target << 32)</CODE>,
   *         or <CODE>0</CODE>.
   */
  private long searchTauSuccessors(final int startroot,
                                   final int current1,
                                   final int current2,
                                   final long targetpair,
                                   final TLongArrayList queue)
  {
    final long predpair = getPair(current1, current2);
    long predinfo = mBFSLongVisited.get(predpair);
    final int list = mUnobservableTauSuccessors[current1];
    final int root2 = getRootIndex(current2);
    mAltReadOnlyIterator.reset(list);
    while (mAltReadOnlyIterator.advance()) {
      final int succ = mAltReadOnlyIterator.getCurrentData();
      if (succ != current2) {
        final int succroot = getRootIndex(succ);
        if (succroot == startroot) {
          final long succpair = getPair(succ, current2);
          if (!mBFSLongVisited.containsKey(succpair)) {
            if (predinfo == 0) {
              predinfo = ((long) current1) | (((long) succ) << 32);
            }
            mBFSLongVisited.put(succpair, predinfo);
            queue.add(succpair);
          }
        } else if (getPair(succroot, root2) == targetpair) {
          if (predinfo == 0) {
            predinfo = ((long) current1) | (((long) succ) << 32);
          }
          return predinfo;
        }
      }
    }
    return 0;
  }

  private long searchProperSuccessors(final int startroot,
                                      final int current1,
                                      final int current2,
                                      final long targetpair,
                                      final TLongArrayList queue)
  {
    final long predpair = getPair(current1, current2);
    final long predinfo = mBFSLongVisited.get(predpair);
    for (int e = 0; e < mUnobservableTau; e++) {
      final int succ1 = mObservableSucessor[current1][e];
      final int succ2 = mObservableSucessor[current2][e];
      if (succ1 != NO_TRANSITION && succ2 != NO_TRANSITION && succ1 != succ2) {
        final int root1 = getRootIndex(succ1);
        final int root2 = getRootIndex(succ1);
        if (root1 == startroot && root2 == startroot) {
          final long succpair = getPair(succ1, succ2);
          if (!mBFSLongVisited.containsKey(succpair)) {
            mBFSLongVisited.put(succpair, predinfo);
            queue.add(succpair);
          }
        } else if (getPair(root1, root2) == targetpair) {
          return predinfo;
        }
      }
    }
    return 0;
  }


  //#########################################################################
  //# Output Automaton Construction
  private boolean hasSilentTransitions()
  {
    final int numStates = mOriginalStates.length;
    for (int s = 0; s < numStates; s++) {
      if (mUnobservableTauSuccessors[s] != IntListBuffer.NULL) {
        return true;
      }
    }
    return false;
  }

  private AutomatonProxy createOutputAutomaton()
  {
    String name = getOutputName();
    if (name == null) {
      name = mInputAutomaton.getName();
    }
    ComponentKind kind = getOutputKind();
    if (kind == null) {
      kind = mInputAutomaton.getKind();
    }

    // 1. Merge states
    // All strongly tau-connected components are treated as a single state.
    // Furthermore, any two components linked by a tau-transitions are merged.
    // The observer property ensures that the result is still observation
    // equivalent to the original automaton.
    final int numStates = mOriginalStates.length;
    for (int src = 0; src < numStates; src++) {
      final int list = mUnobservableTauSuccessors[src];
      if (list != IntListBuffer.NULL) {
        mReadOnlyIterator.reset(list);
        while (mReadOnlyIterator.advance()) {
          final int tausucc = mReadOnlyIterator.getCurrentData();
          mergeComponents(src, tausucc);
        }
      }
    }

    // 2. Create States
    Collection<EventProxy> allProps = new LinkedList<EventProxy>();
    for (final EventProxy event : mEvents) {
      if (event.getKind() == EventKind.PROPOSITION) {
        allProps.add(event);
      }
    }
    final Collection<StateProxy> states = new ArrayList<StateProxy>(numStates);
    final TIntObjectHashMap<MemStateProxy> stateMap =
      new TIntObjectHashMap<MemStateProxy>(numStates);
    int code = 0;
    for (int s = 0; s < numStates; s++) {
      final int root = getRootIndex(s);
      if (!stateMap.containsKey(root)) {
        final boolean init = (root == 0);
        final StronglyConnectedComponent comp = mComponentOfState[s];
        final Collection<EventProxy> props = new LinkedList<EventProxy>();
        for (final EventProxy prop : allProps) {
          final int p = mEventMap.get(prop);
          final boolean enabled = comp == null ?
                                  mObservableSucessor[s][p] != NO_TRANSITION :
                                  comp.isEnabledEvent(p);
          if (enabled) {
            props.add(prop);
          }
        }
        final MemStateProxy state = new MemStateProxy(code, init, props);
        states.add(state);
        stateMap.put(root, state);
        code++;
      }
    }
    allProps = null;

    // 3. Create Transitions
    final ProductDESProxyFactory factory = getFactory();
    final List<EventProxy> observableTauEvents = new ArrayList<EventProxy>();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>();
    final int numEvents = mEvents.length;
    final BitSet observableTauSuccessors = new BitSet(numStates);
    for (int s = 0; s < numStates; s++) {
      final StronglyConnectedComponent comp = mComponentOfState[s];
      if (comp == null || comp.getRootIndex() == s) {
        final StateProxy sourceState = stateMap.get(s);
        for (int e = 0; e < numEvents; e++) {
          final EventProxy event = mEvents[e];
          if (e == mUnobservableTau ||
              event.getKind() == EventKind.PROPOSITION) {
            // skip
          } else if (e == mObservableTau) {
            if (comp == null) {
              createObservableTauTransitions(s, stateMap,
                                             observableTauEvents, transitions,
                                             observableTauSuccessors, 0);
            } else if (comp.isEnabledEvent(e)) {
              int eindex = 0;
              comp.iterate();
              while (mReadOnlyIterator.advance()) {
                final int source = mReadOnlyIterator.getCurrentData();
                final int root = getRootIndex(source);
                eindex =
                  createObservableTauTransitions(root, stateMap,
                                                 observableTauEvents,
                                                 transitions,
                                                 observableTauSuccessors,
                                                 eindex);
              }
            }
          } else {
            if (comp == null) {
              final int succ = mObservableSucessor[s][e];
              if (succ != NO_TRANSITION) {
                final int target = getRootIndex(succ);
                final StateProxy targetState = stateMap.get(target);
                final TransitionProxy trans =
                  factory.createTransitionProxy(sourceState,
                                                event,
                                                targetState);
                transitions.add(trans);
              }
            } else if (comp.isEnabledEvent(e)) {
              comp.iterate();
              while (mReadOnlyIterator.advance()) {
                final int src = mReadOnlyIterator.getCurrentData();
                final int succ = mObservableSucessor[src][e];
                if (succ != NO_TRANSITION) {
                  final int target = getRootIndex(succ);
                  final StateProxy targetState = stateMap.get(target);
                  final TransitionProxy trans =
                    factory.createTransitionProxy(sourceState,
                                                  event,
                                                  targetState);
                  transitions.add(trans);
                  break;
                }
              }
            }
          }
        }
      }
    }

    // 3. Create Output Automaton
    final int numOutputEvents = mEvents.length + observableTauEvents.size();
    final List<EventProxy> events = new ArrayList<EventProxy>(numOutputEvents);
    for (final EventProxy event : mEvents) {
      events.add(event);
    }
    events.addAll(observableTauEvents);
    return factory.createAutomatonProxy(name, kind,
                                        events, states, transitions);
  }

  private void mergeComponents(final int state1, final int state2)
  {
    final StronglyConnectedComponent comp1 = mComponentOfState[state1];
    final StronglyConnectedComponent comp2 = mComponentOfState[state2];
    if (comp1 == null) {
      if (comp2 == null) {
        final int list = mListBuffer.createList();
        mListBuffer.append(list, state1);
        if (state1 < state2) {
          mListBuffer.append(list, state2);
        } else {
          mListBuffer.prepend(list, state2);
        }
        final StronglyConnectedComponent comp =
          new StronglyConnectedComponent(list);
        mComponentOfState[state1] = mComponentOfState[state2] = comp;
        comp.setUpEventStatus();
      } else {
        comp2.merge(state1);
      }
    } else {
      if (comp2 == null) {
        comp1.merge(state2);
      } else if (comp1 != comp2) {
        comp1.merge(comp2);
      }
    }
  }

  int createObservableTauTransitions
    (final int source,
     final TIntObjectHashMap<? extends StateProxy> stateMap,
     final List<EventProxy> events,
     final Collection<TransitionProxy> transitions,
     final BitSet used,
     int eindex)
  {
    final int list = mObservableTauSuccessors[source];
    if (list != IntListBuffer.NULL) {
      final ProductDESProxyFactory factory = getFactory();
      final StateProxy sourceState = stateMap.get(source);
      mAltReadOnlyIterator.reset(list);
      while (mAltReadOnlyIterator.advance()) {
        final int succ = mAltReadOnlyIterator.getCurrentData();
        final int target = getRootIndex(succ);
        if (target != source && !used.get(target)) {
          final EventProxy event;
          if (eindex < events.size()) {
            event = events.get(eindex);
          } else {
            eindex++;
            event = createObservableTauEvent(eindex);
            events.add(event);
          }
          final StateProxy targetState = stateMap.get(target);
          final TransitionProxy trans =
            factory.createTransitionProxy(sourceState, event, targetState);
          transitions.add(trans);
          used.set(target);
        }
      }
    }
    return eindex;
  }

  private EventProxy createObservableTauEvent(final int eindex)
  {
    final ProductDESProxyFactory factory = getFactory();
    String autname = getOutputName();
    if (autname == null) {
      autname = mInputAutomaton.getName();
    }
    final String ename = ":op" + eindex + ':' + autname;
    return factory.createEventProxy(ename, EventKind.CONTROLLABLE, false);
  }


  //#########################################################################
  //# Auxiliary Methods
  private long getPair(final int root1, final int root2)
  {
    if (root1 < root2) {
      return root1 | ((long) root2 << 32);
    } else {
      return root2 | ((long) root1 << 32);
    }
  }

  private int getRootIndex(final int state)
  {
    final StronglyConnectedComponent comp = mComponentOfState[state];
    if (comp == null) {
      return state;
    } else {
      return comp.getRootIndex();
    }
  }


  //#########################################################################
  //# Inner Class Tarjan
  private class Tarjan
  {

    //#########################################################################
    //# Constructor
    private Tarjan(final int numStates)
    {
      mTarjan = new int[numStates];
      mLowLink = new int[numStates];
      mStack = new TIntStack();
      mOnStack = new boolean[numStates];
      mComponents = new ArrayList<StronglyConnectedComponent>();
    }

    //#########################################################################
    //# Invocation
    private void findStronglyConnectedComponents()
    {
      final int numStates = mTarjan.length;
      mCallIndex = 1;
      for (int state = 0; state < numStates; state++) {
        if (mTarjan[state] == 0) {
          tarjan(state);
        }
      }
      setUpEventStatus();
    }

    private void split(final StronglyConnectedComponent comp)
    {
      comp.iterate();
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        mTarjan[state] = mLowLink[state] = 0;
      }
      mCallIndex = 1;
      comp.iterate();
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        if (mTarjan[state] == 0) {
          tarjan(state);
        }
      }
      setUpEventStatus();
    }

    private void setUpEventStatus()
    {
      for (final StronglyConnectedComponent comp : mComponents) {
        comp.setUpEventStatus();
      }
      mComponents.clear();
    }

    //#########################################################################
    //# Algorithm
    private void tarjan(final int state)
    {
      mTarjan[state] = mLowLink[state] = mCallIndex++;
      mOnStack[state] = true;
      mStack.push(state);
      final int successors = mUnobservableTauSuccessors[state];
      if (successors != IntListBuffer.NULL) {
        final IntListBuffer.Iterator iter =
          mListBuffer.createReadOnlyIterator(successors);
        while (iter.advance()) {
          final int succ = iter.getCurrentData();
          if (mOnStack[succ]) {
            mLowLink[state] = mTarjan[succ] < mLowLink[state] ?
                              mTarjan[succ] : mLowLink[state];
          } else if (mTarjan[succ] == 0) {
            tarjan(succ);
            mLowLink[state] = mLowLink[succ] < mLowLink[state] ?
                              mLowLink[succ] : mLowLink[state];
          }
        }
      }
      if (mTarjan[state] == mLowLink[state]) {
        final int list = mListBuffer.createList();
        int pop;
        int count = 0;
        do {
          pop = mStack.pop();
          mListBuffer.append(list, pop);
          mOnStack[pop] = false;
          count++;
        } while (pop != state);
        if (count > 1) {
          final StronglyConnectedComponent comp =
            new StronglyConnectedComponent(list);
          mComponents.add(comp);
          mReadOnlyIterator.reset(list);
          while (mReadOnlyIterator.advance()) {
            final int elem = mReadOnlyIterator.getCurrentData();
            mComponentOfState[elem] = comp;
          }
        } else {
          mListBuffer.dispose(list);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final int[] mTarjan;
    private final int[] mLowLink;
    private final TIntStack mStack;
    private final boolean[] mOnStack;
    private final Collection<StronglyConnectedComponent> mComponents;

    private int mCallIndex;
  }


  //#########################################################################
  //# Inner Class StronglyConnectedComponent
  private class StronglyConnectedComponent
  {

    //#######################################################################
    //# Constructor
    private StronglyConnectedComponent(final int states)
    {
      mStates = states;
      mEnabledEvents = new BitSet(mObservableTau);
    }

    //#######################################################################
    //# Simple Access
    private int getRootIndex()
    {
      return mListBuffer.getFirst(mStates);
    }

    private void iterate()
    {
      mReadOnlyIterator.reset(mStates);
    }

    private boolean isEnabledEvent(final int e)
    {
      return mEnabledEvents.get(e);
    }

    //#######################################################################
    //# Flag Setup
    private void setUpEventStatus()
    {
      events:
      for (int e = 0; e < mUnobservableTau; e++) {
        iterate();
        while (mReadOnlyIterator.advance()) {
          final int state = mReadOnlyIterator.getCurrentData();
          if (mObservableSucessor[state][e] != NO_TRANSITION) {
            mEnabledEvents.set(e);
            continue events;
          }
        }
      }
      iterate();
      tau:
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        final int list = mUnobservableTauSuccessors[state];
        if (list != IntListBuffer.NULL) {
          mAltReadOnlyIterator.reset(list);
          while (mAltReadOnlyIterator.advance()) {
            final int succ = mAltReadOnlyIterator.getCurrentData();
            if (mComponentOfState[succ] != this) {
              mEnabledEvents.set(mUnobservableTau);
              break tau;
            }
          }
        }
      }
    }

    //#######################################################################
    //# Merging
    private void merge(final int state)
    {
      final int first = mListBuffer.getFirst(mStates);
      if (state < first) {
        mListBuffer.prepend(mStates, state);
      } else {
        mListBuffer.append(mStates, state);
      }
      mComponentOfState[state] = this;
      for (int e = 0; e < mUnobservableTau; e++) {
        if (mObservableSucessor[state][e] != NO_TRANSITION) {
          mEnabledEvents.set(e);
        }
      }
      if (mUnobservableTauSuccessors[state] != IntListBuffer.NULL) {
        mEnabledEvents.set(mUnobservableTau);
      }
    }

    private void merge(final StronglyConnectedComponent comp)
    {
      comp.iterate();
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        mComponentOfState[state] = this;
      }
      mStates = mListBuffer.catenateDestructively(mStates, comp.mStates);
      mEnabledEvents.or(comp.mEnabledEvents);
    }

    //#######################################################################
    //# Data Members
    private int mStates;
    private final BitSet mEnabledEvents;

  }


  //#########################################################################
  //# Data Members
  private Collection<EventProxy> mHiddenEvents;
  private Collection<EventProxy> mPropositions;

  private AutomatonProxy mInputAutomaton;
  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mReadOnlyIterator;
  private IntListBuffer.ReadOnlyIterator mAltReadOnlyIterator;
  private EventProxy[] mEvents;
  private TObjectIntHashMap<EventProxy> mEventMap;
  private int mUnobservableTau;
  private int mObservableTau;
  private StateProxy[] mOriginalStates;
  private int[][] mObservableSucessor;
  private int[] mUnobservableTauSuccessors;
  private int[] mObservableTauSuccessors;
  private Tarjan mTarjan;
  private StronglyConnectedComponent[] mComponentOfState;

  private TLongArrayList mVerifierStatePairs;
  private TLongIntHashMap mVerifierStateMap;
  private TIntArrayList mVerifierPredecessors;
  private int mPredecessorsOfDead;
  private TIntArrayList mBFSIntList1;
  private TIntArrayList mBFSIntList2;
  private TIntHashSet mBFSIntVisited;
  private TLongArrayList mBFSLongList1;
  private TLongArrayList mBFSLongList2;
  private TLongLongHashMap mBFSLongVisited;


  //#########################################################################
  //# Data Members
  private static final int NO_TRANSITION = -1;
  private static final int DUMP_STATE = -2;

}
