package net.sourceforge.waters.gui.transfer;

import java.util.Collection;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.base.IndexedHashSetSubject;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import java.awt.geom.Rectangle2D;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import java.awt.Point;
import net.sourceforge.waters.gui.renderer.GeometryTools;

public class GraphContainer
{
  public GraphContainer(IndexedSetSubject<NodeSubject> nodes,
                        Collection<EdgeSubject> edges)
  {
    mNodes = new IndexedHashSetSubject<NodeSubject>(nodes.size());
    mEdges = new ArrayList<EdgeSubject>(edges.size());
    double minx = Double.POSITIVE_INFINITY;
    double miny = Double.POSITIVE_INFINITY;
    for (NodeSubject node : nodes) {
      double x = 0;
      double y = 0;
      if (node instanceof GroupNodeSubject) {
        Rectangle2D rect = ((GroupNodeSubject) node).getGeometry().getRectangle();
        x = rect.getX();
        y = rect.getY();
      } else {
        Point2D pos = ((SimpleNodeSubject) node).getPointGeometry().getPoint();
        x = pos.getX();
        y = pos.getY();
      }
      mNodes.add(node.clone());
      minx = Math.min(minx, x);
      miny = Math.min(miny, y);
    }
    for (EdgeSubject edge : edges) {
      EdgeSubject nedge = edge.clone();
      NodeSubject n = mNodes.get(edge.getSource().getName());
      if (n == null) {
        continue;
      }
      nedge.setSource(n);
      n = mNodes.get(edge.getTarget().getName());
      if (n == null) {
        continue;
      }
      nedge.setTarget(n);
      mEdges.add(nedge);
    }
    mPoint = new Point((int)minx, (int)miny);
  }
  
  public IndexedSetSubject<NodeSubject> getNodes(Point trans)
  {
    trans = new Point(trans);
    trans.setLocation(trans.getX() - mPoint.getX(),
                      trans.getY() - mPoint.getY());
    IndexedSetSubject<NodeSubject> nodes =
      new IndexedHashSetSubject<NodeSubject>(mNodes.size());
    System.out.println("trans nodes");
    for (NodeSubject node : mNodes) {
      NodeSubject n = node.clone();
      GeometryTools.translate(n, trans);
      nodes.add(n);
    }
    return nodes;
  }
  
  public Collection<EdgeSubject> getEdges(IndexedSetSubject<NodeSubject> nodes,
                                          Point trans)
  {
    trans = new Point(trans);
    trans.setLocation(trans.getX() - mPoint.getX(),
                      trans.getY() - mPoint.getY());
    Collection<EdgeSubject> edges = new ArrayList<EdgeSubject>(mEdges.size());
    System.out.println("trans edges");
    for (EdgeSubject edge : mEdges) {
      EdgeSubject e = edge.clone();
      GeometryTools.translate(e, trans);
      e.setSource(nodes.get(edge.getSource().getName()));
      e.setTarget(nodes.get(edge.getTarget().getName()));
      edges.add(e);
    }
    return edges;
  }
  
  private final IndexedSetSubject<NodeSubject> mNodes;
  private final Collection<EdgeSubject> mEdges;
  private final Point mPoint;
}
