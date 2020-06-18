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

import java.util.List;


/**
 * <P>A module component to be replaced by the contents of another module.</P>
 *
 * <P>Instances can occur in a module's component list. They represent
 * instructions to insert all components of another module after
 * substitution often parameters.</P>
 *
 * <P>Each instance has the following components.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>The name to be used for the instance. The name is used to prefix all
 * the components and events in the instantiated module. It can be a
 * structured identifier.</DD>
 * <DT><STRONG>Module.</STRONG></DT>
 * <DD>The module to be instantiated.</DD>
 * <DT><STRONG>Binding list.</STRONG></DT>
 * <DD>A list of pairs of names and expressions ({@link ParameterBindingProxy})
 * that describes the values to be used for the parameters of the instantiated
 * module.</DD>
 * </DL>
 *
 * <P>As an example, consider the following simple <I>machine</I> module,
 * which has two event parameters <CODE>start</CODE> and
 * <CODE>finish</CODE>.</P>
 *
 * <PRE>
 *   MODULE machine;
 *   PARAMETERS
 *     controllable start;
 *     uncontrollable finish;
 *   EVENTS
 *     uncontrollable break;
 *     controllable repair;
 *   COMPONENTS
 *     plant mach;
 * </PRE>
 *
 * <P>This module may be instantiated from another module <I>factory</I>
 * as follows.</P>
 *
 * <PRE>
 *   MODULE factory;
 *   EVENTS
 *     controllable start1;
 *     uncontrollable finish1;
 *     ...
 *   COMPONENTS
 *     machine1 = machine(
 *                  start = start1;
 *                  finish = finish1;
 *                );
 *     ...
 * </PRE>
 *
 * <P>This will include all automata of the <I>machine</I> module in
 * <I>factory</I> after replacing the events <CODE>start</CODE> and
 * <CODE>finish</CODE> in <I>machine</I> by <CODE>start1</CODE> and
 * <CODE>finish1</CODE> from <I>factory</I>, respectively. The compiled
 * <I>factory</I> model will include new events <CODE>machine1.break</CODE>
 * and <CODE>machine1.repair</CODE>, and a plant automaton
 * <CODE>machine1.mach</CODE> which is result of applying the event
 * substitution to the automaton from the <I>machine</I> module.</P>
 *
 * @author Robi Malik
 */

public interface InstanceProxy extends ComponentProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the module name of this instance.
   * The module name is given as a string identifying a complete name,
   * if necessary, but without extension.
   */
  public String getModuleName();

  /**
   * Gets the binding list of this instance.
   * @return A list of name-value pairs describing how the parameters
   *         of the instantiated module are bound to values.
   */
  public List<ParameterBindingProxy> getBindingList();

}
