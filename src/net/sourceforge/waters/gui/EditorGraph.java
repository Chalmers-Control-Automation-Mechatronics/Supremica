package net.sourceforge.waters.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.sourceforge.waters.model.base.VisitorException;

import net.sourceforge.waters.gui.renderer.GeometryTools;

import net.sourceforge.waters.model.base.ProxyVisitor;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.SetSubject;
import net.sourceforge.waters.subject.base.Subject;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.module.SplineKind;

import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import java.util.Map;
import java.util.HashSet;
import java.util.Comparator;
import java.awt.Rectangle;
import net.sourceforge.waters.subject.base.IndexedSetSubject;

public class EditorGraph
	extends AbstractSubject
	implements GraphProxy,
			   ModelObserver
{
	public EditorGraph(GraphSubject graph)
	{
		mGraph = graph;
    mChanged = new HashSet<Subject>();
		mObservers = Collections.synchronizedCollection(new ArrayList());
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

		this.addModelObserver(this);
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
		EdgeSubject s = (EdgeSubject)mOriginalMap.get(o.getParent());
		return s.getLabelBlock();
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
		EdgeSubject s = (EdgeSubject)mFakeMap.get(o.getParent());
		return s.getLabelBlock();
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
		EdgeSubject edge = e.clone();
		try
		{
			edge.setSource((NodeSubject)mOriginalMap.get(e.getSource()));
			edge.setTarget((NodeSubject)mOriginalMap.get(e.getTarget()));
			mEdges.add(edge);
      mObserverMap.get(edge.getSource()).addEdge(edge);
			mObserverMap.get(edge.getTarget()).addEdge(edge);
			mFakeMap.put(edge, e);
			mOriginalMap.put(e, edge);
      if (edge.getGeometry() == null) {
        final Collection<Point2D> points = Collections.singleton(
					GeometryTools.getMidPoint(GeometryTools.getPosition(edge.getSource()),
                                    GeometryTools.getPosition(edge.getTarget())
                                    ));
        edge.setGeometry(new SplineGeometrySubject(points,
                                                   SplineKind.INTERPOLATING));
      }
      if (edge.getStartPoint() == null) {
        PointGeometrySubject p = new PointGeometrySubject(
                              GeometryTools.defaultPosition(edge.getSource(),
                             edge.getGeometry().getPoints().get(0)));
        edge.setStartPoint(p);
      }
      if (edge.getEndPoint() == null) {
        PointGeometrySubject p = new PointGeometrySubject(
                              GeometryTools.defaultPosition(edge.getTarget(),
                                      edge.getGeometry().getPoints().get(0)));
        edge.setEndPoint(p);
      }
		}
		catch (Throwable t)
		{
      System.err.println("either source or target node not in graph");
      assert(false);
			t.printStackTrace();
			mEdges.remove(edge);
			mFakeMap.remove(edge);
			mOriginalMap.remove(e);
		}
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
		SimpleNodeSubject node = n.clone();
		mNodes.add(node);
		mObserverMap.put(node, new EditorSimpleNode(node));
		mFakeMap.put(node, n);
		mOriginalMap.put(n, node);
	}

	public void modelChanged(ModelChangeEvent event)
	{
		if (event.getSource() instanceof EdgeSubject)
		{
			EdgeSubject e = (EdgeSubject)event.getSource();
			if (event.getKind() == ModelChangeEvent.STATE_CHANGED)
			{
				mObserverMap.get(e.getSource()).addEdge(e);
				mObserverMap.get(e.getTarget()).addEdge(e);
			}
		}
		if (event.getSource() instanceof NodeSubject)
		{
			NodeSubject n = (NodeSubject)event.getSource();
			if (event.getKind() == ModelChangeEvent.GEOMETRY_CHANGED)
			{
				mObserverMap.get(n).update();
        mNodeMoved = true;
			}
		}
    if (event.getKind() == ModelChangeEvent.GEOMETRY_CHANGED) {
      mChanged.add(event.getSource());
    }
    if (event.getKind() == ModelChangeEvent.ITEM_ADDED
        || event.getKind() == ModelChangeEvent.ITEM_REMOVED) {
      if (event.getSource().getParent() != null
          && event.getSource().getParent() instanceof GroupNodeSubject) {
        mChanged.add(event.getSource().getParent());
      }
    }
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

	public void addModelObserver(ModelObserver o)
	{
		mObservers.add(o);
	}

	public void removeModelObserver(ModelObserver o)
	{
		mObservers.remove(o);
	}

	public void fireModelChanged(ModelChangeEvent event)
	{
    for (ModelObserver o : mObservers) {
      o.modelChanged(event);
    }
		super.fireModelChanged(event);
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
      for (GroupNodeSubject group : groups) {
        if (n != group) {
          if (group.getGeometry().getRectangle().contains(r1)) {
            group.getImmediateChildNodesModifiable().add(n);
            break;
          }
        }
      }
    }
  }

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

		protected final void transformEdge(final EdgeSubject edge, final Point2D neo)
		{
			if (edge.getSource() == edge.getTarget())
			{
				Point2D p = edge.getGeometry().getPointsModifiable().get(0);
				Point2D s = edge.getStartPoint().getPoint();
				edge.getGeometry().getPointsModifiable().set(0 ,
						new Point2D.Double(p.getX() + (neo.getX() - (int) s.getX()),
                               p.getY() + (neo.getY() - s.getY())));
			}
			else
			{
				final Point2D c = edge.getGeometry().getPointsModifiable().get(0);
				final Point2D n = new Point2D.Double(neo.getX(), neo.getY());
				final Point2D o;
				final Point2D e;

				if (edge.getSource() == getNodeSubject())
				{
					o = edge.getStartPoint().getPoint();
					e = edge.getEndPoint().getPoint();
					//  Cx = (.25 * (double)(ox + 2*getCPointX() + (double)endNode.getX())) - (double)endNode.getX();
					//  Cy = (.25 * (double)(oy + 2*getCPointY() + (double)endNode.getY())) - (double)endNode.getY();
				}
				else
				{
					e = edge.getStartPoint().getPoint();
					o = edge.getEndPoint().getPoint();
				}
				c.setLocation(c.getX() - e.getX(), c.getY() - e.getY());
				o.setLocation(o.getX() - e.getX(), o.getY() - e.getY());
				n.setLocation(n.getX() - e.getX(), n.getY() - e.getY());

				// If ox and oy are 0, this becomes 0...
				double divide = Math.pow(o.getX(), 2) + Math.pow(o.getY(), 2);
				if (Math.abs(divide) < Double.MIN_VALUE)
				{
					if (divide >= 0)
					{
						divide = 2*Double.MIN_VALUE;
					}
					else
					{
						divide = -2*Double.MIN_VALUE;
					}
				}

				// ... which is not good here!
				double a1 = (n.getX() * o.getX() + o.getY() * n.getY())
							/ divide;
				double a2 = ((o.getY() * n.getX()) - (o.getX() * n.getY()))
							/ divide;
				double a3 = -a2;
				double a4 = a1;				
				c.setLocation(a1 * c.getX() + a2 * c.getY() + e.getX(),
							  a3 * c.getX() + a4 * c.getY() + e.getY());
				// This is where it sometimes goes wrong...
				// Cx and Cy sometimes becomes NaN...
				edge.getGeometry().getPointsModifiable().set(0 , c);
			}
			if (edge.getSource() == getNodeSubject())
			{
				edge.getStartPoint().setPoint(neo);
			}
			if (edge.getTarget() == getNodeSubject())
			{
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

	private class EditorSimpleNode
		extends EditorNode
	{
		public EditorSimpleNode(SimpleNodeSubject node)
		{
			super(node);
			mPoint = node.getPointGeometry().getPoint();
		}

		public SimpleNodeSubject getNodeSubject()
		{
			return (SimpleNodeSubject)super.getNodeSubject();
		}

		public void update()
		{
      mUpdate = true;
			if (!mPoint.equals(getNodeSubject().getPointGeometry().getPoint()))
			{
				for (EdgeSubject e : getEdges())
				{
					transformEdge(e, getNodeSubject().getPointGeometry().getPoint());
				}
				mPoint.setLocation(getNodeSubject().getPointGeometry().getPoint());
			}
      mUpdate = false;
		}		

		private final Point2D mPoint;
	}

	private class EditorGroupNode
		extends EditorNode
	{
		public EditorGroupNode(GroupNodeSubject node)
		{
			super(node);
			mRect = node.getGeometry().getRectangle();
		}

		public GroupNodeSubject getNodeSubject()
		{
			return (GroupNodeSubject)super.getNodeSubject();
		}

		public void addEdge(EdgeSubject e)
		{
			super.addEdge(e);
			/*
			final Rectangle2D r = getSubject().getGeometry().getRectangle();
			final Point 2D p;
			if (e.getSource() == getSubject())
			{
				p = e.getStartPoint().getPoint();
			}
			else
			{
				p = e.getEndPoint().getPoint();
			}
			if (Maths.abs(p.getX() - r.getMinX()) <
				Maths.abs(p.getX() - r.getMaxX()))
			{
				p.setLocation(r.getMinX(), p.getY());
			}
			else
			{
				p.setLocation(r.getMaxX(), p.getY());
			}
			if (Maths.abs(p.getY() - r.getMinY()) <
				Maths.abs(p.getY() - r.getMaxY()))
			{
				p.setLocation(p.getX(), r.getMinY());
			}
			else
			{
				p.setLocation(p.getX(), r.getMaxY());
			}
			if (e.getSource() == getSubject())
			{
				e.getStartPoint().setPoint(p);
			}
			else
			{
				e.getEndPoint().setPoint(p);
			}
			*/
		}

		public void update()
		{
			if (!mRect.equals(getNodeSubject().getGeometry().getRectangle()))
			{
				for (EdgeSubject e : getEdges())
				{
					Rectangle2D newR = getNodeSubject().getGeometry().getRectangle();
					Point2D o;
					Point2D n = new Point2D.Double();
					if (e.getSource() == getNodeSubject()) {
						o = e.getStartPoint().getPoint();
					}	else if (e.getTarget() == getNodeSubject())	{
						o = e.getEndPoint().getPoint();
					} else {
            continue;
          }
					n.setLocation(((o.getX() - mRect.getMinX()) / mRect.getWidth())
                        * newR.getWidth() + newR.getMinX(),
                        ((o.getY() - mRect.getMinY()) / mRect.getHeight())
                        * newR.getHeight() + newR.getMinY());
					transformEdge(e, n);
				}
				mRect.setRect(getNodeSubject().getGeometry().getRectangle());
			}
		}

		private final Rectangle2D mRect;
	}

  private boolean mUpdate = false;
	private final GraphSubject mGraph;
	private final ListSubject<EdgeSubject> mEdges;
	private final IndexedSetSubject<NodeSubject> mNodes;
	private final Collection<ModelObserver> mObservers;
	private final IdentityHashMap<NodeSubject, EditorNode> mObserverMap;
	private final IdentityHashMap<Subject, Subject> mFakeMap;
	private final IdentityHashMap<Subject, Subject> mOriginalMap;
	private final LabelBlockSubject mBlockedEvents;
  private final Set<Subject> mChanged;
  private boolean mNodeMoved = false;
}
