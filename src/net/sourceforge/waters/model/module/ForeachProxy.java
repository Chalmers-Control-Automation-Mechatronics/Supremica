//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ForeachProxy
//###########################################################################
//# $Id: ForeachProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ArrayListProxy;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.MutableNamedProxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionElementFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ForeachType;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;


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
 * MutableNamedProxy}.</DD>
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

public abstract class ForeachProxy extends MutableNamedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct without guard and an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   */
  ForeachProxy(final String dummy, final SimpleExpressionProxy range)
  {
    this(dummy, range, null);
  }

  /**
   * Creates a new foreach construct with an empty body.
   * @param  dummy       The name of the dummy variable.
   * @param  range       The range of iteration.
   * @param  guard       The guard expression.
   */
  ForeachProxy(final String dummy,
	       final SimpleExpressionProxy range,
	       final SimpleExpressionProxy guard)
  {
    super(dummy);
    mRange = range;
    mGuard = guard;
    mBodyProxy = new ForeachBodyProxy();
  }

  /**
   * Creates a copy of a foreach construct.
   * @param  partner     The foreach construct to be copied.
   */
  ForeachProxy(final ForeachProxy partner)
  {
    super(partner);
    mRange = partner.mRange;
    mGuard = partner.mGuard;
    mBodyProxy = new ForeachBodyProxy(partner.mBodyProxy);
  }

  /**
   * Creates a foreach construct from a parsed XML structure.
   * @param  foreach     The parsed XML structure of the new construct.
   * @param  factory     The factory to be used to create body elements.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ForeachProxy(final ForeachType foreach,
	       final ForeachProxyFactory factory)
    throws ModelException
  {
    super(foreach);
    final ProxyFactory exprfactory = new SimpleExpressionProxyFactory();
    final SimpleExpressionType range = foreach.getRange();
    final SimpleExpressionType guard = foreach.getGuard();
    mRange = (SimpleExpressionProxy) exprfactory.createProxy(range);
    mGuard = guard != null ?
      (SimpleExpressionProxy) exprfactory.createProxy(guard) : null;
    mBodyProxy = new ForeachBodyProxy(foreach, factory);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the range of this foreach construct.
   * @return An expression that evaluates to a range and represents the range
   *         of iteration.
   */
  public SimpleExpressionProxy getRange()
  {
    return mRange;
  }

  /**
   * Sets the range of this foreach construct.
   */
  public void setRange(final SimpleExpressionProxy range)
  {
    mRange = range;
  }

  /**
   * Gets the guard of this foreach construct.
   * @return An expression that evaluates to an integer and represents the
   *         guard, or <CODE>null</CODE> to indicate that there is no guard.
   */
  public SimpleExpressionProxy getGuard()
  {
    return mGuard;
  }

  /**
   * Sets the guard of this foreach construct.
   * @param  guard       The new guard expression, or <CODE>null</CODE> to
   *                     remove the guard.
   */
  public void setGuard(final SimpleExpressionProxy guard)
  {
    mGuard = guard;
  }

  /**
   * Gets the body of this foreach construct.
   * @return The list of body elements. Each element is of type {@link
   *         net.sourceforge.waters.model.base.ElementProxy}. The type of
   *         elements depends on the particular type of foreach construct.
   */
  public List getBody()
  {
    return mBodyProxy;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ForeachProxy foreach = (ForeachProxy) partner;
      return
	mRange.equals(foreach.mRange) &&
	(mGuard == null ?
	 foreach.mGuard == null :
	 mGuard.equals(foreach.mGuard)) &&
	mBodyProxy.equals(foreach.mBodyProxy);
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equals(partner)) {
      final ForeachProxy foreach = (ForeachProxy) partner;
      return
	mRange.equals(foreach.mRange) &&
	(mGuard == null ?
	 foreach.mGuard == null :
	 mGuard.equals(foreach.mGuard)) &&
	mBodyProxy.equalsWithGeometry(foreach.mBodyProxy);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print("FOREACH ");
    printer.print(getName());
    printer.print(" IN ");
    mRange.pprint(printer);
    if (mGuard != null) {
      printer.print(" WHERE ");
      mGuard.pprint(printer);
    }
    printer.print(' ');
    mBodyProxy.pprint(printer);
  }

  public void pprintln(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer);
    printer.println();
  }


  //#########################################################################
  //# Marshalling
  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final ElementFactory exprfactory = new SimpleExpressionElementFactory();
    final ForeachType foreach = (ForeachType) factory.createElement(this);
    toJAXBElement(foreach);
    foreach.setRange((SimpleExpressionType) getRange().toJAXB(exprfactory));
    if (getGuard() != null) {
      foreach.setGuard((SimpleExpressionType) getGuard().toJAXB(exprfactory));
    }
    if (mBodyProxy.size() > 0) {
      final ElementType body = mBodyProxy.toJAXB(factory);
      storeBodyElement(foreach, body);
    }
    return foreach;
  }


  //#########################################################################
  //# Provided by Users
  abstract void storeBodyElement(ForeachType foreach, ElementType body);


  //#########################################################################
  //# Local Class ForeachBodyProxy
  private class ForeachBodyProxy extends ArrayListProxy {

    //#######################################################################
    //# Constructor
    private ForeachBodyProxy()
    {
    }

    private ForeachBodyProxy(final Collection input)
    {
      super(input);
    }

    private ForeachBodyProxy(final ForeachType foreach,
			     final ForeachProxyFactory factory)
      throws ModelException
    {
      super(factory.getForeachBody(foreach), factory);
    }
  
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mRange;
  private SimpleExpressionProxy mGuard;
  private final ListProxy mBodyProxy;

}
