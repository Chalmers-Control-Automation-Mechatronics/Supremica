//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   Renderer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
  public void renderGraph(final GraphProxy graph,
                          final List<MiscShape> shapes,
                          final ProxyShapeProducer producer,
                          final Graphics2D graphics)
  {
    final RenderingContext context = producer.getRenderingContext();
    final PriorityQueue<ShapeToRender> queue =
      new PriorityQueue<ShapeToRender>();

    // Blocked events
    final LabelBlockProxy blocked = graph.getBlockedEvents();
    if (blocked != null) {
      final ProxyShape shape = producer.getShape(blocked);
      final RenderingInformation info =
        context.getRenderingInformation(blocked);
      queue.offer(new ShapeToRender(shape, info));
      for (final Proxy proxy : blocked.getEventList()) {
        queue.offer(new ShapeToRender(producer.getShape(proxy),
                                      context.getRenderingInformation(proxy)));
      }
    }

    // Nodes
    for (final NodeProxy proxy : graph.getNodes()) {
      queue.offer(new ShapeToRender(producer.getShape(proxy), context
          .getRenderingInformation(proxy)));
      if (proxy instanceof SimpleNodeProxy) {
        queue.offer(new ShapeToRender(producer
            .getShape(((SimpleNodeProxy) proxy).getLabelGeometry()), context
            .getRenderingInformation(((SimpleNodeProxy) proxy)
                .getLabelGeometry())));
      }
    }

    // Edges
    for (final EdgeProxy edge : graph.getEdges()) {
      queue.offer(new ShapeToRender(producer.getShape(edge), context
          .getRenderingInformation(edge)));
      queue.offer(new ShapeToRender(producer.getShape(edge.getLabelBlock()),
          context.getRenderingInformation(edge.getLabelBlock())));
      for (final Proxy p : edge.getLabelBlock().getEventList()) {
        queue.offer(new ShapeToRender(producer.getShape(p), context
            .getRenderingInformation(p)));
      }
      if (edge.getGuardActionBlock() != null) {
        queue.offer(new ShapeToRender(producer.getShape(edge
            .getGuardActionBlock()), context.getRenderingInformation(edge
            .getGuardActionBlock())));
        for (final BinaryExpressionProxy action : edge.getGuardActionBlock()
            .getActions()) {
          queue.offer(new ShapeToRender(producer.getShape(action),
              new RenderingInformation(false, false, EditorColor.ACTIONCOLOR,
                  EditorColor.ACTIONCOLOR, 0)));
        }
        final List<SimpleExpressionProxy> guards =
            edge.getGuardActionBlock().getGuards();
        if (!guards.isEmpty()) {
          final SimpleExpressionProxy guard = guards.get(0); // there should be
                                                             // only one guard.
          queue.offer(new ShapeToRender(producer.getShape(guard),
              new RenderingInformation(false, false, EditorColor.GUARDCOLOR,
                  EditorColor.GUARDCOLOR, 0)));
        }
      }
    }

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                              RenderingHints.VALUE_FRACTIONALMETRICS_ON);
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

    protected void drawShape(final ProxyShape p, final RenderingInformation status,
        final Graphics2D graphics)
    {
        p.draw(graphics, status);
    }

    /**
     * Just a wrapper class to aid with setting up priority Queue
     */
    private class ShapeToRender
        implements Comparable<ShapeToRender>
    {
        private final ProxyShape mShape;
        private final RenderingInformation mStatus;

        public ShapeToRender(final ProxyShape shape, final RenderingInformation status)
        {
            mShape = shape;
            mStatus = status;
        }

        public void draw(final Graphics2D g)
        {
            if (mStatus.getPriority() >= 0)
            {
                drawShape(mShape, mStatus, g);
            }
        }

        public int compareTo(final ShapeToRender o)
        {
            return mStatus.getPriority() - o.mStatus.getPriority();
        }

    }
}
