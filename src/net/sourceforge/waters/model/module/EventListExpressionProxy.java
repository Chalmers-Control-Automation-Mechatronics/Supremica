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

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>An expression defined by an event list.</P>
 *
 * <P>Event lists are used in various places in a module, where several
 * events are grouped together. The most common application is on
 * transitions in graphs ({@link LabelBlockProxy}), but event lists can
 * also be used for parameter bindings ({@link ParameterBindingProxy}) of
 * instance components or alias declarations ({@link AliasProxy}) in a
 * module's event alias list.</P>
 *
 * <P>Technically, an event list is a wrapper of an object implementing the
 * {@link java.util.List} interface, which can have two different kinds of
 * elements.</P>
 * <DL>
 * <DT>{@link IdentifierProxy}</DT>
 * <DD>Identifiers are used to include a single event with a given name (or
 * all elements of an array of events) in an event list. There can be
 * simple identifiers ({@link SimpleIdentifierProxy}) that are just
 * names or indexed identifiers ({@link IndexedIdentifierProxy}) that can have
 * one or more array indexes.</DD>
 * <DT>{@link ForeachProxy}</DT>
 * <DD>This construct can be used to include several events by processing a
 * loop.</DD>
  *
 * @author Robi Malik
 */

public interface EventListExpressionProxy extends ExpressionProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of event identifiers constituting this event list
   * expression. Each element is of type {@link
   * net.sourceforge.waters.model.module.IdentifierProxy IdentifierProxy} or
   * {@link net.sourceforge.waters.model.module.ForeachProxy ForeachProxy}.
   * @return A list of identifiers or expressions.
   */
  public List<Proxy> getEventIdentifierList();

}








