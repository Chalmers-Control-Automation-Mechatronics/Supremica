//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntArrayHashingStrategy;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersHashSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * A Java implementation of the monolithic synchronous product algorithm. This
 * implementation supports nondeterministic automata and hiding. States are
 * stored in integer arrays without compression, so it is not recommended to
 * use this implementation to compose a large number of automata.
 *
 * @author Robi Malik, fq11
 */

public class MonolithicSynthesizer extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public MonolithicSynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public MonolithicSynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  //#########################################################################
  //# Configuration
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mSupervisorReductionEnabled = enable;
  }

  public boolean getSupervisorReductionEnabled()
  {
    return mSupervisorReductionEnabled;
  }

  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mSupervisorLocalizationEnabled = enable;
  }

  public boolean getSupervisorLocalizationEnabled()
  {
    return mSupervisorLocalizationEnabled;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSupervisorSimplifier != null) {
      mSupervisorSimplifier.requestAbort();
    }
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  @Override
  public Collection<EventProxy> getPropositions()
  {
    return mUsedPropositions;
  }

  @Override
  public void setPropositions(final Collection<EventProxy> props)
  {
    mUsedPropositions = props;
  }

  public void setConfiguredDefaultMarking(EventProxy marking)
    throws EventNotFoundException
  {
    if (marking == null) {
      final ProductDESProxy model = getModel();
      marking = AbstractConflictChecker.getMarkingProposition(model);
    }
    final Collection<EventProxy> props = Collections.singletonList(marking);
    setPropositions(props);
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      // initial search
      while (!mGlobalStack.isEmpty()) {
        final int[] sentinel = new int[1];
        final int[] encodedRoot = mGlobalStack.pop();
        final int r = mGlobalVisited.get(encodedRoot);
        if (mSafeStates.get(r) || mBadStates.get(r)) {
          continue;
        }
        mLocalStack.push(encodedRoot);
        mLocalVisited.add(encodedRoot);
        mBackTrace.push(sentinel);
        boolean safe = true;
        while (!mLocalStack.isEmpty()) {
          if (mLocalStack.peek() == mBackTrace.peek()) {
            mLocalStack.pop();
            mBackTrace.pop();
          } else {
            final int[] current = mLocalStack.peek();
            mBackTrace.push(current);
            safe = mUnctrlInitialReachabilityExplorer.explore(current);
            if (!safe) {
              break;
            }
          }
        }
        if (safe) {
          for (final int[] current : mLocalVisited) {
            final int n = addEncodedNewState(current);
            mSafeStates.set(n);
          }
          for (final int[] current : mLocalVisited) {
            mCtrlInitialReachabilityExplorer.explore(current);
          }
        } else {
          for (final int[] current : mBackTrace) {
            if (current == sentinel) {
              continue;
            } else {
              int n = 0;
              if (mGlobalVisited.containsKey(current)) {
                n = mGlobalVisited.get(current);
                if (n < mNumInitialStates) {
                  return setBooleanResult(false);
                }
              } else {
                n = addEncodedNewState(current);
              }
              mBadStates.set(n);
            }
          }
        }
        mLocalStack.clear();
        mLocalVisited.clear();
        mBackTrace.clear();
      }

      mMustContinue = false;
      do {
        // mark non-coreachable states (trim)
        mNonCoreachableStates.set(0, mNumStates);
        tuples: for (int t = 0; t < mNumStates; t++) {
          if (!mBadStates.get(t)) {
            final int[] tuple = new int[mNumAutomata];
            final int[] encodedTuple = mStateTuples.get(t);
            decode(encodedTuple, tuple);
            for (int aut = 0; aut < tuple.length; aut++) {
              if (mStateMarkings[aut][tuple[aut]].isEmpty()) {
                continue tuples;
              }
            }
            mUnvisited.add(encodedTuple);
            mNonCoreachableStates.set(t, false);
            while (!mUnvisited.isEmpty()) {
              final int[] s = mUnvisited.remove();
              mCoreachabilityExplorer.explore(s);
            }
          }
        }

        for (int b = 0; b < mNumInitialStates; b++) {
          if (!mBadStates.get(b) && mNonCoreachableStates.get(b)) {
            initialIsBad = true;
          }
        }
        // mark uncontrollable states
        mMustContinue = false;
        mUnvisited.clear();
        for (int state = 0; state < mNumStates; state++) {
          if (!mBadStates.get(state) && mNonCoreachableStates.get(state)) {
            mBadStates.set(state);
            mUnvisited.offer(mStateTuples.get(state));
            while (!mUnvisited.isEmpty()) {
              final int[] s = mUnvisited.remove();
              mSuccessorStatesExplorer.explore(s);
            }
          }
        }
      } while (mMustContinue);

      // reachability search (count the number of reachable states)
      mNumGoodStates = 0;
      mUnvisited.clear();
      mReachabilityExplorer.permutations(mNumAutomata, null, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] s = mUnvisited.remove();
        if (mGlobalVisited.containsKey(s)) {
          mReachabilityExplorer.explore(s);
        }
      }
      for (int b = 0; b < mNumInitialStates; b++) {
        if (!mReachableStates.get(b)) {
          initialIsBad = true;
        }
      }
      if (initialIsBad) {
        return setBooleanResult(false);
      }

      // re-encode states (make only one bad state)
      mTransitionRelation =
        new ListBufferTransitionRelation(
                                         "rel",
                                         ComponentKind.SUPERVISOR,
                                         mNumProperEvents + 1,
                                         mCurrentPropositions.size(),
                                         mNumGoodStates + 1,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mStateMap = new int[mNumStates];
      int index = 0;
      final int[] tuple = new int[mNumAutomata];
      for (int i = 0; i < mNumStates; i++) {
        if (mReachableStates.get(i)) {
          mStateMap[i] = index++;
          decode(mStateTuples.get(i), tuple);
          props: for (final EventProxy prop : mCurrentPropositions) {
            for (int a = 0; a < mNumAutomata; a++) {
              final List<EventProxy> stateMarking =
                getStateMarking(a, tuple[a]);
              if (!stateMarking.contains(prop)) {
                continue props;
              }
            }
            mTransitionRelation.setMarked(index - 1, mEventToIndex.get(prop),
                                          true);
          }
          if (i < mNumInitialStates) {
            mTransitionRelation.setInitial(index - 1, true);
          }
        } else {
          mStateMap[i] = mNumGoodStates;// the index of the bad state
        }
      }

      // final search (add transitions)
      mUnvisited.clear();
      mGoodStates = new BitSet();
      mFinalStateExplorer.permutations(mNumAutomata, null, -1);
      while (!mUnvisited.isEmpty()) {
        final int[] s = mUnvisited.remove();
        if (mGlobalVisited.containsKey(s)) {
          mFinalStateExplorer.explore(s);
        }
      }

      mStateTuples = null;
      mGlobalVisited = null;
      mNonCoreachableStates = null;
      mBadStates = null;
      mSafeStates = null;
      mReachableStates = null;

      // Write event status into TR
      for (int e = mNumUncontrollableEvents + 1; e <= mNumProperEvents; e++) {
        mTransitionRelation
          .setProperEventStatus(e, EventEncoding.STATUS_CONTROLLABLE);
      }

      if (getConstructsResult()) {
        ProductDESProxy des = null;
        if (mSupervisorReductionEnabled) {
          // Supervisor Reduction Enabled
          mSupervisorSimplifier.setTransitionRelation(mTransitionRelation);//set TR
          mSupervisorSimplifier.setEvent(-1);//set event
          mSupervisorSimplifier.setOutputName("Supervisor!!");//set name
          mSupervisorSimplifier.setBadStateIndex(mNumGoodStates);//set bad state
          mSupervisorSimplifier.setRetainedDumpStateEvents(null);//set retained transitions
          mSupervisorSimplifier.run();
          mTransitionRelation = mSupervisorSimplifier.getTransitionRelation();
          if (!mSupervisorLocalizationEnabled) {
            removeBadStateTransitions(mTransitionRelation,
                                      mSupervisorSimplifier
                                        .getBadStateIndex());
            removeSelfloops(mTransitionRelation);
            des = createDESProxy(mTransitionRelation);
          } else {
            // Supervisor Localization Enabled
            final TIntArrayList enabDisabEvents = new TIntArrayList();
            final TIntArrayList disabEvents = new TIntArrayList(0);
            mSupervisorSimplifier
              .setUpEventList(enabDisabEvents, disabEvents);
            final List<AutomatonProxy> autList =
              new ArrayList<AutomatonProxy>();
            boolean simplified = true;
            if (enabDisabEvents.size() == 0) {
              removeBadStateTransitions(mTransitionRelation,
                                        mSupervisorSimplifier
                                          .getBadStateIndex());
              removeSelfloops(mTransitionRelation);
              autList.add(mTransitionRelation
                .createAutomaton(getFactory(), getEventEncoding()));
            } else {
              for (int e = 0; e < enabDisabEvents.size(); e++) {
                ListBufferTransitionRelation copy =
                  new ListBufferTransitionRelation(
                                                   mTransitionRelation,
                                                   ListBufferTransitionRelation.CONFIG_SUCCESSORS);
                mSupervisorSimplifier.setTransitionRelation(copy);//set TR
                mSupervisorSimplifier.setEvent(enabDisabEvents.get(e));//set event
                mSupervisorSimplifier.setOutputName("Supervisor:"
                                                    + mEvents[enabDisabEvents
                                                      .get(e)].getName());//set name
                mSupervisorSimplifier.setBadStateIndex(mSupervisorSimplifier
                  .getBadStateIndex());//set bad state
                mSupervisorSimplifier
                  .setRetainedDumpStateEvents(new TIntHashSet());//set retained transitions
                simplified = simplified & mSupervisorSimplifier.run();
                if (!simplified) {
                  removeBadStateTransitions(mTransitionRelation,
                                            mSupervisorSimplifier
                                              .getBadStateIndex());
                  removeSelfloops(mTransitionRelation);
                  autList.add(mTransitionRelation
                    .createAutomaton(getFactory(), getEventEncoding()));
                  break;
                }
                copy = mSupervisorSimplifier.getTransitionRelation();
                autList.add(copy.createAutomaton(getFactory(),
                                                 getEventEncoding()));
              }
            }
            if (simplified) {
              final IsomorphismChecker checker =
                new IsomorphismChecker(getFactory(), false, false);
              final THashSet<AutomatonProxy> removeSet =
                new THashSet<AutomatonProxy>();
              for (int autom = 0; autom < autList.size() - 1; autom++) {
                for (int auto = autom + 1; auto < autList.size(); auto++) {
                  if (checker.checkBisimulation(autList.get(autom),
                                                autList.get(auto))) {
                    removeSet.add(autList.get(auto));
                  }
                }
              }
              for (int a = autList.size() - 1; a >= 0; a--) {
                if (removeSet.contains(autList.get(a))) {
                  removeSet.remove(autList.get(a));
                  autList.remove(a);
                }
              }
            }
            des =
              AutomatonTools.createProductDESProxy("Sup!!Local", autList,
                                                   getFactory());
          }
        } else {
          // Supervisor Reduction Not Enabled
          removeBadStateTransitions(mTransitionRelation, mNumGoodStates);
          mTransitionRelation.removeProperSelfLoopEvents();
          mTransitionRelation.removeRedundantPropositions();
          des = createDESProxy(mTransitionRelation);
        }
        return setProxyResult(des);
      } else {
        return true;
      }
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  //#########################################################################

  private void removeBadStateTransitions(final ListBufferTransitionRelation rel,
                                         final int badStateIndex)
  {
    final TransitionIterator iter =
      rel.createAllTransitionsModifyingIterator();
    while (iter.advance()) {
      final int to = iter.getCurrentTargetState();
      if (to == badStateIndex) {
        iter.remove();
      }
    }
    rel.setReachable(badStateIndex, false);
  }

  private void removeSelfloops(final ListBufferTransitionRelation rel)
  {
    for (int e = 1; e <= mNumUncontrollableEvents; e++) {
      final TransitionIterator iter =
        rel.createAllTransitionsReadOnlyIterator(e);
      boolean isSelfloopOnly = true;
      while (iter.advance()) {
        if (iter.getCurrentSourceState() != iter.getCurrentTargetState()) {
          isSelfloopOnly = false;
          break;
        }
      }
      if (isSelfloopOnly) {
        rel.removeEvent(e);
      }
    }
    rel.removeProperSelfLoopEvents();
  }

  //#########################################################################
  //# Callbacks
  @SuppressWarnings("unchecked")
  public final List<EventProxy> getStateMarking(final int aut, final int state)
  {
    return (List<EventProxy>) mStateMarkings[aut][state];
  }

  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    int[][][][] transitions;
    int[][][][] reverseTransitions;
    int[][] eventAutomata;
    int[][] reverseEventAutomata;
    int[][] ndTuple1;
    int[][] ndTuple2;
    int[][] ndTuple3;

    super.setUp();

    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    mNumEvents = events.size();

    final KindTranslator translator = getKindTranslator();

    ArrayList<AutomatonProxy> plants = new ArrayList<AutomatonProxy>();
    ArrayList<AutomatonProxy> specs = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        plants.add(aut);
      } else if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
        specs.add(aut);
      }
    }
    mNumAutomata = plants.size() + specs.size();
    mNumPlants = plants.size();
    mAutomata = new ArrayList<AutomatonProxy>(mNumAutomata);
    mAutomata.addAll(plants);
    mAutomata.addAll(specs);
    plants = specs = null;

    mEventToIndex = new TObjectIntHashMap<EventProxy>(mNumEvents + 1);
    if (mUsedPropositions == null) {
      mCurrentPropositions = new ArrayList<EventProxy>();
    } else {
      mCurrentPropositions = mUsedPropositions;
    }

    int numProperEvents = 0;
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        numProperEvents += 1;
      }
    }
    int unctrlEvents = 1;
    int ctrlEvents = numProperEvents;
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) == EventKind.PROPOSITION) {
        if (mUsedPropositions == null) {
          mCurrentPropositions.add(event);
        }
      } else if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        mEventToIndex.put(event, unctrlEvents++);
      } else if (translator.getEventKind(event) == EventKind.CONTROLLABLE) {
        mEventToIndex.put(event, ctrlEvents--);
      }
    }
    mNumProperEvents = numProperEvents;
    mNumUncontrollableEvents = unctrlEvents - 1;
    mCurrentDeadlock = new boolean[mNumProperEvents + 1];
    mEvents = new EventProxy[mNumProperEvents + 1];
    final TObjectIntIterator<EventProxy> iter = mEventToIndex.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final EventProxy event = iter.key();
      final int e = iter.value();
      mEvents[e] = event;
    }
    final int numProps = mCurrentPropositions.size();
    mOriginalStates = new StateProxy[mNumAutomata][];
    mAllMarkings = new HashMap<List<EventProxy>,List<EventProxy>>();
    mStateMarkings = new List<?>[mNumAutomata][];
    mDeadlock = new boolean[mNumAutomata][];
    mTargetTuple = new int[mNumAutomata];
    mDeadlockState = -1;

    //transitions indexed first by automaton then by event then by source state
    transitions = new int[mNumAutomata][mNumProperEvents + 1][][];
    reverseTransitions = new int[mNumAutomata][mNumProperEvents + 1][][];
    eventAutomata = new int[mNumProperEvents + 1][];
    reverseEventAutomata = new int[mNumProperEvents + 1][];
    ndTuple1 = new int[mNumAutomata][];

    int a = 0;
    for (final AutomatonProxy aut : mAutomata) {
      final Collection<EventProxy> localEvents = aut.getEvents();
      final List<EventProxy> nonLocalProps =
        new ArrayList<EventProxy>(numProps);
      for (final EventProxy prop : mCurrentPropositions) {
        if (!localEvents.contains(prop)) {
          nonLocalProps.add(prop);
        }
      }
      Collections.sort(nonLocalProps);
      final Collection<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      final TObjectIntHashMap<StateProxy> stateToIndex =
        new TObjectIntHashMap<StateProxy>(numStates);
      final TIntArrayList initials = new TIntArrayList(1);
      int snum = 0;
      mOriginalStates[a] = new StateProxy[numStates];
      mStateMarkings[a] = new List<?>[numStates];
      mDeadlock[a] = new boolean[numStates];

      for (final StateProxy state : states) {
        stateToIndex.put(state, snum);
        mOriginalStates[a][snum] = state;
        if (state.isInitial()) {
          initials.add(snum);
        }
        final Collection<EventProxy> props = state.getPropositions();
        final List<EventProxy> stateProps;
        if (props.isEmpty()) {
          stateProps = nonLocalProps;
        } else {
          stateProps = new ArrayList<EventProxy>(numProps + props.size());
          stateProps.addAll(nonLocalProps);
          for (final EventProxy prop : props) {
            if (mCurrentPropositions.contains(prop)) {
              stateProps.add(prop);
            }
          }
          Collections.sort(stateProps);
        }
        mStateMarkings[a][snum] = getUniqueMarking(stateProps);
        mDeadlock[a][snum] = stateProps.isEmpty();
        snum++;
      }
      ndTuple1[a] = initials.toArray();
      final TIntArrayList[][] autTransitionLists =
        new TIntArrayList[mNumProperEvents + 1][numStates];
      final TIntArrayList[][] autTransitionListsRvs =
        new TIntArrayList[mNumProperEvents + 1][numStates];
      for (final TransitionProxy trans : aut.getTransitions()) {
        final int event = mEventToIndex.get(trans.getEvent());
        final int source = stateToIndex.get(trans.getSource());
        final int target = stateToIndex.get(trans.getTarget());
        TIntArrayList list = autTransitionLists[event][source];
        TIntArrayList listRvs = autTransitionListsRvs[event][target];
        if (list == null) {
          list = new TIntArrayList(1);
          autTransitionLists[event][source] = list;
        }
        list.add(target);
        if (source != target) {
          mDeadlock[a][source] = false;
        }
        if (listRvs == null) {
          listRvs = new TIntArrayList(1);
          autTransitionListsRvs[event][target] = listRvs;
        }
        listRvs.add(source);
      }
      for (final EventProxy event : localEvents) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION) {
          final int e = mEventToIndex.get(event);
          transitions[a][e] = new int[numStates][];
          reverseTransitions[a][e] = new int[numStates][];
          for (int source = 0; source < numStates; source++) {
            final TIntArrayList list = autTransitionLists[e][source];
            if (list != null) {
              transitions[a][e][source] = list.toArray();
            }
          }
          for (int target = 0; target < numStates; target++) {
            final TIntArrayList listRvs = autTransitionListsRvs[e][target];
            if (listRvs != null) {
              reverseTransitions[a][e][target] = listRvs.toArray();
            }
          }
        }
      }
      a++;
    }
    eventAutomata = new int[mNumProperEvents + 1][];
    reverseEventAutomata = new int[mNumProperEvents + 1][];
    final List<AutomatonEventInfo> list =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    final List<AutomatonEventInfo> listRvs =
      new ArrayList<AutomatonEventInfo>(mNumAutomata);
    for (int e = 1; e <= mNumProperEvents; e++) {
      for (a = 0; a < mNumAutomata; a++) {
        if (transitions[a][e] != null) {
          final int numStates = transitions[a][e].length;
          int count = 0;
          for (int source = 0; source < numStates; source++) {
            if (transitions[a][e][source] != null) {
              count++;
            }
          }
          final double avg = (double) count / (double) numStates;
          if (e > mNumUncontrollableEvents) {
            final ControllableAutomatonEventInfo pair =
              new ControllableAutomatonEventInfo(a, avg);
            list.add(pair);
          } else {
            final UncontrollableAutomatonEventInfo pair =
              new UncontrollableAutomatonEventInfo(a, avg);
            list.add(pair);
          }
        }
        if (reverseTransitions[a][e] != null) {
          final int numStates = reverseTransitions[a][e].length;
          int countRvs = 0;
          for (int target = 0; target < numStates; target++) {
            if (reverseTransitions[a][e][target] != null) {
              countRvs++;
            }
          }
          final double avgRvs = (double) countRvs / (double) numStates;
          final ControllableAutomatonEventInfo pairRvs =
            new ControllableAutomatonEventInfo(a, avgRvs);
          listRvs.add(pairRvs);
        }
      }
      Collections.sort(list);
      Collections.sort(listRvs);
      final int count = list.size();
      final int countRvs = listRvs.size();
      eventAutomata[e] = new int[count];
      reverseEventAutomata[e] = new int[countRvs];
      int i = 0;
      for (final AutomatonEventInfo info : list) {
        eventAutomata[e][i++] = info.getAutomaton();
      }
      int j = 0;
      for (final AutomatonEventInfo info : listRvs) {
        reverseEventAutomata[e][j++] = info.getAutomaton();
      }
      list.clear();
      listRvs.clear();
    }

    final int tableSize = Math.min(getNodeLimit(), MAX_TABLE_SIZE);
    mNonCoreachableStates = new BitSet(mNumStates);
    mReachableStates = new BitSet(mNumStates);
    mBadStates = new BitSet(mNumStates);
    mSafeStates = new BitSet(mNumStates);
    final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
    mGlobalVisited = new TObjectIntCustomHashMap<int[]>(strategy);
    mGlobalVisited.ensureCapacity(tableSize);
    mLocalVisited = new WatersHashSet<int[]>(strategy);
    mGlobalStack = new ArrayDeque<int[]>();
    mLocalStack = new ArrayDeque<int[]>();
    mBackTrace = new ArrayDeque<int[]>();
    mStateTuples = new ArrayList<int[]>();
    mTransitionBuffer = new TIntArrayList();
    mTransitionBufferLimit = 3 * getTransitionLimit();
    mUnvisited = new ArrayDeque<int[]>(100);
    mNumStates = 0;

    // get encoding information
    mNumBits = new int[mNumAutomata];
    mNumBitsMasks = new int[mNumAutomata];

    // get mNumBits
    mNumInts = 1;
    int totalBits = SIZE_INT;
    int counter = 0;
    for (int aut = 0; aut < mNumAutomata; aut++) {
      final int bits = AutomatonTools.log2(mOriginalStates[aut].length);
      mNumBits[counter] = bits;
      mNumBitsMasks[counter] = (1 << bits) - 1;
      if (totalBits >= bits) { // if current buffer can store this automaton
        totalBits -= bits;
      } else {
        mNumInts++;
        totalBits = SIZE_INT - bits;
      }
      counter++;
    }

    // get index
    counter = 0;
    totalBits = SIZE_INT;
    mIndexAutomata = new int[mNumInts + 1];
    mIndexAutomata[0] = counter++;
    for (int i = 0; i < mNumAutomata; i++) {
      if (totalBits >= mNumBits[i]) {
        totalBits -= mNumBits[i];
      } else {
        mIndexAutomata[counter++] = i;
        totalBits = SIZE_INT - mNumBits[i];
      }
    }
    mIndexAutomata[mNumInts] = mNumAutomata;

    mCtrlInitialReachabilityExplorer =
      new CtrlInitialReachabilityExplorer(eventAutomata, transitions,
                                          ndTuple1,
                                          mNumUncontrollableEvents + 1,
                                          mNumProperEvents);

    mCtrlInitialReachabilityExplorer.permutations(mNumAutomata, null, -1);
    ndTuple2 = Arrays.copyOf(ndTuple1, mNumAutomata);
    ndTuple3 = Arrays.copyOf(ndTuple1, mNumAutomata);
    mNumInitialStates = mNumStates;

    mUnctrlInitialReachabilityExplorer =
      new UnctrlInitialReachabilityExplorer(eventAutomata, transitions,
                                            ndTuple1, 1,
                                            mNumUncontrollableEvents);
    mCoreachabilityExplorer =
      new CoreachabilityExplorer(reverseEventAutomata, reverseTransitions,
                                 ndTuple1, 1, mNumProperEvents);
    mSuccessorStatesExplorer =
      new SuccessorStatesExplorer(reverseEventAutomata, reverseTransitions,
                                  ndTuple1, 1, mNumUncontrollableEvents);
    mReachabilityExplorer =
      new ReachabilityExplorer(eventAutomata, transitions, ndTuple2, 1,
                               mNumProperEvents);
    mFinalStateExplorer =
      new FinalStateExplorer(eventAutomata, transitions, ndTuple3, 1,
                             mNumProperEvents);
    mSupervisorSimplifier = new SupervisorReductionTRSimplifier();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final ProxyResult<ProductDESProxy> result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setNumberOfStates(mNumStates);
    if (mTransitionBuffer != null) {
      result.setNumberOfTransitions(mTransitionBuffer.size() / 3);
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEvents = null;
    mCurrentPropositions = null;
    mOriginalStates = null;
    mAllMarkings = null;
    mStateMarkings = null;
    mDeadlock = null;
    mGlobalVisited = null;
    mStateTuples = null;
    mUnvisited = null;
    mTransitionBuffer = null;
    mTargetTuple = null;
    mCurrentDeadlock = null;
  }

  //#########################################################################
  //# Auxiliary Methods

  /**
   * It will take a single state tuple as a parameter and encode it.
   *
   * @param stateCodes
   *          state tuple that will be encoded
   * @return encoded state tuple
   */
  private int[] encode(final int[] stateCodes)
  {
    final int encoded[] = new int[mNumInts];
    int i, j;
    for (i = 0; i < mNumInts; i++) {
      for (j = mIndexAutomata[i]; j < mIndexAutomata[i + 1]; j++) {
        encoded[i] <<= mNumBits[j];
        encoded[i] |= stateCodes[j];
      }
    }
    return encoded;
  }

  /**
   * It will take an encoded state tuple as a parameter and decode it. Decoded
   * result will be contained in the second parameter
   *
   * @param encodedStateCodes
   *          state tuple that will be decoded
   * @param currTuple
   *          the decoded state tuple will be stored here
   */
  private void decode(final int[] encodedStateCodes, final int[] currTuple)
  {
    int tmp, mask, i, j;
    for (i = 0; i < mNumInts; i++) {
      tmp = encodedStateCodes[i];
      for (j = mIndexAutomata[i + 1] - 1; j >= mIndexAutomata[i]; j--) {
        mask = mNumBitsMasks[j];
        currTuple[j] = tmp & mask;
        tmp = tmp >>> mNumBits[j];
      }
    }
  }

  private int addDecodedNewState(final int[] decodedTuple)
    throws OverflowException
  {
    final int[] encoded = encode(decodedTuple);
    return addEncodedNewState(encoded);
  }

  private int addEncodedNewState(final int[] encodedTuple)
    throws OverflowException
  {
    if (mGlobalVisited.containsKey(encodedTuple)) {
      return mGlobalVisited.get(encodedTuple);
    } else {
      final int code = mNumStates++;
      final int limit = getNodeLimit();
      if (mNumStates >= limit) {
        throw new OverflowException(limit);
      }
      mGlobalVisited.put(encodedTuple, code);
      mStateTuples.add(encodedTuple);
      return code;
    }
  }

  private EventEncoding getEventEncoding()
  {
    final Collection<EventProxy> events = new ArrayList<EventProxy>();
    for (int i = 0; i < mEvents.length; i++) {
      events.add(mEvents[i]);
    }
    events.addAll(mCurrentPropositions);
    return new EventEncoding(events, getKindTranslator());
  }

  private List<EventProxy> getUniqueMarking(final List<EventProxy> marking)
  {
    final List<EventProxy> found = mAllMarkings.get(marking);
    if (found == null) {
      mAllMarkings.put(marking, marking);
      return marking;
    } else {
      return found;
    }
  }

  private ProductDESProxy createDESProxy(final ListBufferTransitionRelation rel)
  {
    final AutomatonProxy aut =
      rel.createAutomaton(getFactory(), getEventEncoding());
    return AutomatonTools.createProductDESProxy(aut, getFactory());
  }


  //#########################################################################
  //# Inner Class MemStateProxy
  /**
   * Stores states, encoding the name as an int rather than a long string
   * value.
   */
  @SuppressWarnings("unused")
  private static class MemStateProxy implements StateProxy
  {
    //#######################################################################
    //# Constructor
    private MemStateProxy(final int name, final Collection<EventProxy> props,
                          final boolean isInitial)
    {
      mName = name;
      mProps = props;
      mIsInitial = isInitial;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.StateProxy
    @Override
    public Collection<EventProxy> getPropositions()
    {
      return mProps;
    }

    @Override
    public boolean isInitial()
    {
      return mIsInitial;
    }

    @Override
    public MemStateProxy clone()
    {
      return new MemStateProxy(mName, mProps, mIsInitial);
    }

    public int getCode()
    {
      return mName;
    }

    @Override
    public String getName()
    {
      return "S:" + mName;
    }

    @Override
    public boolean refequals(final NamedProxy o)
    {
      if (o instanceof MemStateProxy) {
        final MemStateProxy s = (MemStateProxy) o;
        return s.mName == mName;
      } else {
        return false;
      }
    }

    @Override
    public int refHashCode()
    {
      return mName;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
        (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    @Override
    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    @Override
    public int compareTo(final NamedProxy n)
    {
      return n.getName().compareTo(getName());
    }

    @Override
    public String toString()
    {
      return getName();
    }

    //#######################################################################
    //# Data Members
    private final int mName;
    private final boolean mIsInitial;
    private final Collection<EventProxy> mProps;
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  private abstract class AutomatonEventInfo implements
    Comparable<AutomatonEventInfo>
  {
    public AutomatonEventInfo(final int aut, final double probability)
    {
      mAut = aut;
      mProbability = probability;
    }

    protected int getAutomaton()
    {
      return mAut;
    }

    protected double getProbability()
    {
      return mProbability;
    }

    private final int mAut;
    private final double mProbability;
  }


  //#########################################################################
  //# Inner Class UncontrollableAutomatonEventInfo
  private class UncontrollableAutomatonEventInfo extends AutomatonEventInfo
  {
    public UncontrollableAutomatonEventInfo(final int aut,
                                            final double probability)
    {
      super(aut, probability);
    }

    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getAutomaton() < mNumPlants && this.getProbability() == 1.0f) {
        return 1;
      } else if (info.mAut < mNumPlants && info.mProbability == 1.0f) {
        return -1;
      } else if ((this.getAutomaton() < mNumPlants && info.mAut < mNumPlants)
                 || (this.getAutomaton() >= mNumPlants && info.mAut >= mNumPlants)) {
        return (this.getProbability() < info.mProbability) ? -1 : 1;
      } else {
        return (this.getAutomaton() < mNumPlants) ? -1 : 1;
      }
    }
  }


  //#########################################################################
  //# Inner Class ControllableAutomatonEventInfo
  private class ControllableAutomatonEventInfo extends AutomatonEventInfo
  {
    public ControllableAutomatonEventInfo(final int aut,
                                          final double probability)
    {
      super(aut, probability);
    }

    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (this.getProbability() < info.getProbability()) {
        return -1;
      } else if (this.getProbability() < info.getProbability()) {
        return 1;
      } else {
        return 0;
      }
    }
  }


  //#########################################################################
  //# Inner Class StateExplorer
  private abstract class StateExplorer
  {
    public StateExplorer(final int[][] theEventAutomata,
                         final int[][][][] theTransitions,
                         final int[][] theNDTuple, final int theFirstEvent,
                         final int theLastEvent)
    {
      mmFirstEvent = theFirstEvent;
      mmLastEvent = theLastEvent;
      mmEventAutomata = theEventAutomata;
      mmTransitions = theTransitions;
      mmNDTuple = theNDTuple;
      mmDecodedTuple = new int[mNumAutomata];
    }

    public boolean explore(final int[] encodedTuple)
      throws OverflowException, AnalysisAbortException
    {
      checkAbort();
      decode(encodedTuple, mmDecodedTuple);
      events: for (int e = mmFirstEvent; e <= mmLastEvent; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              continue events;
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    public boolean permutations(int a, final int[] decodedSource,
                                final int event) throws OverflowException
    {
      if (a == 0) {
        if (!processNewState(decodedSource, event, decodedSource == null)) {
          return false;
        }
      } else {
        a--;
        final int[] codes = mmNDTuple[a];
        if (codes == null) {
          mTargetTuple[a] = decodedSource[a];
          if (!permutations(a, decodedSource, event)) {
            return false;
          }
        } else {
          for (int i = 0; i < codes.length; i++) {
            mTargetTuple[a] = codes[i];
            if (!permutations(a, decodedSource, event)) {
              return false;
            }
          }
        }
      }
      return true;
    }

    public abstract boolean processNewState(final int[] decodedSource,
                                            final int event,
                                            final boolean isInitial)
      throws OverflowException;

    protected final int[][] mmEventAutomata;
    protected final int[][][][] mmTransitions;
    protected final int[][] mmNDTuple;
    protected int mmFirstEvent;
    protected int mmLastEvent;
    protected int[] mmDecodedTuple;
  }


  //#########################################################################
  //# Inner Class CtrlInitialReachabilityExplorer
  private class CtrlInitialReachabilityExplorer extends StateExplorer
  {
    public CtrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                           final int[][][][] transitions,
                                           final int[][] NDTuple,
                                           final int firstEvent,
                                           final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int currentNumStates = mNumStates;
      final int t = addDecodedNewState(mTargetTuple);
      if (currentNumStates != mNumStates) {
        mGlobalStack.push(mStateTuples.get(t));
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class UnctrlInitialReachabilityExplorer
  private class UnctrlInitialReachabilityExplorer extends StateExplorer
  {
    public UnctrlInitialReachabilityExplorer(final int[][] eventAutomata,
                                             final int[][][][] transitions,
                                             final int[][] NDTuple,
                                             final int firstEvent,
                                             final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean explore(final int[] encodedTuple)
      throws OverflowException, AnalysisAbortException
    {
      checkAbort();
      decode(encodedTuple, mmDecodedTuple);
      events: for (int e = 1; e <= mNumUncontrollableEvents; e++) {
        Arrays.fill(mmNDTuple, null);
        for (final int a : mmEventAutomata[e]) {
          if (mmTransitions[a][e] != null) {
            final int[] succ = mmTransitions[a][e][mmDecodedTuple[a]];
            if (succ == null) {
              if (a >= mNumPlants) {
                return false;
              } else {
                continue events;
              }
            }
            mmNDTuple[a] = succ;
          }
        }
        if (!permutations(mNumAutomata, mmDecodedTuple, e)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int[] encoded = encode(mTargetTuple);
      if (mLocalVisited.contains(encoded)) {
        return true;
      } else if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (mBadStates.get(s)) {
          return false;
        } else if (mSafeStates.get(s)) {
          return true;
        }
      }
      mLocalVisited.add(encoded);
      mLocalStack.push(encoded);
      return true;
    }
  }


  //#########################################################################
  //# Inner Class CoreachabilityExplorer
  private class CoreachabilityExplorer extends StateExplorer
  {
    public CoreachabilityExplorer(final int[][] eventAutomata,
                                  final int[][][][] transitions,
                                  final int[][] NDTuple,
                                  final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = encode(mTargetTuple);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s) && mNonCoreachableStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mNonCoreachableStates.set(s, false);
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class SuccessorStatesExplorer
  private class SuccessorStatesExplorer extends StateExplorer
  {
    public SuccessorStatesExplorer(final int[][] eventAutomata,
                                   final int[][][][] transitions,
                                   final int[][] NDTuple,
                                   final int firstEvent, final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
    {
      final int[] encoded = encode(mTargetTuple);
      if (mGlobalVisited.containsKey(encoded)) {
        final int s = mGlobalVisited.get(encoded);
        if (!mBadStates.get(s)) {
          mUnvisited.offer(mStateTuples.get(s));
          mBadStates.set(s);
          mMustContinue = true;
        }
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class ReachabilityExplorer
  private class ReachabilityExplorer extends StateExplorer
  {
    public ReachabilityExplorer(final int[][] eventAutomata,
                                final int[][][][] transitions,
                                final int[][] NDTuple, final int firstEvent,
                                final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      final int target = mGlobalVisited.get(encode(mTargetTuple));
      if (!mBadStates.get(target) && !mReachableStates.get(target)) {
        mReachableStates.set(target);
        mUnvisited.offer(mStateTuples.get(target));
        mNumGoodStates++;
      }
      return true;
    }
  }


  //#########################################################################
  //# Inner Class FinalStateExplorer
  private class FinalStateExplorer extends StateExplorer
  {
    public FinalStateExplorer(final int[][] eventAutomata,
                              final int[][][][] transitions,
                              final int[][] NDTuple, final int firstEvent,
                              final int lastEvent)
    {
      super(eventAutomata, transitions, NDTuple, firstEvent, lastEvent);
    }

    @Override
    public boolean processNewState(final int[] decodedSource,
                                   final int event, final boolean isInitial)
      throws OverflowException
    {
      int source = 0;
      if (!isInitial) {
        source = mGlobalVisited.get(encode(decodedSource));
        source = mStateMap[source];
      }
      int target = mGlobalVisited.get(encode(mTargetTuple));
      if (mReachableStates.get(target) && !mGoodStates.get(target)) {
        mGoodStates.set(target);
        mUnvisited.offer(mStateTuples.get(target));
      }
      target = mStateMap[target];
      if (!isInitial) {
        mTransitionRelation.addTransition(source, event, target);
      }
      return true;
    }
  }

  //#########################################################################
  //# Debug
  @SuppressWarnings("unused")
  private int[] showTuple(final int[] encodedTuple)
  {
    final int[] tuple = new int[mNumAutomata];
    decode(encodedTuple, tuple);
    return tuple;
  }

  @SuppressWarnings("unused")
  private String showStateTuple(final int[] tuple)
  {
    String msg = "";
    for (int i = 0; i < tuple.length; i++) {
      final AutomatonProxy aut = mAutomata.get(i);
      final ComponentKind kind = getKindTranslator().getComponentKind(aut);
      final StateProxy state = mOriginalStates[i][tuple[i]];
      msg +=
        kind.toString() + " " + aut.getName() + " : [" + tuple[i] + "] "
          + state.getName() + "\n";
    }
    return msg;
  }

  //#########################################################################
  //# Data Members
  //# Variables used for encoding/decoding
  /** a list contains number of bits needed for each automaton */
  private int mNumBits[];
  /** a list contains masks needed for each automaton */
  private int mNumBitsMasks[];
  /** a number of integers used to encode synchronized state */
  private int mNumInts;
  /** an index of first automaton in each integer buffer */
  private int mIndexAutomata[];

  private List<AutomatonProxy> mAutomata;
  private EventProxy[] mEvents;
  private TObjectIntHashMap<EventProxy> mEventToIndex;
  private Collection<EventProxy> mCurrentPropositions;
  private Collection<EventProxy> mUsedPropositions;
  @SuppressWarnings("unused")
  private int mTransitionBufferLimit;
  private TIntArrayList mTransitionBuffer;

  private int mNumAutomata;
  private static int mNumPlants;
  private int mNumStates;
  private int mNumInitialStates;
  private int mNumEvents;
  private int mNumProperEvents;
  private int mNumUncontrollableEvents;

  private StateProxy[][] mOriginalStates;
  private Map<List<EventProxy>,List<EventProxy>> mAllMarkings;
  private List<?>[][] mStateMarkings;
  private boolean[][] mDeadlock;
  @SuppressWarnings("unused")
  private boolean[] mCurrentDeadlock;
  @SuppressWarnings("unused")
  private int mDeadlockState;

  private List<int[]> mStateTuples;
  private TObjectIntCustomHashMap<int[]> mGlobalVisited;
  private Deque<int[]> mGlobalStack;
  private Deque<int[]> mLocalStack;
  private Deque<int[]> mBackTrace;
  private Set<int[]> mLocalVisited;
  private Queue<int[]> mUnvisited;
  private int[] mTargetTuple;

  private BitSet mNonCoreachableStates;
  private BitSet mBadStates;
  private BitSet mSafeStates;
  private BitSet mReachableStates;
  private boolean initialIsBad;
  private boolean mMustContinue;

  private StateExplorer mCtrlInitialReachabilityExplorer;
  private StateExplorer mUnctrlInitialReachabilityExplorer;
  private StateExplorer mCoreachabilityExplorer;
  private StateExplorer mSuccessorStatesExplorer;
  private StateExplorer mReachabilityExplorer;
  private StateExplorer mFinalStateExplorer;
  private ListBufferTransitionRelation mTransitionRelation;

  private boolean mSupervisorReductionEnabled = false;
  private boolean mSupervisorLocalizationEnabled = false;

  @SuppressWarnings("unused")
  private int[] mStateToClass;
  @SuppressWarnings("unused")
  private IntListBuffer mClasses;
  @SuppressWarnings("unused")
  private int[] mShadowStateToClass;
  @SuppressWarnings("unused")
  private IntListBuffer mShadowClasses;
  private SupervisorReductionTRSimplifier mSupervisorSimplifier;

  private int mNumGoodStates;
  private BitSet mGoodStates;
  private int[] mStateMap;

  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;
  private static final int SIZE_INT = 32;
}