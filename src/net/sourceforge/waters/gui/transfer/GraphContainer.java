//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   GraphContainer
//###########################################################################
//# $Id: GraphContainer.java,v 1.2 2007-02-25 09:42:49 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.transfer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.NodeSubject;


public class GraphContainer
{

  //#########################################################################
  //# Constructors
  public GraphContainer(final Collection<NodeSubject> nodes,
                        final Collection<EdgeSubject> edges)
  {
    final int numnodes = nodes.size();
    final int numedges = edges.size();
    final Map<NodeSubject,NodeSubject> map =
      new HashMap<NodeSubject,NodeSubject>(numnodes);
    mNodes = new ArrayList<NodeSubject>(numnodes);
    mEdges = new ArrayList<EdgeSubject>(numedges);
    for (final NodeSubject node : nodes) {
      final NodeSubject copy = node.clone();
      mNodes.add(copy);
      map.put(node, copy);
    }
    for (final EdgeSubject edge : edges) {
      final NodeSubject source = edge.getSource();
      final NodeSubject nsource = map.get(source);
      if (nsource == null) {
	continue;
      }
      final NodeSubject target = edge.getTarget();
      final NodeSubject ntarget = map.get(target);
      if (ntarget == null) {
	continue;
      }
      final EdgeSubject nedge = edge.clone();
      nedge.setSource(nsource);
      nedge.setTarget(ntarget);
      mEdges.add(nedge);
    }
  }

  public GraphContainer(final GraphContainer partner,
			final GraphSubject target,
			final Point2D newpos)
  {
    this(partner.getNodes(), partner.getEdges());
    int testindex = 0;
    final Set<String> names = new HashSet<String>(mNodes.size());
    final IndexedSetSubject<NodeSubject> existing =
      target.getNodesModifiable();
    final Point2D oldpos = partner.getTopLeftPosition();
    final double dx = newpos.getX() - oldpos.getX();
    final double dy = newpos.getY() - oldpos.getY();
    final Point2D delta = new Point2D.Double(dx, dy);
    for (final NodeSubject node : mNodes) {
      String name = node.getName();
      while (existing.containsName(name) || names.contains(name)) {
        name = "S" + testindex++;
      }
      names.add(name);
      node.setName(name);
      GeometryTools.translate(node, delta);
    }
    for (final EdgeSubject edge : mEdges) {
      GeometryTools.translate(edge, delta);
    }
  }


  //#########################################################################
  //# Constructors
  public Collection<NodeSubject> getNodes()
  {
    return mNodes;
  }

  public Collection<EdgeSubject> getEdges()
  {
    return mEdges;
  }

  public Point2D getTopLeftPosition()
  {
    if (mTopLeftPosition == null) {
      double minx = Double.POSITIVE_INFINITY;
      double miny = Double.POSITIVE_INFINITY;
      for (final NodeSubject node : mNodes) {
	final Point2D point = GeometryTools.getTopLeftPosition(node);
	final double x = point.getX();
	final double y = point.getY();
	minx = Math.min(minx, x);
	miny = Math.min(miny, y);
      }
      mTopLeftPosition = new Point2D.Double(minx, miny);
    }
    return mTopLeftPosition;
  }

  
  //#########################################################################
  //# Data Members
  private final Collection<NodeSubject> mNodes;
  private final Collection<EdgeSubject> mEdges;

  private Point2D mTopLeftPosition;

}
