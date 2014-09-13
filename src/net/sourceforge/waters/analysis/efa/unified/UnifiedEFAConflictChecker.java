//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFAConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.efa.base.EFANonblockingChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.module.AbstractModuleConflictChecker;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.apache.log4j.Logger;


/**
 * A compositional conflict checker for EFA.
 * This implementation is based on the unified EFA model.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAConflictChecker extends AbstractModuleConflictChecker
{

  //#########################################################################
  //# Constructors
  public UnifiedEFAConflictChecker(final ModuleProxyFactory factory)
  {
    super(factory);
  }

  public UnifiedEFAConflictChecker(final ModuleProxy model,
                                   final ModuleProxyFactory factory)
  {
    super(model, factory);
  }

  public UnifiedEFAConflictChecker(final ModuleProxy model,
                                   final IdentifierProxy marking,
                                   final ModuleProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Configuration
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }

  public CompilerOperatorTable getOperatorTable()
  {
    return mCompilerOperatorTable;
  }

  public void setCompilerOperatorTable(final CompilerOperatorTable op)
  {
    mCompilerOperatorTable = op;
  }

  public SelectionHeuristic<UnifiedEFACandidate> getCompositionSelectionHeuristic()
  {
    return mCompositionSelectionHeuristic;
  }

  public void setCompositionSelectionHeuristic
    (final SelectionHeuristic<UnifiedEFACandidate> heuristic)
  {
    mCompositionSelectionHeuristic = heuristic;
  }

  public SelectionHeuristic<UnifiedEFAVariable> getVariableSelectionHeuristic()
  {
    return mVariableSelectionHeuristic;
  }

  public void setVariableSelectionHeuristic
    (final SelectionHeuristic<UnifiedEFAVariable> heuristic)
  {
    mVariableSelectionHeuristic = heuristic;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  public boolean getUsesLocalVariable()
  {
    return mUsesLocalVariable;
  }

  public void setUsesLocalVariable(final boolean prefer)
  {
    mUsesLocalVariable = prefer;
  }

  public void setSimplifierFactory(final UnifiedEFASimplifierFactory factory)
  {
    mSimplifierFactory = factory;
  }

  public int getInternalStateLimit()
  {
    return mInternalStateLimit;
  }

  public void setInternalStateLimit(final int limit)
  {
    mInternalStateLimit = limit;
  }

  public int getInternalTransitionLimit()
  {
    return mInternalTransitionLimit;
  }

  public void setInternalTransitionLimit(final int limit)
  {
    mInternalTransitionLimit = limit;
  }


  //#########################################################################
  //# Access
  AbstractEFAEvent getDummyRoot()
  {
    return mDummyRoot;
  }

  Set<UnifiedEFATransitionRelation> getTransitionRelations()
  {
    return mCurrentSubSystem.getTransitionRelations();
  }

  List<UnifiedEFAVariable> getVariables()
  {
    return mCurrentSubSystem.getVariables();
  }

  VariableInfo getVariableInfo(final UnifiedEFAVariable var)
  {
    return mCurrentSubSystem.getVariableInfo(var);
  }

  EventInfo getEventInfo(final AbstractEFAEvent event)
  {
    return mCurrentSubSystem.getEventInfo(event);
  }

  int getNumberOfAutomata()
  {
    return
      mMainEFASystem.getVariables().size() +
      mMainEFASystem.getTransitionRelations().size();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mUnfolder != null) {
      mUnfolder.requestAbort();
    }
    if (mSimplifier != null) {
      mSimplifier.requestAbort();
    }
    if (mUpdateMerger != null) {
      mUpdateMerger.requestAbort();
    }
    if (mSynchronizer != null) {
      mSynchronizer.requestAbort();
    }
    if (mNonblockingChecker != null) {
      mNonblockingChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mUnfolder != null) {
      mUnfolder.resetAbort();
    }
    if (mSimplifier != null) {
      mSimplifier.resetAbort();
    }
    if (mUpdateMerger != null) {
      mUpdateMerger.resetAbort();
    }
    if (mSynchronizer != null) {
      mSynchronizer.resetAbort();
    }
    if (mNonblockingChecker != null) {
      mNonblockingChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Invocation
  @Override
  public void setUp()
    throws EvalException, AnalysisException
  {
    super.setUp();
    if (mCompilerOperatorTable == null) {
      mCompilerOperatorTable = CompilerOperatorTable.getInstance();
    }
    if (mDocumentManager == null) {
      mDocumentManager = new DocumentManager();
    }
    final ModuleProxy module = getModel();
    final List<ParameterBindingProxy> binding = getBindings();
    final UnifiedEFACompiler compiler =
      new UnifiedEFACompiler(mDocumentManager, module);
    compiler.setConfiguredDefaultMarking(getConfiguredDefaultMarking());
    final List<String> none = Collections.emptyList();
    compiler.setEnabledPropertyNames(none);
    mMainEFASystem = compiler.compile(binding);
    final UnifiedEFAVariableContext context =
      mMainEFASystem.getVariableContext();
    mSubSystemQueue = new PriorityQueue<SubSystemInfo>();
    mVariableCollector = new UnifiedEFAVariableCollector
      (mCompilerOperatorTable, context);
    if (mVariableSelectionHeuristic == null) {
      // The following is the default. Can be overridden from outside
      // using setVariableSelectionHeuristic().
      final SelectionHeuristic<UnifiedEFAVariable> maxE =
        new VariableSelectionHeuristicMaxE();
      final SelectionHeuristic<UnifiedEFAVariable> maxS =
        new VariableSelectionHeuristicMaxS();
      final SelectionHeuristic<UnifiedEFAVariable> minD =
        new VariableSelectionHeuristicMinD();
      mVariableSelectionHeuristic =
        new ChainSelectionHeuristic<UnifiedEFAVariable>(maxE, maxS, minD);
    }
    mVariableSelectionHeuristic.setContext(this);
    if (mCompositionSelectionHeuristic == null) {
      // The following is the default. Can be overridden from outside
      // using setCompositionSelectionHeuristic().
      final SelectionHeuristic<UnifiedEFACandidate> minS =
        new CompositionSelectionHeuristicMinS();
      final SelectionHeuristic<UnifiedEFACandidate> minF =
        new CompositionSelectionHeuristicMinF();
      mCompositionSelectionHeuristic =
        new ChainSelectionHeuristic<UnifiedEFACandidate>(minF, minS);
    }
    mCompositionSelectionHeuristic.setContext(this);
    mUnfolder = new UnifiedEFAVariableUnfolder
      (getFactory(), mCompilerOperatorTable, context);
    mDummyRoot = new SilentEFAEvent(":root");
    mUpdateMerger = new UnifiedEFAUpdateMerger
      (getFactory(), mCompilerOperatorTable, context,mDummyRoot);
    mSynchronizer = new UnifiedEFASynchronousProductBuilder();
    mSynchronizer.setStateLimit(mInternalStateLimit);
    mSynchronizer.setTransitionLimit(mInternalTransitionLimit);
    if (mSimplifierFactory == null) {
      mSimplifierFactory = UnifiedEFASimplifierFactory.NB;
    }
    mSimplifier = UnifiedEFASimplifier.createStandardNonblockingProcedure
      (Equivalence.OBSERVATION_EQUIVALENCE, mInternalTransitionLimit);
    final UnifiedEFAConflictCheckerAnalysisResult result = getAnalysisResult();

    result.addSynchronousProductStatistics(mSynchronizer.getStatistics());
    mSimplifier = mSimplifierFactory.createAbstractionProcedure(this);
    result.setSimplifierStatistics(mSimplifier);
    mNonblockingChecker = new EFANonblockingChecker();
  }

  @Override
  protected UnifiedEFAConflictCheckerAnalysisResult createAnalysisResult()
  {
    return new UnifiedEFAConflictCheckerAnalysisResult();
  }

  @Override
  public UnifiedEFAConflictCheckerAnalysisResult getAnalysisResult()
  {
    return (UnifiedEFAConflictCheckerAnalysisResult) super.getAnalysisResult();
  }

  @Override
  public boolean run()
    throws EvalException, AnalysisException
  {
    try {
      //TODO
      setUp();
      Collection<UnifiedEFATransitionRelation> trs =
        mMainEFASystem.getTransitionRelations();
      mCurrentSubSystem =
        new SubSystemInfo(mMainEFASystem.getEvents().size(),
                          mMainEFASystem.getVariables().size(),
                          trs);
      mDirtyTRs = new THashSet<>(trs);
      createVariableInfo();
      createEventInfo();
      simplifyDirtyTransitionRelations();
      splitSubsystems();
//      int i = 0;
      while (mCurrentSubSystem != null) {
        while (mCurrentSubSystem.isReducible()) {
          final UnifiedEFACandidate minCandidate =
            mCurrentSubSystem.selectCandidate();
          applyCandidate(minCandidate);
          simplifyDirtyTransitionRelations();
          splitSubsystems();
//          saveCurrentSystem("debug/sub" + i);
//          getLogger().debug("wrote debug/sub" + i);
//          i++;
        }
        trs = mCurrentSubSystem.getTransitionRelations();
        if (trs.isEmpty()) {
          mCurrentSubSystem = mSubSystemQueue.poll();
          continue;
        }
        final UnifiedEFATransitionRelation finalTR =
          trs.iterator().next();
        final boolean nonblocking = mNonblockingChecker.run(finalTR);
        getLogger().debug("Result for final TR " + finalTR.getName() +
                          ": " + nonblocking);
        if (!nonblocking) {
          return setBooleanResult(false);
        } else {
          mCurrentSubSystem = mSubSystemQueue.poll();
        }
      }
      return setSatisfiedResult();
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
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
  public void tearDown()
  {
    super.tearDown();
    mCompilerOperatorTable = null;
    mDocumentManager = null;
    mMainEFASystem = null;
    mVariableCollector = null;
    mUnfolder = null;
    mSimplifier = null;
    mSynchronizer = null;
    mNonblockingChecker = null;
    mSubSystemQueue = null;
    mDummyRoot = null;
    if (mCompositionSelectionHeuristic != null) {
      mCompositionSelectionHeuristic.setContext(null);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createVariableInfo()
  {
    final List<UnifiedEFAVariable> vars = mMainEFASystem.getVariables();
    for (final UnifiedEFAVariable var : vars) {
      final VariableInfo info = new VariableInfo(var);
      mCurrentSubSystem.addVariableInfo(info);
    }
  }

  private void createEventInfo()
  {
    final List<AbstractEFAEvent> events = mMainEFASystem.getEvents();
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = new EventInfo(event);
      mCurrentSubSystem.addEventInfo(info);
    }
    final List<UnifiedEFATransitionRelation> trs =
      mMainEFASystem.getTransitionRelations();
    for (final UnifiedEFATransitionRelation tr : trs) {
      for (final AbstractEFAEvent event : tr.getUsedEventsExceptTau()) {
        final EventInfo info =  mCurrentSubSystem.getEventInfo(event);
        info.addTransitionRelation(tr);
      }
    }
  }

  private void splitSubsystems() throws AnalysisAbortException
  {
    final Set<UnifiedEFATransitionRelation> trs =
      mCurrentSubSystem.getTransitionRelations();
    final Iterator<UnifiedEFATransitionRelation> iter = trs.iterator();
    while (iter.hasNext()) {
      final UnifiedEFATransitionRelation tr = iter.next();
      if (tr.getUsedEventsExceptTau().isEmpty()) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        if (rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA)) {
          final Set<UnifiedEFATransitionRelation> singletonTR =
            Collections.singleton(tr);
          mCurrentSubSystem = new SubSystemInfo(0, 0, singletonTR);
          return;
        } else {
          iter.remove();
        }
      }
    }
    if (mCurrentSubSystem.getNumberOfEvents() == 0) {
      return;
    }
    final Set<EventInfo> roots = mCurrentSubSystem.getRootEventInfo();
    SubSystemInfo sub = collectSubSystem(roots);
    if (roots.isEmpty()) {
      return;
    }
    mSubSystemQueue.add(sub);
    while (!roots.isEmpty()) {
      sub = collectSubSystem(roots);
      mSubSystemQueue.add(sub);
    }
    mCurrentSubSystem = mSubSystemQueue.poll();
  }

  private SubSystemInfo collectSubSystem(final Collection<EventInfo> remaining)
    throws AnalysisAbortException
  {
    final Iterator<EventInfo> iter = remaining.iterator();
    final EventInfo startInfo = iter.next();
    iter.remove();
    final Queue<EventInfo> queue = new ArrayDeque<>();
    queue.add(startInfo);
    final SubSystemInfo sub = new SubSystemInfo();
    sub.addEventInfoWithChildren(startInfo);
    while (!queue.isEmpty()) {
      checkAbort();
      final EventInfo info = queue.remove();
      final Set<UnifiedEFATransitionRelation> trs =
        new THashSet<UnifiedEFATransitionRelation>();
      final Set<VariableInfo> vars = new THashSet<VariableInfo>();
      info.collectCandidate(trs, vars);
      for (final UnifiedEFATransitionRelation tr : trs) {
        if (sub.addTransitionRelation(tr)) {
          final Set<AbstractEFAEvent> events = tr.getUsedEventsExceptTau();
          for (final AbstractEFAEvent nextEvent : events) {
            final AbstractEFAEvent rootEvent = getRootEvent(nextEvent);
            final EventInfo rootInfo =
              mCurrentSubSystem.getEventInfo(rootEvent);
            if (sub.addEventInfoWithChildren(rootInfo)) {
              queue.add(rootInfo);
              remaining.remove(rootInfo);
            }
          }
        }
      }
      for (final VariableInfo var : vars) {
        if (sub.addVariableInfo(var)) {
          final Collection<EventInfo> events = var.getEvents();
          for (final EventInfo eventInfo : events) {
            final AbstractEFAEvent nextEvent = eventInfo.getEvent();
            final AbstractEFAEvent rootEvent = getRootEvent(nextEvent);
            final EventInfo rootInfo =
              mCurrentSubSystem.getEventInfo(rootEvent);
            if (sub.addEventInfoWithChildren(rootInfo)) {
              queue.add(rootInfo);
              remaining.remove(rootInfo);
            }
          }
        }
      }
    }
    return sub;
  }

  private void applyCandidate(final UnifiedEFACandidate candidate)
    throws AnalysisException, EvalException
  {
    checkAbort();
    final List<UnifiedEFAVariable> vars = candidate.getVariables();
    List<UnifiedEFATransitionRelation> trs =
      candidate.getTransitionRelations();
    final UnifiedEFAConflictCheckerAnalysisResult result =
      getAnalysisResult();
    mLocalVariableTR = null;
    if (!vars.isEmpty()) {
      final UnifiedEFAVariable selectedVar =
        mVariableSelectionHeuristic.select(vars);
      final VariableInfo selectedVarInfo = getVariableInfo(selectedVar);
      mUnfolder.setUnfoldedVariable(selectedVar);
      final List<AbstractEFAEvent> originalEvents =
        selectedVarInfo.getLeaveEvents();
      mUnfolder.setOriginalEvents(originalEvents);
      mUnfolder.run();
      final UnifiedEFATransitionRelation unfoldedTR =
        mUnfolder.getTransitionRelation();
      result.addUnifiedEFATRTransitionRelation(unfoldedTR);
      registerTR(unfoldedTR, true);
      unregisterVariable(selectedVarInfo);
      UnifiedEFATransitionRelation simplifiedTR = simplifyTR(unfoldedTR);
      if (simplifiedTR == null) {
        recordBlockedEvents(unfoldedTR);
        simplifiedTR = unfoldedTR;
      } else {
        mergeUpdates(simplifiedTR);
      }
      if (candidate.isVariableLocal()) {
        mLocalVariableTR = simplifiedTR;
      }
    } else if (trs.size() > 1) {
      final Logger logger = getLogger();
      logger.debug("Synchronizing: " + candidate);
      trs = addSelfloopTR(trs);
      mSynchronizer.setInputTransitionRelations(trs);
      mSynchronizer.run();
      final UnifiedEFATransitionRelation syncTR =
        mSynchronizer.getSynchronousProduct();
      registerTR(syncTR, false);
      result.addUnifiedEFATRTransitionRelation(syncTR);
      for (final UnifiedEFATransitionRelation tr : trs) {
        unregisterTR(tr);
      }
      final UnifiedEFATransitionRelation simplifiedTR = simplifyTR(syncTR);
      if (simplifiedTR == null) {
        recordBlockedEvents(syncTR);
        mergeUpdates(syncTR);
      } else {
        mergeUpdates(simplifiedTR);
      }
    } else {
      final UnifiedEFATransitionRelation tr = trs.get(0);
      result.addUnifiedEFATRTransitionRelation(tr);
      final UnifiedEFATransitionRelation simplifiedTR = simplifyTR(tr);
      if (simplifiedTR != null) {
        mergeUpdates(simplifiedTR);
      }
    }
  }

  private void simplifyDirtyTransitionRelations() throws AnalysisException
  {
    while (!mDirtyTRs.isEmpty()) {
      final UnifiedEFATransitionRelation tr = Collections.min(mDirtyTRs);
      mDirtyTRs.remove(tr);
      simplifyTR(tr);
    }
  }

  private UnifiedEFATransitionRelation simplifyTR
    (final UnifiedEFATransitionRelation tr)
    throws AnalysisException
  {
    final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    // 1. Mark blocked events for removal and local events for hiding.
    for (final AbstractEFAEvent event : tr.getUsedEventsExceptTau()) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      final int code = encoding.getEventId(event);
      byte status = rel.getProperEventStatus(code);
      if (info.isBlocked()) {
        status |= EventEncoding.STATUS_BLOCKED;
      } else if (info.isLocal(tr)) {
        final Logger logger = getLogger();
        logger.debug("Hiding event: " + event.getName());
        status |= EventEncoding.STATUS_LOCAL;
      }
      rel.setProperEventStatus(code, status);
    }
    // 2. Run abstraction chain.
    final UnifiedEFATransitionRelation simplifiedTR = mSimplifier.run(tr);
    // 3. Update index structures.
    if (simplifiedTR != null) {
      registerTR(simplifiedTR, false);
      unregisterTR(tr);
      recordBlockedEvents(simplifiedTR);
    }
    return simplifiedTR;
  }

  private void mergeUpdates(final UnifiedEFATransitionRelation tr)
    throws AnalysisException, EvalException
  {
    mUpdateMerger.setTransitionRelation(tr);
    final Map<AbstractEFAEvent,List<AbstractEFAEvent>> candidates =
      findMergeCandidates(tr);
    for (final Entry<AbstractEFAEvent,List<AbstractEFAEvent>> entry :
         candidates.entrySet()) {
      final AbstractEFAEvent event = entry.getKey();
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      final List<AbstractEFAEvent> events = entry.getValue();
      mUpdateMerger.setCandidateEvents(events);
      mUpdateMerger.run();
      for (final AbstractEFAEvent removedEvent :
           mUpdateMerger.getRemovedEvents()) {
        final EventInfo removedInfo =
          mCurrentSubSystem.getEventInfo(removedEvent);
        mCurrentSubSystem.removeEventInfo(removedEvent);
        if (info != null) {
          info.removeChildEvent(removedInfo);
        }
        for (final VariableInfo varInfo : removedInfo.getVariables()) {
          varInfo.removeEvent(removedInfo);
        }
      }
      for (final AbstractEFAEvent addedEvent : mUpdateMerger.getAddedEvents()) {
        final EventInfo newInfo = new EventInfo(addedEvent);
        newInfo.addTransitionRelation(tr);
        mCurrentSubSystem.addEventInfo(newInfo);
        if (info != null) {
          info.addChildEvent(newInfo);
        }
      }
    }
  }

  private Map<AbstractEFAEvent,List<AbstractEFAEvent>> findMergeCandidates
    (final UnifiedEFATransitionRelation tr)
  {
    final Collection<AbstractEFAEvent> events = tr.getUsedEventsExceptTau();
    final Map<AbstractEFAEvent,List<AbstractEFAEvent>> mergeCandidates =
      new HashMap<>();
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      if (info.getChildren().isEmpty() &&
          info.getTransitionRelations().size() == 1 &&
          !info.isBlocked()) {
        final EventInfo parentInfo = info.getParent();
        final AbstractEFAEvent parent =
          parentInfo != null ? parentInfo.getEvent() : mDummyRoot;
        List<AbstractEFAEvent> children = mergeCandidates.get(parent);
        if (children == null) {
          children = new ArrayList<>();
          mergeCandidates.put(parent, children);
        }
        children.add(event);
      }
    }
    return mergeCandidates;
  }


  private void registerTR(final UnifiedEFATransitionRelation tr,
                          final boolean unfolding)
    throws AnalysisAbortException
  {
    // getLogger().debug(tr.getName() + " is nonblocking: " +
    //                   mNonblockingChecker.run(tr));
    for (final AbstractEFAEvent event : tr.getAllEventsExceptTau()) {
      EventInfo info = mCurrentSubSystem.getEventInfo(event);
      if (unfolding || tr.isUsedEvent(event)) {
        if (info == null) {
          info = new EventInfo(event);
          mCurrentSubSystem.addEventInfo(info);
          if (unfolding) {
            final AbstractEFAEvent originalEvent = event.getOriginalEvent();
            if (originalEvent != null) {
              final EventInfo originalInfo =
                mCurrentSubSystem.getEventInfo(originalEvent);
              originalInfo.addChildEvent(info);
            }
          }
        }
        if (tr.isUsedEvent(event)) {
          info.addTransitionRelation(tr);
        }
      } else if (info != null) {
        removeEmptyEventInfo(info);
      }
    }
    mCurrentSubSystem.addTransitionRelation(tr);
  }

  /**
   * Checks the given transition relation for blocked events.
   * This method checks whether the given transition relation uses any
   * events that does not appear on any transition. Such events are
   * marked as blocked and their remaining transition relations are
   * added to the set {@link #mDirtyTRs} for blocked events removal.
   */
  private void recordBlockedEvents(final UnifiedEFATransitionRelation tr)
  {
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    final int numberOfProperEvents = rel.getNumberOfProperEvents();
    final boolean[] used = new boolean[numberOfProperEvents];
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    while (iter.advance()) {
      final int event = iter.getCurrentEvent();
      used[event] = true;
    }
    final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < numberOfProperEvents; e++) {
      final byte status = rel.getProperEventStatus(e);
      if (EventEncoding.isUsedEvent(status) && !used[e]) {
        final AbstractEFAEvent event = encoding.getEvent(e);
        final EventInfo info = mCurrentSubSystem.getEventInfo(event);
        rel.setProperEventStatus(e, status | EventEncoding.STATUS_UNUSED);
        info.removeTransitionRelation(tr);
        info.setBlocked();
        removeEmptyEventInfo(info);
      }
    }
  }

  /**
   * Marks a variable as removed. This method is called after a variable
   * has been unfolded, and all its events have been renamed and the event
   * information updated. Therefore, the variable is removed, and the
   * associated event informations are updated by removing <I>all</I>
   * variables, and by removing the event from the associated variable
   * information records.
   */
  private void unregisterVariable(final VariableInfo varInfo)
  {
    mCurrentSubSystem.removeVariableInfo(varInfo);
    for (final EventInfo eventInfo : varInfo.getEvents()) {
      for (final VariableInfo otherVarInfo : eventInfo.getVariables()) {
        if (otherVarInfo != varInfo) {
          otherVarInfo.removeEvent(eventInfo);
        }
      }
      eventInfo.clearVariables();
      removeEmptyEventInfo(eventInfo);
    }
  }

  private void unregisterTR(final UnifiedEFATransitionRelation tr)
  {
    for (final AbstractEFAEvent event : tr.getUsedEventsExceptTau()) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      info.removeTransitionRelation(tr);
      removeEmptyEventInfo(info);
    }
    mCurrentSubSystem.removeTransitionRelation(tr);
    mDirtyTRs.remove(tr);
  }

  private boolean removeEmptyEventInfo(final EventInfo info)
  {
    if (info.isRemovable()) {
      final AbstractEFAEvent event = info.getEvent();
      if (isRootEvent(event)) {
        final List<EventInfo> childrenCopy =
          new ArrayList<>(info.getChildren());
        for (final EventInfo childInfo : childrenCopy) {
          removeEmptyEventInfo(childInfo);
        }
        mCurrentSubSystem.removeEventInfo(info);
      } else {
        mCurrentSubSystem.removeEventInfo(info);
        AbstractEFAEvent originalEvent = event.getOriginalEvent();
        while (originalEvent != null) {
          final EventInfo originalInfo =
            mCurrentSubSystem.getEventInfo(originalEvent);
          if (originalInfo != null) {
            originalInfo.removeChildEvent(info);
            break;
          }
          originalEvent = originalEvent.getOriginalEvent();
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private boolean isRootEvent(final AbstractEFAEvent event)
  {
    AbstractEFAEvent original = event.getOriginalEvent();
    while (original != null) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(original);
      if (info != null) {
        return false;
      } else {
        original = original.getOriginalEvent();
      }
    }
    return true;
  }

  private AbstractEFAEvent getRootEvent(final AbstractEFAEvent event)
  {
    AbstractEFAEvent root = event;
    AbstractEFAEvent original = event.getOriginalEvent();
    while (original != null) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(original);
      if (info != null) {
        root = original;
      }
      original = original.getOriginalEvent();
    }
    return root;
  }

  private List<UnifiedEFATransitionRelation> addSelfloopTR
    (final List<UnifiedEFATransitionRelation> trs) throws OverflowException
  {
    final Collection<AbstractEFAEvent> missingEvents =
      findMissingChildren(trs);
    if (missingEvents.isEmpty()) {
      return trs;
    } else {
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(":missingSelfloops",
                                         ComponentKind.PLANT,
                                         missingEvents.size() + 1, 0, 1,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final UnifiedEFAEventEncoding encoding =
        new UnifiedEFAEventEncoding(":missingSelfloops", missingEvents.size()+1);
      for (final AbstractEFAEvent missingEvent : missingEvents) {
        final int code = encoding.createEventId(missingEvent);
        rel.addTransition(0, code, 0);
      }
      rel.setProperEventStatus(EventEncoding.TAU,
                               EventEncoding.STATUS_FULLY_LOCAL |
                               EventEncoding.STATUS_UNUSED);
      rel.setInitial(0, true);
      final UnifiedEFATransitionRelation selfloop = new
        UnifiedEFATransitionRelation(rel, encoding);
      final List<UnifiedEFATransitionRelation> selfloopsTR =
        new ArrayList<>(trs.size() + 1);
      selfloopsTR.addAll(trs);
      selfloopsTR.add(selfloop);
      return selfloopsTR;
    }
  }

  private Collection<AbstractEFAEvent> findMissingChildren
    (final Collection<UnifiedEFATransitionRelation> trs)
  {
    final Set<AbstractEFAEvent> events = new THashSet<>();
    for (final UnifiedEFATransitionRelation tr : trs) {
      events.addAll(tr.getUsedEventsExceptTau());
    }
    final Map<AbstractEFAEvent, SyncInfo> syncMap = new HashMap<>();
    for (final AbstractEFAEvent event : events) {
      final SyncInfo syncInfo = new SyncInfo(true, true);
      syncMap.put(event, syncInfo);
    }
    for (final AbstractEFAEvent event : events) {
      final SyncInfo eventInfo = syncMap.get(event);
      AbstractEFAEvent original = event.getOriginalEvent();
      while (original != null) {
        if (mCurrentSubSystem.getEventInfo(original) != null) {
          final SyncInfo originalInfo = syncMap.get(original);
          if (originalInfo != null) {
            if (events.contains(original)) {
              originalInfo.setIsLeave(false);
              eventInfo.setIsRoot(false);
            }
          } else {
            final SyncInfo syncInfo = new SyncInfo(false, false);
            syncMap.put(original, syncInfo);
          }
        }
        original = original.getOriginalEvent();
      }
    }
    final List<AbstractEFAEvent> children = new ArrayList<>();
    for (final AbstractEFAEvent event : events) {
      final SyncInfo syncInfo = syncMap.get(event);
      if (syncInfo.isRoot()) {
        final EventInfo eventInfo = mCurrentSubSystem.getEventInfo(event);
        eventInfo.findAdditionalChildren(syncMap, children);
      }
    }
    Collections.sort(children);
    return children;
  }

  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private void saveCurrentSystem(final String name)
    throws OverflowException
  {
    final UnifiedEFAVariableContext context =
      mMainEFASystem.getVariableContext();
    final List<UnifiedEFAVariable> vars = mCurrentSubSystem.getVariables();
    final List<UnifiedEFATransitionRelation> trs =
      new ArrayList<>(mCurrentSubSystem.getTransitionRelations());
    Collections.sort(trs);
    final UnifiedEFATransitionRelation blocked = createBlockedEventsTR();
    if (blocked != null) {
      trs.add(blocked);
    }
    final List<AbstractEFAEvent> events = mCurrentSubSystem.getEvents();
    final UnifiedEFASystem system =
      new UnifiedEFASystem(name, vars, trs, events, context);
    final UnifiedEFASystemImporter importer =
      new UnifiedEFASystemImporter(getFactory(), mCompilerOperatorTable);
    final ModuleProxy module = importer.importModule(system);
    MarshallingTools.saveModule(module, name + ".wmod");
  }

  private UnifiedEFATransitionRelation createBlockedEventsTR()
    throws OverflowException
  {
    final List<AbstractEFAEvent> events = mCurrentSubSystem.getEvents();
    final UnifiedEFAEventEncoding encoding =
      new UnifiedEFAEventEncoding(":blocked");
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      if (info.isBlocked() && info.getChildren().isEmpty()) {
        encoding.createEventId(event);
      }
    }
    if (encoding.size() > 1) {
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(":blocked", ComponentKind.PLANT,
                                         encoding.size(), 0, 1,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      rel.setProperEventStatus(EventEncoding.TAU,
                               EventEncoding.STATUS_FULLY_LOCAL |
                               EventEncoding.STATUS_UNUSED);
      rel.setInitial(0, true);
      return new UnifiedEFATransitionRelation(rel, encoding);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unused")
  private void checkConsistency()
  {
    for (final AbstractEFAEvent event : mCurrentSubSystem.getEvents()) {
      final EventInfo info = mCurrentSubSystem.getEventInfo(event);
      for (final UnifiedEFATransitionRelation tr : info.getTransitionRelations()) {
        assert tr.isUsedEvent(event);
      }
    }
  }


  //#########################################################################
  //# Inner Class EventInfo
  class EventInfo
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final AbstractEFAEvent event)
    {
      mEvent = event;
      mTransitionRelations = new ArrayList<>();
      final Set<UnifiedEFAVariable> primed = new THashSet<>();
      final Set<UnifiedEFAVariable> unprimed = new THashSet<>();
      mVariableCollector.collectAllVariables(mEvent.getUpdate(),
                                             unprimed, primed);
      mPrimedVariables = collectVariableInfo(primed);
      mUnPrimedVariables = collectVariableInfo(unprimed);
      final Set<UnifiedEFAVariable> vars = primed;
      vars.addAll(unprimed);
      mVariables = collectVariableInfo(vars);
      for (final VariableInfo info : mVariables) {
        info.addEvent(this);
      }
      mChildrenEvents = new ArrayList<>();
    }

    private List<VariableInfo> collectVariableInfo
      (final Set<UnifiedEFAVariable> vars)
    {
      switch (vars.size()) {
      case 0:
        return Collections.emptyList();
      case 1:
        final UnifiedEFAVariable var0 = vars.iterator().next();
        final VariableInfo info0 = mCurrentSubSystem.getVariableInfo(var0);
        return Collections.singletonList(info0);
      default:
        final List<VariableInfo> result = new ArrayList<>(vars.size());
        for (final UnifiedEFAVariable var : vars) {
          final VariableInfo info = mCurrentSubSystem.getVariableInfo(var);
          result.add(info);
        }
        return result;
      }
    }

    //#######################################################################
    //# Simple Access
    AbstractEFAEvent getEvent()
    {
      return mEvent;
    }

    List<UnifiedEFATransitionRelation> getTransitionRelations()
    {
      return mTransitionRelations;
    }

    private void addTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.add(tr);
    }

    private void removeTransitionRelation
      (final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.remove(tr);
    }

    Collection<VariableInfo> getVariables()
    {
      return mVariables;
    }

    public void clearVariables()
    {
      mVariables = mPrimedVariables = mUnPrimedVariables =
        Collections.emptyList();
    }

    private Collection<VariableInfo> getPrimedVariables()
    {
      return mPrimedVariables;
    }

    private Collection<VariableInfo> getUnPrimedVariables()
    {
      return mUnPrimedVariables;
    }

    /**
     * Collects data for a candidate with this event. All transition
     * relations and variables associated with this event and all
     * its descendants are added to the sets <CODE>trs</CODE>
     * and&nbsp;<CODE>vars</CODE>, respectively.
     */
    private void collectCandidate(final Set<UnifiedEFATransitionRelation> trs,
                                  final Set<VariableInfo> vars)
    {
      trs.addAll(getTransitionRelations());
      vars.addAll(getVariables());
      for (final EventInfo info : mChildrenEvents) {
        info.collectCandidate(trs, vars);
      }
    }

    private List<EventInfo> getChildren()
    {
      return mChildrenEvents;
    }

    private void addChildEvent(final EventInfo event)
    {
      mChildrenEvents.add(event);
    }

    private void removeChildEvent(final EventInfo event)
    {
      mChildrenEvents.remove(event);
      mChildrenEvents.addAll(event.getChildren());
    }

    EventInfo getParent()
    {
      AbstractEFAEvent original = mEvent.getOriginalEvent();
      while (original != null) {
        final EventInfo info = mCurrentSubSystem.getEventInfo(original);
        if (info != null) {
          return info;
        }
        original = original.getOriginalEvent();
      }
      return null;
    }

    //#######################################################################
    //# Checking for Local and Blocked Events etc.
    /**
     * Checks whether this event can be removed.
     * An event can be removed if it has no listed transition relations
     * or variables, and one of the following conditions holds.
     * Either the event has children (in which case the children can
     * be linked to its parent), or the event is a leave and none
     * of its ancestors has any listed transition relations or variables.
     */
    private boolean isRemovable()
    {
      if (mTransitionRelations.isEmpty() && mVariables.isEmpty()) {
        if (!mChildrenEvents.isEmpty()) {
          return true;
        }
        AbstractEFAEvent original = mEvent.getOriginalEvent();
        while (original != null) {
          final EventInfo originalInfo =
            mCurrentSubSystem.getEventInfo(original);
          if (originalInfo != null &&
              !(originalInfo.getTransitionRelations().isEmpty() &&
                originalInfo.getVariables().isEmpty())) {
            return false;
          }
          original = original.getOriginalEvent();
        }
        return true;
      } else {
        return false;
      }
    }

    private boolean isLocal(final UnifiedEFATransitionRelation tr)
    {
      if (mTransitionRelations.size() == 1 && mVariables.size() == 0 &&
          isRootEvent(mEvent)) {
        assert mTransitionRelations.get(0) == tr;
        for (final EventInfo child : mChildrenEvents) {
          if (child.containsTRorVariable()) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    private boolean containsTRorVariable()
    {
      if (!(mTransitionRelations.isEmpty() && mVariables.isEmpty())) {
        return true;
      }
      for (final EventInfo child : mChildrenEvents) {
        if (child.containsTRorVariable()) {
          return true;
        }
      }
      return false;
    }

    private boolean isBlocked()
    {
      return mIsBlocked;
    }

    private void setBlocked()
    {
      if (!mIsBlocked) {
        final Logger logger = getLogger();
        logger.debug("Found blocked event: " + mEvent.getName());
        setBlockedDownwards();
        final EventInfo parent = getParent();
        if (parent != null) {
          parent.setBlockedUpwards();
        }
      }
    }

    private void setBlockedDownwards()
    {
      if (!mIsBlocked) {
        setBlockedHere();
        for (final EventInfo childInfo : mChildrenEvents) {
          childInfo.setBlockedDownwards();
        }
      }
    }

    private void setBlockedUpwards()
    {
      if (!mIsBlocked) {
        for (final EventInfo child : mChildrenEvents) {
          if (!child.isBlocked()) {
            return;
          }
        }
        setBlockedHere();
        final EventInfo parent = getParent();
        if (parent != null) {
          parent.setBlockedUpwards();
        }
      }
    }

    private void setBlockedHere()
    {
      mIsBlocked = true;
      mDirtyTRs.addAll(mTransitionRelations);
      for (final VariableInfo var : mVariables) {
        var.removeEvent(this);
      }
      clearVariables();
    }

    private void findAdditionalChildren
      (final Map<AbstractEFAEvent, SyncInfo> syncMap,
       final Collection<AbstractEFAEvent> children)
    {
      if (!isBlocked()) {
        final SyncInfo syncInfo = syncMap.get(mEvent);
        if (syncInfo == null) {
          children.add(mEvent);
        } else if (!syncInfo.isLeave()) {
          for (final EventInfo child : mChildrenEvents) {
            child.findAdditionalChildren(syncMap, children);
          }
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder buffer = new StringBuilder("<");
      buffer.append(mEvent.toString());
      buffer.append(" ");
      buffer.append(mTransitionRelations.isEmpty() ? "-" : "T");
      buffer.append(mVariables.isEmpty() ? "-" : "V");
      buffer.append(mChildrenEvents.isEmpty() ? "-" : "C");
      buffer.append(">");
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final AbstractEFAEvent mEvent;
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private Collection<VariableInfo> mVariables;
    private Collection<VariableInfo> mPrimedVariables;
    private Collection<VariableInfo> mUnPrimedVariables;
    private final List<EventInfo> mChildrenEvents;
    private boolean mIsBlocked = false;

  }


  //#########################################################################
  //# Inner Class SyncInfo
  private static class SyncInfo
  {
    private SyncInfo(final boolean isLeave, final boolean isRoot)
    {
      mIsLeave = isLeave;
      mIsRoot = isRoot;
    }

    //#######################################################################
    //# Simple Access
    private boolean isLeave()
    {
      return mIsLeave;
    }

    private boolean isRoot()
    {
      return mIsRoot;
    }

    private void setIsLeave(final boolean isLeave)
    {
      mIsLeave = isLeave;
    }

    private void setIsRoot(final boolean isRoot)
    {
      mIsRoot = isRoot;
    }


    //#######################################################################
    //# Data Members
    private boolean mIsLeave;
    private boolean mIsRoot;
  }


  //#########################################################################
  //# Inner Class VariableInfo
  class VariableInfo implements Comparable<VariableInfo>
  {
    //#######################################################################
    //# Constructor
    private VariableInfo(final UnifiedEFAVariable var)
    {
      mVariable = var;
      mEventInfo = new THashSet<>();
    }

    //#######################################################################
    //# Simple Access
    UnifiedEFAVariable getVariable()
    {
      return mVariable;
    }

    String getName()
    {
      return mVariable.getName();
    }

    int getRangeSize()
    {
      return mVariable.getRange().size();
    }

    Collection<EventInfo> getEvents()
    {
      return mEventInfo;
    }

    private void addEvent(final EventInfo info)
    {
      mEventInfo.add(info);
      mNumberOfSelfloops = -1;
    }

    private void removeEvent(final EventInfo info)
    {
      mEventInfo.remove(info);
      mNumberOfSelfloops = -1;
    }

    List<AbstractEFAEvent> getLeaveEvents()
    {
      final List<AbstractEFAEvent> events =
        new ArrayList<AbstractEFAEvent>(mEventInfo.size());
      for (final EventInfo info : mEventInfo) {
        events.add(info.getEvent());
      }
      return events;
    }

    private boolean isPrimedOnly()
    {
      for (final EventInfo info : mEventInfo){
        if (info.getUnPrimedVariables().contains(this)) {
          return false;
        }
      }
      return true;
    }

    private boolean isUnPrimedOnly()
    {
      for (final EventInfo info : mEventInfo){
        if (info.getPrimedVariables().contains(this)) {
          return false;
        }
      }
      return true;
    }

    int getNumberOfEvents()
    {
      return getEvents().size();
    }

    /**
     * Returns the number of selfloop events of a variable unfolding
     * candidate. The number of selfloop events is computed as the
     * number of events of the first variable of the candidate,
     * but only counting events that do not appear in any transition
     * relation and whose ancestors do not appear in any transition
     * relation.
     */
    int getNumberOfSelfloops()
    {
      if (mNumberOfSelfloops < 0) {
        final Collection<EventInfo> eventInfo = getEvents();
        int selfloops = 0;
        outer :
          for (final EventInfo info : eventInfo) {
            List<UnifiedEFATransitionRelation> trs =
              info.getTransitionRelations();
            if (trs.size() > 0) {
              continue;
            }
            AbstractEFAEvent original = info.getEvent().getOriginalEvent();
            while (original != null) {
              final EventInfo originalInfo =
                mCurrentSubSystem.mEventInfoMap.get(original);
              if (originalInfo != null) {
                trs =
                  mCurrentSubSystem.mEventInfoMap.get(original).getTransitionRelations();
                if (trs.size() > 0) {
                  continue outer;
                }
              }
              original = original.getOriginalEvent();
            }
            selfloops++;
          }
        mNumberOfSelfloops = selfloops;
      }
      return mNumberOfSelfloops;
    }

    private UnifiedEFATransitionRelation getLocalTR()
    {
      UnifiedEFATransitionRelation tr = null;
      for (EventInfo info : mEventInfo) {
        while (info != null) {
          final List<UnifiedEFATransitionRelation> trs =
            info.getTransitionRelations();
          switch (trs.size()) {
          case 0:
            break;
          case 1:
            if (tr == null) {
              tr = trs.get(0);
            } else if (tr != trs.get(0)) {
              return null;
            }
            break;
          default:
            return null;
          }
          info = info.getParent();
        }
      }
      return tr;
    }

    //#######################################################################
    //# Interface java.util.Comparable
    @Override
    public int compareTo(final VariableInfo info)
    {
      final int size1 = getRangeSize();
      final int size2 = info.getRangeSize();
      if (size1 != size2) {
        return size1 - size2;
      }
      return mVariable.compareTo(info.mVariable);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mVariable.getName() + "@" + mEventInfo.toString();
    }

    //#######################################################################
    //# Data Members
    private final UnifiedEFAVariable mVariable;
    private final Set<EventInfo> mEventInfo;
    private int mNumberOfSelfloops = -1;
  }


  //#########################################################################
  //# Inner Class SubSystemInfo
  class SubSystemInfo implements Comparable<SubSystemInfo>
  {
    //#######################################################################
    //# Constructors
    private SubSystemInfo()
    {
      mEventInfoMap = new HashMap<>();
      mVariableInfoMap = new HashMap<>();
      mTransitionRelations = new THashSet<>();
    }

    public SubSystemInfo(final int numberOfEvents,
                         final int numberOfVariables,
                         final Collection<UnifiedEFATransitionRelation> trs)
    {
      mEventInfoMap = new HashMap<>(numberOfEvents);
      mVariableInfoMap = new HashMap<>(numberOfVariables);
      mTransitionRelations = new THashSet<>(trs);
    }

    //#######################################################################
    //# Simple Access
    private int getNumberOfEvents()
    {
      return mEventInfoMap.size();
    }

    private EventInfo getEventInfo(final AbstractEFAEvent event)
    {
      return mEventInfoMap.get(event);
    }

    private boolean addEventInfo(final EventInfo info)
    {
      final AbstractEFAEvent event = info.getEvent();
      final EventInfo previous = mEventInfoMap.put(event, info);
      return previous == null;
    }

    private boolean addEventInfoWithChildren(final EventInfo info)
    {
      final boolean result = addEventInfo(info);
      if (result) {
        final List<EventInfo> children = info.getChildren();
        for (final EventInfo child : children) {
          addEventInfoWithChildren(child);
        }
      }
      return result;
    }

    private void removeEventInfo(final EventInfo info)
    {
      final AbstractEFAEvent event = info.getEvent();
      mEventInfoMap.remove(event);
    }

    private void removeEventInfo(final AbstractEFAEvent event)
    {
      mEventInfoMap.remove(event);
    }

    private List<AbstractEFAEvent> getEvents()
    {
      final List<AbstractEFAEvent> events =
        new ArrayList<>(mEventInfoMap.keySet());
      Collections.sort(events);
      return events;
    }

    VariableInfo getVariableInfo(final UnifiedEFAVariable var)
    {
      return mVariableInfoMap.get(var);
    }

    private boolean addVariableInfo(final VariableInfo info)
    {
      final UnifiedEFAVariable var = info.getVariable();
      final VariableInfo previous = mVariableInfoMap.put(var, info);
      return previous == null;
    }

    private void removeVariableInfo(final VariableInfo info)
    {
      final UnifiedEFAVariable var = info.getVariable();
      mVariableInfoMap.remove(var);
    }

    @SuppressWarnings("unused")
    private void removeVariableInfo(final UnifiedEFAVariable var)
    {
      mVariableInfoMap.remove(var);
    }

    private List<UnifiedEFAVariable> getVariables()
    {
      final List<UnifiedEFAVariable> vars =
        new ArrayList<>(mVariableInfoMap.keySet());
      Collections.sort(vars);
      return vars;
    }

    private Set<UnifiedEFATransitionRelation> getTransitionRelations()
    {
      return mTransitionRelations;
    }

    private boolean addTransitionRelation
      (final UnifiedEFATransitionRelation tr)
    {
      return mTransitionRelations.add(tr);
    }

    private void removeTransitionRelation
      (final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.remove(tr);
    }

    //#######################################################################
    //# Algorithm Support
    private boolean isReducible()
    {
      return mTransitionRelations.size() > 1 || mVariableInfoMap.size() > 0;
    }

    private Set<EventInfo> getRootEventInfo()
    {
      final Set<EventInfo> roots = new THashSet<>();
      for (final AbstractEFAEvent event : mEventInfoMap.keySet()) {
        final AbstractEFAEvent root = getRootEvent(event);
        final EventInfo info = mEventInfoMap.get(root);
        roots.add(info);
      }
      return roots;
    }

    private UnifiedEFACandidate selectCandidate() throws AnalysisException
    {
      if (mLocalVariableTR != null &&
          mTransitionRelations.contains(mLocalVariableTR)) {
        final UnifiedEFACandidate candidate = createLocalTRCandidate();
        if (candidate != null) {
          return candidate;
        }
      }
      final Collection<UnifiedEFAVariable> primedVariables =
        collectOnlyPrimedVariables();
      if (!primedVariables.isEmpty()) {
        return createVariablesCandidate(primedVariables);
      }
      /* only-variables-of-updates seems a bad strategy ...
      final Collection<UnifiedEFAVariable> onlyVariables =
        collectOnlyVariablesOfUpdates();
      if (!onlyVariables.isEmpty()) {
        return createVariablesCandidate(onlyVariables);
      }
      */
      if (mUsesLocalVariable) {
        final Collection<UnifiedEFAVariable> localVars = collectLocalVariables();
        if (!localVars.isEmpty()) {
          final UnifiedEFAVariable var =
            mVariableSelectionHeuristic.select(localVars);
          final VariableInfo info = getVariableInfo(var);
          final Set<VariableInfo> selectedVariableSet =
            Collections.singleton(info);
          final UnifiedEFATransitionRelation tr = info.getLocalTR();
          final Set<UnifiedEFATransitionRelation> trs =
            Collections.singleton(tr);
          final UnifiedEFACandidate candidate =
            new UnifiedEFACandidate(trs, selectedVariableSet);
          candidate.setIsVariableLocal(true);
          return candidate;
        }
      }
      final Collection<UnifiedEFACandidate> candidates =
        createMustLCandidates();
      return mCompositionSelectionHeuristic.select(candidates);
    }

    private UnifiedEFACandidate createVariablesCandidate
      (final Collection<UnifiedEFAVariable> vars)
      throws AnalysisException
    {
      final Set<UnifiedEFATransitionRelation> trs = Collections.emptySet();
      final UnifiedEFAVariable var =
        mVariableSelectionHeuristic.select(vars);
      final VariableInfo info = getVariableInfo(var);
      final Set<VariableInfo> selectedVariableSet =
        Collections.singleton(info);
      final UnifiedEFACandidate candidate =
        new UnifiedEFACandidate(trs, selectedVariableSet);
      return candidate;
    }

    private UnifiedEFACandidate createLocalTRCandidate()
    {
      final Set<UnifiedEFATransitionRelation> trsCopy = new THashSet<>(2);
      for (final AbstractEFAEvent event :
           mLocalVariableTR.getUsedEventsExceptTau()) {
        final EventInfo info = getEventInfo(event);
        final EventInfo parentInfo = info.getParent();
        if (parentInfo == null) {
          continue;
        }
        final Collection<UnifiedEFATransitionRelation> trs =
          parentInfo.getTransitionRelations();
        assert trs.size() == 0 || trs.size() == 1;
        if (trs.size() == 1) {
          trsCopy.addAll(trs);
          break;
        }
      }
      trsCopy.add(mLocalVariableTR);
      if (trsCopy.size() == 2) {
        final Set<VariableInfo> vars = Collections.emptySet();
        final UnifiedEFACandidate candidate =
          new UnifiedEFACandidate(trsCopy, vars);
        return candidate;
      } else {
        return null;
      }
    }

    private Collection<UnifiedEFAVariable> collectOnlyPrimedVariables()
    {
      final Collection<UnifiedEFAVariable> candidates = new ArrayList<>();
      for (final VariableInfo info : mVariableInfoMap.values()) {
        if (info.isPrimedOnly() || info.isUnPrimedOnly()) {
          final UnifiedEFAVariable var = info.getVariable();
          candidates.add(var);
        }
      }
      return candidates;
    }

    @SuppressWarnings("unused")
    private Collection<UnifiedEFAVariable> collectOnlyVariablesOfUpdates()
    {
      final Collection<UnifiedEFAVariable> candidates = new THashSet<>();
      for (final EventInfo event : mEventInfoMap.values()) {
        final Collection<VariableInfo> vars = event.getVariables();
        if (vars.size() == 1) {
          final VariableInfo info = vars.iterator().next();
          final UnifiedEFAVariable var = info.getVariable();
          candidates.add(var);
        }
      }
      return candidates;
    }

    private Collection<UnifiedEFAVariable> collectLocalVariables()
    {
      final Collection<UnifiedEFAVariable> localVars = new ArrayList<>();
      for (final VariableInfo info : mVariableInfoMap.values()) {
        if (info.getLocalTR() != null) {
          final UnifiedEFAVariable var = info.getVariable();
          localVars.add(var);
        }
      }
      return localVars;
    }

    private Collection<UnifiedEFACandidate> createMustLCandidates()
      throws AnalysisAbortException
    {
      final Map<UnifiedEFACandidate,UnifiedEFACandidate> candidateMap =
        new HashMap<>(mEventInfoMap.size());
      for (final Map.Entry<AbstractEFAEvent,EventInfo> entry :
           mEventInfoMap.entrySet()) {
        checkAbort();
        final EventInfo info = entry.getValue();
        final AbstractEFAEvent event = entry.getKey();
        if (isRootEvent(event)) {
          final Set<UnifiedEFATransitionRelation> trs =
            new THashSet<>();
          final Set<VariableInfo> vars = new THashSet<>();
          info.collectCandidate(trs, vars);
//          if (!mUsesLocalVariable || vars.isEmpty()) {
            UnifiedEFACandidate candidate =
              new UnifiedEFACandidate(trs, vars);
            final UnifiedEFACandidate existing = candidateMap.get(candidate);
            if (existing == null) {
              candidateMap.put(candidate, candidate);
            } else {
              candidate = existing;
            }
            candidate.addLocalEvent(info);
//          }
        }
      }
      return candidateMap.values();
    }

//    @SuppressWarnings("unused")
//    private void createOneVariableCandidates()
//    {
//      final Collection<VariableInfo> vars = mVariableInfoMap.values();
//      for (final VariableInfo var : vars) {
//        final Set<UnifiedEFATransitionRelation> trs = Collections.emptySet();
//          final Set<VariableInfo> varCandidate = Collections.singleton(var);
//          final UnifiedEFACandidate candidate = new UnifiedEFACandidate(trs, varCandidate);
//          mCandidateMap.put(candidate, candidate);
//      }
//    }


    //#######################################################################
    //# Interface java.util.Comparable
    @Override
    public int compareTo(final SubSystemInfo sub)
    {
      int size1 = mTransitionRelations.size() + mVariableInfoMap.size();
      int size2 = sub.mTransitionRelations.size() + sub.mVariableInfoMap.size();
      if (size1 != size2) {
        return size1 - size2;
      }
      size1 = mEventInfoMap.size();
      size2 = sub.mEventInfoMap.size();
      if (size1 != size2) {
        return size1 - size2;
      }
      size1 = mTransitionRelations.size();
      size2 = sub.mTransitionRelations.size();
      if (size1 != size2) {
        return size1 - size2;
      }
      size1 = mVariableInfoMap.size();
      size2 = sub.mVariableInfoMap.size();
      if (size1 != size2) {
        return size1 - size2;
      }
      return 0;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuilder buffer = new StringBuilder("Transition relations");
      char sep = ':';
      for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
        buffer.append(sep);
        buffer.append(' ');
        buffer.append(tr.getName());
        sep = ',';
      }
      buffer.append("\nVariables");
      sep = ':';
      for (final UnifiedEFAVariable var : mVariableInfoMap.keySet()) {
        buffer.append(sep);
        buffer.append(' ');
        buffer.append(var.getName());
        sep = ',';
      }
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final Map<AbstractEFAEvent,EventInfo> mEventInfoMap;
    private final Map<UnifiedEFAVariable,VariableInfo> mVariableInfoMap;
    private final Set<UnifiedEFATransitionRelation> mTransitionRelations;
  }


  //#########################################################################
  //# Data Members
  private DocumentManager mDocumentManager;
  private CompilerOperatorTable mCompilerOperatorTable;
  private UnifiedEFASimplifierFactory mSimplifierFactory;
  private SelectionHeuristic<UnifiedEFACandidate> mCompositionSelectionHeuristic;
  private SelectionHeuristic<UnifiedEFAVariable> mVariableSelectionHeuristic;
  private int mInternalStateLimit = Integer.MAX_VALUE;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;
  private boolean mUsesLocalVariable;

  private UnifiedEFAVariableCollector mVariableCollector;
  private UnifiedEFAVariableUnfolder mUnfolder;
  private UnifiedEFASimplifier mSimplifier;
  private UnifiedEFAUpdateMerger mUpdateMerger;
  private UnifiedEFASynchronousProductBuilder mSynchronizer;
  private EFANonblockingChecker mNonblockingChecker;

  private UnifiedEFASystem mMainEFASystem;
  private PriorityQueue<SubSystemInfo> mSubSystemQueue;
  private Set<UnifiedEFATransitionRelation> mDirtyTRs;
  private SubSystemInfo mCurrentSubSystem;
  private UnifiedEFATransitionRelation mLocalVariableTR;
  private AbstractEFAEvent mDummyRoot;

}
