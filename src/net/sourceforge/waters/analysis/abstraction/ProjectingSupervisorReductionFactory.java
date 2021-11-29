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

import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;

/**
 * A supervisor reduction factory that can be configured to
 * perform projection and supervisor reduction using two enumerations of
 * pre-configured simplifiers.
 *
 * @author Robi Malik
 */

public class ProjectingSupervisorReductionFactory
  implements SupervisorReductionFactory
{
  //#########################################################################
  //# Constructor
  /**
   * Creates a supervisor reduction factory that does not perform any
   * supervisor reduction.
   */
  public ProjectingSupervisorReductionFactory()
  {
    this(SupervisorReductionMainMethod.OFF);
  }

  /**
   * Creates a supervisor reduction factory that performs supervisor reduction
   * without projection.
   * @param  method     An enum constant to specify the method of supervisor
   *                    reduction.
   */
  public ProjectingSupervisorReductionFactory
    (final SupervisorReductionMainMethod method)
  {
    this(SupervisorReductionProjectionMethod.OFF, method);
  }

  /**
   * Creates a supervisor reduction factory that performs supervisor reduction
   * without projection.
   * @param  method     An enum constant to specify the method of supervisor
   *                    reduction.
   * @param  ordering   The state ordering imposed on the transition
   *                    relation before invoking the main step of
   *                    supervisor reduction. It should be one of
   *                    {@link StateReorderingTRSimplifier#UNCHANGED},
   *                    {@link StateReorderingTRSimplifier#REVERSED},
   *                    {@link StateReorderingTRSimplifier#BFS},
   *                    {@link StateReorderingTRSimplifier#BFS_REVERSED},
   *                    {@link StateReorderingTRSimplifier#DFS}, or
   *                    {@link StateReorderingTRSimplifier#DFS_REVERSED}.
   */
  public ProjectingSupervisorReductionFactory
    (final SupervisorReductionMainMethod method,
     final StateReorderingTRSimplifier.StateOrdering ordering)
  {
    this(SupervisorReductionProjectionMethod.OFF, method, ordering);
  }

  /**
   * Creates a supervisor reduction factory that performs supervisor reduction
   * and projection.
   * @param  projection An enum constant to specify the method of projection
   *                    to reduce the number of events before supervisor
   *                    reduction.
   * @param  main       An enum constant to specify the main method of
   *                    supervisor reduction.
   */
  public ProjectingSupervisorReductionFactory
    (final SupervisorReductionProjectionMethod projection,
     final SupervisorReductionMainMethod main)
  {
    this(projection, main, StateReorderingTRSimplifier.UNCHANGED);
  }

  /**
   * Creates a supervisor reduction factory that performs supervisor reduction
   * and projection.
   * @param  projection An enum constant to specify the method of projection
   *                    to reduce the number of events before supervisor
   *                    reduction.
   * @param  main       An enum constant to specify the main method of
   *                    supervisor reduction.
   * @param  ordering   The state ordering imposed on the transition
   *                    relation before invoking the main step of
   *                    supervisor reduction. It should be one of
   *                    {@link StateReorderingTRSimplifier#UNCHANGED},
   *                    {@link StateReorderingTRSimplifier#REVERSED},
   *                    {@link StateReorderingTRSimplifier#BFS},
   *                    {@link StateReorderingTRSimplifier#BFS_REVERSED},
   *                    {@link StateReorderingTRSimplifier#DFS}, or
   *                    {@link StateReorderingTRSimplifier#DFS_REVERSED}.
   */
  public ProjectingSupervisorReductionFactory
    (final SupervisorReductionProjectionMethod projection,
     final SupervisorReductionMainMethod main,
     final StateReorderingTRSimplifier.StateOrdering ordering)
  {
    mProjectionMethod = projection;
    mMainMethod = main;
    mStateOrdering = ordering;
  }


  //#########################################################################
  //# Configuration
  public void setProjectionMethod(final SupervisorReductionProjectionMethod method)
  {
    mProjectionMethod = method;
  }

  public SupervisorReductionProjectionMethod getProjectionMethod()
  {
    return mProjectionMethod;
  }

  public void setMainMethod(final SupervisorReductionMainMethod method)
  {
    mMainMethod = method;
  }

  public SupervisorReductionMainMethod getMainMethod()
  {
    return mMainMethod;
  }

  public void setStateOrdering
    (final StateReorderingTRSimplifier.StateOrdering ordering)
  {
    mStateOrdering = ordering;
  }

  public StateReorderingTRSimplifier.StateOrdering getStateOrdering()
  {
    return mStateOrdering;
  }


  //#########################################################################
  //# Setting Synthesiser Options
  public static void configureSynthesizer
    (final SupervisorSynthesizer synthesizer,
     final SupervisorReductionProjectionMethod method)
  {
    final ProjectingSupervisorReductionFactory factory =
      ensureProjectingSupervisorReductionFactory(synthesizer);
    factory.setProjectionMethod(method);
  }

  public static void configureSynthesizer
    (final SupervisorSynthesizer synthesizer,
     final SupervisorReductionMainMethod method)
  {
    final ProjectingSupervisorReductionFactory factory =
      ensureProjectingSupervisorReductionFactory(synthesizer);
    factory.setMainMethod(method);
  }

  public static void configureSynthesizer
    (final SupervisorSynthesizer synthesizer,
     final StateReorderingTRSimplifier.StateOrdering ordering)
  {
    final ProjectingSupervisorReductionFactory factory =
      ensureProjectingSupervisorReductionFactory(synthesizer);
    factory.setStateOrdering(ordering);
  }

  public static ProjectingSupervisorReductionFactory
    ensureProjectingSupervisorReductionFactory
    (final SupervisorSynthesizer synthesizer)
  {
    final SupervisorReductionFactory factory =
      synthesizer.getSupervisorReductionFactory();
    if (factory instanceof ProjectingSupervisorReductionFactory) {
      return (ProjectingSupervisorReductionFactory) factory;
    } else {
      final ProjectingSupervisorReductionFactory projectingFactory =
        new ProjectingSupervisorReductionFactory();
      synthesizer.setSupervisorReductionFactory(projectingFactory);
      return projectingFactory;
    }
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory
  @Override
  public TransitionRelationSimplifier createInitialMinimizer
    (final boolean includeCoreachability)
  {
    if (isSupervisedReductionEnabled()) {
      return new SupervisorReductionChain(includeCoreachability);
    } else {
      return null;
    }
  }

  @Override
  public SupervisorReductionSimplifier createSupervisorReducer
    (final boolean localisation, final double maxIncrease)
  {
    if (isSupervisedReductionEnabled()) {
      final TransitionRelationSimplifier projector =
        mProjectionMethod.createSimplifier();
      final SupervisorReductionSimplifier main = mMainMethod.createSimplifier();
      return new SupervisorReductionChain(projector, main, mStateOrdering,
                                          localisation, maxIncrease);
    } else {
      return null;
    }
  }

  @Override
  public boolean isSupervisedReductionEnabled()
  {
    return mMainMethod != SupervisorReductionMainMethod.OFF;
  }


  //#########################################################################
  //# Data Members
  private SupervisorReductionProjectionMethod mProjectionMethod =
    SupervisorReductionProjectionMethod.OFF;
  private SupervisorReductionMainMethod mMainMethod =
    SupervisorReductionMainMethod.OFF;
  private StateReorderingTRSimplifier.StateOrdering mStateOrdering =
    StateReorderingTRSimplifier.UNCHANGED;

}
