//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorGraph
//###########################################################################
//# $Id: EditorGraph.java,v 1.14 2007-02-06 04:31:47 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.unchecked.Casting;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.MutableSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


public class EditorGraph
  extends MutableSubject
  implements GraphProxy
{
  public EditorGraph(final GraphSubject graph)
  {
		mGraph = graph;
		mChanged = new HashSet<Subject>();
		mObserverMap = new IdentityHashMap<NodeSubject, EditorNode>(graph.getNodes().size());
		mFakeMap = new IdentityHashMap<Subject, Subject>(graph.getNodes().size() + graph.getEdges().size()+1);
		mOriginalMap = new IdentityHashMap<Subject, Subject>(graph.getNodes().size() + graph.getEdges().size()+1);
		mEdges = new ArrayListSubject<EdgeSubject>(graph.getEdges().size());
		mNodes = new IndexedHashSetSubject<NodeSubject>(graph.getNodes().size());
		mEdges.setParent(this);
		mNodes.setParent(this);
		if (graph.getBlockedEvents() != null)	{
			mBlockedEvents = graph.getBlockedEvents().clone();
			mFakeMap.put(mBlockedEvents, graph.getBlockedEvents());
			mOriginalMap.put(graph.getBlockedEvents(), mBlockedEvents);
      mBlockedEvents.setParent(this);
		}	else {
			mBlockedEvents = null;
		}

		final Collection<NodeSubject> nodes = graph.getNodesModifiable();
		final Iterator<NodeSubject> iter = nodes.iterator();

		while (iter.hasNext())
		{
			NodeSubject temp = (NodeSubject) iter.next();

			if (temp instanceof SimpleNodeSubject)
			{
				SimpleNodeSubject np = (SimpleNodeSubject) temp;
				addNode(np);
			}
			else if (temp instanceof GroupNodeSubject)
			{
				GroupNodeSubject gn = (GroupNodeSubject) temp;
				addGroupNode(gn);
			}
		}

		for (EdgeSubject e : graph.getEdgesModifiable())
		{
			if ((e.getSource() != null) && (e.getTarget() != null))
			{
				addEdge(e);
			}
			else
			{
				//TODO: Do something here!
				System.err.println("SOURCE OR TARGET IS NULL!");
			}
		}

    final OriginalModelListener listener1 = new OriginalModelListener();
    graph.addModelObserver(listener1);
    final CopiedModelListener listener2 = new CopiedModelListener();
    addModelObserver(listener2);
  }

	public Object acceptVisitor(ProxyVisitor p)
    throws VisitorException
	{
		return ((ModuleProxyVisitor)p).visitGraphProxy(this);
	}

	public Subject getCopy(Subject o)
	{
		Subject s = mOriginalMap.get(o);
		if (s == null)
		{
			if (o instanceof LabelBlockSubject)
			{
				s = getCopy((LabelBlockSubject)o);
			}
			if (o instanceof GuardActionBlockSubject)
			{
				s = getCopy((GuardActionBlockSubject)o);
			}
			if (o instanceof LabelGeometrySubject)
			{
				s = getCopy((LabelGeometrySubject)o);
			}
		}
		return s;
	}

	public Subject getCopy(LabelBlockSubject o)
	{
    if (o.getParent() instanceof EdgeSubject) {
      EdgeSubject s = (EdgeSubject)mOriginalMap.get(o.getParent());
      return s.getLabelBlock();
    } else {
      return mOriginalMap.get(o);
    }
	}

	public Subject getCopy(GuardActionBlockSubject o)
	{
		EdgeSubject s = (EdgeSubject)mOriginalMap.get(o.getParent());
		return s.getGuardActionBlock();
	}

	public Subject getCopy(LabelGeometrySubject o)
	{
		SimpleNodeSubject s = (SimpleNodeSubject)mOriginalMap.get(o.getParent());
		return s.getLabelGeometry();
	}
  
  public Subject getOriginal(Subject o)
	{
		Subject s = mFakeMap.get(o);
		if (s == null)
		{
			if (o instanceof LabelBlockSubject)
			{
				s = getOriginal((LabelBlockSubject)o);
			}
			if (o instanceof GuardActionBlockSubject)
			{
				s = getOriginal((GuardActionBlockSubject)o);
			}
			if (o instanceof LabelGeometrySubject)
			{
				s = getOriginal((LabelGeometrySubject)o);
			}
		}
		return s;
	}

	public Subject getOriginal(LabelBlockSubject o)
	{
    if (o.getParent() instanceof EdgeSubject) {
      EdgeSubject s = (EdgeSubject)mFakeMap.get(o.getParent());
      return s.getLabelBlock();
    } else {
      return mFakeMap.get(o);
    }
	}

	public Subject getOriginal(GuardActionBlockSubject o)
	{
		EdgeSubject s = (EdgeSubject)mFakeMap.get(o.getParent());
		return s.getGuardActionBlock();
	}

	public Subject getOriginal(LabelGeometrySubject o)
	{
		SimpleNodeSubject s = (SimpleNodeSubject)mFakeMap.get(o.getParent());
		return s.getLabelGeometry();
	}

	public Collection<EdgeSubject> getNodeEdges(NodeSubject n)
	{
		return Collections.unmodifiableCollection(mObserverMap.get(n).getEdges());
	}

	public LabelBlockProxy getBlockedEvents()
	{
		return mBlockedEvents;
	}

	public Set<NodeProxy> getNodes()
	{
		final Set<NodeProxy> downcast = Casting.toSet(mNodes);
		return Collections.unmodifiableSet(downcast);
	}

	public List<EdgeProxy> getEdges()
	{
		final List<EdgeProxy> downcast = Casting.toList(mEdges);
		return Collections.unmodifiableList(downcast);
	}

	public Set<NodeSubject> getNodeSubjects()
	{
		return Collections.unmodifiableSet(mNodes);
	}

	public List<EdgeSubject> getEdgeSubjects()
	{
		return Collections.unmodifiableList(mEdges);
	}

	public boolean isDeterministic()
	{
		return mGraph.isDeterministic();
	}

	private Point2D defaultPosition(NodeSubject node, Point2D turningpoint)
	{
		if (node instanceof SimpleNodeSubject)
		{
			return defaultPosition((SimpleNodeSubject)node);
		}
		return defaultPosition((GroupNodeSubject)node, turningpoint);
	}

	private Point2D defaultPosition(SimpleNodeSubject node)
	{
		return node.getPointGeometry().getPoint();
	}

	private Point2D defaultPosition(GroupNodeSubject node, Point2D point)
	{
		Rectangle2D r = node.getGeometry().getRectangle();
		return GeometryTools.findIntersection(r, point);
	}

	private Point2D getPosition(NodeSubject node)
	{
		if (node instanceof SimpleNodeSubject)
		{  
			return getPosition((SimpleNodeSubject)node);
		}
		return getPosition((GroupNodeSubject)node);
	}

	private Point2D getPosition(SimpleNodeSubject node)
	{
		return node.getPointGeometry().getPoint();
	}

	private Point2D getPosition(GroupNodeSubject node)
	{
		Rectangle2D r = node.getGeometry().getRectangle();
		return new Point2D.Double(r.getCenterX(), r.getCenterY());
	}

  private void addEdge(EdgeSubject e)
  {
    final EdgeSubject edge = e.clone();
    edge.setSource((NodeSubject) mOriginalMap.get(e.getSource()));
    edge.setTarget((NodeSubject) mOriginalMap.get(e.getTarget()));
    mEdges.add(edge);
    mObserverMap.get(edge.getSource()).addEdge(edge);
    mObserverMap.get(edge.getTarget()).addEdge(edge);
    mFakeMap.put(edge, e);
    mOriginalMap.put(e, edge);
    if (edge.getGeometry() == null) {
      final Collection<Point2D> points =
	Collections.singleton
	(GeometryTools.getMidPoint(GeometryTools.getPosition(edge.getSource()),
				   GeometryTools.getPosition(edge.getTarget())
				   ));
      edge.setGeometry(new SplineGeometrySubject(points,
						 SplineKind.INTERPOLATING));
    }
  }
  
  private void removeEdge(EdgeSubject e)
  {
    EdgeSubject remove = (EdgeSubject)mOriginalMap.get(e);
    mEdges.remove(remove);
    mFakeMap.remove(remove);
    mOriginalMap.remove(e);
  }
  
  private void removeNode(NodeSubject n)
  {
    NodeSubject remove = (NodeSubject)mOriginalMap.get(n);
    mNodes.remove(remove);
    mFakeMap.remove(remove);
    mOriginalMap.remove(n);
  }

	private void addGroupNode(GroupNodeSubject gn)
	{
		if (gn.getGeometry() == null)
		{
			throw new IllegalArgumentException("GroupNode " + gn + 
										   " has no Geometry Data");
		}
		else
		{
			GroupNodeSubject group = gn.clone();
			mNodes.add(group);
			mObserverMap.put(group, new EditorGroupNode(group));
			mFakeMap.put(group, gn);
			mOriginalMap.put(gn, group);
		}
	}

  private void addNode(SimpleNodeSubject n)
  {
    final SimpleNodeSubject node = n.clone();
    mNodes.add(node);
    mObserverMap.put(node, new EditorSimpleNode(node));
    mFakeMap.put(node, n);
    mOriginalMap.put(n, node);
  }
  
  public Map<ProxySubject, ProxySubject> getChanged()
  {
    IdentityHashMap<ProxySubject, ProxySubject> changed = 
      new IdentityHashMap<ProxySubject, ProxySubject>(mChanged.size());
    for (Subject s : mChanged) {
      ProxySubject orig = (ProxySubject)getOriginal(s);
      if (orig != null) {
        changed.put(orig, (ProxySubject)s);
      }
    }
    return changed;
  }

	public static LabelGeometrySubject defaultLabelBlockOffset()
	{
		Point2D p = new Point2D.Double(10, 10);
		return new LabelGeometrySubject(p);
	}

	public static LabelGeometrySubject defaultGuardActionBlockOffset()
	{
		Point2D p = new Point2D.Double(10, 10);
		return new LabelGeometrySubject(p);
	}

	public static LabelGeometrySubject defaultLabelOffset()
	{
		Point2D p = new Point2D.Double(-10, 10);
		return new LabelGeometrySubject(p);
	}

	public static PointGeometrySubject defaultNodePosition()
	{
		Random rand = new Random();
		Point2D p = new Point2D.Double(rand.nextInt(1000), rand.nextInt(1000));
		return new PointGeometrySubject(p);
	}

	public static PointGeometrySubject defaultInitialStateArrow()
	{
		Point2D p = new Point2D.Double(0, -10);
		return new PointGeometrySubject(p);
	}
  
  public static void updateChildNodes(GraphSubject graph) {
    List<GroupNodeSubject> groups = new ArrayList<GroupNodeSubject>();
    for (NodeSubject n : graph.getNodesModifiable()) {
      if (n instanceof GroupNodeSubject) {
        ((GroupNodeSubject) n).getImmediateChildNodesModifiable().clear();
        groups.add((GroupNodeSubject) n);
      }
    }
    // sort all the node groups from smallest to largest
    Collections.sort(groups, new Comparator<GroupNodeSubject>() {
      public int compare(GroupNodeSubject g1, GroupNodeSubject g2)
      {
        Rectangle2D r1 = g1.getGeometry().getRectangle();
        Rectangle2D r2 = g2.getGeometry().getRectangle();
        return (int) ((r1.getHeight() * r1.getWidth())
                      - (r2.getHeight() * r2.getWidth()));
      }
    });
    // go through all the nodes
    for (NodeSubject n : graph.getNodesModifiable()) {
      Rectangle2D r1;
      if (n instanceof GroupNodeSubject) {
        r1 = ((GroupNodeSubject) n).getGeometry().getRectangle();
      } else {
        Point2D p = ((SimpleNodeSubject) n).getPointGeometry().getPoint();
        r1 = new Rectangle((int) p.getX(), (int) p.getY(), 1, 1);
      }
      Collection<GroupNodeSubject> parents = new ArrayList<GroupNodeSubject>();
      mainloop:
      for (GroupNodeSubject group : groups) {
        if (n != group) {
          if (group.getGeometry().getRectangle().contains(r1)) {
            for (GroupNodeSubject parent : parents) {
              if (group.getGeometry().getRectangle().contains(parent.getGeometry().getRectangle())) {
                continue mainloop;
              }
            }
            group.getImmediateChildNodesModifiable().add(n);
            parents.add(group);
          }
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class OriginalModelListener
  /**
   * This listener receives all change event associated with the original
   * editor graph, of which the EditorGraph is a copy.
   */
  private class OriginalModelListener
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      final Subject esource = event.getSource();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
	if (!(esource.getParent() instanceof GroupNodeSubject)) {
	  final ProxySubject added = (ProxySubject) event.getValue();
	  if (added instanceof EdgeSubject) {
	    addEdge((EdgeSubject) added);
	  } else if (added instanceof SimpleNodeSubject) {
	    addNode((SimpleNodeSubject) added);
	  } else if (added instanceof GroupNodeSubject) {
	    addGroupNode((GroupNodeSubject) added);
	  } else if (esource.getParent() instanceof LabelBlockSubject) {
	    LabelBlockSubject block = (LabelBlockSubject) esource.getParent();
	    block = (LabelBlockSubject) getCopy(block);
	    block.getEventListModifiable().
	      add(((IdentifierSubject) added).clone());
	  }
	}
	break;
      case ModelChangeEvent.ITEM_REMOVED:
	if (!(esource.getParent() instanceof GroupNodeSubject)) {
	  final ProxySubject victim = (ProxySubject) event.getValue();
	  if (victim instanceof EdgeSubject) {
	    removeEdge((EdgeSubject) victim);
	  } else if (victim instanceof SimpleNodeSubject) {
	    removeNode((NodeSubject) victim);
	  } else if (victim instanceof GroupNodeSubject) {
	    removeNode((GroupNodeSubject) victim);
	  } else if (esource.getParent() instanceof LabelBlockSubject) {
	    LabelBlockSubject block = (LabelBlockSubject) esource.getParent();
	    block = (LabelBlockSubject) getCopy(block);
	    AbstractSubject remove = null;
	    for (AbstractSubject a : block.getEventListModifiable()) {
	      if (a.equalsByContents(victim)) {
		remove = a;
	      }
	    }
	    block.getEventListModifiable().remove((IdentifierSubject) remove);
	  }
	}
	break;
      case ModelChangeEvent.GEOMETRY_CHANGED:
	if (esource instanceof EdgeSubject &&
	    mOriginalMap.containsKey(esource)) {
	  final EdgeSubject orig = (EdgeSubject) esource;
	  final EdgeSubject copy = (EdgeSubject) getCopy(orig);
	  final PointGeometrySubject origStart = orig.getStartPoint();
	  final PointGeometrySubject copyStart = copy.getStartPoint();
	  if (origStart == null) {
	    copy.setStartPoint(null);
	  } else {//if (copyStart == null) {
	    final PointGeometrySubject cloned = origStart.clone();
	    copy.setStartPoint(cloned);
	  } /*else {
	    final Point2D point = origStart.getPoint();
	    copyStart.setPoint(point);
	  }*/
	  final PointGeometrySubject origEnd = orig.getEndPoint();
	  final PointGeometrySubject copyEnd = copy.getEndPoint();
	  if (origEnd == null) {
	    copy.setEndPoint(null);
	  } else { //if (copyEnd == null) {
	    final PointGeometrySubject cloned = origEnd.clone();
	    copy.setEndPoint(cloned);
	  }/* else {
	    final Point2D point = origEnd.getPoint();
	    copyEnd.setPoint(point);
	  }*/
	}
	break;
      }
    }

  }


  //#########################################################################
  //# Inner Class CopiedModelListener
  /**
   * This listener receives all change event associated with this
   * editor graph.
   */
  private class CopiedModelListener
    implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      final Subject esource = event.getSource();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
	{
	  final Subject parent = esource.getParent();
	  if (parent != null && parent instanceof GroupNodeSubject) {
	    mChanged.add(parent);
	  }
	}
	break;
      case ModelChangeEvent.STATE_CHANGED:
	if (esource instanceof EdgeSubject) {
	  final EdgeSubject edge = (EdgeSubject) esource;
	  final NodeSubject source = edge.getSource();
	  final NodeSubject target = edge.getTarget();
	  mObserverMap.get(source).addEdge(edge);
	  mObserverMap.get(target).addEdge(edge);
	  // TODO: Remove unlinked edges?
	}
	break;
      case ModelChangeEvent.GEOMETRY_CHANGED:
	if (esource instanceof NodeSubject) {
	  final NodeSubject node = (NodeSubject) esource;
	  mObserverMap.get(node).update();
	  mNodeMoved = true;
	}
	mChanged.add(esource);
	break;
      }
    }

  }
  

  //#########################################################################
  //# Inner Class EditorNode
  private abstract class EditorNode
    implements ModelObserver
  {
		protected EditorNode(NodeSubject node)
		{
			mNode = node;
			mEdges = new IdentityHashMap<EdgeSubject, Boolean>();
		}

		public void removeEdge(EdgeSubject edge)
		{
			if (mEdges.remove(edge) != null)
			{
				edge.removeModelObserver(this);
			}
		}

		public void addEdge(EdgeSubject edge)
		{
			if (mEdges.put(edge, new Boolean(true)) != null)
			{
				edge.addModelObserver(this);
			}
		}

		public Set<EdgeSubject> getEdges()
		{
			return Collections.unmodifiableSet(mEdges.keySet());
		}

		public void modelChanged(ModelChangeEvent event)
		{
			if (event.getSource() instanceof EdgeSubject)
			{
				EdgeSubject e = (EdgeSubject) event.getSource();
				if (e.getSource() != getNodeSubject() && e.getTarget() != getNodeSubject())
				{
					removeEdge(e);
				}
			}
		}

		public abstract void update();

    /**
     * Updates edge geometry when dragging node.
     * @param  edge   The edge to be modified.
     * @param  old    The old position of the start or end point.
     * @param  neo    The new position of the start or end point.
     */ 
    protected final void transformEdge(final EdgeSubject edge,
				       final Point2D old,
				       final Point2D neo)
    {
      final double ox = old.getX();
      final double oy = old.getY();
      final double nx = neo.getX();
      final double ny = neo.getY();
      final Point2D c = edge.getGeometry().getPointsModifiable().get(0);
      final double cx = c.getX();
      final double cy = c.getY();
      final Point2D newCenter;
      if (edge.getSource() == edge.getTarget())	{
	newCenter = new Point2D.Double(cx + nx - ox, cy + ny - oy);
      }	else {
	final Point2D e;
	if (edge.getSource() == getNodeSubject()) {
	  e = GeometryTools.getEndPoint(edge);
	} else {
	  e = GeometryTools.getStartPoint(edge);
	}
	final double ex = e.getX();
	final double ey = e.getY();
	final double cx1 = cx - ex;
	final double cy1 = cy - ey;
	final double ox1 = ox - ex;
	final double oy1 = oy - ey;
	final double nx1 = nx - ex;
	final double ny1 = ny - ey;
	final double divide = ox1 * ox1 + oy1 * oy1;
	// Correction needed for very short edge, when ox1 and oy1 are 0 ...
        final double factor;
	if (Math.abs(divide) > GeometryTools.EPSILON) {
          factor = 1.0 / divide;
        } else if (divide >= 0.0) {
          factor = 1.0 / GeometryTools.EPSILON;
        } else {
          factor = -1.0 / GeometryTools.EPSILON;
        }
	final double a1 = factor * (nx1 * ox1 + ny1 * oy1);
	final double a2 = factor * (nx1 * oy1 - ny1 * ox1);
	final double a3 = -a2;
	final double a4 = a1;
	newCenter = new Point2D.Double(a1 * cx1 + a2 * cy1 + ex,
				       a3 * cx1 + a4 * cy1 + ey);
      }
      edge.getGeometry().getPointsModifiable().set(0, newCenter);
      if (edge.getSource() == getNodeSubject() &&
	  edge.getStartPoint() != null) {
	edge.getStartPoint().setPoint(neo);
      }
      if (edge.getTarget() == getNodeSubject() &&
	  edge.getEndPoint() != null) {
	edge.getEndPoint().setPoint(neo);
      }
    }

		public NodeSubject getNodeSubject()
		{
			return mNode;
		}

		private final NodeSubject mNode;
		private final IdentityHashMap<EdgeSubject, Boolean> mEdges;
	}


  //#########################################################################
  //# Inner Class EditorSimpleNode
  private class EditorSimpleNode
    extends EditorNode
  {

    //#######################################################################
    //# Constructors
    public EditorSimpleNode(final SimpleNodeSubject node)
    {
      super(node);
      mPoint = node.getPointGeometry().getPoint();
    }

    //#######################################################################
    //# Simple Access
    public SimpleNodeSubject getNodeSubject()
    {
      return (SimpleNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
    public void update()
    {
      mUpdate = true;
      final Point2D newpos = getNodeSubject().getPointGeometry().getPoint();
      if (!mPoint.equals(newpos)) {
	for (final EdgeSubject edge : getEdges()) {
	  transformEdge(edge, mPoint, newpos);
	}
	mPoint = newpos;
      }
      mUpdate = false;
    }		
    
    //#######################################################################
    //# Data Members
    private Point2D mPoint;
  }


  //#########################################################################
  //# Inner Class EditorGroupNode
  private class EditorGroupNode
    extends EditorNode
  {
    //#######################################################################
    //# Constructors
    public EditorGroupNode(GroupNodeSubject node)
    {
      super(node);
      mRect = node.getGeometry().getRectangle();
    }

    //#######################################################################
    //# Simple Access
    public GroupNodeSubject getNodeSubject()
    {
      return (GroupNodeSubject) super.getNodeSubject();
    }

    //#######################################################################
    //# Editing
    public void update()
    {
      final GroupNodeSubject node = getNodeSubject();
      final Rectangle2D newR = node.getGeometry().getRectangle();
      if (!mRect.equals(newR)) {
	for (final EdgeSubject edge : getEdges()) {
	  final Point2D old;
	  if (edge.getSource() == node) {
	    old = edge.getStartPoint().getPoint();
	  } else if (edge.getTarget() == node)	{
	    old = edge.getEndPoint().getPoint();
	  } else {
	    continue;
	  }
	  final Point2D neo = new Point2D.Double
	    (((old.getX() - mRect.getMinX()) / mRect.getWidth()) *
	     newR.getWidth() + newR.getMinX(),
	     ((old.getY() - mRect.getMinY()) / mRect.getHeight()) *
	     newR.getHeight() + newR.getMinY());
	  transformEdge(edge, old, neo);
	}
	mRect = newR;
      }
    }

    //#######################################################################
    //# Data Members
    private Rectangle2D mRect;
  }


  //#########################################################################
  //# Data Members
  private boolean mUpdate = false;
	private final GraphSubject mGraph;
	private final ListSubject<EdgeSubject> mEdges;
	private final IndexedSetSubject<NodeSubject> mNodes;
	private final IdentityHashMap<NodeSubject, EditorNode> mObserverMap;
	private final IdentityHashMap<Subject, Subject> mFakeMap;
	private final IdentityHashMap<Subject, Subject> mOriginalMap;
	private final LabelBlockSubject mBlockedEvents;
  private final Set<Subject> mChanged;
  private boolean mNodeMoved = false;
}
