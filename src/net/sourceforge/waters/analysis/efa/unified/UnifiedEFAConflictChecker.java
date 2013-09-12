//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
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

  public UnifiedEFAConflictChecker(final ModuleProxy model, final IdentifierProxy marking,
                             final ModuleProxyFactory factory)
  {
    super(model, marking, factory);
  }


  //#########################################################################
  //# Simple Access
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager document)
  {
    mDocumentManager = document;
  }

  public CompilerOperatorTable getCompilerOperatorTable()
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

  public CompilerOperatorTable getOperatorTable()
  {
    return mCompilerOperatorTable;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
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
  }

  @Override
  public void tearDown()
  {
    mCompilerOperatorTable = null;
    mDocumentManager = null;

    mEFASynchronizer = null;

    mCurrentEFASystem = null;

    super.tearDown();
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
      createCandidates();
      @SuppressWarnings("unused")
      final Candidate minCandidate = Collections.min(mCandidateMap.keySet());
      return false;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createEventInfo()
  {
    final List<UnifiedEFATransitionRelation> trs =
      mCurrentEFASystem.getTransitionRelations();
    final List<AbstractEFAEvent> events = mCurrentEFASystem.getEvents();
    mEventInfoMap = new HashMap<>(events.size());
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = new EventInfo(event);
      mEventInfoMap.put(event, info);
    }
    for (final UnifiedEFATransitionRelation tr : trs) {
      final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      final int eventnum = rel.getNumberOfProperEvents();
      for (int e= EventEncoding.NONTAU; e<eventnum; e++) {
        final AbstractEFAEvent event = encoding.getEvent(e);
        final EventInfo info =  mEventInfoMap.get(event);
        info.addTransitionRelation(tr);
      }
    }
  }

  private void createCandidates()
  {
    final List<AbstractEFAEvent> events = mCurrentEFASystem.getEvents();
    for (final AbstractEFAEvent event : events) {
      final EventInfo info = mEventInfoMap.get(event);
      final List<UnifiedEFATransitionRelation> trs = info.getTransitionRelations();
      final List<UnifiedEFAVariable> vars = info.getVariables();
      Candidate candidate = new Candidate(trs, vars);
      final Candidate existing = mCandidateMap.get(candidate);
      if (existing != null) {
        mCandidateMap.put(candidate, candidate);
      } else {
        candidate = existing;
      }
      candidate.addLocalEvent(event);
    }
  }

  @SuppressWarnings("unused")
  private void createVariableInfo()
  {
    final List<UnifiedEFAVariable> vars = mCurrentEFASystem.getVariables();
    mVariableInfoMap = new HashMap<>(vars.size());
    for (final UnifiedEFAVariable var : vars) {
      final VariableInfo info = new VariableInfo();
      mVariableInfoMap.put(var, info);
    }
    for (final EventInfo eventInfo : mEventInfoMap.values()) {
      final AbstractEFAEvent event = eventInfo.getEvent();
      for (final UnifiedEFAVariable var : eventInfo.getVariables()){
        final VariableInfo varInfo = mVariableInfoMap.get(var);
        varInfo.addEvent(event);
      }
    }
  }

  @SuppressWarnings("unused")
  private void applyCandidate(final Candidate candidate)
    throws AnalysisException, EvalException
  {
    int smallest = 0;
    UnifiedEFAVariable selectedVar = null;
    final List<UnifiedEFAVariable> vars = candidate.getVariables();
    if (!vars.isEmpty()) {
      for (final UnifiedEFAVariable var: vars) {
        final int size = var.getRange().size();
        if (smallest > size) {
          smallest = size;
          selectedVar = var;
        }
      }
    }
    mUnfolder = new UnifiedEFAVariableUnfolder
      (getFactory(), mCompilerOperatorTable, mCurrentEFASystem.getVariableContext());
    mUnfolder.setUnfoldedVariable(selectedVar);
    final VariableInfo varInfo = mVariableInfoMap.get(selectedVar);
    final List<AbstractEFAEvent> originalEvents = varInfo.getEvents();
    mUnfolder.setOriginalEvents(originalEvents);
    mUnfolder.run();
    final UnifiedEFATransitionRelation unfoldedTR =
      mUnfolder.getTransitionRelation();
    registerTR(unfoldedTR);
    unregisterVariable(selectedVar);
    //TODO hide
    final UnifiedEFATransitionRelation simplifiedTR = mSimplifier.run(unfoldedTR);
  }

  private void registerTR(final UnifiedEFATransitionRelation tr)
  {
    final List<AbstractEFAEvent> events = tr.getUsedEvents();
    for (final AbstractEFAEvent event : events) {
      EventInfo info = mEventInfoMap.get(event);
      if (info == null) {
        info = new EventInfo(event);
        mEventInfoMap.put(event, info);
        final AbstractEFAEvent originalEvent = event.getOriginalEvent();
        if (originalEvent != null) {
          final EventInfo originalInfo = mEventInfoMap.get(originalEvent);
          originalInfo.addChild();
        }
      }
      info.addTransitionRelation(tr);
    }
  }

  private void unregisterVariable(final UnifiedEFAVariable selectedVar)
  {
    mVariableInfoMap.remove(selectedVar);
    final VariableInfo varInfo = mVariableInfoMap.get(selectedVar);
    final List<AbstractEFAEvent> originalEvents = varInfo.getEvents();
    for (final AbstractEFAEvent event : originalEvents) {
      final EventInfo info = mEventInfoMap.get(event);
      info.removeVariable(selectedVar);
      removeEmptyEventInfo(info);
    }
  }

  @SuppressWarnings("unused")
  private void unregisterTR(final UnifiedEFATransitionRelation tr)
  {
    final List<AbstractEFAEvent> originalEvents = tr.getUsedEvents();
    for (final AbstractEFAEvent event : originalEvents) {
      final EventInfo info = mEventInfoMap.get(event);
      info.removeTransitionRelation(tr);
      removeEmptyEventInfo(info);
    }
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
          final int numChildren = info.getNumberOfChildren();
          originalInfo.addChildren(numChildren - 1);
          break;
        }
        originalEvent = originalEvent.getOriginalEvent();
      }
    }
  }


  //#########################################################################
  //# Inner Class
  private class Candidate implements Comparable<Candidate>
  {
    private Candidate(final List<UnifiedEFATransitionRelation> trs,
                      final List<UnifiedEFAVariable> vars)
    {
      mTransitionRelations = new ArrayList<>(trs);
      Collections.sort(mTransitionRelations);
      mVariables = new ArrayList<>(vars);
      Collections.sort(mVariables);
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

    private double getEstimateSize()
    {
      if (mEstimateSize < 0) {
        mEstimateSize = calculateMinS();
      }
      return mEstimateSize;
    }

    private double calculateMinS()
    {
      final Set<AbstractEFAEvent> eventSet = new THashSet<>();
      int numStates = 1;
      for (final UnifiedEFAVariable var : mVariables) {
        numStates *= var.getRange().size();
        final VariableInfo info = mVariableInfoMap.get(var);
        final List<AbstractEFAEvent> events = info.getEvents();
        eventSet.addAll(events);
      }
      for (final UnifiedEFATransitionRelation tr : mTransitionRelations) {
        numStates *= tr.getTransitionRelation().getNumberOfReachableStates();
        eventSet.addAll(tr.getUsedEvents());
      }
      return (1-((double)mLocalEvents.size())/eventSet.size())*numStates;
    }

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
        if (candidate.getTransitionRelations().equals(getTransitionRelations())
          && candidate.getVariables().equals(getVariables())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }

    @Override
    public int compareTo(final Candidate candidate)
    {
      if (candidate.getEstimateSize() < getEstimateSize()) {
        return -1;
      } else if (candidate.getEstimateSize() > getEstimateSize()) {
        return 1;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Data Members
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private List<AbstractEFAEvent> mLocalEvents;
    private final List<UnifiedEFAVariable> mVariables;
    private double mEstimateSize = -1;
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
      mVariables = new ArrayList<>();
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

    private List<UnifiedEFAVariable> getVariables()
    {
      return mVariables;
    }

    private void addTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.add(tr);
    }

    private void removeTransitionRelation(final UnifiedEFATransitionRelation tr)
    {
      mTransitionRelations.remove(tr);
    }

    public void removeVariable(final UnifiedEFAVariable selectedVar)
    {
      mVariables.remove(selectedVar);
    }

    private void addChild()
    {
      mNumberOfChildren++;
    }


    private void addChildren(final int children)
    {
      mNumberOfChildren += children;
    }

    private int getNumberOfChildren()
    {
      return mNumberOfChildren;
    }

    private boolean isEmpty()
    {
      if (mTransitionRelations.isEmpty() && mVariables.isEmpty()) {
        return true;
      } else {
        return false;
      }
    }

    @SuppressWarnings("unused")
    private boolean isLocal()
    {
      if (mNumberOfChildren != 0) {
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
    //# Data Members
    private final AbstractEFAEvent mEvent;
    private final List<UnifiedEFATransitionRelation> mTransitionRelations;
    private final List<UnifiedEFAVariable> mVariables;
    private int mNumberOfChildren = 0;
  }

  //#########################################################################
  //# Inner Class VariableInfo
  private class VariableInfo
  {
    //#######################################################################
    //# Constructor
    private VariableInfo()
    {
      mEvents = new ArrayList<>();
    }

    //#######################################################################
    //# Simple Access
    private List<AbstractEFAEvent> getEvents()
    {
      return mEvents;
    }

    private void addEvent(final AbstractEFAEvent event)
    {
      mEvents.add(event);
    }

    //#########################################################################
    //# Data Members
    private final List<AbstractEFAEvent> mEvents;
  }
  //#########################################################################
  //# Data Members
  private CompilerOperatorTable mCompilerOperatorTable;
  private int mInternalTransitionLimit = Integer.MAX_VALUE;

  private DocumentManager mDocumentManager;
  private UnifiedEFAVariableCollector mVariableCollector;
  @SuppressWarnings("unused")
  private UnifiedEFASynchronousProductBuilder mEFASynchronizer;
  private UnifiedEFAVariableUnfolder mUnfolder;

  private UnifiedEFASystem mCurrentEFASystem;
  private Map<AbstractEFAEvent, EventInfo> mEventInfoMap;
  private Map<UnifiedEFAVariable, VariableInfo> mVariableInfoMap;
  private Map<Candidate, Candidate> mCandidateMap;
  private UnifiedEFASimplifier mSimplifier;

}
