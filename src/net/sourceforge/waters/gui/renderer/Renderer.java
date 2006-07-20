package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import java.util.List;
import net.sourceforge.waters.model.base.VisitorException;
import java.util.PriorityQueue;

public class Renderer
{
	public void renderGraph(GraphProxy graph, List<MiscShape> shapes,
                          Renderable render, ProxyShapeProducer producer,
                          Graphics2D graphics)
	{
    PriorityQueue<ShapeToRender> queue = new PriorityQueue<ShapeToRender>();
    try {
      for (NodeProxy proxy : graph.getNodes())
      {
        queue.offer(new ShapeToRender(producer.getShape(proxy),
                                      render.getRenderingInformation(proxy)));
        if (proxy instanceof SimpleNodeProxy)
        {
          queue.offer(new ShapeToRender(producer.getShape(proxy.getName()),
                      render.getRenderingInformation(((SimpleNodeProxy) proxy)
                                               .getLabelGeometry())));
        }
      }
      
      for (EdgeProxy edge : graph.getEdges())
      {
        queue.offer(new ShapeToRender(producer.getShape(edge),
                                      render.getRenderingInformation(edge)));
        queue.offer(new ShapeToRender(producer.getShape(edge.getLabelBlock()),
                        render.getRenderingInformation(edge.getLabelBlock())));
        for (Proxy p : edge.getLabelBlock().getEventList())
        {
          queue.offer(new ShapeToRender(producer.getShape(p),
                                        render.getRenderingInformation(p)));
        }
      }
    } catch (VisitorException vis) {
        vis.printStackTrace();
    }
    while (!queue.isEmpty()) {
      queue.poll().draw(graphics);
    }
    for (MiscShape shape : shapes) {
      shape.draw(graphics);
    }
	}
	
	protected void drawShape(ProxyShape p, RenderingInformation status,
							 Graphics2D graphics)
	{
		p.draw(graphics, status);
	}
  
  /**
   * just a wrapper class to aid with setting up priority Queue
   */
  private class ShapeToRender
    implements Comparable<ShapeToRender>
  {
    private ProxyShape mShape;
    private RenderingInformation mStatus;
    
    public ShapeToRender(ProxyShape shape, RenderingInformation status)
    {
      mShape = shape;
      mStatus = status;
    }
    
    public void draw(Graphics2D g)
    {
      if (mStatus.getPriority() >= 0) {
        drawShape(mShape, mStatus, g);
      }
    }
    
    public int compareTo(ShapeToRender o) 
    {
      return mStatus.getPriority() - o.mStatus.getPriority();
    }
  }
}
