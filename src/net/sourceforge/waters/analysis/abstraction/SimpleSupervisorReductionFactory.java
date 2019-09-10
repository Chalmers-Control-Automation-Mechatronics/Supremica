//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

/**
 * <P>A supervisor reduction factory that provides a fixed manually configured
 * transition relation simplifier to perform supervisor reduction.</P>
 *
 * <P>A supervisor reduction method is defined by a core algorithm
 * such as {@link SuWonhamSupervisorReductionTRSimplifier} or {@link
 * MaxCliqueSupervisorReductionTRSimplifier} that defines how supervisor
 * reduction is performed. Based on this, the simple supervisor reduction
 * factory returns a simplifier chain {@link ChainTRSimplifier} that includes
 * the core algorithm between some pre- and post-processing steps.</P>
 *
 * <P>The chain consists of:</P>
 * <OL>
 * <LI>Optionally a {@link ProjectingSupervisorReductionTRSimplifier}
 *     to identify events that are not needed to make control decisions,
 *     followed by a {@link SpecialEventsTRSimplifier} to hide such events,
 *     a {@link SubsetConstructionTRSimplifier} to compute the natural
 *     projection, and a {@link ObservationEquivalenceTRSimplifier} to compute
 *     the minimal language-equivalent state machine.</LI>
 * <LI>The core algorithm</LI>
 * <LI>A {@link SelfloopSupervisorReductionTRSimplifier} to remove events that
 *     appear only on selfloops and are not under supervision.</LI>
 * </OL>
 *
 * @author Robi Malik
 */

public class SimpleSupervisorReductionFactory
  implements SupervisorReductionFactory
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a supervisor reduction factory for localised and non-localised
   * supervisor reduction, without projection.
   * @param  simplifier  The transition relation simplifier that implements
   *                     the core supervisor reduction algorithm.
   */
  public SimpleSupervisorReductionFactory
    (final SupervisorReductionSimplifier simplifier)
  {
    this(null, simplifier);
  }

  /**
   * Creates a supervisor reduction factory that may or may not support
   * for non-localised supervisor reduction, optionally with projection.
   * @param  simplifier    The transition relation simplifier that implements
   *                       the core supervisor reduction algorithm.
   */
  public SimpleSupervisorReductionFactory
    (final TransitionRelationSimplifier projector,
     final SupervisorReductionSimplifier simplifier)
  {
    mProjector = projector;
    mSimplifier = simplifier;
  }


  //#########################################################################
  //# Factory Methods
  @Override
  public TransitionRelationSimplifier createInitialMinimizer
    (final boolean includeCoreachability)
  {
    return new SupervisorReductionChain(includeCoreachability);
  }

  @Override
  public SupervisorReductionSimplifier createSupervisorReducer
    (final boolean localisation)
  {
    return new SupervisorReductionChain(mProjector, mSimplifier, localisation);
  }

  @Override
  public boolean isSupervisedReductionEnabled()
  {
    return true;
  }


  //#########################################################################
  //# Data Members
  private final TransitionRelationSimplifier mProjector;
  private final SupervisorReductionSimplifier mSimplifier;

}
