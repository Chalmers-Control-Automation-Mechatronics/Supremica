package net.sourceforge.waters.gui.springembedder;

import java.util.TimerTask;
import javax.swing.WindowConstants;
import java.util.Timer;
import javax.swing.JLabel;
import javax.swing.JFrame;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import java.util.Random;
import net.sourceforge.waters.model.base.Proxy;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class SpringEmbedder
  implements Runnable
{
  private final GraphSubject mGraph;
  
  public SpringEmbedder(GraphSubject graph)
  {
    mGraph = graph;
  }
  
  public void run()
	{
    TimeFrame time = new TimeFrame(30000);
		Collection<NodeWrapper> nodes = new ArrayList<NodeWrapper>();
    Collection<EdgeWrapper> edges = new ArrayList<EdgeWrapper>();
    for (NodeProxy node : mGraph.getNodes()) {
      if (node instanceof SimpleNodeSubject) {
        SimpleNodeSubject n = (SimpleNodeSubject) node;
        nodes.add(new NodeWrapper(n));
      }
    }
    for (EdgeSubject edge : mGraph.getEdgesModifiable()) {
      edges.add(new EdgeWrapper(edge));
    }
		//proxyies.addAll(g.getEdges());
		try
		{
			DisplacementCalculator d = new DisplacementCalculator(mGraph);
			while(true) {
				for (NodeWrapper node : nodes) {
					node.setPoint((Point2D)node.getSubject().acceptVisitor(d));
				}
        for (EdgeWrapper edge : edges) {
					edge.setPoint((Point2D)edge.getSubject().acceptVisitor(d));
				}
        double max = Double.NEGATIVE_INFINITY;
        for (NodeWrapper node : nodes) {
					double tmp = node.update();
          if (tmp > max) {
            max = tmp;
          }
				}
        for (EdgeWrapper edge : edges) {
					double tmp = edge.update();
          if (tmp > max) {
            max = tmp;
          }
				}
        System.out.println(max);
				if (max < .01 || !time.isVisible())
				{
					break;
				}
        Thread.yield();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
    time.setVisible(false);
	}
	
	static private class DisplacementCalculator
			extends AbstractModuleProxyVisitor
	{
		private final GraphProxy mGraph;
		static public double EDGEATTRACTION = .005;
    static public double SPRINGCONSTANT = .005;
		static public double REPULSIONCONST = 400;
		static public double REPULSIONCONST2 = 100;
		static public double EDGEREPULSE = 10;
		
		private int nodes = 0;
		private int edges = 0;
		
		public DisplacementCalculator(final GraphProxy graph)
		{			
			mGraph = graph;
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
		
		public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
		{
			nodes++;
			double dx = 0;
			double dy = 0;
      Point2D pos = node.getPointGeometry().getPoint();
			for (NodeProxy n : mGraph.getNodes())
			{
				if (n instanceof SimpleNodeProxy && n != node)
				{
					SimpleNodeProxy node2 = (SimpleNodeProxy)n;
          Point2D pos2 = node2.getPointGeometry().getPoint();
					Point2D p = repulsion(pos,
                                pos2,
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
          Point2D pos2 = node2.getPointGeometry().getPoint();
					Point2D p = attraction(pos,
                                 pos2,
                                 SPRINGCONSTANT);
					dx += p.getX();
					dy += p.getY();
				}
			}
      System.out.println(dx + "," + dy);
			//System.out.println("Node" + nodes + ": " + mPositions.get(node));
			return new Point2D.Double(pos.getX() + dx,
                                pos.getY() + dy);
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
      Point2D pos = edge.getGeometry().getPoints().get(0);
			for (EdgeProxy e : mGraph.getEdges())
			{
        Point2D pos2 = e.getGeometry().getPoints().get(0);
				if (edge != e)
				{
					p = repulsion(pos,
                        pos2,
                        EDGEREPULSE);
					dx += p.getX();
					dy += p.getY();
				}
			}
      Point2D pos2 = ((SimpleNodeProxy)edge.getSource()).getPointGeometry().getPoint();
			p = attraction(pos,
                     pos2,
                     EDGEATTRACTION);
			dx += p.getX();
			dy += p.getY();
      pos2 = ((SimpleNodeProxy)edge.getTarget()).getPointGeometry().getPoint();
			p = attraction(pos,
                     pos2,
                     EDGEATTRACTION);
			dx += p.getX();
			dy += p.getY();
			if (edge.getSource() == edge.getTarget())
			{
        p = repulsion(pos,
                      pos2,
                      REPULSIONCONST2);
        dx += p.getX();
        dy += p.getY();
			}
			//System.out.println("Edge" + edges + ": " + mPositions.get(edge));
			p.setLocation(pos.getX() + dx,
                    pos.getY() + dy);
			return p;
		}
	}
  
  static private class NodeWrapper
  {
    private final SimpleNodeSubject mSubject;
    private Point2D mPoint;
    
    public NodeWrapper(SimpleNodeSubject subject)
    {
      mSubject = subject;
      mPoint = null;
    }
    
    public SimpleNodeSubject getSubject()
    {
      return mSubject;
    }
    
    public void setPoint(Point2D newPoint)
    {
      mPoint = newPoint;
    }
    
    public double update()
    {
      double dx = Math.abs(mSubject.getPointGeometry().getPoint().getX() 
                           - mPoint.getX());
      double dy = Math.abs(mSubject.getPointGeometry().getPoint().getY() 
                           - mPoint.getY());
      double dist = Math.pow(Math.pow(dx, 2) + Math.pow(dy, 2), .5);
      System.out.println("Dist: " + dist + " dx:" + dx + " dy:" + dy);
      mSubject.getPointGeometry().setPoint(mPoint);
      mPoint = null;
      return dist;
    }
  }

  static private class EdgeWrapper
  {
    private final EdgeSubject mSubject;
    private Point2D mPoint;
    
    public EdgeWrapper(EdgeSubject subject)
    {
      mSubject = subject;
      mPoint = null;
    }
    
    public EdgeSubject getSubject()
    {
      return mSubject;
    }
    
    public void setPoint(Point2D newPoint)
    {
      mPoint = newPoint;
    }
    
    public double update()
    {
      double dist = mSubject.getGeometry().getPoints().get(0).distance(mPoint);
      mSubject.getGeometry().getPointsModifiable().set(0, mPoint);
      mSubject.getStartPoint().setPoint(((SimpleNodeProxy)mSubject.getSource())
                                        .getPointGeometry().getPoint());
      mSubject.getEndPoint().setPoint(((SimpleNodeProxy)mSubject.getTarget())
                                      .getPointGeometry().getPoint());
      mPoint = null;
      return dist;
    }
  }
  
  static private class TimeFrame
    extends JFrame
  {
    private final JLabel mLabel;
    private final Timer mTimer;
    private final int mTime;
    private int cTime;
    
    public TimeFrame(int time)
    {
      mTime = time;
      mTimer = new Timer(true);
      mLabel = new JLabel();
      cTime = 0;
      mTimer.scheduleAtFixedRate(new TimerTask()
      {
        public void run()
        {
          cTime++;
          mLabel.setText(Integer.toString(cTime));
          if (cTime >= mTime) {
            setVisible(false);
          }
        }
      }, 1000, 1000);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      setSize(300, 300);
      add(mLabel);
      pack();
      setVisible(true);
    }
    
    public void setVisible(boolean vis) 
    {
      if (!vis) {
        mTimer.cancel();
      }
      super.setVisible(vis);
    }
  }
}
