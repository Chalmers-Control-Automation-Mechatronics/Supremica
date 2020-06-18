//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
 * An enumeration of options to configure the optional projection step
 * of supervisor reduction.
 *
 * @author Robi Malik
 */

public enum SupervisorReductionProjectionMethod
{

  //#########################################################################
  //# Enumeration
  /**
   * An option to disable projection.
   * With this option, supervisors are passed directly to the supervisor
   * reduction algorithm.
   */
  OFF("Off") {
    @Override
    public TransitionRelationSimplifier createSimplifier()
    {
      return null;
    }
  },
  /**
   * <P>An option to specify a greedy search for a projection that
   * removes the most transitions possible.</P>
   */
  GREEDY("Greedy") {
    @Override
    public TransitionRelationSimplifier createSimplifier()
    {
      final ProjectingSupervisorReductionTRSimplifier projector =
        new ProjectingSupervisorReductionTRSimplifier();
      projector.setExhaustive(false);
      projector.setEnsuringOP(false);
      return projector;
    }
  },
  /**
   * <P>An option to specify a greedy search for a projection that
   * removes the most transitions possible while also satisfying the
   * observer property.</P>
   */
  GREEDY_OP("Greedy OP") {
    @Override
    public TransitionRelationSimplifier createSimplifier()
    {
      final ProjectingSupervisorReductionTRSimplifier projector =
        new ProjectingSupervisorReductionTRSimplifier();
      projector.setExhaustive(false);
      projector.setEnsuringOP(true);
      return projector;
    }
  },
  /**
   * <P>An option to specify an exhaustive search for the projection that
   * removes the most transitions possible.</P>
   */
  EXHAUSTIVE("Exhaustive") {
    @Override
    public TransitionRelationSimplifier createSimplifier()
    {
      final ProjectingSupervisorReductionTRSimplifier projector =
        new ProjectingSupervisorReductionTRSimplifier();
      projector.setExhaustive(true);
      projector.setEnsuringOP(false);
      return projector;
    }
  },
  /**
   * <P>An option to specify an exhaustive search for the projection that
   * removes the most transitions possible while also satisfying the
   * observer property.</P>
   */
  EXHAUSTIVE_OP("Exhaustive OP") {
    @Override
    public TransitionRelationSimplifier createSimplifier()
    {
      final ProjectingSupervisorReductionTRSimplifier projector =
        new ProjectingSupervisorReductionTRSimplifier();
      projector.setExhaustive(true);
      projector.setEnsuringOP(true);
      return projector;
    }
  };


  //#########################################################################
  //# Constructors
  private SupervisorReductionProjectionMethod(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Access
  public abstract TransitionRelationSimplifier createSimplifier();


  //#########################################################################
  //# Data Members
  private String mName;

}
