//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis Algorithms
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObservationEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntStack;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TLongObjectIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ProxyTools;
import org.apache.log4j.Logger;


/**
 * @author Simon Ware, Rachel Francis, Robi Malik
 */

public class ObservationEquivalenceTRSimplifier implements
    TransitionRelationSimplifier
{

  // #########################################################################
  // # Constructors
  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public ObservationEquivalenceTRSimplifier(
                                            final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
    mSuppressRedundantHiddenTransitions = false;
    mTransitionLimit = Integer.MAX_VALUE;
  }

  // #########################################################################
  // # Interface net.sourceforge.waters.analysis.op.TransitionRelationSimplifier
  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public void setTransitionRelation(final ListBufferTransitionRelation rel)
  {
    mTransitionRelation = rel;
  }

  // #########################################################################
  // # Configuration
  /**
   * Sets whether redundant hidden transitions can be suppressed in the output
   * automaton. If this is set to <CODE>true</CODE>, the transition minimisation
   * algorithm will attempt to remove tau-transitions that can be replaced by a
   * sequence of two or more other tau-transitions. This only works if the input
   * automaton is already tau-loop free, so it should only be set in this case.
   * The default is <CODE>false</CODE>, which guarantees a correct but not
   * necessarily minimal result for all inputs.
   */
  public void setSuppressRedundantHiddenTransitions(final boolean suppress)
  {
    mSuppressRedundantHiddenTransitions = suppress;
  }

  /**
   * Gets whether redundant hidden transitions can be suppressed in the output
   * automaton.
   *
   * @see #setSuppressRedundantHiddenTransitions(boolean)
   *      setSuppressRedundantHiddenTransitions()
   */
  public boolean getSuppressRedundantHiddenTransitions()
  {
    return mSuppressRedundantHiddenTransitions;
  }

  /**
   * Sets an initial partition for the bisimulation algorithm. If non-null, any
   * partition computed will be a refinement of the given initial partition. If
   * null, an initial partition will be determined based on the propositions of
   * the states.
   *
   * @param partition
   *          A collection of classes constituting the initial partition. Each
   *          array in the collection represents a class of equivalent state
   *          codes.
   */
  public void setInitialPartition(final Collection<int[]> partition)
  {
    mInitialPartition = partition;
  }

  /**
   * Gets the initial partition, if any was set.
   *
   * @return Initial partition or <CODE>null</CODE>.
   * @see #setInitialPartition(Collection) setInitialPartition()
   */
  public Collection<int[]> getInitialPartition()
  {
    return mInitialPartition;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be constructed. An attempt to store more
   * transitions leads to an {@link OverflowException}.
   *
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
   *
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  // #########################################################################
  // # Invocation
  public boolean run() throws AnalysisException
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      final String msg =
          "ENTER " + ProxyTools.getShortClassName(this) + ".run(): "
              + mTransitionRelation.getName() + " with "
              + mTransitionRelation.getNumberOfReachableStates()
              + " states and " + mTransitionRelation.getNumberOfTransitions()
              + " transitions ...";
      logger.debug(msg);
    }

    setUp();

    while (true) {
      final Iterator<? extends EquivalenceClass> it;
      if (!mWS.isEmpty()) {
        it = mWS.iterator();
      } else if (!mWC.isEmpty()) {
        it = mWC.iterator();
      } else {
        break;
      }
      final EquivalenceClass ec = it.next();
      it.remove();
      ec.splitOn();
    }
    final int numClasses = mP.size();
    assert numClasses >= 0;
    assert numClasses <= mNumStates;
    mHasModifications |= numClasses != mNumStates;
    if (!mHasModifications) {
      if (logger.isDebugEnabled()) {
        final String msg =
            "EXIT " + ProxyTools.getShortClassName(this) + ".run(): no change.";
        logger.debug(msg);
      }
      return false;
    }

    buildResultPartition();

    if (logger.isDebugEnabled()) {
      final String msg =
          "EXIT " + ProxyTools.getShortClassName(this) + ".run(): "
              + mTransitionRelation.getNumberOfReachableStates()
              + " states and " + mTransitionRelation.getNumberOfTransitions()
              + " transitions ...";
      logger.debug(msg);
    }
    return true;
  }

  /**
   * Gets the partition resulting from the last call to {@link #run()}.
   *
   * @return A list of classes constituting the partition, or <CODE>null</CODE>.
   *         <CODE>null</CODE> may be returned to indicate a trivial partition,
   *         i.e., no states are merged. Otherwise each array in the returned
   *         list represents a class of equivalent state codes.
   */
  public List<int[]> getResultPartition()
  {
    return mResultPartition;
  }

  // #########################################################################
  // # Auxiliary Methods
  private void setUp() throws OverflowException
  {
    mNumStates = mTransitionRelation.getNumberOfStates();
    mNumEvents = mTransitionRelation.getNumberOfProperEvents();
    mHasModifications = false;
    setUpTauPredecessors();
    removeRedundantTransitions();
    final Collection<int[]> partition = createInitialPartition();
    setUpInitialPartition(partition);
  }

  private void setUpTauPredecessors() throws OverflowException
  {
    final int tau = EventEncoding.TAU;
    final TransitionIterator iter =
        mTransitionRelation.createPredecessorsReadOnlyIterator();
    int numtrans = mTransitionRelation.getNumberOfTransitions();
    final TIntHashSet hashTauPreds = new TIntHashSet();
    final TIntArrayList listTauPreds = new TIntArrayList();
    final TIntStack stack = new TIntStack();
    mTauPreds = new int[mNumStates][];
    for (int s = 0; s < mNumStates; s++) {
      if (mTransitionRelation.isReachable(s)) {
        hashTauPreds.add(s);
        listTauPreds.add(s);
        stack.push(s);
        while (stack.size() > 0) {
          final int taupred = stack.pop();
          iter.reset(taupred, tau);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            if (hashTauPreds.add(pred)) {
              listTauPreds.add(pred);
              stack.push(pred);
            }
          }
        }
        mTauPreds[s] = listTauPreds.toNativeArray();
        numtrans += listTauPreds.size();
        if (numtrans > mTransitionLimit) {
          throw new OverflowException(OverflowException.Kind.TRANSITION,
              mTransitionLimit);
        }
        hashTauPreds.clear();
        listTauPreds.clear();
      }
    }
  }

  private void removeRedundantTransitions()
  {
    final int tau = EventEncoding.TAU;
    final TransitionIterator iter0 =
        mTransitionRelation.createAllTransitionsModifyingIterator();
    final TransitionIterator iter2 =
        mTransitionRelation.createPredecessorsReadOnlyIterator();
    trans: while (iter0.advance()) {
      final int e = iter0.getCurrentEvent();
      if (e == tau && !mSuppressRedundantHiddenTransitions) {
        continue;
      }
      final int source0 = iter0.getCurrentSourceState();
      final int target0 = iter0.getCurrentTargetState();
      for (final int p1 : mTauPreds[target0]) {
        iter2.reset(p1, e);
        while (iter2.advance()) {
          final int p2 = iter2.getCurrentSourceState();
          if (e == tau) {
            if (p1 != target0 && p2 == source0) {
              iter0.remove();
              mHasModifications = true;
              continue trans;
            }
          } else {
            if (p1 != target0 || p2 != source0) {
              for (final int p3 : mTauPreds[p2]) {
                if (p3 == source0) {
                  iter0.remove();
                  mHasModifications = true;
                  continue trans;
                }
              }
            }
          }
        }
      }
    }
  }

  private Collection<int[]> createInitialPartition()
  {
    if (mInitialPartition == null) {
      final long[] markings = new long[mNumStates];
      for (int state = 0; state < mNumStates; state++) {
        if (mTransitionRelation.isReachable(state)) {
          markings[state] = mTransitionRelation.getAllMarkings(state);
        }
      }
      for (int state = 0; state < mNumStates; state++) {
        if (mTransitionRelation.isReachable(state)) {
          final long marking = markings[state];
          for (final int taupred : mTauPreds[state]) {
            markings[taupred] =
                mTransitionRelation.mergeMarkings(marking, markings[taupred]);
          }
        }
      }
      final TLongObjectHashMap<TIntArrayList> prepartition =
          new TLongObjectHashMap<TIntArrayList>();
      for (int state = 0; state < mNumStates; state++) {
        if (mTransitionRelation.isReachable(state)) {
          final long marking = markings[state];
          TIntArrayList list = prepartition.get(marking);
          if (list == null) {
            list = new TIntArrayList();
            prepartition.put(marking, list);
          }
          list.add(state);
        }
      }
      final Collection<int[]> partition =
          new ArrayList<int[]>(prepartition.size());
      final TLongObjectIterator<TIntArrayList> iter = prepartition.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final TIntArrayList list = iter.value();
        final int[] array = list.toNativeArray();
        partition.add(array);
      }
      return partition;
    } else {
      return mInitialPartition;
    }
  }

  private void setUpInitialPartition(final Collection<int[]> partition)
  {
    mWS = new THashSet<SimpleEquivalenceClass>();
    mWC = new THashSet<ComplexEquivalenceClass>();
    mP = new THashSet<SimpleEquivalenceClass>();
    mStateToClass = new SimpleEquivalenceClass[mNumStates];
    for (final int[] array : partition) {
      final SimpleEquivalenceClass clazz = new SimpleEquivalenceClass(array);
      mWS.add(clazz);
      for (final int state : array) {
        mStateToClass[state] = clazz;
      }
    }
  }

  public void refineInitialPartition(final ListBufferTransitionRelation tr,
                                     final int alphaCode)
  {
    if (mTauPreds != null) {
      final Collection<int[]> refinedPartition = new HashSet<int[]>();
      final Set<Integer> initialStates = new HashSet<Integer>();

      for (final int[] equivClass : getInitialPartition()) {
        for (int i = 0; i < equivClass.length; i++) {
          final int stateCode = equivClass[i];
          if (tr.isInitial(stateCode)) {
            initialStates.add(stateCode);
            final int[] tauPreds = mTauPreds[stateCode];
            for (final int pred : tauPreds) {
              initialStates.add(pred);
            }
          } else {
            // skip over equivalence class of "remaining" states (which cannot
            // possibly contain initial states)
            refinedPartition.add(equivClass);
            break;
          }
        }
      }
      final int[] initialStatesArray = new int[initialStates.size()];
      for (int i = 0; i < initialStatesArray.length; i++) {
        final int stateCode = initialStatesArray[i];
        if (tr.isMarked(stateCode, alphaCode)) {
          // creates a separate equivalence class for every state marked alpha
          final int[] alphaClass = new int[1];
          alphaClass[0] = stateCode;
          refinedPartition.add(alphaClass);
          initialStatesArray[i] = -1;
        } else {
          initialStatesArray[i] = stateCode;
        }
      }
      refinedPartition.add(initialStatesArray);
      setInitialPartition(refinedPartition);
    } else {
      // TODO: throw some kind of exception to say setUpTauPredecessors must be
      // called first
    }

  }

  private TransitionIterator getPredecessorIterator(final int state,
                                                    final int event)
  {
    if (event == EventEncoding.TAU) {
      return new TauClosureTransitionIterator(state);
    } else {
      return new ProperEventClosureTransitionIterator(state, event);
    }
  }

  private void addToW(final SimpleEquivalenceClass sec, final int[] X1,
                      final int[] X2)
  {
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    mP.remove(sec);
    if (mWS.remove(sec)) {
      mWS.add(child1);
      mWS.add(child2);
    } else {
      final ComplexEquivalenceClass comp =
          new ComplexEquivalenceClass(child1, child2);
      comp.mInfo = sec.mInfo;
      if (sec.mParent != null) {
        comp.mParent = sec.mParent;
        final ComplexEquivalenceClass p = sec.mParent;
        p.mChild1 = p.mChild1 == sec ? comp : p.mChild1;
        p.mChild2 = p.mChild2 == sec ? comp : p.mChild2;
      } else {
        mWC.add(comp);
      }
    }
  }

  private void addToW(final SimpleEquivalenceClass sec, int[] X1, int[] X2,
                      int[] X3)
  {
    if (X2.length < X1.length) {
      final int[] t = X1;
      X1 = X2;
      X2 = t;
    }
    if (X3.length < X1.length) {
      final int[] t = X1;
      X1 = X3;
      X3 = t;
    }
    final SimpleEquivalenceClass child1 = new SimpleEquivalenceClass(X1);
    final SimpleEquivalenceClass child2 = new SimpleEquivalenceClass(X2);
    final SimpleEquivalenceClass child3 = new SimpleEquivalenceClass(X3);
    mP.remove(sec);
    final ComplexEquivalenceClass X23 =
        new ComplexEquivalenceClass(child2, child3);
    final ComplexEquivalenceClass X123 =
        new ComplexEquivalenceClass(child1, X23);
    X123.mInfo = sec.mInfo;
    if (sec.mParent != null) {
      X123.mParent = sec.mParent;
      final ComplexEquivalenceClass p = sec.mParent;
      p.mChild1 = p.mChild1 == sec ? X123 : p.mChild1;
      p.mChild2 = p.mChild2 == sec ? X123 : p.mChild2;
    } else {
      mWC.add(X123);
    }
  }

  private void buildResultPartition()
  {
    final int numClasses = mP.size();
    if (numClasses == mNumStates) {
      mResultPartition = null;
    } else {
      int nextCode = 0;
      final int[] index = new int[numClasses];
      final int[][] partition = new int[numClasses][];
      for (int state = 0; state < mNumStates; state++) {
        if (mTransitionRelation.isReachable(state)) {
          final SimpleEquivalenceClass sec = mStateToClass[state];
          int code = sec.getCode();
          if (code < 0) {
            code = nextCode++;
            sec.setCode(code);
            partition[code] = new int[sec.mSize];
          }
          final int i = index[code]++;
          partition[code][i] = state;
        }
      }
      mResultPartition = Arrays.asList(partition);
    }
  }


  // #########################################################################
  // # Inner Class EquivalenceClass
  private abstract class EquivalenceClass
  {
    ComplexEquivalenceClass mParent = null;
    TIntIntHashMap[] mInfo = null;
    int mSize;

    public abstract TIntIntHashMap getInfo(int event);

    public abstract void splitOn();
  }


  // #########################################################################
  // # Inner Class EquivalenceClass
  private class SimpleEquivalenceClass extends EquivalenceClass
  {

    // #######################################################################
    // # Constructor
    public SimpleEquivalenceClass(final int[] states)
    {
      mStates = states;
      mSize = states.length; // TODO make this into function so less space
      mCode = -1;
      for (int i = 0; i < states.length; i++) {
        mStateToClass[states[i]] = this;
      }
      mP.add(this);
    }

    // #######################################################################
    // # Simple Access
    private int getCode()
    {
      return mCode;
    }

    private void setCode(final int code)
    {
      mCode = code;
    }

    // #######################################################################
    // # Splitting
    // TODO maybe keep track of what events an equivalence class has no
    // incoming events from
    public void splitOn()
    {
      mInfo = new TIntIntHashMap[mNumEvents];
      final List<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      for (int e = 0; e < mNumEvents; e++) {
        mInfo[e] = new TIntIntHashMap();
        final TIntIntHashMap map = mInfo[e];
        for (int s = 0; s < mStates.length; s++) {
          final int target = mStates[s];
          final TransitionIterator iter = getPredecessorIterator(target, e);
          while (iter.advance()) {
            final int pred = iter.getCurrentSourceState();
            final SimpleEquivalenceClass ec = mStateToClass[pred];
            TIntHashSet split = ec.mSplit1;
            if (split == null) {
              split = new TIntHashSet(ec.mSize);
              ec.mSplit1 = split;
              classes.add(ec);
            }
            split.add(pred);
            map.adjustOrPutValue(pred, 1, 1);
          }
        }
        for (final SimpleEquivalenceClass sec : classes) {
          if (sec.mSplit1.size() != sec.mSize) {
            final int[] X1 = new int[sec.mSize - sec.mSplit1.size()];
            final int[] X2 = new int[sec.mSplit1.size()];
            int x1 = 0, x2 = 0;
            for (int s = 0; s < sec.mStates.length; s++) {
              final int state = sec.mStates[s];
              if (sec.mSplit1.contains(state)) {
                X2[x2] = state;
                x2++;
              } else {
                X1[x1] = state;
                x1++;
              }
            }
            addToW(sec, X1, X2);
          }
          sec.mSplit1 = null;
        }
        classes.clear();
      }
    }

    public TIntIntHashMap getInfo(final int event)
    {
      if (mInfo == null) {
        mInfo = new TIntIntHashMap[mNumEvents];
      }
      TIntIntHashMap info = mInfo[event];
      if (info != null) {
        return info;
      }
      info = new TIntIntHashMap();
      mInfo[event] = info;
      for (int s = 0; s < mStates.length; s++) {
        final int state = mStates[s];
        final TransitionIterator iter = getPredecessorIterator(state, event);
        while (iter.advance()) {
          final int pred = iter.getCurrentSourceState();
          info.adjustOrPutValue(pred, 1, 1);
        }
      }
      return info;
    }

    // #######################################################################
    // # Data Members
    private final int[] mStates;
    private int mCode;
    private TIntHashSet mSplit1 = null;
    private TIntArrayList mX1 = null;
    private TIntArrayList mX2 = null;
    private TIntArrayList mX3 = null;
    private boolean mSplit = false;

  }


  // #########################################################################
  // # Inner Class EquivalenceClass
  private class ComplexEquivalenceClass extends EquivalenceClass
  {
    private EquivalenceClass mChild1;
    private EquivalenceClass mChild2;

    public ComplexEquivalenceClass(final EquivalenceClass child1,
                                   final EquivalenceClass child2)
    {
      if (child1.mSize < child2.mSize) {
        mChild1 = child1;
        mChild2 = child2;
      } else {
        mChild2 = child1;
        mChild1 = child2;
      }
      mChild1.mParent = this;
      mChild2.mParent = this;
      mSize = child1.mSize + child2.mSize;
    }

    public void splitOn()
    {
      final ArrayList<SimpleEquivalenceClass> classes =
          new ArrayList<SimpleEquivalenceClass>();
      mChild2.mInfo = new TIntIntHashMap[mNumEvents];
      for (int e = 0; e < mNumEvents; e++) {
        final TIntIntHashMap info = getInfo(e);
        final TIntIntHashMap process = new TIntIntHashMap();
        final TIntIntHashMap info1 = mChild1.getInfo(e);
        info.forEachEntry(new TIntIntProcedure() {
          public boolean execute(final int state, int value)
          {
            if (value == 0) {
              System.out.println("zero value split");
              info.remove(state);
              return true;
            }
            final int value1 = info1.get(state);
            final SimpleEquivalenceClass sec = mStateToClass[state];
            if (!sec.mSplit) {
              classes.add(sec);
              sec.mSplit = true;
            }
            if (value == value1) {
              TIntArrayList X1 = sec.mX1;
              if (X1 == null) {
                X1 = new TIntArrayList();
                sec.mX1 = X1;
              }
              X1.add(state);
            } else if (value1 == 0) {
              TIntArrayList X2 = sec.mX2;
              if (X2 == null) {
                X2 = new TIntArrayList();
                sec.mX2 = X2;
              }
              X2.add(state);
            } else {
              TIntArrayList X3 = sec.mX3;
              if (X3 == null) {
                X3 = new TIntArrayList();
                sec.mX3 = X3;
              }
              X3.add(state);
            }
            value -= value1;
            if (value != 0) {
              process.put(state, value);
            }
            return true;
          }
        });
        mChild2.mInfo[e] = process;
        for (int c = 0; c < classes.size(); c++) {
          final SimpleEquivalenceClass sec = classes.get(c);
          int[] X1, X2, X3;
          int number = sec.mX1 != null ? 1 : 0;
          number = sec.mX2 != null ? number + 1 : number;
          number = sec.mX3 != null ? number + 1 : number;
          /*
           * System.out.println("number:" + number); System.out.println("X1:" +
           * (sec.X1 != null) + " X2:" + (sec.X2 != null) + " X3:" + (sec.X3 !=
           * null)); X1 = sec.X1 == null ? null : sec.X1.toNativeArray(); X2 =
           * sec.X2 == null ? null : sec.X2.toNativeArray(); X3 = sec.X3 == null
           * ? null : sec.X3.toNativeArray(); System.out.println("X1:" +
           * Arrays.toString(X1)); System.out.println("X2:" +
           * Arrays.toString(X2)); System.out.println("X3:" +
           * Arrays.toString(X3)); System.out.println("X:" +
           * Arrays.toString(sec.mStates));
           */
          if (number == 2) {
            X1 = sec.mX1 == null ? null : sec.mX1.toNativeArray();
            X2 = sec.mX2 == null ? null : sec.mX2.toNativeArray();
            X3 = sec.mX3 == null ? null : sec.mX3.toNativeArray();
            if (X1 == null) {
              X1 = X3;
            } else if (X2 == null) {
              X2 = X3;
            }
            addToW(sec, X1, X2);
          } else if (number == 3) {
            X1 = sec.mX1.toNativeArray();
            X2 = sec.mX2.toNativeArray();
            X3 = sec.mX3.toNativeArray();
            sec.mSplit = false;
            addToW(sec, X1, X2, X3);
          }
          sec.mX1 = null;
          sec.mX2 = null;
          sec.mX3 = null;
          sec.mSplit = false;
        }
        classes.clear();
      }
      // mChild2.mInfo = mInfo;
      mInfo = null;
      mChild1.mParent = null;
      mChild2.mParent = null;
      if (mChild1 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild1);
      }
      if (mChild2 instanceof ComplexEquivalenceClass) {
        mWC.add((ComplexEquivalenceClass) mChild2);
      }
    }

    public TIntIntHashMap getInfo(final int event)
    {
      if (mInfo == null) {
        mInfo = new TIntIntHashMap[mNumEvents];
      }
      TIntIntHashMap res = mInfo[event];
      if (res != null) {
        return res;
      }
      TIntIntHashMap info1 = mChild1.getInfo(event);
      TIntIntHashMap info2 = mChild2.getInfo(event);
      if (info1.size() < info2.size()) {
        final TIntIntHashMap t = info1;
        info1 = info2;
        info2 = t;
      }
      final TIntIntHashMap info = new TIntIntHashMap(info1.size());
      info1.forEachEntry(new TIntIntProcedure() {
        public boolean execute(final int state, final int value)
        {
          info.put(state, value);
          return true;
        }
      });
      info2.forEachEntry(new TIntIntProcedure() {
        public boolean execute(final int state, final int value)
        {
          info.adjustOrPutValue(state, value, value);
          return true;
        }
      });
      res = info;
      mInfo[event] = res;
      return res;
    }
  }


  // #########################################################################
  // # Inner Class TauClosureTransitionIterator
  private class TauClosureTransitionIterator implements TransitionIterator
  {

    // #######################################################################
    // # Constructor
    private TauClosureTransitionIterator()
    {
      mTarget = -1;
      mTauPreds = EMPTY_ARRAY;
      mIndex = -1;
    }

    private TauClosureTransitionIterator(final int target)
    {
      reset(target);
    }

    // #######################################################################
    // # Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauPreds = ObservationEquivalenceTRSimplifier.this.mTauPreds[mTarget];
      mIndex = -1;
    }

    public void reset(final int from)
    {
      mTarget = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      if (event == EventEncoding.TAU) {
        reset(from);
      } else {
        throw new UnsupportedOperationException(ProxyTools
            .getShortClassName(this)
            + " only iterates with tau event!");
      }
    }

    public boolean advance()
    {
      return ++mIndex < mTauPreds.length;
    }

    public int getCurrentEvent()
    {
      return EventEncoding.TAU;
    }

    public int getCurrentSourceState()
    {
      return getCurrentToState();
    }

    public int getCurrentToState()
    {
      if (mIndex < mTauPreds.length) {
        return mTauPreds[mIndex];
      } else {
        throw new NoSuchElementException("Reading past end of list in "
            + ProxyTools.getShortClassName(this));
      }
    }

    public int getCurrentTargetState()
    {
      return mTarget;
    }

    public int getCurrentFromState()
    {
      return mTarget;
    }

    public void remove()
    {
      throw new UnsupportedOperationException(ProxyTools
          .getShortClassName(this)
          + " does not support transition removal!");
    }

    // #########################################################################
    // # Data Members
    private int mTarget;
    private int[] mTauPreds;
    private int mIndex;

  }


  // #########################################################################
  // # Inner Class ProperEventClosureTransitionIterator
  private class ProperEventClosureTransitionIterator implements
      TransitionIterator
  {

    // #######################################################################
    // # Constructor
    private ProperEventClosureTransitionIterator()
    {
      mInnerIterator = mTransitionRelation.createPredecessorsReadOnlyIterator();
      mVisited = new TIntHashSet();
      reset();
    }

    private ProperEventClosureTransitionIterator(final int target,
                                                 final int event)
    {
      mInnerIterator = mTransitionRelation.createPredecessorsReadOnlyIterator();
      mVisited = new TIntHashSet();
      reset(target, event);
    }

    // #######################################################################
    // # Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauPreds1 = mTauPreds[mTarget];
      mTauPreds2 = EMPTY_ARRAY;
      mIndex1 = mIndex2 = -1;
      mVisited.clear();
    }

    public void reset(final int from)
    {
      mTarget = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mEvent = event;
      reset(from);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mTauPreds2[mIndex2];
        if (mVisited.add(state)) {
          return true;
        }
      }
      reset();
      return false;
    }

    public int getCurrentEvent()
    {
      return mEvent;
    }

    public int getCurrentSourceState()
    {
      return getCurrentToState();
    }

    public int getCurrentToState()
    {
      if (mIndex2 < mTauPreds2.length) {
        return mTauPreds2[mIndex2];
      } else {
        throw new NoSuchElementException("Reading past end of list in "
            + ProxyTools.getShortClassName(this));
      }
    }

    public int getCurrentTargetState()
    {
      return mTarget;
    }

    public int getCurrentFromState()
    {
      return mTarget;
    }

    public void remove()
    {
      throw new UnsupportedOperationException(ProxyTools
          .getShortClassName(this)
          + " does not support transition removal!");
    }

    // #########################################################################
    // # Auxiliary Methods
    private boolean seek()
    {
      if (++mIndex2 < mTauPreds2.length) {
        return true;
      } else {
        while (mIndex1 < 0 || !mInnerIterator.advance()) {
          if (++mIndex1 >= mTauPreds1.length) {
            return false;
          }
          final int taupred1 = mTauPreds1[mIndex1];
          mInnerIterator.reset(taupred1, mEvent);
        }
        final int taupred2 = mInnerIterator.getCurrentSourceState();
        mTauPreds2 = mTauPreds[taupred2];
        mIndex2 = 0;
        return true;
      }
    }

    // #########################################################################
    // # Data Members
    private int mTarget;
    private int mEvent;

    private int[] mTauPreds1;
    private int mIndex1;
    private final TransitionIterator mInnerIterator;
    private int[] mTauPreds2;
    private int mIndex2;
    private final TIntHashSet mVisited;

  }

  // #########################################################################
  // # Logging
  private Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }

  // #########################################################################
  // # Data Members
  private ListBufferTransitionRelation mTransitionRelation;
  private Collection<int[]> mInitialPartition;
  private boolean mSuppressRedundantHiddenTransitions;
  private int mTransitionLimit;

  private int mNumStates;
  private int mNumEvents;

  private int[][] mTauPreds;
  private THashSet<SimpleEquivalenceClass> mWS;
  private THashSet<ComplexEquivalenceClass> mWC;
  private THashSet<SimpleEquivalenceClass> mP;
  private SimpleEquivalenceClass[] mStateToClass;
  private List<int[]> mResultPartition;
  private boolean mHasModifications;

  // #########################################################################
  // # Class Constants
  private static final int[] EMPTY_ARRAY = new int[0];

}
