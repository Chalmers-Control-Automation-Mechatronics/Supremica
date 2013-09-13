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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier.Equivalence;
import net.sourceforge.waters.analysis.efa.base.EFANonblockingChecker;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
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
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
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
    mEFASynchronizer = new UnifiedEFASynchronousProductBuilder();
    mSimplifier = UnifiedEFASimplifier.createStandardNonblockingProcedure
      (Equivalence.OBSERVATION_EQUIVALENCE, mInternalTransitionLimit);
    mNonblockingChecker = new EFANonblockingChecker();

  }

  @Override
  public boolean run()
    throws EvalException, AnalysisException
  {
    try {
      setUp();
      final ModuleProxy module = getModel();
      final List<ParameterBindingProxy> binding = getBindings();
      final UnifiedEFACompiler compiler =
        new UnifiedEFACompiler(mDocumentManager, module);
      compiler.setConfiguredDefaultMarking(getConfiguredDefaultMarking());
      final List<String> none = Collections.emptyList();
      compiler.setEnabledPropertyNames(none);
      mCurrentEFASystem = compiler.compile(binding);
      mVariableCollector = new UnifiedEFAVariableCollector
        (mCompilerOperatorTable, mCurrentEFASystem.getVariableContext());
      createEventInfo();
      createVariableInfo();
      mTransitionRelations =
        new THashSet<>(mCurrentEFASystem.getTransitionRelations());
      for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
        simplifyTR(tr);
      }
      while (mTransitionRelations.size() > 1 || mVariableInfoMap.size() > 0) {
        createCandidates();
        final Candidate minCandidate = Collections.min(mCandidateMap.keySet());
        applyCandidate(minCandidate);
      }
      if (mTransitionRelations.isEmpty()) {
        return true;
      }
      final UnifiedEFATransitionRelation finalTR = mTransitionRelations.iterator().next();
      return mNonblockingChecker.run(finalTR);
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
    mCompilerOperatorTable = null;
    mDocumentManager = null;
    mEFASynchronizer = null;
    mSimplifier = null;
    mCurrentEFASystem = null;
    super.tearDown();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createEventInfo()
  {
    final List<AbstractEFAEvent> events = mCurrentEFASystem.getEvents();
    mEventInfoMap = new HashMap<>(events.size());
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = new EventInfo(event);
      mEventInfoMap.put(event, info);
    }
    final List<UnifiedEFATransitionRelation> trs =
      mCurrentEFASystem.getTransitionRelations();
    for (final UnifiedEFATransitionRelation tr : trs) {
      for (final AbstractEFAEvent event : tr.getUsedEvents()) {
        final EventInfo info =  mEventInfoMap.get(event);
        info.addTransitionRelation(tr);
      }
    }
  }

  private void createVariableInfo()
  {
    final List<UnifiedEFAVariable> vars = mCurrentEFASystem.getVariables();
    mVariableInfoMap = new HashMap<>(vars.size());
    for (final UnifiedEFAVariable var : vars) {
      final VariableInfo info = new VariableInfo();
      mVariableInfoMap.put(var, info);
    }
    for (final EventInfo eventInfo : mEventInfoMap.values()) {
      for (final UnifiedEFAVariable var : eventInfo.getVariables()){
        final VariableInfo varInfo = mVariableInfoMap.get(var);
        varInfo.addEvent(eventInfo);
      }
    }
  }

  private void createCandidates()
  {
    mCandidateMap = new HashMap<>(mEventInfoMap.size());
    for (final Map.Entry<AbstractEFAEvent,EventInfo> entry :
      mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      final AbstractEFAEvent event = entry.getKey();
      if (isRoot(event)) {
        final Set<UnifiedEFATransitionRelation> trs =
          new THashSet<>();
        final Set<UnifiedEFAVariable> vars = new THashSet<>();
        info.collectCandidate(trs, vars);
        Candidate candidate = new Candidate(trs, vars);
        final Candidate existing = mCandidateMap.get(candidate);
        if (existing == null) {
          mCandidateMap.put(candidate, candidate);
        } else {
          candidate = existing;
        }
        candidate.addLocalEvent(event);
      }
    }
  }

  private void applyCandidate(final Candidate candidate)
    throws AnalysisException, EvalException
  {
    final List<UnifiedEFAVariable> vars = candidate.getVariables();
    final UnifiedEFATransitionRelation result;
    if (!vars.isEmpty()) {
      int smallest = Integer.MAX_VALUE;
      UnifiedEFAVariable selectedVar = null;
      for (final UnifiedEFAVariable var: vars) {
        final int size = var.getRange().size();
        if (smallest > size) {
          smallest = size;
          selectedVar = var;
        }
      }
      mUnfolder = new UnifiedEFAVariableUnfolder
        (getFactory(), mCompilerOperatorTable, mCurrentEFASystem.getVariableContext());
      mUnfolder.setUnfoldedVariable(selectedVar);
      final VariableInfo varInfo = mVariableInfoMap.get(selectedVar);
      final List<AbstractEFAEvent> originalEvents = varInfo.getChildrenEvents();
      mUnfolder.setOriginalEvents(originalEvents);
      mUnfolder.run();
      result = mUnfolder.getTransitionRelation();
      registerTR(result);
      unregisterVariable(selectedVar);
    } else {
      final List<UnifiedEFATransitionRelation> trs =
        candidate.getTransitionRelations();
      if (trs.size() > 1) {
        mEFASynchronizer.setInputTransitionRelations(trs);
        mEFASynchronizer.run();
        result =  mEFASynchronizer.getSynchronousProduct();
        registerTR(result);
        for (final UnifiedEFATransitionRelation tr : trs) {
          unregisterTR(tr);
        }
      } else {
        result = trs.get(0);
      }
    }
    simplifyTR(result);
  }

  private boolean isRoot(final AbstractEFAEvent event)
  {
    AbstractEFAEvent original = event.getOriginalEvent();
    while (original != null) {
      final EventInfo info = mEventInfoMap.get(original);
      if (info != null) {
        return false;
      } else {
        original = original.getOriginalEvent();
      }
    }
    return true;
  }

  private void simplifyTR(final UnifiedEFATransitionRelation tr)
    throws AnalysisException
  {
    final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
    final ListBufferTransitionRelation resultRel = tr.getTransitionRelation();
    for (final AbstractEFAEvent event : tr.getUsedEvents()) {
      final EventInfo localInfo = mEventInfoMap.get(event);
      if (localInfo.isLocal()) {
        final int code = encoding.getEventId(event);
        final byte status = resultRel.getProperEventStatus(code);
        resultRel.setProperEventStatus(code, status | EventEncoding.STATUS_LOCAL);
      }
    }
    final UnifiedEFATransitionRelation simplifiedTR = mSimplifier.run(tr);
    if (simplifiedTR != null) {
      registerTR(simplifiedTR);
      unregisterTR(tr);
    }
  }

  private void registerTR(final UnifiedEFATransitionRelation tr)
  {
    for (final AbstractEFAEvent event : tr.getUsedEvents()) {
      EventInfo info = mEventInfoMap.get(event);
      if (info == null) {
        info = new EventInfo(event);
        mEventInfoMap.put(event, info);
        final AbstractEFAEvent originalEvent = event.getOriginalEvent();
        if (originalEvent != null) {
          final EventInfo originalInfo = mEventInfoMap.get(originalEvent);
          originalInfo.addChild(info);
        }
      }
      info.addTransitionRelation(tr);
    }
    mTransitionRelations.add(tr);
  }

  private void unregisterVariable(final UnifiedEFAVariable var)
  {
    final VariableInfo varInfo = mVariableInfoMap.remove(var);
    final List<EventInfo> originalInfo = varInfo.getEvents();
    for (final EventInfo eventInfo : originalInfo) {
      eventInfo.removeVariable(var);
      removeEmptyEventInfo(eventInfo);
    }
  }

  private void unregisterTR(final UnifiedEFATransitionRelation tr)
  {
    for (final AbstractEFAEvent event : tr.getUsedEvents()) {
      final EventInfo info = mEventInfoMap.get(event);
      info.removeTransitionRelation(tr);
      removeEmptyEventInfo(info);
    }
    mTransitionRelations.remove(tr);
  }

  private void removeEmptyEventInfo(final EventInfo info)
  {
    if (info.isEmpty()) {
      final AbstractEFAEvent event = info.getEvent();
      mEventInfoMap.remove(event);
      AbstractEFAEvent originalEvent = event.getOriginalEvent();
      while (originalEvent != null) {
        final EventInfo originalInfo = mEventInfoMap.get(originalEvent);
        if (originalInfo != null) {
          originalInfo.removeChild(info);
          break;
        }
        originalEvent = originalEvent.getOriginalEvent();
      }
    }
  }

  @SuppressWarnings("unused")
  private void splitSubsystems()
  {

  }

  @SuppressWarnings("unused")
  private void collectSybsystem(final EventInfo startInfo)
  {
    final BFSSearchSpace<EventInfo> search = new BFSSearchSpace<>();
    search.add(startInfo);
    while (!search.isEmpty()) {
      final EventInfo info = search.remove();
      final List<UnifiedEFATransitionRelation> trs = info.getTransitionRelations();
      for (final UnifiedEFATransitionRelation tr : trs) {
        final Set<AbstractEFAEvent> events = tr.getUsedEvents();
        for (final AbstractEFAEvent nextEvent : events) {
          AbstractEFAEvent originalEvent = nextEvent.getOriginalEvent();
          while (originalEvent != null) {
            originalEvent = originalEvent.getOriginalEvent();
          }
          search.add(mEventInfoMap.get(originalEvent));
        }
      }
    }
  }

  //#########################################################################
  //# Inner Class Candidate
  private class Candidate implements Comparable<Candidate>
  {
    //#######################################################################
    //# Constructors
    private Candidate(final Set<UnifiedEFATransitionRelation> trs,
                      final Set<UnifiedEFAVariable> vars)
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

    private List<UnifiedEFAVariable> getVariables()
    {
      return mVariables;
    }

    private void addLocalEvent(final AbstractEFAEvent event)
    {
      mLocalEvents.add(event);
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
      final Set<AbstractEFAEvent> eventSet = new THashSet<>();
      double numStates = 1.0;
      for (final UnifiedEFAVariable var : mVariables) {
        numStates *= var.getRange().size();
        final VariableInfo info = mVariableInfoMap.get(var);
        final List<AbstractEFAEvent> events = info.getChildrenEvents();
        eventSet.addAll(events);
      }
      for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
        numStates *= tr.getTransitionRelation().getNumberOfReachableStates();
        eventSet.addAll(tr.getUsedEvents());
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
    //# Data Members
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private final List<UnifiedEFAVariable> mVariables;
    private final List<AbstractEFAEvent> mLocalEvents;
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
      mChildrenEvents = new ArrayList<UnifiedEFAConflictChecker.EventInfo>();
      mVariables = new THashSet<>();
      mVariableCollector.collectAllVariables(mEvent.getUpdate(), mVariables);
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

    private Set<UnifiedEFAVariable> getVariables()
    {
      return mVariables;
    }

    public void removeVariable(final UnifiedEFAVariable selectedVar)
    {
      mVariables.remove(selectedVar);
    }

    private List<EventInfo> getChildren()
    {
      return mChildrenEvents;
    }

    private void addChild(final EventInfo event)
    {
      mChildrenEvents.add(event);
    }

    private void removeChild(final EventInfo event)
    {
      mChildrenEvents.remove(event);
      mChildrenEvents.addAll(event.getChildren());
    }

    private void collectLeaves(final List<AbstractEFAEvent> leaves)
    {
      if (mChildrenEvents.isEmpty()) {
        leaves.add(mEvent);
      } else {
        for (final EventInfo info : mChildrenEvents) {
          info.collectLeaves(leaves);
        }
      }
    }

    /**
     * Collects data for a candidate with this event. All transition
     * relations and variables associated with this event and all its
     * descendants are added to the sets trs and vars, respectively.
     */
    private void collectCandidate(final Set<UnifiedEFATransitionRelation> trs,
                                  final Set<UnifiedEFAVariable> vars)
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
        AbstractEFAEvent original = mEvent.getOriginalEvent();
        while (original != null) {
          final EventInfo info = mEventInfoMap.get(original);
          if (info != null) {
            return false;
          }
          original = original.getOriginalEvent();
        }
        return true;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mEvent.getName();
    }

    //#######################################################################
    //# Data Members
    private final AbstractEFAEvent mEvent;
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private final Set<UnifiedEFAVariable> mVariables;
    private final List<EventInfo> mChildrenEvents;
  }


  //#########################################################################
  //# Inner Class VariableInfo
  private static class VariableInfo
  {
    //#######################################################################
    //# Constructor
    private VariableInfo()
    {
      mEventInfo = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private List<AbstractEFAEvent> getChildrenEvents()
    {
      final List<AbstractEFAEvent> result = new ArrayList<>();
      for (final EventInfo info : mEventInfo) {
        info.collectLeaves(result);
      }
      return result;
    }

    private List<EventInfo> getEvents()
    {
      return mEventInfo;
    }

    private void addEvent(final EventInfo event)
    {
      mEventInfo.add(event);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mEventInfo.toString();
    }

    //#######################################################################
    //# Data Members
    private final List<EventInfo> mEventInfo;
  }


  //#########################################################################
  //# Data Members
  private CompilerOperatorTable mCompilerOperatorTable;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;

  private DocumentManager mDocumentManager;
  private UnifiedEFAVariableCollector mVariableCollector;
  private UnifiedEFASynchronousProductBuilder mEFASynchronizer;
  private UnifiedEFASimplifier mSimplifier;
  private UnifiedEFAVariableUnfolder mUnfolder;

  private UnifiedEFASystem mCurrentEFASystem;
  private Map<AbstractEFAEvent, EventInfo> mEventInfoMap;
  private Map<UnifiedEFAVariable, VariableInfo> mVariableInfoMap;
  private Map<Candidate, Candidate> mCandidateMap;
  private Set<UnifiedEFATransitionRelation> mTransitionRelations;
  private EFANonblockingChecker mNonblockingChecker;

}
