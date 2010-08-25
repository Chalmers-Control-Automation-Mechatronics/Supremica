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
import gnu.trove.TIntStack;
import gnu.trove.TLongArrayList;
import gnu.trove.TLongIntHashMap;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.NondeterministicDESException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;


/**
 * An implementation of the OP-search algorithm by Patricia Pena et.al.
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
      return false;
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
    setUpAutomatonEncoding();
    setUpStronglyConnectedComponents();
    setUpVerifier();
  }

  protected void tearDown()
  {
    super.tearDown();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setUpAutomatonEncoding()
    throws AnalysisException
  {
    final AutomatonProxy aut = getInputAutomaton();

    final Collection<EventProxy> events = aut.getEvents();
    int numEvents = 0;
    for (final EventProxy event : events) {
      if (!mHiddenEvents.contains(event)) {
        numEvents++;
      }
    }
    mEvents = new EventProxy[numEvents];
    mUnobservableTau = numEvents++;
    mObservableTau = numEvents++;
    final TObjectIntHashMap<EventProxy> eventMap =
      new TObjectIntHashMap<EventProxy>(numEvents);
    int next = 0;
    for (final EventProxy event : events) {
      if (!mHiddenEvents.contains(event)) {
        mEvents[next] = event;
        eventMap.put(event, next);
        next++;
      } else {
        eventMap.put(event, mUnobservableTau);
      }
    }

    final Collection<StateProxy> states = aut.getStates();
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
        throw new NondeterministicDESException(aut, state);
      }
      mOriginalStates[code] = state;
      stateMap.put(state, code);
    }

    mObservableSucessor = new int[numEvents][numStates];
    for (next = 0; next < numEvents; next++) {
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
            final int propCode = eventMap.get(prop);
            setUpTransition(aut, state, prop, stateCode, propCode, DUMP_STATE);
          }
        }
      }
    }
    for (final TransitionProxy trans : aut.getTransitions()) {
      final StateProxy source = trans.getSource();
      final int sourceCode = stateMap.get(source);
      final StateProxy target = trans.getTarget();
      final int targetCode = stateMap.get(target);
      final EventProxy event = trans.getEvent();
      final int eventCode = eventMap.get(event);
      setUpTransition(aut, source, event, sourceCode, eventCode, targetCode);
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
    mComponents = new ArrayList<StronglyConnectedComponent>();
    mComponentOfState = new StronglyConnectedComponent[numStates];
    final Tarjan tarjan = new Tarjan();
    tarjan.findStronglyConnectedComponents();
    for (final StronglyConnectedComponent comp : mComponents) {
      comp.setUpEventStatus();
    }
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
    final int pindex = 0;
    while (pindex < mVerifierStatePairs.size()) {
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
    final StronglyConnectedComponent comp1 = mComponents.get(code1);
    final int tausucc1 = mUnobservableTauSuccessors[code1];
    final boolean entau1 = comp1 == null ?
                           tausucc1 != IntListBuffer.NULL :
                           comp1.isEnabledEvent(mUnobservableTau);
    final StronglyConnectedComponent comp2 = mComponents.get(code2);
    final int tausucc2 = mUnobservableTauSuccessors[code2];
    final boolean entau2 = comp2 == null ?
                           tausucc2 != IntListBuffer.NULL :
                           comp2.isEnabledEvent(mUnobservableTau);
    // Proper event transitions ...
    for (int e = 0; e < mUnobservableTau; e++) {
      int esucc1 = 0, esucc2 = 0;
      final boolean en1, en2;
      if (comp1 == null) {
        esucc1 = mObservableSucessor[e][code1];
        en1 = esucc1 != NO_TRANSITION;
      } else {
        en1 = comp1.isEnabledEvent(e);
      }
      if (comp2 == null) {
        esucc2 = mObservableSucessor[e][code1];
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
            for (final int member1 : comp1.getStates()) {
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
    final int[] states = comp.getStates();
    final int numStates = states.length;
    for (int i = 0; i < numStates; i++) {
      final int source1 = states[i];
      final int succ1 = mObservableSucessor[e][source1];
      if (succ1 != NO_TRANSITION) {
        for (int j = i + 1; i < numStates; j++) {
          final int source2 = states[j];
          final int succ2 = mObservableSucessor[e][source2];
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
    for (final int member2 : comp2.getStates()) {
      final int succ2 = mObservableSucessor[e][member2];
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
      for (final int state : comp1.getStates()) {
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
      final StronglyConnectedComponent comp1 = mComponents.get(to1);
      final int root1 = comp1 == null ? to1 : comp1.getRootIndex();
      final StronglyConnectedComponent comp2 = mComponents.get(to2);
      final int root2 = comp2 == null ? to2 : comp2.getRootIndex();
      if (root1 != root2) {
        final long pair;
        if (root1 < root2) {
          pair = root1 | ((long) root2 << 32);
        } else {
          pair = root2 | ((long) root1 << 32);
        }
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
  //# Inner Class Tarjan
  private class Tarjan
  {

    //#########################################################################
    //# Invocation
    private void findStronglyConnectedComponents()
    {
      final int numStates = mOriginalStates.length;
      mTarjan = new int[numStates];
      mLowLink = new int[numStates];
      mStack = new TIntStack();
      mOnStack = new boolean[numStates];
      mTempComponent = new TIntArrayList();
      mCallIndex = 1;
      for (int state = 0; state < numStates; state++) {
        if (mTarjan[state] == 0) {
          tarjan(state);
        }
      }
    }

    //#########################################################################
    //# Inner Class Tarjan
    private void tarjan(final int state)
    {
      mTarjan[state] = mLowLink[state] = mCallIndex++;
      final int successors = mUnobservableTauSuccessors[state];
      if (successors == IntListBuffer.NULL) {
        return;
      }
      mOnStack[state] = true;
      mStack.push(state);
      final IntListBuffer.Iterator iter =
        mListBuffer.createReadOnlyIterator(successors);
      while (iter.advance()) {
        final int succ = iter.getCurrentData();
        if (mOnStack[succ]) {
          mLowLink[state] =
            mTarjan[succ] < mLowLink[state] ? mTarjan[succ] : mLowLink[state];
        } else if (mTarjan[succ] == 0) {
          tarjan(succ);
          mLowLink[state] =
            mLowLink[succ] < mLowLink[state] ? mLowLink[succ] : mLowLink[state];
        }
      }
      if (mTarjan[state] == mLowLink[state]) {
        int pop;
        do {
          pop = mStack.pop();
          mTempComponent.add(pop);
          mOnStack[pop] = false;
        } while (pop != state);
        if (mTempComponent.size() > 1) {
          final int[] array = mTempComponent.toNativeArray();
          final int end = array.length - 1;
          array[end] = array[0];
          array[0] = state;
          final StronglyConnectedComponent comp =
            new StronglyConnectedComponent(array);
          mComponents.add(comp);
          for (final int elem : array) {
            mComponentOfState[elem] = comp;
          }
        }
        mTempComponent.clear();
      }
    }

    //#######################################################################
    //# Data Members
    private int[] mTarjan;
    private int[] mLowLink;
    private TIntStack mStack;
    private boolean[] mOnStack;
    private int mCallIndex;
    private TIntArrayList mTempComponent;

  }


  //#########################################################################
  //# Inner Class StronglyConnectedComponent
  private class StronglyConnectedComponent
  {

    //#######################################################################
    //# Constructor
    private StronglyConnectedComponent(final int[] states)
    {
      mStates = states;
      mEnabledEvents = new BitSet(mObservableTau);
    }

    //#######################################################################
    //# Simple Access
    private int getRootIndex()
    {
      return mStates[0];
    }

    private int[] getStates()
    {
      return mStates;
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
        final int[] successors = mObservableSucessor[e];
        for (final int state : mStates) {
          if (successors[state] != NO_TRANSITION) {
            mEnabledEvents.set(e);
            continue events;
          }
        }
      }
      tau:
      for (final int state : mStates) {
        final int list = mUnobservableTauSuccessors[state];
        if (list != IntListBuffer.NULL) {
          mReadOnlyIterator.reset(list);
          while (mReadOnlyIterator.advance()) {
            final int succ = mReadOnlyIterator.getCurrentData();
            if (mComponentOfState[succ] != this) {
              mEnabledEvents.set(mUnobservableTau);
              break tau;
            }
          }
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final int[] mStates;
    private final BitSet mEnabledEvents;

  }


  //#########################################################################
  //# Data Members
  private Set<EventProxy> mHiddenEvents;
  private Set<EventProxy> mPropositions;

  private IntListBuffer mListBuffer;
  private IntListBuffer.Iterator mReadOnlyIterator;
  private EventProxy[] mEvents;
  private int mUnobservableTau;
  private int mObservableTau;
  private StateProxy[] mOriginalStates;
  private int[][] mObservableSucessor;
  private int[] mUnobservableTauSuccessors;
  private int[] mObservableTauSuccessors;
  private List<StronglyConnectedComponent> mComponents;
  private StronglyConnectedComponent[] mComponentOfState;

  private TLongArrayList mVerifierStatePairs;
  private TLongIntHashMap mVerifierStateMap;
  private TIntArrayList mVerifierPredecessors;
  private int mPredecessorsOfDead;


  //#########################################################################
  //# Data Members
  private static final int NO_TRANSITION = -1;
  private static final int DUMP_STATE = -2;

}
