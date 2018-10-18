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

/**
 * A supervisor reduction factory that provides a collection of simple
 * pre-configured methods for supervisor reduction.
 *
 * @author Robi Malik
 */

public enum DefaultSupervisorReductionFactory
  implements SupervisorReductionFactory
{

  //#########################################################################
  //# Enumeration
  /**
   * An option to disable supervisor reduction.
   * With this option, synthesised supervisors are returned unchanged
   * without any attempts of minimisation.
   */
  OFF("Off") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      return null;
    }
  },
  /**
   * <P>An option to specify the Su/Wonham supervisor reduction algorithm.
   * This option applies the Su/Wonham algorithm and afterwards removes
   * events that appear only on selfloops and are not under supervision.</P>
   *
   * <P><I>Reference.</I><BR>
   * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
   * Systems. Discrete Event Dynamic Systems: Theory and Applications,
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   *
   * @see ObservationEquivalenceTRSimplifier
   * @see SuWonhamSupervisorReductionTRSimplifier
   * @see SelfloopSupervisorReductionTRSimplifier
   */
  SU_WONHAM("Su/Wonham") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SuWonhamSupervisorReductionTRSimplifier main =
        new SuWonhamSupervisorReductionTRSimplifier();
      main.setExperimentalMode(true);
      return new SupervisorReductionChain(false, main);
    }
  },
  /**
   * <P>An option to specify a combination of event removal and the Su/Wonham
   * supervisor reduction algorithm. This options simplifies supervisors by
   * first removing events that are not needed to make control decisions,
   * then invoking the Su/Wonham algorithm, and afterwards removing events
   * that appear only on selfloops and are not under supervision.</P>
   *
   * <P><I>Reference.</I><BR>
   * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
   * Systems. Discrete Event Dynamic Systems: Theory and Applications,
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   *
   * @see ObservationEquivalenceTRSimplifier
   * @see ProjectingSupervisorReductionTRSimplifier
   * @see SuWonhamSupervisorReductionTRSimplifier
   * @see SelfloopSupervisorReductionTRSimplifier
   */
  PROJECTION_SU_WONHAM("Projection + Su/Wonham") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SuWonhamSupervisorReductionTRSimplifier main =
        new SuWonhamSupervisorReductionTRSimplifier();
      main.setExperimentalMode(true);
      return new SupervisorReductionChain(true, main);
    }
  },
  /**
   * <P>An option to specify clique-based supervisor reduction.
   * This options simplifies supervisors by invoking the clique-based
   * algorithm that attempts to find a supervisor that uses the smallest
   * possible number of cliques of compatible states, and afterwards removing
   * events that appear only on selfloops and are not under supervision.</P>
   *
   * @see ObservationEquivalenceTRSimplifier
   * @see CliqueBasedSupervisorReductionTRSimplifier
   * @see SelfloopSupervisorReductionTRSimplifier
   */
  CLIQUE_BASED("Clique-based") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SupervisorReductionSimplifier main =
        new CliqueBasedSupervisorReductionTRSimplifier();
      return new SupervisorReductionChain(false, main);
    }

    @Override
    public boolean isSupervisedEventRequired()
    {
      return true;
    }
  },
  /**
   * <P>An option to specify a combination of event removal and clique-based
   * supervisor reduction. This options simplifies supervisors by removing
   * events that are not needed to make control decisions, then invoking the
   * clique-based algorithm that attempts to find a supervisor that uses the
   * smallest possible number of cliques of compatible states, and afterwards
   * removing events that appear only on selfloops and are not under
   * supervision.</P>
   *
   * @see ObservationEquivalenceTRSimplifier
   * @see ProjectingSupervisorReductionTRSimplifier
   * @see CliqueBasedSupervisorReductionTRSimplifier
   * @see SelfloopSupervisorReductionTRSimplifier
   */
  PROJECTION_CLIQUE_BASED("Projection + Clique-based") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SupervisorReductionSimplifier main =
        new CliqueBasedSupervisorReductionTRSimplifier();
      return new SupervisorReductionChain(true, main);
    }

    @Override
    public boolean isSupervisedEventRequired()
    {
      return true;
    }
  };


  //#########################################################################
  //# Constructors
  private DefaultSupervisorReductionFactory(final String name)
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
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory
  @Override
  public boolean isSupervisedEventRequired()
  {
    return false;
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
