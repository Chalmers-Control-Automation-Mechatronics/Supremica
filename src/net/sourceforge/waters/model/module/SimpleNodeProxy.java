//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   SimpleNodeProxy
//###########################################################################
//# $Id: SimpleNodeProxy.java,v 1.3 2005-02-28 19:16:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.LabelGeometryType;
import net.sourceforge.waters.xsd.module.PointGeometryType;
import net.sourceforge.waters.xsd.module.SimpleNodeType;


/**
 * <P>A node representing a state.</P>
 *
 * <P>The states of a finite-state machine are represented as simple nodes
 * in a graph, connected to each other by edges representing the state
 * transitions. A simple node thus can be viewed as a point in a graph
 * ({@link GraphProxy}) with edges ({@link EdgeProxy}) linking to it.</P>
 *
 * <P>The information attached to a simple node is mostly used for
 * rendering.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string identifying the node.</DD>
 * <DT><STRONG>Initial.</STRONG>
 * <DD>A boolean flag identifying whether the node represents an initial
 * state or not. In a <I>deterministic</I> graph, the must be exactly
 * one initial node.</DD>
 * <DT><STRONG>Geometry information.</STRONG></DT>
 * <DD>In addition, each node may have layout information to describe
 * the node's position in the graph, and the placement of the label, i.e.,
 * the name string, relative to the node.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public class SimpleNodeProxy extends NodeProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a simple node.
   * This method creates a simple node that is not initial and has
   * no associated geometry information.
   * @param  name        The name to be given to the new node.
   */
  public SimpleNodeProxy(final String name)
  {
    this(name, false);
  }

  /**
   * Creates a simple node.
   * This method creates a simple node that without associated geometry
   * information.
   * @param  name        The name to be given to the new node.
   * @param  initial     A flag indicating whether the new node is to be
   *                     an initial node.
   */
  public SimpleNodeProxy(final String name, final boolean initial)
  {
    super(name);
    mIsInitial = initial;
  }

  /**
   * Creates a copy of a simple node.
   * @param  partner     The object to be copied from.
   */
  public SimpleNodeProxy(final SimpleNodeProxy partner)
  {
	  super(partner);
	  mIsInitial = partner.mIsInitial;
	  if (partner.mPointGeometry != null) {
		  mPointGeometry = new PointGeometryProxy(partner.mPointGeometry);
	  }
	  if (partner.mLabelGeometry != null) {
		  mLabelGeometry = new LabelGeometryProxy(partner.mLabelGeometry);
	  }
  }

  /**
   * Creates a simple node from a parsed XML structure.
   * @param  state       The parsed XML structure of the new node.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  SimpleNodeProxy(final SimpleNodeType state)
    throws ModelException
  {
    super(state);
    mIsInitial = state.isInitial();
    final PointGeometryType pointgeo = state.getPointGeometry();
    if (pointgeo != null) {
      mPointGeometry = new PointGeometryProxy(pointgeo);
    }
    final LabelGeometryType labelgeo = state.getLabelGeometry();
    if (labelgeo != null) {
      mLabelGeometry = new LabelGeometryProxy(labelgeo);
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this node.
   */
  public Object clone()
  {
    return new SimpleNodeProxy(this);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the initial status of this node.
   * @return <CODE>true</CODE> if this is an initial node,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isInitial()
  {
    return mIsInitial;
  }

  /**
   * Sets the initial status of this node.
   * @param  initial     <CODE>true</CODE> if this node becomes an initial
   *                     node, <CODE>false</CODE> otherwise.
   */
  public void setInitial(boolean initial)
  {
    mIsInitial = initial;
  }

  /**
   * Gets the geometric position of this node.
   * @return A {@link PointGeometryProxy} identifying the position of the
   *         center of the circle representing this node in a graph.
   */
  public PointGeometryProxy getPointGeometry()
  {
    return mPointGeometry;
  }

  /**
   * Sets the geometric position of this node.
   * @param  geo         A {@link PointGeometryProxy} identifying the position
   *                     of the center of the circle representing this node in
   *                     a graph.
   */
  public void setPointGeometry(final PointGeometryProxy geo)
  {
    mPointGeometry = geo;
  }

  /**
   * Gets the geometric position of this node's label.
   * @return A {@link PointGeometryProxy} identifying the position of the
   *         label's anchor point relative to the center of the circle
   *         representing the node.
   */
  public LabelGeometryProxy getLabelGeometry()
  {
    return mLabelGeometry;
  }

  /**
   * Sets the geometric position of this node's label.
   * @return geo         A {@link PointGeometryProxy} identifying the position
   *                     of the label's anchor point relative to the center of
   *                     the circle representing the node.
   */
  public void setLabelGeometry(final LabelGeometryProxy geo)
  {
    mLabelGeometry = geo;
  }

  public Iterator getImmediateChildNodeIterator()
  {
    return Collections.EMPTY_SET.iterator();
  }

  public Iterator getChildNodeIterator()
  {
    final List singleton = Collections.singletonList(this);
    return singleton.iterator();
  }

  public Iterator getSimpleChildNodeIterator()
  {
    final List singleton = Collections.singletonList(this);
    return singleton.iterator();
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final SimpleNodeProxy state = (SimpleNodeProxy) partner;
      return
	isInitial() == state.isInitial();
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (equals(partner)) {
      final SimpleNodeProxy state = (SimpleNodeProxy) partner;
      return
	GeometryProxy.equalGeometry(mPointGeometry, state.mPointGeometry) &&
	GeometryProxy.equalGeometry(mLabelGeometry, state.mLabelGeometry);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (isInitial()) {
      printer.print("initial ");
    }
    printer.print(getName());
    super.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    if (element instanceof SimpleNodeType) {
      final SimpleNodeType state = (SimpleNodeType) element;
      state.setInitial(isInitial());
      if (mPointGeometry != null) {
	final PointGeometryType geo = mPointGeometry.toPointGeometryType();
	state.setPointGeometry(geo);
      }
      if (mLabelGeometry != null) {
	final LabelGeometryType geo = mLabelGeometry.toLabelGeometryType();
	state.setLabelGeometry(geo);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mIsInitial;
  private PointGeometryProxy mPointGeometry;
  private LabelGeometryProxy mLabelGeometry;

}
