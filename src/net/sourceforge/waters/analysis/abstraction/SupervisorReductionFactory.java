//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
   * Creates a transition relation simplifier that can perform supervisor
   * reduction.
   * @return  The fully configured transition relation simplifier,
   *          or <CODE>null</CODE> to disable supervisor reduction.
   */
  public abstract SupervisorReductionSimplifier createSimplifier();

  /**
   * Whether the supervisor reduction algorithm provided by this algorithm
   * requires a supervised event. If a supervised event is required,
   * supervisor reduction must be performed for one controllable event
   * at a time, i.e., supervisor localisation. Then the {@link
   * SupervisorReductionSimplifier#setSupervisedEvent(int)} must be called
   * to specify a controllable event before running the simplifier.
   * @return <CODE>true</CODE> if a supervised event is required.
   */
  public boolean isSupervisedEventRequired();


  //#########################################################################
  //# Inner Class SupervisorReductionChain
  public static class SupervisorReductionChain
    extends ChainTRSimplifier
    implements SupervisorReductionSimplifier
  {
    //#######################################################################
    //# Constructor
    protected SupervisorReductionChain(final boolean projecting,
                                       final SupervisorReductionSimplifier main)
    {
      mMainSimplifier = main;
      if (projecting) {
        add(new ProjectingSupervisorReductionTRSimplifier());
        add(new SpecialEventsTRSimplifier());
        final SubsetConstructionTRSimplifier subset =
          new SubsetConstructionTRSimplifier();
        subset.setDumpStateAware(true);
        add(subset);
        add(new SelfloopSupervisorReductionTRSimplifier());
        final ObservationEquivalenceTRSimplifier bisimulator =
          new ObservationEquivalenceTRSimplifier();
        bisimulator.setEquivalence
          (ObservationEquivalenceTRSimplifier.Equivalence.
           DETERMINISTIC_MINSTATE);
        add(bisimulator);
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

    //#######################################################################
    //# Data Members
    private final SupervisorReductionSimplifier mMainSimplifier;
  }

}
