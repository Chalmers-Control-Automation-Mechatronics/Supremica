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

package net.sourceforge.waters.model.des;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A state of an automaton.</P>
 *
 * <P>A state consists of a <I>name</I> (inherited from {@link NamedProxy}),
 * its <I>initial state</I> status, and a collection of <I>propositions</I>
 * or <I>markings</I>.
 *
 * @see AutomatonProxy
 * @author Robi Malik
 */

public interface StateProxy
  extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Returns whether this is an initial state of its automaton.
   */
  public boolean isInitial();

  /**
   * Gets the collection of propositions associated with this state.
   * Propositions are generally used to determine whether a state is marked.
   * In a standard model, the list of propositions will either be empty
   * or contain a single proposition event with the {@link
   * net.sourceforge.waters.model.module.EventDeclProxy#DEFAULT_MARKING_NAME
   * DEFAULT_MARKING_NAME}. In multi-coloured models or Kripke-structures,
   * there may be more than one proposition associated with a state.
   * @return  An unmodifiable collection of events, which should all be of
   *          type {@link net.sourceforge.waters.xsd.base.EventKind#PROPOSITION
   *          PROPOSITION}.
   */
  public Collection<EventProxy> getPropositions();

}
