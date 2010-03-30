//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;
import java.util.Collection;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * For a given Automaton applies an abstraction rule which removes states from
 * which neither an alpha or omega state can be reached.
 *
 * @author Rachel Francis
 */

class RemovalOfNoncoreachableStatesRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfNoncoreachableStatesRule(final ProductDESProxyFactory factory,
                                    final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
  }

  // #######################################################################
  // # Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  EventProxy getDefaultMarking()
  {
    return mDefaultMarking;
  }

  void setDefaultMarking(final EventProxy defaultMarking)
  {
    mDefaultMarking = defaultMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    final boolean modified = false;
    final ObserverProjectionTransitionRelation tr =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    @SuppressWarnings("unused")
    final int alphaID = tr.getEventInt(mAlphaMarking);
    @SuppressWarnings("unused")
    final int defaultID = tr.getEventInt(mDefaultMarking);
    @SuppressWarnings("unused")
    final int numStates = tr.getNumberOfStates();

    @SuppressWarnings("unused")
    final TIntArrayList initialStates = tr.getAllInitialStates();

    if (modified) {
      final AutomatonProxy convertedAut = tr.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return null;
  }


  // #########################################################################
  // # Inner Class SearchRecord
  @SuppressWarnings("unused")
  private static class SearchRecord
  {

    // #######################################################################
    // # Constructors
    SearchRecord(final int state)
    {
      this(state, false, -1, null);
    }

    SearchRecord(final int state, final boolean hasEvent, final int event,
                 final SearchRecord pred)
    {
      mState = state;
      mHasMarking = hasEvent;
      mEvent = event;
      mPredecessor = pred;
    }

    // #######################################################################
    // # Getters
    boolean hasProperEvent()
    {
      return mHasMarking;
    }

    int getState()
    {
      return mState;
    }

    SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    int getEvent()
    {
      return mEvent;
    }

    // #######################################################################
    // # Data Members
    private final int mState;
    private final boolean mHasMarking;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  @SuppressWarnings("unused")
  private AutomatonProxy mAutToAbstract;
}
