//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   Renderer
//###########################################################################
//# $Id: Renderer.java,v 1.13 2007-11-07 06:16:04 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.PriorityQueue;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


public class Renderer
{
    public void renderGraph(GraphProxy graph, List<MiscShape> shapes,
        Renderable render, ProxyShapeProducer producer,
        Graphics2D graphics)
  {
    final PriorityQueue<ShapeToRender> queue =
      new PriorityQueue<ShapeToRender>();

    // Blocked events
    final LabelBlockProxy blocked = graph.getBlockedEvents();
    if (blocked != null) {
      queue.offer(new ShapeToRender(producer.getShape(blocked),
                                    render.getRenderingInformation(blocked)));
      for (final Proxy proxy : blocked.getEventList()) {
        queue.offer(new ShapeToRender(producer.getShape(proxy),
                                      render.getRenderingInformation(proxy)));
      }
    }

            // Nodes
            for (NodeProxy proxy : graph.getNodes())
            {
                queue.offer(new ShapeToRender(producer.getShape(proxy),
                    render.getRenderingInformation(proxy)));
                if (proxy instanceof SimpleNodeProxy)
                {
                    queue.offer(new ShapeToRender(producer.getShape(((SimpleNodeProxy)proxy).getLabelGeometry()),
                        render.getRenderingInformation(((SimpleNodeProxy) proxy)
                        .getLabelGeometry())));
                }
            }
            // Edges
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
                if (edge.getGuardActionBlock() != null)
                {
                    queue.offer(new ShapeToRender(producer.getShape(edge.getGuardActionBlock()),
                        render.getRenderingInformation(edge.getGuardActionBlock())));
                    for(BinaryExpressionProxy action : edge.getGuardActionBlock().getActions())
                    {
                        queue.offer(new ShapeToRender(producer.getShape(action),
                            new RenderingInformation(false, false, EditorColor.ACTIONCOLOR, EditorColor.ACTIONCOLOR, 0)));
                    }
                    List<SimpleExpressionProxy> guards = edge.getGuardActionBlock().getGuards();
                    if(!guards.isEmpty())
                    {
                        SimpleExpressionProxy guard = guards.get(0); //there should be only one guard.
                        queue.offer(new ShapeToRender(producer.getShape(guard),
                            new RenderingInformation(false, false, EditorColor.GUARDCOLOR, EditorColor.GUARDCOLOR, 0)));
                    }}
            }
        while (!queue.isEmpty()) {
          final ShapeToRender shape = queue.poll();
          shape.draw(graphics);
        }
        if (shapes != null) {
          for (final MiscShape shape : shapes) {
            shape.draw(graphics);
          }
        }
    }
    
    protected void drawShape(ProxyShape p, RenderingInformation status,
        Graphics2D graphics)
    {
        p.draw(graphics, status);
    }
    
    /**
     * Just a wrapper class to aid with setting up priority Queue
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
            if (mStatus.getPriority() >= 0)
            {
                drawShape(mShape, mStatus, g);
            }
        }
        
        public int compareTo(ShapeToRender o)
        {
            return mStatus.getPriority() - o.mStatus.getPriority();
        }

    }
}
