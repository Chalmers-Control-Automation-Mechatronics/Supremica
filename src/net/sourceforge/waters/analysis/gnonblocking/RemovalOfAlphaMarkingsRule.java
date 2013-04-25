//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Rachel Francis
 */

class RemovalOfAlphaMarkingsRule extends AbstractionRule
{

  //#######################################################################
  //# Constructors
  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                             final KindTranslator translator,
                             final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions);
    mIsAborting = false;
  }


  //#######################################################################
  //# Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }


  //#######################################################################
  // # Rule Application
  @Override
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
    throws AbortException
  {
    mAutToAbstract = autToAbstract;
    mTR =
        new OldTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }
    final int numStates = mTR.getNumberOfStates();
    boolean modified = false;
    int alphaID = mTR.getEventInt(mAlphaMarking);
    if (alphaID == -1) {
      alphaID = mTR.addProposition(mAlphaMarking, true);
    }

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntArrayStack();

    // performs a backwards search to remove alpha markings from states which
    // satisfy the rule conditions
    for (int stateID = 0; stateID < numStates; stateID++) {
      checkAbort();
      if (mTR.isMarked(stateID, alphaID)) {
        unvisitedStates.push(stateID);
        while (unvisitedStates.size() > 0) {
          final int newState = unvisitedStates.pop();
          final TIntHashSet predeccessors =
              mTR.getPredecessors(newState, tauID);
          if (predeccessors != null) {
            final TIntIterator iter = predeccessors.iterator();
            while (iter.hasNext()) {
              final int predID = iter.next();
              if (predID != stateID && predID != newState) {
                if (reachableStates.add(predID)) {
                  unvisitedStates.push(predID);
                }
                if (mTR.isMarked(predID, alphaID)) {
                  mTR.markState(predID, alphaID, false);
                  modified = true;
                }
              }
            }
          }
        }
      }
      reachableStates.clear();
    }
    if (modified) {
      mTR.removeRedundantPropositions();
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      mOriginalIntToStateMap = mTR.getOriginalIntToStateMap();
      mResultingStateToIntMap = mTR.getResultingStateToIntMap();
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  @Override
  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mOriginalIntToStateMap,
                                               mResultingStateToIntMap);
  }

  @Override
  public void cleanup()
  {
    mOriginalIntToStateMap = null;
    mResultingStateToIntMap = null;
    mTR = null;
    mAutToAbstract = null;
  }


  //#######################################################################
  //# Auxiliary Methods
  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing an {@link AbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  private void checkAbort()
    throws AbortException
  {
    if (mIsAborting) {
      final AbortException exception = new AbortException();
      throw exception;
    }
  }


  //#######################################################################
  //# Data Members
  private EventProxy mAlphaMarking;
  private AutomatonProxy mAutToAbstract;
  private StateProxy[] mOriginalIntToStateMap;
  private TObjectIntHashMap<StateProxy> mResultingStateToIntMap;
  private boolean mIsAborting;
  private OldTransitionRelation mTR = null;

}

