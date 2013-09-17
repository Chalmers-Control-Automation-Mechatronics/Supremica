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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.efa.base.EFANonblockingChecker;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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
  }

  @Override
  public boolean run()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
        Collection<UnifiedEFATransitionRelation> trs =
        mMainEFASystem.getTransitionRelations();
        mCurrentSubSystem = new SubSystemInfo(mMainEFASystem.getEvents().size(),
                                            mMainEFASystem.getVariables().size(),
                                            trs);
      createVariableInfo();
      createEventInfo();
      for (final UnifiedEFATransitionRelation tr : trs) {
        simplifyTR(tr);
      }
      splitSubsystems();
      while (mCurrentSubSystem != null) {
        while (mCurrentSubSystem.isReducible()) {
          mCurrentSubSystem.createCandidates();
          final Candidate minCandidate = Collections.min(mCandidateMap.keySet());
          applyCandidate(minCandidate);
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
    if (mCurrentSubSystem.getNumberOfEvents() == 0) {
      final Set<UnifiedEFATransitionRelation> trs =
        mCurrentSubSystem.getTransitionRelations();
      for (final UnifiedEFATransitionRelation tr : trs) {
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        if (rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA)) {
          final Set<UnifiedEFATransitionRelation> singletonTR =
            Collections.singleton(tr);
          mCurrentSubSystem = new SubSystemInfo(0, 0, singletonTR);
        }
      }
      final Set<UnifiedEFATransitionRelation> empty =
        Collections.emptySet();
      mCurrentSubSystem = new SubSystemInfo(0, 0, empty);
    } else {
      final Set<EventInfo> roots = mCurrentSubSystem.getRootEventInfo();
      SubSystemInfo sub = collectSybsystem(roots);
      if (roots.isEmpty()) {
        return;
      } else {
        mSubSystemQueue.add(sub);
        while (!roots.isEmpty()) {
          sub = collectSybsystem(roots);
          mSubSystemQueue.add(sub);
        }
        mCurrentSubSystem = mSubSystemQueue.poll();
      }
    }
  }

  private SubSystemInfo collectSybsystem(final Collection<EventInfo> remaining)
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
    if (!vars.isEmpty()) {
      final VariableInfo selectedVarInfo = vars.iterator().next();
      final UnifiedEFAVariable selectedVar = selectedVarInfo.getVariable();
      mUnfolder.setUnfoldedVariable(selectedVar);
      final List<AbstractEFAEvent> originalEvents =
        selectedVarInfo.getLeaveEvents();
      mUnfolder.setOriginalEvents(originalEvents);
      mUnfolder.run();
      result = mUnfolder.getTransitionRelation();
      registerTR(result);
      unregisterVariable(selectedVarInfo);
    } else if (trs.size() > 1) {
      mSynchronizer.setInputTransitionRelations(trs);
      mSynchronizer.run();
      result =  mSynchronizer.getSynchronousProduct();
      registerTR(result);
      for (final UnifiedEFATransitionRelation tr : trs) {
        unregisterTR(tr);
      }
    } else {
      result = trs.get(0);
    }
    simplifyTR(result);
  }

  private void simplifyTR(final UnifiedEFATransitionRelation tr)
    throws AnalysisException
  {
    final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
    final ListBufferTransitionRelation resultRel = tr.getTransitionRelation();
    // 1. Mark local events for hiding.
    for (final AbstractEFAEvent event : tr.getUsedEventsExceptTau()) {
      final EventInfo localInfo = mCurrentSubSystem.getEventInfo(event);
      if (localInfo.isLocal()) {
        final int code = encoding.getEventId(event);
        final byte status = resultRel.getProperEventStatus(code);
        resultRel.setProperEventStatus(code,
                                       status | EventEncoding.STATUS_LOCAL);
      }
    }
    // 2. Run abstraction chain.
    final UnifiedEFATransitionRelation simplifiedTR = mSimplifier.run(tr);
    // 3. Update index structures.
    if (simplifiedTR != null) {
      registerTR(simplifiedTR);
      unregisterTR(tr);
    }
  }

  private void registerTR(final UnifiedEFATransitionRelation tr)
  {
    for (final AbstractEFAEvent event : tr.getAllEventsExceptTau()) {
      EventInfo info = mCurrentSubSystem.getEventInfo(event);
      if (info == null) {
        info = new EventInfo(event);
        mCurrentSubSystem.addEventInfo(info);
        final AbstractEFAEvent originalEvent = event.getOriginalEvent();
        if (originalEvent != null) {
          final EventInfo originalInfo =
            mCurrentSubSystem.getEventInfo(originalEvent);
          originalInfo.addChildEvent(info);
        }
      }
      if (tr.isUsedEvent(event)) {
        info.addTransitionRelation(tr);
      } else {
        removeEmptyEventInfo(info);
      }
    }
    mCurrentSubSystem.addTransitionRelation(tr);
  }

  /**
   * Marks a variable as removed. This method is called after a variable
   * has been unfolded, and all its events have been renamed and the event
   * information updated. Therefore, the variable is removed, and the
   * associated event informations are updated by removing <I>all</I>
   * variables, and by removing those variables from the associated
   * event information records.
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
  }

  private void removeEmptyEventInfo(final EventInfo info)
  {
    if (info.isEmpty()) {
      final AbstractEFAEvent event = info.getEvent();
      mCurrentSubSystem.removeEventInfo(event);
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
        root = event;
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

    private double getHeuristicValue()
    {
      if (mHeuristicValue < 0) {
        mHeuristicValue = calculateMinS();
      }
      return mHeuristicValue;
    }

    private double calculateMinS()
    {
      // TODO Event counts may be wrong --- watch out for renamings
      final Set<AbstractEFAEvent> eventSet = new THashSet<>();
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
      final double numLocalEvents = mLocalEvents.size();
      final double numEvents = eventSet.size();
      return (1.0 - numLocalEvents / numEvents) * numStates;
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
      if (candidate.getHeuristicValue() < getHeuristicValue()) {
        return -1;
      } else if (candidate.getHeuristicValue() > getHeuristicValue()) {
        return 1;
      } else {
        return 0;
      }
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
    private double mHeuristicValue = -1;
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
      final Set<UnifiedEFAVariable> vars = new THashSet<>();
      mVariableCollector.collectAllVariables(mEvent.getUpdate(), vars);
      mVariables = new ArrayList<>(vars.size());
      for (final UnifiedEFAVariable var : vars) {
        final VariableInfo info = mCurrentSubSystem.getVariableInfo(var);
        mVariables.add(info);
        info.addEvent(this);
      }
      mChildrenEvents = new ArrayList<>();
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
      mVariables = Collections.emptyList();
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

    //#######################################################################
    //# Checking for Local Events
    private boolean isEmpty()
    {
      return mTransitionRelations.isEmpty() && mVariables.isEmpty();
    }

    private boolean isLocal()
    {
      if (mChildrenEvents.size() != 0) {
        return false;
      } else if (mTransitionRelations.size() + mVariables.size() > 1) {
        return false;
      } else {
        return isRootEvent(mEvent);
      }
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
    private final List<EventInfo> mChildrenEvents;
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

    @SuppressWarnings("unused")
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

    private void createCandidates()
      throws AnalysisAbortException
    {
      mCandidateMap = new HashMap<>(mEventInfoMap.size());
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
      }
      buffer.append("\nVariables");
      sep = ':';
      for (final UnifiedEFAVariable var : mVariableInfoMap.keySet()) {
        buffer.append(sep);
        buffer.append(' ');
        buffer.append(var.getName());
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
  private SubSystemInfo mCurrentSubSystem;
  private Map<Candidate,Candidate> mCandidateMap;

}
