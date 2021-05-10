//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;


/**
 * An interface to define a supervisor reduction method that can be used
 * by synthesisers ({@link SupervisorSynthesizer}). A supervisor reduction
 * factory is called by the synthesiser to produce a
 * {@link TransitionRelationSimplifier} that performs supervisor reduction.
 *
 * @author Robi Malik
 */

public interface SupervisorReductionFactory
{

  //#########################################################################
  //# Factory Methods
  /**
   * Creates a transition relation simplifier that performs initial
   * minimisation steps. If supervisor localisation is performed, then
   * the same automaton will undergo supervisor reduction several times,
   * once for each controllable event. In this case, some simplification
   * operations may be the same for controllable events and can be performed
   * at the start, before the automaton is replicated and separate
   * supervisor reduction begins.
   * @param   includeCoreachability  Whether the input supervisor may include
   *          non-coreachable states that should be removed as a first step.
   * @return  A fully configured transition relation simplifier to perform
   *          common steps, or <CODE>null</CODE> if there are no common steps.
   */
  public TransitionRelationSimplifier createInitialMinimizer
    (boolean includeCoreachability);

  /**
   * Creates a transition relation simplifier that can perform supervisor
   * reduction. If the factory returns an initial minimiser, then the
   * transition relation simplifier returned by this method assumes that
   * the initial minimisation has been performed on any input it receives.
   * @param   localisation  Whether or not supervisor localisation is
   *                        performed. If <CODE>true</CODE>, the result of
   *                        the initial minimiser may have been changed
   *                        after selection of a single controllable event.
   * @return  The fully configured transition relation simplifier,
   *          or <CODE>null</CODE> to disable supervisor reduction.
   */
  public SupervisorReductionSimplifier createSupervisorReducer
    (boolean localisation);

  /**
   * Whether the supervisor reduction factory is configured to perform
   * supervisor reduction.
   * @return <CODE>true</CODE> if the factory in its current configuration
   *         will perform some kind of supervisor reduction,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isSupervisedReductionEnabled();


  //#########################################################################
  //# Inner Class SupervisorReductionChain
  public static class SupervisorReductionChain
    extends ChainTRSimplifier
    implements SupervisorReductionSimplifier
  {
    //#######################################################################
    //# Constructors
    public SupervisorReductionChain(final boolean includeCoreachability)
    {
      mMainSimplifier = null;
      addInitialMinimisationSteps(includeCoreachability);
    }

    public SupervisorReductionChain(final TransitionRelationSimplifier projector,
                                    final SupervisorReductionSimplifier main,
                                    final boolean localisation)
    {
      mMainSimplifier = main;
      if (localisation) {
        addInitialMinimisationSteps(false);
      }
      if (projector != null) {
        add(projector);
        add(new ConditionalSupervisorReductionSubChain());
      }
      add(main);
      add(new SelfloopSupervisorReductionTRSimplifier());
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier
    @Override
    public void setSupervisedEvent(final int event)
    {
      mMainSimplifier.setSupervisedEvent(event);
    }

    @Override
    public int getSupervisedEvent()
    {
      return mMainSimplifier.getSupervisedEvent();
    }

    //#########################################################################
    //# Auxiliary Methods
    private void addInitialMinimisationSteps(final boolean includeCoreachability)
    {
      if (includeCoreachability) {
        final CoreachabilityTRSimplifier coreachability =
          new CoreachabilityTRSimplifier();
        coreachability.setKeepingDumpState(true);
        add(coreachability);
      }
      add(new SelfloopSupervisorReductionTRSimplifier());
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
      add(bisimulator);
    }

    //#######################################################################
    //# Data Members
    private final SupervisorReductionSimplifier mMainSimplifier;
  }


  //#########################################################################
  //# Inner Class ConditionalSupervisorReductionSubChain
  public static class ConditionalSupervisorReductionSubChain
    extends ConditionalTRSimplifier
  {
    //#######################################################################
    //# Constructor
    private ConditionalSupervisorReductionSubChain()
    {
      add(new SpecialEventsTRSimplifier());
      final SubsetConstructionTRSimplifier subset =
        new SubsetConstructionTRSimplifier();
      subset.setDumpStateAware(true);
      subset.setMaxIncrease(2.0);
      add(subset);
      final ObservationEquivalenceTRSimplifier bisimulator =
        new ObservationEquivalenceTRSimplifier();
      bisimulator.setEquivalence
        (ObservationEquivalenceTRSimplifier.Equivalence.DETERMINISTIC_MINSTATE);
      add(bisimulator);
    }

    //#######################################################################
    //# Overrides for
    //# net.sourceforge.waters.analysis.abstraction.ConditionalTRSimplifier
    @Override
    protected boolean checkPreCondition()
    {
      final ListBufferTransitionRelation rel = getTransitionRelation();
      final int numEvents = rel.getNumberOfProperEvents();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if (EventStatus.isLocalEvent(status)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected boolean checkPostCondition(final ListBufferTransitionRelation result)
    {
      final ListBufferTransitionRelation orig = getTransitionRelation();
      return result.getNumberOfReachableStates() <=
             orig.getNumberOfReachableStates();
    }
  }
}
