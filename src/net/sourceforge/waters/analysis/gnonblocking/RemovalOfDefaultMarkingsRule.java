//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * For a given Automaton applies an abstraction rule which removes the default
 * marking proposition from states which are not reachable from any state with
 * an alpha marking.
 *
 * @author Rachel Francis
 */

class RemovalOfDefaultMarkingsRule extends AbstractionRule
{

  //#######################################################################
  //# Constructors
  RemovalOfDefaultMarkingsRule(final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  RemovalOfDefaultMarkingsRule(final ProductDESProxyFactory factory,
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

  EventProxy getDefaultMarking()
  {
    return mDefaultMarking;
  }

  void setDefaultMarking(final EventProxy defaultMarking)
  {
    mDefaultMarking = defaultMarking;
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

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#######################################################################
  //# Rule Application
  @Override
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
  throws AnalysisAbortException
  {
    mAutToAbstract = autToAbstract;
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      return autToAbstract;
    }
    boolean modified = false;
    mTR =
        new OldTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = mTR.getEventInt(mAlphaMarking);
    int defaultID = mTR.getEventInt(mDefaultMarking);
    if (defaultID == -1) {
      defaultID = mTR.addProposition(mDefaultMarking, true);
    }
    final int numStates = mTR.getNumberOfStates();

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntArrayStack();

    // creates a hash set of all states which are reachable from an alpha marked
    // state
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (mTR.isMarked(sourceID, alphaID) &&
          !reachableStates.contains(sourceID)) {
        checkAbort();
        unvisitedStates.push(sourceID);
        reachableStates.add(sourceID);
        while (unvisitedStates.size() > 0) {
          final int newSource = unvisitedStates.pop();
          final TIntHashSet[] successors = mTR.getAllSuccessors(newSource);
          if (successors != null) {
            for (int e = 0; e < successors.length; e++) {
              final TIntHashSet targets = successors[e];
              if (targets != null) {
                final TIntIterator iter = targets.iterator();
                while (iter.hasNext()) {
                  final int targetID = iter.next();
                  if (!reachableStates.contains(targetID)) {
                    reachableStates.add(targetID);
                    unvisitedStates.push(targetID);
                  }
                }
              }
            }
          }
        }
      }
    }
    // removes default marking from all states which were not found as reachable
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (!reachableStates.contains(sourceID)
          && mTR.isMarked(sourceID, defaultID)) {
        mTR.markState(sourceID, defaultID, false);
        modified = true;
      }
    }
    if (modified) {
      mTR.removeRedundantPropositions();
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  @Override
  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mTR.getOriginalIntToStateMap(),
                                               mTR.getResultingStateToIntMap());
  }

  @Override
  public void cleanup()
  {
    mTR = null;
    mAutToAbstract = null;
  }

  //#######################################################################
  //# Auxiliary Methods
  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  private void checkAbort()
    throws AnalysisAbortException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw exception;
    }
  }


  //#######################################################################
  //# Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;

  private boolean mIsAborting;
  private OldTransitionRelation mTR;

}
