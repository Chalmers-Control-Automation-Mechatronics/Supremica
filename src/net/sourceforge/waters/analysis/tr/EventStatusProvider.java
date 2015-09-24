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

package net.sourceforge.waters.analysis.tr;


/**
 * Interface to define a mapping from event codes to status bits
 * for transition relations.
 *
 * @see EventStatus
 * @see DefaultEventStatusProvider
 * @see EventEncoding
 * @see ListBufferTransitionRelation
 *
 * @author Robi Malik
 */

public interface EventStatusProvider
  extends Cloneable
{

  //#########################################################################
  //# Interface java.lang.Cloneable
  public EventStatusProvider clone();


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of proper events, i.e., non-proposition events,
   * in this encoding. The number of proper events always includes the
   * silent (tau) event, even if none has been specified.
   */
  public int getNumberOfProperEvents();

  /**
   * Retrieves the status flags for the given proper event.
   * @param  event  Code of the proper event to be looked up.
   *                Must be in the range of events in the encoding.
   * @return A combination of the bits
   *         {@link EventStatus#STATUS_CONTROLLABLE},
   *         {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *         {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *         {@link EventStatus#STATUS_BLOCKED},
   *         {@link EventStatus#STATUS_FAILING}, and
   *         {@link EventStatus#STATUS_UNUSED}.
   */
  public byte getProperEventStatus(int event);

  /**
   * Assigns new status flags to the given proper event.
   * @param  event  Code of the proper event to be modified.
   *                Must be in the range of events in the encoding.
   * @param  status A combination of the bits
   *                {@link EventStatus#STATUS_CONTROLLABLE},
   *                {@link EventStatus#STATUS_ALWAYS_ENABLED},
   *                {@link EventStatus#STATUS_SELFLOOP_ONLY},
   *                {@link EventStatus#STATUS_BLOCKED},
   *                {@link EventStatus#STATUS_FAILING},
   *                and {@link EventStatus#STATUS_UNUSED}.
   */
  public void setProperEventStatus(int event, int status);

  /**
   * Gets the number of proposition events in this encoding.
   */
  public int getNumberOfPropositions();

  /**
   * Retrieves whether the given proposition is used. Propositions that
   * are not marked as used are assumed to be not in the alphabet of an
   * automaton and all states are assumed implicitly marked by these
   * propositions.
   * @param  prop   Code of the proposition to be looked up.
   *                Must be in the range of propositions in the encoding.
   */
  public boolean isPropositionUsed(int prop);

  /**
   * Assigns a new value for the used status of the given proposition.
   * @param  prop   Code of the proposition to be modified.
   *                Must be in the range of propositions in the encoding.
   * @param  used   <CODE>true</CODE> to mark the proposition as used,
   *                <CODE>false</CODE> to mark it as unused.
   * @see #isPropositionUsed(int) isPropositionUsed()
   */
  public void setPropositionUsed(int prop, boolean used);

  /**
   * Gets the pattern of propositions marked as used.
   * @return An integer with bits set for each proposition that is marked
   *         as used. If proposition <I>p</I> is marked as used, then the
   *         <I>p</I>-th bit (<CODE>1&nbsp;&lt;&lt;&nbsp;p</CODE>) in the
   *         result is set.
   */
  public int getUsedPropositions();


  //#########################################################################
  //# Class Constants
  /**
   * The maximum number of propositions supported in an event encoding.
   * Currently only 30 propositions are supported to allows proposition
   * sets of states to be encoded in an <CODE>int</CODE> with room for
   * two additional status flags.
   * @see IntStateBuffer
   */
  public int MAX_PROPOSITIONS = 30;

}
