package net.sourceforge.waters.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Random;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.EdgeProxy;

public class SpringEmbedder
{	
	static public void run(EditorSurface s, GraphProxy g, int iterations)
	{
		Map<Proxy, Object> movements = new IdentityHashMap<Proxy, Object>();
		Collection<Proxy> proxyies = new ArrayList<Proxy>();
		proxyies.addAll(g.getNodes());
		double max = 0;
		//proxyies.addAll(g.getEdges());
		try
		{
			DisplacementCalculator d = new DisplacementCalculator(g);
			while(true)
			{			
				for (Proxy p : proxyies)
				{
					if (p instanceof SimpleNodeProxy)
					{
						movements.put(p, p.acceptVisitor(d));
					}
				}
				max = d.updatePositions(movements);
				System.out.println(max);
				if (max < .01)
				{
					break;
				}
			}
			proxyies.clear();
			proxyies.addAll(g.getEdges());
			while(true)
			{
				for (Proxy p : proxyies)
				{
					movements.put(p, p.acceptVisitor(d));
				}
				max = d.updatePositions(movements);
				System.out.println(max);
				if (max < .01)
				{
					break;
				}
			}
			// most likely going to have to update this when also dealing with nodegroups
			// makes certain that every thing has a positive position
			double minx = java.lang.Double.MAX_VALUE;
			double miny = java.lang.Double.MAX_VALUE;
			for (Object o : movements.values())
			{
				if (o instanceof Point2D)
				{
					Point2D p = (Point2D)o;
					if (p.getX() < minx)
					{
						minx = p.getX();
					}
					if (p.getY() < miny)
					{
						miny = p.getY();
					}
				}
				if (o instanceof Rectangle2D)
				{
					Rectangle2D r = (Rectangle2D)o;
					if (r.getMinX() < minx)
					{
						minx = r.getMinX();
					}
					if (r.getMinY() < miny)
					{
						miny = r.getMinY();
					}
				}
			}
			double dx = 50 - minx;
			double dy = 50 - miny;
			for (Object o : movements.values())
			{
				if (o instanceof Point2D)
				{
					Point2D p = (Point2D)o;
					p.setLocation(p.getX() + dx, p.getY() + dy);
				}
				if (o instanceof Rectangle2D)
				{
					Rectangle2D r = (Rectangle2D)o;
					r.setFrame(r.getMinX() + dx, r.getMinY() + dy,
							   r.getHeight(), r.getWidth());
				}
			}
			for (Object o : s.getNodes())
			{
				EditorNode n = (EditorNode)o;
				Point2D p = (Point2D)movements.get(n.getSubject());
				n.setPosition(p.getX(), p.getY());
			}
			for (Object o : s.getEdges())
			{
				EditorEdge e = (EditorEdge)o;
				Point2D p = (Point2D)movements.get(e.getSubject());
				e.setPosition(p.getX(), p.getY());
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	static private class DisplacementCalculator
			extends AbstractModuleProxyVisitor
	{
		private final GraphProxy mGraph;
		private final Map<Proxy, Point2D.Double> mPositions;
		static public double EDGEATTRACTION = .005;
	    static public double SPRINGCONSTANT = .005;
		static public double REPULSIONCONST = 40;
		static public double REPULSIONCONST2 = 10;
		static public double EDGEREPULSE = 1;
		
		private int nodes = 0;
		private int edges = 0;
		
		public DisplacementCalculator(final GraphProxy graph)
		{			
			mGraph = graph;
			mPositions = new IdentityHashMap<Proxy, Point2D.Double>();
			for (NodeProxy n : mGraph.getNodes())
			{
				if (n instanceof SimpleNodeProxy)
				{
					SimpleNodeProxy node = (SimpleNodeProxy)n;
					Point2D.Double p = new Point2D.Double();
					p.setLocation(node.getPointGeometry().getPoint());
					mPositions.put(node, p);
				}
			}
			for (EdgeProxy e : mGraph.getEdges())
			{
				Point2D.Double p = new Point2D.Double();
				p.setLocation(e.getGeometry().getPoints().get(0));
				mPositions.put(e, p);
			}
		}
		
		private Point2D repulsion(Point2D p1, Point2D p2, double constant)
		{
			double dx = p1.getX() - p2.getX();
			double dy = p1.getY() - p2.getY();
			double len = dx * dx + dy * dy;	
			if (len != 0)
			{
				return new Point2D.Double((dx / len) * constant,
										  (dy / len) * constant);
			}
			else
			{			
				Random rand = new Random();				
				return new Point2D.Double(rand.nextDouble(), rand.nextDouble());
			}
		}		
		
		private Point2D attraction(Point2D p1, Point2D p2, double constant)
		{
			double dx = p1.getX() - p2.getX();
			double dy = p1.getY() - p2.getY();
			return new Point2D.Double(-dx*constant, -dy*constant);
		}
		
		public double updatePositions(Map<? extends Proxy, ? extends Object> m )
		{
			double max = 0;
			for (Proxy p : mPositions.keySet())
			{
				if (m.get(p) instanceof Point2D)
				{					
					Point2D point = (Point2D)m.get(p);
					if (max < point.distance(mPositions.get(p)))
					{
						max = point.distance(mPositions.get(p));
					}
					mPositions.get(p).setLocation(point);
				}
			}
			return max;
		}
		
		public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
		{
			nodes++;
			double dx = 0;
			double dy = 0;
			for (NodeProxy n : mGraph.getNodes())
			{
				if (n instanceof SimpleNodeProxy && n != node)
				{
					SimpleNodeProxy node2 = (SimpleNodeProxy)n;
					Point2D p = repulsion(mPositions.get(node),
										  mPositions.get(node2),
										  REPULSIONCONST);
					dx += p.getX();
					dy += p.getY();
				}
			}
			for (EdgeProxy e : mGraph.getEdges())
			{
				NodeProxy n = null;
				if (node == e.getSource())
				{
					n = e.getTarget();
				}
				else if(node == e.getTarget())
				{
					n = e.getSource();
				}
				if (n instanceof SimpleNodeProxy)
				{
					SimpleNodeProxy node2 = (SimpleNodeProxy)n;
					Point2D p = attraction(mPositions.get(node),
										   mPositions.get(node2),
										   SPRINGCONSTANT);
					dx += p.getX();
					dy += p.getY();
				}
			}
			//System.out.println("Node" + nodes + ": " + mPositions.get(node));
			return new Point2D.Double(mPositions.get(node).getX()+dx,
									  mPositions.get(node).getY()+dy);
		}
		
		public Object visitGroupNodeProxy(GroupNodeProxy node)
		{
			// maybe do something here later
			return node.getGeometry().getRectangle();
		}
		
		public Object visitEdgeProxy(EdgeProxy edge)
		{
			edges++;
			double dx = 0;
			double dy = 0;
			Point2D p;
			for (EdgeProxy e : mGraph.getEdges())
			{
				if (edge != e)
				{
					p = repulsion(mPositions.get(edge),
								  mPositions.get(e),
								  EDGEREPULSE);
					dx += p.getX();
					dy += p.getY();
				}
			}			
			p = attraction(mPositions.get(edge),
						   mPositions.get(edge.getSource()),
						   EDGEATTRACTION);
			dx += p.getX();
			dy += p.getY();
			p = attraction(mPositions.get(edge),
						   mPositions.get(edge.getTarget()),
						   EDGEATTRACTION);
			dx += p.getX();
			dy += p.getY();
			if (edge.getSource() == edge.getTarget())
			{
				p = repulsion(mPositions.get(edge),
						   	  mPositions.get(edge.getSource()),
							  REPULSIONCONST2);
							  dx += p.getX();
							  dy += p.getY();
				p = repulsion(mPositions.get(edge),
						   	  mPositions.get(edge.getTarget()),
							  REPULSIONCONST2);
							  dx += p.getX();
							  dy += p.getY();
			}
			//System.out.println("Edge" + edges + ": " + mPositions.get(edge));
			p.setLocation(mPositions.get(edge).getX() + dx,
						  mPositions.get(edge).getY() + dy);
			return p;
		}
	}
	
	static private class DisplacementUpdater
			extends AbstractModuleProxyVisitor
	{
		final Map<Proxy, Object> mMovements;
		
		public DisplacementUpdater(final Map movements)
		{
			mMovements = movements;
		}
		
		public Object visitSimpleNodeProxy(SimpleNodeProxy node)
		{
			Point2D p = (Point2D)mMovements.get(node);
			node.getPointGeometry().getPoint().setLocation(p);
			return null;
		}
		
		public Object visitGroupNodeProxy(GroupNodeProxy node)
		{
			Rectangle2D r = (Rectangle2D)mMovements.get(node);
			node.getGeometry().getRectangle().setRect(r);
			return null;
		}
		
		public Object visitEdgeProxy(EdgeProxy edge)
		{
			Point2D p = (Point2D)mMovements.get(edge);
			edge.getGeometry().getPoints().get(0).setLocation(p);
			return null;
		}
	}
}
