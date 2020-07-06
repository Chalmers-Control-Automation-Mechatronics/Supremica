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

/**
 * <P>A conditional (<CODE>if</CODE>) construct.</P>
 *
 * <P>Conditional constructs can be used in a module's component list or
 * on an edge's event list to restrict the occurrence of certain elements
 * based on a programmable condition. The condition is typically evaluated
 * during instantiation. Alternatively, if it occurs on the edge of a state
 * machine, the condition may include EFSM variables and act as an EFSM
 * guard.</P>
 * @author Robi Malik
 */
// @short conditional construct

public interface ConditionalProxy extends NestedBlockProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the guard of this conditional construct.
   * @return An expression that evaluates to an integer and represents the
   *         condition that must be true for the elements in the body to
   *         be compiled.
   */
  public SimpleExpressionProxy getGuard();

}
