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

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;


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
 * nested foreach constructs and includes a guard, creating instances
 * <CODE>square[1][2]</CODE>, <CODE>square[1][3]</CODE>,
 * <CODE>square[2][1]</CODE>, <CODE>square[2][3]</CODE>,
 * <CODE>square[3][2]</CODE>, and <CODE>square[3][3]</CODE>.</P>
 *
 * <PRE>
 *   FOREACH i IN 1..3
 *     FOREACH j IN 1..3 WHERE i != j
 *       instance square[i][j] = square(...);
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
 * <DT><I>Guard.</I></DT>
 * <DD>An optional expression that evaluates to an integer, <CODE>0</CODE>
 * indicating <I>false</I> and other numbers indicating
 * <CODE>true</CODE>. The body of a foreach construct will only be
 * evaluated for those entries in the range where the guard evaluates to
 * <I>true</I>.  A missing guard is assumed to be always <I>true</I>. In
 * the above examples, only the last foreach construct has a guard,
 * <CODE>i&nbsp;!=&nbsp;j</CODE>.</DD>
 * <DT><I>Body.</I></DT>
 * <DD>A list of entries that are to be evaluated for each assignment of
 * the dummy variable to an element in the range, provided that the guard
 * is satisfied. The type of entries in the body varies depending on the
 * type of foreach construct-see the subclasses. In most cases, nested
 * foreach constructs are possible in addition to leaf entries. In the
 * above examples, the bodies contain only a single entry, but the number
 * of entries in a foreach body is not limited in general.</DD>
 * </DL>
 *
 * <P>There are different types of foreach constructs, depending on the
 * context in which they can occur, and on the types of entries their
 * bodies can contain.</P>
 *
 * <UL>
 * <LI><I>Foreach-component</I> constructs occur in the <I>component list</I>
 * of a module. Their body entries can be of type {@link SimpleComponentProxy},
 * {@link VariableComponentProxy}, {@link InstanceProxy}, or
 * {@link ForeachProxy}.</LI>
 * <LI><I>Foreach-event</I> constructs occur in <I>event lists</I> ({@link
 * EventListExpressionProxy}), which may occur on a graph's edge, in an alias
 * definition, or in the actual parameter of an instance. Their body entries
 * can be of type {@link IdentifierProxy} or {@link ForeachProxy}.</LI>
 * <LI><I>Foreach-event-alias</I> constructs occur in the <I>event alias
 * list</I> of a module. Their body entres can be of type
 * {@link EventAliasProxy} or {@link ForeachProxy}.</LI>
 * </UL>
 *
 * @author Robi Malik
 */
// @short foreach construct

public interface ForeachProxy extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the range of this foreach construct.
   * @return An expression that evaluates to a range and represents the range
   *         of iteration.
   */
  public SimpleExpressionProxy getRange();

  /**
   * Gets the guard of this foreach construct.
   * @return An expression that evaluates to an integer and represents the
   *         guard, or <CODE>null</CODE> to indicate that there is no guard.
   */
  // @optional
  public SimpleExpressionProxy getGuard();

  /**
   * Gets the body of this foreach construct.
   * @return The list of body elements. The possible types of
   *         elements depends on the particular type of foreach construct.
   */
  public List<Proxy> getBody();

}
