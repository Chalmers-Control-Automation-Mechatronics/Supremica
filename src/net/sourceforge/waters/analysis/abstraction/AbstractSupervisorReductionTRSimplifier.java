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

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;


/**
 * <P>An abstract base for transition relation simplifiers that implement
 * supervisor reduction.</P>
 *
 * @see SupervisorReductionSimplifier
 *
 * @author Robi Malik
 */

public abstract class AbstractSupervisorReductionTRSimplifier
  extends AbstractTRSimplifier
  implements SupervisorReductionSimplifier
{

  //#########################################################################
  //# Constructors
  public AbstractSupervisorReductionTRSimplifier()
  {
  }

  public AbstractSupervisorReductionTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    return false;
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    final TRSimplifierStatistics stats =
      new TRSimplifierStatistics(this, true, false);
    return setStatistics(stats);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.abstraction.SupervisorReductionSimplifier
  @Override
  public void setSupervisedEvent(final int event)
  {
    mSupervisedEvent = event;
  }

  @Override
  public int getSupervisedEvent()
  {
    return mSupervisedEvent;
  }

  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of states that will be created. The default for supervisor
   * reduction is <STRONG>not</STRONG> to support state limits, but
   * this behaviour may be overridden by subclasses.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  @Override
  public void setStateLimit(final int limit)
  {
  }

  @Override
  public int getStateLimit()
  {
    return Integer.MAX_VALUE;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions that will be created. The default for supervisor
   * reduction is <STRONG>not</STRONG> to support transition limits, but
   * this behaviour may be overridden by subclasses.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of transitions.
   */
  @Override
  public void setTransitionLimit(final int limit)
  {
  }

  @Override
  public int getTransitionLimit()
  {
    return Integer.MAX_VALUE;
  }


  //#########################################################################
  //# Data Members
  private int mSupervisedEvent = -1;

}
