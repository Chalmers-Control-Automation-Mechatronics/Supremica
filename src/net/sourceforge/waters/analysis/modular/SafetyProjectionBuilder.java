//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.modular;

import java.util.Set;

import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * The interface for projection algorithms used in safety verification.
 *
 * A projection builder takes one or more automata as input, builds their
 * synchronous product, then hides events to be projected out, and afterwards
 * uses subset construction to build an automaton accepting the same language.
 * The final result may be minimised before it is returned.
 *
 * @author Robi Malik
 */
public interface SafetyProjectionBuilder
  extends AutomatonBuilder
{

  //#########################################################################
  //# Configuration
  /**
   * Gets the set of events projected out by this projection.
   * Hidden events are replaced by silent events (epsilon-moves)
   * after synchronous composition and before subset construction.
   */
  public Set<EventProxy> getHidden();

  /**
   * Sets the set of events projected out by this projection.
   * @see #getHidden()
   */
  public void setHidden(final Set<EventProxy> hidden);

  /**
   * Gets the set of forbidden events of this safety projection.
   * In safety verification, it may be known for certain events that the
   * property to be verified fails if one of these events is possible.
   * With this knowledge, state exploration can be stopped as soon as a
   * forbidden event is found to be possible, leading to a simpler
   * projection result.
   */
  public Set<EventProxy> getForbidden();

  /**
   * Sets the set of forbidden events of this safety projection.
   * @see #getForbidden()
   */
  public void setForbidden(final Set<EventProxy> forbidden);

}
