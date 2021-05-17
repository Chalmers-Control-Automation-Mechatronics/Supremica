//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntListBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.MemStateProxy;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.analysis.tr.WatersLongHashingStrategy;
import net.sourceforge.waters.analysis.tr.WatersLongIntHashMap;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
  public OPSearchAutomatonSimplifier(final ProductDESProxyFactory factory,
                                     final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public OPSearchAutomatonSimplifier(final ProductDESProxy model,
                                     final ProductDESProxyFactory factory,
                                     final KindTranslator translator)
  {
    super(model, factory);
    mKindTranslator = translator;
  }

  public OPSearchAutomatonSimplifier(final AutomatonProxy aut,
                                     final Collection<EventProxy> hidden,
                                     final ProductDESProxyFactory factory,
                                     final KindTranslator translator)
  {
    super(aut, factory);
    mKindTranslator = translator;
    mOperationMode = Mode.MINIMIZE;
    mHiddenEvents = new THashSet<EventProxy>(hidden);
  }


  //#########################################################################
  //# Configuration
  /**
   * Specifies whether OP-Verifier or OP-Search is used.
   * @param  mode   Either {@link Mode#MINIMIZE} (the default) or
   *                {@link Mode#VERIFY}.
   */
  public void setOperationMode(final Mode mode)
  {
    mOperationMode = mode;
  }

  /**
   * Gets the current operation mode, i.e., whether OP-Verifier or OP-Search
   * is used.
   */
  public Mode getOperationMode()
  {
    return mOperationMode;
  }

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

  /**
   * Specifies whether the output automaton should be made deterministic.
   * If non-null, any silent transitions in the output automaton will be
   * labelled by this event, possibly causing nondeterminism. If not set,
   * new events will be created for silent transitions in the output,
   * and nondeterminism will be resolved by creating different events if
   * needed.
   * @param  event  The event to be used for silent transitions in the output
   *                automaton, or <CODE>null</CODE>.
   */
  public void setOutputHiddenEvent(final EventProxy event)
  {
    mOutputHiddenEvent = event;
  }

  /**
   * Gets the event representing silent transitions in the output automaton,
   * or <CODE>null</CODE> if no such event is specified.
   * @see #setOutputHiddenEvent(EventProxy) setOutputHiddenEvent()
   */
  public EventProxy getOutputHiddenEvent()
  {
    return mOutputHiddenEvent;
  }

  public void setLogFile(final File logFile)
  {
    mLogFile = logFile;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run()
    throws AnalysisException
  {
    final Logger logger = LogManager.getLogger();
    try {
      if (logger.isDebugEnabled()) {
        final AutomatonProxy aut = getInputAutomaton();
        final String msg =
            "ENTER " + ProxyTools.getShortClassName(this) + ".run(): " +
            aut.getName() + " with " + aut.getStates().size() +
            " states and " + aut.getTransitions().size() + " transitions ...";
        logger.debug(msg);
        //MarshallingTools.saveModule(aut, "before.wmod");
      }
      setUp();
      dumpVerifierToLogFile("Initial OP-Verifier:");
      if (mOperationMode == Mode.VERIFY) {
        final boolean satisfied = mListBuffer.isEmpty(mPredecessorsOfDead);
        return setBooleanResult(satisfied);
      } else {
        mOPSearchPhase = true;
        while (!mListBuffer.isEmpty(mPredecessorsOfDead)) {
          checkAbort();
          doOPSearchStep();
        }
        final boolean force = needsMerge();
        return createReducedOutputAutomaton(force);
      }
    } finally {
      tearDown();
      if (logger.isDebugEnabled()) {
        String msg = "EXIT " + ProxyTools.getShortClassName(this) + ".run()";
        final PartitionedAutomatonResult result = getAnalysisResult();
        final AutomatonProxy aut =
          result == null ? null : result.getComputedAutomaton();
        if (aut != null) {
          msg += ": " + aut.getStates().size() + " states and " +
                 aut.getTransitions().size() + " transitions";
          //MarshallingTools.saveModule(aut, "after.wmod");
        }
        logger.debug(msg);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalsyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mListBuffer = new IntListBuffer();
    mReadOnlyIterator = mListBuffer.createReadOnlyIterator();
    mAltReadOnlyIterator = mListBuffer.createReadOnlyIterator();
    setUpAutomatonEncoding();
    setUpStronglyConnectedComponents();
    mOPSearchPhase = false;
    setUpVerifier();
    if (mHiddenEvents == null && getInputAutomaton() instanceof TRAutomatonProxy) {
      mHiddenEvents = new THashSet<>();
      final TRAutomatonProxy trAut = (TRAutomatonProxy) getInputAutomaton();
      final EventEncoding enc = trAut.getEventEncoding();
      for (final EventProxy event : trAut.getEvents()) {
        if (enc.getTauEvent().equals(event)) {
          mHiddenEvents.add(event);
        }
        final int code = enc.getEventCode(event);
        final byte status = enc.getProperEventStatus(code);
        if (EventStatus.isLocalEvent(status)) {
          mHiddenEvents.add(event);
        }
      }
    }
  }

  @Override
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
    mObservableSuccessor = null;
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
    mContainmentTestSet = null;
  }

  @Override
  public OPSearchAutomatonResult getAnalysisResult()
  {
    return (OPSearchAutomatonResult) super.getAnalysisResult();
  }

  @Override
  public OPSearchAutomatonResult createAnalysisResult()
  {
    return new OPSearchAutomatonResult();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    printer.flush();
    return writer.toString();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.model.options.Configurable
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage page)
  {
    final List<Option<?>> options = super.getOptions(page);
    page.append(options, ChainSimplifierFactory.
                OPTION_OPSearchAutomatonSimplifier_OperationMode);
    page.append(options, ChainSimplifierFactory.
                OPTION_OPSearchAutomatonSimplifier_HiddenEvents);
    page.append(options, ChainSimplifierFactory.
                OPTION_OPSearchAutomatonSimplifier_Propositions);
//    page.append(options, ChainSimplifierFactory.
//                OPTION_OPSearchAutomatonSimplifier_OutputHiddenEvent);
    page.append(options, ChainSimplifierFactory.
                OPTION_OPSearchAutomatonSimplifier_LogFile);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(ChainSimplifierFactory.
                     OPTION_OPSearchAutomatonSimplifier_OperationMode)) {
      @SuppressWarnings("unchecked")
      final
      EnumOption<Mode> opt = (EnumOption<Mode>) option;
      setOperationMode(opt.getValue());
    } else if (option.hasID
      (ChainSimplifierFactory.
       OPTION_OPSearchAutomatonSimplifier_HiddenEvents)) {
      final EventSetOption opt = (EventSetOption) option;
      setHiddenEvents(opt.getValue());
    } else if (option.hasID
      (ChainSimplifierFactory.
       OPTION_OPSearchAutomatonSimplifier_Propositions)) {
      final EventSetOption opt = (EventSetOption) option;
      setPropositions(opt.getValue());
    } else if (option.hasID
      (ChainSimplifierFactory.
       OPTION_OPSearchAutomatonSimplifier_OutputHiddenEvent)) {
      //TODO
    } else if (option.hasID
      (ChainSimplifierFactory.
       OPTION_OPSearchAutomatonSimplifier_LogFile)) {
      final FileOption opt = (FileOption) option;
      setLogFile(opt.getValue());
    }
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

    final Collection<TransitionProxy> transitions =
      mInputAutomaton.getTransitions();
    final int numTransitions = transitions.size();
    mObservableSuccessor =
      new WatersLongIntHashMap(2 * numTransitions, NO_TRANSITION,
                               ObservableSuccessorHashingStrategy.INSTANCE);
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
    } else {
      final int old = putObservableSuccessor(sourceCode, eventCode, targetCode);
      if (old != NO_TRANSITION && old != targetCode) {
        throw new NondeterministicDESException(aut, source, event);
      }
    }
  }

  private void setUpStronglyConnectedComponents()
  {
    final int numStates = mOriginalStates.length;
    mTarjan = new Tarjan(numStates);
    mComponentOfState = new StronglyConnectedComponent[numStates];
    final int comps = mTarjan.findStronglyConnectedComponents();
    final OPSearchAutomatonResult result = getAnalysisResult();
    result.recordComponents(comps);
  }

  private void setUpVerifier()
    throws AnalysisAbortException, OverflowException
  {
    final int numStates = mOriginalStates.length;
    mVerifierStatePairs = new TLongArrayList(numStates);
    mVerifierStateMap = new TLongIntHashMap(numStates);
    mVerifierPredecessors = new TIntArrayList(numStates);
    mPredecessorsOfDead = mListBuffer.createList();
    mContainmentTestSet = new BitSet(numStates);
    buildVerifier();
  }

  private void rebuildVerifier()
    throws AnalysisAbortException, OverflowException
  {
    mVerifierStatePairs.clear();
    mVerifierStateMap.clear();
    for (int i = 0; i < mVerifierPredecessors.size(); i++) {
      final int list = mVerifierPredecessors.get(i);
      mListBuffer.dispose(list);
    }
    mVerifierPredecessors.clear();
    mListBuffer.dispose(mPredecessorsOfDead);
    mPredecessorsOfDead = mListBuffer.createList();
    buildVerifier();
  }

  private void buildVerifier()
    throws AnalysisAbortException, OverflowException
  {
    final int numStates = mOriginalStates.length;
    for (int s = 0; s < numStates; s++) {
      checkAbort();
      expandVerifierPairSingleton(s);
    }
    for (int pindex = 0; pindex < mVerifierStatePairs.size(); pindex++) {
      checkAbort();
      final long pair = mVerifierStatePairs.get(pindex);
      final int pcode = pindex + mOriginalStates.length;
      expandVerifierPairEncoded(pcode, pair);
      if (mOperationMode == Mode.VERIFY &&
          !mListBuffer.isEmpty(mPredecessorsOfDead)) {
        break;
      }
    }
    final OPSearchAutomatonResult result = getAnalysisResult();
    result.recordVerifier(mVerifierStatePairs.size());
  }

  private void expandVerifierPairSingleton(final int code)
    throws OverflowException
  {
    if (getRootIndex(code) == code) {
      expandVerifierPairTagged(code, code, code);
    }
  }

  private void expandVerifierPairEncoded(final int pcode, final long pair)
    throws OverflowException
  {
    final int code1 = (int) (pair & 0xffffffffL);
    final int code2 = (int) (pair >> 32);
    expandVerifierPairTagged(pcode, code1, code2);
  }

  private void expandVerifierPairTagged(final int pcode,
                                        final int code1,
                                        final int code2)
    throws OverflowException
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
    // Observable tau transitions ...
    if (mOPSearchPhase) {
      if (!entau1 && !containsObservableTauSuccessors(code2, code1) ||
          !entau2 && !containsObservableTauSuccessors(code1, code2)) {
        mListBuffer.append(mPredecessorsOfDead, pcode);
        return;
      }
    }
    // Proper event transitions ...
    final boolean check;
    if (code1 != code2) {
      check = true;
    } else if (comp1 != null) {
      // When running OP-Verifier, we must also check pairs reached from
      // different states in a strongly component. This is not necessary
      // in OP-Search, because all nondeterminism in strongly connected
      // components will be resolved anyway in the output automaton.
      check = (mOperationMode == Mode.VERIFY);
    } else {
      check = false;
    }
    if (check) {
      for (int e = 0; e < mUnobservableTau; e++) {
        int esucc1 = 0, esucc2 = 0;
        final boolean en1, en2;
        if (comp1 == null) {
          esucc1 = getObservableSuccessor(code1, e);
          en1 = esucc1 != NO_TRANSITION;
        } else {
          en1 = comp1.isEnabledEvent(e);
        }
        if (comp2 == null) {
          esucc2 = getObservableSuccessor(code2, e);
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
              comp1.iterate(mReadOnlyIterator);
              while (mReadOnlyIterator.advance()) {
                final int member1 = mReadOnlyIterator.getCurrentData();
                final int succ1 = getObservableSuccessor(member1, e);
                if (succ1 != NO_TRANSITION) {
                  enqueueSuccessors(pcode, e, succ1, comp2);
                }
              }
            }
          }
        } else if (en1 && !entau2 || en2 && !entau1) {
          mListBuffer.prepend(mPredecessorsOfDead, pcode);
          return;
        }
      }
    }
    // Unobservable tau transitions ...
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
    throws OverflowException
  {
    comp.iterate(mReadOnlyIterator);
    while (mReadOnlyIterator.advance()) {
      final int source1 = mReadOnlyIterator.getCurrentData();
      final int succ1 = getObservableSuccessor(source1, e);
      if (succ1 != NO_TRANSITION) {
        mAltReadOnlyIterator.reset(mReadOnlyIterator);
        while (mAltReadOnlyIterator.advance()) {
          final int source2 = mAltReadOnlyIterator.getCurrentData();
          final int succ2 = getObservableSuccessor(source2, e);
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
    throws OverflowException
  {
    comp2.iterate(mAltReadOnlyIterator);
    while (mAltReadOnlyIterator.advance()) {
      final int member2 = mAltReadOnlyIterator.getCurrentData();
      final int succ2 = getObservableSuccessor(member2, e);
      if (succ2 != NO_TRANSITION) {
        enqueueSuccessor(pcode, esucc1, succ2);
      }
    }
  }

  private void enqueueTauSuccessors(final int pcode,
                                    final StronglyConnectedComponent comp1,
                                    final int code1,
                                    final int code2)
    throws OverflowException
  {
    if (comp1 == null) {
      enqueueTauSuccessors(pcode, code1, code2);
    } else {
      comp1.iterate(mReadOnlyIterator);
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        enqueueTauSuccessors(pcode, state, code2);
      }
    }
  }

  private void enqueueTauSuccessors(final int pcode,
                                    final int code1,
                                    final int code2)
    throws OverflowException
  {
    final int list1 = mUnobservableTauSuccessors[code1];
    if (list1 != IntListBuffer.NULL) {
      mAltReadOnlyIterator.reset(list1);
      while (mAltReadOnlyIterator.advance()) {
        final int succ1 = mAltReadOnlyIterator.getCurrentData();
        enqueueSuccessor(pcode, succ1, code2);
      }
    }
  }

  private void enqueueSuccessor(final int from, final int to1, final int to2)
    throws OverflowException
  {
    if (to1 != to2) {
      final int root1 = getRootIndex(to1);
      final int root2 = getRootIndex(to2);
      if (root1 != root2) {
        final long pair = getPair(root1, root2);
        final int lookup = mVerifierStateMap.get(pair);
        final int list;
        if (lookup > 0) {
          final int pindex = lookup - mOriginalStates.length;
          list = mVerifierPredecessors.get(pindex);
        } else {
          final int pindex = mVerifierStateMap.size();
          if (pindex >= getNodeLimit()) {
            throw new OverflowException(OverflowKind.NODE, getNodeLimit());
          }
          final int pcode = pindex + mOriginalStates.length;
          mVerifierStatePairs.add(pair);
          mVerifierStateMap.put(pair, pcode);
          list = mListBuffer.createList();
          mVerifierPredecessors.add(list);
        }
        mListBuffer.prependUnique(list, from);
      }
    }
  }

  /**
   * Tests whether all strongly connected components of observable tau
   * successors of the strongly connected component of state inner are also
   * strongly connected components of observable tau successors of the strongly
   * connected component of state outer.
   */
  private boolean containsObservableTauSuccessors(final int inner,
                                                  final int outer)
  {
    try {
      final StronglyConnectedComponent ocomp = mComponentOfState[outer];
      if (ocomp == null) {
        collectContainmentTestSet(outer);
      } else if (ocomp.isEnabledEvent(mObservableTau)) {
        ocomp.iterate(mReadOnlyIterator);
        while (mReadOnlyIterator.advance()) {
          final int state = mReadOnlyIterator.getCurrentData();
          collectContainmentTestSet(state);
        }
      }
      final StronglyConnectedComponent icomp = mComponentOfState[inner];
      if (icomp == null) {
        return containedInContainmentTestSet(inner);
      } else {
        if (icomp.isEnabledEvent(mObservableTau)) {
          icomp.iterate(mReadOnlyIterator);
          while (mReadOnlyIterator.advance()) {
            final int state = mReadOnlyIterator.getCurrentData();
            if (!containedInContainmentTestSet(state)) {
              return false;
            }
          }
        }
        return true;
      }
    } finally {
      mContainmentTestSet.clear();
    }
  }

  private void collectContainmentTestSet(final int state)
  {
    final int list = mObservableTauSuccessors[state];
    if (list != IntListBuffer.NULL) {
      mAltReadOnlyIterator.reset(list);
      while (mAltReadOnlyIterator.advance()) {
        final int succ = mAltReadOnlyIterator.getCurrentData();
        final int root = getRootIndex(succ);
        mContainmentTestSet.set(root);
      }
    }
  }

  private boolean containedInContainmentTestSet(final int state)
  {
    final int list = mObservableTauSuccessors[state];
    if (list != IntListBuffer.NULL) {
      mAltReadOnlyIterator.reset(list);
      while (mAltReadOnlyIterator.advance()) {
        final int succ = mAltReadOnlyIterator.getCurrentData();
        final int root = getRootIndex(succ);
        if (!mContainmentTestSet.get(root)) {
          return false;
        }
      }
    }
    return true;
  }


  //#########################################################################
  //# OP-Search Algorithm
  private void doOPSearchStep()
    throws AnalysisAbortException, OverflowException
  {
    final OPSearchAutomatonResult result = getAnalysisResult();
    result.recordIteration();
    final long trans = findOPSearchTransition();
    final int source = (int) (trans & 0xffffffffL);
    final int target = (int) (trans >> 32);
    final int unobsList = mUnobservableTauSuccessors[source];
    mListBuffer.remove(unobsList, target);
    if (mListBuffer.isEmpty(unobsList)) {
      mUnobservableTauSuccessors[source] = IntListBuffer.NULL;
      mListBuffer.dispose(unobsList);
    }
    int obsList = mObservableTauSuccessors[source];
    if (obsList == IntListBuffer.NULL) {
      mObservableTauSuccessors[source] = obsList = mListBuffer.createList();
    }
    mListBuffer.add(obsList, target);
    final StronglyConnectedComponent comp = mComponentOfState[source];
    if (comp != null) {
      if (comp == mComponentOfState[target]) {
        mTarjan.split(comp);
      } else {
        comp.updateEventStatus();
      }
    }
    rebuildVerifier();
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
    final int numStates = mOriginalStates.length;
    TIntArrayList current = mBFSIntList1;
    TIntArrayList next = mBFSIntList2;
    boolean found = false;
    int state = -1;
    int pred = -1;
    mAltReadOnlyIterator.reset(mPredecessorsOfDead);
    outer:
    while (mAltReadOnlyIterator.advance()) {
      state = mAltReadOnlyIterator.getCurrentData();
      final int index = state - numStates;
      final int list = mVerifierPredecessors.get(index);
      mReadOnlyIterator.reset(list);
      while (mReadOnlyIterator.advance()) {
        pred = mReadOnlyIterator.getCurrentData();
        if (pred < numStates) {
          found = true;
          break outer;
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
          final int index = state - numStates;
          final int list = mVerifierPredecessors.get(index);
          mReadOnlyIterator.reset(list);
          while (mReadOnlyIterator.advance()) {
            pred = mReadOnlyIterator.getCurrentData();
            if (pred < numStates) {
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
    // found: pred -> state

    // pred is start/start; state is end1/end2
    // We must search the strongly connected component of start
    // to find a link to strongly connected components end1/end2.
    // The search only uses states in start/start or end1/end2.
    // The first step of the shortest path found gets selected.
    int start = pred;
    final int startroot = getRootIndex(start);
    final StronglyConnectedComponent comp = mComponentOfState[start];
    final int index = state - numStates;
    final long targetpair = mVerifierStatePairs.get(index);
    long foundtrans = 0;
    if (comp == null) {
      foundtrans = searchTauSuccessors(startroot, start, start,
                                       targetpair, mBFSLongList1);
    } else {
      comp.iterate(mReadOnlyIterator);
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
          final long pair = currentpairs.get(i);
          final int state1 = (int) (pair & 0xffffffffL);
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
        currentpairs.clear();
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
    if (list != IntListBuffer.NULL) {
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
                predinfo = current1 | (((long) succ) << 32);
              }
              mBFSLongVisited.put(succpair, predinfo);
              queue.add(succpair);
            }
          } else if (getPair(succroot, root2) == targetpair) {
            if (predinfo == 0) {
              predinfo = current1 | (((long) succ) << 32);
            }
            return predinfo;
          }
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
      final int succ1 = getObservableSuccessor(current1, e);
      final int succ2 = getObservableSuccessor(current2, e);
      if (succ1 != NO_TRANSITION && succ2 != NO_TRANSITION && succ1 != succ2) {
        final int root1 = getRootIndex(succ1);
        final int root2 = getRootIndex(succ2);
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
  private boolean needsMerge()
  {
    if (mVerifierStatePairs.isEmpty()) {
      final int numStates = mOriginalStates.length;
      for (int s = 0; s < numStates; s++) {
        final StronglyConnectedComponent comp = mComponentOfState[s];
        if (comp != null) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  private boolean createReducedOutputAutomaton(final boolean force)
    throws AnalysisException
  {
    final ListBufferTransitionRelation rel = createTransitionRelation();
    final ObservationEquivalenceTRSimplifier simp =
      new ObservationEquivalenceTRSimplifier(rel);
    simp.setEquivalence
      (ObservationEquivalenceTRSimplifier.Equivalence.BISIMULATION);
    simp.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.NONE);
    simp.setAppliesPartitionAutomatically(false);
    final boolean change = simp.run();
    if (force || change) {
      final TRPartition partition = simp.getResultPartition();
      return createOutputAutomaton(rel, partition);
    } else {
      return setAutomatonResult(mInputAutomaton);
    }
  }

  private ListBufferTransitionRelation createTransitionRelation()
    throws OverflowException
  {
    // 1. Encode events
    final int numEvents = mEvents.length + 1;
    final List<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    final EventProxy otau = createObservableTauEvent(events, 0);
    for (final EventProxy event : mEvents) {
      events.add(event);
    }
    mEventEncoding = new EventEncoding(events, mKindTranslator);
    final int numProperEvents = mEventEncoding.getNumberOfProperEvents();
    final int numProps = mEventEncoding.getNumberOfPropositions();
    final int ocode = mEventEncoding.getEventCode(otau);

    // 2. Merge states
    // All strongly tau-connected components are treated as a single state.
    // Furthermore, any two components that contain states reached by strings
    // with equal projection are merged. The observer property ensures that the
    // result is still observation equivalent to the original automaton.
    final int numPairs = mVerifierStatePairs.size();
    for (int i = 0; i < numPairs; i++) {
      final long pair = mVerifierStatePairs.get(i);
      final int state1 = (int) (pair & 0xffffffffL);
      final int state2 = (int) (pair >> 32);
      mergeComponents(state1, state2);
    }
    mRootStates = new TIntArrayList(mOriginalStates.length);
    final TIntIntHashMap stateMap = new TIntIntHashMap(mOriginalStates.length);
    for (int s0 = 0; s0 < mOriginalStates.length; s0++) {
      final StronglyConnectedComponent comp = mComponentOfState[s0];
      if (comp == null) {
        final int s1 = mRootStates.size();
        mRootStates.add(s0);
        stateMap.put(s0, s1);
      } else {
        final int root = comp.getRootIndex();
        if (root == s0) {
          final int s1 = mRootStates.size();
          mRootStates.add(s0);
          stateMap.put(s0, s1);
        }
      }
    }
    final int numStates = mRootStates.size();

    // 3. Initialise transition relation
    String name = getOutputName();
    if (name == null) {
      name = mInputAutomaton.getName();
    }
    ComponentKind kind = getOutputKind();
    if (kind == null) {
      kind = mInputAutomaton.getKind();
    }
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(name, kind, mEventEncoding, numStates,
                                       ListBufferTransitionRelation.
                                       CONFIG_PREDECESSORS);

    // 4. Assign initial and marked states
    for (int s1 = 0; s1 < numStates; s1++) {
      final int s0 = mRootStates.get(s1);
      final StronglyConnectedComponent comp = mComponentOfState[s0];
      if (comp == null) {
        if (s0 == 0) {
          rel.setInitial(s1, true);
        }
        for (int p1 = 0; p1 < numProps; p1++) {
          final EventProxy prop = mEventEncoding.getProposition(p1);
          final int p0 = mEventMap.get(prop);
          if (getObservableSuccessor(s0, p0) != NO_TRANSITION) {
            rel.setMarked(s1, p1, true);
          }
        }
      } else {
        if (comp.isInitial()) {
          rel.setInitial(s1, true);
        }
        for (int p1 = 0; p1 < numProps; p1++) {
          final EventProxy prop = mEventEncoding.getProposition(p1);
          final int p0 = mEventMap.get(prop);
          if (comp.isEnabledEvent(p0)) {
            rel.setMarked(s1, p1, true);
          }
        }
      }
    }

    // 5. Create Transitions
    for (int s1 = 0; s1 < numStates; s1++) {
      final int s0 = mRootStates.get(s1);
      final StronglyConnectedComponent comp = mComponentOfState[s0];
      // observable tau transitions ...
      if (comp == null) {
        final int list = mObservableTauSuccessors[s0];
        if (list != IntListBuffer.NULL) {
          mAltReadOnlyIterator.reset(list);
          while (mAltReadOnlyIterator.advance()) {
            final int succ = mAltReadOnlyIterator.getCurrentData();
            if (s0 != succ) {
              final int t0 = getRootIndex(succ);
              final int t1 = stateMap.get(t0);
              rel.addTransition(s1, ocode, t1);
            }
          }
        }
      } else if (comp.isEnabledEvent(mObservableTau)) {
        comp.iterate(mReadOnlyIterator);
        while (mReadOnlyIterator.advance()) {
          final int source = mReadOnlyIterator.getCurrentData();
          final int list = mObservableTauSuccessors[source];
          if (list != IntListBuffer.NULL) {
            mAltReadOnlyIterator.reset(list);
            while (mAltReadOnlyIterator.advance()) {
              final int succ = mAltReadOnlyIterator.getCurrentData();
              final int t0 = getRootIndex(succ);
              if (s0 != t0) {
                final int t1 = stateMap.get(t0);
                rel.addTransition(s1, ocode, t1);
              }
            }
          }
        }
      }
      // proper event transitions ...
      for (int e1 = OBSERVABLE_TAU + 1; e1 < numProperEvents; e1++) {
        final EventProxy event = mEventEncoding.getProperEvent(e1);
        final int e0 = mEventMap.get(event);
        if (comp == null) {
          final int succ = getObservableSuccessor(s0, e0);
          if (succ != NO_TRANSITION) {
            final int t0 = getRootIndex(succ);
            final int t1 = stateMap.get(t0);
            rel.addTransition(s1, e1, t1);
          }
        } else if (comp.isEnabledEvent(e0)) {
          comp.iterate(mReadOnlyIterator);
          while (mReadOnlyIterator.advance()) {
            final int src = mReadOnlyIterator.getCurrentData();
            final int succ = getObservableSuccessor(src, e0);
            if (succ != NO_TRANSITION) {
              final int t0 = getRootIndex(succ);
              final int t1 = stateMap.get(t0);
              rel.addTransition(s1, e1, t1);
            }
          }
        }
      }
    }

    return rel;
  }

  private boolean createOutputAutomaton(final ListBufferTransitionRelation rel,
                                        final TRPartition partition)
    throws AnalysisException
  {
    // 1. Establish class map
    final int numInputStates = rel.getNumberOfStates();
    final int numClasses;
    final int[] classMap;
    int[] current = null;
    if (partition == null) {
      numClasses = numInputStates;
      classMap = new int[numInputStates];
      for (int c = 0; c < numClasses; c++) {
        classMap[c] = c;
      }
      current = new int[1];
    } else {
      numClasses = partition.getNumberOfClasses();
      classMap = partition.getStateToClass();
    }

    // 2. Find degree of nondeterminism for each class
    final int numProperEvents = mEventEncoding.getNumberOfProperEvents();
    final int[] maxFanout = new int[numClasses];
    final int[] currentFanout = new int[numClasses];
    final int[] cls = new int[numClasses];
    final TransitionIterator iter = rel.createPredecessorsReadOnlyIterator();
    for (int e = OBSERVABLE_TAU + 1; e < numProperEvents; e++) {
      Arrays.fill(currentFanout, 0);
      Arrays.fill(cls, -1);
      for (int toClass = 0; toClass < numClasses; toClass++) {
        if (partition == null) {
          if (rel.isReachable(toClass)) {
            current[0] = toClass;
          } else {
            continue;
          }
        } else {
          current = partition.getStates(toClass);
          if (current == null) {
            continue;
          }
        }
        for (final int t : current) {
          iter.reset(t, e);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            final int fromClass = classMap[s];
            if (cls[fromClass] != toClass) {
              currentFanout[fromClass]++;
              cls[fromClass] = toClass;
            }
          }
        }
      }
      for (int c = 0; c < numClasses; c++) {
        if (currentFanout[c] > maxFanout[c]) {
          maxFanout[c] = currentFanout[c];
        }
      }
    }

    // 3. Create output automaton states and partition
    int numOutputStates = 0;
    int outputFanout = 0;
    for (int c = 0; c < numClasses; c++) {
      numOutputStates += maxFanout[c];
      if (maxFanout[c] > outputFanout) {
        outputFanout = maxFanout[c];
      }
    }
    final List<StateProxy> states = new ArrayList<>(numOutputStates);
    final int[] cindex = new int[numClasses];
    final int numProps = mEventEncoding.getNumberOfPropositions();
    final TLongObjectHashMap<Collection<EventProxy>> markingsMap =
      new TLongObjectHashMap<Collection<EventProxy>>();
    final Collection<EventProxy> empty = Collections.emptyList();
    markingsMap.put(0, empty);
    final List<int[]> outputPartition = new ArrayList<int[]>(numOutputStates);
    final TIntArrayList currentClass = new TIntArrayList();
    int code = 0;
    for (int c = 0; c < numClasses; c++) {
      if (partition == null) {
        if (rel.isReachable(c)) {
          current[0] = c;
        } else {
          continue;
        }
      } else {
        current = partition.getStates(c);
        if (current == null) {
          continue;
        }
      }
      boolean init = false;
      long markings = 0;
      for (final int s : current) {
        init |= rel.isInitial(s);
        final long stateMarkings = rel.getAllMarkings(s);
        markings = rel.mergeMarkings(markings, stateMarkings);
        final int root = mRootStates.get(s);
        final StronglyConnectedComponent comp = mComponentOfState[root];
        if (comp == null) {
          currentClass.add(root);
        } else {
          comp.iterate(mReadOnlyIterator);
          while (mReadOnlyIterator.advance()) {
            final int member = mReadOnlyIterator.getCurrentData();
            currentClass.add(member);
          }
        }
      }
      final int[] currentClassArray = currentClass.toArray();
      currentClass.clear();
      Collection<EventProxy> props = markingsMap.get(markings);
      if (props == null) {
        props = new ArrayList<EventProxy>(numProps);
        for (int p = 0; p < numProps; p++) {
          if (rel.isMarked(markings, p)) {
            final EventProxy prop = mEventEncoding.getProposition(p);
            props.add(prop);
          }
        }
        markingsMap.put(markings, props);
      }
      cindex[c] = code;
      final StateProxy state0 = new MemStateProxy(code++, init, props);
      states.add(state0);
      outputPartition.add(currentClassArray);
      for (int i = 1; i < maxFanout[c]; i++) {
        final StateProxy state = new MemStateProxy(code++, false, props);
        states.add(state);
        outputPartition.add(currentClassArray);
      }
    }

    // 4. Create output automaton transitions
    final ProductDESProxyFactory factory = getFactory();
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>();
    final List<EventProxy> otaus = new ArrayList<EventProxy>();

    for (int c = 0; c < numClasses; c++) {
      final int fanout = maxFanout[c];
      if (fanout > 1) {
        final EventProxy otau = createObservableTauEvent(otaus, 0);
        final int start = cindex[c];
        for (int offset = 0; offset < fanout; offset++) {
          final int next = (offset + 1) % fanout;
          final StateProxy source = states.get(start + offset);
          final StateProxy target = states.get(start + next);
          final TransitionProxy trans =
            factory.createTransitionProxy(source, otau, target);
          transitions.add(trans);
        }
      }
    }

    final int numEvents = mEventEncoding.getNumberOfEvents() + outputFanout;
    final Collection<EventProxy> events = new ArrayList<EventProxy>(numEvents);
    for (int p = 0; p < numProps; p++) {
      final EventProxy prop = mEventEncoding.getProposition(p);
      events.add(prop);
    }

    events:
    for (int e = OBSERVABLE_TAU; e < numProperEvents; e++) {
      EventProxy event = mEventEncoding.getProperEvent(e);
      if (e != OBSERVABLE_TAU) {
        boolean selfloop = true;
        classes:
        for (int toClass = 0; toClass < numClasses; toClass++) {
          if (partition == null) {
            if (rel.isReachable(toClass)) {
              current[0] = toClass;
            } else {
              continue;
            }
          } else {
            current = partition.getStates(toClass);
            if (current == null) {
              continue;
            }
          }
          selfloop = false;
          for (final int t : current) {
            iter.reset(t, e);
            while (iter.advance()) {
              final int s = iter.getCurrentSourceState();
              final int fromClass = classMap[s];
              if (fromClass == toClass) {
                selfloop = true;
              } else {
                selfloop = false;
                break classes;
              }
            }
            if (!selfloop) {
              break classes;
            }
          }
        }
        if (selfloop) {
          continue events;
        } else {
          events.add(event);
        }
      }
      Arrays.fill(currentFanout, 0);
      Arrays.fill(cls, -1);
      for (int toClass = 0; toClass < numClasses; toClass++) {
        final StateProxy target = states.get(cindex[toClass]);
        if (partition == null) {
          if (rel.isReachable(toClass)) {
            current[0] = toClass;
          } else {
            continue;
          }
        } else {
          current = partition.getStates(toClass);
          if (current == null) {
            continue;
          }
        }
        for (final int t : current) {
          iter.reset(t, e);
          while (iter.advance()) {
            final int s = iter.getCurrentSourceState();
            final int fromClass = classMap[s];
            if (cls[fromClass] != toClass) {
              int offset = currentFanout[fromClass]++;
              if (e == OBSERVABLE_TAU) {
                if (fromClass == toClass) {
                  continue;
                }
                final int eindex;
                if (maxFanout[fromClass] <= 1) {
                  eindex = offset;
                  offset = 0;
                } else {
                  eindex = offset / maxFanout[fromClass] + 1;
                  offset = offset % maxFanout[fromClass];
                }
                // Note. First observable tau has been read from encoding.
                event = createObservableTauEvent(otaus, eindex);
              }
              cls[fromClass] = toClass;
              final StateProxy source = states.get(cindex[fromClass] + offset);
              final TransitionProxy trans =
                factory.createTransitionProxy(source, event, target);
              transitions.add(trans);
            }
          }
        }
      }
    }

    // 5. Create output automaton
    final String name = rel.getName();
    final ComponentKind kind = rel.getKind();
    events.addAll(otaus);
    final AutomatonProxy aut =
      factory.createAutomatonProxy(name, kind, events, states, transitions);
    final StateEncoding inputEnc = new StateEncoding(mOriginalStates);
    final StateEncoding outputEnc = new StateEncoding(states);
    final PartitionedAutomatonResult result = getAnalysisResult();
    result.setComputedAutomaton(aut, inputEnc, outputEnc, outputPartition);
    return result.isSatisfied();
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
        comp.initialiseEventStatus();
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

  private EventProxy createObservableTauEvent(final List<EventProxy> events,
                                              final int eindex)
  {
    if (mOutputHiddenEvent != null) {
      if (events.isEmpty()) {
        events.add(mOutputHiddenEvent);
      }
      return mOutputHiddenEvent;
    } else if (eindex < events.size()) {
      return events.get(eindex);
    } else {
      assert eindex == events.size() : "Unexpected event index!";
      final ProductDESProxyFactory factory = getFactory();
      String autname = getOutputName();
      if (autname == null) {
        autname = mInputAutomaton.getName();
      }
      final String ename = ":op" + (eindex + 1) + ':' + autname;
      final EventProxy event =
        factory.createEventProxy(ename, EventKind.CONTROLLABLE, false);
      events.add(event);
      return event;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getObservableSuccessor(final int state, final int event)
  {
    final long key = state | ((long) event << 32);
    return mObservableSuccessor.get(key);
  }

  private int putObservableSuccessor(final int state,
                                     final int event,
                                     final int succ)
  {
    final long key = state | ((long) event << 32);
    return mObservableSuccessor.put(key, succ);
  }

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
  //# Debugging
  private void dumpVerifierToLogFile(final String header)
    throws AnalysisException
  {
    if (mLogFile != null) {
      try {
        final OutputStream stream = new FileOutputStream(mLogFile);
        final PrintWriter writer = new PrintWriter(stream);
        writer.println(header);
        dump(writer);
        writer.close();
      } catch (final IOException exception) {
        throw new AnalysisException(exception);
      }
    }
  }

  private void dump(final PrintWriter writer)
  {
    writer.println("EVENTS");
    for (int e = 0; e < mEvents.length; e++) {
      final EventProxy event = mEvents[e];
      writer.println("  " + e + ": " + event);
    }
    writer.println("STATES");
    for (int s = 0; s < mOriginalStates.length; s++) {
      final StateProxy state = mOriginalStates[s];
      writer.println("  " + s + ": " + state.getName());
    }
    writer.println("TRANSITIONS");
    for (int s = 0; s < mOriginalStates.length; s++) {
      for (int e = 0; e < mEvents.length; e++) {
        final int succ = getObservableSuccessor(s, e);
        switch (succ) {
        case DUMP_STATE:
          writer.println("  " + s + " -" + e + "-> DUMP");
          break;
        case NO_TRANSITION:
          break;
        default:
          writer.println("  " + s + " -" + e + "-> " + succ);
          break;
        }
      }
      final int utau = mUnobservableTauSuccessors[s];
      if (utau != IntListBuffer.NULL) {
        writer.print("  " + s + " -utau-> ");
        mListBuffer.dumpList(writer, utau);
        writer.println();
      }
      final int otau = mObservableTauSuccessors[s];
      if (otau != IntListBuffer.NULL) {
        writer.print("  " + s + " -otau-> ");
        mListBuffer.dumpList(writer, otau);
        writer.println();
      }
    }
    writer.println("PAIRS");
    for (int i = 0; i < mVerifierStatePairs.size(); i++) {
      final int code = mOriginalStates.length + i;
      final long pair = mVerifierStatePairs.get(i);
      final int p1 = (int) (pair & 0xffffffffL);
      final int p2 = (int) (pair >> 32);
      writer.println("  " + code + ": (" + p1 + '/' + p2 + ')');
    }
    writer.println("PREDECESSORS");
    for (int i = 0; i < mVerifierPredecessors.size(); i++) {
      final int code = mOriginalStates.length + i;
      writer.print("  " + code + ": ");
      final int list = mVerifierPredecessors.get(i);
      mListBuffer.dumpList(writer, list);
      writer.println();
    }
    writer.print("  DEAD: ");
    mListBuffer.dumpList(writer, mPredecessorsOfDead);
    writer.println();
  }

  @SuppressWarnings("unused")
  private void checkComponentIntegrity()
  {
    for (final StronglyConnectedComponent comp : mComponentOfState) {
      if (comp != null) {
        comp.checkIntegrity();
      }
    }
  }


  //#########################################################################
  //# Inner Class Mode
  /**
   * Enumeration of operation modes of {@link OPSearchAutomatonSimplifier}.
   * This class allows the user to choose between the OP-Verifier and
   * OP-Search algorithms.
   */
  public static enum Mode
  {
    /**
     * Constant selecting OP-Verifier algorithm.
     * When run in VERIFY mode, the {@link OPSearchAutomatonSimplifier}
     * tests whether the projection of the input automaton using the given
     * silent transitions has the observer property. If so a <CODE>true</CODE>
     * analysis result is returned, otherwise <CODE>false</CODE>. In no case,
     * an output automaton is computed.
     */
    VERIFY,
    /**
     * Constant selection OP-Search algorithm.
     * When run in MINIMIZE mode, the {@link OPSearchAutomatonSimplifier}
     * tries to minimise the input automaton in a conflict-preserving way,
     * using the OP-Search algorithm. The analysis result will always be
     * <CODE>true</CODE> and contain an automaton representing a simplified
     * version of the input. If no minimisation is possible, the output
     * automaton will be identical to (the same object as) the input
     * automaton.
     */
    MINIMIZE;
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
      mStack = new TIntArrayStack();
      mOnStack = new boolean[numStates];
      mComponents = new ArrayList<StronglyConnectedComponent>();
    }

    //#########################################################################
    //# Invocation
    private int findStronglyConnectedComponents()
    {
      mCallIndex = 1;
      mNumComponents = 0;
      final int numStates = mTarjan.length;
      for (int state = 0; state < numStates; state++) {
        if (mTarjan[state] == 0) {
          tarjan(state);
        }
      }
      setUpEventStatus();
      return mNumComponents;
    }

    private void split(final StronglyConnectedComponent comp)
    {
      comp.iterate(mReadOnlyIterator);
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        mTarjan[state] = mLowLink[state] = 0;
      }
      mCallIndex = 1;
      comp.iterate(mReadOnlyIterator);
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
        comp.initialiseEventStatus();
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
          mListBuffer.prepend(list, pop);
          mOnStack[pop] = false;
          count++;
        } while (pop != state);
        mNumComponents++;
        if (count > 1) {
          final StronglyConnectedComponent comp =
            new StronglyConnectedComponent(list);
          mComponents.add(comp);
          mAltReadOnlyIterator.reset(list);
          while (mAltReadOnlyIterator.advance()) {
            final int elem = mAltReadOnlyIterator.getCurrentData();
            mComponentOfState[elem] = comp;
          }
        } else {
          mListBuffer.dispose(list);
          mComponentOfState[state] = null;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final int[] mTarjan;
    private final int[] mLowLink;
    private final TIntStack mStack;
    private final boolean[] mOnStack;
    private int mNumComponents;
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
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      final PrintWriter printer = new PrintWriter(writer);
      mListBuffer.dumpList(printer, mStates);
      printer.flush();
      return writer.toString();
    }

    //#######################################################################
    //# Simple Access
    private int getRootIndex()
    {
      return mListBuffer.getFirst(mStates);
    }

    private void iterate(final IntListBuffer.ReadOnlyIterator iter)
    {
      iter.reset(mStates);
    }

    private boolean isEnabledEvent(final int e)
    {
      return mEnabledEvents.get(e);
    }

    @SuppressWarnings("unused")
    private int size()
    {
      return mListBuffer.getLength(mStates);
    }

    private boolean isInitial()
    {
      return mListBuffer.contains(mStates, 0);
    }

    //#######################################################################
    //# Flag Setup
    private void initialiseEventStatus()
    {
      setUpProperEventStatus();
      setUpTauEventStatus(mUnobservableTau, mUnobservableTauSuccessors);
      if (mOPSearchPhase) {
        setUpTauEventStatus(mObservableTau, mObservableTauSuccessors);
      }
    }

    private void updateEventStatus()
    {
      mEnabledEvents.clear(mUnobservableTau);
      setUpTauEventStatus(mUnobservableTau, mUnobservableTauSuccessors);
      mEnabledEvents.set(mObservableTau);
    }

    private void setUpProperEventStatus()
    {
      for (int e = 0; e < mUnobservableTau; e++) {
        iterate(mReadOnlyIterator);
        while (mReadOnlyIterator.advance()) {
          final int state = mReadOnlyIterator.getCurrentData();
          if (getObservableSuccessor(state, e) != NO_TRANSITION) {
            mEnabledEvents.set(e);
            break;
          }
        }
      }
    }

    private void setUpTauEventStatus(final int taucode, final int[] successors)
    {
      iterate(mReadOnlyIterator);
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        final int list = successors[state];
        if (list != IntListBuffer.NULL) {
          mAltReadOnlyIterator.reset(list);
          while (mAltReadOnlyIterator.advance()) {
            final int succ = mAltReadOnlyIterator.getCurrentData();
            if (mComponentOfState[succ] != this) {
              mEnabledEvents.set(taucode);
              return;
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
        if (getObservableSuccessor(state, e) != NO_TRANSITION) {
          mEnabledEvents.set(e);
        }
      }
      if (mUnobservableTauSuccessors[state] != IntListBuffer.NULL) {
        mEnabledEvents.set(mUnobservableTau);
      }
      if (mObservableTauSuccessors[state] != IntListBuffer.NULL) {
        mEnabledEvents.set(mObservableTau);
      }
    }

    private void merge(final StronglyConnectedComponent comp)
    {
      comp.iterate(mReadOnlyIterator);
      while (mReadOnlyIterator.advance()) {
        final int state = mReadOnlyIterator.getCurrentData();
        mComponentOfState[state] = this;
      }
      mStates = mListBuffer.catenateDestructively(mStates, comp.mStates);
      mEnabledEvents.or(comp.mEnabledEvents);
    }

    //#######################################################################
    //# Debugging
    private void checkIntegrity()
    {
      final IntListBuffer.ReadOnlyIterator iter =
        mListBuffer.createReadOnlyIterator(mStates);
      while (iter.advance()) {
        final int state = iter.getCurrentData();
        assert mComponentOfState[state] == this : "Unexpected component!";
      }
    }

    //#######################################################################
    //# Data Members
    private int mStates;
    private final BitSet mEnabledEvents;

  }


  //#########################################################################
  //# Inner Class ObservableSuccessorHashingStrategy
  private static class ObservableSuccessorHashingStrategy
    implements WatersLongHashingStrategy
  {
    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.
    //# WatersLongHashingStrategy
    @Override
    public int computeHashCode(final long val)
    {
      final int h0 = -2128831035;
      final int h1 = 16777619;
      final int lo = (int) (val & 0xffffffffL);
      final int hi = (int) (val >> 32);
      return ((h0 * h1) ^ hi) * h1 ^ lo;
      // return 31 * (int) (val + (val >> 16));
    }

    @Override
    public boolean equals(final long val1, final long val2)
    {
      return val1 == val2;
    }

    //#######################################################################
    //# Class Constants
    private static final ObservableSuccessorHashingStrategy INSTANCE =
      new ObservableSuccessorHashingStrategy();
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Data Members
  private final KindTranslator mKindTranslator;
  private Mode mOperationMode;
  private Collection<EventProxy> mHiddenEvents;
  private Collection<EventProxy> mPropositions;
  private EventProxy mOutputHiddenEvent;
  private File mLogFile;

  private AutomatonProxy mInputAutomaton;
  private IntListBuffer mListBuffer;
  private IntListBuffer.ReadOnlyIterator mReadOnlyIterator;
  private IntListBuffer.ReadOnlyIterator mAltReadOnlyIterator;
  private EventProxy[] mEvents;
  private TObjectIntHashMap<EventProxy> mEventMap;
  private int mUnobservableTau;
  private int mObservableTau;
  private StateProxy[] mOriginalStates;
  private WatersLongIntHashMap mObservableSuccessor;
  private int[] mUnobservableTauSuccessors;
  private int[] mObservableTauSuccessors;
  private Tarjan mTarjan;
  private StronglyConnectedComponent[] mComponentOfState;

  private TLongArrayList mVerifierStatePairs;
  private TLongIntHashMap mVerifierStateMap;
  private TIntArrayList mVerifierPredecessors;
  private int mPredecessorsOfDead;
  private boolean mOPSearchPhase;
  private TIntArrayList mBFSIntList1;
  private TIntArrayList mBFSIntList2;
  private TIntHashSet mBFSIntVisited;
  private TLongArrayList mBFSLongList1;
  private TLongArrayList mBFSLongList2;
  private TLongLongHashMap mBFSLongVisited;
  private BitSet mContainmentTestSet;

  private EventEncoding mEventEncoding;
  private TIntArrayList mRootStates;


  //#########################################################################
  //# Data Members
  private static final int NO_TRANSITION = -1;
  private static final int DUMP_STATE = -2;
  private static final int OBSERVABLE_TAU = 1;

}
