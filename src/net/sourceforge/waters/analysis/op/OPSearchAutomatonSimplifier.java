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
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AbstractAutomatonTransformer;
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
  extends AbstractAutomatonTransformer
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
   * @see {@link #setPropositions(Collection) setPropositions()}
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
      mCallIndex = 0;
      for (int state = 0; state < numStates; state++) {
        tarjan(state);
      }
    }

    //#########################################################################
    //# Inner Class Tarjan
    private void tarjan(final int state)
    {
      mTarjan[state] = mCallIndex;
      mLowLink[state] = mCallIndex;
      mCallIndex++;
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
    //# Flag Access
    private void setUpEventStatus()
    {
      events:
      for (int e = 0; e < mUnobservableTau; e++) {
        final int[] successors = mObservableSucessor[e];
        for (final int state : mStates) {
          if (successors[state] != NO_TRANSITION) {
            mEnabledEvents.set(e);
            break events;
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

    @SuppressWarnings("unused")
    private boolean mayExecuteEvent(final int e)
    {
      return mEnabledEvents.get(e) || mEnabledEvents.get(mUnobservableTau);
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


  //#########################################################################
  //# Data Members
  private static final int NO_TRANSITION = -1;
  private static final int DUMP_STATE = -2;

}
