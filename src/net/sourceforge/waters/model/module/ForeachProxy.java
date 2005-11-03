//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ForeachProxy
//###########################################################################
//# $Id: ForeachProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
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
   * @return The list of body elements. Each element is of type {@link
   *         net.sourceforge.waters.model.base.Proxy}. The type of
   *         elements depends on the particular type of foreach construct.
   */
  public List<Proxy> getBody();

}
