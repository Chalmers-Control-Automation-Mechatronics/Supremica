//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.bdd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractModelVerifier;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.apache.log4j.Logger;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * An abstract base class for all BDD-based model verifiers.
 *
 * @author Robi Malik
 */

public abstract class BDDModelVerifier
  extends AbstractModelVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new BDD-based model verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDModelVerifier(final KindTranslator translator,
                          final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  /**
   * Creates a new BDD-based model verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDModelVerifier(final KindTranslator translator,
                          final ProductDESProxyFactory desfactory,
                          final BDDPackage bddpackage)
  {
    this(null, translator, desfactory, bddpackage);
  }

  /**
   * Creates a new BDD-based model verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDModelVerifier(final ProductDESProxy model,
                          final KindTranslator translator,
                          final ProductDESProxyFactory factory)
  {
    this(model, translator, factory, BDD_PACKAGE);
  }

  /**
   * Creates a new BDD-based model verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD package to be used.
   */
  public BDDModelVerifier(final ProductDESProxy model,
                          final KindTranslator translator,
                          final ProductDESProxyFactory desfactory,
                          final BDDPackage bddpackage)
  {
    super(model, desfactory, translator);
    mBDDPackage = bddpackage;
    mVariableOrdering = VariableOrdering.FORCE;
    mIsReorderingEnabled = true;
    mInitialSize = 50000;
    mPartitioningStrategy = TransitionPartitioningStrategy.AUTOMATA;
    mPartitioningSizeLimit = 10000;
  }


  //#########################################################################
  //# Configuration
  public void setBDDPackage(final BDDPackage pack)
  {
    mBDDPackage = pack;
  }

  public BDDPackage getBDDPackage()
  {
    return mBDDPackage;
  }

  public void setVariableOrdering(final VariableOrdering ordering)
  {
    mVariableOrdering = ordering;
  }

  public VariableOrdering getVariableOrdering()
  {
    return mVariableOrdering;
  }

  public void setReorderingEnabled(final boolean enable)
  {
    mIsReorderingEnabled = enable;
  }

  public boolean isReorderingEnabled()
  {
    return mIsReorderingEnabled;
  }

  public void setInitialSize(final int size)
  {
    mInitialSize = size;
  }

  public int getInitialSize()
  {
    return mInitialSize;
  }

  public void setTransitionPartitioningStrategy
    (final TransitionPartitioningStrategy strategy)
  {
    mPartitioningStrategy = strategy;
  }

  public TransitionPartitioningStrategy getTransitionPartitioningStrategy()
  {
    return mPartitioningStrategy;
  }

  public void setPartitioningSizeLimit(final int limit)
  {
    mPartitioningSizeLimit = limit < 0 ? Integer.MAX_VALUE : limit;
  }

  public int getPartitioningSizeLimit()
  {
    return mPartitioningSizeLimit;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mTransitionPartitioning != null) {
      mTransitionPartitioning.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mTransitionPartitioning != null) {
      mTransitionPartitioning.resetAbort();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    clearAnalysisResult();
  }

  @Override
  public void checkAbort()
    throws AnalysisAbortException, OverflowException
  {
    super.checkAbort();
    if (mBDDFactory != null) {
      final int numNodes = mBDDFactory.getNodeNum();
      if (numNodes > mPeakNodes) {
        mPeakNodes = numNodes;
        if (numNodes > getNodeLimit()) {
          final OverflowException exception =
            new OverflowException(OverflowKind.NODE, getNodeLimit());
          getLogger().debug(exception.getMessage() + " - aborting ...");
          throw exception;
        }
      }
    }
  }


  //#########################################################################
  //# Access for Algorithms
  int getNumberOfAutomata()
  {
    return mNumAutomata;
  }


  //#########################################################################
  //# Overrides for Algorithms
  @Override
  public void setUp() throws AnalysisException
  {
    super.setUp();
    final int initnodes = mInitialSize;
    final String name = mBDDPackage.getBDDPackageName();
    mBDDFactory = BDDFactory.init(name, initnodes, CACHE_RATIO * initnodes);
    mBDDFactory.setCacheRatio(CACHE_RATIO);
    try {
      final Class<?>[] parameterTypes =
        new Class<?>[] {Object.class, Object.class};
      final Method method =
        getClass().getMethod("silentBDDHandler", parameterTypes);
      mBDDFactory.registerGCCallback(this, method);
      mBDDFactory.registerReorderCallback(this, method);
      mBDDFactory.registerResizeCallback(this, method);
    } catch (final SecurityException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final NoSuchMethodException exception) {
      throw new WatersRuntimeException(exception);
    }
    try {
      mBDDFactory.disableReorder();
    } catch (final UnsupportedOperationException exception) {
      // No auto reorder? --- Never mind!
    }
  }

  @Override
  public void tearDown()
  {
    super.tearDown();
    if (mBDDFactory != null) {
      mBDDFactory.done();
    }
    mNumAutomata = 0;
    mBDDFactory = null;
    mAutomata = null;
    mAutomatonBDDs = null;
    mAutomatonBDDbyVarIndex = null;
    mPeakNodes = 0;
    mTransitionPartitioning = null;
    mLevels = null;
    mCurrentReorderIndex = mNextReorderIndex = -1;
    System.gc();  // Garbage-collect all BDDs
                  // so a new BDDFactory can be created later ...
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
    result.setPeakNumberOfNodes(mPeakNodes);
  }


  //#########################################################################
  //# Debug Output
  public void silentBDDHandler(final Object dummy1, final Object dummy2)
  {
    try {
      checkAbort();
    } catch (final AnalysisException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Algorithm Implementation
  void createAutomatonBDDs()
    throws AnalysisAbortException, OverflowException
  {
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<AutomatonProxy> ordering =
      mVariableOrdering.getOrder(model, translator);
    mNumAutomata = ordering.size();
    mAutomata = new AutomatonProxy[mNumAutomata];
    mAutomatonBDDs = new AutomatonBDD[mNumAutomata];
    mIsFullyDeterministic = true;
    mAutomatonBDDbyVarIndex = null;
    int index = mNumAutomata;
    for (final AutomatonProxy aut : ordering) {
      checkAbort();
      final ComponentKind kind = translator.getComponentKind(aut);
      index--;
      mAutomata[index] = aut;
      final AutomatonBDD autBDD = mAutomatonBDDs[index] =
        new AutomatonBDD(aut, kind, index, mBDDFactory);
      mIsFullyDeterministic &= autBDD.isDeterministic();
    }

    final int numVars = mBDDFactory.varNum();
    mAutomatonBDDbyVarIndex = new AutomatonBDD[numVars];
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      final int first = autBDD.getFirstVariableIndex();
      final int last = autBDD.getLastVariableIndex();
      for (int i = first; i <= last; i++) {
        mAutomatonBDDbyVarIndex[i] = autBDD;
      }
    }

    if (mIsReorderingEnabled) {
      try {
        for (final AutomatonBDD autBDD : mAutomatonBDDs) {
          autBDD.createVarBlocks(mBDDFactory);
        }
      } catch (final UnsupportedOperationException exception) {
        // No variable blocks? --- Better don't reorder ...
        mIsReorderingEnabled = false;
      }
    }
  }

  EventBDD[] createTransitionBDDs()
    throws AnalysisException
  {
    final EventBDD[] eventBDDs = createEventBDDs();
    if (eventBDDs != null) {
      createTransitionBDDs(mPartitioningStrategy, eventBDDs);
    }
    return eventBDDs;
  }

  EventBDD[] createEventBDDs()
    throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    int numEvents = 0;
    for (final EventProxy event : events) {
      switch (translator.getEventKind(event)) {
      case UNCONTROLLABLE:
      case CONTROLLABLE:
        numEvents++;
        break;
      default:
        break;
      }
    }
    final EventBDD[] eventBDDs = new EventBDD[numEvents];
    final Map<EventProxy,EventBDD> eventmap =
      new HashMap<EventProxy,EventBDD>(numEvents);
    int eventindex = 0;
    for (final EventProxy event : events) {
      checkAbort();
      final EventBDD eventBDD = createEventBDD(event);
      if (eventBDD != null) {
        eventmap.put(event, eventBDD);
        eventBDDs[eventindex++] = eventBDD;
      }
    }

    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      checkAbort();
      final AutomatonProxy aut = autBDD.getAutomaton();
      final Collection<EventProxy> localevents = aut.getEvents();
      for (final EventProxy event : localevents) {
        if (isProperEvent(event)) {
          final EventBDD eventBDD = eventmap.get(event);
          eventBDD.startAutomaton(autBDD, mBDDFactory);
        }
      }
      for (final TransitionProxy trans : aut.getTransitions()) {
        final StateProxy source = trans.getSource();
        if (autBDD.isReachable(source)) {
          final EventProxy event = trans.getEvent();
          final EventBDD eventBDD = eventmap.get(event);
          eventBDD.includeTransition(trans, mBDDFactory);
        }
      }
      for (final EventProxy event : localevents) {
        if (isProperEvent(event)) {
          final EventBDD eventBDD = eventmap.get(event);
          eventBDD.finishAutomaton(mBDDFactory);
        }
      }
      mIsFullyDeterministic &= autBDD.isDeterministic();
    }
    final VerificationResult result = getAnalysisResult();
    result.setTotalNumberOfEvents(numEvents);
    if (result.isFinished()) {
      return null;
    }
    return eventBDDs;
  }

  EventBDD createEventBDD(final EventProxy event)
  {
    final KindTranslator translator = getKindTranslator();
    switch (translator.getEventKind(event)) {
    case UNCONTROLLABLE:
      return new UncontrollableEventBDD(event, mNumAutomata, mBDDFactory);
    case CONTROLLABLE:
      return new ControllableEventBDD(event, mNumAutomata, mBDDFactory);
    default:
      return null;
    }
  }

  void createTransitionBDDs(final TransitionPartitioningStrategy strategy,
                            final EventBDD[] eventBDDs)
    throws AnalysisException
  {
    if (mTransitionPartitioning != null) {
      mTransitionPartitioning.disposeComposedBDDs();
    }

    final ProductDESProxy model = getModel();
    mTransitionPartitioning = strategy.createPartitioning
        (mBDDFactory, model, mPartitioningSizeLimit);
    int transcount0 = 0;
    for (final EventBDD eventBDD : eventBDDs) {
      final BDD trans = eventBDD.getTransitionsBDD();
      if (trans != null) {
        final TransitionPartitionBDD part =
          new TransitionPartitionBDD(eventBDD);
        mTransitionPartitioning.add(part);
        transcount0++;
      }
    }
    mTransitionPartitioning.merge(mAutomatonBDDs);
    final List<TransitionPartitionBDD> bdds =
      mTransitionPartitioning.getFullPartition();
    final int transcount1 = bdds.size();
    final Logger logger = getLogger();
    if (logger.isDebugEnabled() && transcount0 > transcount1) {
      logger.debug("Merged transitions: " + transcount0 + " >> " + transcount1);
    }
  }

  BDD createInitialStateBDD(final boolean withLevels)
    throws AnalysisException
  {
    if (mLevels != null) {
      return mLevels.get(0);
    }
    final BDD initial = mBDDFactory.one();
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      checkAbort();
      final BDD autInit = createInitialStateBDD(autBDD);
      if (autInit == null) {
        return null;
      }
      initial.andWith(autInit);
    }
    if (withLevels) {
      mLevels = new ArrayList<BDD>();
      mLevels.add(initial.id());
    }
    mDepth = 0;
    return initial;
  }

  BDD createInitialStateBDD(final AutomatonBDD autBDD)
  {
    final BDD bdd = autBDD.createInitialStateBDD(mBDDFactory);
    if (bdd.isZero()) {
      setSatisfiedResult();
      return null;
    } else {
      mIsFullyDeterministic &= autBDD.isDeterministic(null);
      return bdd;
    }
  }

  BDD getMarkedStateBDD(final EventProxy prop)
    throws AnalysisAbortException, OverflowException
  {
    final BDD result = mBDDFactory.one();
    for (final AutomatonBDD autbdd : mAutomatonBDDs) {
      checkAbort();
      final BDD autmarking = autbdd.getMarkedStateBDD(prop, mBDDFactory);
      if (autmarking == null) {
        // skip
      } else if (autmarking.isZero()) {
        result.free();
        return autmarking;
      } else {
        result.andWith(autmarking);
      }
    }
    return result;
  }

  BDD computeReachability(final BDD init)
    throws AnalysisException
  {
    resetReorderIndex();
    final List<TransitionPartitionBDD> partitioning =
      mTransitionPartitioning.getFullPartition();
    for (final TransitionPartitionBDD part : partitioning) {
      part.buildForwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    mTransitionPartitioning.startIteration();
    final Logger logger = getLogger();
    List<TransitionPartitionBDD> group =
      mTransitionPartitioning.startIteration();
    final boolean multiPart =
      group != null && group.size() < partitioning.size();
    BDD current = multiPart ? init : init.id();
    while (true) {
      checkAbort();
      if (logger.isDebugEnabled()) {
        final int numNodes = mBDDFactory.getNodeNum();
        logger.debug("Depth " + getDepth() + ", " + numNodes + " nodes ...");
      }
      if (containsBadState(current)) {
        recordStateCount(current);
        current.free();
        return null;
      }
      if (group == null) {
        break;
      }
      final BDD next = mBDDFactory.zero();
      for (final TransitionPartitionBDD part : group) {
        checkAbort();
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getCurrentStateCube();
        final BDD nextPart = current.relprod(transpart, cube);
        final BDDPairing renaming = part.getNextToCurrent();
        nextPart.replaceWith(renaming);
        next.orWith(nextPart);
      }
      next.orWith(multiPart ? current.id() : init.id());
      final boolean stable = next.equals(current);
      current.free();
      current = next;
      if (!stable) {
        addLevel(next);
        reorder();
      }
      group = mTransitionPartitioning.nextGroup(stable);
    }
    recordStateCount(current);
    return current;
  }

  private void addLevel(final BDD level)
  {
    mDepth++;
    if (mLevels != null) {
      mLevels.add(level.id());
    }
  }

  BDD computeCoreachability(final BDD endset, final BDD restriction)
    throws AnalysisException
  {
    resetReorderIndex();
    final List<TransitionPartitionBDD> partitioning =
      mTransitionPartitioning.getFullPartition();
    for (final TransitionPartitionBDD part : partitioning) {
      part.buildBackwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    BDD current;
    if (restriction == null) {
      current = endset;
    } else {
      current = endset.and(restriction);
      endset.free();
    }
    final Logger logger = getLogger();
    int level = 0;
    List<TransitionPartitionBDD> group =
      mTransitionPartitioning.startIteration();
    while (group != null) {
      checkAbort();
      if (logger.isDebugEnabled()) {
        final int numNodes = mBDDFactory.getNodeNum();
        logger.debug("Coreachability " + level + ", " +
                     numNodes + " nodes ...");
      }
      if (isCoreachabilityExhausted(current)) {
        current.free();
        return null;
      }
      BDD prev = current.id();
      for (final TransitionPartitionBDD part : group) {
        checkAbort();
        final BDDPairing renaming = part.getCurrentToNext();
        final BDD nextpart = current.replace(renaming);
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getNextStateCube();
        final BDD prevpart = nextpart.relprod(transpart, cube);
        nextpart.free();
        prev.orWith(prevpart);
      }
      if (restriction != null) {
        final BDD tmp = prev.and(restriction);
        prev.free();
        prev = tmp;
      }
      final boolean stable = prev.equals(current);
      current.free();
      current = prev;
      if (!stable) {
        level++;
        reorder();
      }
      group = mTransitionPartitioning.nextGroup(stable);
    }
    return current;
  }

  boolean containsBadState(final BDD reached)
    throws AnalysisAbortException, OverflowException
  {
    return false;
  }

  boolean isCoreachabilityExhausted(final BDD coreached)
  {
    return false;
  }

  List<TraceStepProxy> computeTrace(final BDD target)
    throws AnalysisAbortException, OverflowException
  {
    int lower = 0;
    int upper = mLevels.size() - 1;
    while (lower < upper) {
      checkAbort();
      final int mid = lower + ((upper - lower) >> 1);
      final BDD level = mLevels.get(mid);
      final BDD intersection = level.and(target);
      if (intersection.isZero()) {
        lower = mid + 1;
      } else {
        upper = mid;
      }
      intersection.free();
    }
    final BDD level = mLevels.get(lower);
    target.andWith(level);
    return computeTrace(target, lower);
  }

  List<TraceStepProxy> computeTrace(final BDD target, final int level)
    throws AnalysisAbortException, OverflowException
  {
    final List<TransitionPartitionBDD> partitioning =
      mTransitionPartitioning.getFullPartition();
    for (final TransitionPartitionBDD part : partitioning) {
      part.buildBackwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    final BDDFactory bddFactory = getBDDFactory();
    BDD current = target;
    final Map<AutomatonProxy,StateProxy> statemap =
      new HashMap<AutomatonProxy,StateProxy>(mNumAutomata);
    final List<TraceStepProxy> trace = new LinkedList<TraceStepProxy>();
    final ListIterator<BDD> liter = mLevels.listIterator(level);
    while (liter.hasPrevious()) {
      checkAbort();
      TransitionPartitionBDD foundPart = null;
      BDD foundPrev = bddFactory.zero();
      BDD foundPreds = bddFactory.zero();
      BDD foundPrimed = bddFactory.zero();
      BDD prev = liter.previous();
      parts:
      for (final TransitionPartitionBDD part : partitioning) {
        checkAbort();
        final BDDPairing renaming = part.getCurrentToNext();
        final BDD currentPrimed = current.id();
        currentPrimed.replaceWith(renaming);
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getNextStateCube();
        final BDD preds = currentPrimed.relprod(transpart, cube);
        BDD intersection = preds.and(prev);
        if (!intersection.isZero()) {
          intersection.free();
          foundPrev.free();
          foundPreds.free();
          foundPrimed.free();
          foundPart = part;
          foundPrev = prev;
          foundPreds = preds;
          foundPrimed = currentPrimed;
          if (!mTransitionPartitioning.isStrictBFS()) {
            while (liter.hasPrevious()) {
              checkAbort();
              prev = liter.previous();
              intersection = preds.and(prev);
              if (intersection.isZero()) {
                continue parts;
              }
              intersection.free();
              foundPrev.free();
              foundPrev = prev;
            }
          }
          break;
        }
        preds.free();
        currentPrimed.free();
      }
      final BDD preds = createTraceStep(foundPart, foundPrev, current,
                                        foundPreds, foundPrimed, trace);
      if (foundPrev != prev) {
        liter.next();
      }
      foundPrev.free();
      current.free();
      current = preds;
    }
    if (!isDeterministic(null)) {
      getDeterminisedInitialStateMap(current, statemap);
    }
    final ProductDESProxyFactory desFactory = getFactory();
    final TraceStepProxy step = desFactory.createTraceStepProxy(null, statemap);
    trace.add(0, step);
    return trace;
  }

  BDD createTraceStep(final TransitionPartitionBDD part,
                      final BDD source,
                      final BDD target,
                      BDD preds,
                      BDD targetPrimed,
                      final List<TraceStepProxy> trace)
    throws AnalysisAbortException, OverflowException
  {
    final Map<EventProxy,TransitionPartitionBDD> map =
      part.getTransitionComponents();
    final Iterator<Map.Entry<EventProxy,TransitionPartitionBDD>> iter =
      map.entrySet().iterator();
    Map.Entry<EventProxy,TransitionPartitionBDD> entry = iter.next();
    if (iter.hasNext()) {
      while (true) {
        checkAbort();
        preds.free();
        targetPrimed.free();
        final TransitionPartitionBDD subpart = entry.getValue();
        final BDDPairing subrenaming = subpart.getCurrentToNext();
        targetPrimed = target.id();
        targetPrimed.replaceWith(subrenaming);
        final BDD subtranspart = subpart.getBDD();
        final BDDVarSet subcube = subpart.getNextStateCube();
        preds = targetPrimed.relprod(subtranspart, subcube);
        preds.andWith(source.id());
        if (!preds.isZero()) {
          break;
        }
        entry = iter.next();
      }
    }
    final EventProxy event = entry.getKey();
    final Map<AutomatonProxy,StateProxy> statemap;
    if (isDeterministic(event)) {
      statemap = null;
      targetPrimed.free();
    } else {
      preds.free();
      final TransitionPartitionBDD subpart = entry.getValue();
      statemap = new HashMap<AutomatonProxy,StateProxy>(mNumAutomata);
      preds = getDeterminisedPredecessorsBDD
        (source, targetPrimed, event, subpart, statemap);
    }
    final ProductDESProxyFactory factory = getFactory();
    final TraceStepProxy step = factory.createTraceStepProxy(event, statemap);
    trace.add(0, step);
    return preds;
  }

  void resetReorderIndex()
  {
    if (mIsReorderingEnabled && mBDDPackage.isReorderingSupported()) {
      mCurrentReorderIndex = mNextReorderIndex = START_REORDER_INDEX;
    }
  }

  void reorder()
  {
    if (mCurrentReorderIndex >= 0) {
      if (--mCurrentReorderIndex == 0) {
        // BuDDy used to break when reorder() was called with 0 vars :-(
        // if (mBDDFactory.varNum() > 0) {
        mBDDFactory.reorder(BDDFactory.REORDER_SIFT);
        mNextReorderIndex <<= 1;
        mCurrentReorderIndex = mNextReorderIndex;
      }
    }
  }

  //#########################################################################
  //# Simple Access
  BDDFactory getBDDFactory()
  {
    return mBDDFactory;
  }

  List<AutomatonProxy> getAutomata()
  {
    return Arrays.asList(mAutomata);
  }

  AutomatonBDD[] getAutomatonBDDs()
  {
    return mAutomatonBDDs;
  }

  Partitioning<TransitionPartitionBDD> getTransitionPartitioning()
  {
    return mTransitionPartitioning;
  }

  int getDepth()
  {
    return mDepth;
  }


  //#########################################################################
  //# Determinisation
  private boolean isDeterministic(final EventProxy event)
  {
    if (!mIsFullyDeterministic) {
      for (final AutomatonBDD autBDD : mAutomatonBDDs) {
        if (!autBDD.isDeterministic(event)) {
          return false;
        }
      }
    }
    return true;
  }

  private BDD getDeterminisedPredecessorsBDD
    (final BDD source,
     final BDD currentPrimed,
     final EventProxy event,
     final TransitionPartitionBDD part,
     final Map<AutomatonProxy,StateProxy> statemap)
  {
    final BDDVarSet detNextStateCube = mBDDFactory.emptySet();
    final BDDVarSet nondetNextStateCube = mBDDFactory.emptySet();
    final int numvars = mBDDFactory.varNum();
    for (int l = numvars - 1; l >= 0; l--) {
      final int varindex = mBDDFactory.level2Var(l);
      final AutomatonBDD autBDD = mAutomatonBDDbyVarIndex[varindex];
      if (part.dependsOn(autBDD) && autBDD.isNextStateVariable(varindex)) {
        if (autBDD.isDeterministic(event)) {
          detNextStateCube.unionWith(varindex);
        } else {
          nondetNextStateCube.unionWith(varindex);
        }
      }
    }
    final BDD partBDD = part.getBDD();
    final BDD currentPrimedRestricted =
      currentPrimed.relprod(partBDD, detNextStateCube);
    currentPrimed.free();
    detNextStateCube.free();
    currentPrimedRestricted.andWith(source.id());

    final TObjectIntHashMap<AutomatonBDD> codemap =
      new TObjectIntHashMap<AutomatonBDD>(mNumAutomata);
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      if (!autBDD.isDeterministic(event)) {
        codemap.put(autBDD, 0);
      }
    }
    BDD bdd = currentPrimedRestricted;
    while (!bdd.isOne()) {
      final int varindex = bdd.var();
      final BDD low = bdd.low();
      final BDD high = bdd.high();
      if (low.isZero()) {
        bdd = high;
      } else if (high.isZero()) {
        bdd = low;
      } else if (low.level() < high.level()) {
        bdd = high;
      } else {
        bdd = low;
      }
      if (bdd == high) {
        final AutomatonBDD autBDD = mAutomatonBDDbyVarIndex[varindex];
        if (autBDD.isNextStateVariable(varindex) &&
            !autBDD.isDeterministic(event)) {
          final int bitindex = autBDD.getBitIndex(varindex);
          final int code = codemap.get(autBDD) | (1 << bitindex);
          codemap.put(autBDD, code);
        }
      }
    }
    final TObjectIntIterator<AutomatonBDD> iter = codemap.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final AutomatonBDD autBDD = iter.key();
      final AutomatonProxy aut = autBDD.getAutomaton();
      final int code = iter.value();
      final StateProxy state = autBDD.getState(code);
      statemap.put(aut, state);
    }

    final BDD sat1 = mBDDFactory.one();
    for (int l = numvars - 1; l >= 0; l--) {
      final int varindex = mBDDFactory.level2Var(l);
      final AutomatonBDD autBDD = mAutomatonBDDbyVarIndex[varindex];
      final AutomatonProxy aut = autBDD.getAutomaton();
      final StateProxy state = statemap.get(aut);
      if (state != null && autBDD.isNextStateVariable(varindex)) {
        final int bitindex = autBDD.getBitIndex(varindex);
        final BDD bitBDD =
          autBDD.getNextStateBitBDD(state, bitindex, mBDDFactory);
        if (bitBDD != null) {
          sat1.andWith(bitBDD);
        }
      }
    }

    final BDD result =
      currentPrimedRestricted.relprod(sat1, nondetNextStateCube);
    currentPrimedRestricted.free();
    sat1.free();
    nondetNextStateCube.free();
    return result;
  }

  private void getDeterminisedInitialStateMap
    (final BDD stateset,
     final Map<AutomatonProxy,StateProxy> statemap)
  {
    final TObjectIntHashMap<AutomatonBDD> codemap =
      new TObjectIntHashMap<AutomatonBDD>(mNumAutomata);
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      if (!autBDD.isDeterministic(null)) {
        codemap.put(autBDD, 0);
      }
    }
    BDD bdd = stateset;
    while (!bdd.isOne()) {
      final int varindex = bdd.var();
      final BDD low = bdd.low();
      final BDD high = bdd.high();
      if (low.isZero()) {
        bdd = high;
      } else if (high.isZero()) {
        bdd = low;
      } else if (low.level() < high.level()) {
        bdd = high;
      } else {
        bdd = low;
      }
      if (bdd == high) {
        final AutomatonBDD autBDD = mAutomatonBDDbyVarIndex[varindex];
        if (!autBDD.isDeterministic(null)) {
          final int bitindex = autBDD.getBitIndex(varindex);
          final int code = codemap.get(autBDD) | (1 << bitindex);
          codemap.put(autBDD, code);
        }
      }
    }
    final TObjectIntIterator<AutomatonBDD> iter = codemap.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final AutomatonBDD autBDD = iter.key();
      final AutomatonProxy aut = autBDD.getAutomaton();
      final int code = iter.value();
      final StateProxy state = autBDD.getState(code);
      statemap.put(aut, state);
    }
  }


  //#########################################################################
  //# Statistics
  private void recordStateCount(final BDD bdd)
  {
    final VerificationResult result = getAnalysisResult();
    if (result.getTotalNumberOfStates() < 0.0) {
      final BDDStateCountRecursion counter =
        new BDDStateCountRecursion(mBDDFactory, mAutomatonBDDbyVarIndex);
      final double count = counter.count(bdd);
      result.setNumberOfStates(count);
    }
  }


  //#########################################################################
  //# Debugging
  @SuppressWarnings("unused")
  private void showOrder(final Collection<AutomatonProxy> ordering)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("AUTOMATON ORDERING:");
      for (final AutomatonProxy aut : ordering) {
        logger.debug(aut.getName());
      }
      logger.debug("(end of ordering)");
    }
  }


  //#########################################################################
  //# Data Members
  private BDDPackage mBDDPackage;
  private VariableOrdering mVariableOrdering;
  private int mInitialSize;
  private TransitionPartitioningStrategy mPartitioningStrategy;
  private int mPartitioningSizeLimit;
  private boolean mIsReorderingEnabled;

  private int mNumAutomata;
  private BDDFactory mBDDFactory;
  private AutomatonProxy[] mAutomata;
  private AutomatonBDD[] mAutomatonBDDs;
  private boolean mIsFullyDeterministic;
  private AutomatonBDD[] mAutomatonBDDbyVarIndex;
  private int mPeakNodes;
  private Partitioning<TransitionPartitionBDD> mTransitionPartitioning;
  private List<BDD> mLevels;
  private int mDepth;
  private int mCurrentReorderIndex;
  private int mNextReorderIndex;


  //#########################################################################
  //# Class Constants
  private static final BDDPackage BDD_PACKAGE = BDDPackage.CUDD;
  private static final int CACHE_RATIO = 10;
  private static final int START_REORDER_INDEX = 8;

}
