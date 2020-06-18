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

package net.sourceforge.waters.model.module;

import java.util.Map;

import net.sourceforge.waters.model.base.ComponentKind;

/**
 * <P>A component representing a single finite-state machine.</P>
 *
 * <P>Simple components are the basic way of representing the <I>plants</I>,
 * <I>specifications</I>, and <I>properties</I> in a Waters module.
 * Each simple component contains of the following information.</P>
 *
 * <DL>
 * <DT><I>Name.</I></DT>
 * <DD>The name uniquely identifies the component in its module.
 * It is of type {@link IdentifierProxy}, to support structured names
 * as they may occur in parameterised structures.</DD>
 * <DT><I>Kind.</I></DT>
 * <DD>The kind of a component identifies it as a <I>plants</I>,
 * <I>specification</I>, or <I>properties</I>. It uses the enumerative
 * type {@link ComponentKind}.</DD>
 * <DT><I>Graph.</I></DT>
 * <DD>The graph shows the states and transitions of the finite-state
 * machine representing the component. It is of type {@link GraphProxy}.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public interface SimpleComponentProxy extends ComponentProxy {


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind (plant, specification, etc.) of the automaton of this simple
   * component.
   */
  public ComponentKind getKind();

  /**
   * Gets the graph that defines the automaton of this simple component.
   */
  public GraphProxy getGraph();

  /**
   * Gets the attribute map for this simple component.
   * The attribute map can be used by tools supporting external model
   * formats to store information that does not appear in standard DES
   * models.
   * @return An immutable map mapping attribute names to values.
   */
  public Map<String,String> getAttributes();

}
