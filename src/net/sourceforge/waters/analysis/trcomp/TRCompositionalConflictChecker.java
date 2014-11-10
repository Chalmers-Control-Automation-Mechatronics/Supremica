//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRCompositionalConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import net.sourceforge.waters.analysis.abstraction.SpecialEventsFinder;
import net.sourceforge.waters.analysis.tr.DuplicateFreeQueue;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Robi Malik
 */

public class TRCompositionalConflictChecker
  extends AbstractModelAnalyzer
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public TRCompositionalConflictChecker(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory,
                                        final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public VerificationResult getAnalysisResult()
  {
    return (VerificationResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ModelVerifier
  @Override
  public void setCounterExampleEnabled(final boolean enable)
  {
    setDetailedOutputEnabled(enable);
  }

  @Override
  public boolean isCounterExampleEnabled()
  {
    return isDetailedOutputEnabled();
  }

  @Override
  public boolean isSatisfied()
  {
    final AnalysisResult result = getAnalysisResult();
    return result.isSatisfied();
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    final VerificationResult result = getAnalysisResult();
    return (ConflictTraceProxy) result.getCounterExample();
  }


  //#########################################################################
  //# Interface for net.sourceforge.waters.model.analysis.des.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredDefaultMarking = marking;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mConfiguredPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mConfiguredPreconditionMarking;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether blocked events are to be considered in abstraction.
   * @see #isBlockedEventsSupported()
   */
  public void setBlockedEventsSupported(final boolean enable)
  {
    mBlockedEventsSupported = enable;
  }

  /**
   * Returns whether blocked events are considered in abstraction.
   * Blocked events are events that are disabled in all reachable states of
   * some automaton. If supported, this will remove all transitions with
   * blocked events from the model.
   * @see #setBlockedEventsSupported(boolean) setBlockedEventsSupported()
   */
  public boolean isBlockedEventsSupported()
  {
    return mBlockedEventsSupported;
  }

  /**
   * Sets whether failing events are to be considered in abstraction.
   * @see #isFailingEventsSupported()
   */
  public void setFailingEventsSupported(final boolean enable)
  {
    mFailingEventsSupported = enable;
  }

  /**
   * Returns whether failing events are considered in abstraction.
   * Failing events are events that always lead to a dump state in some
   * automaton. If supported, this will redirect failing events in other
   * automata to dump states.
   * @see #setFailingEventsSupported(boolean) setFailingEventsSupported()
   */
  public boolean isFailingEventsSupported()
  {
    return mFailingEventsSupported;
  }

  /**
   * Sets whether selfloop-only events are to be considered in abstraction.
   * @see #isSelfloopOnlyEventsSupported()
   */
  public void setSelfloopOnlyEventsSupported(final boolean enable)
  {
    mSelfloopOnlyEventsSupported = enable;
  }

  /**
   * Returns whether selfloop-only events are considered in abstraction.
   * Selfloop-only events are events that appear only as selfloops in the
   * entire model or in all but one automaton in the model. Events that
   * are selfloop-only in the entire model can be removed, while events
   * that are selfloop-only in all but one automaton can be used to
   * simplify that automaton.
   * @see #setSelfloopOnlyEventsSupported(boolean) setSelfloopOnlyEventsSupported()
   */
  public boolean isSelfloopOnlyEventsSupported()
  {
    return mSelfloopOnlyEventsSupported;
  }

  /**
   * Sets whether always enabled events are to be considered in abstraction.
   * @see #isAlwaysEnabledEventsSupported()
   */
  public void setAlwaysEnabledEventsSupported(final boolean enable)
  {
    mAlwaysEnabledEventsSupported = enable;
  }

  /**
   * Returns whether always enabled events are considered in abstraction.
   * Always enabled events are events that are enabled in all states of the
   * entire model or of all but one automaton in the model. Always enabled
   * events can help to simplify automata.
   * @see #setAlwaysEnabledEventsSupported(boolean) setAlwaysEnabledEventsSupported()
   * @see #isControllabilityConsidered()
   */
  public boolean isAlwaysEnabledEventsSupported()
  {
    return mAlwaysEnabledEventsSupported;
  }


  //#########################################################################
  //# Hooks
  /**
   * Returns whether simplification needs to distinguish controllable and
   * uncontrollable events. If this is the case, it can affect how events
   * are encoded, and how special events are recognised. For example,
   * only uncontrollable events are ever considered as always enabled.
   * @see #isAlwaysEnabledEventsSupported()
   */
  protected boolean isControllabilityConsidered()
  {
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final ProductDESProxy model = getModel();
    if (mConfiguredDefaultMarking == null) {
      mUsedDefaultMarking = AbstractConflictChecker.getMarkingProposition(model);
    } else {
      mUsedDefaultMarking = mConfiguredDefaultMarking;
    }
    // TODO Generalised nonblocking ...
    final Collection<EventProxy> markings =
      Collections.singleton(mUsedDefaultMarking);
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final Collection<TRAutomatonProxy> trs = new ArrayList<>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      if (isProperAutomaton(aut)) {
        final EventEncoding eventEnc = createInitialEventEncoding(aut);
        final StateProxy dumpState = AutomatonTools.findDumpState(aut, markings);
        final TRAutomatonProxy tr =
          new TRAutomatonProxy(aut, eventEnc, dumpState, INITIAL_CONFIG);
        trs.add(tr);
      }
    }
    final int numEvents = model.getEvents().size();
    mSpecialEventsFinder = new SpecialEventsFinder();
    mSpecialEventsFinder.setDefaultMarkingID(DEFAULT_MARKING);
    mSpecialEventsFinder.setBlockedEventsDetected(mBlockedEventsSupported);
    mSpecialEventsFinder.setFailingEventsDetected(mFailingEventsSupported);
    mSpecialEventsFinder.setSelfloopOnlyEventsDetected(mSelfloopOnlyEventsSupported);
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(false);
    mSpecialEventsFinder.setControllabilityConsidered(isControllabilityConsidered());
    mCurrentSubsystem = new SubsystemInfo(trs, numEvents);
    for (final TRAutomatonProxy aut : trs) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mSpecialEventsFinder.setTransitionRelation(rel);
      mSpecialEventsFinder.run();
      final byte[] status = mSpecialEventsFinder.getComputedEventStatus();
      mCurrentSubsystem.registerEvents(aut, status);
    }
    mSpecialEventsFinder.setAlwaysEnabledEventsDetected(mAlwaysEnabledEventsSupported);
    mSubsystemQueue = new PriorityQueue<>();
    mNeedsSimplification = new DuplicateFreeQueue<>(trs);
    mNeedsDisjointSubsystemsCheck = true;
    mAlwaysEnabledDetectedInitially = !mAlwaysEnabledEventsSupported;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final AnalysisResult result = getAnalysisResult();
      do {
        analyseCurrentSubsystemCompositionally();
        if (result.isFinished()) {
          // TODO Trace expansion ???
          return result.isSatisfied();
        }
        mCurrentSubsystem = mSubsystemQueue.poll();
      } while (mCurrentSubsystem != null);
      result.setSatisfied(true);
      return true;
    } catch (final OutOfMemoryError error) {
      throw new OverflowException(error);
    } catch (final StackOverflowError error) {
      throw new OverflowException(error);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSpecialEventsFinder = null;
    mUsedDefaultMarking = null;
    mSubsystemQueue = null;
    mCurrentSubsystem = null;
    mNeedsSimplification = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mSpecialEventsFinder != null) {
      mSpecialEventsFinder.resetAbort();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventEncoding createInitialEventEncoding(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding enc = new EventEncoding();
    enc.addEvent(mUsedDefaultMarking, translator, EventStatus.STATUS_UNUSED);
    if (mConfiguredPreconditionMarking != null) {
      enc.addEvent(mConfiguredPreconditionMarking, translator,
                   EventStatus.STATUS_UNUSED);
    }
    for (final EventProxy event : aut.getEvents()) {
      enc.addEvent(event, translator, EventStatus.STATUS_NONE);
    }
    return enc;
  }

  private void analyseCurrentSubsystemCompositionally()
    throws AnalysisException
  {
    while (mCurrentSubsystem.getNumberOfAutomata() >= 2) {
      checkAbort();
      if (earlyTerminationCheckCurrentSubsystem()) {
        return;
      }
      final boolean simplified = simplifyAllAutomataIndividually();
      if (simplified && earlyTerminationCheckCurrentSubsystem()) {
        return;
      } else if (disjointSubsystemsCheck()) {
        return;
      } else if (mCurrentSubsystem.getNumberOfAutomata() == 2) {
        break;
      }
      // TODO candidate selection & simplification
    }
    analyseCurrentSubsystemMonolithically();
  }

  private boolean earlyTerminationCheckCurrentSubsystem()
  {
    boolean allMarked = true;
    outer:
    for (final TRAutomatonProxy aut : mCurrentSubsystem.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(DEFAULT_MARKING)) {
        allMarked = false;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s) && rel.isMarked(s, DEFAULT_MARKING)) {
            continue outer;
          }
        }
        final AnalysisResult result = getAnalysisResult();
        result.setSatisfied(false);
        return true;
      }
    }
    // TODO Generalised nonblocking stuff ...
    return allMarked;
  }

  private boolean simplifyAllAutomataIndividually()
    throws AnalysisException
  {
    boolean simplified = false;
    int remaining =
      mAlwaysEnabledDetectedInitially ? 0 : mNeedsSimplification.size();
    while (!mNeedsSimplification.isEmpty()) {
      final TRAutomatonProxy aut = mNeedsSimplification.poll();
      simplified |= simplifyAutomatonIndividually(aut);
      if (remaining > 0) {
        mAlwaysEnabledDetectedInitially = (--remaining == 0);
      }
      // TODO Update automaton information?
    }
    return simplified;
  }

  private boolean simplifyAutomatonIndividually(final TRAutomatonProxy aut)
    throws AnalysisException
  {
    // Set event status ...
    final EventEncoding enc = aut.getEventEncoding();
    final int numEvents = enc.getNumberOfProperEvents();
    final byte[] oldStatus = new byte[numEvents];
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      byte status = enc.getProperEventStatus(e);
      if (EventStatus.isUsedEvent(status)) {
        final EventProxy event = enc.getProperEvent(e);
        final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
        final byte newStatus = info.getEventStatus(aut);
        if (newStatus != status) {
          status = newStatus;
          enc.setProperEventStatus(e, status);
        }
      }
      oldStatus[e] = status;
    }
    // TODO simplify ...
    final boolean simplified = false;
    // Update event status ...
    if (simplified || !mAlwaysEnabledDetectedInitially) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      mSpecialEventsFinder.setTransitionRelation(rel);
      mSpecialEventsFinder.run();
      final byte[] newStatus = mSpecialEventsFinder.getComputedEventStatus();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        if (EventStatus.isUsedEvent(oldStatus[e])) {
          final EventProxy event = enc.getProperEvent(e);
          final TREventInfo info = mCurrentSubsystem.getEventInfo(event);
          info.updateAutomatonStatus(aut, newStatus[e], mNeedsSimplification);
          mNeedsDisjointSubsystemsCheck |=
            !EventStatus.isLocalEvent(oldStatus[e]) &&
            !EventStatus.isUsedEvent(newStatus[e]);
        }
      }
    }
    return simplified;
  }

  private boolean disjointSubsystemsCheck()
  {
    if (mNeedsDisjointSubsystemsCheck) {
      mNeedsDisjointSubsystemsCheck = false;
      final List<SubsystemInfo> splits =
        mCurrentSubsystem.findEventDisjointSubsystems();
      if (splits == null) {
        return false;
      } else {
        mCurrentSubsystem = null;
        mSubsystemQueue.addAll(splits);
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean analyseCurrentSubsystemMonolithically()
  {
    // TODO Auto-generated method stub
    return false;
  }


  //#########################################################################
  //# Data Members
  // Configuration
  private EventProxy mConfiguredDefaultMarking;
  private EventProxy mConfiguredPreconditionMarking;
  private boolean mBlockedEventsSupported;
  private boolean mFailingEventsSupported;
  private boolean mSelfloopOnlyEventsSupported;
  private boolean mAlwaysEnabledEventsSupported;

  // Tools
  private SpecialEventsFinder mSpecialEventsFinder;

  // Data Structures
  private EventProxy mUsedDefaultMarking;
  private Queue<SubsystemInfo> mSubsystemQueue;
  private SubsystemInfo mCurrentSubsystem;
  private Queue<TRAutomatonProxy> mNeedsSimplification;
  private boolean mNeedsDisjointSubsystemsCheck;
  private boolean mAlwaysEnabledDetectedInitially;


  //#########################################################################
  //# Class Constants
  static final int DEFAULT_MARKING = 0;
  static final int PRECONDITION_MARKING = 1;

  private static final int INITIAL_CONFIG =
    ListBufferTransitionRelation.CONFIG_SUCCESSORS;

}
