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

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * @author Simon Ware
 */

public class CanonizeAbstractionRule
  extends AbstractionRule
{
  //#######################################################################
  //# Constructor
  public CanonizeAbstractionRule(final ProductDESProxyFactory factory,
                                 final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  public CanonizeAbstractionRule(final ProductDESProxyFactory factory,
                                 final KindTranslator translator,
                                 final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions);
    mCont = factory.createEventProxy(":cont",
                                      EventKind.CONTROLLABLE);
  }

  void setOmegaMarking(final EventProxy omegaMarking)
  {
    mOmegaMarking = omegaMarking;
  }

  EventProxy getOmegaMarking()
  {
    return mOmegaMarking;
  }

  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  @Override
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tauproxy)
      throws AnalysisException
  {
    final EventEncoding ee = new EventEncoding(autToAbstract, getKindTranslator(), tauproxy);
    if (!autToAbstract.getEvents().contains(mCont)) {
      ee.addEvent(mCont, getKindTranslator(), (byte)0);
    }
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      ee.addEvent(mAlphaMarking, getKindTranslator(),
                  EventStatus.STATUS_UNUSED);
    }
    if (!autToAbstract.getEvents().contains(mOmegaMarking)) {
      ee.addEvent(mOmegaMarking, getKindTranslator(),
                  EventStatus.STATUS_UNUSED);
    }
    final ListBufferTransitionRelation tr =
      new ListBufferTransitionRelation(autToAbstract, ee,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      final int alpha = ee.getEventCode(mAlphaMarking);
      for (int s = 0; s < tr.getNumberOfStates(); s++) {
        tr.setMarked(s, alpha, true);
      }
    }
    if (!autToAbstract.getEvents().contains(mOmegaMarking)) {
      final int omega = ee.getEventCode(mAlphaMarking);
      for (int s = 0; s < tr.getNumberOfStates(); s++) {
        tr.setMarked(s, omega, true);
      }
    }
    final int tau = EventEncoding.TAU;
    final int marking = ee.getEventCode(mOmegaMarking);
    final int alpha = ee.getEventCode(mAlphaMarking);
    final int cont = ee.getEventCode(mCont);
    if (autToAbstract.getEvents().contains(mCont)) {
      tr.replaceEvent(cont, tau);
    }
    try {
      final Canonize canonizer = new Canonize(tr, ee, marking, alpha, cont);
      final ListBufferTransitionRelation canon = canonizer.run(getFactory());
      canon.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final TIntHashSet visited = new TIntHashSet();
      final TIntArrayList tovisit = new TIntArrayList();
      for (int i = 0; i < canon.getNumberOfStates(); i++) {
        if (canon.isInitial(i)) {
          tovisit.add(i);
          visited.add(i);
        }
      }
      while (!tovisit.isEmpty()) {
        final int state = tovisit.removeAt(tovisit.size() -1);
        canon.setReachable(state, true);
        final TransitionIterator it = canon.createSuccessorsReadOnlyIterator(state);
        while (it.advance()) {
          final int suc = it.getCurrentTargetState();
          if (visited.add(suc)) {tovisit.add(suc);}
        }
      }
      for (int i = 0; i < canon.getNumberOfStates(); i++) {
        if (!visited.contains(i))  {canon.setReachable(i, false);}
      }
      canon.removeUnreachableTransitions();
      System.out.println(autToAbstract.getName());
      System.out.println("Canon: " + canon.getNumberOfReachableStates() + " Comp: " + autToAbstract.getStates().size());
      //if (canon.getNumberOfReachableStates() <= autToAbstract.getStates().size() * 2) {
        return canon.createAutomaton(getFactory(), ee);
      //}
    } catch (final AnalysisException analysis) {
      System.out.println("overflow");
    }
    return autToAbstract;
  }

  @Override
  CompositionalGeneralisedConflictChecker.Step createStep(final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return null;
  }

  @Override
  public void cleanup()
  {
    return;
  }


  //########################################################################
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


  //########################################################################
  //# Logging
  @Override
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#######################################################################
  //# Data Members
  private EventProxy mOmegaMarking;
  private EventProxy mAlphaMarking;
  private final EventProxy mCont;


  private boolean mIsAborting = false;
}
