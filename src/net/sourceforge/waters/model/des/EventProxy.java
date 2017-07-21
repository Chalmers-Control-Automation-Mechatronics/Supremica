//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.util.Map;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>An event used by the automata in a DES.</P>
 *
 * <P>Each {@link ProductDESProxy} object consists of a set of automata
 * and an event alphabet. Each event can be used by one or more automata;
 * synchronisation is modelled by several automata using the same event.
 * The {@link ProductDESProxy} data structure is set up in such a way
 * that each event object is used only once. If several automata (or
 * transitions) share an event, they all will use the same object.</P>
 *
 * <P>In contrast to the event declarations of a module (class {@link
 * net.sourceforge.waters.model.module.EventDeclProxy}), the events
 * in a {@link ProductDESProxy} represent single events only.
 * Each event contains the following information.</P>
 *
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string defining the name of the event. This name may be
 * a result from compilation of a module and therefore may contain
 * special charcters, e.g., <CODE>machine[1].start</CODE>.</DD>
 * <DT><STRONG>Kind.</STRONG></DT>
 * <DD>The type of the event. This can be <I>controllable</I>,
 * <I>uncontrollable</I>, or <I>proposition</I>.</DD>
 * <DT><STRONG>Observability.</STRONG></DT>
 * <DD>A boolean flag, indicating whether the event is
 * <I>observable</I>.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public interface EventProxy
  extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind of this event.
   * @return One of {@link EventKind#CONTROLLABLE},
   *         {@link EventKind#UNCONTROLLABLE}, or
   *         {@link EventKind#PROPOSITION}.
   */
  public EventKind getKind();

  /**
   * Gets the observability status of this event.
   * @return <CODE>true</CODE> if the event is observable,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isObservable();

  /**
   * Gets the attribute map for this event.
   * The attribute map can be used by tools supporting external model
   * formats to store information that does not appear in standard DES
   * models.
   * @return An immutable map mapping attribute names to values.
   */
  public Map<String,String> getAttributes();

}
