//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDSafetyVerifier
//###########################################################################
//# $Id: BDDSafetyVerifier.java,v 1.1 2007-11-02 00:30:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
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
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.apache.log4j.Logger;

/**
 * <P>A BDD implementation of a basic safety verifier.</P>
 *
 * @author Robi Malik
 */

public class BDDSafetyVerifier
  extends AbstractModelVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDSafetyVerifier(final KindTranslator translator,
                           final ProductDESProxyFactory factory)
  {
    this(null, translator, factory);
  }

  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD packe to be used.
   */
  public BDDSafetyVerifier(final KindTranslator translator,
                           final ProductDESProxyFactory desfactory,
                           final String bddpackage)
  {
    this(null, translator, desfactory, bddpackage);
  }

  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  factory     The factory used for trace construction.
   */
  public BDDSafetyVerifier(final ProductDESProxy model,
                           final KindTranslator translator,
                           final ProductDESProxyFactory factory)
  {
    this(model, translator, factory, BDD_PACKAGE);
  }

  /**
   * Creates a new BDD-based safety verifier to check a particular model.
   * @param  model       The model to be checked by this verifier.
   * @param  translator  The kind translator is used to remap component and
   *                     event kinds.
   * @param  desfactory  The factory used for trace construction.
   * @param  bddpackage  The name of the BDD packe to be used.
   */
  public BDDSafetyVerifier(final ProductDESProxy model,
                           final KindTranslator translator,
                           final ProductDESProxyFactory desfactory,
                           final String bddpackage)
  {
    super(model, desfactory);
    mKindTranslator = translator;
    mBDDPackage = bddpackage;
    mIsReorderingEnabled =
      bddpackage.equals("buddy") || bddpackage.equals("cudd");
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws OverflowException
  {
    LOGGER.debug("BDDSafetyVerifier.run(): " +
                 getModel().getName() + " ...");
    try {
      cleanup();
      createAutomatonBDDs();
      if (getAnalysisResult() != null) {
        return isSatisfied();
      }
      createEventBDDs();
      if (getAnalysisResult() != null) {
        return isSatisfied();
      }
      final boolean controllable = computeFixedPoint();
      if (controllable) {
        setSatisfiedResult();
      } else {
        computeCounterExample();
      }
      LOGGER.debug("result=" + controllable);
      return controllable;
    } finally {
      cleanup();
      LOGGER.debug("BDDSafetyVerifier.run(): " +
                   getModel().getName() + " done.");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyVerifier
  public void setKindTranslator(KindTranslator translator)
  {
    mKindTranslator = translator;
    clearAnalysisResult();
  }

  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# BDD Specific Options
  public void setBDDPackage(final String name)
  {
    mBDDPackage = name;
  }

  public String getBDDPackage()
  {
    return mBDDPackage;
  }


  //#########################################################################
  //# Setting the Result
  protected void addStatistics(final VerificationResult result)
  {
    result.setNumberOfAutomata(mNumAutomata);
    result.setPeakNumberOfNodes(mPeakNodes);
  }


  //#########################################################################
  //# Algorithm Implementation
  private void cleanup()
  {
    if (mBDDFactory != null) {
      mBDDFactory.done();
    }
    mNumAutomata = 0;
    mBDDFactory = null;
    mAutomatonBDDs = null;
    mPeakNodes = 0;
    mConditionBDDs = null;
    mTransitionBDDs = null;
    mBadStateBDD = null;
    mLevels = null;
    mNextReorderIndex = Integer.MAX_VALUE;
    mBadEvent = null;
  }

  private void createAutomatonBDDs()
  {
    final ProductDESProxy model = getModel();
    final VariableOrdering ordering =
      new VariableOrdering(model, mKindTranslator);
    if (ordering.getNumSpecs() == 0) {
      setSatisfiedResult();
      return;
    }
    final int initnodes = 10000; // breaks BuDDy at 57600 ???
    mBDDFactory = BDDFactory.init(mBDDPackage, initnodes, initnodes >> 1);
    try {
      mBDDFactory.disableReorder();
    } catch (final UnsupportedOperationException exception) {
      // No auto reorder? --- Never mind!
    }
    mNumAutomata = ordering.getNumAutomata();
    mAutomatonBDDs = new AutomatonBDD[mNumAutomata];
    int index = mNumAutomata;
    for (final AutomatonProxy aut : ordering) {
      final ComponentKind kind = mKindTranslator.getComponentKind(aut);
      index--;
      mAutomatonBDDs[index] = new AutomatonBDD(aut, kind, index, mBDDFactory);
    }
    if (mIsReorderingEnabled) {
      try {
        for (final AutomatonBDD autBDD : mAutomatonBDDs) {
          autBDD.createVarBlocks(mBDDFactory);
        }
        mNextReorderIndex = 8;
      } catch (final UnsupportedOperationException exception) {
        // No variable blocks? --- Better don't reorder ...
      }
    }
  }

  private void createEventBDDs()
  {
    final ProductDESProxy model = getModel();
    final Collection<EventProxy> events = model.getEvents();
    int numevents = 0;
    int numuncont = 0;
    for (final EventProxy event : events) {
      switch (mKindTranslator.getEventKind(event)) {
      case UNCONTROLLABLE:
        numuncont++;
        // fall through ...
      case CONTROLLABLE:
        numevents++;
        // fall through ...
      default:
        break;
      }
    }
    if (numuncont == 0) {
      setSatisfiedResult();
      return;
    }
    final EventBDD[] eventBDDs = new EventBDD[numevents];
    final Map<EventProxy,EventBDD> eventmap =
      new HashMap<EventProxy,EventBDD>(numevents);
    int eventindex = 0;
    for (final EventProxy event : events) {
      final EventBDD eventBDD;
      switch (mKindTranslator.getEventKind(event)) {
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
    mLevels = new LinkedList<BDD>();
    mLevels.add(initial);
    for (final AutomatonBDD autBDD : mAutomatonBDDs) {
      final BDD autinit = autBDD.getInitialStateBDD(mBDDFactory);
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
        final EventProxy event = trans.getEvent();
        final EventBDD eventBDD = eventmap.get(event);
        eventBDD.includeTransition(trans, mBDDFactory);
      }
      for (final EventBDD eventBDD : eventBDDs) {
        eventBDD.finishAutomaton(mBDDFactory);
      }
    }

    final Partitioning<ConditionPartitionBDD> condPartitioning = 
      new Partitioning<ConditionPartitionBDD>(ConditionPartitionBDD.class);
    final Partitioning<TransitionPartitionBDD> transPartitioning = 
      new Partitioning<TransitionPartitionBDD>(TransitionPartitionBDD.class);
    int condcount = 0;
    int transcount = 0;
    for (final EventBDD eventBDD : eventBDDs) {
      final BDD cond = eventBDD.getControllabilityConditionBDD();
      if (cond != null) {
        final ConditionPartitionBDD part = new ConditionPartitionBDD(eventBDD);
        condPartitioning.add(part);
        condcount++;
      }
      final BDD trans = eventBDD.getTransitionsBDD();
      if (trans != null) {
        final TransitionPartitionBDD part =
          new TransitionPartitionBDD(eventBDD);
        transPartitioning.add(part);
        transcount++;
      }
    }

    final Collection<ConditionPartitionBDD> conditions =
      condPartitioning.mergePartitions(mAutomatonBDDs, mBDDFactory);
    mConditionBDDs = new ArrayList<ConditionPartitionBDD>(conditions);
    LOGGER.debug("Merged conditions: " + condcount +
                 " >> " + mConditionBDDs.size());
    final Collection<TransitionPartitionBDD> transitions =
      transPartitioning.mergePartitions(mAutomatonBDDs, mBDDFactory);
    mTransitionBDDs = new ArrayList<TransitionPartitionBDD>(transitions);
    LOGGER.debug("Merged transitions: " + transcount +
                 " >> " + mTransitionBDDs.size());
  }

  private boolean computeFixedPoint()
    throws OverflowException
  {
    for (final TransitionPartitionBDD trans : mTransitionBDDs) {
      trans.buildForwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    BDD current = mLevels.iterator().next(); // initial state
    outer:
    do {
      final int numnodes = mBDDFactory.getNodeNum();
      LOGGER.debug("Depth " + mLevels.size() + ", " + numnodes + " nodes ...");
      if (numnodes > mPeakNodes) {
        mPeakNodes = numnodes;
        if (numnodes > getNodeLimit()) {
          throw new OverflowException(getNodeLimit());
        }
      }
      for (final ConditionPartitionBDD part : mConditionBDDs) {
        BDD condpart = part.getBDD();
        BDD imp = current.imp(condpart);
        if (!imp.isOne()) {
          final Map<EventProxy,PartitionBDD> map = part.getComponents();
          final Iterator<Map.Entry<EventProxy,PartitionBDD>> iter =
            map.entrySet().iterator();
          Map.Entry<EventProxy,PartitionBDD> entry = iter.next();
          if (iter.hasNext()) {
            while (true) {
              imp.free();
              condpart = entry.getValue().getBDD();
              imp = current.imp(condpart);
              if (!imp.isOne()) {
                break;
              }
              entry = iter.next();
            }
          }
          mBadEvent = entry.getKey();
          mBadStateBDD = imp.not();
          imp.free();
          break outer;
        }
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
      if (mLevels.size() == mNextReorderIndex) {
        reorder();
        mNextReorderIndex <<= 1;
      }
      current = next;
    } while (true);
    current.free();
    return mBadEvent == null;
  }

  private SafetyTraceProxy computeCounterExample()
  {
    for (final PartitionBDD part : mConditionBDDs) {
      part.dispose();
    }
    for (final TransitionPartitionBDD trans : mTransitionBDDs) {
      trans.buildBackwardCubes(mAutomatonBDDs, mBDDFactory);
    }
    BDD current = mBadStateBDD;
    final List<EventProxy> trace = new LinkedList<EventProxy>();
    final int depth = mLevels.size();
    final ListIterator<BDD> liter = mLevels.listIterator(depth - 1);
    while (liter.hasPrevious()) {
      final BDD prev = liter.previous();
      for (final TransitionPartitionBDD part : mTransitionBDDs) {
        final BDDPairing renaming = part.getCurrentToNext();
        final BDD current1 = current.id();
        current1.replaceWith(renaming);
        final BDD transpart = part.getBDD();
        final BDDVarSet cube = part.getNextStateCube();
        BDD preds = current1.relprod(transpart, cube);
        current1.free();
        preds.andWith(prev.id());
        if (!preds.isZero()) {
          final Map<EventProxy,TransitionPartitionBDD> map =
            part.getTransitionComponents();
          final Iterator<Map.Entry<EventProxy,TransitionPartitionBDD>> iter =
            map.entrySet().iterator();
          Map.Entry<EventProxy,TransitionPartitionBDD> entry = iter.next();
          if (iter.hasNext()) {
            while (true) {
              preds.free();
              final TransitionPartitionBDD subpart = entry.getValue();
              final BDDPairing subrenaming = subpart.getCurrentToNext();
              final BDD subcurrent1 = current.id();
              subcurrent1.replaceWith(subrenaming);
              final BDD subtranspart = subpart.getBDD();
              final BDDVarSet subcube = subpart.getNextStateCube();
              preds = subcurrent1.relprod(subtranspart, subcube);
              subcurrent1.free();
              preds.andWith(prev.id());
              if (!preds.isZero()) {
                break;
              }
              entry = iter.next();
            }
          }
          current.free();
          current = preds;
          final EventProxy event = entry.getKey();
          trace.add(0, event);
          break;
        }
      }
      prev.free();
    }
    trace.add(mBadEvent);
    final ProductDESProxyFactory desfactory = getFactory();
    final ProductDESProxy des = getModel();
    final SafetyTraceProxy counterex =
      desfactory.createSafetyTraceProxy(des, trace);
    setFailedResult(counterex);
    return counterex;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void reorder()
  {
    // BuDDy used to break when reorder() was called with 0 vars :-(
    // if (mIsReorderingEnabled && mBDDFactory.varNum() > 0) {
    if (mIsReorderingEnabled) {
      mBDDFactory.reorder(BDDFactory.REORDER_SIFT);
    }
  }


  //#########################################################################
  //# Data Members
  private KindTranslator mKindTranslator;
  private String mBDDPackage;
  private boolean mIsReorderingEnabled;

  private int mNumAutomata;
  private BDDFactory mBDDFactory;
  private AutomatonBDD[] mAutomatonBDDs;
  private int mPeakNodes;
  private List<ConditionPartitionBDD> mConditionBDDs;
  private List<TransitionPartitionBDD> mTransitionBDDs;
  private BDD mBadStateBDD;
  private List<BDD> mLevels;
  private int mNextReorderIndex;
  private EventProxy mBadEvent;

  private static final String BDD_PACKAGE = "cudd";

  private static final Logger LOGGER =
    Logger.getLogger(BDDSafetyVerifier.class);

}
