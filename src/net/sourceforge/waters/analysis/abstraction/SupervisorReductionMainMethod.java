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
 * An enumeration of options to select and configure the main algorithm
 * of supervisor reduction.
 *
 * @author Robi Malik
 */

public enum SupervisorReductionMainMethod
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
   * <P>An option to specify the Su/Wonham supervisor reduction algorithm
   * using the lexicographic state pair ordering as originally proposed.
   * If the states are ordered 0,1,2,...,<I>n</I> then this strategy
   * attempts to merge pairs in the order (0,1), (0,2), ..., (0,<I>n</I>),
   * (1,2), (1,3), ..., (1,<I>n</I>), (2,3), ...</P>
   *
   * <P><I>Reference.</I><BR>
   * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
   * Systems. Discrete Event Dynamic Systems: Theory and Applications,
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   *
   * @see SuWonhamSupervisorReductionTRSimplifier
   */
  SU_WONHAM("Su/Wonham (lexicographic)") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SuWonhamSupervisorReductionTRSimplifier simplifier =
        new SuWonhamSupervisorReductionTRSimplifier();
      simplifier.setPairOrdering
        (SuWonhamSupervisorReductionTRSimplifier.PairOrdering.LEXICOGRAPHIC);
      return simplifier;
    }
  },
  /**
   * <P>An option to specify the Su/Wonham supervisor reduction algorithm
   * using an alternative diagonal state pair ordering, which is more
   * likely to merge states that are close to each other in the state
   * ordering. If the states are ordered 0,1,2,..., then this strategy
   * attempts to merge pairs in the order (1,0), (2,0), (2,1), (3,0), (3,1),
   * (3,2), ...</P>
   *
   * <P><I>Reference.</I><BR>
   * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
   * Systems. Discrete Event Dynamic Systems: Theory and Applications,
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   *
   * @see SuWonhamSupervisorReductionTRSimplifier
   */
  SU_WONHAM_DIAGONAL1("Su/Wonham (diagonal1)") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SuWonhamSupervisorReductionTRSimplifier simplifier =
        new SuWonhamSupervisorReductionTRSimplifier();
      simplifier.setPairOrdering
        (SuWonhamSupervisorReductionTRSimplifier.PairOrdering.DIAGONAL1);
      return simplifier;
    }
  },
  /**
   * <P>An option to specify the Su/Wonham supervisor reduction algorithm
   * using an alternative diagonal state pair ordering, which is more
   * likely to merge states that are close to each other in the state
   * ordering. If the states are ordered 0,1,2,..., then this strategy
   * attempts to merge pairs in the order (1,0), (2,1), (2,0), (3,2), (3,1),
   * (3,0), ...</P>
   *
   * <P><I>Reference.</I><BR>
   * R. Su and W. Murray Wonham. Supervisor Reduction for Discrete-Event
   * Systems. Discrete Event Dynamic Systems: Theory and Applications,
   * <STRONG>14</STRONG>&nbsp;(1), 31-53, 2004.</P>
   *
   * @see SuWonhamSupervisorReductionTRSimplifier
   */
  SU_WONHAM_DIAGONAL2("Su/Wonham (diagonal2)") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      final SuWonhamSupervisorReductionTRSimplifier simplifier =
        new SuWonhamSupervisorReductionTRSimplifier();
      simplifier.setPairOrdering
        (SuWonhamSupervisorReductionTRSimplifier.PairOrdering.DIAGONAL2);
      return simplifier;
    }
  },
  /**
   * <P>An option to specify clique-based supervisor reduction.
   * This options simplifies supervisors by invoking the clique-based
   * algorithm that attempts to find a supervisor that uses the smallest
   * possible number of cliques of compatible states.</P>
   *
   * @see CliqueBasedSupervisorReductionTRSimplifier
   */
  CLIQUE("Clique-based") {
    @Override
    public SupervisorReductionSimplifier createSimplifier()
    {
      return new CliqueBasedSupervisorReductionTRSimplifier();
    }

    @Override
    public boolean isSupervisedEventRequired()
    {
      return true;
    }
  };


  //#########################################################################
  //# Constructors
  private SupervisorReductionMainMethod(final String name)
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
  public abstract SupervisorReductionSimplifier createSimplifier();

  public boolean isSupervisedEventRequired()
  {
    return false;
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
