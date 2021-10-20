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

package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.DefaultEventStatusProvider;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.EventStatusProvider;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StatusGroupTransitionIterator;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>An abstract base class for monolithic model analysers that use
 * {@link ListBufferTransitionRelation} as their automata representation.</P>
 *
 * <P>The base class supports the automatic conversion of general automata
 * ({@link AutomatonProxy}) to transition-relation based ({@link
 * TRAutomatonProxy}) objects when needed, and contains general support
 * to store the synchronous product state space and expand state tuples.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractTRMonolithicModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractTRMonolithicModelAnalyzer()
  {
    this(null);
  }

  public AbstractTRMonolithicModelAnalyzer(final ProductDESProxy model)
  {
    this(model, IdenticalKindTranslator.getInstance());
  }

  public AbstractTRMonolithicModelAnalyzer(final ProductDESProxy model,
                                           final KindTranslator translator)
  {
    super(model, ProductDESElementFactory.getInstance(), translator);
  }


  //#########################################################################
  //# Configuration
  /**
   * <P>Sets an event encoding for use during analysis.</P>
   * <P>The event encoding can request hiding, event renaming, and special
   * event types. Proper events not in the encoding are added automatically.
   * This information is available during analysis and may affect how
   * the state space is explored, and how an output automaton is created
   * (if applicable).</P>
   * <P>Events in the encoding but not in the input automata are marked as
   * unused in an output transition relation. Propositions not in the encoding
   * are suppressed in an output transition relation. If the event encoding is
   * left unspecified, a default event encoding with all events and
   * propositions in the input automata is used.</P>
   * @param  enc      The event encoding to be used, or <CODE>null</CODE>
   *                  to make the event encoding unspecified.
   * @see EventEncoding
   */
  public void setEventEncoding(final EventEncoding enc)
  {
    mConfiguredEventEncoding = enc;
    mHasConfiguredPropositions = (enc != null);
  }

  /**
   * Retrieves the event encoding used during analysis.
   * @see #setEventEncoding(EventEncoding) setEventEncoding()
   */
  public EventEncoding getEventEncoding()
  {
    return mConfiguredEventEncoding;
  }

  /**
   * Defines the set of propositions to be considered during analysis.
   * If specified, only the events from the given proposition set will
   * be recorded and available during analysis. All other propositions
   * will not be available and thus ignored.
   * @param  props       The set of propositions to be used,
   *                     or <CODE>null</CODE> to use all propositions.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the maximum supported by the underlying
   *                     data structures.
   */
  public void setPropositions(final Collection<EventProxy> props)
    throws OverflowException
  {
    mHasConfiguredPropositions = (props != null);
    if (props == null || props.isEmpty()) {
      if (mConfiguredEventEncoding == null) {
        // nothing
      } else if (mConfiguredEventEncoding.getNumberOfProperEvents() == 1 &&
                 mConfiguredEventEncoding.getProperEvent(EventEncoding.TAU) == null) {
        mConfiguredEventEncoding = null;
      } else {
        mConfiguredEventEncoding.removeAllPropositions();
      }
    } else {
      final KindTranslator translator = getKindTranslator();
      if (mConfiguredEventEncoding == null) {
        mConfiguredEventEncoding = new EventEncoding(props, translator);
      } else {
        mConfiguredEventEncoding.removeAllPropositions();
        for (final EventProxy prop : props) {
          mConfiguredEventEncoding.addProposition(prop, true);
        }
      }
    }
  }

  /**
   * Gets the set of propositions used in analysis.
   * @see #setPropositions(Collection) setPropositions()
   */
  public Collection<EventProxy> getPropositions()
  {
    if (!mHasConfiguredPropositions) {
      return null;
    } else if (mConfiguredEventEncoding == null ||
               mConfiguredEventEncoding.getNumberOfPropositions() == 0) {
      return Collections.emptyList();
    } else {
      final int numProps = mConfiguredEventEncoding.getNumberOfPropositions();
      final Collection<EventProxy> props = new ArrayList<>(numProps);
      for (int p = 0; p < numProps; p++) {
        if (mConfiguredEventEncoding.isPropositionUsed(p)) {
          final EventProxy prop = mConfiguredEventEncoding.getProposition(p);
          props.add(prop);
        }
      }
      return props;
    }
  }

  /**
   * <P>Sets whether deadlock states are pruned.</P>
   *
   * <P>If enabled, and there are propositions in the output event encoding,
   * the model analyser checks for deadlock states in the input
   * automata, i.e., for states that are not marked by any of the
   * propositions, and which do not have any outgoing transitions.
   * Synchronous product states, of which at least one state component is a
   * deadlock state, are not expanded and instead merged into a dump state.</P>
   *
   * <P>In addition, transitions with events marked as <I>failing</I>
   * ({@link EventStatus#STATUS_FAILING}) in the event encoding are replaced
   * by transitions to the dump state regardless of their target states.
   * Events marked as <I>failing</I> ({@link EventStatus#STATUS_FAILING}) and
   * <I>always enabled</I> ({@link EventStatus#STATUS_ALWAYS_ENABLED})
   * in addition suppress other transitions from states where they are
   * enabled. This is useful in verification that only seeks to determine
   * whether or not failing events are ever enabled.</P>
   *
   * @see #getPropositions()
   */
  public void setPruningDeadlocks(final boolean pruning)
  {
    mPruningDeadlocks = pruning;
  }

  /**
   * Returns whether deadlock states are pruned.
   * @see #setPruningDeadlocks(boolean) setPruningDeadlocks()
   */
  public boolean isPruningDeadlocks()
  {
    return mPruningDeadlocks;
  }

  /**
   * Sets a callback that specifies user-defined actions to be performed
   * before adding a new state.
   * @param  callback  The callback to be invoked when adding states,
   *                   or <CODE>null</CODE> to disable this feature.
   */
  public void setStateCallback(final StateCallback callback)
  {
    mStateCallback = callback;
  }

  /**
   * Gets the callback interface executed when adding a new state.
   * @see #setStateCallback(StateCallback) setStateCallback()
   */
  public StateCallback getStateCallback()
  {
    return mStateCallback;
  }

  /**
   * Determines whether controllability is to be checked during synchronous
   * product exploration. If this method returns <CODE>true</CODE>, the
   * event and automaton data structures are set up for a controllability
   * check, and the method {@link #handleUncontrollableState(int, int)
   * handleUncontrollableState()} is called if an uncontrollable state
   * is detected during expansion.
   * @return  <CODE>false</CODE> by default,
   *          but can be overridden by subclasses.
   */
  public boolean isSensitiveToControllability()
  {
    return false;
  }

  public int getDefaultConfig()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
                       OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final AnalysisResult result = getAnalysisResult();
    // Set up input automata and state encoding
    setUpAutomata();
    if (result.isFinished()) {
      return;
    }
    // Set up output event encoding
    final Map<EventProxy,EventInfo> eventInfoMap = setUpEventEncoding();
    checkEventsInAutomta(eventInfoMap);
    if (result.isFinished()) {
      return;
    }
    // Set up deadlock information ...
    setUpDeadlockInfo();
    if (result.isFinished()) {
      return;
    }
    // Add transition information to event info ...
    final List<EventInfo> eventInfoList =
      setUpTransitions(mTRAutomata, eventInfoMap, false);
    if (result.isFinished()) {
      return;
    }
    // Sort event info and merge local events
    postProcessEventInfo(eventInfoList);
  }

  protected void setUpAutomata()
    throws AnalysisException
  {
    // Set up input automata and find their sizes
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    int numAutomata = 0;
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) != null) {
        numAutomata++;
      }
    }
    mInputAutomata = new AutomatonProxy[numAutomata];
    mTRAutomata = new TRAutomatonProxy[numAutomata];
    mDecodedDeadlockState = new int[numAutomata];
    final int[] sizes = new int[numAutomata];
    int a = 0;
    boolean gotDump = false;
    for (final AutomatonProxy aut : automata) {
      checkAbort();
      if (translator.getComponentKind(aut) != null) {
        mInputAutomata[a] = aut;
        final int config = getDefaultConfig();
        final TRAutomatonProxy tr = TRAutomatonProxy.createTRAutomatonProxy
          (aut, translator, config);
        mTRAutomata[a] = tr;
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        int numStates = getLastReachableState(rel) + 1;
        final int dumpIndex = getAlternativeDumpState(rel, numStates);
        if (dumpIndex >= 0) {
          gotDump = true;
          mDecodedDeadlockState[a] = dumpIndex;
          if (dumpIndex >= numStates) {
            numStates = dumpIndex + 1;
          }
        }
        sizes[a++] = numStates;
      }
    }
    if (numAutomata > 0 && !gotDump) {
      mDecodedDeadlockState[0] = sizes[0]++;
    }

    // Set up state encoding
    mStateTupleEncoding = new StateTupleEncoding(sizes);
    final int numWords = mStateTupleEncoding.getNumberOfWords();
    final int stateLimit = getNodeLimit();
    final int tableSize = Math.min(stateLimit, MAX_TABLE_SIZE);
    mStateSpace = new IntArrayBuffer(numWords, stateLimit, tableSize, -1);
    mDecodedSource = new int[numAutomata];
    mEncodedSource = new int[numWords];
    mDecodedTarget = new int[numAutomata];
    mEncodedTarget = new int[numWords];
  }

  protected Map<EventProxy,EventInfo> setUpEventEncoding()
    throws OverflowException, AnalysisAbortException
  {
    final ProductDESProxy des = getModel();

    // Set up output event encoding
    final Collection<EventProxy> events = des.getEvents();
    final int numEvents = events.size();
    final Set<EventProxy> outputEvents;
    if (mConfiguredEventEncoding == null) {
      mOutputEventEncoding = new EventEncoding();
      outputEvents = null;
    } else {
      mOutputEventEncoding = new EventEncoding(mConfiguredEventEncoding);
      outputEvents = new THashSet<>(numEvents);
    }

    // Add propositions to output event encoding ...
    final Map<EventProxy,EventInfo> eventInfoMap = new HashMap<>(numEvents);
    final int numAutomata = mTRAutomata.length;
    for (int a = 0; a < numAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy aut = mTRAutomata[a];
      final EventEncoding enc = aut.getEventEncoding();
      for (int p = 0; p < enc.getNumberOfPropositions(); p++) {
        if (enc.isPropositionUsed(p)) {
          final EventProxy prop = enc.getProposition(p);
          if (mHasConfiguredPropositions) {
            // If the proposition is configured as unused, mark it as used ...
            final int configP = mConfiguredEventEncoding.getEventCode(prop);
            if (configP >= 0) {
              mOutputEventEncoding.setPropositionUsed(configP, true);
            }
          } else {
            // If no configured propositions, add it ...
            mOutputEventEncoding.addProposition(prop, true);
          }
        }
      }
      final KindTranslator translator = getKindTranslator();
      for (int e = EventEncoding.TAU; e < enc.getNumberOfProperEvents(); e++) {
        if ((enc.getProperEventStatus(e) & EventStatus.STATUS_UNUSED) == 0) {
          // For any used event: create event info if not yet present
          final EventProxy event = enc.getProperEvent(e);
          if (!eventInfoMap.containsKey(event)) {
            final boolean controllable;
            if (isSensitiveToControllability()) {
              controllable =
                translator.getEventKind(event) == EventKind.CONTROLLABLE;
            } else {
              controllable = true;
            }
            final EventInfo info = new EventInfo(event, controllable);
            eventInfoMap.put(event, info);
            if (mConfiguredEventEncoding != null) {
              final int confCode = mConfiguredEventEncoding.getEventCode(event);
              if (confCode >= 0) {
                // Set status from configured encoding if present
                final byte status =
                  mConfiguredEventEncoding.getProperEventStatus(confCode);
                info.setStatus(status);
                // Remember which event is in output
                // (Note: output may be different from event if hiding)
                final EventProxy output =
                  mConfiguredEventEncoding.getProperEvent(confCode);
                outputEvents.add(output);
              }
            }
          }
        }
      }
    }

    // Mark events unused if in configured encoding but not in output
    if (mConfiguredEventEncoding != null) {
      final int numConfigured =
        mConfiguredEventEncoding.getNumberOfProperEvents();
      for (int e = EventEncoding.TAU; e < numConfigured; e++) {
        final EventProxy event = mConfiguredEventEncoding.getProperEvent(e);
        if (!outputEvents.contains(event)) {
          final byte status = mConfiguredEventEncoding.getProperEventStatus(e);
          mOutputEventEncoding.setProperEventStatus
            (e, status | EventStatus.STATUS_UNUSED);
        }
      }
    }

    return eventInfoMap;
  }

  protected void setUpDeadlockInfo()
    throws AnalysisAbortException, OverflowException
  {
    final int numProps = mOutputEventEncoding.getNumberOfPropositions();
    if (mPruningDeadlocks && numProps > 0) {
      boolean allPropsUsed = true;
      for (int p = 0; p < numProps; p++) {
        if (!mOutputEventEncoding.isPropositionUsed(p)) {
          allPropsUsed = false;
          break;
        }
      }
      if (allPropsUsed) {
        final int numAutomata = mTRAutomata.length;
        deadlock:
        for (int a = 0; a < numAutomata; a++) {
          final TRAutomatonProxy aut = mTRAutomata[a];
          final EventEncoding enc = aut.getEventEncoding();
          final ListBufferTransitionRelation rel = aut.getTransitionRelation();
          long pattern = rel.createMarkings();
          for (int global = 0; global < numProps; global++) {
            final EventProxy prop = mOutputEventEncoding.getProposition(global);
            final int local = enc.getEventCode(prop);
            if (local >= 0 && rel.isPropositionUsed(local)) {
              pattern = rel.addMarking(pattern, local);
            } else {
              continue deadlock;
            }
          }
          final DeadlockInfo info = new DeadlockInfo(rel, pattern);
          final int numStates = rel.getNumberOfStates();
          for (int s = 0; s < numStates; s++) {
            checkAbort();
            if (rel.isReachable(s) && info.isDeadlockState(s)) {
              if (mDeadlockInfo == null) {
                mDeadlockInfo = new DeadlockInfo[numAutomata];
              }
              mDeadlockInfo[a] = info;
              break;
            }
          }
        }
      }
    }
    mDeadlockState = -1;
  }

  protected List<EventInfo> setUpTransitions
    (final TRAutomatonProxy[] automata,
     final Map<EventProxy,EventInfo> eventInfoMap,
     final boolean reverse)
    throws AnalysisException
  {
    final boolean controllabilitySensitive =
      !reverse && isSensitiveToControllability();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    final Collection<EventProxy> events = des.getEvents();
    final int numAutomata = automata.length;
    final int numEvents = events.size();
    final List<EventInfo> eventInfoList = new ArrayList<>(numEvents);
    for (int a = 0; a < numAutomata; a++) {
      final TRAutomatonProxy aut = automata[a];
      final boolean nondeterminismSupported =
        reverse || supportsNondeterminism();
      final boolean plant = !controllabilitySensitive ||
        translator.getComponentKind(aut) == ComponentKind.PLANT;
      final DeadlockInfo deadlockInfo =
        mDeadlockInfo == null ? null : mDeadlockInfo[a];
      final EventEncoding enc = aut.getEventEncoding();
      final int numLocalEvents = enc.getNumberOfProperEvents();
      for (int local = EventEncoding.TAU; local < numLocalEvents; local++) {
        checkAbort();
        byte status = enc.getProperEventStatus(local);
        if (EventStatus.isUsedEvent(status)) {
          final EventProxy event = enc.getProperEvent(local);
          if (event == null) {
            assert local == EventEncoding.TAU;
            assert nondeterminismSupported;
            final boolean controllable = !controllabilitySensitive ||
              EventStatus.isControllableEvent(status);
            final AutomatonEventInfo autInfo =
              new AutomatonEventInfo(a, aut, plant, local, controllable,
                                     deadlockInfo, true);
            if (!autInfo.isBlocked()) {
              final EventInfo info = new EventInfo(null, controllable);
              info.addAutomatonEventInfo(autInfo);
              info.setOutputCode(EventEncoding.TAU);
              info.sort();
              eventInfoList.add(info);
              mOutputEventEncoding.setProperEventStatus(EventEncoding.TAU,
                                                        status);
            }
          } else {
            final EventInfo info = eventInfoMap.get(event);
            final boolean controllable =
              !controllabilitySensitive || info.isControllable();
            if (controllable) {
              status |= EventStatus.STATUS_CONTROLLABLE;
            }
            final int global =
              mOutputEventEncoding.addProperEvent(event, status);
            if (info != null && !info.isBlocked()) {
              info.setOutputCode(global);
              final AutomatonEventInfo autInfo =
                new AutomatonEventInfo(a, aut, plant, local, controllable,
                                       deadlockInfo, nondeterminismSupported);
              info.addAutomatonEventInfo(autInfo);
            }
          }
        }
      }
    }
    for (final EventProxy event : events) {
      final EventInfo info = eventInfoMap.get(event);
      if (info != null && info.getOutputCode() >= 0 && !info.isBlocked()) {
        info.sort();
        eventInfoList.add(info);
      }
    }
    return eventInfoList;
  }

  protected void postProcessEventInfo(final List<EventInfo> eventInfoList)
    throws AnalysisException
  {
    Collections.sort(eventInfoList);
    mEventInfo = new ArrayList<>(eventInfoList.size());
    final List<EventInfo> group = new ArrayList<>();
    EventInfo current = null;
    for (final EventInfo info : eventInfoList) {
      checkAbort();
      if (current == null) {
        current = info;
        group.add(info);
      } else if (current.isMergible(info)) {
        group.add(info);
      } else {
        addMergedEventInfo(group);
        group.clear();
        current = info;
        group.add(info);
      }
    }
    addMergedEventInfo(group);
  }


  protected List<EventInfo> setUpReverseTransitions()
    throws AnalysisException
  {
    final int numAutomata = mTRAutomata.length;
    final TRAutomatonProxy[] reversedAutomata = new TRAutomatonProxy[numAutomata];
    for (int a = 0; a < numAutomata; a++) {
      checkAbort();
      final TRAutomatonProxy aut = mTRAutomata[a];
      final EventEncoding enc = aut.getEventEncoding();
      final EventEncoding reversedEnc = enc.clone();
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final ListBufferTransitionRelation reversedRel =
        new ListBufferTransitionRelation(rel, reversedEnc,
                                         ListBufferTransitionRelation.CONFIG_PREDECESSORS);
      reversedRel.reverse();
      final TRAutomatonProxy reversedAut =
        new TRAutomatonProxy(reversedEnc, reversedRel);
      reversedAutomata[a] = reversedAut;
    }
    final int numEvents = mEventInfo.size();
    final Map<EventProxy,EventInfo> eventInfoMap = new HashMap<>(numEvents);
    for (final EventInfo info : mEventInfo) {
      info.reset();
      final EventProxy event = info.getEvent();
      eventInfoMap.put(event, info);
    }
    final List<EventInfo> eventInfoList =
      setUpTransitions(reversedAutomata, eventInfoMap, true);
    postProcessEventInfo(eventInfoList);
    return eventInfoList;
  }


  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final AnalysisResult result = getAnalysisResult();
    if (mTRAutomata != null) {
      result.setNumberOfAutomata(mTRAutomata.length);
    }
    if (mStateSpace != null) {
      result.setNumberOfStates(mStateSpace.size());
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mInputAutomata = null;
    mTRAutomata = null;
    mEventInfo = null;
    mStateTupleEncoding = null;
    mStateSpace = null;
    mOutputEventEncoding = null;
    mDeadlockInfo = null;
    mInputStateArrays = null;
    mDecodedSource = null;
    mEncodedSource = null;
    mDecodedTarget = null;
    mEncodedTarget = null;
  }


  //#########################################################################
  //# Hooks
  protected void setConfiguredEventEncoding(final EventEncoding enc)
  {
    mConfiguredEventEncoding = enc;
  }

  protected EventEncoding getOutputEventEncoding()
  {
    return mOutputEventEncoding;
  }

  /**
   * Retrieves an automaton from the input model. If the input model contains
   * {@link TRAutomatonProxy} objects, this method returns the same objects
   * as {@link #getTRAutomaton(int) getTRAutomaton()}, otherwise the results
   * will be different.
   * @param  index  The index of the automaton to be retrieved. The index
   *                corresponds the the position in state tuples and to the
   *                array returned by {@link #getTRAutomata()}.
   * @return An {@link AutomatonProxy} object that is guaranteed to appear
   *         in the input model.
   */
  protected AutomatonProxy getInputAutomaton(final int index)
  {
    return mInputAutomata[index];
  }

  /**
   * Retrieves an array containing the automata from the input model that are
   * used by the analyser. If the input model contains {@link TRAutomatonProxy}
   * objects, this array contains the same objects as {@link #getTRAutomata()},
   * otherwise this is not guaranteed.
   * @return The array of input automata in the order in which they are
   *         used in state tuples.
   */
  protected AutomatonProxy[] getInputAutomata()
  {
    return mInputAutomata;
  }

  /**
   * Retrieves a {@link TRAutomatonProxy} object used during analysis.
   * If the input model contains {@link TRAutomatonProxy} objects,
   * this method returns the same objects as {@link #getInputAutomaton(int)
   * getInputAutomaton()}, otherwise the results will be different.
   * @param  index  The index of the automaton to be retrieved. The index
   *                corresponds the the position in state tuples and to the
   *                array returned by {@link #getTRAutomata()}.
   * @return A {@link TRAutomatonProxy} object used by the algorithms,
   *         which may or may not appear in the input model.
   */
  protected TRAutomatonProxy getTRAutomaton(final int index)
  {
    return mTRAutomata[index];
  }

  /**
   * Retrieves the array of {@link TRAutomatonProxy} objects used during
   * analysis. This array is the subset of automata from the input model
   * selected for analysis, possibly converted to {@link TRAutomatonProxy}.
   * If the input model already contains {@link TRAutomatonProxy} objects,
   * the array returned by this method contains the same objects as
   * the input model, otherwise this is not guaranteed.
   * @return The array of {@link TRAutomatonProxy} objects in the order in
   *         which they are used in state tuples.
   */
  protected TRAutomatonProxy[] getTRAutomata()
  {
    return mTRAutomata;
  }

  protected Collection<EventInfo> getEventInfo()
  {
    return mEventInfo;
  }

  protected StateTupleEncoding getStateTupleEncoding()
  {
    return mStateTupleEncoding;
  }

  protected IntArrayBuffer getStateSpace()
  {
    return mStateSpace;
  }

  protected int getNumberOfInitialStates()
  {
    return mNumberOfInitialStates;
  }

  protected int getCurrentSource()
  {
    return mCurrentSource;
  }

  protected int getDeadlockState()
  {
    return mDeadlockState;
  }

  /**
   * Retrieves a state from the input model.
   * @param  autIndex    The index of the automaton to be examined. It
   *                     corresponds the the position in state tuples and
   *                     to the array returned by {@link #getTRAutomata()}.
   * @param  stateIndex  The number of the state to be retrieved. It
   *                     corresponds to the number stored in state tuples.
   * @return A {@link StateProxy} object corresponding to the identified
   *         state, which is guaranteed to appear in the input model.
   */
  protected StateProxy getInputState(final int autIndex, final int stateIndex)
  {
    final AutomatonProxy aut = mInputAutomata[autIndex];
    if (aut instanceof TRAutomatonProxy) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
      return tr.getState(stateIndex);
    } else {
      if (mInputStateArrays == null) {
        final int numAutomata = mInputAutomata.length;
        mInputStateArrays = new StateProxy[numAutomata][];
      }
      StateProxy[] array = mInputStateArrays[autIndex];
      if (array == null) {
        final Collection<StateProxy> states = aut.getStates();
        final int numStates = states.size();
        array = new StateProxy[numStates];
        mInputStateArrays[autIndex] = states.toArray(array);
      }
      return array[stateIndex];
    }
  }


  /**
   * Performs full state space exploration.
   * This methods adds all initial states to the state space, and then
   * enters a loop to calculate and record successor states. Exploration
   * continues until all reachable states and transitions have been processed,
   * or it can stop prematurely if the analysis result is found to have been
   * set.
   */
  protected void exploreStateSpace()
    throws AnalysisException
  {
    final AnalysisResult result = getAnalysisResult();
    storeInitialStates();
    for (int current = 0;
         current < mStateSpace.size() && !result.isFinished();
         current++) {
      checkAbort();
      expandState(current);
    }
  }

  /**
   * Expands the given state.
   * This method calculates all transitions originating from the given state,
   * creating new states and storing information as needed.
   * @param  encoded  Compressed state tuple to be expanded.
   * @param  decoded  Decompressed version of the same state tuple.
   */
  protected void expandState(final int[] encoded, final int[] decoded)
    throws AnalysisException
  {
    for (final EventInfo event : mEventInfo) {
      if (!expandState(encoded, decoded, event)) {
        break;
      }
    }
  }

  /**
   * Expands the transitions of the given event from the given state.
   * This method calculates all transitions of the given event originating
   * from the given state, creating new states and storing information as
   * needed.
   * @param  encoded  Compressed state tuple to be expanded.
   * @param  decoded  Decompressed version of the same state tuple.
   * @param  event    The event whose transitions are expanded.
   * @return <CODE>true</CODE> to indicate that exploration should continue
   *         after the call, <CODE>false</CODE> to indicate no further
   *         transitions originating from this state should be processed.
   */
  protected boolean expandState(final int[] encoded,
                                final int[] decoded,
                                final EventInfo event)
    throws AnalysisException
  {
    final AutomatonEventInfo disablingAut = event.findDisabling(decoded);
    if (disablingAut == null) {
      if (mPruningDeadlocks && event.isForbidden()) {
        final int e = event.getOutputCode();
        createDeadlockTransition(e);
        if (event.isOutsideAlwaysEnabled()) {
          return false;
        }
      } else {
        // TODO Add Boolean return value to abandon state.
        createSuccessorStates(encoded, decoded, event);
      }
    } else if (isSensitiveToControllability() &&
               !event.isControllable() &&
               !disablingAut.isPlant()) {
      final int e = event.getOutputCode();
      final int a = disablingAut.getAutomatonIndex();
      if (!handleUncontrollableState(e, a)) {
        return false;
      }
    }
    return true;
  }

  protected int storeInitialStates()
    throws AnalysisException
  {
    final TIntArrayList nondeterministicIndices = new TIntArrayList();
    for (int a = 0; a < mTRAutomata.length; a++) {
      final TRAutomatonProxy aut = mTRAutomata[a];
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      boolean found = false;
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          if (!found) {
            mDecodedTarget[a] = s;
            found = true;
          } else if (supportsNondeterminism()) {
            nondeterministicIndices.add(a);
            break;
          } else {
            final StateProxy state = aut.getState(s);
            throw new NondeterministicDESException(aut, state);
          }
        }
      }
      if (!found) {
        return 0;
      }
    }
    storeInitialStates(nondeterministicIndices, 0);
    return mNumberOfInitialStates = mStateSpace.size();
  }

  protected void createSuccessorStates(final int[] encoded,
                                       final int[] decoded,
                                       final EventInfo event)
    throws OverflowException
  {
    if (mStateCallback == null) {
      System.arraycopy(encoded, 0, mEncodedTarget, 0, encoded.length);
      event.createSuccessorStatesEncoded(mEncodedTarget, this);
    } else {
      System.arraycopy(decoded, 0, mDecodedTarget, 0, decoded.length);
      event.createSuccessorStatesDecoded(mDecodedTarget, this);
    }
  }

  public MarkingInfo getMarkingInfo(final EventProxy prop)
  {
    return new OnTheFlyMarkingInfo(prop);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void addMergedEventInfo(final List<EventInfo> group)
    throws AnalysisException
  {
    switch (group.size()) {
    case 0:
      break;
    case 1:
      mEventInfo.addAll(group);
      break;
    default:
      final EventInfo first = group.get(0);
      final int a = first.getLocalAutomatonIndex();
      final TRAutomatonProxy aut = mTRAutomata[a];
      final DeadlockInfo deadlockInfo =
        mDeadlockInfo == null ? null : mDeadlockInfo[a];
      final EventInfo merged = new EventInfo(group, aut, deadlockInfo, true);
      mEventInfo.add(merged);
      break;
    }
  }

  private void storeInitialStates(final TIntArrayList nondeterministicIndices,
                                  final int index)
    throws OverflowException
  {
    if (index < nondeterministicIndices.size()) {
      final int a = nondeterministicIndices.get(index);
      final TRAutomatonProxy aut = mTRAutomata[a];
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          mDecodedTarget[a] = s;
          storeInitialStates(nondeterministicIndices, index + 1);
        }
      }
    } else {
      createNewStateDecoded(mDecodedTarget);
    }
  }

  /**
   * Expands the given state.
   * This method calculates all transitions originating from the given state,
   * creating new states and storing information as needed.
   * @param  source  Number of state to be expanded.
   */
  public void expandState(final int source)
    throws AnalysisException
  {
    if (source != mDeadlockState) {
      mCurrentSource = source;
      mStateSpace.getContents(source, mEncodedSource);
      mStateTupleEncoding.decode(mEncodedSource, mDecodedSource);
      expandState(mEncodedSource, mDecodedSource);
    }
  }

  private int createDeadlockState() throws OverflowException
  {
    if (mDeadlockState < 0) {
      final int numWords = mStateTupleEncoding.getNumberOfWords();
      final int[] encodedDeadlock = new int[numWords];
      mDeadlockState =
        createNewStateDecoded(mDecodedDeadlockState, encodedDeadlock);
    }
    return mDeadlockState;
  }

  private int createNewStateDecoded(final int[] decoded)
    throws OverflowException
  {
    return createNewStateDecoded(decoded, mEncodedTarget);
  }

  protected int createNewStateEncoded(final int[] decoded,
                                      final int[] encoded)
    throws OverflowException
  {
    if (mStateCallback == null) {
      return createNewStateEncoded(encoded);
    } else {
      return createNewStateDecoded(decoded, encoded);
    }
  }

  protected int createNewStateDecoded(final int[] decoded,
                                      final int[] encoded)
    throws OverflowException
  {
    if (mStateCallback == null || mStateCallback.newState(decoded)) {
      mStateTupleEncoding.encode(decoded, encoded);
      return createNewStateEncoded(encoded);
    } else {
      return -1;
    }
  }

  private int createNewStateEncoded(final int[] encoded)
    throws OverflowException
  {
    return mStateSpace.add(encoded);
  }

  private void createDeadlockTransition(final int event)
    throws OverflowException
  {
    createDeadlockState();
    createTransition(event, mDeadlockState);
  }

  /**
   * Callback for new transitions. This method is called whenever state
   * exploration has discovered a new transition. The event and target
   * state numbers are provided as arguments. The source state of the
   * transition is obtained by calling {@link #getCurrentSource()}.
   */
  protected void createTransition(final int event, final int target)
    throws OverflowException
  {
  }

  /**
   * Callback for uncontrollable states. This method is called when state
   * exploration has discovered an uncontrollable state, i.e., a state
   * where an uncontrollable event is enabled in all plants but not in
   * all specifications. The number of the uncontrollable event and
   * the automaton index of a specification disabling this event are provided
   * as arguments. The uncontrollable source state is obtained by calling
   * {@link #getCurrentSource()}. This callback is only called for analysers
   * that enable controllability checking by overriding {@link
   * #isSensitiveToControllability()}.
   * @param  event  The uncontrollable event that causes controllability to
   *                fail, identified by its event index in the output event
   *                encoding.
   * @param  spec   The first specification that disables the uncontrollable
   *                event, identified by its index in the list of input
   *                automata.
   * @return <CODE>true</CODE> if state exploration should continue after
   *         this uncontrollable state, <CODE>false</CODE> to stop
   *         exploration immediately.
   */
  protected boolean handleUncontrollableState(final int event, final int spec)
    throws AnalysisException
  {
    return true;
  }

  private static void createSuccessorStatesEncoded
    (final AutomatonEventInfo updateSequence,
     final int event,
     final int[] encodedTarget,
     final boolean suppressedSelfloop,
     final AbstractTRMonolithicModelAnalyzer builder)
    throws OverflowException
  {
    if (updateSequence != null) {
      updateSequence.createSuccessorStatesEncoded
        (event, encodedTarget, suppressedSelfloop, builder);
    } else if (!suppressedSelfloop) {
      final int target = builder.createNewStateEncoded(encodedTarget);
      builder.createTransition(event, target);
    }
  }

  private static void createSuccessorStatesDecoded
    (final AutomatonEventInfo updateSequence,
     final int event,
     final int[] decodedTarget,
     final boolean suppressedSelfloop,
     final AbstractTRMonolithicModelAnalyzer builder)
    throws OverflowException
  {
    if (updateSequence != null) {
      updateSequence.createSuccessorStatesDecoded
        (event, decodedTarget, suppressedSelfloop, builder);
    } else if (!suppressedSelfloop) {
      final int target = builder.createNewStateDecoded(decodedTarget);
      builder.createTransition(event, target);
    }
  }

  private boolean isDeadlockState(final int autIndex, final int state)
  {
    if (mDeadlockInfo == null || mDeadlockInfo[autIndex] == null) {
      return false;
    } else {
      return mDeadlockInfo[autIndex].isDeadlockState(state);
    }
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static int getLastReachableState
    (final ListBufferTransitionRelation rel)
  {
    for (int s = rel.getNumberOfStates() - 1; s >= 0; s--) {
      if (rel.isReachable(s)) {
        return s;
      }
    }
    return -1;
  }

  private static int getAlternativeDumpState
    (final ListBufferTransitionRelation rel, final int numStates)
  {
    final int dumpIndex = rel.getDumpStateIndex();
    if (dumpIndex < numStates) {
      return dumpIndex;
    }
    final int topIndex = AutomatonTools.log2(numStates);
    if (dumpIndex < topIndex) {
      return dumpIndex;
    } else if (numStates < topIndex) {
      return numStates;
    }
    for (int s = 0; s < numStates; s++) {
      if (!rel.isReachable(s)) {
        return s;
      }
    }
    return -1;
  }


  //#########################################################################
  //# Error Handling
  /**
   * Checks the event alphabets of all input automata to see whether all
   * used non-proposition events have associated event information.
   * @param  eventInfoMap  Event information map that contains an entry for
   *                       all recognised events
   * @throws EventNotFoundException to indicate that some input automaton
   *                       uses an event not mapped to anything in the map.
   */
  private void checkEventsInAutomta(final Map<EventProxy,EventInfo> eventInfoMap)
    throws EventNotFoundException
  {
    for (final TRAutomatonProxy aut : mTRAutomata) {
      final EventEncoding enc = aut.getEventEncoding();
      final int numLocalEvents = enc.getNumberOfProperEvents();
      for (int local = EventEncoding.TAU; local < numLocalEvents; local++) {
        final byte status = enc.getProperEventStatus(local);
        if (EventStatus.isUsedEvent(status)) {
          final EventProxy event = enc.getProperEvent(local);
          if (event != null && !eventInfoMap.containsKey(event)) {
            final ProductDESProxy des = getModel();
            throw new EventNotFoundException(des, event.getName());
          }
        }
      }
    }
  }


  //#########################################################################
  //# Local Interface StateCallback
  /**
   * A callback interface to enable the user to perform custom actions
   * when a new state is encountered.
   */
  public interface StateCallback
  {
    /**
     * This method is called by the {@link AbstractTRMonolithicModelAnalyzer}
     * before adding a new state to the synchronous product state space.
     * @param  tuple    Integer array representing state codes of a new
     *                  state tuple.
     * @return <CODE>true</CODE> if the state should be included in the
     *         synchronous product, <CODE>false</CODE> if it should be
     *         suppressed.
     */
    public boolean newState(int[] tuple) throws OverflowException;
  }


  //#########################################################################
  //# Inner Interface MarkingInfo
  public interface MarkingInfo
  {
    //#######################################################################
    //# Access
    public boolean isTrivial();

    public boolean isMarkedState(final int state);

    public boolean isMarkedStateEncoded(int[] encoded);

    public boolean isMarkedStateDecoded(int[] decoded);
  }


  //#########################################################################
  //# Inner Class OnTheFlyMarkingInfo
  public class OnTheFlyMarkingInfo implements MarkingInfo
  {
    //#######################################################################
    //# Constructor
    public OnTheFlyMarkingInfo(final EventProxy prop)
    {
      final int globalP = mOutputEventEncoding.getEventCode(prop);
      if (globalP < 0) {
        mMarkingInfo = Collections.emptyList();
      } else {
        mMarkingInfo  = new ArrayList<>(mTRAutomata.length);
        int a = 0;
        for (final TRAutomatonProxy localAut : mTRAutomata) {
          final EventEncoding enc = localAut.getEventEncoding();
          final int localP = enc.getEventCode(prop);
          if (localP >= 0) {
            final ListBufferTransitionRelation localRel =
              localAut.getTransitionRelation();
            final AutomatonMarkingInfo info = new AutomatonMarkingInfo(a, localRel, localP);
            mMarkingInfo.add(info);
          }
          a++;
        }
        Collections.sort(mMarkingInfo );
      }
      final int encodedSize = mStateTupleEncoding.getNumberOfWords();
      mEncodedTuple = new int[encodedSize];
    }

    //#######################################################################
    //# Overrides for
    //# TRAbstractSynchronousProductBuilder.MarkingInfo
    @Override
    public boolean isTrivial()
    {
      return mMarkingInfo.isEmpty();
    }

    @Override
    public boolean isMarkedState(final int state)
    {
      mStateSpace.getContents(state, mEncodedTuple);
      return isMarkedStateEncoded(mEncodedTuple);
    }

    @Override
    public boolean isMarkedStateEncoded(final int[] encoded)
    {
      for (final AutomatonMarkingInfo info : mMarkingInfo) {
        final int a = info.getAutomatonIndex();
        final int s = mStateTupleEncoding.get(encoded, a);
        if (!info.isMarked(s)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean isMarkedStateDecoded(final int[] decoded)
    {
      for (final AutomatonMarkingInfo info : mMarkingInfo) {
        final int a = info.getAutomatonIndex();
        final int s = decoded[a];
        if (!info.isMarked(s)) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Data Members
    private final List<AutomatonMarkingInfo> mMarkingInfo;
    private final int[] mEncodedTuple;
  }


  //#########################################################################
  //# Inner Class StoredMarkingInfo
  public class StoredMarkingInfo implements MarkingInfo
  {
    //#######################################################################
    //# Constructor
    public StoredMarkingInfo()
    {
      mMarkedStates = new TIntHashSet();
      final int encodedSize = mStateTupleEncoding.getNumberOfWords();
      mEncodedTuple = new int[encodedSize];
    }

    //#######################################################################
    //# Overrides for
    //# TRAbstractSynchronousProductBuilder.MarkingInfo
    @Override
    public boolean isTrivial()
    {
      return mMarkedStates.size() == mStateSpace.size();
    }

    @Override
    public boolean isMarkedState(final int state)
    {
      return mMarkedStates.contains(state);
    }

    @Override
    public boolean isMarkedStateEncoded(final int[] encoded)
    {
      final int state = mStateSpace.getIndex(encoded);
      return isMarkedState(state);
    }

    @Override
    public boolean isMarkedStateDecoded(final int[] decoded)
    {
      mStateTupleEncoding.encode(decoded, mEncodedTuple);
      return isMarkedStateEncoded(mEncodedTuple);
    }

    //#######################################################################
    //# Access
    public void setMarked(final int state)
    {
      mMarkedStates.add(state);
    }

    //#######################################################################
    //# Data Members
    private final TIntHashSet mMarkedStates;
    private final int[] mEncodedTuple;
  }


  //#########################################################################
  //# Inner Class AutomatonMarkingInfo
  private static class AutomatonMarkingInfo
    implements Comparable<AutomatonMarkingInfo>
  {
    //#######################################################################
    //# Constructor
    private AutomatonMarkingInfo(final int autIndex,
                        final ListBufferTransitionRelation rel,
                        final int prop)
    {
      mAutomatonIndex = autIndex;
      mTransitionRelation = rel;
      mProposition = prop;
      mProbability = (float) rel.getNumberOfMarkings(prop, true) /
                     (float) rel.getNumberOfReachableStates();
    }

    //#######################################################################
    //# Access
    private int getAutomatonIndex()
    {
      return mAutomatonIndex;
    }

    private boolean isMarked(final int state)
    {
      return mTransitionRelation.isMarked(state, mProposition);
    }

    //#######################################################################
    //# Interface java.util.Comparable<MarkingInfo>
    @Override
    public int compareTo(final AutomatonMarkingInfo info)
    {
      if (mProbability < info.mProbability) {
        return -1;
      } else if (mProbability > info.mProbability) {
        return 1;
      } else {
        return mAutomatonIndex - info.mAutomatonIndex;
      }
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonIndex;
    private final ListBufferTransitionRelation mTransitionRelation;
    private final int mProposition;
    private final float mProbability;
  }


  //#########################################################################
  //# Inner Class EventInfo
  public static class EventInfo
    implements Comparable<EventInfo>
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final EventProxy event, final boolean controllable)
    {
      mEvent = event;
      mOutputCode = -1;
      mStatus =
        controllable ? EventStatus.STATUS_CONTROLLABLE : EventStatus.STATUS_NONE;
      mBlocked = false;
      mDisablingAutomata = new ArrayList<>();
      mUpdatingAutomata = new ArrayList<>();
      mUpdateSequence = null;
    }

    private EventInfo(final Collection<EventInfo> parts,
                      final TRAutomatonProxy aut,
                      final DeadlockInfo deadlockInfo,
                      final boolean nondeterminismSupported)
      throws AnalysisException
    {
      mEvent = null;
      mBlocked = false;
      final Collection<AutomatonEventInfo> autParts =
        new ArrayList<>(parts.size());
      boolean first = true;
      boolean controllable = true;
      for (final EventInfo part : parts) {
        mOutputCode = part.mOutputCode;
        assert part.mDisablingAutomata.size() == 1;
        final AutomatonEventInfo autPart = part.mDisablingAutomata.get(0);
        autParts.add(autPart);
        if (first) {
          controllable = part.isControllable();
          first = false;
        } else {
          assert controllable == part.isControllable();
        }
      }
      mStatus =
        controllable ? EventStatus.STATUS_CONTROLLABLE : EventStatus.STATUS_NONE;
      final AutomatonEventInfo merged =
        new AutomatonEventInfo(autParts, aut,
                               deadlockInfo, nondeterminismSupported);
      mDisablingAutomata = Collections.singletonList(merged);
      if (merged.isSelfloopOnly()) {
        mUpdatingAutomata = Collections.emptyList();
        mUpdateSequence = null;
      } else {
        mUpdatingAutomata = mDisablingAutomata;
        mUpdateSequence = merged;
      }
    }

    //#######################################################################
    //# Simple Access
    public EventProxy getEvent()
    {
      return mEvent;
    }

    public int getOutputCode()
    {
      return mOutputCode;
    }

    public boolean isBlocked()
    {
      return mBlocked;
    }

    public boolean isControllable()
    {
      return EventStatus.isControllableEvent(mStatus);
    }

    public boolean isLocal()
    {
      return EventStatus.isLocalEvent(mStatus);
    }

    public boolean isForbidden()
    {
      return EventStatus.isFailingEvent(mStatus);
    }

    public boolean isOutsideAlwaysEnabled()
    {
      return EventStatus.isAlwaysEnabledEvent(mStatus);
    }

    //#######################################################################
    //# Strongly Local and Strongly Forbidden
    public boolean isStronglyForbidden()
    {
      final byte pattern =
        EventStatus.STATUS_FAILING | EventStatus.STATUS_ALWAYS_ENABLED;
      return (mStatus & pattern) == pattern;
    }

    public boolean isStronglyLocal()
    {
      return
        EventStatus.isLocalEvent(mStatus) && mDisablingAutomata.size() == 1;
    }

    public boolean isGloballyAlwaysEnabled()
    {
      return mDisablingAutomata.isEmpty() && !mBlocked;
    }

    public int getLocalAutomatonIndex()
    {
      if (mDisablingAutomata.size() == 1) {
        final AutomatonEventInfo info = mDisablingAutomata.get(0);
        return info.getAutomatonIndex();
      } else {
        return -1;
      }
    }

    public AutomatonEventInfo getLocalAutomatonInfo()
    {
      if (mDisablingAutomata.size() == 1) {
        return mDisablingAutomata.get(0);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Uncontrollability
    public boolean canCauseUncontrollability()
    {
      if (!isControllable()) {
        for (final AutomatonEventInfo info : mDisablingAutomata) {
          if (!info.isPlant() && info.getProbability() < 1.0f) {
            return true;
          }
        }
      }
      return false;
    }

    public void setControllable()
    {
      if (!isControllable()) {
        mStatus |= EventStatus.STATUS_CONTROLLABLE;
        for (final AutomatonEventInfo info : mDisablingAutomata) {
          info.setControllable(true);
        }
        Collections.sort(mDisablingAutomata);
        for (final AutomatonEventInfo info : mUpdatingAutomata) {
          info.setControllable(true);
        }
      }
    }

    //#######################################################################
    //# Merging of Local Events
    private boolean isMergible(final EventInfo info)
    {
      final int index = getLocalAutomatonIndex();
      if (index >= 0) {
        return index == info.getLocalAutomatonIndex() &&
               mOutputCode == info.mOutputCode;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Setup
    private void reset()
    {
      mStatus = EventStatus.STATUS_CONTROLLABLE; // for reverse
      mBlocked = false;
      mDisablingAutomata.clear();
      mUpdatingAutomata.clear();
      mUpdateSequence = null;
    }

    private void setOutputCode(final int e)
    {
      mOutputCode = e;
    }

    private void setStatus(final byte status)
    {
      mStatus = status;
    }

    private void addAutomatonEventInfo(final AutomatonEventInfo info)
    {
      if (mBlocked) {
        // nothing ...
      } else if (info.isBlocked() &&
                 (isControllable() || info.isPlant())) {
        mBlocked = true;
        mDisablingAutomata.clear();
        mUpdatingAutomata.clear();
      } else {
        mDisablingAutomata.add(info);
        if (!info.isSelfloopOnly()) {
          mUpdatingAutomata.add(info);
        }
      }
    }

    private void sort()
    {
      Collections.sort(mDisablingAutomata);
      final int numUpdates = mUpdatingAutomata.size();
      final List<AutomatonEventInfo> deterministicUpdates =
        new ArrayList<>(numUpdates);
      final List<AutomatonEventInfo> nondeterministicUpdates =
        new ArrayList<>(numUpdates);
      for (final AutomatonEventInfo info : mUpdatingAutomata) {
        if (info.isDetermistic()) {
          deterministicUpdates.add(info);
        } else {
          nondeterministicUpdates.add(info);
        }
      }
      prependUpdateSequence(nondeterministicUpdates);
      prependUpdateSequence(deterministicUpdates);
    }

    private void prependUpdateSequence(final List<AutomatonEventInfo> updates)
    {
      final int end = updates.size();
      final ListIterator<AutomatonEventInfo> iter = updates.listIterator(end);
      while (iter.hasPrevious()) {
        final AutomatonEventInfo update = iter.previous();
        update.setNextUpdate(mUpdateSequence);
        mUpdateSequence = update;
      }
    }

    //#######################################################################
    //# State Expansion
    public AutomatonEventInfo findDisabling(final int[] decoded)
    {
      for (final AutomatonEventInfo info : mDisablingAutomata) {
        if (!info.isEnabled(decoded)) {
          return info;
        }
      }
      return null;
    }

    public void createSuccessorStatesEncoded
      (final int[] encodedTarget,
       final AbstractTRMonolithicModelAnalyzer builder)
      throws OverflowException
    {
      final boolean selfloopOnly = EventStatus.isSelfloopOnlyEvent(mStatus);
      AbstractTRMonolithicModelAnalyzer.createSuccessorStatesEncoded
        (mUpdateSequence, mOutputCode, encodedTarget, selfloopOnly, builder);
    }

    public void createSuccessorStatesDecoded
      (final int[] decodedTarget,
       final AbstractTRMonolithicModelAnalyzer builder)
      throws OverflowException
    {
      final boolean selfloopOnly = EventStatus.isSelfloopOnlyEvent(mStatus);
      AbstractTRMonolithicModelAnalyzer.createSuccessorStatesDecoded
        (mUpdateSequence, mOutputCode, decodedTarget, selfloopOnly, builder);
    }

    public TraceStepProxy buildFinalTraceStep
      (final AbstractTRMonolithicModelAnalyzer analyzer, final int s)
    {
      final int numAut = analyzer.mTRAutomata.length;
      final int[] encoded =
        new int[analyzer.mStateTupleEncoding.getNumberOfWords()];
      final int[] decoded = new int[numAut];
      analyzer.mStateSpace.getContents(s, encoded);
      analyzer.mStateTupleEncoding.decode(encoded, decoded);
      final Map<AutomatonProxy,StateProxy> stateMap = new HashMap<>(numAut);
      for (final AutomatonEventInfo info : mDisablingAutomata) {
        if (!info.isDetermistic()) {
          final int t = info.getFirstSuccessorState(s);
          if (t >= 0) {
            final int a = info.getAutomatonIndex();
            final AutomatonProxy aut = analyzer.getInputAutomaton(a);
            final StateProxy state = analyzer.getInputState(a, t);
            stateMap.put(aut, state);
          }
        }
      }
      final ProductDESProxyFactory factory = analyzer.getFactory();
      return factory.createTraceStepProxy(mEvent, stateMap);
    }


    //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
    {
      final boolean stronglyForbidden1 = isStronglyForbidden();
      final boolean stronglyForbidden2 = info.isStronglyForbidden();
      if (stronglyForbidden1 != stronglyForbidden2) {
        return stronglyForbidden1 ? -1 : 1;
      }
      final boolean controllable1 = isControllable();
      final boolean controllable2 = info.isControllable();
      if (controllable1 != controllable2) {
        return controllable1 ? 1 : -1;
      }
      final boolean stronglyLocal1 = isStronglyLocal();
      final boolean stronglyLocal2 = info.isStronglyLocal();
      if (stronglyLocal1 != stronglyLocal2) {
        return stronglyLocal1 ? -1 : 1;
      } else if (stronglyLocal1) {
        final int local1 = getLocalAutomatonIndex();
        final int local2 = info.getLocalAutomatonIndex();
        if (local1 != local2) {
          return local1 - local2;
        }
      }
      return mOutputCode - info.mOutputCode;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      if (mEvent != null) {
        return mEvent.getName();
      } else if (mUpdatingAutomata.size() == 1) {
        final AutomatonEventInfo info = mUpdatingAutomata.get(0);
        return "tau:" + info.mAutomatonIndex;
      } else {
        return "(null)";
      }
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private int mOutputCode;
    private byte mStatus;
    private boolean mBlocked;
    private final List<AutomatonEventInfo> mDisablingAutomata;
    private final List<AutomatonEventInfo> mUpdatingAutomata;
    private AutomatonEventInfo mUpdateSequence;
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  public static class AutomatonEventInfo
    implements Comparable<AutomatonEventInfo>
  {
    //#######################################################################
    //# Constructor
    private AutomatonEventInfo(final int autIndex,
                               final TRAutomatonProxy aut,
                               final boolean plant,
                               final int e,
                               final boolean controllable,
                               final DeadlockInfo deadlockInfo,
                               final boolean nondeterminismSupported)
      throws NondeterministicDESException
    {
      mAutomatonIndex = autIndex;
      mPlant = plant;
      mControllable = controllable;
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mTransitionIterator = rel.createSuccessorsReadOnlyIterator();
      mTransitionIterator.resetEvent(e);
      countTransitions(aut, deadlockInfo, true, nondeterminismSupported);
    }

    private AutomatonEventInfo(final Collection<AutomatonEventInfo> parts,
                               final TRAutomatonProxy aut,
                               final DeadlockInfo deadlockInfo,
                               final boolean nondeterminismSupported)
      throws AnalysisException
    {
      assert !parts.isEmpty();
      final AutomatonEventInfo first = parts.iterator().next();
      mAutomatonIndex = first.mAutomatonIndex;
      mPlant = first.mPlant;
      mControllable = first.mControllable;
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      final EventStatusProvider provider =
        new DefaultEventStatusProvider(numEvents, 0);
      for (int e = EventEncoding.TAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (!EventStatus.isUsedEvent(status)) {
          provider.setProperEventStatus(e, status);
        }
      }
      final byte status = mControllable ?
        EventStatus.STATUS_LOCAL | EventStatus.STATUS_CONTROLLABLE :
        EventStatus.STATUS_LOCAL;
      for (final AutomatonEventInfo part : parts) {
        assert mPlant == part.mPlant;
        assert mControllable == part.mControllable;
        final int e = part.mTransitionIterator.getFirstEvent();
        provider.setProperEventStatus(e, status);
      }
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      mTransitionIterator = new StatusGroupTransitionIterator
        (iter, provider, EventStatus.STATUS_LOCAL);
      countTransitions(aut, deadlockInfo, true, nondeterminismSupported);
    }

    private void countTransitions(final TRAutomatonProxy aut,
                                  final DeadlockInfo deadlockInfo,
                                  final boolean canBlock,
                                  final boolean nondeterminismSupported)
      throws NondeterministicDESException
    {
      mDeterministic = true;
      mSelfloopOnly = true;
      mBlocked = canBlock;
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      int numReachable = 0;
      int numEnabled = 0;
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) &&
            (deadlockInfo == null || !deadlockInfo.isDeadlockState(s))) {
          numReachable++;
          mTransitionIterator.resetState(s);
          int count = 0;
          while (count < 2 && mTransitionIterator.advance()) {
            final int t = mTransitionIterator.getCurrentTargetState();
            mSelfloopOnly &= (s == t);
            count++;
          }
          if (count > 0) {
            numEnabled++;
            mBlocked = false;
            if (count > 1) {
              if (nondeterminismSupported) {
                mDeterministic = false;
              } else {
                final StateProxy source = aut.getState(s);
                final int e = mTransitionIterator.getCurrentEvent();
                final EventProxy event = aut.getEventEncoding().getProperEvent(e);
                throw new NondeterministicDESException(aut, source, event);
              }
            }
          }
        }
      }
      if (numReachable > 0) {
        mProbability = (float) numEnabled / (float) numReachable;
      }
    }

    //#######################################################################
    //# Simple Access
    public int getAutomatonIndex()
    {
      return mAutomatonIndex;
    }

    public boolean isPlant()
    {
      return mPlant;
    }

    public boolean isControllable()
    {
      return mControllable;
    }

    public void setControllable(final boolean controllable)
    {
      mControllable = controllable;
    }

    public boolean isDetermistic()
    {
      return mDeterministic;
    }

    public boolean isSelfloopOnly()
    {
      return mSelfloopOnly;
    }

    public boolean isBlocked()
    {
      return mBlocked;
    }

    public float getProbability()
    {
      return mProbability;
    }

    public TransitionIterator getTransitionIterator()
    {
      return mTransitionIterator;
    }

    private void setNextUpdate(final AutomatonEventInfo next)
    {
      mNextUpdate = next;
    }


    //#######################################################################
    //# State Expansion
    public boolean isEnabled(final int[] decoded)
    {
      mTransitionIterator.resetState(decoded[mAutomatonIndex]);
      return mTransitionIterator.advance();
    }

    public void createSuccessorStatesEncoded
      (final int event,
       final int[] encodedTarget,
       final boolean suppressedSelfloop,
       final AbstractTRMonolithicModelAnalyzer builder)
      throws OverflowException
    {
      final int source = mTransitionIterator.getCurrentSourceState();
      if (mDeterministic) {
        final int target = mTransitionIterator.getCurrentTargetState();
        if (builder.isDeadlockState(mAutomatonIndex, target)) {
          builder.createDeadlockTransition(event);
        } else {
          builder.mStateTupleEncoding.set
            (encodedTarget, mAutomatonIndex, target);
          AbstractTRMonolithicModelAnalyzer.createSuccessorStatesEncoded
            (mNextUpdate, event, encodedTarget,
             suppressedSelfloop && source == target, builder);
        }
      } else {
        mTransitionIterator.reset();
        while (mTransitionIterator.advance()) {
          final int target = mTransitionIterator.getCurrentTargetState();
          if (builder.isDeadlockState(mAutomatonIndex, target)) {
            builder.createDeadlockTransition(event);
            break;
          } else {
            builder.mStateTupleEncoding.set
              (encodedTarget, mAutomatonIndex, target);
            AbstractTRMonolithicModelAnalyzer.createSuccessorStatesEncoded
              (mNextUpdate, event, encodedTarget,
               suppressedSelfloop && source == target, builder);
          }
        }
      }
    }

    public void createSuccessorStatesDecoded
      (final int event,
       final int[] decodedTarget,
       final boolean suppressedSelfloop,
       final AbstractTRMonolithicModelAnalyzer builder)
      throws OverflowException
    {
      final int source = mTransitionIterator.getCurrentSourceState();
      if (mDeterministic) {
        final int target = mTransitionIterator.getCurrentTargetState();
        if (builder.isDeadlockState(mAutomatonIndex, target)) {
          builder.createDeadlockTransition(event);
        } else {
          decodedTarget[mAutomatonIndex] = target;
          AbstractTRMonolithicModelAnalyzer.createSuccessorStatesDecoded
            (mNextUpdate, event, decodedTarget,
             suppressedSelfloop && source == target, builder);
        }
      } else {
        mTransitionIterator.reset();
        while (mTransitionIterator.advance()) {
          final int target = mTransitionIterator.getCurrentTargetState();
          if (builder.isDeadlockState(mAutomatonIndex, target)) {
            builder.createDeadlockTransition(event);
            break;
          } else {
            decodedTarget[mAutomatonIndex] = target;
            AbstractTRMonolithicModelAnalyzer.createSuccessorStatesDecoded
              (mNextUpdate, event, decodedTarget,
               suppressedSelfloop && source == target, builder);
          }
        }
      }
    }

    public int getFirstSuccessorState(final int state)
    {
      mTransitionIterator.resetState(state);
      if (mTransitionIterator.advance()) {
        return mTransitionIterator.getCurrentToState();
      } else {
        return -1;
      }
    }


    //#######################################################################
    //# Interface java.util.Comparable<AutomatonEventInfo>
    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (!mControllable) {
        final boolean prioritised1 = mPlant && mProbability < 1.0f;
        final boolean prioritised2 = info.mPlant && info.mProbability < 1.0f;
        if (prioritised1 && !prioritised2) {
          return -1;
        } else if (prioritised2 && !prioritised1) {
          return 1;
        }
      }
      if (mProbability < info.mProbability) {
        return -1;
      } else if (mProbability > info.mProbability) {
        return 1;
      } else {
        return mAutomatonIndex - info.mAutomatonIndex;
      }
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonIndex;
    private final boolean mPlant;
    private boolean mControllable;
    private boolean mDeterministic;
    private boolean mSelfloopOnly;
    private boolean mBlocked;
    private float mProbability;
    private final TransitionIterator mTransitionIterator;
    private AutomatonEventInfo mNextUpdate;
  }


  //#########################################################################
  //# Inner Class DeadlockInfo
  private static class DeadlockInfo
  {
    //#######################################################################
    //# Constructor
    private DeadlockInfo(final ListBufferTransitionRelation rel,
                         final long pattern)
    {
      mTransitionRelation = rel;
      mTransitionIterator = rel.createSuccessorsReadOnlyIterator();
      mPattern = pattern;
    }

    //#######################################################################
    //# Access
    private boolean isDeadlockState(final int state)
    {
      if ((mTransitionRelation.getAllMarkings(state) & mPattern) == 0) {
        mTransitionIterator.resetState(state);
        return !mTransitionIterator.advance();
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private final ListBufferTransitionRelation mTransitionRelation;
    private final TransitionIterator mTransitionIterator;
    private final long mPattern;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventEncoding mConfiguredEventEncoding = null;
  private boolean mHasConfiguredPropositions = false;
  private boolean mPruningDeadlocks = false;
  private StateCallback mStateCallback = null;

  // Data structures for state space representation
  private AutomatonProxy[] mInputAutomata;
  private TRAutomatonProxy[] mTRAutomata;
  private Collection<EventInfo> mEventInfo;
  private StateTupleEncoding mStateTupleEncoding;
  private IntArrayBuffer mStateSpace;
  private EventEncoding mOutputEventEncoding;
  private DeadlockInfo[] mDeadlockInfo;
  private int mNumberOfInitialStates;
  private StateProxy[][] mInputStateArrays;

  // Temporary variables
  private int mCurrentSource;
  private int[] mDecodedSource;
  private int[] mEncodedSource;
  private int[] mDecodedTarget;
  private int[] mEncodedTarget;
  private int[] mDecodedDeadlockState;
  private int mDeadlockState;


  //#########################################################################
  //# Class Constants
  private static final int MAX_TABLE_SIZE = 500000;

}
