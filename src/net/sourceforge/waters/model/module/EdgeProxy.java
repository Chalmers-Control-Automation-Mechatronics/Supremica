//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   EdgeProxy
//###########################################################################
//# $Id: EdgeProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.EdgeListType;
import net.sourceforge.waters.xsd.module.EdgeType;
import net.sourceforge.waters.xsd.module.LabelBlockType;
import net.sourceforge.waters.xsd.module.PointGeometryType;
import net.sourceforge.waters.xsd.module.SplineGeometryType;


/**
 * <P>An edge in a graph.</P>
 *
 * <P>The transitions between the states of a finite-state machine are
 * represented as edges between the nodes of a graph.  These edges are
 * directed, i.e., they can be considered as arrows that link two nodes
 * ({@link NodeProxy}), called the <I>source</I> and <I>target</I>. In
 * addition, each edge has an associated set of <I>labels</I> that
 * represent the events for the transition. Each edge can have several
 * labels in its label block ({@link LabelBlockProxy}), and each label will
 * produce its own transition when the graph is translated into a
 * finite-state machine.</P>
 *
 * <P>The way how an edge is to be rendered graphically is stored in
 * separate <I>geometry</I> objects.</P>
 *
 * @author Robi Malik
 */

public class EdgeProxy extends ElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edge.
   * This constructor creates a new edge linking the two given nodes,
   * and initialises it with an empty label block and without
   * geometry information.
   * @param  source      The source node of the new edge.
   * @param  target      The target node of the new edge.
   */
  public EdgeProxy(final NodeProxy source, final NodeProxy target)
  {
    mSource = source;
    mTarget = target;
    mLabelBlock = new LabelBlockProxy();
  }

  /**
   * Creates an edge from a parsed XML structure.
   * @param  edge        The parsed XML structure of the new edge.
   * @param  factory     The factory to be used to identify nodes
   *                     by their names.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EdgeProxy(final EdgeType edge, final NodeLookupFactory factory)
    throws ModelException
  {
    mSource = factory.findNode(edge.getSource());
    mTarget = factory.findNode(edge.getTarget());
    mLabelBlock = new LabelBlockProxy(edge.getLabelBlock());
    final SplineGeometryType geo = edge.getSplineGeometry();
    if (geo != null) {
      mGeometry = new SplineGeometryProxy(geo);
    }
    final PointGeometryType start = edge.getStartPointGeometry();
    if (start != null) {
      mStartPoint = new PointGeometryProxy(start);
    }
    final PointGeometryType end = edge.getEndPointGeometry();
    if (end != null) {
      mEndPoint = new PointGeometryProxy(end);
    }
  }


  //#########################################################################
  //# Getters and Setters
  public LabelBlockProxy getLabelBlock()
  {
    return mLabelBlock;
  }

  /**
   * Gets the source node of this edge.
   */
  public NodeProxy getSource()
  {
    return mSource;
  }

  /**
   * Gets the target node of this edge.
   */
  public NodeProxy getTarget()
  {
    return mTarget;
  }

  /**
   * Sets the source node of this edge.
   * @param  source      The new source node, which must be a node of the
   *                     same graph.
   * @throws IllegalArgumentException to indicate that the given node has not
   *                     been registered with the edge's graph yet.
   */
  public void setSource(final NodeProxy source)
  {
    if (source.getMap() == mSource.getMap()) {
      mSource = source;
    } else {
      throw new IllegalArgumentException
	("Old and new source state belong to different symbol tables!");
    }
  }

  /**
   * Sets the target node of this edge.
   * @param  target      The new target node, which must be a node of the
   *                     same graph.
   * @throws IllegalArgumentException to indicate that the given node has not
   *                     been registered with the edge's graph yet.
   */
  public void setTarget(final NodeProxy target)
  {
    if (target.getMap() == mTarget.getMap()) {
      mTarget = target;
    } else {
      throw new IllegalArgumentException
	("Old and new target state belong to different symbol tables!");
    }
  }

  /**
   * Gets the rendering information for this edge.
   * @return A spline geometry object containing the control points
   *         for the edge, or <CODE>null</CODE> if the edge is rendered as
   *         a straight line. The spline geometry does not contain the
   *         start and end points of the edge, as these can be obtained
   *         from the position of the source and target nodes, or the
   *         #getStartPoint() and #getEndPoint() methods.
   */
  public SplineGeometryProxy getGeometry()
  {
    return mGeometry;
  }

  /**
   * Sets the rendering information for this edge.
   * @param  geo         The new geometry information, or <CODE>null</CODE>
   *                     if the edge is to rendered as a straight line.
   * @see    #getGeometry()
   */
  public void setGeometry(final SplineGeometryProxy geo)
  {
    mGeometry = geo;
  }

  /**
   * Gets the rendering information for the start point of this edge.
   * For most edges, the location of the start point is the position of
   * the source node. But in some cases, namely if the source node
   * is a group node ({@link GroupNodeProxy}), an alternative start
   * position may be specified.
   * @return A point geometry object defining an alternative start
   *         point, or <CODE>null</CODE> if the start point is obtained
   *         from the position of the source node.
   */
  public PointGeometryProxy getStartPoint()
  {
    return mStartPoint;
  }

  /**
   * Sets the rendering information for the start point of this edge.
   * @param  geo         The new position of the start point,
   *                     or <CODE>null</CODE> if the start point is
   *                     to be obtained from the position of the source node.
   * @see    #getStartPoint()
   */
  public void setStartPoint(final PointGeometryProxy geo)
  {
    mStartPoint = geo;
  }

  /**
   * Gets the rendering information for the end point of this edge.
   * For most edges, the location of the end point is the position of the
   * target node. But in some cases, namely if the target node is a group
   * node ({@link GroupNodeProxy}), an alternative end position may be
   * specified. This only makes sense in a nondeterministic graph.
   * @return A point geometry object defining an alternative end
   *         point, or <CODE>null</CODE> if the end point is obtained
   *         from the position of the target node.
   */
  public PointGeometryProxy getEndPoint()
  {
    return mEndPoint;
  }

  /**
   * Sets the rendering information for the end point of this edge.
   * @param  geo         The new position of the end point,
   *                     or <CODE>null</CODE> if the end point is
   *                     to be obtained from the position of the target node.
   * @see    #getEndPoint()
   */
  public void setEndPoint(final PointGeometryProxy geo)
  {
    mEndPoint = geo;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final EdgeProxy edge = (EdgeProxy) partner;
      return
	getSource().refequals(edge.getSource()) &&
	getTarget().refequals(edge.getTarget()) &&
	getLabelBlock().equals(edge.getLabelBlock());
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass() &&
	super.equals(partner)) {
      final EdgeProxy edge = (EdgeProxy) partner;
      return
	getSource().refequals(edge.getSource()) &&
	getTarget().refequals(edge.getTarget()) &&
	getLabelBlock().equalsWithGeometry(edge.getLabelBlock()) &&
	GeometryProxy.equalGeometry(mGeometry, edge.mGeometry) &&
	GeometryProxy.equalGeometry(mStartPoint, edge.mStartPoint) &&
	GeometryProxy.equalGeometry(mEndPoint, edge.mEndPoint);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print(mSource.getName());
    printer.print(" -> ");
    printer.print(mTarget.getName());
    printer.print(' ');
    mLabelBlock.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final EdgeType edge = (EdgeType) element;
    edge.setSource(getSource().getName());
    edge.setTarget(getTarget().getName());
    final LabelBlockType list = mLabelBlock.toLabelBlockType();
    edge.setLabelBlock(list);
    if (mGeometry != null) {
      final SplineGeometryType geo = mGeometry.toSplineGeometryType();
      edge.setSplineGeometry(geo);
    }
    if (mStartPoint != null) {
      final PointGeometryType geo = mStartPoint.toStartPointGeometryType();
      edge.setStartPointGeometry(geo);
    }
    if (mEndPoint != null) {
      final PointGeometryType geo = mStartPoint.toEndPointGeometryType();
      edge.setEndPointGeometry(geo);
    }
  }


  //#########################################################################
  //# Local Class EdgeProxyFactory
  static class EdgeProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Constructor
    EdgeProxyFactory(final NodeLookupFactory factory)
    {
      mFactory = factory;
    }

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final EdgeType edge = (EdgeType) element;
      return new EdgeProxy(edge, mFactory);
    }

    public List getList(final ElementType parent)
    {
      final EdgeListType list = (EdgeListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Data Members
    private final NodeLookupFactory mFactory;

  }


  //#########################################################################
  //# Local Class EdgeElementFactory
  static class EdgeElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createEdge();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createEdgeList();
    }

    public List getElementList(final ElementType container)
    {
      final EdgeListType list = (EdgeListType) container;
      return list.getList();
    }

  }


  //#########################################################################
  //# Data Members
  private final LabelBlockProxy mLabelBlock;
  private NodeProxy mSource;
  private NodeProxy mTarget;
  private SplineGeometryProxy mGeometry;
  private PointGeometryProxy mStartPoint;
  private PointGeometryProxy mEndPoint;

}
