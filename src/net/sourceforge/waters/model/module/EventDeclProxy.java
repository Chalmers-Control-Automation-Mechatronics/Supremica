//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EventDeclProxy
//###########################################################################
//# $Id: EventDeclProxy.java,v 1.2 2005-02-21 19:19:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ArrayListProxy;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.UnexpectedWatersException;
import net.sourceforge.waters.model.base.UniqueElementProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionElementFactory;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ColorGeometryType;
import net.sourceforge.waters.xsd.module.EventBaseType;
import net.sourceforge.waters.xsd.module.EventDeclListType;
import net.sourceforge.waters.xsd.module.IdentifierType;


/**
 * <P>An event declaration.</P>
 *
 * <P>Event declarations are used to declare an event or array of events by
 * introducing its name and giving the required type information. They are
 * found in the event declaration list of a module ({@link
 * ModuleProxy}).</P>
 *
 * <P>Each event declaration has the following components.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string defining the name of the event. It is unique within an
 * a module and must obey syntactic restrictions for identifiers.</DD>
 * <DT><STRONG>Kind.</STRONG></DT>
 * <DD>The type of the events obtained from the declaration.
 * This can be <I>controllable</I>, <I>uncontrollable</I>,
 * or <I>proposition</I>.</DD>
 * <DT><STRONG>Observability.</STRONG></DT>
 * <DD>A boolean flag, indicating whether the events obtained from the
 * declaration are considered <I>observable</I>.</DD>
 * <DT><STRONG>Indexes.</STRONG></DT>
 * <DD>Event arrays are declared by specifying a list of index ranges,
 * each defining the possible range of indexes at one index position.</DD>
 * <DT><STRONG>Color Geometry.</STRONG></DT>
 * <DD>Events of kind <I>proposition</I> may have a color associated to
 * them, which then defines how nodes marked with the proposition are
 * rendered.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class EventDeclProxy extends UniqueElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a simple event declaration.
   * This constructor creates an observable event declaration.
   * @param  name        The name of the new event declaration.
   * @param  kind        The kind of the new event declaration.
   */
  public EventDeclProxy(final String name, final EventKind kind)
  {
    this(name, kind, true);
  }

  /**
   * Creates a simple event declaration.
   * @param  name        The name of the new event declaration.
   * @param  kind        The kind of the new event declaration.
   * @param  observable  <CODE>true</CODE> if the event declaration is to
   *                     be observable, <CODE>false</CODE> otherwise.
   */
  public EventDeclProxy(final String name,
			final EventKind kind,
			final boolean observable)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
    mRangeListProxy = new RangeListProxy();
    mColorGeometry = null;
  }

  /**
   * Creates an event declaration from a parsed XML structure.
   * @param  decl        The parsed XML structure of the event declaration.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventDeclProxy(final EventBaseType decl)
    throws ModelException
  {
    super(decl);
    mKind = decl.getKind();
    mIsObservable = decl.isObservable();
    mRangeListProxy = new RangeListProxy(decl);
    final ColorGeometryType geo = decl.getColorGeometry();
    if (geo != null) {
      mColorGeometry = new ColorGeometryProxy(geo);
    }
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind of this event declaration.
   * @return One of {@link EventKind#CONTROLLABLE},
   *         {@link EventKind#UNCONTROLLABLE}, or
   *         {@link EventKind#PROPOSITION}.
   */
  public EventKind getKind()
  {
    return mKind;
  }

  /**
   * Sets the kind of this event declaration.
   * @param  kind        The new event kind, one of
   *                     {@link EventKind#CONTROLLABLE},
   *                     {@link EventKind#UNCONTROLLABLE}, or
   *                     {@link EventKind#PROPOSITION}.
   */
  public void setKind(final EventKind kind)
  {
    mKind = kind;
  }

  /**
   * Gets the observability status of this event declaration.
   * @return <CODE>true</CODE> if the event declaration is observable,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isObservable()
  {
    return mIsObservable;
  }

  /**
   * Sets the observability status of this event declaration.
   * @param  observable  <CODE>true</CODE> if the event declaration is to
   *                     be observable, <CODE>false</CODE> otherwise.
   */
  public void setObservable(final boolean observable)
  {
    mIsObservable = observable;
  }

  /**
   * Gets the number of array dimensions of this event.
   * The arity of an event is the number of array indexes that it can
   * take.
   */
  public int getArity()
  {
    return mRangeListProxy.size();
  }

  /**
   * Gets the index ranges of this event.
   * @return A (modifiable) list of expressions, each identifying
   *         range of array indexes for the correspoding dimension.
   *         Each element is of type {@link SimpleExpressionProxy}.
   */
  public List getRanges()
  {
    return mRangeListProxy;
  }

  /**
   * Gets the color information for this event.
   * Events of kind <I>proposition</I> may have a color associated to them,
   * which defines how nodes marked with the proposition are rendered. This
   * information may be missing, in which case a default color is to be
   * used, and it is meaningless for events that are not of kind
   * <I>proposition</I>.
   * @return A color geometry object, or <CODE>null</CODE> if none
   *         is specified.
   */
  public ColorGeometryProxy getColorGeometry()
  {
    return mColorGeometry;
  }

  /**
   * Sets the color information for this event.
   * @param geo          The new color information or <CODE>null</CODE> to
   *                     reset it to default.
   * @see #getColorGeometry()
   */
  public void setColorGeometry(final ColorGeometryProxy geo)
  {
    mColorGeometry = geo;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final EventDeclProxy decl = (EventDeclProxy) partner;
      return
	mKind.equals(decl.mKind) &&
	(mIsObservable == decl.mIsObservable) &&
	getRanges().equals(decl.getRanges());
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (equals(partner)) {
      final EventDeclProxy decl = (EventDeclProxy) partner;
      return GeometryProxy.equalGeometry(mColorGeometry, decl.mColorGeometry);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  /**
   * Returns a printable string describing this event declaration with its
   * ranges.  This method returns a string consisting just of the event
   * name followed by its ranges enclosed in square brackets. For example,
   * on a controllable event array named <CODE>put</CODE> with two index
   * ranges, this method might return the string
   * <CODE>"put[1..3][1..3]"</CODE>.
   * @see #getName()
   * @see #toString()
   */
  public String getNameWithRanges()
  {
    try {
      final StringWriter writer = new StringWriter();
      final ModelPrinter printer = new ModelPrinter(writer);
      pprint(printer, false);
      return writer.toString();
    } catch (final IOException exception) {
      throw new UnexpectedWatersException(exception);
    }
  }

  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    pprint(printer, true);
  }

  private void pprint(final ModelPrinter printer, final boolean withkind)
    throws IOException
  {
    if (withkind) {
      final EventKind kind = getKind();
      final String kindname = kind.toString();
      final String lowername = kindname.toLowerCase();
      printer.print(lowername);
      if (!isObservable()) {
	printer.print("unobservable ");
      }
      printer.print(' ');
    }
    printer.print(getName());
    final Iterator iter = mRangeListProxy.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy expr = (SimpleExpressionProxy) iter.next();
      printer.print('[');
      expr.pprint(printer);
      printer.print(']');
    }
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final EventBaseType decl = (EventBaseType) element;
    decl.setKind(getKind());
    decl.setObservable(isObservable());
    final ElementFactory factory = new RangeElementFactory(decl);
    mRangeListProxy.toJAXB(factory);
    if (mColorGeometry != null) {
      final ColorGeometryType geo = mColorGeometry.toColorGeometryType();
      decl.setColorGeometry(geo);
    }
   }


  //#########################################################################
  //# Local Class EventDeclProxyFactory
  static class EventDeclProxyFactory implements ProxyFactory
  {
    //#######################################################################
    //# Interface waters.model.module.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final EventBaseType decl = (EventBaseType) element;
      return new EventDeclProxy(decl);
    }

    public List getList(final ElementType parent)
    {
      final EventDeclListType list = (EventDeclListType) parent;
      return list.getList();
    }
  }


  //#########################################################################
  //# Local Class EventDeclElementFactory
  static class EventDeclElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEventDecl();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createEventDeclList();
    }

    public List getElementList(final ElementType container)
    {
      final EventDeclListType list = (EventDeclListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Local Class RangeListProxy
  private static class RangeListProxy extends ArrayListProxy {

    //#######################################################################
    //# Constructor
    RangeListProxy()
    {
    }

    RangeListProxy(final EventBaseType decl)
      throws ModelException
    {
      super(decl, new RangeProxyFactory());
    }

  }


  //#########################################################################
  //# Local Class RangeProxyFactory
  private static class RangeProxyFactory extends SimpleExpressionProxyFactory
  {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public List getList(final ElementType parent)
    {
      final EventBaseType decl = (EventBaseType) parent;
      return decl.getRanges();
    }

  }


  //#########################################################################
  //# Local Class RangeElementFactory
  private static class RangeElementFactory
    extends SimpleExpressionElementFactory
  {

    //#######################################################################
    //# Constructor
    RangeElementFactory(final EventBaseType decl)
    {
      mEventDecl = decl;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createContainerElement()
    {
      return mEventDecl;
    }

    public List getElementList(final ElementType container)
    {
      final EventBaseType list = (EventBaseType) container;
      return list.getRanges();
    }

    //#######################################################################
    //# Data Members
    private final EventBaseType mEventDecl;

  }


  //#########################################################################
  //# Data Members
  private EventKind mKind;
  private boolean mIsObservable;
  private ColorGeometryProxy mColorGeometry;
  private final ListProxy mRangeListProxy;

}
