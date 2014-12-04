//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductBuilder
//###########################################################################
//# $Id$
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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * A Java implementation of the monolithic synchronous product algorithm,
 * based on {@link ListBufferTransitionRelation} as automaton representation.
 *
 * @author Robi Malik
 */

public class TRSynchronousProductBuilder
  extends AbstractAutomatonBuilder
  implements SynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public TRSynchronousProductBuilder()
  {
    this(null);
  }

  public TRSynchronousProductBuilder(final ProductDESProxy model)
  {
    this(model, IdenticalKindTranslator.getInstance());
  }

  public TRSynchronousProductBuilder
    (final ProductDESProxy model,
     final KindTranslator translator)
  {
    super(model, ProductDESElementFactory.getInstance(), translator);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets an event encoding for the output transition relation.
   * The event encoding can request hiding, event renaming, and special event
   * types. Proper events not in the encoding are added automatically.
   * Events in the encoding but not in the input automata are marked as unused
   * in the output transition relation. Propositions not in the encoding are
   * suppressed in the output transition relation. If the event encoding is
   * left unspecified, a default event encoding with all events and
   * propositions in the input automata is used.
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
   * Retrieves the event encoding for the output transition relation.
   * @see #setEventEncoding(EventEncoding) setEventEncoding()
   */
  public EventEncoding getEventEncoding()
  {
    return mConfiguredEventEncoding;
  }

  /**
   * Sets whether redundant selfloops are to be removed.
   * If enabled, events that appear as selfloops on all states except dump
   * states and nowhere else are removed from the output, and markings
   * that appear on all states are also removed.
   */
  public void setRemovingSelfloops(final boolean removing)
  {
    mRemovingSelfloops = removing;
  }

  /**
   * Returns whether selfloops are removed.
   * @see #setRemovingSelfloops(boolean) setRemovingSelfloops()
   */
  public boolean getRemovingSelfloops()
  {
    return mRemovingSelfloops;
  }

  /**
   * <P>Sets whether deadlock states are pruned.</P>
   *
   * <P>If enabled, and there are propositions in the output automaton, the
   * synchronous product builder checks for deadlock states in the input
   * automata, i.e., for states that are not marked by any of the
   * propositions, and which do not have any outgoing transitions.
   * Synchronous product states, of which at least one state component is a
   * deadlock state, are not expanded and instead merged into the dump state
   * of the output automaton.</P>
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
  public boolean getPruningDeadlocks()
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


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  @Override
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

  @Override
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

  @Override
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement)
    throws OverflowException
  {
    if (mConfiguredEventEncoding == null) {
      mConfiguredEventEncoding = new EventEncoding();
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy alias : hidden) {
      mConfiguredEventEncoding.addEventAlias(alias, replacement, translator,
                                             EventStatus.STATUS_NONE);
    }
  }

  @Override
  public void clearMask()
  {
    if (mConfiguredEventEncoding == null) {
      // nothing
    } else if (mConfiguredEventEncoding.getNumberOfPropositions() == 0) {
      mConfiguredEventEncoding = null;
    } else {
      mConfiguredEventEncoding.removeAllProperEvents();
    }
  }

  @Override
  public TRSynchronousProductResult getAnalysisResult()
  {
    return (TRSynchronousProductResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  @Override
  public TRAutomatonProxy getComputedAutomaton()
  {
    return (TRAutomatonProxy) getComputedProxy();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Invocation
  @Override
  public TRSynchronousProductResult createAnalysisResult()
  {
    return new TRSynchronousProductResult();
  }

  @Override
  protected void setUp()
  throws AnalysisException
  {
    super.setUp();

    // Set up input automata and find their sizes
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int numAutomata = automata.size();
    mInputAutomata = new TRAutomatonProxy[numAutomata];
    mDecodedDeadlockState = new int[numAutomata];
    final int[] sizes = new int[numAutomata];
    int a = 0;
    boolean gotDump = false;
    for (final AutomatonProxy aut : automata) {
      final TRAutomatonProxy tr = TRAutomatonProxy.createTRAutomatonProxy
        (aut, translator, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mInputAutomata[a] = tr;
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
    if (numAutomata > 0 && !gotDump) {
      mDecodedDeadlockState[0] = sizes[0]++;
    }

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
    for (a = 0; a < numAutomata; a++) {
      final TRAutomatonProxy aut = mInputAutomata[a];
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
    }

    // Set up deadlock information ...
    if (mPruningDeadlocks &&
        mOutputEventEncoding.getNumberOfPropositions() > 0) {
      deadlock:
      for (a = 0; a < numAutomata; a++) {
        final TRAutomatonProxy aut = mInputAutomata[a];
        final EventEncoding enc = aut.getEventEncoding();
        final ListBufferTransitionRelation rel = aut.getTransitionRelation();
        final int numProps = mOutputEventEncoding.getNumberOfPropositions();
        long pattern = rel.createMarkings();
        for (int global = 0; global < numProps; global++) {
          if (mOutputEventEncoding.isPropositionUsed(global)) {
            final EventProxy prop = mOutputEventEncoding.getProposition(global);
            final int local = enc.getEventCode(prop);
            if (rel.isPropositionUsed(local)) {
              pattern = rel.addMarking(pattern, local);
            } else {
              continue deadlock;
            }
          }
        }
        final DeadlockInfo info = new DeadlockInfo(rel, pattern);
        final int numStates = rel.getNumberOfStates();
        for (int s = 0; s < numStates; s++) {
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
    mDeadlockState = -1;

    // Add proper events to output event encoding and create event info ...
    final Map<EventProxy,EventInfo> eventInfoMap = new HashMap<>(numEvents);
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        final EventInfo info = new EventInfo(event);
        eventInfoMap.put(event, info);
        if (mConfiguredEventEncoding != null) {
          final int e = mConfiguredEventEncoding.getEventCode(event);
          if (e >= 0) {
            final EventProxy output = mConfiguredEventEncoding.getProperEvent(e);
            outputEvents.add(output);
            final byte status = mConfiguredEventEncoding.getProperEventStatus(e);
            info.setForbidden(status);
          }
        }
      }
    }
    if (mConfiguredEventEncoding != null) {
      // Check for unused events in configured event encoding ...
      final int numConfigured =
        mConfiguredEventEncoding.getNumberOfProperEvents();
      for (int e = EventEncoding.TAU; e < numConfigured; e++) {
        final EventProxy event = mConfiguredEventEncoding.getProperEvent(e);
        if (event != null && !outputEvents.contains(event)) {
          final byte status = mConfiguredEventEncoding.getProperEventStatus(e);
          mOutputEventEncoding.setProperEventStatus
            (e, status | EventStatus.STATUS_UNUSED);
        }
      }
    }

    // Add transition information to event info ...
    final List<EventInfo> eventInfoList = new ArrayList<>(numEvents);
    for (a = 0; a < numAutomata; a++) {
      final TRAutomatonProxy aut = mInputAutomata[a];
      final DeadlockInfo deadlockInfo =
        mDeadlockInfo == null ? null : mDeadlockInfo[a];
      final EventEncoding enc = aut.getEventEncoding();
      final int numLocalEvents = enc.getNumberOfProperEvents();
      for (int local = EventEncoding.TAU; local < numLocalEvents; local++) {
        final byte status = enc.getProperEventStatus(local);
        if (EventStatus.isUsedEvent(status)) {
          final EventProxy event = enc.getProperEvent(local);
          if (event == null) {
            assert local == EventEncoding.TAU;
            final AutomatonEventInfo autInfo =
              new AutomatonEventInfo(a, aut, local, deadlockInfo);
            if (!autInfo.isBlocked()) {
              final EventInfo info = new EventInfo(null);
              info.addAutomatonEventInfo(autInfo);
              info.setOutputCode(EventEncoding.TAU);
              info.sort();
              eventInfoList.add(info);
              mOutputEventEncoding.setProperEventStatus(EventEncoding.TAU,
                                                        status);
            }
          } else {
            final int global =
              mOutputEventEncoding.addEvent(event, translator, status);
            final EventInfo info = eventInfoMap.get(event);
            if (info == null) {
              throw new EventNotFoundException(des, event.getName());
            } else if (!info.isBlocked()) {
              info.setOutputCode(global);
              final AutomatonEventInfo autInfo =
                new AutomatonEventInfo(a, aut, local, deadlockInfo);
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
    Collections.sort(eventInfoList);
    mEventInfo = eventInfoList;
    final int numOutputEvents = mOutputEventEncoding.getNumberOfProperEvents();
    final int transitionLimit = getTransitionLimit();
    mPreTransitionBuffer =
      new PreTransitionBuffer(numOutputEvents, transitionLimit);

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

  @Override
  public boolean run()
    throws AnalysisException
  {
    try {
      setUp();
      storeInitialStates();
      for (int current = 0; current < mStateSpace.size(); current++) {
        expandState(current);
      }
      createAutomaton();
      return true;
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
  protected void addStatistics()
  {
    super.addStatistics();
    final AutomatonResult result = getAnalysisResult();
    if (mInputAutomata != null) {
      result.setNumberOfAutomata(mInputAutomata.length);
    }
    if (mStateSpace != null) {
      result.setNumberOfStates(mStateSpace.size());
    }
    if (mPreTransitionBuffer != null) {
      result.setNumberOfTransitions(mPreTransitionBuffer.size());
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mInputAutomata = null;
    mEventInfo = null;
    mStateTupleEncoding = null;
    mStateSpace = null;
    mOutputEventEncoding = null;
    mPreTransitionBuffer = null;
    mDeadlockInfo = null;
    mCurrentTargets = null;
    mDecodedSource = null;
    mEncodedSource = null;
    mDecodedTarget = null;
    mEncodedTarget = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void storeInitialStates()
    throws OverflowException
  {
    final TIntArrayList nondeterministicIndices = new TIntArrayList();
    for (int a = 0; a < mInputAutomata.length; a++) {
      final TRAutomatonProxy aut = mInputAutomata[a];
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      boolean found = false;
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          if (found) {
            nondeterministicIndices.add(a);
          } else {
            mDecodedTarget[a] = s;
            found = true;
          }
        }
      }
    }
    storeInitialStates(nondeterministicIndices, 0);
    mNumberOfInitialStates = mStateSpace.size();
  }

  private void storeInitialStates(final TIntArrayList nondeterministicIndices,
                                  final int index)
    throws OverflowException
  {
    if (index < nondeterministicIndices.size()) {
      final int a = nondeterministicIndices.get(index);
      final TRAutomatonProxy aut = mInputAutomata[a];
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

  private void expandState(final int source)
    throws OverflowException
  {
    if (source != mDeadlockState) {
      mCurrentSource = source;
      mCurrentEvent = -1;
      mCurrentTargets = new TIntHashSet();
      mStateSpace.getContents(source, mEncodedSource);
      mStateTupleEncoding.decode(mEncodedSource, mDecodedSource);
      for (final EventInfo event : mEventInfo) {
        if (event.isEnabled(mDecodedSource)) {
          if (mPruningDeadlocks && event.isForbidden()) {
            final int e = event.getOutputCode();
            createDeadlockTransition(e);
            if (event.isOutsideAlwaysEnabled()) {
              break;
            }
          } else if (mStateCallback == null) {
            for (int w = 0; w < mEncodedSource.length; w++) {
              mEncodedTarget[w] = mEncodedSource[w];
            }
            event.createSuccessorStatesEncoded(mEncodedTarget, this);
          } else {
            for (int a = 0; a < mDecodedSource.length; a++) {
              mDecodedTarget[a] = mDecodedSource[a];
            }
            event.createSuccessorStatesDecoded(mDecodedTarget, this);
          }
        }
      }
    }
  }

  private int createDeadlockState() throws OverflowException
  {
    if (mDeadlockState < 0) {
      final int[] backup = mEncodedTarget;
      final int numWords = mStateTupleEncoding.getNumberOfWords();
      mEncodedTarget = new int[numWords];
      mDeadlockState = createNewStateDecoded(mDecodedDeadlockState);
      mEncodedTarget = backup;
    }
    return mDeadlockState;
  }

  private int createNewStateDecoded(final int[] decoded)
    throws OverflowException
  {
    if (mStateCallback == null || mStateCallback.newState(decoded)) {
      mStateTupleEncoding.encode(decoded, mEncodedTarget);
      return createNewStateEncoded(mEncodedTarget);
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

  private void createTransition(final int event, final int target)
    throws OverflowException
  {
    if (target >= 0 &&
        (event != EventEncoding.TAU || target != mCurrentSource)) {
      if (event != mCurrentEvent) {
        mCurrentEvent = event;
        mCurrentTargets.clear();
        mCurrentTargets.add(target);
        mPreTransitionBuffer.addTransition(mCurrentSource, event, target);
      } else if (mCurrentTargets.add(target)) {
        mPreTransitionBuffer.addTransition(mCurrentSource, event, target);
      }
    }
  }

  private static void createSuccessorStatesEncoded
    (final AutomatonEventInfo updateSequence,
     final int event,
     final int[] encodedTarget,
     final TRSynchronousProductBuilder builder)
    throws OverflowException
  {
    if (updateSequence == null) {
      final int target = builder.createNewStateEncoded(encodedTarget);
      builder.createTransition(event, target);
    } else {
      updateSequence.createSuccessorStatesEncoded
        (event, encodedTarget, builder);
    }
  }

  private static void createSuccessorStatesDecoded
    (final AutomatonEventInfo updateSequence,
     final int event,
     final int[] decodedTarget,
     final TRSynchronousProductBuilder builder)
    throws OverflowException
  {
    if (updateSequence == null) {
      final int target = builder.createNewStateDecoded(decodedTarget);
      builder.createTransition(event, target);
    } else {
      updateSequence.createSuccessorStatesDecoded
        (event, decodedTarget, builder);
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

  private void createAutomaton()
    throws OverflowException
  {
    if (isDetailedOutputEnabled()) {
      final ListBufferTransitionRelation rel;
      if (mDeadlockState >= 0) {
        rel = new ListBufferTransitionRelation(computeOutputName(),
                                               getOutputKind(),
                                               mOutputEventEncoding,
                                               mStateSpace.size(),
                                               mDeadlockState,
                                               ListBufferTransitionRelation.
                                               CONFIG_SUCCESSORS);
      } else {
        rel = new ListBufferTransitionRelation(computeOutputName(),
                                               getOutputKind(),
                                               mOutputEventEncoding,
                                               mStateSpace.size(),
                                               ListBufferTransitionRelation.
                                               CONFIG_SUCCESSORS);

      }
      for (int s = 0; s < mNumberOfInitialStates; s++) {
        rel.setInitial(s, true);
      }
      final int numProps = mOutputEventEncoding.getNumberOfPropositions();
      boolean hasProps = false;
      for (int p = 0; p < numProps; p++) {
        if (mOutputEventEncoding.isPropositionUsed(p)) {
          final EventProxy prop = mOutputEventEncoding.getProposition(p);
          addMarkings(rel, prop);
          hasProps = true;
        }
      }
      mPreTransitionBuffer.addOutgoingTransitions(rel);
      if (getRemovingSelfloops()) {
        rel.removeTauSelfLoops();
        if (getPruningDeadlocks() && hasProps) {
          removeSelfloopsConsideringDeadlocks(rel);
        } else {
          rel.removeProperSelfLoopEvents();
        }
      }
      final TRAutomatonProxy aut =
        new TRAutomatonProxy(mOutputEventEncoding, rel);
      final TRSynchronousProductResult result = getAnalysisResult();
      result.setComputedAutomaton(aut);
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> automata = model.getAutomata();
      final TRSynchronousProductStateMap stateMap =
        new TRSynchronousProductStateMap(automata, mStateTupleEncoding, mStateSpace);
      result.setStateMap(stateMap);
    }
  }

  private void addMarkings(final ListBufferTransitionRelation outputRel,
                           final EventProxy prop)
  {
    final int globalP = mOutputEventEncoding.getEventCode(prop);
    if (globalP < 0) {
      return;
    }
    final List<MarkingInfo> list = new ArrayList<>(mInputAutomata.length);
    int a = 0;
    for (final TRAutomatonProxy localAut : mInputAutomata) {
      final EventEncoding enc = localAut.getEventEncoding();
      final int localP = enc.getEventCode(prop);
      if (localP >= 0) {
        final ListBufferTransitionRelation localRel =
          localAut.getTransitionRelation();
        final MarkingInfo info = new MarkingInfo(a, localRel, localP);
        list.add(info);
      }
      a++;
    }
    if (list.isEmpty() && getRemovingSelfloops()) {
      return;
    }
    boolean allMarked = true;
    final int numStates = outputRel.getNumberOfStates();
    states:
    for (int globalS = 0; globalS < numStates; globalS++) {
      if (globalS != mDeadlockState) {
        mStateSpace.getContents(globalS, mEncodedSource);
        for (final MarkingInfo info : list) {
          a = info.getAutomatonIndex();
          final int localS = mStateTupleEncoding.get(mEncodedSource, a);
          if (!info.isMarked(localS)) {
            allMarked = false;
            continue states;
          }
        }
        outputRel.setMarked(globalS, globalP, true);
      }
    }
    if (!allMarked || !getRemovingSelfloops()) {
      outputRel.setPropositionUsed(globalP, true);
    }
  }

  private void removeSelfloopsConsideringDeadlocks
    (final ListBufferTransitionRelation rel)
  {
    final int numStates = rel.getNumberOfStates();
    final long props = rel.getUsedPropositions();
    final int numEvents = rel.getNumberOfProperEvents();
    final int[] progress = new int[numEvents];
    int expected = 0;
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    iter.resetEvents(EventEncoding.NONTAU, numEvents - 1);
    for (int s = 0; s < numStates; s++) {
      iter.resetState(s);
      if (iter.advance()) {
        do {
          final int e = iter.getCurrentEvent();
          final int t = iter.getCurrentTargetState();
          if (s != t) {
            progress[e] = -1;
          } else if (progress[e] == expected) {
            progress[e] = s + 1;
          }
        } while (iter.advance());
        expected = s + 1;
      } else if ((rel.getAllMarkings(s) & props) != 0) {
        return;
      }
    }
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      if (progress[e] == expected) {
        rel.removeEvent(e);
      }
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
  //# Local Interface StateCallback
  /**
   * A callback interface to enable the user to perform custom actions
   * when a new state is encountered.
   */
  public interface StateCallback
  {
    /**
     * This method is called by the {@link TRSynchronousProductBuilder}
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
  //# Inner Class EventInfo
  private static class EventInfo
    implements Comparable<EventInfo>
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final EventProxy event)
    {
      mEvent = event;
      mOutputCode = -1;
      mForbidden = mBlocked = mOutsideAlwaysEnabled = false;
      mDisablingAutomata = new ArrayList<>();
      mUpdatingAutomata = new ArrayList<>();
      mUpdateSequence = null;
    }

    //#######################################################################
    //# Simple Access
    private int getOutputCode()
    {
      return mOutputCode;
    }

    private boolean isBlocked()
    {
      return mBlocked;
    }

    private boolean isForbidden()
    {
      return mForbidden;
    }

    private boolean isOutsideAlwaysEnabled()
    {
      return mOutsideAlwaysEnabled;
    }

    //#######################################################################
    //# Setup
    private void setOutputCode(final int e)
    {
      mOutputCode = e;
    }

    private void setForbidden(final byte status)
    {
      mForbidden = EventStatus.isFailingEvent(status);
      mOutsideAlwaysEnabled = EventStatus.isAlwaysEnabledEvent(status);
    }

    private void addAutomatonEventInfo(final AutomatonEventInfo info)
    {
      if (mBlocked) {
        // nothing ...
      } else if (info.isBlocked()) {
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
    private boolean isEnabled(final int[] decoded)
    {
      for (final AutomatonEventInfo info : mDisablingAutomata) {
        if (!info.isEnabled(decoded)) {
          return false;
        }
      }
      return true;
    }

    private void createSuccessorStatesEncoded
      (final int[] encodedTarget,
       final TRSynchronousProductBuilder builder)
      throws OverflowException
    {
      TRSynchronousProductBuilder.createSuccessorStatesEncoded
        (mUpdateSequence, mOutputCode, encodedTarget, builder);
    }

    private void createSuccessorStatesDecoded
      (final int[] decodedTarget,
       final TRSynchronousProductBuilder builder)
      throws OverflowException
    {
      TRSynchronousProductBuilder.createSuccessorStatesDecoded
        (mUpdateSequence, mOutputCode, decodedTarget, builder);
    }

    //#######################################################################
    //# Interface java.util.Comparable<EventInfo>
    @Override
    public int compareTo(final EventInfo info)
    {
      final boolean stronglyForbidden1 =
        mForbidden && mOutsideAlwaysEnabled;
      final boolean stronglyForbidden2 =
        info.mForbidden && info.mOutsideAlwaysEnabled;
      if (stronglyForbidden1 != stronglyForbidden2) {
        return stronglyForbidden1 ? -1 : 1;
      } else {
        return mOutputCode - info.mOutputCode;
      }
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
    private boolean mForbidden;
    private boolean mOutsideAlwaysEnabled;
    private boolean mBlocked;
    private final List<AutomatonEventInfo> mDisablingAutomata;
    private final List<AutomatonEventInfo> mUpdatingAutomata;
    private AutomatonEventInfo mUpdateSequence;
  }


  //#########################################################################
  //# Inner Class AutomatonEventInfo
  private static class AutomatonEventInfo
    implements Comparable<AutomatonEventInfo>
  {
    //#######################################################################
    //# Constructor
    private AutomatonEventInfo(final int autIndex,
                               final TRAutomatonProxy aut,
                               final int e,
                               final DeadlockInfo deadlockInfo)
    {
      mAutomatonIndex = autIndex;
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      mDeterministic = true;
      mSelfloopOnly = true;
      mBlocked = true;
      mTransitionIterator = rel.createSuccessorsReadOnlyIterator();
      mTransitionIterator.resetEvent(e);
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
            mDeterministic &= (count == 1);
          }
        }
      }
      if (numReachable > 0) {
        mProbability = (float) numEnabled / (float) numReachable;
      }
    }

    //#######################################################################
    //# Simple Access
    private boolean isDetermistic()
    {
      return mDeterministic;
    }

    private boolean isSelfloopOnly()
    {
      return mSelfloopOnly;
    }

    private boolean isBlocked()
    {
      return mBlocked;
    }

    private void setNextUpdate(final AutomatonEventInfo next)
    {
      mNextUpdate = next;
    }

    //#######################################################################
    //# State Expansion
    private boolean isEnabled(final int[] decoded)
    {
      mTransitionIterator.resetState(decoded[mAutomatonIndex]);
      return mTransitionIterator.advance();
    }

    private void createSuccessorStatesEncoded
      (final int event,
       final int[] encodedTarget,
       final TRSynchronousProductBuilder builder)
      throws OverflowException
    {
      if (mDeterministic) {
        final int target = mTransitionIterator.getCurrentTargetState();
        if (builder.isDeadlockState(mAutomatonIndex, target)) {
          builder.createDeadlockTransition(event);
        } else {
          builder.mStateTupleEncoding.set
            (encodedTarget, mAutomatonIndex, target);
          TRSynchronousProductBuilder.createSuccessorStatesEncoded
            (mNextUpdate, event, encodedTarget, builder);
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
            TRSynchronousProductBuilder.createSuccessorStatesEncoded
              (mNextUpdate, event, encodedTarget, builder);
          }
        }
      }
    }

    private void createSuccessorStatesDecoded
      (final int event,
       final int[] decodedTarget,
       final TRSynchronousProductBuilder builder)
      throws OverflowException
    {
      if (mDeterministic) {
        final int target = mTransitionIterator.getCurrentTargetState();
        if (builder.isDeadlockState(mAutomatonIndex, target)) {
          builder.createDeadlockTransition(event);
        } else {
          decodedTarget[mAutomatonIndex] = target;
          TRSynchronousProductBuilder.createSuccessorStatesDecoded
            (mNextUpdate, event, decodedTarget, builder);
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
            TRSynchronousProductBuilder.createSuccessorStatesDecoded
              (mNextUpdate, event, decodedTarget, builder);
          }
        }
      }
    }

    //#######################################################################
    //# Interface java.util.Comparable<AutomatonEventInfo>
    @Override
    public int compareTo(final AutomatonEventInfo info)
    {
      if (mProbability < info.mProbability) {
        return -1;
      } else if (mProbability > info.mProbability) {
        return 1;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonIndex;
    private boolean mDeterministic;
    private boolean mSelfloopOnly;
    private boolean mBlocked;
    private float mProbability;
    private final TransitionIterator mTransitionIterator;
    private AutomatonEventInfo mNextUpdate;
  }


  //#########################################################################
  //# Inner Class MarkingInfo
  private static class MarkingInfo
  {
    //#######################################################################
    //# Constructor
    private MarkingInfo(final int autIndex,
                        final ListBufferTransitionRelation rel,
                        final int prop)
    {
      mAutomatonIndex = autIndex;
      mTransitionRelation = rel;
      mProposition = prop;
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
    //# Data Members
    private final int mAutomatonIndex;
    private final ListBufferTransitionRelation mTransitionRelation;
    private final int mProposition;
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
  private boolean mRemovingSelfloops = true;
  private boolean mPruningDeadlocks = false;
  private StateCallback mStateCallback = null;

  // Data structures for state space representation
  private TRAutomatonProxy[] mInputAutomata;
  private Collection<EventInfo> mEventInfo;
  private StateTupleEncoding mStateTupleEncoding;
  private IntArrayBuffer mStateSpace;
  private EventEncoding mOutputEventEncoding;
  private PreTransitionBuffer mPreTransitionBuffer;
  private DeadlockInfo[] mDeadlockInfo;
  private int mNumberOfInitialStates;

  // Temporary variables
  private int mCurrentSource;
  private int mCurrentEvent;
  private TIntHashSet mCurrentTargets;
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

