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

package net.sourceforge.waters.model.des;

import java.util.Set;

import net.sourceforge.waters.model.base.DocumentProxy;

/**
 * <P>A collection of finite-state machines.</P>
 *
 * <P>A product DES is a set of finite-state machines that are to be
 * composed by the synchronous product operation, synchronising by shared
 * events. The product DES includes the event alphabet available to all
 * finite-state machines, but each finite-state machine has its own event
 * alphabet identifying the set of events it actually synchronises on.</P>
 *
 * <P>In contrast to the module representation ({@link
 * net.sourceforge.waters.model.module.ModuleProxy}), the product DES
 * representation is very simple and supports no parametric
 * structures. Most product DES are obtained from a module by
 * compilation.</P>
 * 
 * @see net.sourceforge.waters.model.module.ModuleProxy
 * @see net.sourceforge.waters.model.compiler.ModuleCompiler
 *
 * @author Robi Malik
 */
public interface ProductDESProxy
  extends DocumentProxy
{
  //#########################################################################
  //# Cloning
  public ProductDESProxy clone();

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the set of events for this product DES.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Set<EventProxy> getEvents();

  /**
   * Gets the list of automata for this product DES.
   * @return  An unmodifiable list of objects of type {@link AutomatonProxy}.
   */
  public Set<AutomatonProxy> getAutomata();
}
