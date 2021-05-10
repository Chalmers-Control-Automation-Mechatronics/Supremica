//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A loop construct.</P>
 *
 * <P>Foreach constructs can be used in a module's component list or
 * on an edge's event list to include several similar items by
 * iteration. An example is found in the small factory module,
 * where two similar instances <CODE>machine[1]</CODE> and
 * <CODE>machine[2]</CODE> are to be created.</P>
 *
 * <PRE>
 *   FOREACH i IN 1..2
 *     instance machine[i] = machine(
 *       start = start[i];
 *       finish = finish[i];
 *     );
 * </PRE>
 *
 * <P>A more complicated example is the following, which consists of two
 * nested foreach constructs and a conditional block ({@link
 * ConditionalProxy}), creating instances
 * <CODE>square[1][2]</CODE>, <CODE>square[1][3]</CODE>,
 * <CODE>square[2][1]</CODE>, <CODE>square[2][3]</CODE>,
 * <CODE>square[3][2]</CODE>, and <CODE>square[3][3]</CODE>.</P>
 *
 * <PRE>
 *   FOREACH i IN 1..3
 *     FOREACH j IN 1..3
 *       IF i != j
 *         instance square[i][j] = square(...);
 * </PRE>
 *
 * <P>Every foreach construct contains the following information.</P>
 * <DL>
 * <DT><I>Name.</I></DT>
 * <DD>A string identifying the name of the dummy variable that changes
 * during iteration. In the examples above, the name of the dummy variable
 * would be <CODE>"i"</CODE> or&nbsp;<CODE>"j"</CODE>, respectively. This
 * attribute is inherited from the superclass {@link
 * NamedProxy}.</DD>
 * <DT><I>Range.</I></DT>
 * <DD>An expression that evaluates to the range of values to be assumed
 * by the dummy variable during iteration. In the examples above, the
 * ranges are <CODE>1..2</CODE> or&nbsp;<CODE>1..2</CODE>, respectively.</DD>
 * <DT><I>Body.</I></DT>
 * <DD>A list of entries that are to be evaluated for each assignment of
 * the dummy variable to an element in the range, provided that the guard
 * is satisfied. Nested foreach constructs are possible in addition to leaf
 * entries of various types. In the above examples, the bodies contain only
 * a single entry, but the number of entries in a foreach body is not
 * limited in general.</DD>
 * </DL>
 *
 * @author Robi Malik
 */
// @short foreach construct

public interface ForeachProxy extends NestedBlockProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the name of the dummy variable of this foreach construct.
   */
  public String getName();

  /**
   * Gets the range of this foreach construct.
   * @return An expression that evaluates to a range and represents the range
   *         of iteration.
   */
  public SimpleExpressionProxy getRange();

}
