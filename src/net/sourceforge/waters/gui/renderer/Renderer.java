//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.PriorityQueue;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


public class Renderer
{

  //#########################################################################
  //# Constructor
  public Renderer(final GraphProxy graph, final List<MiscShape> shapes,
                  final ProxyShapeProducer producer)
  {
    mGraph = graph;
    mShapes = shapes;
    mProxyShapeProducer = producer;
    mQueue = new PriorityQueue<>();
  }


  //#########################################################################
  //# Invocation
  public void renderGraph(final Graphics2D graphics)
  {
    mGraphics = graphics;

    // Blocked events
    final LabelBlockProxy blocked = mGraph.getBlockedEvents();
    if (blocked != null) {
      addToQueue(blocked);
      listToRender(blocked.getEventIdentifierList());
    }

    // Nodes
    for (final NodeProxy proxy : mGraph.getNodes()) {
      addToQueue(proxy);
      if (proxy instanceof SimpleNodeProxy) {
        addToQueue(((SimpleNodeProxy) proxy).getLabelGeometry());
      }
    }

    // Edges
    for (final EdgeProxy edge : mGraph.getEdges()) {
      addToQueue(edge);
      addToQueue(edge.getLabelBlock());

      listToRender(edge.getLabelBlock().getEventIdentifierList());

      if (edge.getGuardActionBlock() != null) {
        addToQueue(edge.getGuardActionBlock());
        for (final BinaryExpressionProxy action : edge.getGuardActionBlock().getActions()) {
          addToQueue(action, EditorColor.ACTIONCOLOR);
        }
        final List<SimpleExpressionProxy> guards = edge.getGuardActionBlock().getGuards();
        // A naive solution for showing the added guards (after synthesis) with different color.
        if(guards.size() == 1)
        {
          addToQueue(guards.get(0), EditorColor.GUARDCOLOR); //there should be only one guard.
        }
        else if(guards.size() == 2)
        {
          addToQueue(guards.get(0), EditorColor.ADDEDGUARDCOLOR);
        }
        else if(guards.size() == 3)
        {
          for(int i= 1;i<guards.size();i++)
          {
            Color guardColor = EditorColor.GUARDCOLOR;
            if(i==2)
              guardColor = EditorColor.ADDEDGUARDCOLOR;

            addToQueue(guards.get(i), guardColor); //Change color
          }
        }
      }
    }

    mGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
    mGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                               RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    while (!mQueue.isEmpty()) {
      final ShapeToRender shapeToRender = mQueue.poll();
      shapeToRender.draw();
    }
    if (mShapes != null) {
      for (final MiscShape shape : mShapes) {
        shape.draw(mGraphics);
      }
    }
  }


  //#########################################################################
  //# Simple Access
  protected ProxyShapeProducer getProxyShapeProducer()
  {
    return mProxyShapeProducer;
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void drawShape(final ProxyShape p, final RenderingInformation status)
  {
    p.draw(mGraphics, status);
  }

  private void listToRender(final List<Proxy> list)
  {
    for (final Proxy proxy : list) {
      addToQueue(proxy);
      if (proxy instanceof ForeachProxy){
        final ForeachProxy foreach = (ForeachProxy) proxy;
        listToRender(foreach.getBody());
        addToQueue(foreach.getRange());
        if(foreach.getGuard() != null) {
          addToQueue(foreach.getGuard());
        }
      }
    }
  }

  protected void addToQueue(final Proxy proxy)
  {
    final RenderingContext context =
      mProxyShapeProducer.getRenderingContext();
    final RenderingInformation info = context.getRenderingInformation(proxy);
    final ProxyShape shape = mProxyShapeProducer.getShape(proxy);
    mQueue.offer(new ShapeToRender(shape, info));
  }

  protected void addToQueue(final Proxy proxy, final Color color)
  {
    final Color shadow = EditorColor.shadow(color);
    final RenderingContext context =
      mProxyShapeProducer.getRenderingContext();
    final RenderingInformation info = context.getRenderingInformation(proxy);
    final RenderingInformation fixed = new RenderingInformation(
        info.isSelected(), info.showHandles(), info.isUnderlined(),
        info.isFocused(), color, shadow, info.getPriority());
    final ProxyShape shape = mProxyShapeProducer.getShape(proxy);
    mQueue.offer(new ShapeToRender(shape, fixed));
  }


  //#########################################################################
  //# Inner Class ShapeToRender
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

    public void draw()
    {
      if (mStatus.getPriority() >= 0) {
        drawShape(mShape, mStatus);
      }
    }

    @Override
    public int compareTo(final ShapeToRender o)
    {
      return mStatus.getPriority() - o.mStatus.getPriority();
    }

  }


  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final List<MiscShape> mShapes;
  private final ProxyShapeProducer mProxyShapeProducer;
  private final PriorityQueue<ShapeToRender> mQueue;

  private Graphics2D mGraphics;

}








