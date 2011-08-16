//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

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
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.EventNotFoundException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.NondeterministicDESException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.VerificationResult;
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

/**
 * An abstract base class for all BDD-based model verifiers.</P>
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
    mPartitioningSizeLimit = Integer.MAX_VALUE;
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

  public VariableOrdering getVariableOrdering()
  {
    return mVariableOrdering;
  }

  public void setVariableOrdering(final VariableOrdering ordering)
  {
    mVariableOrdering = ordering;
  }

  public boolean isReorderingEnabled()
  {
    return mIsReorderingEnabled;
  }

  public void setReorderingEnabled(final boolean enable)
  {
    mIsReorderingEnabled = enable;
  }

  public int getPartitioningSizeLimit()
  {
    return mPartitioningSizeLimit;
  }

  public void setPartitioningSizeLimit(final int limit)
  {
    mPartitioningSizeLimit = limit < 0 ? Integer.MAX_VALUE : limit;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelVerifier
  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    clearAnalysisResult();
  }


  //#########################################################################
  //# Overrides for Algorithms
  @Override
  public void setUp() throws AnalysisException
  {
    super.setUp();
    final int initnodes = 10000; // breaks BuDDy at 57600 ???
    final String name = mBDDPackage.getBDDPackageName();
    mBDDFactory = BDDFactory.init(name, initnodes, initnodes >> 1);
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
    mTransitionBDDs = null;
    mLevels = null;
    mCurrentReorderIndex = mNextReorderIndex = -1;
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
  }


  //#########################################################################
  //# Algorithm Implementation
  void createAutomatonBDDs()
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
      final ComponentKind kind = translator.getComponentKind(aut);
      index--;
      mAutomata[index] = aut;
      final AutomatonBDD autBDD = mAutomatonBDDs[index] =
        new AutomatonBDD(aut, kind, index, mBDDFactory);
      mIsFullyDeterministic &= autBDD.isDeterministic();
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

  EventBDD[] createEventBDDs()
    throws EventNotFoundException, NondeterministicDESException
  {
    final ProductDESProxy model = getModel();
    final KindTranslator translator = getKindTranslator();
    final Collection<EventProxy> events = model.getEvents();
    int numevents = 0;
    for (final EventProxy event : events) {
      switch (translator.getEventKind(event)) {
      case UNCONTROLLABLE:
      case CONTROLLABLE:
        numevents++;
        break;
      default:
        break;
      }
    }
    final EventBDD[] eventBDDs = new EventBDD[numevents];
    final Map<EventProxy,EventBDD> eventmap =
      new HashMap<EventProxy,EventBDD>(numevents);
    int eventindex = 0;
    for (final EventProxy event : events) {
      final EventBDD eventBDD;
      switch (translator.getEventKind(event)) {
      case UNCONTROLLABLE:
        eventBDD =
          new UncontrollableEventBDD(event, mNumAutomata, mBDDFactory);
        break;
      case CONTROLLABLE:
        eventBDD =
          new ControllableEventBDD(event, mNumAutomata, mBDDFactory);
        break;
      default:
        continue;
      }
      eventmap.put(event, eventBDD);
      eventBDDs[eventindex++] = eventBDD;
    }

    final BDD initial = mBDDFactory.one();
    mLevels = new ArrayList<BDD>();
    mLevels.add(initial);
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      final BDD autinit = autBDD.getInitialStateBDD(mBDDFactory);
      if (autinit.isZero()) {
        setSatisfiedResult();
        return null;
      }
      initial.andWith(autinit);
      final AutomatonProxy aut = autBDD.getAutomaton();
      final Collection<EventProxy> localevents = aut.getEvents();
      for (final EventBDD eventBDD : eventBDDs) {
        final EventProxy event = eventBDD.getEvent();
        if (localevents.contains(event)) {
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
      for (final EventBDD eventBDD : eventBDDs) {
        eventBDD.finishAutomaton(mBDDFactory);
      }
      mIsFullyDeterministic &= autBDD.isDeterministic();
    }

    final int numvars = mBDDFactory.varNum();
    mAutomatonBDDbyVarIndex = new AutomatonBDD[numvars];
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      final int first = autBDD.getFirstVariableIndex();
      final int last = autBDD.getLastVariableIndex();
      for (int i = first; i <= last; i++) {
        mAutomatonBDDbyVarIndex[i] = autBDD;
      }
    }

    final Partitioning<TransitionPartitionBDD> transPartitioning =
      new Partitioning<TransitionPartitionBDD>(TransitionPartitionBDD.class);
    int transcount0 = 0;
    for (final EventBDD eventBDD : eventBDDs) {
      final BDD trans = eventBDD.getTransitionsBDD();
      if (trans != null) {
        final TransitionPartitionBDD part =
          new TransitionPartitionBDD(eventBDD);
        transPartitioning.add(part);
        transcount0++;
      }
    }
    final Collection<TransitionPartitionBDD> transitions =
      transPartitioning.mergePartitions(mAutomatonBDDs, mBDDFactory,
                                        mPartitioningSizeLimit);
    mTransitionBDDs = new ArrayList<TransitionPartitionBDD>(transitions);
    final int transcount1 = mTransitionBDDs.size();
    final Logger logger = getLogger();
    if (logger.isDebugEnabled() && transcount0 > transcount1) {
      logger.debug("Merged transitions: " + transcount0 + " >> " + transcount1);
    }
    return eventBDDs;
  }

  BDD getInitialStateBDD()
  {
    return mLevels.iterator().next();
  }

  BDD getMarkedStateBDD(final EventProxy prop)
  {
    final BDD result = mBDDFactory.one();
    for (final AutomatonBDD autbdd : mAutomatonBDDs) {
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

  BDD computeReachability()
    throws OverflowException
  {
    resetReorderIndex();
    for (final TransitionPartitionBDD trans : mTransitionBDDs) {
      trans.buildForwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    final Logger logger = getLogger();
    BDD current = getInitialStateBDD();
    do {
      final int numnodes = mBDDFactory.getNodeNum();
      if (logger.isDebugEnabled()) {
        logger.debug("Depth " + mLevels.size() + ", " +
                     numnodes + " nodes ...");
      }
      if (numnodes > mPeakNodes) {
        mPeakNodes = numnodes;
        if (numnodes > getNodeLimit()) {
          throw new OverflowException(OverflowKind.NODE, getNodeLimit());
        }
      }
      if (containsBadState(current)) {
        recordStateCount(current);
        current.free();
        return null;
      }
      final BDD next = current.id();
      for (final TransitionPartitionBDD part : mTransitionBDDs) {
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getCurrentStateCube();
        final BDD nextpart = current.relprod(transpart, cube);
        final BDDPairing renaming = part.getNextToCurrent();
        nextpart.replaceWith(renaming);
        next.orWith(nextpart);
      }
      if (next.equals(current)) {
        break;
      }
      mLevels.add(next);
      current = next;
      reorder();
    } while (true);
    recordStateCount(current);
    return current;
  }

  BDD computeCoreachability(final BDD endset, final BDD restriction)
    throws OverflowException
  {
    resetReorderIndex();
    for (final TransitionPartitionBDD trans : mTransitionBDDs) {
      trans.buildBackwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    BDD current;
    if (restriction == null) {
      current = endset;
    } else {
      current = endset.and(restriction);
      endset.free();
    }
    int level = 0;
    final Logger logger = getLogger();
    do {
      final int numnodes = mBDDFactory.getNodeNum();
      if (logger.isDebugEnabled()) {
        logger.debug("Coreachability " + (level++) + ", " +
                     numnodes + " nodes ...");
      }
      if (numnodes > mPeakNodes) {
        mPeakNodes = numnodes;
        if (numnodes > getNodeLimit()) {
          throw new OverflowException(OverflowKind.NODE, getNodeLimit());
        }
      }
      if (isCoreachabilityExhausted(current)) {
        current.free();
        return null;
      }
      BDD prev = current.id();
      for (final TransitionPartitionBDD part : mTransitionBDDs) {
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
      if (prev.equals(current)) {
        prev.free();
        break;
      }
      current.free();
      current = prev;
      reorder();
    } while (true);
    return current;
  }

  boolean containsBadState(final BDD reached)
  {
    return false;
  }

  boolean isCoreachabilityExhausted(final BDD coreached)
  {
    return false;
  }

  List<TraceStepProxy> computeTrace(final BDD target)
  {
    int lower = 0;
    int upper = mLevels.size() - 1;
    while (lower < upper) {
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
  {
    for (final TransitionPartitionBDD trans : mTransitionBDDs) {
      trans.buildBackwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    BDD current = target;
    final ProductDESProxyFactory factory = getFactory();
    final Map<AutomatonProxy,StateProxy> statemap =
      new HashMap<AutomatonProxy,StateProxy>(mNumAutomata);
    final List<TraceStepProxy> trace = new LinkedList<TraceStepProxy>();
    final ListIterator<BDD> liter = mLevels.listIterator(level);
    while (liter.hasPrevious()) {
      final BDD prev = liter.previous();
      for (final TransitionPartitionBDD part : mTransitionBDDs) {
        final BDDPairing renaming = part.getCurrentToNext();
        BDD currentPrimed = current.id();
        currentPrimed.replaceWith(renaming);
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getNextStateCube();
        BDD preds = currentPrimed.relprod(transpart, cube);
        preds.andWith(prev.id());
        if (preds.isZero()) {
          currentPrimed.free();
        } else {
          final Map<EventProxy,TransitionPartitionBDD> map =
            part.getTransitionComponents();
          final Iterator<Map.Entry<EventProxy,TransitionPartitionBDD>> iter =
            map.entrySet().iterator();
          Map.Entry<EventProxy,TransitionPartitionBDD> entry = iter.next();
          if (iter.hasNext()) {
            while (true) {
              currentPrimed.free();
              preds.free();
              final TransitionPartitionBDD subpart = entry.getValue();
              final BDDPairing subrenaming = subpart.getCurrentToNext();
              currentPrimed = current.id();
              currentPrimed.replaceWith(subrenaming);
              final BDD subtranspart = subpart.getBDD();
              final BDDVarSet subcube = subpart.getNextStateCube();
              preds = currentPrimed.relprod(subtranspart, subcube);
              preds.andWith(prev.id());
              if (!preds.isZero()) {
                break;
              }
              entry = iter.next();
            }
          }
          current.free();
          final EventProxy event = entry.getKey();
          if (isDeterministic(event)) {
            currentPrimed.free();
          } else {
            preds.free();
            final TransitionPartitionBDD subpart = entry.getValue();
            preds = getDeterminisedPredecessorsBDD
              (currentPrimed, event, subpart, statemap);
            preds.andWith(prev.id());
          }
          final TraceStepProxy step =
            factory.createTraceStepProxy(event, statemap);
          trace.add(0, step);
          statemap.clear();
          current = preds;
          break;
        }
      }
      prev.free();
    }
    if (!isDeterministic(null)) {
      getDeterminisedInitialStateMap(current, statemap);
    }
    final TraceStepProxy step = factory.createTraceStepProxy(null, statemap);
    trace.add(0, step);
    return trace;
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

  List<TransitionPartitionBDD> getTransitionBDDs()
  {
    return mTransitionBDDs;
  }

  int getDepth()
  {
    return mLevels.size();
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
    (final BDD currentPrimed,
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
    final BDDStateCounter counter =
      new BDDStateCounter(mBDDFactory, mAutomatonBDDbyVarIndex);
    final double count = counter.count(bdd);
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(count);
  }


  //#########################################################################
  //# Data Members
  private BDDPackage mBDDPackage;
  private VariableOrdering mVariableOrdering;
  private boolean mIsReorderingEnabled;
  private int mPartitioningSizeLimit;

  private int mNumAutomata;
  private BDDFactory mBDDFactory;
  private AutomatonProxy[] mAutomata;
  private AutomatonBDD[] mAutomatonBDDs;
  private boolean mIsFullyDeterministic;
  private AutomatonBDD[] mAutomatonBDDbyVarIndex;
  private int mPeakNodes;
  private List<TransitionPartitionBDD> mTransitionBDDs;
  private List<BDD> mLevels;
  private int mCurrentReorderIndex;
  private int mNextReorderIndex;


  //#########################################################################
  //# Class Constants
  private static final BDDPackage BDD_PACKAGE = BDDPackage.CUDD;
  private static final int START_REORDER_INDEX = 8;

}
