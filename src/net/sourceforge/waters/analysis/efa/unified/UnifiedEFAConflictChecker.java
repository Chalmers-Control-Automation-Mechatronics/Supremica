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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.efa.base.EFANonblockingChecker;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
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
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


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

  public void setDocumentManager(final DocumentManager document)
  {
    mDocumentManager = document;
  }

  public CompilerOperatorTable getOperatorTable()
  {
    return mCompilerOperatorTable;
  }

  public void setCompilerOperatorTable(final CompilerOperatorTable op)
  {
    mCompilerOperatorTable = op;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
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
    mOnlyAutomata = true;
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
    mUnfolder = new UnifiedEFAVariableUnfolder
      (getFactory(), mCompilerOperatorTable, context);
    mSimplifier = UnifiedEFASimplifier.createStandardNonblockingProcedure
      (Equivalence.OBSERVATION_EQUIVALENCE, mInternalTransitionLimit);
    mSynchronizer = new UnifiedEFASynchronousProductBuilder();
    mSynchronizer.setStateLimit(mInternalStateLimit);
    mSynchronizer.setTransitionLimit(mInternalTransitionLimit);
    mNonblockingChecker = new EFANonblockingChecker();
    final Comparator<Candidate> maxEvents = new ComparatorMaxEvents();
    final Comparator<Candidate> maxSelfloops = new ComparatorMaxSelfloops();
    final Comparator<Candidate> minStates = new ComparatorMinStates();
    mVariableComparator = new CandidateComparator(maxEvents, maxSelfloops, minStates);
    mAutomataComparator = new CandidateComparator(minStates);
    mUpdateMerger =
      new UnifiedEFAUpdateMerger(getFactory(), mCompilerOperatorTable, context);
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
      mCurrentSubSystem = new SubSystemInfo(mMainEFASystem.getEvents().size(),
                                            mMainEFASystem.getVariables().size(),
                                            trs);
      mDirtyTRs = new THashSet<>(trs);
      createVariableInfo();
      createEventInfo();
      simplifyDirtyTransitionRelations();
      splitSubsystems();
      while (mCurrentSubSystem != null) {
        while (mCurrentSubSystem.isReducible()) {
          final Candidate minCandidate = mCurrentSubSystem.selectCandidate();
          applyCandidate(minCandidate);
          simplifyDirtyTransitionRelations();
          splitSubsystems();
        }
        trs = mCurrentSubSystem.getTransitionRelations();
        if (trs.isEmpty()) {
          mCurrentSubSystem = mSubSystemQueue.poll();
          continue;
        }
        final UnifiedEFATransitionRelation finalTR =
          trs.iterator().next();
        if (!mNonblockingChecker.run(finalTR)) {
          return false;
        } else {
          mCurrentSubSystem = mSubSystemQueue.poll();
        }
      }
      return true;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
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
    final SubSystemInfo sub = new SubSystemInfo();
    final BFSSearchSpace<EventInfo> search = new BFSSearchSpace<>();
    final EventInfo startInfo = remaining.iterator().next();
    remaining.remove(startInfo);
    search.add(startInfo);
    while (!search.isEmpty()) {
      checkAbort();
      final EventInfo info = search.remove();
      final Set<UnifiedEFATransitionRelation> trs =
        new THashSet<UnifiedEFATransitionRelation>();
      final Set<VariableInfo> vars = new THashSet<VariableInfo>();
      info.collectCandidate(trs, vars);
      for (final UnifiedEFATransitionRelation tr : trs) {
        sub.addTransitionRelation(tr);
        final Set<AbstractEFAEvent> events = tr.getUsedEventsExceptTau();
        for (final AbstractEFAEvent nextEvent : events) {
          final AbstractEFAEvent rootEvent = getRootEvent(nextEvent);
          final EventInfo rootInfo = mCurrentSubSystem.getEventInfo(rootEvent);
          search.add(rootInfo);
          sub.addEventInfo(rootInfo);
          remaining.remove(rootInfo);
        }
      }
      for (final VariableInfo var : vars) {
        sub.addVariableInfo(var);
        final Collection<EventInfo> events = var.getEvents();
        for (final EventInfo eventInfo : events) {
          final AbstractEFAEvent nextEvent = eventInfo.getEvent();
          final AbstractEFAEvent rootEvent = getRootEvent(nextEvent);
          final EventInfo rootInfo = mCurrentSubSystem.getEventInfo(rootEvent);
          search.add(rootInfo);
          sub.addEventInfo(rootInfo);
          remaining.remove(rootInfo);
        }
      }
    }
    return sub;
  }

  private void applyCandidate(final Candidate candidate)
    throws AnalysisException, EvalException
  {
    checkAbort();
    final List<VariableInfo> vars = candidate.getVariables();
    final List<UnifiedEFATransitionRelation> trs =
      candidate.getTransitionRelations();
    final UnifiedEFATransitionRelation result;
    final boolean isNewTR;
    List<AbstractEFAEvent> originalEvents = null;
    if (!vars.isEmpty()) {
      final VariableInfo selectedVarInfo = vars.iterator().next();
      final UnifiedEFAVariable selectedVar = selectedVarInfo.getVariable();
      mUnfolder.setUnfoldedVariable(selectedVar);
      originalEvents = selectedVarInfo.getLeaveEvents();
      mUnfolder.setOriginalEvents(originalEvents);
      mUnfolder.run();
      result = mUnfolder.getTransitionRelation();
      registerTR(result, true);
      unregisterVariable(selectedVarInfo);
      isNewTR = true;
    } else if (trs.size() > 1) {
      mSynchronizer.setInputTransitionRelations(trs);
      mSynchronizer.run();
      result = mSynchronizer.getSynchronousProduct();
      registerTR(result, false);
      for (final UnifiedEFATransitionRelation tr : trs) {
        unregisterTR(tr);
      }
      isNewTR = true;
    } else {
      result = trs.get(0);
      isNewTR = false;
    }
    final UnifiedEFATransitionRelation simplifiedTR = simplifyTR(result);
    if (simplifiedTR == null && isNewTR) {
      recordBlockedEvents(result);
    }
    if (originalEvents != null) {
      if (simplifiedTR == null) {
        mUpdateMerger.setTransitionRelation(result);
      } else {
        mUpdateMerger.setTransitionRelation(simplifiedTR);
      }
      for (final AbstractEFAEvent event : originalEvents) {
        final EventInfo info = mCurrentSubSystem.getEventInfo(event);
        if (!(info == null || info.isBlocked())) {
          final List<AbstractEFAEvent> children = new ArrayList<>();
          for (final EventInfo childInfo : info.getChildrenEvents()) {
            children.add(childInfo.getEvent());
          }
          mUpdateMerger.setUnfoldedEvents(children);
          mUpdateMerger.run();
          for (final AbstractEFAEvent removedEvent : mUpdateMerger.getRemovedEvents()) {
            final EventInfo removedInfo = mCurrentSubSystem.getEventInfo(removedEvent);
            mCurrentSubSystem.removeEventInfo(removedEvent);
            info.removeChildEvent(removedInfo);
            for (final VariableInfo varInfo : removedInfo.getVariables()) {
              varInfo.removeEvent(removedInfo);
            }
          }
          for (final AbstractEFAEvent addedEvent : mUpdateMerger.getAddedEvents()) {
            final EventInfo newInfo = new EventInfo(addedEvent);
            mCurrentSubSystem.addEventInfo(newInfo);
          }
        }
      }
    }
  }

  private void simplifyDirtyTransitionRelations() throws AnalysisException
  {
    while (!mDirtyTRs.isEmpty()) {
      final UnifiedEFATransitionRelation tr = mDirtyTRs.iterator().next();
      mDirtyTRs.remove(tr);
      simplifyTR(tr);
    }
  }

  private UnifiedEFATransitionRelation simplifyTR(final UnifiedEFATransitionRelation tr)
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

  private void registerTR(final UnifiedEFATransitionRelation tr,
                          final boolean unfolding) throws AnalysisAbortException
  {
    getLogger().debug(tr.getName() + " is nonblocking: " +
                      mNonblockingChecker.run(tr));
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
          new ArrayList<>(info.getChildrenEvents());
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


  //#########################################################################
  //# Inner Class Candidate
  private class Candidate implements Comparable<Candidate>
  {
    //#######################################################################
    //# Constructors
    private Candidate(final Set<UnifiedEFATransitionRelation> trs,
                      final Set<VariableInfo> vars)
    {
      mTransitionRelations = new ArrayList<>(trs);
      Collections.sort(mTransitionRelations);
      mVariables = new ArrayList<>(vars);
      Collections.sort(mVariables);
      mLocalEvents = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private List<UnifiedEFATransitionRelation> getTransitionRelations()
    {
      return mTransitionRelations;
    }

    private List<VariableInfo> getVariables()
    {
      return mVariables;
    }

    private void addLocalEvent(final EventInfo info)
    {
      mLocalEvents.add(info);
    }

    private double getEstimatedNumberOfStates()
    {
      if (mEstimatedNumberOfStates < 0) {
        Set<AbstractEFAEvent> eventSet = new THashSet<>();
        double numStates = 1.0;
        for (final VariableInfo var : mVariables) {
          numStates *= var.getRangeSize();
          final List<AbstractEFAEvent> events = var.getLeaveEvents();
          eventSet.addAll(events);
        }
        for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
          numStates *= tr.getTransitionRelation().getNumberOfReachableStates();
          eventSet.addAll(tr.getUsedEventsExceptTau());
        }
        eventSet = removeNonLeaves(eventSet);
        Set<AbstractEFAEvent> localSet = new THashSet<>(mLocalEvents.size());
        for (final EventInfo localInfo : mLocalEvents) {
          localSet.add(localInfo.getEvent());
        }
        localSet = removeNonLeaves(localSet);
        final double numLocalEvents = localSet.size();
        final double numEvents = eventSet.size();
        mEstimatedNumberOfStates = (1.0 - numLocalEvents / numEvents) * numStates;
      }
      return mEstimatedNumberOfStates;
    }

    private int getNumberOfEvents()
    {
      final VariableInfo varInfo = mVariables.get(0);
      return varInfo.getEvents().size();
    }

    /**
     * Returns the number of selfloop events of a variable unfolding
     * candidate. The number of selfloop events is computed as the
     * number of events of the first variable of the candidate,
     * but only counting events that do not appear in any transition
     * relation and whose ancestors do not appear in any transition
     * relation.
     */
    private int getNumberOfSelfloops()
    {
      if (mNumberOfSelfloops < 0) {
        final VariableInfo varInfo = mVariables.get(0);
        final Collection<EventInfo> eventInfo = varInfo.getEvents();
        int selfloops = 0;
        outer :
          for (final EventInfo info : eventInfo) {
            List<UnifiedEFATransitionRelation> trs = info.getTransitionRelations();
            if (trs.size() > 0) {
              continue;
            }
            AbstractEFAEvent original = info.getEvent().getOriginalEvent();
            while (original != null) {
              final EventInfo originalInfo = mCurrentSubSystem.mEventInfoMap.get(original);
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

    private Set<AbstractEFAEvent> removeNonLeaves
      (final Collection<AbstractEFAEvent> eventSet)
    {
      final Set<AbstractEFAEvent> copy = new THashSet<>(eventSet);
      for (final AbstractEFAEvent event : eventSet) {
        AbstractEFAEvent original = event.getOriginalEvent();
        while (original != null) {
          copy.remove(original);
          original = original.getOriginalEvent();
        }
      }
      return copy;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public int hashCode()
    {
      return mTransitionRelations.hashCode() + mVariables.hashCode();
    }

    @Override
    public boolean equals(final Object object)
    {
      if (object != null && object.getClass() == getClass()) {
        final Candidate candidate = (Candidate) object;
        return
          candidate.mTransitionRelations.equals(mTransitionRelations) &&
          candidate.mVariables.equals(mVariables);
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Interface java.util.Comparable
    @Override
    public int compareTo(final Candidate candidate)
    {
      return toString().compareTo(candidate.toString());
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      String sep = "";
      final StringBuffer buffer = new StringBuffer("{");
      for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
        buffer.append(sep);
        buffer.append(tr.getName());
        sep = ",";
      }
      sep = ";";
      for (final VariableInfo var : mVariables) {
        buffer.append(sep);
        buffer.append(var.getName());
        sep = ",";
      }
      buffer.append('}');
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private final List<VariableInfo> mVariables;
    private final List<EventInfo> mLocalEvents;
    private int mNumberOfSelfloops = -1;
    private double mEstimatedNumberOfStates = -1;
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo
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
    private AbstractEFAEvent getEvent()
    {
      return mEvent;
    }

    private List<UnifiedEFATransitionRelation> getTransitionRelations()
    {
      return mTransitionRelations;
    }

    private void addTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.add(tr);
    }

    private void removeTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.remove(tr);
    }

    private Collection<VariableInfo> getVariables()
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

    private List<EventInfo> getChildrenEvents()
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
      mChildrenEvents.addAll(event.getChildrenEvents());
    }

    private EventInfo getParent()
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
     * or variables, and of the following conditions holds.
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

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuffer buffer = new StringBuffer("<");
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
  //# Inner Class VariableInfo
  private static class VariableInfo implements Comparable<VariableInfo>
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
    private UnifiedEFAVariable getVariable()
    {
      return mVariable;
    }

    private String getName()
    {
      return mVariable.getName();
    }

    private int getRangeSize()
    {
      return mVariable.getRange().size();
    }

    private Collection<EventInfo> getEvents()
    {
      return mEventInfo;
    }

    private void addEvent(final EventInfo event)
    {
      mEventInfo.add(event);
    }

    private void removeEvent(final EventInfo event)
    {
      mEventInfo.remove(event);
    }

    private List<AbstractEFAEvent> getLeaveEvents()
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

    @SuppressWarnings("unused")
    private boolean isUnPrimedOnly()
    {
      for (final EventInfo info : mEventInfo){
        if (info.getPrimedVariables().contains(this)) {
          return false;
        }
      }
      return true;
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
  }


  //#########################################################################
  //# Inner Class SubSystemInfo
  private class SubSystemInfo implements Comparable<SubSystemInfo>
  {
    //#######################################################################
    //# Constructors
    private SubSystemInfo()
    {
      mEventInfoMap = new HashMap<>();
      mVariableInfoMap = new HashMap<>();
      mTransitionRelations = new THashSet<>();
    }

    public SubSystemInfo(final int numberOfEvents, final int numberOfVariables,
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

    private void addEventInfo(final EventInfo info)
    {
      final AbstractEFAEvent event = info.getEvent();
      mEventInfoMap.put(event, info);
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

    private VariableInfo getVariableInfo(final UnifiedEFAVariable var)
    {
      return mVariableInfoMap.get(var);
    }

    private void addVariableInfo(final VariableInfo info)
    {
      final UnifiedEFAVariable var = info.getVariable();
      mVariableInfoMap.put(var, info);
    }

    private void removeVariableInfo(final VariableInfo info)
    {
      final UnifiedEFAVariable var = info.getVariable();
      mVariableInfoMap.remove(var);
    }

    @SuppressWarnings("unused")
    private void removeVariableInfo(final UnifiedEFAVariable var)
    {
      mEventInfoMap.remove(var);
    }

    private Set<UnifiedEFATransitionRelation> getTransitionRelations()
    {
      return mTransitionRelations;
    }

    private void addTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.add(tr);
    }

    private void removeTransitionRelation(final UnifiedEFATransitionRelation tr)
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

    private Candidate selectCandidate() throws AnalysisAbortException
    {
      mCandidateMap = new HashMap<>(mEventInfoMap.size());
      createOnlyPrimedCandidates();
      if (!mCandidateMap.isEmpty()) {
        return Collections.min(mCandidateMap.keySet(), mVariableComparator);
      }
      createMustLCandidates();
      if (!mCandidateMap.isEmpty()) {
        return Collections.min(mCandidateMap.keySet(), mAutomataComparator);
      }
      createOneVariableCandidates();
      return Collections.min(mCandidateMap.keySet(), mVariableComparator);
    }

    private void createOnlyPrimedCandidates()
    {
      final Collection<VariableInfo> vars = mVariableInfoMap.values();
      for (final VariableInfo var : vars) {
        if (var.isPrimedOnly()) {
          final Set<UnifiedEFATransitionRelation> trs = Collections.emptySet();
          final Set<VariableInfo> singleVar = Collections.singleton(var);
          final Candidate candidate = new Candidate(trs, singleVar);
          mCandidateMap.put(candidate, candidate);
        }
      }
    }

    private void createMustLCandidates()
      throws AnalysisAbortException
    {
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
          if (!mOnlyAutomata || vars.isEmpty()) {
            Candidate candidate = new Candidate(trs, vars);
            final Candidate existing = mCandidateMap.get(candidate);
            if (existing == null) {
              mCandidateMap.put(candidate, candidate);
            } else {
              candidate = existing;
            }
            candidate.addLocalEvent(info);
          }
        }
      }
    }

    private void createOneVariableCandidates()
    {
      final Collection<VariableInfo> vars = mVariableInfoMap.values();
      for (final VariableInfo var : vars) {
        final Set<UnifiedEFATransitionRelation> trs = Collections.emptySet();
          final Set<VariableInfo> varCandidate = Collections.singleton(var);
          final Candidate candidate = new Candidate(trs, varCandidate);
          mCandidateMap.put(candidate, candidate);
      }
    }

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
      final StringBuffer buffer = new StringBuffer("Transition relations");
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
  //# Inner Class
  private static class ComparatorMaxEvents implements Comparator<Candidate>
  {

    @Override
    public int compare(final Candidate candidate1, final Candidate candidate2)
    {
      return candidate2.getNumberOfEvents()-candidate1.getNumberOfEvents();
    }
  }

  //#########################################################################
  //# Inner Class
  private static class ComparatorMaxSelfloops implements Comparator<Candidate>
  {

    @Override
    public int compare(final Candidate candidate1, final Candidate candidate2)
    {
      return candidate2.getNumberOfSelfloops()-
        candidate1.getNumberOfSelfloops();
    }
  }

  //#########################################################################
  //# Inner Class
  private static class ComparatorMinStates implements Comparator<Candidate>
  {

    @Override
    public int compare(final Candidate candidate1, final Candidate candidate2)
    {
      if (candidate1.getEstimatedNumberOfStates() < candidate2.getEstimatedNumberOfStates()) {
        return -1;
      } else if (candidate1.getEstimatedNumberOfStates() > candidate2.getEstimatedNumberOfStates()) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  //#########################################################################
  //# Inner Class
  private static class CandidateComparator implements Comparator<Candidate>
  {
    @SafeVarargs
    private CandidateComparator(final Comparator<Candidate>... comparators)
    {
      mComparators = comparators;
    }

    @Override
    public int compare(final Candidate candidate1, final Candidate candidate2)
    {
      for (final Comparator<Candidate> comparator : mComparators) {
        final int compare= comparator.compare(candidate1, candidate2);
        if (compare != 0) {
          return compare;
        }
      }
      return candidate1.compareTo(candidate2);
    }
    //#########################################################################
    //# Data Members
    private final Comparator<Candidate>[] mComparators;
  }

  //#########################################################################
  //# Data Members
  private CompilerOperatorTable mCompilerOperatorTable;
  private int mInternalStateLimit = Integer.MAX_VALUE;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;

  private DocumentManager mDocumentManager;
  private UnifiedEFAVariableCollector mVariableCollector;
  private UnifiedEFAVariableUnfolder mUnfolder;
  private UnifiedEFASimplifier mSimplifier;
  private UnifiedEFASynchronousProductBuilder mSynchronizer;
  private EFANonblockingChecker mNonblockingChecker;

  private UnifiedEFASystem mMainEFASystem;
  private PriorityQueue<SubSystemInfo> mSubSystemQueue;
  private Set<UnifiedEFATransitionRelation> mDirtyTRs;
  private SubSystemInfo mCurrentSubSystem;
  private Map<Candidate,Candidate> mCandidateMap;
  private boolean mOnlyAutomata;
  private Comparator<Candidate> mVariableComparator;
  private Comparator<Candidate> mAutomataComparator;
  private UnifiedEFAUpdateMerger mUpdateMerger;


}
