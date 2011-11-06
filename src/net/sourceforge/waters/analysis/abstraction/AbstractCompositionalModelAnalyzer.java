//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AbstractCompositionalModelVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.THashSet;
import gnu.trove.TObjectByteHashMap;
import gnu.trove.TObjectByteIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.
  MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.AutomatonResult;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SynchronousProductStateMap;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * <P>A general compositional model analyser to be subclassed for different
 * algorithms.</P>
 *
 * <P>This model analyser implements compositional minimisation
 * of the input model, and leaves it to the subclasses to decide what is
 * to be done with the minimisation result. It provides a variety of
 * candidate selection heuristics, which can be configured by the user.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying Generalised
 * Nonblocking, Proc. 7th International Conference on Control and Automation,
 * ICCA'09, 448-453, Christchurch, New Zealand, 2009.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractCompositionalModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    this(factory, translator,
         new PreselectingMethodFactory(), new SelectingMethodFactory());
  }

  /**
   * Creates an abstracting model verifier without a model.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    this(null, factory, translator,
         preselectingMethodFactory, selectingMethodFactory);
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    this(model, factory, translator,
         new PreselectingMethodFactory(), new SelectingMethodFactory());
  }

  /**
   * Creates an abstracting model verifier to check the given model.
   * @param model
   *          The model to be checked by this model verifier.
   * @param factory
   *          Factory used for trace construction.
   * @param translator
   *          Kind translator used to determine event and component kinds.
   * @param preselectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          preselection methods.
   * @param selectingMethodFactory
   *          Enumeration factory that determines possible candidate
   *          selection methods.
   */
  protected AbstractCompositionalModelAnalyzer
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final PreselectingMethodFactory preselectingMethodFactory,
     final SelectingMethodFactory selectingMethodFactory)
  {
    super(model, factory, translator);
    mPreselectingMethodFactory = preselectingMethodFactory;
    mPreselectingMethod = MustL;
    mSelectingMethodFactory = selectingMethodFactory;
    mSelectingMethod = MinS;
    mSubsumptionEnabled = false;
    mLowerInternalStateLimit = mUpperInternalStateLimit =
      super.getNodeLimit();
    mInternalTransitionLimit = super.getTransitionLimit();
  }


  //#########################################################################
  //# Configuration
  /**
   * Gets the enumeration factory that provides the possible preselection
   * methods.
   * @see PreselectingMethod
   */
  public PreselectingMethodFactory getPreselectingMethodFactory()
  {
    return mPreselectingMethodFactory;
  }

  /**
   * Sets the preselecting heuristics used to choose candidates.
   * Possible heuristics are available as static instances of the
   * {@link AbstractCompositionalModelAnalyzer} class, or can be
   * obtained from the verifier's {@link PreselectingMethodFactory}.
   *
   * @see #MustL
   * @see #MinT
   * @see #MaxS
   * @see #getPreselectingMethodFactory()
   */
  public void setPreselectingMethod(final PreselectingMethod method)
  {
    mPreselectingMethod = method;
  }

  /**
   * Gets the preselecting heuristics used to choose candidates.
   * @see #setPreselectingMethod(PreselectingMethod) setPreselectingMethod()
   */
  public PreselectingMethod getPreselectingMethod()
  {
    return mPreselectingMethod;
  }

  /**
   * Gets the enumeration factory that provides the possible selection
   * methods.
   * @see SelectingMethod
   */
  public SelectingMethodFactory getSelectingMethodFactory()
  {
    return mSelectingMethodFactory;
  }

  /**
   * Sets the selecting heuristics to be used to choose candidates.
   * Possible heuristics are available as static instances of the
   * {@link AbstractCompositionalModelAnalyzer} class, or can be
   * obtained from the verifier's {@link SelectingMethodFactory}.
   *
   * @see #MaxC
   * @see #MaxL
   * @see #MinE
   * @see #MinS
   * @see #MinSync
   * @see #getSelectingMethodFactory()
   * @see SelectingMethod
   */
  public void setSelectingMethod(final SelectingMethod method)
  {
    mSelectingMethod = method;
  }

  /**
   * Gets the selecting heuristics used to choose candidates.
   * @see #setSelectingMethod(SelectingMethod) setSelectingMethod()
   */
  public SelectingMethod getSelectingMethod()
  {
    return mSelectingMethod;
  }

  /**
   * Returns whether subsumption is enabled in the selecting heuristic.
   * @see #setSubumptionEnabled(boolean)
   * @see SelectingMethod
   */
  public boolean isSubsumptionEnabled()
  {
    return mSubsumptionEnabled;
  }

  /**
   * Sets whether subsumption is enabled in the selecting heuristic.
   * If subsumption is enabled, and the heuristic returns a candidate that
   * is subsumed by another candidate, the a new candidate will be selected
   * from the list of all preselected candidates that subsume the originally
   * selected candidate. The selection heuristics will be used again to
   * resolve ties.
   * @see PreselectingMethod
   */
  public void setSubumptionEnabled(final boolean enable)
  {
    mSubsumptionEnabled = enable;
  }


  @Override
  public int getNodeLimit()
  {
    final int limit1 = getInternalStateLimit();
    final int limit2 = getMonolithicStateLimit();
    return Math.max(limit1, limit2);
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    setInternalStateLimit(limit);
    setMonolithicStateLimit(limit);
  }

  public int getInternalStateLimit()
  {
    return Math.max(mLowerInternalStateLimit, mUpperInternalStateLimit);
  }

  public void setInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = mUpperInternalStateLimit = limit;
  }

  public int getLowerInternalStateLimit()
  {
    return mLowerInternalStateLimit;
  }

  public void setLowerInternalStateLimit(final int limit)
  {
    mLowerInternalStateLimit = limit;
  }

  public int getUpperInternalStateLimit()
  {
    return mUpperInternalStateLimit;
  }

  public void setUpperInternalStateLimit(final int limit)
  {
    mUpperInternalStateLimit = limit;
  }

  public int getMonolithicStateLimit()
  {
    return super.getNodeLimit();
  }

  public void setMonolithicStateLimit(final int limit)
  {
    super.setNodeLimit(limit);
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
    setInternalTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    final int limit1 = getInternalTransitionLimit();
    final int limit2 = getMonolithicTransitionLimit();
    return Math.max(limit1, limit2);
  }

  public int getMonolithicTransitionLimit()
  {
    return super.getTransitionLimit();
  }

  public void setMonolithicTransitionLimit(final int limit)
  {
    super.setTransitionLimit(limit);
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
  //# Specific Access
  protected void setAbstractionProcedure(final AbstractionProcedure proc)
  {
    mAbstractionProcedure = proc;
  }

  protected AbstractionProcedure getAbstractionProcedure()
  {
    return mAbstractionProcedure;
  }

  protected MonolithicSynchronousProductBuilder
    getCurrentSynchronousProductBuilder()
  {
    return mCurrentSynchronousProductBuilder;
  }

  protected void setPropositions(final Collection<EventProxy> props)
  {
    mPropositions = props;
  }

  protected Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }

  protected List<AutomatonProxy> getCurrentAutomata()
  {
    return mCurrentAutomata;
  }

  protected Collection<EventProxy> getCurrentEvents()
  {
    return mEventInfoMap.keySet();
  }

  protected Collection<SubSystem> getPostponedSubsystems()
  {
    return mPostponedSubsystems;
  }

  protected Collection<SubSystem> getProcessedSubsystems()
  {
    return mProcessedSubsystems;
  }

  protected int getCurrentInternalStateLimit()
  {
    return mCurrentInternalStateLimit;
  }


  protected void setupSynchronousProductBuilder()
  {
    if (mCurrentSynchronousProductBuilder == null) {
      if (mSynchronousProductBuilder == null) {
        final ProductDESProxyFactory factory = getFactory();
        mCurrentSynchronousProductBuilder =
          new MonolithicSynchronousProductBuilder(factory);
      } else {
        mCurrentSynchronousProductBuilder = mSynchronousProductBuilder;
      }
      mCurrentSynchronousProductBuilder.setPropositions(mPropositions);
      final KindTranslator translator = getKindTranslator();
      mCurrentSynchronousProductBuilder.setKindTranslator(translator);
      final int tlimit = getInternalTransitionLimit();
      mCurrentSynchronousProductBuilder.setTransitionLimit(tlimit);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mAbstractionProcedure != null) {
      mAbstractionProcedure.requestAbort();
    }
    if (mCurrentSynchronousProductBuilder != null) {
      mCurrentSynchronousProductBuilder.requestAbort();
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.AbstractModelAnalyser
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setNumberOfStates(0.0);
    result.setNumberOfTransitions(0.0);
    mAbstractionProcedure.storeStatistics();
    mPreselectingHeuristic = mPreselectingMethod.createHeuristic(this);
    mSelectingHeuristic = mSelectingMethod.createHeuristic(this);
    setupSynchronousProductBuilder();
    mOverflowCandidates = new THashSet<List<AutomatonProxy>>();
    mCurrentInternalStateLimit = mLowerInternalStateLimit;
    initialiseEventsToAutomata();
    simplify(true);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mPropositions = null;
    mAbstractionProcedure = null;
    mPreselectingHeuristic = null;
    mSelectingHeuristic = null;
    mCurrentSynchronousProductBuilder = null;
    mCurrentAutomata = null;
    mEventInfoMap = null;
    mDirtyAutomata = null;
    mRedundantEvents = null;
    mPostponedSubsystems = null;
    mProcessedSubsystems = null;
    mUsedEventNames = null;
    mOverflowCandidates = null;
  }

  @Override
  protected CompositionalAnalysisResult createAnalysisResult()
  {
    return new CompositionalAnalysisResult();
  }

  @Override
  public CompositionalAnalysisResult getAnalysisResult()
  {
    return (CompositionalAnalysisResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Hooks
  /**
   * Performs compositional minimisation of the model.
   * This method should be called as part of the {@link #run()} method of
   * subclasses extending this class. It performs compositional minimisation
   * of the set of automata in the input model and calls
   * {@link #doMonolithicAnalysis(List) doMonolithicAnalysis()} for each
   * subsystem that cannot be minimised further. It stops when one of these
   * calls sets the analysis result to be completed, or when no further
   * minimisation is possible. At this point, the list returned by
   * {@link #getCurrentAutomata()} contains the automata of the minimised
   * system. This method does not take steps to put further data
   * (such as counterexamples) into the analysis result.
   */
  protected void runCompositionalMinimisation()
    throws AnalysisException
  {
    simplify(true);
    boolean cancheck = true;
    OverflowException lastOverflow = null;
    final CompositionalAnalysisResult result = getAnalysisResult();
    Collection<Candidate> candidates;
    Candidate candidate = null;
    outer:
      do {
        subsystem:
          do {
            if (isSubsystemTrivial(mCurrentAutomata)) {
              if (result.isFinished()) {
                break outer;
              } else {
                continue outer;
              }
            }
            candidates = mPreselectingHeuristic.findCandidates();
            candidate = selectCandidate(candidates);
            while (candidate != null) {
              try {
                mEventHasDisappeared = false;
                applyCandidate(candidate);
                simplify(mEventHasDisappeared);
                cancheck = true;
                continue subsystem;
              } catch (final OutOfMemoryError error) {
                getLogger().debug("<out of memory>");
                // caught - go on ...
              } catch (final OverflowException overflow) {
                // caught - go on ...
              }
              recordUnsuccessfulComposition();
              final List<AutomatonProxy> automata = candidate.getAutomata();
              mOverflowCandidates.add(automata);
              candidates.remove(candidate);
              candidate = selectCandidate(candidates);
            }
          } while (candidate != null);
      try {
        if (cancheck) {
          doMonolithicAnalysis(mCurrentAutomata);
          lastOverflow = null;
        }
      } catch (final OutOfMemoryError error) {
        getLogger().debug("<out of memory>");
        lastOverflow = new OverflowException(error);
        cancheck = false;
      } catch (final OverflowException overflow) {
        lastOverflow = overflow;
        cancheck = false;
      }
      if (lastOverflow != null) {
        if (mCurrentInternalStateLimit < mUpperInternalStateLimit) {
          mCurrentInternalStateLimit =
            Math.min(2 * mCurrentInternalStateLimit, mUpperInternalStateLimit);
          mOverflowCandidates.clear();
          final Logger logger = getLogger();
          if (logger.isDebugEnabled()) {
            final String msg =
              "State limit increased to " + mCurrentInternalStateLimit + ".";
            logger.debug(msg);
          }
        } else {
          throw lastOverflow;
        }
      }
      } while (lastOverflow != null ||
        !result.isFinished() && popEventDisjointSubsystem());

    if (!result.isFinished()) {
      final List<AutomatonProxy> empty = Collections.emptyList();
      doMonolithicAnalysis(empty);
    }
    restoreAutomata();
  }

  /**
   * Creates a product DES for an abstraction step. This hook is
   * invoked before composing the automata of a selected candidate or before
   * running a final monolithic check. It may be overridden by specialised
   * property verifiers that modify the set of automata prior to composition.
   * @param  automata     List of automata for monolithic model.
   * @return A product DES to be passed to a synchronous product builder or
   *         monolithic verifier.
   */
  protected ProductDESProxy createProductDESProxy
    (final List<AutomatonProxy> automata)
  {
    final ProductDESProxyFactory factory = getFactory();
    final String name = Candidate.getCompositionName(automata);
    final String comment =
      "Automatically generated by " + ProxyTools.getShortClassName(this);
    final Collection<EventProxy> events = Candidate.getAllEvents(automata);
    return factory.createProductDESProxy(name, comment, null,
                                         events, automata);
  }

  /**
   * Records an abstraction step.
   * This hook is called when an abstraction has been completed with success
   * to allow subclasses to perform necessary bookkeeping. The default
   * implementation does not nothing, as not all model analyser may need
   * to record all abstraction steps.
   * @param  step         Detailed information about the abstraction that
   *                      has been performed, including all automata before
   *                      and after the step.
   */
  protected void recordAbstractionStep(final AbstractionStep step)
    throws AnalysisException
  {
  }

  /**
   * Removes the given events from the model.
   * This method is called when redundant events have been identified to
   * remove them.
   * @return An abstraction step representing the event removal, or
   *         <CODE>null</CODE> to signal that no events can be removed
   *         after all.
   * @see #removeRedundantEvents()
   */
  protected AbstractionStep removeEvents(final Collection<EventProxy> removed)
    throws OverflowException
  {
    if (removed.isEmpty()) {
      return null;
    } else {
      final Set<EventProxy> removedSet = new THashSet<EventProxy>(removed);
      final ProductDESProxyFactory factory = getFactory();
      final int numAutomata = mCurrentAutomata.size();
      final List<AutomatonProxy> originals =
        new ArrayList<AutomatonProxy>(numAutomata);
      final List<AutomatonProxy> results =
        new ArrayList<AutomatonProxy>(numAutomata);
      final ListIterator<AutomatonProxy> iter =
        mCurrentAutomata.listIterator();
      while (iter.hasNext()) {
        final AutomatonProxy aut = iter.next();
        final Collection<EventProxy> events = aut.getEvents();
        boolean found = false;
        for (final EventProxy event : events) {
          if (removedSet.contains(event)) {
            found = true;
            break;
          }
        }
        if (!found) {
          continue;
        }
        final int numEvents = events.size();
        final Collection<EventProxy> newEvents =
          new ArrayList<EventProxy>(numEvents - 1);
        for (final EventProxy event : events) {
          if (!removedSet.contains(event)) {
            newEvents.add(event);
          }
        }
        final Collection<TransitionProxy> transitions = aut.getTransitions();
        final int numTrans = transitions.size();
        final Collection<TransitionProxy> newTransitions =
          new ArrayList<TransitionProxy>(numTrans);
        boolean dirty = false;
        for (final TransitionProxy trans : transitions) {
          final EventProxy event = trans.getEvent();
          if (!removedSet.contains(event)) {
            newTransitions.add(trans);
          } else if (trans.getSource() != trans.getTarget()) {
            dirty = true;
          }
        }
        final String name = aut.getName();
        final ComponentKind kind = aut.getKind();
        final Collection<StateProxy> states = aut.getStates();
        final AutomatonProxy newAut = factory.createAutomatonProxy
          (name, kind, newEvents, states, newTransitions);
        originals.add(aut);
        results.add(newAut);
        iter.set(newAut);
        for (final EventProxy event : newEvents) {
          final EventInfo info = mEventInfoMap.get(event);
          if (info != null) {
            info.replaceAutomaton(aut, newAut);
          }
        }
        if (dirty) {
          mDirtyAutomata.add(newAut);
        }
      }
      final CompositionalAnalysisResult stats = getAnalysisResult();
      final int numRemoved = removed.size();
      stats.addRedundantEvents(numRemoved);
      return new EventRemovalStep(results, originals);
    }
  }

  /**
   * Checks whether the given automata form a trivial subsystem.
   * This hook is called before evaluating heuristics and choosing the
   * next candidate. It checks whether it can be determined directly
   * whether or not the current subsystem subsystem satisfies the property
   * in question, and whether this already leads to a result for the
   * global system.
   * @param  automata     The automata in the current subsystem, which are
   *                      to be checked by this method.
   * @return <CODE>true</CODE> if the subsystem can be determined to satisfy
   *         or not to satisfy the property being checked. If <CODE>true</CODE>
   *         is returned, the verification result is updated to contain the
   *         correct result and a counterexample for the abstracted model,
   *         if the property being checked is not satisfied.
   */
  protected boolean isSubsystemTrivial
    (final Collection<AutomatonProxy> automata)
  throws AnalysisException
  {
    return false;
  }

  /**
   * Uses a primitive (typically monolithic) algorithm to perform analysis
   * of the given automata. This hook is called when a subsystem has been
   * minimised as much as possible, for it to be processed by other means.
   * It may be called more than once if the model being analysed is split
   * into event-disjoint subsystems.
   * @param  automata   List of automata comprising the subsystem to be
   *                    analysed monolithically.
   * @return The Boolean result of analysis. If analysis of the subsystem
   *         leads to a global result for the entire system, the analysis
   *         result should be set to completed and appropriate data should
   *         be stored in it. If the analysis result is not set to completed,
   *         compositional minimisation may resume considering other
   *         subsystems.
   */
  protected abstract boolean doMonolithicAnalysis
    (final List<AutomatonProxy> automata)
    throws AnalysisException;


  //#########################################################################
  //# Events+Automata Maps
  /**
   * Maps the events in the model to a set of the automata that contain the
   * event in their alphabet.
   */
  protected void initialiseEventsToAutomata()
    throws OverflowException
  {
    final ProductDESProxy model = getModel();
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    final KindTranslator translator = getKindTranslator();
    mCurrentAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    final int numEvents = model.getEvents().size();
    mEventInfoMap = new HashMap<EventProxy,EventInfo>(numEvents);
    mDirtyAutomata = new LinkedList<AutomatonProxy>();
    for (final AutomatonProxy aut : automata) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        mCurrentAutomata.add(aut);
        addEventsToAutomata(aut);
        mDirtyAutomata.add(aut);
      }
    }
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.setNumberOfAutomata(mCurrentAutomata.size());
    mUsedEventNames = new THashSet<String>(numEvents + numAutomata);
    for (final EventProxy event : mEventInfoMap.keySet()) {
      final String name = event.getName();
      mUsedEventNames.add(name);
    }
    mRedundantEvents = new LinkedList<EventProxy>();
    for (final Map.Entry<EventProxy,EventInfo> entry :
         mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.isRemovable()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
    mPostponedSubsystems = new PriorityQueue<SubSystem>();
    mProcessedSubsystems = new LinkedList<SubSystem>();
  }

  protected void updateEventsToAutomata
    (final AutomatonProxy autToAdd,
     final List<AutomatonProxy> autToRemove)
  {
    mCurrentAutomata.removeAll(autToRemove);
    mCurrentAutomata.add(autToAdd);
    addEventsToAutomata(autToAdd);
    removeEventsToAutomata(autToRemove);
  }

  /**
   * Creates an event information record for the given event.
   * @see EventInfo
   */
  protected EventInfo createEventInfo(final EventProxy event)
  {
    return new EventInfo(event);
  }

  protected EventInfo getEventInfo(final EventProxy event)
  {
    return mEventInfoMap.get(event);
  }

  protected void addEventsToAutomata(final AutomatonProxy aut)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final TObjectByteHashMap<EventProxy> statusMap =
      new TObjectByteHashMap<EventProxy>(numEvents);
    for (final TransitionProxy trans : aut.getTransitions()) {
      final EventProxy event = trans.getEvent();
      if (trans.getSource() != trans.getTarget()) {
        statusMap.put(event, NOT_ONLY_SELFLOOP);
      } else if (!statusMap.containsKey(event)) {
        statusMap.put(event, ONLY_SELFLOOP);
      }
    }
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : events) {
      if (translator.getEventKind(event) != EventKind.PROPOSITION) {
        EventInfo info = mEventInfoMap.get(event);
        if (info == null) {
          info = createEventInfo(event);
          if (!info.isLocal()) {
            continue;
          }
          mEventInfoMap.put(event, info);
        }
        final byte lookup = statusMap.get(event);
        final byte status = lookup == UNKNOWN_SELFLOOP ? BLOCKED : lookup;
        info.addAutomaton(aut, status);
      }
    }
  }

  /**
   * Removes the given automata from the current subsystem data structures.
   * This method is called after a candidate has been composed and simplified.
   * It removes entries from the event information map {@link #mEventInfoMap}),
   * and tests for events that become redundant. Events are redundant if
   * they are known to be globally disabled, or if they only ever appear as
   * selfloops. Such events are added to the list {@link #mRedundantEvents}.
   * @param victims
   *          Collection of automata to be removed.
   * @see #removeRedundantEvents()
   */
  protected void removeEventsToAutomata
    (final Collection<AutomatonProxy> victims)
  {
    mRedundantEvents.clear();
    final Iterator<Map.Entry<EventProxy,EventInfo>> iter =
      mEventInfoMap.entrySet().iterator();
    while (iter.hasNext()) {
      final Map.Entry<EventProxy,EventInfo> entry = iter.next();
      final EventInfo info = entry.getValue();
      info.removeAutomata(victims);
      if (info.isEmpty()) {
        iter.remove();
      } else if (info.isRemovable()) {
        final EventProxy event = entry.getKey();
        mRedundantEvents.add(event);
      }
    }
  }

  /**
   * Finds the set of events that are local to a candidate (i.e. a set of
   * automata).
   */
  private Set<EventProxy> identifyLocalEvents
    (final Collection<AutomatonProxy> candidate)
  {
    final Set<EventProxy> localEvents = new THashSet<EventProxy>();
    for (final Map.Entry<EventProxy,EventInfo> entry :
         mEventInfoMap.entrySet()) {
      final EventInfo info = entry.getValue();
      if (info.isLocal() && info.containedIn(candidate)) {
        final EventProxy event = entry.getKey();
        localEvents.add(event);
      }
    }
    return localEvents;
  }

  private boolean isPermissibleCandidate(final List<AutomatonProxy> automata)
  {
    return
      automata.size() < mCurrentAutomata.size() &&
      !mOverflowCandidates.contains(automata);
  }

  /**
   * Removes events that have been found to be redundant from the current
   * subsystem. This method removes any events contained in the list
   * {@link #mRedundantEvents} from all automata and records the abstraction
   * as an {@link EventRemovalStep}.
   * @return <CODE>true</CODE> if the model was modified, <CODE>false</CODE>
   *         otherwise.
   * @see #removeEventsToAutomata(Collection) removeEventsToAutomata()
   */
  private boolean removeRedundantEvents()
    throws AnalysisException
  {
    final AbstractionStep step = removeEvents(mRedundantEvents);
    if (step != null) {
      recordAbstractionStep(step);
      for (final EventProxy event : mRedundantEvents) {
        mEventInfoMap.remove(event);
      }
      mRedundantEvents.clear();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Checks whether the current subsystem can be split into event-disjoint
   * components, and if so, performs the split by replacing the current
   * subsystem by its smallest component and adding any split off subsystems
   * to the list {@link #mPostponedSubsystems}.
   */
  private boolean findEventDisjointSubsystems()
    throws AnalysisException
  {
    if (mEventInfoMap.isEmpty()) {
      return false;
    }
    final Collection<AutomatonProxy> remainingAutomata =
      new THashSet<AutomatonProxy>(mCurrentAutomata);
    final List<EventProxy> remainingEvents =
      new LinkedList<EventProxy>(getCurrentEvents());
    Collections.sort(remainingEvents);
    final List<SubSystem> tasks = new LinkedList<SubSystem>();
    while (!remainingEvents.isEmpty()) {
      final int numAutomata = remainingAutomata.size();
      final int numEvents = remainingEvents.size();
      final Iterator<EventProxy> iter1 = remainingEvents.iterator();
      final EventProxy event1 = iter1.next();
      iter1.remove();
      final List<EventProxy> subSystemEvents =
        new ArrayList<EventProxy>(numEvents);
      subSystemEvents.add(event1);
      final EventInfo info1 = mEventInfoMap.get(event1);
      final Collection<AutomatonProxy> subSystemAutomata =
        info1.getAutomataSet();
      if (subSystemAutomata.size() == numAutomata) {
        subSystemEvents.addAll(remainingEvents);
        remainingEvents.clear();
      } else {
        boolean change;
        do {
          change = false;
          final Iterator<EventProxy> iter = remainingEvents.iterator();
          while (iter.hasNext()) {
            final EventProxy event = iter.next();
            final EventInfo info = mEventInfoMap.get(event);
            if (info.intersects(subSystemAutomata)) {
              if (info.addAutomataTo(subSystemAutomata)) {
                if (subSystemAutomata.size() == numAutomata) {
                  subSystemEvents.addAll(remainingEvents);
                  remainingEvents.clear();
                  change = false;
                  break;
                }
                change = true;
              }
              iter.remove();
              subSystemEvents.add(event);
            }
          }
        } while (change);
      }
      if (subSystemAutomata.size() < numAutomata) {
        remainingAutomata.removeAll(subSystemAutomata);
      } else if (tasks.isEmpty()) {
        return false;
      } else {
        remainingAutomata.clear();
      }
      final List<AutomatonProxy> subSystemAutomataList =
        new ArrayList<AutomatonProxy>(subSystemAutomata);
      Collections.sort(subSystemAutomataList);
      final SubSystem task = new SubSystem(subSystemEvents,
                                           subSystemAutomataList,
                                           mCurrentInternalStateLimit);
      tasks.add(task);
    }
    for (final AutomatonProxy aut : remainingAutomata) {
      final SubSystem task = new SubSystem(aut, mCurrentInternalStateLimit);
      tasks.add(task);
    }
    final Iterator<SubSystem> iter = tasks.iterator();
    SubSystem task0 = iter.next();
    while (iter.hasNext()) {
      final SubSystem task = iter.next();
      if (task0.compareTo(task) < 0) {
        mPostponedSubsystems.add(task);
      } else {
        mPostponedSubsystems.add(task0);
        task0 = task;
      }
    }
    loadSubSystem(task0);
    return tasks.size() > 1;
  }

  private boolean popEventDisjointSubsystem()
  {
    final SubSystem next = mPostponedSubsystems.poll();
    if (next == null) {
      return false;
    } else {
      final List<EventProxy> events =
        new ArrayList<EventProxy>(mEventInfoMap.keySet());
      Collections.sort(events);
      final SubSystem current =
        new SubSystem(events, mCurrentAutomata, mCurrentInternalStateLimit);
      mProcessedSubsystems.add(current);
      loadSubSystem(next);
      return true;
    }
  }

  private void loadSubSystem(final SubSystem task)
  {
    mCurrentAutomata = task.getAutomata();
    mCurrentInternalStateLimit = task.getStateLimit();
    mEventInfoMap.clear();
    for (final AutomatonProxy aut : mCurrentAutomata) {
      addEventsToAutomata(aut);
    }
  }

  private void restoreAutomata()
  {
    for (final SubSystem task : mProcessedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
    for (final SubSystem task : mPostponedSubsystems) {
      final Collection<AutomatonProxy> automata = task.getAutomata();
      mCurrentAutomata.addAll(automata);
    }
  }


  //#########################################################################
  //# Candidate Selection
  /**
   * Performs the second step of candidate selection.
   * @param  preselected  List of preselected candidates from step&nbsp;1.
   * @return Preferred candidate from the given list, taking subsumption
   *         into account, or <CODE>null</CODE> if no suitable candidate
   *         could be found within the state limits.
   */
  private Candidate selectCandidate(final Collection<Candidate> preselected)
  throws AnalysisException
  {
    if (preselected.isEmpty()) {
      return null;
    } else {
      final Candidate result = mSelectingHeuristic.selectCandidate(preselected);
      if (mSubsumptionEnabled) {
        final Collection<Candidate> subsumedBy = new LinkedList<Candidate>();
        for (final Candidate candidate : preselected) {
          if (candidate.subsumes(result)) {
            subsumedBy.add(candidate);
          }
        }
        if (!subsumedBy.isEmpty()) {
          return selectCandidate(subsumedBy);
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Abstraction Steps
  /**
   * Attempts to simplify the current subsystem without composing automata.
   * This method attempts to simplify automata individually and to remove
   * redundant events. If events are removed, it also checks whether the
   * current subsystem can be split into event-disjoint components, and if
   * so, performs the split and replaces the current subsystem by its
   * smallest component.
   * @param  eventsChanged  A flag, indicating that the event alphabet has
   *                        been changed prior to the call, so the test for
   *                        event-disjoint subsystems is performed even if
   *                        no further simplification is possible.
   * @see #simplifyDirtyAutomata()
   * @see #removeRedundantEvents()
   * @see #findEventDisjointSubsystems()
   */
  private void simplify(final boolean eventsChanged)
    throws AnalysisException
  {
    final boolean change1 = simplifyDirtyAutomata();
    final boolean change2 = removeRedundantEvents();
    boolean change = change1 || change2;
    if (change || eventsChanged) {
      while (change) {
        change = simplifyDirtyAutomata() && removeRedundantEvents();
      }
      findEventDisjointSubsystems();
    }
  }

  /**
   * Simplifies any automata that have been marked as <I>dirty</I>.
   * This method checks all automata in the list {@link #mDirtyAutomata}
   * and applies the current abstraction rule to each of them.
   * @return <CODE>true</CODE> if some automaton was changed by abstraction,
   *         <CODE>false</CODE> otherwise.
   * @see #mDirtyAutomata
   */
  private boolean simplifyDirtyAutomata()
    throws AnalysisException
  {
    final AnalysisResult analysisResult = getAnalysisResult();
    boolean result = false;
    while (!mDirtyAutomata.isEmpty() && !analysisResult.isFinished()) {
      final AutomatonProxy aut = mDirtyAutomata.remove();
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      final Set<EventProxy> local = new THashSet<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        final EventInfo info = mEventInfoMap.get(event);
        if (info != null && info.getNumberOfAutomata() == 1) {
          local.add(event);
        }
      }
      final List<AutomatonProxy> singleton = Collections.singletonList(aut);
      final Candidate candidate = new Candidate(singleton, local);
      result |= applyCandidate(candidate);
    }
    return result;
  }

  /**
   * Applies the current abstraction rule to the given candidate.
   * @param  candidate   The candidate representing a set of automata to
   *                     be composed and simplified.
   * @return <CODE>true</CODE> if the current subsystem has been changed
   *         by abstraction, <CODE>false</CODE> otherwise.
   */
  private boolean applyCandidate(final Candidate candidate)
    throws AnalysisException
  {
    final HidingStep syncStep = composeSynchronousProduct(candidate);
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final int numLocal = local.size();
    final Collection<EventProxy> notHidden = new ArrayList<EventProxy>(numLocal);
    for (final EventProxy event : local) {
      final EventInfo info = mEventInfoMap.get(event);
      if (info.isLocal() && !info.isTau()) {
        notHidden.add(event);
      }
    }
    AutomatonProxy aut;
    final EventProxy tau;
    if (syncStep == null) {
      aut = candidate.getAutomata().iterator().next();
      tau = null;
    } else {
      aut = syncStep.getResultAutomaton();
      tau = syncStep.getTauEvent();
      if (tau != null) {
        notHidden.add(tau);
      }
    }
    recordStatistics(aut);
    final AbstractionStep simpStep = mAbstractionProcedure.run(aut, notHidden);
    if (syncStep != null || simpStep != null) {
      if (syncStep != null) {
        recordAbstractionStep(syncStep);
      }
      if (simpStep != null) {
        final Collection<EventProxy> oldEvents = aut.getEvents();
        aut = simpStep.getResultAutomaton();
        final Collection<EventProxy> newEvents =
          new THashSet<EventProxy>(aut.getEvents());
        for (final EventProxy event : oldEvents) {
          if (event != tau && !newEvents.contains(event)) {
            mEventHasDisappeared = true;
            break;
          }
        }
      }
      updateEventsToAutomata(aut, candidate.getAutomata());
      if (simpStep != null) {
        recordAbstractionStep(simpStep);
      }
      return true;
    } else {
      return false;
    }
  }


  /**
   * Builds the synchronous product for a given candidate.
   */
  private HidingStep composeSynchronousProduct(final Candidate candidate)
    throws AnalysisException
  {
    final List<AutomatonProxy> automata0 = candidate.getAutomata();
    final Collection<EventProxy> local = candidate.getLocalEvents();
    final int numLocal = local.size();
    final Collection<EventProxy> hidden = new ArrayList<EventProxy>(numLocal);
    for (final EventProxy event : local) {
      final EventInfo info = mEventInfoMap.get(event);
      if (info.isTau()) {
        hidden.add(event);
      }
    }
    final EventProxy tau;
    if (hidden.isEmpty()) {
      tau = null;
    } else {
      final ProductDESProxyFactory factory = getFactory();
      tau = createSilentEvent(automata0, factory);
    }
    final ProductDESProxy des = createProductDESProxy(automata0);
    final Collection<AutomatonProxy> automata1 = des.getAutomata();
    if (automata1.size() > 1) {
      return composeSeveralAutomata(des, hidden, tau);
    } else {
      final AutomatonProxy aut = automata1.iterator().next();
      return composeOneAutomaton(aut, hidden, tau);
    }
  }

  private HidingStep composeOneAutomaton(final AutomatonProxy aut,
                                         final Collection<EventProxy> hidden,
                                         final EventProxy tau)
    throws OverflowException
  {
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc = new EventEncoding();
    eventEnc.addSilentEvent(tau);
    for (final EventProxy event : aut.getEvents()) {
      if (hidden.contains(event)) {
        eventEnc.addSilentEvent(event);
      } else if (translator.getEventKind(event) != EventKind.PROPOSITION ||
                 (mPropositions != null && mPropositions.contains(event))) {
        eventEnc.addEvent(event, translator, false);
      }
    }
    final StateEncoding stateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
      (aut, eventEnc, stateEnc,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final boolean change1 = rel.checkReachability();
    final boolean change2 = rel.removeTauSelfLoops();
    final boolean change3 = rel.removeProperSelfLoopEvents();
    final boolean change4 = rel.removeRedundantPropositions();
    final EventProxy trueTau = change2 ? null : tau;
    mEventHasDisappeared |= change3;
    final ProductDESProxyFactory factory = getFactory();
    if (change4) {
      final StateEncoding newStateEnc = new StateEncoding();
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, newStateEnc);
      final SynchronousProductStateMap stateMap =
        new OneAutomatonStateMap(aut, stateEnc, newStateEnc);
      return new HidingStep(abstracted, hidden, trueTau, stateMap);
    } else if (tau != null || change1 || change2 || change3) {
      final AutomatonProxy abstracted =
        rel.createAutomaton(factory, eventEnc, stateEnc);
      return new HidingStep(abstracted, aut, hidden, trueTau);
    } else {
      return null;
    }
  }

  private HidingStep composeSeveralAutomata
    (final ProductDESProxy des,
     final Collection<EventProxy> hidden,
     final EventProxy tau)
    throws AnalysisException
  {
    mCurrentSynchronousProductBuilder.setModel(des);
    final Collection<EventProxy> events = des.getEvents();
    int expectedNumberOfEvents = events.size() - hidden.size();
    if (tau != null) {
      mCurrentSynchronousProductBuilder.addMask(hidden, tau);
      expectedNumberOfEvents++;
    }
    mCurrentSynchronousProductBuilder.setConstructsResult(true);
    mCurrentSynchronousProductBuilder.setNodeLimit(mCurrentInternalStateLimit);
    mCurrentSynchronousProductBuilder.setStateCallback(null);
    mCurrentSynchronousProductBuilder.setPropositions(null);
    try {
      mCurrentSynchronousProductBuilder.run();
      final AutomatonProxy sync =
        mCurrentSynchronousProductBuilder.getComputedAutomaton();
      mEventHasDisappeared |= sync.getEvents().size() < expectedNumberOfEvents;
      final SynchronousProductStateMap stateMap =
        mCurrentSynchronousProductBuilder.getStateMap();
      return new HidingStep(sync, hidden, tau, stateMap);
    } finally {
      final CompositionalAnalysisResult stats = getAnalysisResult();
      final AutomatonResult result =
        mCurrentSynchronousProductBuilder.getAnalysisResult();
      stats.addSynchronousProductAnalysisResult(result);
      mCurrentSynchronousProductBuilder.clearMask();
    }
  }

  /**
   * Creates a silent event for hiding within the given automata.
   * @return A new event named according to the candidate's automata.
   */
  private EventProxy createSilentEvent(final List<AutomatonProxy> automata,
                                       final ProductDESProxyFactory factory)
  {
    String name = Candidate.getCompositionName("tau:", automata);
    int prefix = 0;
    while (!mUsedEventNames.add(name)) {
      prefix++;
      name = Candidate.getCompositionName("tau" + prefix + ":", automata);
    }
    return factory.createEventProxy(name, EventKind.UNCONTROLLABLE, false);
  }


  //#########################################################################
  //# Statistics
  private void recordStatistics(final AutomatonProxy aut)
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addCompositionAttempt();
    final int numStates = aut.getStates().size();
    final int numTrans = aut.getTransitions().size();
    final double totalStates = result.getTotalNumberOfStates() + numStates;
    result.setTotalNumberOfStates(totalStates);
    final double peakStates =
      Math.max(result.getPeakNumberOfStates(), numStates);
    result.setPeakNumberOfStates(peakStates);
    final double totalTrans = result.getTotalNumberOfTransitions() + numTrans;
    result.setTotalNumberOfTransitions(totalTrans);
    final double peakTrans =
      Math.max(result.getPeakNumberOfTransitions(), numTrans);
    result.setPeakNumberOfTransitions(peakTrans);
    result.updatePeakMemoryUsage();
  }

  private void recordUnsuccessfulComposition()
  {
    final CompositionalAnalysisResult result = getAnalysisResult();
    result.addUnsuccessfulComposition();
  }


  //#########################################################################
  //# Inner Class AbstractionProcedure
  protected abstract class AbstractionProcedure
    implements Abortable
  {
    //#######################################################################
    //# Rule Application
    protected abstract AbstractionStep run(AutomatonProxy aut,
                                           Collection<EventProxy> local)
      throws AnalysisException;

    protected abstract void storeStatistics();

    protected abstract void resetStatistics();
  }


  //#########################################################################
  //# Inner Class TRSimplifierAbstractionProcedure
  protected abstract class TRSimplifierAbstractionProcedure
    extends AbstractionProcedure
  {
    //#######################################################################
    //# Constructor
    protected TRSimplifierAbstractionProcedure
      (final TransitionRelationSimplifier simplifier)
    {
      mSimplifier = simplifier;
    }

    //#######################################################################
    //# Overrides for AbstractionProcedure
    @Override
    protected AbstractionStep run(final AutomatonProxy aut,
                                  final Collection<EventProxy> local)
      throws AnalysisException
    {
      try {
        assert local.size() <= 1 : "At most one tau event supported!";
        final Iterator<EventProxy> iter = local.iterator();
        final EventProxy tau = iter.hasNext() ? iter.next() : null;
        final EventEncoding eventEnc = createEventEncoding(aut, tau);
        final StateEncoding inputStateEnc = new StateEncoding(aut);
        final int config = mSimplifier.getPreferredInputConfiguration();
        final ListBufferTransitionRelation rel =
          new ListBufferTransitionRelation(aut, eventEnc,
                                           inputStateEnc, config);
        final int numStates = rel.getNumberOfStates();
        final int numTrans = rel.getNumberOfTransitions();
        final int numMarkings = rel.getNumberOfMarkings();
        mSimplifier.setTransitionRelation(rel);
        if (mSimplifier.run()) {
          if (rel.getNumberOfReachableStates() == numStates &&
              rel.getNumberOfTransitions() == numTrans &&
              rel.getNumberOfMarkings() == numMarkings) {
            return null;
          }
          rel.removeRedundantPropositions();
          final ProductDESProxyFactory factory = getFactory();
          final StateEncoding outputStateEnc = new StateEncoding();
          final AutomatonProxy convertedAut =
            rel.createAutomaton(factory, eventEnc, outputStateEnc);
          return createStep
            (aut, inputStateEnc, convertedAut, outputStateEnc, tau);
        } else {
          return null;
        }
      } finally {
        mSimplifier.reset();
      }
    }

    @Override
    protected void storeStatistics()
    {
      final CompositionalAnalysisResult result = getAnalysisResult();
      result.setSimplifierStatistics(mSimplifier);
    }

    @Override
    protected void resetStatistics()
    {
      mSimplifier.createStatistics();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.Abortable
    public void requestAbort()
    {
      mSimplifier.requestAbort();
    }

    public boolean isAborting()
    {
      return mSimplifier.isAborting();
    }

    //#######################################################################
    //# Simple Access
    protected TransitionRelationSimplifier getSimplifier()
    {
      return mSimplifier;
    }

    //#######################################################################
    //# Auxiliary Methods
    protected EventEncoding createEventEncoding(final AutomatonProxy aut,
                                                final EventProxy tau)
    {
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> filter;
      if (mPropositions == null) {
        filter = Collections.emptyList();
      } else {
        filter = mPropositions;
      }
      return new EventEncoding(aut, translator, tau, filter,
                               EventEncoding.FILTER_PROPOSITIONS);
    }

    @SuppressWarnings("unused")
    private EventEncoding createEventEncoding(final AutomatonProxy aut,
                                              final Collection<EventProxy> local)
    {
      final KindTranslator translator = getKindTranslator();
      final Collection<EventProxy> filter;
      if (mPropositions == null) {
        filter = Collections.emptyList();
      } else {
        filter = mPropositions;
      }
      final Collection<EventProxy> autAlphabet = aut.getEvents();
      final Collection<EventProxy> localUncontrollableEvents =
              new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> localControllableEvents =
              new ArrayList<EventProxy>(local.size());
      final Collection<EventProxy> sharedEvents =
              new ArrayList<EventProxy>(autAlphabet.size() - local.size());
      final Collection<EventProxy> encodedEvents =
              new ArrayList<EventProxy>(autAlphabet.size());
      for(final EventProxy event:autAlphabet){
          if(local.contains(event) && translator.getEventKind(event) ==
                  EventKind.CONTROLLABLE)
              localControllableEvents.add(event);
          else if(local.contains(event) && translator.getEventKind(event) ==
                  EventKind.UNCONTROLLABLE)
              localUncontrollableEvents.add(event);
          else
              sharedEvents.add(event);
      }
      encodedEvents.addAll(localUncontrollableEvents);
      encodedEvents.addAll(localControllableEvents);
      encodedEvents.addAll(sharedEvents);
      return new EventEncoding(encodedEvents, translator, filter,
                               EventEncoding.FILTER_PROPOSITIONS);
    }

    protected abstract AbstractionStep createStep
      (final AutomatonProxy input,
       final StateEncoding inputStateEnc,
       final AutomatonProxy output,
       final StateEncoding outputStateEnc,
       final EventProxy tau);

    //#######################################################################
    //# Data Members
    private final TransitionRelationSimplifier mSimplifier;
  }


  //#########################################################################
  //# Inner Class PreselectingMethod
  /**
   * The configuration setting to determine the {@link
   * AbstractCompositionalModelAnalyzer.PreselectingHeuristic PreselectingHeuristic} used to
   * choose candidates during compositional verification. The preselecting
   * represents the first step of candidate selection. It generates a list
   * of candidates, from which the best candidate is to be chosen by the
   * selecting heuristic in the second step.
   *
   * @see SelectingMethod
   */
  public abstract static class PreselectingMethod
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethod(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Override for java.lang.Object
    @Override
    public String toString()
    {
      return mName;
    }

    //#######################################################################
    //# Heuristics
    /**
     * Gets the common method associated with this method.
     * Not all compositional model verifiers support all preselecting
     * methods. By calling {@link #getCommonMethod()}, it should be
     * possible to obtain an alternative that is supported by all
     * compositional model verifiers.
     */
    protected PreselectingMethod getCommonMethod()
    {
      return this;
    }

    /**
     * Creates the actual heuristics object implementing this preselecting
     * method.
     */
    abstract PreselectingHeuristic createHeuristic
      (AbstractCompositionalModelAnalyzer verifier);

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Inner Class PreselectingMethodFactory
  /**
   * The default preselecting method factory. This class can be used to
   * obtain a list of available preselecting heuristics, or to find
   * a preselecting heuristic given its name.
   *
   * Every compositional model verifier has its preselecting method factory
   * initialised by the constructor, but different subtypes may be initialised
   * with different factories.
   *
   * @see AbstractCompositionalModelAnalyzer#getPreselectingMethodFactory()
   * @see PreselectingMethod
   */
  protected static class PreselectingMethodFactory
    extends ListedEnumFactory<PreselectingMethod>
  {
    //#######################################################################
    //# Constructors
    protected PreselectingMethodFactory()
    {
      register(MustL);
      register(MaxS);
      register(MinT);
    }

    //#######################################################################
    //# Migration
    /**
     * Returns a preselecting method from this factory with the same name
     * as the given method.
     * @return Preselecting method if found, otherwise <CODE>null</CODE>.
     */
    protected PreselectingMethod getEnumValue(final PreselectingMethod method)
    {
      final PreselectingMethod common = method.getCommonMethod();
      final String name = common.toString();
      return getEnumValue(name);
    }
  }


  //#########################################################################
  //# Preselection Methods
  /**
   * The preselecting method that considers every set of automata with at
   * least one local event as a candidate.
   */
  public static final PreselectingMethod MustL =
      new PreselectingMethod("MustL")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new HeuristicMustL();
    }
  };

  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the most states to every other automaton in the model.
   */
  public static final PreselectingMethod MaxS =
      new PreselectingMethod("MaxS")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new HeuristicMaxS();
    }
  };

  /**
   * The preselecting method that produces candidates by pairing the
   * automaton with the fewest transitions to every other automaton in the
   * model.
   */
  public static final PreselectingMethod MinT =
      new PreselectingMethod("MinT")
  {
    @Override
    PreselectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new HeuristicMinT();
    }
  };


  //#########################################################################
  //# Inner Class SelectingMethod
  /**
   * <P>The configuration setting to determine the selecting heuristic
   * used to choose candidates during compositional verification.</P>
   *
   * <P>The selecting represents the second step of candidate selection. It
   * chooses the best candidate from a list of candidates generated by the
   * {@link AbstractCompositionalModelAnalyzer.PreselectingHeuristic PreselectingHeuristic}
   * in the first step.</P>
   *
   * <P>Selection is implemented using a {@link Comparator}. The smallest
   * candidate according to the defined ordering gets selected.</P>
   *
   * @see PreselectingMethod
   */
  public abstract static class SelectingMethod
  {
    //#######################################################################
    //# Constructors
    protected SelectingMethod(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Override for java.lang.Object
    @Override
    public String toString()
    {
      return mName;
    }

    //#######################################################################
    //# Heuristics
    /**
     * Gets the common method associated with this method.
     * Not all compositional model verifiers support all selecting
     * methods. By calling {@link #getCommonMethod()}, it should be
     * possible to obtain an alternative that is supported by all
     * compositional model verifiers.
     */
    protected SelectingMethod getCommonMethod()
    {
      return this;
    }

    /**
     * Creates a comparator to implement this selecting heuristic.
     * This returns an implementation of only one heuristic, which
     * may consider two candidates as equal.
     * @param  verifier The model verifier requesting and using the
     *                  heuristic.
     * @return A comparator, or <CODE>null</CODE> if the heuristic
     *         is not implemented by a comparator.
     */
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return null;
    }

    /**
     * Creates a selecting heuristic that gives preferences to this method.
     * The returned heuristic first compares candidates according to this
     * selection methods. If two candidates are found equal, all other enabled
     * selection heuristics are used, in the order in which they are
     * defined in the enumeration. If the candidates are equal under
     * all heuristics, they are compared based on their names. This
     * guarantees that no two candidates are equal.
     * @param verifier The model verifier requesting and using the
     *                 heuristic.
     */
    SelectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      final SelectingMethodFactory factory = verifier.mSelectingMethodFactory;
      final Comparator<Candidate> chain =
          factory.createComparatorChain(verifier, this);
      return verifier.new SelectingHeuristic(chain);
    }

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Inner Class SelectingMethodFactory
  protected static class SelectingMethodFactory
    extends ListedEnumFactory<SelectingMethod>
  {
    //#######################################################################
    //# Constructors
    protected SelectingMethodFactory()
    {
      register(MaxL);
      register(MaxC);
      register(MinE);
      register(MinS);
      register(MinSync);
    }

    //#######################################################################
    //# Migration
    protected SelectingMethod getEnumValue(final SelectingMethod method)
    {
      final SelectingMethod common = method.getCommonMethod();
      final String name = common.toString();
      return getEnumValue(name);
    }

    //#######################################################################
    //# Chain Construction
    /**
     * Creates a comparator to implement the given selecting heuristic. The
     * returned comparator first compares candidates according to the given
     * selection methods. If two candidates are found equal, all other enabled
     * selection heuristics are used, in the order in which they are registered
     * in the enumeration. If the candidates are equal under all heuristics,
     * they are compared based on their names. This guarantees that no two
     * candidates are equal.
     * @param method
     *          Primary selection method to be used first.
     */
    Comparator<Candidate> createComparatorChain
      (final AbstractCompositionalModelAnalyzer verifier,
       final SelectingMethod method)
    {
      final List<Comparator<Candidate>> list =
        new LinkedList<Comparator<Candidate>>();
      Comparator<Candidate> heu = method.createComparator(verifier);
      list.add(heu);
      for (final SelectingMethod other : getEnumConstants()) {
        if (other != method) {
          heu = other.createComparator(verifier);
          if (heu != null) {
            list.add(heu);
          }
        }
      }
      return verifier.new ComparatorChain(list);
    }
  }


  //#########################################################################
  //# Selection Methods
  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of common events.
   * An event is considered as common if it is used by at least two
   * automata of the candidate.
   */
  public static final SelectingMethod MaxC = new SelectingMethod("MaxC")
  {
    @Override
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new ComparatorMaxC();
    }
  };

  /**
   * The selecting method that chooses the candidate with the highest
   * proportion of local events.
   */
  public static final SelectingMethod MaxL = new SelectingMethod("MaxL")
  {
    @Override
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new ComparatorMaxL();
    }
  };

  /**
   * The selecting method that chooses the candidate with the smallest
   * alphabet extension. The alphabet extension is given by the quotient
   * of the number of events of a candidate divided by the largest number of
   * events of a single automaton of the candidate.
   */
  public static final SelectingMethod MinE = new SelectingMethod("MinE")
  {
    @Override
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new ComparatorMinE();
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * estimated number of states in the synchronous product.
   */
  public static final SelectingMethod MinS = new SelectingMethod("MinS")
  {
    @Override
    Comparator<Candidate> createComparator
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      return verifier.new ComparatorMinS();
    }
  };

  /**
   * The selecting method that chooses the candidate with the minimum
   * actual number of states in the synchronous product.
   */
  public static final SelectingMethod MinSync =
      new SelectingMethod("MinSync")
  {
    @Override
    SelectingHeuristic createHeuristic
      (final AbstractCompositionalModelAnalyzer verifier)
    {
      final SelectingMethodFactory factory = verifier.mSelectingMethodFactory;
      final Comparator<Candidate> alt =
          factory.createComparatorChain(verifier, MinS);
      return verifier.new HeuristicMinSync(alt);
    }
  };


  //#########################################################################
  //# Inner Class AbstractionStep
  protected abstract class AbstractionStep
  {

    //#######################################################################
    //# Constructors
    protected AbstractionStep(final List<AutomatonProxy> results,
                              final List<AutomatonProxy> originals)
    {
      mResultAutomata = results;
      mOriginalAutomata = originals;
    }

    protected AbstractionStep(final AutomatonProxy result,
                              final Collection<AutomatonProxy> originals)
    {
      this(Collections.singletonList(result),
           new ArrayList<AutomatonProxy>(originals));
    }

    protected AbstractionStep(final AutomatonProxy result,
                              final AutomatonProxy original)
    {
      this(Collections.singletonList(result),
           Collections.singletonList(original));
    }

    //#######################################################################
    //# Simple Access
    protected List<AutomatonProxy> getResultAutomata()
    {
      return mResultAutomata;
    }

    protected AutomatonProxy getResultAutomaton()
    {
      if (mResultAutomata.size() == 1) {
        return mResultAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single result automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mResultAutomata.size() + " result automata!");
      }
    }

    protected List<AutomatonProxy> getOriginalAutomata()
    {
      return mOriginalAutomata;
    }

    protected AutomatonProxy getOriginalAutomaton()
    {
      if (mOriginalAutomata.size() == 1) {
        return mOriginalAutomata.iterator().next();
      } else {
        throw new IllegalStateException
          ("Attempting to get a single input automaton from " +
           ProxyTools.getShortClassName(this) + " with " +
           mOriginalAutomata.size() + " input automata!");
      }
    }

    protected void addAutomatonPair(final AutomatonProxy result,
                                    final AutomatonProxy original)
    {
      mResultAutomata.add(result);
      mOriginalAutomata.add(original);
    }

    //#######################################################################
    //# Trace Computation
    /**
     * Converts the given trace on the result of this rule application
     * to a trace on the original automaton before abstraction.
     * Assumes that a saturated trace is being passed.
     */
    protected List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps)
      throws AnalysisException
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support trace expansion!");
    }

    //#######################################################################
    //# Data Members
    private final List<AutomatonProxy> mResultAutomata;
    private final List<AutomatonProxy> mOriginalAutomata;

  }


  //#########################################################################
  //# Inner Class EventRemovalStep
  /**
   * An abstraction step that consists of removing some events from the
   * model. Event removal is implemented by replacing automata with simplified
   * copies that use the same state objects, so trace expansion can be
   * achieved by replacing only the automata in a trace.
   */
  protected class EventRemovalStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new event removal step.
     * @param  results   List of automata after event removal.
     * @param  originals List of automata before removal, with indexes
     *                   matching those of results. The automaton at position
     *                   <I>i</I> in originals is replaced by the automaton
     *                   at the same position in results.
     */
    private EventRemovalStep(final List<AutomatonProxy> results,
                             final List<AutomatonProxy> originals)
    {
      super(results, originals);
    }

    //#######################################################################
    //# Trace Computation
    protected List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps)
    {
      final List<AutomatonProxy> results = getResultAutomata();
      final List<AutomatonProxy> originals = getOriginalAutomata();
      final int numAutomata = results.size();
      final Map<AutomatonProxy,AutomatonProxy> autMap =
        new HashMap<AutomatonProxy,AutomatonProxy>(numAutomata);
      final Iterator<AutomatonProxy> resultIter = results.iterator();
      final Iterator<AutomatonProxy> originalIter = originals.iterator();
      while (resultIter.hasNext()) {
        final AutomatonProxy result = resultIter.next();
        final AutomatonProxy original = originalIter.next();
        autMap.put(result, original);
      }
      final ProductDESProxyFactory factory = getFactory();
      final ListIterator<TraceStepProxy> iter = steps.listIterator();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final int size = stepMap.size();
        final Map<AutomatonProxy,StateProxy> newStepMap =
          new HashMap<AutomatonProxy,StateProxy>(size);
        for (final Map.Entry<AutomatonProxy,StateProxy> entry :
             stepMap.entrySet()) {
          final AutomatonProxy aut = entry.getKey();
          AutomatonProxy newAut = autMap.get(aut);
          if (newAut == null) {
            newAut = aut;
          }
          final StateProxy state = entry.getValue();
          newStepMap.put(newAut, state);
        }
        final EventProxy event = step.getEvent();
        final TraceStepProxy newStep =
          factory.createTraceStepProxy(event, newStepMap);
        iter.set(newStep);
      }
      return steps;
    }

  }


  //#########################################################################
  //# Inner Class HidingStep
  private class HidingStep extends AbstractionStep
  {

    //#######################################################################
    //# Constructor
    private HidingStep(final AutomatonProxy composedAut,
                       final AutomatonProxy originalAut,
                       final Collection<EventProxy> localEvents,
                       final EventProxy tau)
    {
      super(composedAut, originalAut);
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = null;
    }

    private HidingStep(final AutomatonProxy composedAut,
                       final Collection<EventProxy> localEvents,
                       final EventProxy tau,
                       final SynchronousProductStateMap stateMap)
    {
      super(composedAut, stateMap.getInputAutomata());
      mLocalEvents = localEvents;
      mHiddenEvent = tau;
      mStateMap = stateMap;
    }

    //#######################################################################
    //# Simple Access
    EventProxy getTauEvent()
    {
      return mHiddenEvent;
    }

    //#######################################################################
    //# Trace Computation
    protected List<TraceStepProxy> convertTraceSteps
      (final List<TraceStepProxy> steps)
    {
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy resultAutomaton = getResultAutomaton();
      final Collection<AutomatonProxy> originalAutomata = getOriginalAutomata();
      final int convertedNumAutomata =
        steps.iterator().next().getStateMap().size() +
        originalAutomata.size() - 1;
      Map<AutomatonProxy,StateProxy> previousMap = null;
      final ListIterator<TraceStepProxy> iter = steps.listIterator();
      while (iter.hasNext()) {
        final TraceStepProxy step = iter.next();
        final Map<AutomatonProxy,StateProxy> stepMap = step.getStateMap();
        final Map<AutomatonProxy,StateProxy> convertedStepMap =
          new HashMap<AutomatonProxy,StateProxy>(convertedNumAutomata);
        convertedStepMap.putAll(stepMap);
        final StateProxy convertedState =
          convertedStepMap.remove(resultAutomaton);
        for (final AutomatonProxy aut : originalAutomata) {
          final StateProxy originalState =
            getOriginalState(convertedState, aut);
          convertedStepMap.put(aut, originalState);
        }
        EventProxy event = step.getEvent();
        if (event != null && event == mHiddenEvent) {
          event = findEvent(previousMap, convertedStepMap);
        }
        final TraceStepProxy convertedStep =
          factory.createTraceStepProxy(event, convertedStepMap);
        iter.set(convertedStep);
        previousMap = convertedStepMap;
      }
      return steps;
    }

    private EventProxy findEvent(final Map<AutomatonProxy,StateProxy> sources,
                                 final Map<AutomatonProxy,StateProxy> targets)
    {
      final Collection<EventProxy> possible =
        new LinkedList<EventProxy>(mLocalEvents);
      for (final AutomatonProxy aut : getOriginalAutomata()) {
        if (possible.size() <= 1) {
          break;
        }
        final StateProxy source = sources.get(aut);
        final StateProxy target = targets.get(aut);
        final Collection<EventProxy> alphabet =
          new THashSet<EventProxy>(aut.getEvents());
        final int size = alphabet.size();
        final Collection<EventProxy> retained = new THashSet<EventProxy>(size);
        for (final TransitionProxy trans : aut.getTransitions()) {
          if (trans.getSource() == source && trans.getTarget() == target) {
            final EventProxy event = trans.getEvent();
            retained.add(event);
          }
        }
        final Iterator<EventProxy> iter = possible.iterator();
        while (iter.hasNext()) {
          final EventProxy event = iter.next();
          if (alphabet.contains(event)) {
            if (!retained.contains(event)) {
              iter.remove();
            }
          } else {
            if (source != target) {
              iter.remove();
            }
          }
        }
      }
      return possible.iterator().next();
    }

    final StateProxy getOriginalState(final StateProxy convertedState,
                                      final AutomatonProxy aut)
    {
      if (mStateMap == null) {
        return convertedState;
      } else {
        return mStateMap.getOriginalState(convertedState, aut);
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<EventProxy> mLocalEvents;
    private final EventProxy mHiddenEvent;
    private final SynchronousProductStateMap mStateMap;
  }


  //#########################################################################
  //# Inner Class SubSystem
  /**
   * A collection of automata and associated events.
   * This class is used to store subsystems to be checked later.
   * Essentially it holds the contents of a {@link ProductDESProxy},
   * but in a more lightweight form.
   */
  protected static class SubSystem
    implements Comparable<SubSystem>
  {

    //#######################################################################
    //# Constructors
    private SubSystem(final AutomatonProxy aut, final int limit)
    {
      final Collection<EventProxy> events = aut.getEvents();
      final int numEvents = events.size();
      mEvents = new ArrayList<EventProxy>(numEvents);
      for (final EventProxy event : events) {
        if (event.getKind() != EventKind.PROPOSITION) {
          mEvents.add(event);
        }
      }
      mAutomata = new ArrayList<AutomatonProxy>(1);
      mAutomata.add(aut);
      mStateLimit = limit;
    }

    private SubSystem(final List<EventProxy> events,
                      final List<AutomatonProxy> automata,
                      final int limit)
    {
      mEvents = events;
      mAutomata = automata;
      mStateLimit = limit;
    }

    //#######################################################################
    //# Interface java.util.Comparable<SubSystem>
    public int compareTo(final SubSystem other)
    {
      final int aut1 = mAutomata.size();
      final int aut2 = other.mAutomata.size();
      if (aut1 != aut2) {
        return aut1 - aut2;
      }
      final int events1 = mEvents.size();
      final int events2 = other.mEvents.size();
      if (events1 != events2) {
        return events1 - events2;
      }
      final String name1 = Candidate.getCompositionName(mAutomata);
      final String name2 = Candidate.getCompositionName(other.mAutomata);
      return name1.compareTo(name2);
    }

    //#######################################################################
    //# Simple Access
    protected List<EventProxy> getEvents()
    {
      return mEvents;
    }

    protected List<AutomatonProxy> getAutomata()
    {
      return mAutomata;
    }

    protected int getStateLimit()
    {
      return mStateLimit;
    }

    //#######################################################################
    //# Data Members
    private final List<EventProxy> mEvents;
    private final List<AutomatonProxy> mAutomata;
    private final int mStateLimit;

  }


  //#########################################################################
  //# Inner Class EventInfo
  /**
   * A record to store information about the automata an event occurs in.
   * The event information record basically consists of the set of automata
   * it occurs in, plus information in which automata the event only appears
   * as selfloops.
   */
  protected static class EventInfo
  {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new EventInfo record.
     */
    protected EventInfo(final EventProxy event)
    {
      mEvent = event;
      mAutomataMap = new TObjectByteHashMap<AutomatonProxy>();
      mNumNonSelfloopAutomata = 0;
      mIsBlocked = false;
    }

    //#######################################################################
    //# Event Status
    /**
     * Gets the event associated with this event information record.
     */
    protected EventProxy getEvent()
    {
      return mEvent;
    }

    /**
     * Returns whether this event should be treated as
     * {@link EventEncoding#TAU TAU}. Events treated as TAU are removed
     * during synchronous composition.
     */
    protected boolean isTau()
    {
      return true;
    }

    /**
     * Returns whether this event can be considered as local event. Local
     * events that have not been replaced by TAU during synchronous
     * compositions are passed to the abstraction procedure for special
     * treatment.
     */
    protected boolean isLocal()
    {
      return true;
    }

    /**
     * Returns whether this event can be subject to selfloop removal.
     * Events subject to selfloop removal are removed from the model
     * when it is found that they appear only as selfloop events.
     */
    protected boolean isSubjectToSelfloopRemoval()
    {
      return isTau();
    }

    //#######################################################################
    //# Simple Access
    protected List<AutomatonProxy> getAutomataList()
    {
      final int size = mAutomataMap.size();
      final AutomatonProxy[] automata = new AutomatonProxy[size];
      mAutomataMap.keys(automata);
      return Arrays.asList(automata);
    }

    private boolean addAutomataTo(final Collection<AutomatonProxy> target)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      boolean added = false;
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        added |= target.add(aut);
      }
      return added;
    }

    private void addAutomaton(final AutomatonProxy aut, final byte status)
    {
      final byte present = mAutomataMap.get(aut);
      if (present != status) {
        mAutomataMap.put(aut, status);
        if (present == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
        if (status == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata++;
        }
        mIsBlocked |= status == BLOCKED;
      }
    }

    private boolean containedIn(final Collection<AutomatonProxy> automata)
    {
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        if (!automata.contains(aut)) {
          return false;
        }
      }
      return true;
    }

    private Set<AutomatonProxy> getAutomataSet()
    {
      final int size = mAutomataMap.size();
      final Set<AutomatonProxy> automata = new THashSet<AutomatonProxy>(size);
      final TObjectByteIterator<AutomatonProxy> iter = mAutomataMap.iterator();
      while (iter.hasNext()) {
        iter.advance();
        final AutomatonProxy aut = iter.key();
        automata.add(aut);
      }
      return automata;
    }

    private int getNumberOfAutomata()
    {
      return mAutomataMap.size();
    }

    private boolean intersects(final Collection<AutomatonProxy> automata)
    {
      for (final AutomatonProxy aut : automata) {
        if (mAutomataMap.containsKey(aut)) {
          return true;
        }
      }
      return false;
    }

    private boolean isEmpty()
    {
      return mAutomataMap.isEmpty();
    }

    private boolean isRemovable()
    {
      if (mIsBlocked) {
        return true;
      } else if (mNumNonSelfloopAutomata == 0) {
        return isSubjectToSelfloopRemoval();
      } else {
        return false;
      }
    }

    private void removeAutomata(final Collection<AutomatonProxy> victims)
    {
      for (final AutomatonProxy aut : victims) {
        final byte code = mAutomataMap.remove(aut);
        if (code == NOT_ONLY_SELFLOOP) {
          mNumNonSelfloopAutomata--;
        }
      }
    }

    private boolean replaceAutomaton(final AutomatonProxy oldAut,
                                     final AutomatonProxy newAut)
    {
      final byte code = mAutomataMap.remove(oldAut);
      if (code == UNKNOWN_SELFLOOP) {
        // not found in map ...
        return false;
      } else {
        mAutomataMap.put(newAut, code);
        return true;
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringBuffer buffer = new StringBuffer("[");
      boolean first = true;
      for (final AutomatonProxy aut : getAutomataList()) {
        if (first) {
          first = false;
        } else {
          buffer.append(',');
        }
        buffer.append(aut.getName());
      }
      buffer.append(']');
      return buffer.toString();
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private final TObjectByteHashMap<AutomatonProxy> mAutomataMap;
    private int mNumNonSelfloopAutomata;
    private boolean mIsBlocked;

  }


  //#########################################################################
  //# Local Interface PreselectingHeuristic
  protected interface PreselectingHeuristic
  {
    public Collection<Candidate> findCandidates();
  }


  //#########################################################################
  //# Inner Class PairingHeuristic
  protected abstract class PairingHeuristic
    implements PreselectingHeuristic, Comparator<AutomatonProxy>
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    public Collection<Candidate> findCandidates()
    {
      final AutomatonProxy chosenAut = Collections.min(mCurrentAutomata, this);
      return pairAutomaton(chosenAut, mCurrentAutomata);
    }

    //#######################################################################
    //# Auxiliary Methods
    private Collection<Candidate> pairAutomaton
      (final AutomatonProxy chosenAut,
       final Collection<AutomatonProxy> automata)
    {
      final Set<EventProxy> chosenEvents =
        new THashSet<EventProxy>(chosenAut.getEvents());
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
      for (final AutomatonProxy aut : automata) {
        if (aut != chosenAut && synchronises(chosenEvents, aut.getEvents())) {
          final List<AutomatonProxy> pair = new ArrayList<AutomatonProxy>(2);
          if (chosenAut.compareTo(aut) < 0) {
            pair.add(chosenAut);
            pair.add(aut);
          } else {
            pair.add(aut);
            pair.add(chosenAut);
          }
          if (isPermissibleCandidate(pair)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(pair);
            final Candidate candidate = new Candidate(pair, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }

    private boolean synchronises(final Set<EventProxy> set,
                                 final Collection<EventProxy> collection)
    {
      final KindTranslator translator = getKindTranslator();
      for (final EventProxy event : collection) {
        if (translator.getEventKind(event) != EventKind.PROPOSITION &&
            set.contains(event)) {
          return true;
        }
      }
      return false;
    }
  }


  //#########################################################################
  //# Inner Class HeuristicMinT
  private class HeuristicMinT
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans1 - numtrans2;
      }
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates1 - numstates2;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMaxS
  /**
   * Performs step 1 of the approach to select the automata to compose. A
   * candidate is produced by pairing the automaton with the most states to
   * every other automaton in the model.
   */
  private class HeuristicMaxS
    extends PairingHeuristic
  {

    //#######################################################################
    //# Interface java.util.Comparator<AutomatonProxy>
    public int compare(final AutomatonProxy aut1, final AutomatonProxy aut2)
    {
      final int numstates1 = aut1.getStates().size();
      final int numstates2 = aut2.getStates().size();
      if (numstates1 != numstates2) {
        return numstates2 - numstates1;
      }
      final int numtrans1 = aut1.getTransitions().size();
      final int numtrans2 = aut2.getTransitions().size();
      if (numtrans1 != numtrans2) {
        return numtrans2 - numtrans1;
      }
      return aut1.compareTo(aut2);
    }

  }


  //#########################################################################
  //# Inner Class HeuristicMustL
  private class HeuristicMustL
    implements PreselectingHeuristic
  {

    //#######################################################################
    //# Interface PreselectingHeuristic
    public Collection<Candidate> findCandidates()
    {
      final Collection<Candidate> candidates = new LinkedList<Candidate>();
      final int size = mEventInfoMap.size();
      final Collection<List<AutomatonProxy>> found =
        new THashSet<List<AutomatonProxy>>(size);
      for (final EventInfo info : mEventInfoMap.values()) {
        assert info.getNumberOfAutomata() > 0;
        if (info.isLocal() && info.getNumberOfAutomata() > 1) {
          final List<AutomatonProxy> list = info.getAutomataList();
          Collections.sort(list);
          if (isPermissibleCandidate(list) && found.add(list)) {
            final Set<EventProxy> localEvents = identifyLocalEvents(list);
            final Candidate candidate = new Candidate(list, localEvents);
            candidates.add(candidate);
          }
        }
      }
      return candidates;
    }
  }


  //#########################################################################
  //# Inner Class SelectingHeuristic
  protected class SelectingHeuristic {

    //#######################################################################
    //# Constructor
    protected SelectingHeuristic(final Comparator<Candidate> comparator)
    {
      mComparator = comparator;
    }

    //#######################################################################
    //# Candidate Evaluation
    Comparator<Candidate> getComparator()
    {
      return mComparator;
    }

    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      return Collections.min(candidates, mComparator);
    }

    //#######################################################################
    //# Data Members
    private final Comparator<Candidate> mComparator;

  }


  //#########################################################################
  //# Inner Class SelectingComparator
  protected abstract class SelectingComparator
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      final double heu1 = getHeuristicValue(cand1);
      final double heu2 = getHeuristicValue(cand2);
      if (heu1 < heu2) {
        return -1;
      } else if (heu1 > heu2) {
        return 1;
      } else {
        return 0;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    abstract double getHeuristicValue(final Candidate candidate);

  }


  //#########################################################################
  //# Inner Class HeuristicMinSync
  private class HeuristicMinSync extends SelectingHeuristic {

    //#######################################################################
    //# Constructor
    private HeuristicMinSync(final Comparator<Candidate> comparator)
    {
      super(comparator);
    }

    //#######################################################################
    //# Overrides for SelectingHeuristic
    Candidate selectCandidate(final Collection<Candidate> candidates)
    throws AnalysisException
    {
      final List<Candidate> list = new ArrayList<Candidate>(candidates);
      final Comparator<Candidate> comparator = getComparator();
      Collections.sort(list, comparator);
      int limit = mCurrentInternalStateLimit;
      mCurrentSynchronousProductBuilder.setNodeLimit(limit);
      mCurrentSynchronousProductBuilder.setConstructsResult(false);
      mCurrentSynchronousProductBuilder.setStateCallback(null);
      Candidate best = null;
      final List<EventProxy> empty = Collections.emptyList();
      mCurrentSynchronousProductBuilder.setPropositions(empty);
      for (final Candidate candidate : list) {
        final List<AutomatonProxy> automata = candidate.getAutomata();
        final ProductDESProxy des = createProductDESProxy(automata);
        mCurrentSynchronousProductBuilder.setModel(des);
        try {
          mCurrentSynchronousProductBuilder.run();
          final AnalysisResult result =
            mCurrentSynchronousProductBuilder.getAnalysisResult();
          final double dsize = result.getTotalNumberOfStates();
          final int size = (int) Math.round(dsize);
          if (size < limit || best == null) {
            best = candidate;
            limit = size;
            mCurrentSynchronousProductBuilder.setNodeLimit(limit);
          }
        } catch (final OutOfMemoryError error) {
          getLogger().debug("<out of memory>");
          // skip this one ...
        } catch (final OverflowException overflow) {
          // skip this one ...
        } finally {
          final CompositionalAnalysisResult stats = getAnalysisResult();
          final AutomatonResult result =
            mCurrentSynchronousProductBuilder.getAnalysisResult();
          stats.addSynchronousProductAnalysisResult(result);
        }
      }
      return best;
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMaxL
  private class ComparatorMaxL extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getLocalEventCount() /
               (double) candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMaxC
  private class ComparatorMaxC extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      return - (double) candidate.getCommonEventCount() /
               (double) candidate.getNumberOfEvents();
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMinE
  private class ComparatorMinE extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      final int unionAlphabetSize = candidate.getNumberOfEvents();
      int largestAlphabetSize = 0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        final int size = Candidate.countEvents(aut);
        if (size > largestAlphabetSize) {
          largestAlphabetSize = size;
        }
      }
      return (double) unionAlphabetSize / (double) largestAlphabetSize;
    }

  }


  //#########################################################################
  //# Inner Class ComparatorMinS
  private class ComparatorMinS extends SelectingComparator
  {

    //#######################################################################
    //# Overrides for SelectingComparator
    double getHeuristicValue(final Candidate candidate)
    {
      double product = 1.0;
      for (final AutomatonProxy aut : candidate.getAutomata()) {
        product *= aut.getStates().size();
      }
      final double totalEvents = candidate.getNumberOfEvents();
      final double localEvents = candidate.getLocalEventCount();
      return product * (totalEvents - localEvents) / totalEvents;
    }

  }


  //#########################################################################
  //# Inner Class ComparatorChain
  private class ComparatorChain
    implements Comparator<Candidate>
  {

    //#######################################################################
    //# Constructor
    private ComparatorChain(final List<Comparator<Candidate>> list)
    {
      mHeuristics = list;
    }

    //#######################################################################
    //# Interface java.util.Comparator<Candidate>
    public int compare(final Candidate cand1, final Candidate cand2)
    {
      for (final Comparator<Candidate> heu : mHeuristics) {
        final int result = heu.compare(cand1, cand2);
        if (result != 0) {
          return result;
        }
      }
      return cand1.compareTo(cand2);
    }

    //#######################################################################
    //# Data Members
    private final List<Comparator<Candidate>> mHeuristics;

  }


  //#########################################################################
  //# Inner Class OneAutomatonStateMap
  private class OneAutomatonStateMap
    implements SynchronousProductStateMap
  {
    //#######################################################################
    //# Constructor
    private OneAutomatonStateMap(final AutomatonProxy inputAut,
                                 final StateEncoding inputEnc,
                                 final StateEncoding outputEnc)
    {
      mOriginalAutomaton = inputAut;
      final int numStates = inputEnc.getNumberOfStates();
      mStateMap = new HashMap<StateProxy,StateProxy>(numStates);
      for (int s = 0; s < numStates; s++) {
        final StateProxy inputState = inputEnc.getState(s);
        final StateProxy outputState = outputEnc.getState(s);
        mStateMap.put(outputState, inputState);
      }
    }


    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.model.analysis.SynchronousProductStateMap
    public Collection<AutomatonProxy> getInputAutomata()
    {
      return Collections.singletonList(mOriginalAutomaton);
    }

    public StateProxy getOriginalState(final StateProxy tuple,
                                       final AutomatonProxy aut)
    {
      if (aut == mOriginalAutomaton) {
        return mStateMap.get(tuple);
      } else {
        throw new IllegalArgumentException
          ("Unexpected original automaton '" + aut.getName() + "' in " +
           ProxyTools.getShortClassName(this) + "!");
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mOriginalAutomaton;
    private final Map<StateProxy,StateProxy> mStateMap;
  }


  //#########################################################################
  //# Data Members
  private final PreselectingMethodFactory mPreselectingMethodFactory;
  private PreselectingMethod mPreselectingMethod;
  private final SelectingMethodFactory mSelectingMethodFactory;
  private SelectingMethod mSelectingMethod;
  private boolean mSubsumptionEnabled;
  private MonolithicSynchronousProductBuilder mSynchronousProductBuilder;
  private int mLowerInternalStateLimit;
  private int mUpperInternalStateLimit;
  private int mInternalTransitionLimit;
  private Collection<EventProxy> mPropositions;

  /**
   * The automata currently being analysed. This list is updated after each
   * abstraction step and represents the current state of the model. It may
   * contain abstractions of only part of the original model, if event-disjoint
   * subsystems are found.
   * @see #mPostponedSubsystems
   */
  private List<AutomatonProxy> mCurrentAutomata;

  private Map<EventProxy,EventInfo> mEventInfoMap =
      new HashMap<EventProxy,EventInfo>();
  /**
   * List of <I>dirty</I> automata that need simplifying. An automaton is added
   * to this list if there is the possibility that it can be simplified without
   * having to be composed with another automaton. Initially, all automata are
   * considered <I>dirty</I>, and certain abstractions such as event removal
   * may produce <I>dirty</I> automata at later stages. If there are dirty
   * automata, it is first attempted to simplify them individually, before
   * considering the next candidate for composition.
   * @see #simplifyDirtyAutomata()
   */
  private Queue<AutomatonProxy> mDirtyAutomata;
  /**
   * List of events found to be redundant and scheduled for removal by an
   * {@link EventRemovalStep}.
   * @see #removeEventsToAutomata(Collection) removeEventsToAutomata()
   * @see #removeRedundantEvents()
   */
  private Collection<EventProxy> mRedundantEvents;
  /**
   * A flag indicating that an event has disappeared unexpectedly.
   * This flag is set when a proper event has been found to be only selflooped
   * in an automaton after abstraction, and therefore has been removed from
   * the automaton alphabet.
   */
  private boolean mEventHasDisappeared;
  /**
   * List of subsystems still to be analysed. If a model can be split into
   * event-disjoint subsystems, these subsystems are analysed one-by-one.
   * After splitting a subsystem, parts may be added to this list to be
   * analysed at a later stage.
   * @see #findEventDisjointSubsystems()
   */
  private Queue<SubSystem> mPostponedSubsystems;
  /**
   * List of subsystems that have been analysed.
   */
  private Collection<SubSystem> mProcessedSubsystems;
  private Set<String> mUsedEventNames;
  private Set<List<AutomatonProxy>> mOverflowCandidates;
  private int mCurrentInternalStateLimit;

  private AbstractionProcedure mAbstractionProcedure;
  private PreselectingHeuristic mPreselectingHeuristic;
  private SelectingHeuristic mSelectingHeuristic;
  private MonolithicSynchronousProductBuilder
    mCurrentSynchronousProductBuilder;


  //#########################################################################
  //# Class Constants
  private static final byte UNKNOWN_SELFLOOP = 0;
  private static final byte ONLY_SELFLOOP = 1;
  private static final byte NOT_ONLY_SELFLOOP = 2;
  private static final byte BLOCKED = 3;

}
