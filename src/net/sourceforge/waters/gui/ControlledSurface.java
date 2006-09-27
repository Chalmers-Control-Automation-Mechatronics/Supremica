//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id: ControlledSurface.java,v 1.84 2006-09-27 03:13:44 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.renderer.EdgeProxyShape;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import java.awt.geom.Arc2D;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.waters.gui.command.*;
import net.sourceforge.waters.gui.EditorSurface.DRAGOVERSTATUS;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;
import net.sourceforge.waters.gui.renderer.GeneralShape;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.SplineKind;


public class ControlledSurface
    extends EditorSurface
    implements Observer, ModelObserver
{
    //#########################################################################
    //# Constructor
    public ControlledSurface(GraphSubject graph,
        ModuleSubject module,
        EditorWindowInterface r,
        ControlledToolbar t)
      throws GeometryAbsentException
    {
      super(graph, module, new SubjectShapeProducer(graph, module));
      graph.addModelObserver(this);
      root = r;
      mToolbar = t;
      t.attach(this);
      mController = new SelectListener();
      addMouseListener(mController);
      addMouseListener(new MouseAdapter()
      {
          public void mousePressed(MouseEvent e)
          {
              ControlledSurface.this.requestFocusInWindow();
          }
      });
      addMouseMotionListener(mController);
      addKeyListener(new KeySpy());
      setFocusable(true);
      dtListener = new DTListener();
      dropTarget = new DropTarget(this, dtListener);
    }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(ModelChangeEvent e)
  {
    //minimizeSize();
    drawnAreaBounds = null;
    Rectangle area = getDrawnAreaBounds();
    /*setBounds(0, 0,//-((int)drawnAreaBounds.getMinX() - 5 * gridSize), 
              //-((int)drawnAreaBounds.getMinY() - 5 * gridSize),
              ((int)drawnAreaBounds.getWidth() + 10 * gridSize),
              ((int)drawnAreaBounds.getHeight() + 10 * gridSize));*/
    setPreferredSize(new Dimension((int)drawnAreaBounds.getWidth() + gridSize * 10,
                     (int)drawnAreaBounds.getHeight() + gridSize * 10));
    repaint();
  }

  
  //#########################################################################
  public void examineCollisions() {
    
  }
  
  public ModuleSubject getModule()
  {
    return (ModuleSubject) super.getModule();
  }
  
  public List<ProxySubject> getObjectsAtPosition(int ex, int ey)
    {
        List<ProxySubject> objects = new ArrayList<ProxySubject>();
        for (NodeProxy node : getGraph().getNodes())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(node);
                if (shape != null)
                {
                    if (shape.isClicked(ex, ey))
                    {
                        objects.add((ProxySubject)shape.getProxy());
                    }
                    shape = getShapeProducer().getShape(node.getName());
                    if (shape != null)
                    {
                        if ((shape != null) && (shape.isClicked(ex, ey)))
                        {
                            objects.add((ProxySubject)shape.getProxy());
                        }
                    }
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        for (EdgeProxy edge : getGraph().getEdges())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(edge);
                if (shape != null)
                {
                    if (shape.isClicked(ex, ey))
                    {
                        objects.add((ProxySubject)shape.getProxy());
                    }
                    shape = getShapeProducer().getShape(edge.getLabelBlock());
                    if (shape != null)
                    {
                        if (shape.isClicked(ex, ey))
                        {
                            objects.add((ProxySubject)shape.getProxy());
                        }
                    }
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        return objects;
    }
    
    public RenderingInformation getRenderingInformation(Proxy o)
    {
        assert(o instanceof ProxySubject);
        ProxySubject p = (ProxySubject) o;
        boolean isFocused = isFocused(p);
        boolean selected = isRenderedSelected(p);
        boolean showHandles = selected;
        boolean error = false;
        EditorSurface.DRAGOVERSTATUS dragOver = EditorSurface.DRAGOVERSTATUS.NOTDRAG;
        int priority = getPriority(o);
        if (selected)
        {
            priority += 6;
        }
        if (mDontDraw.contains(o))
        {
            priority = -1;
        }
        if (isFocused)
        {
            dragOver = mDragOver;
        }
        return new RenderingInformation(showHandles, isFocused,
            EditorColor.getColor(o, dragOver, selected, error),
            EditorColor.getShadowColor(o, dragOver, selected, error),
            priority);
    }
    
    public Tool getCommand()
    {
        return mToolbar.getCommand();
    }
    
    public void setToolbar(ControlledToolbar t)
    {
        mToolbar = t;
    }
    
    public void setErrorList(Collection<? extends ProxySubject> error)
    {
        mError.clear();
        mError.addAll(error);
    }
    
    public Set<ProxySubject> getErrorList()
    {
        return new HashSet<ProxySubject>(mError);
    }
    
    public boolean isError(ProxySubject subject)
    {
        return mError.contains(subject);
    }
    
    private Shape edgeshape(Point2D p1, Point2D p2)
    {
      if (p1.equals(p2)) {
        double ARCEXTENT = EdgeProxyShape.Tear.ARCEXTENT;
        double TEARRATIO = EdgeProxyShape.Tear.TEARRATIO;
        Point2D p = p1;
        Point2D c = new Point((int)p1.getX() - 15, (int)p2.getY() - 15);
        double dist = (double) Math.sqrt(Math.pow(c.getX() - p.getX(), 2) +
                                         Math.pow(c.getY() - p.getY(), 2));
        double r = (dist * (1 -TEARRATIO)) / 2;
        double theta = Math.atan2(c.getY() - p.getY(), c.getX() - p.getX());
        Rectangle2D rect = new Rectangle2D.Double(Math.cos(theta) * (dist - r) + p.getX() - r,
                                                  Math.sin(theta) * (dist - r) + p.getY() - r,
                                                  r * 2, r * 2);
        Arc2D arc = new Arc2D.Double(rect, -(Math.toDegrees(theta) + ARCEXTENT/2),
            ARCEXTENT, Arc2D.OPEN);
        // different r for setting up where the handle is positioned
        r = SimpleNodeProxyShape.RADIUS;
        p1 = GeometryTools.getRadialPoint(arc.getStartPoint(), p, r);
        p2 = GeometryTools.getRadialPoint(arc.getEndPoint(), p, r);
        Line2D line1 = new Line2D.Double(p1, arc.getStartPoint());
        Line2D line2 = new Line2D.Double(arc.getEndPoint(), p2);
        GeneralPath curve = new GeneralPath(GeneralPath.WIND_NON_ZERO, 3);
        curve.append(line1, false);
        curve.append(arc , true);
        curve.append(line2, true);
        curve.closePath();
        return curve;
      } else {
        return new Line2D.Double(p1, p2);
      }
    }
    
    public List<MiscShape> getDrawnObjects()
    {
        List<MiscShape> shapes = new ArrayList<MiscShape>();
        if (dragSelect)
        {
            shapes.add(new GeneralShape(getDragRectangle(), EditorColor.SELECTCOLOR,
                EditorColor.shadow(EditorColor.SELECTCOLOR)));
        }
        if (newGroup)
        {
            shapes.add(new GeneralShape(getDragRectangle(), EditorColor.SELECTCOLOR,
                null));
        }
        if (hasDragged && (nodeIsSelected() || nodeGroupIsSelected())
        && getCommand() == Tool.EDGE)
        {
          NodeSubject n1 = (NodeSubject) selectedObjects.get(0);
          ProxySubject s = getObjectAtPosition(dragNowX, dragNowY);
          Point2D p1 = GeometryTools.defaultPosition(n1, new Point(dragStartX,
                                                                   dragStartY));
          Point2D p2;
          if (s instanceof NodeSubject) {
            p2 = GeometryTools.defaultPosition((NodeProxy)s,
                                               new Point(dragNowX, dragNowY));
          } else {
            p2 = new Point(dragNowX, dragNowY);
          }
          shapes.add(new GeneralShape(edgeshape(p1, p2)
                                      , EditorColor.SELECTCOLOR
                                      , null));
        }
        if (draggingTarget)
        {
            EdgeSubject edge = (EdgeSubject) selectedObjects.get(0);
            Point2D p1 = edge.getStartPoint().getPoint();
            ProxySubject s = getObjectAtPosition(dragNowX, dragNowY);
            Point2D p2;
            if (s instanceof NodeSubject) {
              p2 = GeometryTools.defaultPosition((NodeProxy)s,
                                                 new Point(dragNowX, dragNowY));
            } else {
              p2 = new Point(dragNowX, dragNowY);
            }
            shapes.add(new GeneralShape(edgeshape(p1, p2)
                                        , EditorColor.SELECTCOLOR
                                        , null));
        }
        if (draggingSource)
        {
            EdgeSubject edge = (EdgeSubject) selectedObjects.get(0);
            Point2D p2 = edge.getEndPoint().getPoint();
            ProxySubject s = getObjectAtPosition(dragNowX, dragNowY);
            Point2D p1;
            if (s instanceof NodeSubject) {
              p1 = GeometryTools.defaultPosition((NodeProxy)s,
                                                 new Point(dragNowX, dragNowY));
            } else {
              p1 = new Point(dragNowX, dragNowY);
            }
            shapes.add(new GeneralShape(edgeshape(p1, p2)
                                        , EditorColor.SELECTCOLOR
                                        , null));
        }
        if (mLine != null)
        {
            shapes.add(new GeneralShape(mLine, EditorColor.SELECTCOLOR,
                null));
        }
        return shapes;
    }
    
    public void setOptionsVisible(boolean v)
    {
        options.setVisible(v);
    }
    
    public boolean getNodesSnap()
    {
        return nodesSnap;
    }
    
    public void setNodesSnap(boolean n)
    {
        nodesSnap = n;
    }
    
    public GraphSubject getGraph()
    {
        return (GraphSubject)super.getGraph();
    }
    
    public boolean getControlPointsMove()
    {
        return controlPointsMove;
    }
    
    public void setControlPointsMove(boolean c)
    {
        controlPointsMove = c;
    }
    
    private void setOffset(ProxySubject s, MouseEvent e)
    {
        if (s instanceof LabelBlockSubject)
        {
            LabelBlockSubject l = (LabelBlockSubject) s;
            EdgeSubject edge = (EdgeSubject)l.getParent();
            xoff = e.getX() -
                (int)l.getGeometry().getOffset().getX();
            yoff = e.getY() -
                (int)l.getGeometry().getOffset().getY();
            xoff -= (int)edge.getGeometry().getPoints().get(0).getX();
            yoff -= (int)edge.getGeometry().getPoints().get(0).getY();
        }
        else if (s instanceof LabelGeometrySubject)
        {
            LabelGeometrySubject l = (LabelGeometrySubject) s;
            SimpleNodeSubject n = (SimpleNodeSubject)l.getParent();
            xoff = e.getX() - (int)l.getOffset().getX();
            yoff = e.getY() - (int)l.getOffset().getY();
            xoff -= (int)n.getPointGeometry().getPoint().getX();
            yoff -= (int)n.getPointGeometry().getPoint().getY();
        }
        else if (s instanceof GroupNodeSubject)
        {
            // We need this offset to place new edges
            GroupNodeSubject g = (GroupNodeSubject)s;
            xoff = e.getX() - (int)g.getGeometry().getRectangle().getX();
            yoff = e.getY() - (int)g.getGeometry().getRectangle().getY();
        }
    }
    
  private void updateModel(MouseEvent e)
  {
    if (!draggingSource && !draggingTarget) {
      assert(mDummy != null);
      Command move = new MoveObjects(mDummy.getChanged(), getGraph());
      root.getUndoInterface().executeCommand(move);
    } else {
      EdgeSubject edge = (EdgeSubject) selectedObjects.get(0);
      NodeSubject node =
          (NodeSubject)getNodeOrNodeGroupAtPosition(e.getX(), e.getY());
      Point2D p = e.getPoint();
      if (node != null) {
        if ((draggingSource && node != edge.getSource())
            || (draggingTarget && node != edge.getTarget())) {
          if (node instanceof SimpleNodeProxy) {
            p = ((SimpleNodeProxy)node).getPointGeometry().getPoint();
          } else {
            p = GeometryTools.findIntersection(
                 ((GroupNodeSubject)node).getGeometry().getRectangle(), p);
          }
          Command move = new MoveEdgeCommand(this, edge,
                                             node, draggingSource,
                                             (int)p.getX(), (int)p.getY());
          root.getUndoInterface().executeCommand(move);
        }
      }
      mDontDraw.remove(edge);
    }
    if (mDummy != null) {
      mDummy.removeModelObserver(this);
      mDummy = null;
    }
    mDummyShape = null;
    draggingSource = false;
    draggingTarget = false;
    draggingInitial = false;
    root.getUndoInterface().executeCommand(new UpdateErrorCommand(this));
  }
    
    private void setObjectPosition(Subject s, int dx, int dy)
    {
        assert(mDummy != null);
        // Is it a node?
        if (s instanceof SimpleNodeSubject)
        {
            setNodePosition((SimpleNodeSubject)s, dx, dy);
        }
        // Is it a nodegroup?
        else if (s instanceof GroupNodeSubject)
        {
            setGroupNodePosition((GroupNodeSubject)s, dx, dy);
        }
        // Is it an edge?
        else if ((s instanceof EdgeSubject) && (!controlPointsMove || !(nodeIsSelected() || nodeGroupIsSelected())))
        {
            setEdgePosition((EdgeSubject)s, dx, dy);
        }
        
        // DONT MOVE LABELS IN MULTI MODE, (IT'S NO FUN TO TRY TO GET THE OFFSETS RIGHT...)
        if (!edgeIsSelected() && !nodeGroupIsSelected()
        && !nodeIsSelected())
        {
            // Is it a label?
            if (s instanceof LabelGeometrySubject) // Don't move!
            {
                setLabelPosition((LabelGeometrySubject)s, dx, dy);
            }
            // Is it a labelgroup?
            else if (s instanceof LabelBlockSubject) // Don't move
            {
                setLabelBlockPosition((LabelBlockSubject)s, dx, dy);
            }
        }
    }
    
    private void setNodePosition(SimpleNodeSubject node, int dx, int dy)
    {
        assert(mDummy != null);
        // Where did it use to be?
        SimpleNodeSubject dummy = (SimpleNodeSubject)mDummy.getCopy(node);
        if (!draggingInitial)
        {
            Point2D p = dummy.getPointGeometry().getPoint();
            p.setLocation(p.getX() + dx, p.getY() + dy);
            // Move
            dummy.getPointGeometry().setPoint(p);
        }
        else
        {
            Point2D p = dummy.getInitialArrowGeometry().getPoint();
            p.setLocation(p.getX() + dx, p.getY() + dy);
            // Move
            dummy.getInitialArrowGeometry().setPoint(p);
        }
    }
    
    private void setGroupNodePosition(GroupNodeSubject nodeGroup, int dx, int dy)
    {
        assert(mDummy != null);
        //Rectangle2D.Double b = new Rectangle2D.Double();
        //b.setRect(nodeGroup.getBounds());
        GroupNodeSubject dummy = (GroupNodeSubject)mDummy.getCopy(nodeGroup);
        Rectangle2D rect = dummy.getGeometry().getRectangle();
        rect.setFrame(rect.getMinX() + dx, rect.getMinY() + dy,
            rect.getWidth(), rect.getHeight());
        if (nodesSnap)
        {
            rect = findGridRect(rect);
        }
        dummy.getGeometry().setRectangle(rect);
    }
    
    private void setEdgePosition(EdgeSubject edge, int dx, int dy)
    {
        assert(mDummy != null);
        EdgeSubject dummy = (EdgeSubject)mDummy.getCopy(edge);
        if (draggingSource)
        {
            Point2D p = dummy.getStartPoint().getPoint();
            p.setLocation(p.getX() + dx, p.getY() + dy);
            dummy.getStartPoint().setPoint(p);
            p = GeometryTools.getMidPoint(dummy.getStartPoint().getPoint(),
                dummy.getEndPoint().getPoint());
            dummy.getGeometry().getPointsModifiable().set(0, p);
        }
        else if (draggingTarget)
        {
            Point2D p = dummy.getEndPoint().getPoint();
            p.setLocation(p.getX() + dx, p.getY() + dy);
            dummy.getEndPoint().setPoint(p);
            p = GeometryTools.getMidPoint(dummy.getStartPoint().getPoint(),
                dummy.getEndPoint().getPoint());
            dummy.getGeometry().getPointsModifiable().set(0, p);
        }
        else
        {
            Point2D p = dummy.getGeometry().getPoints().get(0);
            p.setLocation(p.getX() + dx, p.getY() + dy);
            dummy.getGeometry().getPointsModifiable().set(0, p);
        }
    }
    
    private void setLabelPosition(LabelGeometrySubject label, int dx, int dy)
    {
        assert(mDummy != null);
        LabelGeometrySubject dummy = (LabelGeometrySubject)mDummy.getCopy(label);
        Point2D p = dummy.getOffset();
        p.setLocation(p.getX() + dx, p.getY() + dy);
        dummy.setOffset(p);
    }
    
    private void setLabelBlockPosition(LabelBlockSubject label, int dx, int dy)
    {
        assert(mDummy != null);
        LabelBlockSubject dummy = (LabelBlockSubject)mDummy.getCopy(label);
        Point2D p = dummy.getGeometry().getOffset();
        p.setLocation(p.getX() + dx, p.getY() + dy);
        dummy.getGeometry().setOffset(p);
    }
    
    private void draggingInitial(ProxySubject s, MouseEvent e)
    {
        if (s instanceof SimpleNodeSubject)
        {
            try
            {
                ProxyShape p = getShapeProducer().getShape(s);
                Handle h = p.getClickedHandle(e.getX(), e.getY());
                if (h != null)
                {
                    draggingInitial = true;
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
    }
    
    private void draggingEdge(ProxySubject s, MouseEvent e)
    {
        if (s instanceof EdgeSubject)
        {
            try
            {
                ProxyShape p = getShapeProducer().getShape(s);
                Handle h = p.getClickedHandle(e.getX(), e.getY());
                if (h != null)
                {
                    mDontDraw.add(s);
                    System.err.println("before switch " + h + " " + h.getType());
                    switch (h.getType())
                    {
                        case SOURCE:
                            System.err.println(h + " " + h.getType());
                            draggingSource = true;
                            return;
                        case TARGET:
                            System.err.println(h + " " + h.getType());
                            draggingTarget = true;
                            return;
                        default:
                            assert(false); //if it's an edge it must be one of these
                    }
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
    }
    
    private void resizingGroupNode(ProxySubject s, MouseEvent e)
    {
        if (s instanceof GroupNodeSubject)
        {
            try
            {
                ProxyShape p = getShapeProducer().getShape(s);
                Handle h = p.getClickedHandle(e.getX(), e.getY());
                if (h != null)
                {
                    mResize = new GroupResizer(((GroupNodeSubject)s)
                    .getGeometry().getRectangle(), h.getType());
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
    }
    
    private void resizeGroupNode(GroupNodeSubject s, Rectangle2D rect)
    {
        assert(mDummy != null);
        ((GroupNodeSubject)mDummy.getCopy(s)).getGeometry().setRectangle(rect);
    }
    
    /**
     * Deletes all selected objects.
     */
    public void deleteSelected()
    {
        CompoundCommand compound = new CompoundCommand("Deletion");
        CompoundCommand deselection = new CompoundCommand("Deselection");
        for (ProxySubject s : selectedObjects)
        {
            if (!(s instanceof LabelBlockSubject) ||
                !hasSelected((LabelBlockSubject) s))
            {
                Command unselect = new UnSelectCommand(this, s);
                deselection.addCommand(unselect);
            }
            if (s instanceof SimpleNodeSubject)
            {
                Command deleteNode =
                    new DeleteNodeCommand(getGraph(), (SimpleNodeSubject) s);
                deleteNode.execute();
                compound.addCommand(deleteNode);
            }
            else if (s instanceof EdgeSubject)
            {
                if (getGraph().getEdges().contains(s))
                {
                    Command deleteEdge =
                        new DeleteEdgeCommand(getGraph(), (EdgeSubject) s);
                    deleteEdge.execute();
                    compound.addCommand(deleteEdge);
                }
            }
            else if (s instanceof GroupNodeSubject)
            {
                Command deleteNodeGroup =
                    new DeleteNodeGroupCommand(getGraph(), (GroupNodeSubject) s);
                deleteNodeGroup.execute();
                compound.addCommand(deleteNodeGroup);
            }
            else if (s instanceof LabelBlockSubject)
            {
                LabelBlockSubject l = (LabelBlockSubject) s;
                if (!hasSelected(l))
                {
                    CompoundCommand c = new CompoundCommand();
                    for (AbstractSubject a : l.getEventListModifiable())
                    {
                        c.addCommand(new RemoveEventCommand(l, a));
                    }
                    c.execute();
                    compound.addCommand(c);
                }
            }
            if ((s.getParent() != null) &&
                (s.getParent().getParent() instanceof EventListExpressionSubject))
            {
                EventListExpressionSubject e =
                    (EventListExpressionSubject) s.getParent().getParent();
                Command removeEvent = new RemoveEventCommand(e, (AbstractSubject) s);
                removeEvent.execute();
                compound.addCommand(removeEvent);
            }
        }
        root.getUndoInterface().addUndoable(new UndoableCommand(compound));
        root.getUndoInterface().executeCommand(deselection);
    }
    
    public boolean hasSelected(EventListExpressionSubject e)
    {
        for (Subject s : selectedObjects)
        {
            if (e.getEventList().contains(s))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasSelectedLabel()
    {
        for (Subject s : selectedObjects)
        {
            if ((s.getParent() != null) &&
                (s.getParent().getParent()
                instanceof EventListExpressionSubject))
            {
                return true;
            }
        }
        return false;
    }
    
    public GraphProxy getDrawnGraph()
    {
        if (mDummy != null)
        {
            return mDummy;
        }
        else
        {
            return getGraph();
        }
    }
    
    public ProxyShapeProducer getShapeProducer()
    {
        if (mDummy == null)
        {
            return super.getShapeProducer();
        }
        assert(mDummyShape != null);
        return mDummyShape;
    }
    
    public void select(ProxySubject s)
    {
        if (!selectedObjects.contains(s))
        {
            if (s instanceof SimpleNodeSubject)
            {
                Iterator<ProxySubject> i = selectedObjects.iterator();
                while (i.hasNext())
                {
                    ProxySubject s2 = i.next();
                    if (((SimpleNodeSubject)s).getLabelGeometry() == s2)
                    {
                        Command c = new UnSelectCommand(this, s2);
                        root.getUndoInterface().executeCommand(c);
                        break;
                    }
                }
                selectedObjects.add(s);
            }
            if (s instanceof EdgeSubject)
            {
                Iterator<ProxySubject> i = selectedObjects.iterator();
                while (i.hasNext())
                {
                    ProxySubject s2 = i.next();
                    if (((EdgeSubject)s).getLabelBlock() == s2)
                    {
                        Command c = new UnSelectCommand(this, s2);
                        root.getUndoInterface().executeCommand(c);
                        break;
                    }
                }
                selectedObjects.add(s);
            }
            if (s instanceof LabelGeometrySubject ||
                s instanceof LabelBlockSubject)
            {
                if (!selectedObjects.contains(s.getParent()))
                {
                    selectedObjects.add(s);
                }
            }
            if (s instanceof GroupNodeSubject || s instanceof IdentifierSubject)
            {
                selectedObjects.add(s);
            }
        }
    }
    
    public void unselect(ProxySubject s)
    {
        selectedObjects.remove(s);
    }
    
    public void unselectAll()
    {
        while (selectedObjects.size() > 0)
        {
            unselect(selectedObjects.get(0));
        }
    }
    
    public boolean isRenderedSelected(ProxySubject s)
    {
        if (mDummy != null)
        {
            s = (ProxySubject)mDummy.getOriginal(s);
        }
        boolean selected = selectedObjects.contains(s);
        if (!selected)
        {
            if (s instanceof LabelGeometrySubject ||
                s instanceof LabelBlockSubject)
            {
                return isSelected((ProxySubject) s.getParent());
            }
            if (s instanceof IdentifierSubject)
            {
                return
                    isSelected((ProxySubject) s.getParent().getParent())
                    && !hasSelected
                    ((EventListExpressionSubject) s.getParent().getParent());
            }
        }
        return selected;
    }
    
    public boolean isSelected(ProxySubject s)
    {
        return selectedObjects.contains(s);
    }
    
    public boolean isFocused(ProxySubject s)
    {
        // Special case
        if (mFocusedObject == null)
            return false;

        // Check if object is focused or the child or parent of something focused
        ProxySubject object = s;
        // Find children
        List<Proxy> childrenOfFocused = getChildren(mFocusedObject);
        // Find parents
        Subject parentSubject = mFocusedObject.getParent();
        Proxy parentOfFocused;
        // Is there a parent?
        if (parentSubject != null && parentSubject instanceof Proxy)
            parentOfFocused = (Proxy) parentSubject;
        else
            parentOfFocused = mFocusedObject;
        
        return object.equals(mFocusedObject) || childrenOfFocused.contains(object) || object.equals(parentOfFocused);
    }
    
    public DRAGOVERSTATUS getDragOver(ProxySubject s)
    {
        if (!isFocused(s))
        {
            return DRAGOVERSTATUS.NOTDRAG;
        }
        return mDragOver;
    }
    
    /*
    private void selectChange(EditorObject o)
    {
        // Only if SELECT is chosen multiple selection is possible...
        if (!(T.getPlace() == EditorToolbar.SELECT))
        {
            unselectAll();
        }
     
        // If object is selected unselect
        if (selectedObjects.contains(o))
        {
            unselect(o);
        }
        else
        {
            select(o);
        }
    }
     */
    
    private boolean nodeIsSelected()
    {
        for (ProxySubject s: selectedObjects)
        {
            if (s instanceof SimpleNodeSubject)
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean nodeGroupIsSelected()
    {
        for (ProxySubject s: selectedObjects)
        {
            if (s instanceof GroupNodeSubject)
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean edgeIsSelected()
    {
        for (ProxySubject s: selectedObjects)
        {
            if (s instanceof EdgeSubject)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the closest coordinate (works for both x and y) lying on the grid.
     */
    private int findGrid(int x)
    {
        return (x+gridSize/2)/gridSize*gridSize;
    }
    
    private Point2D findGridPoint(Point2D p)
    {
        return new Point(findGrid((int)p.getX()), findGrid((int)p.getY()));
    }
    
    private Rectangle2D findGridRect(Rectangle2D r)
    {
        Rectangle2D rect = new Rectangle();
        rect.setFrameFromDiagonal(findGrid((int)r.getMinX()),
            findGrid((int)r.getMinY()),
            findGrid((int)r.getMaxX()),
            findGrid((int)r.getMaxY()));
        return rect;
    }
    
    public ProxySubject getObjectAtPosition(int ex, int ey)
    {
        return (ProxySubject)super.getObjectAtPosition(ex, ey);
    }
    
    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            ProxySubject s = getObjectAtPosition(e.getX(), e.getY());
            if (!(s == null))
            {
                if (s instanceof SimpleNodeSubject)
                {
                    SimpleNodeSubject node = (SimpleNodeSubject) s;
                    
                    EditorNodePopupMenu popup = new EditorNodePopupMenu(this, node);
                    popup.show(this, e.getX(), e.getY());
                }
                if (s instanceof GroupNodeSubject)
                {
                    GroupNodeSubject node = (GroupNodeSubject) s;
                    
                    EditorNodeGroupPopupMenu popup = new EditorNodeGroupPopupMenu(this, node);
                    popup.show(this, e.getX(), e.getY());
                }
                if (s instanceof EdgeSubject)
                {
                    EdgeSubject edge = (EdgeSubject) s;
                    
                    EditorEdgePopupMenu popup = new EditorEdgePopupMenu(this, edge);
                    popup.show(this, e.getX(), e.getY());
                }
                if (s instanceof LabelBlockSubject)
                {
                    LabelBlockSubject label =
                        (LabelBlockSubject) s;
                    
                    EditorLabelBlockPopupMenu popup = new
                        EditorLabelBlockPopupMenu(this, label);
                    popup.show(this, e.getX(), e.getY());
                }
            } else {
              final EditorWindowInterface iface = getEditorInterface();
              final EditorSurfacePopupMenu popup =
                new EditorSurfacePopupMenu(iface, selectedObjects);
              popup.show(this, e.getX(), e.getY());
            }
        }
    }
    
    /**
     * Returns a list of all objects within a rectangle (to be selected).
     */
    public LinkedList<ProxySubject> getDragSelection()
    {
        LinkedList<ProxySubject> selection = new LinkedList<ProxySubject>();
        
        // The bounds of the drag
        Rectangle bounds = getDragRectangle().getBounds();
        for (NodeProxy node : getGraph().getNodes())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(node);
                if (bounds.contains(shape.getShape().getBounds()))
                {
                    selection.add((ProxySubject)shape.getProxy());
                }
                shape = getShapeProducer().getShape(node.getName());
                if ((shape != null) &&
                    (bounds.contains(shape.getShape().getBounds())))
                {
                    selection.add((ProxySubject)shape.getProxy());
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        for (EdgeProxy edge : getGraph().getEdges())
        {
            try
            {
                ProxyShape shape = getShapeProducer().getShape(edge);
                if (bounds.contains(shape.getShape().getBounds()))
                {
                    selection.add((ProxySubject)shape.getProxy());
                }
                shape = getShapeProducer().getShape(edge.getLabelBlock());
                if (bounds.contains(shape.getShape().getBounds()))
                {
                    selection.add((ProxySubject)shape.getProxy());
                }
            }
            catch (VisitorException vis)
            {
                vis.printStackTrace();
            }
        }
        
        return selection;
    }
    
    /**
     * Updates highlighting based on the current location of the mouse pointer.
     */
    private void updateHighlighting(Point2D e)
    {
        boolean needRepaint = false;
        List<ProxySubject> objects = getObjectsAtPosition((int)e.getX(),
            (int)e.getY());
        ProxySubject object = null;
        if (!objects.isEmpty())
        {
            object = Collections.max(objects,
                new Comparator<ProxySubject>()
            {
                public int compare(ProxySubject s1, ProxySubject s2)
                {
                    int c1 = mController.getHighlightPriority(s1);
                    int c2 = mController.getHighlightPriority(s2);
                    if (isSelected(s1) && c1 >= 0)
                    {
                        c1 += Integer.MAX_VALUE/2;
                    }
                    if (isSelected(s2) && c2 >= 0)
                    {
                        c2 += Integer.MAX_VALUE/2;
                    }
                    return c1 - c2;
                }
            });
        }
        
        // Highlight things that are moved over...
        ProxySubject s = null;
        if (mController.getHighlightPriority(object) >= 0 || mDND)
        {
            s = object;
        }
        
        // Unhighlight highligted stuff not in focus
        if ((mFocusedObject != null) && !mFocusedObject.equals(s))
        {
            // Unhighlight object
            mFocusedObject = null;
            
            needRepaint = true;
        }
        
        // Highlight stuff in focus!
        if (s != null && !s.equals(mFocusedObject))
        {
            // Highlight object
            mFocusedObject = s;
            needRepaint = true;
        }
        
        // Need repaint?
        if (needRepaint)
        {
            repaint(false);
        }
    }
    
    public void createOptions(EditorWindowInterface root)
    {
        options = new EditorOptions(root);
    }
    
        /*
        public EditorLabelGroup getSelectedLabelGroup()
        {
                if (selectedEdge != null)
                {
                        for (int i = 0; i < events.size(); i++)
                        {
                                if (((EditorLabelGroup) events.get(i)).getParent() == selectedEdge)
                                {
                                        return ((EditorLabelGroup) events.get(i));
                                }
                        }
                }
         
                return selectedLabelGroup;
        }
         
        public EditorNode getSelectedNode()
        {
                return selectedNode;
        }
         */
    private Point2D getMod()
    {
        assert(mDummy != null);
        Point2D mod = new Point();
        for (final ProxySubject s : selectedObjects)
        {
            if (s instanceof SimpleNodeSubject)
            {
                Point2D p = ((SimpleNodeSubject)mDummy.getCopy(s))
                .getPointGeometry().getPoint();
                mod.setLocation(findGrid((int)p.getX()) - (int)p.getX(),
                    findGrid((int)p.getY()) - (int)p.getY());
                break;
            }
            else if (s instanceof GroupNodeSubject)
            {
                Rectangle2D r = ((GroupNodeSubject)mDummy.getCopy(s))
                .getGeometry().getRectangle();
                mod.setLocation(findGrid((int)r.getX()) - (int)r.getX(),
                    findGrid((int)r.getY()) - (int)r.getY());
                break;
            }
        }
        return mod;
    }
    
    private void setDrag(MouseEvent e)
    {
        if (!hasDragged)
        {
            dragStartX = e.getX();
            dragStartY = e.getY();
        }
        dragNowX = e.getX();
        dragNowY = e.getY();
        if (newGroup && nodesSnap)
        {
            dragStartX = findGrid(dragStartX);
            dragStartY = findGrid(dragStartY);
            dragNowX = findGrid(dragNowX);
            dragNowY = findGrid(dragNowY);
        }
    }
    
    
    private class DTListener extends DropTargetAdapter
    {
        //#######################################################################
        //# Interface java.awt.dnd.DropTargetAdapter
        public void dragOver(final DropTargetDragEvent e)
        {
            mDND = true;
            updateHighlighting(e.getLocation());
            Line2D.Double line = null;
            int operation = DnDConstants.ACTION_MOVE;
            mDragOver = EditorSurface.DRAGOVERSTATUS.NOTDRAG;
            ProxySubject s = null;
            EventListExpressionSubject el = null;
            try
            {
                final IdentifierWithKind i = (IdentifierWithKind)
                e.getTransferable().getTransferData(FLAVOUR);
                final EventKind ek = i.getKind();
                final IdentifierSubject ip = i.getIdentifier();
                s = mFocusedObject;
                if (ek == null)
                {
                    if (s instanceof SimpleNodeSubject)
                    {
                      el = ((SimpleNodeSubject)s).getPropositions();
                    }
                    else if (s instanceof EdgeSubject)
                    {
                      el = ((EdgeSubject)s).getLabelBlock();
                    }
                    else if (s instanceof LabelBlockSubject)
                    {
                      el = (LabelBlockSubject)s;
                    }
                    else if (s instanceof LabelGeometrySubject)
                    {
                      el = ((SimpleNodeSubject)s.getParent()).getPropositions();
                    }
                }
                else if (ek.equals(EventKind.PROPOSITION))
                {
                    if (s instanceof SimpleNodeSubject)
                    {
                      el = ((SimpleNodeSubject)s).getPropositions();
                    }
                    else if (s instanceof LabelGeometrySubject)
                    {
                      el = ((SimpleNodeSubject)s.getParent()).getPropositions();
                    }
                }
                else if (ek.equals(EventKind.CONTROLLABLE) ||
                    ek.equals(EventKind.UNCONTROLLABLE))
                {
                  if (s instanceof EdgeSubject)
                  {
                      el = ((EdgeSubject)s).getLabelBlock();
                  }
                  else if (s instanceof LabelBlockSubject)
                  {
                    el = (LabelBlockSubject)s;
                    LabelBlockSubject l = (LabelBlockSubject) s;
                    Point2D p = e.getLocation();
                    try {
                      double x1 = getShapeProducer().getShape(l).getShape()
                                                    .getBounds().getMinX();
                      double x2 = getShapeProducer().getShape(l).getShape()
                                                    .getBounds().getMaxX();
                      for (Proxy proxy : l.getEventList()) {
                        ProxyShape shape = getShapeProducer().getShape(proxy);
                        Rectangle2D r = shape.getShape().getBounds();
                        if (p.getY() < r.getCenterY()) {
                          line = new Line2D.Double(x1, r.getMinY(),
                                                   x2, r.getMinY());
                          break;
                        } else {
                          line = new Line2D.Double(x1, r.getMaxY(),
                                                   x2, r.getMaxY());
                        }
                      }
                    } catch (VisitorException v) {
                      v.printStackTrace();
                    }
                  }
                }
                if (el != null) {
                  boolean present = false;
                  for (Object o : el.getEventList()) {
                    if (o.toString().equals(ip.toString())) {
                      present = true;
                      break;
                    }
                  }
                  if (present) {
                    line = null;
                  } else {
                    operation = DnDConstants.ACTION_COPY;
                  }
                }
            }
            catch (final UnsupportedFlavorException exception)
            {
                throw new IllegalArgumentException(exception);
            }
            catch (final IOException exception)
            {
                throw new IllegalArgumentException(exception);
            }
            if (s != null)
            {
                if (operation == DnDConstants.ACTION_COPY)
                {
                    mDragOver = EditorSurface.DRAGOVERSTATUS.CANDROP;
                }
                else
                {
                    mDragOver = EditorSurface.DRAGOVERSTATUS.CANTDROP;
                }
            }
            if ((line != null && !line.equals(mLine))
            || (line == null && mLine != null))
            {
                mLine = line;
                repaint();
            }
            e.getDropTargetContext().getDropTarget().setDefaultActions(operation);
            e.acceptDrag(operation);
        }
        
        public void dragExit(DropTargetEvent e)
        {
          mDND = false;
          mDragOver = EditorSurface.DRAGOVERSTATUS.NOTDRAG;
        }
        
        public void drop(final DropTargetDropEvent e)
        {
            mDND = false;
            mLine = null;
            try
            {
                // Drag is finished!
          /*if (mDrag != null)
            {
            setDragOver(mDrag, NOTDRAG);
            }*/
                
                if (e.getDropTargetContext().getDropTarget().
                    getDefaultActions() == DnDConstants.ACTION_COPY)
                {
                    final IdentifierWithKind i = (IdentifierWithKind)
                    e.getTransferable().getTransferData(FLAVOUR);
                    final IdentifierSubject ip = i.getIdentifier();
                    final ProxySubject s = mFocusedObject;
                    mDragOver = EditorSurface.DRAGOVERSTATUS.NOTDRAG;
                    if (s instanceof SimpleNodeSubject)
                    {
                        addToNode((SimpleNodeSubject) s, ip);
                        e.dropComplete(true);
                        return;
                    }
                    else if (s instanceof EdgeSubject)
                    {
                        addToEdge((EdgeSubject) s, ip, e);
                        e.dropComplete(true);
                        return;
                    }
                    else if (s instanceof LabelBlockSubject)
                    {
                        addToLabelGroup((LabelBlockSubject) s, ip, e);
                        e.dropComplete(true);
                        return;
                    }
                    else if (s instanceof LabelGeometrySubject)
                    {
                        addToLabel((LabelGeometrySubject) s, ip);
                        e.dropComplete(true);
                        return;
                    }
                }
            }
            catch (final UnsupportedFlavorException exception)
            {
                throw new IllegalArgumentException(exception);
            }
            catch (final IOException exception)
            {
                throw new IllegalArgumentException(exception);
            }
            e.dropComplete(false);
        }
        
        
        //###################################################################
        //# Auxiliary Methods
        private void addToNode(SimpleNodeSubject n, IdentifierSubject i)
        {
            final IdentifierSubject cloned = i.clone();
            Command addEvent = new AddEventCommand(n.getPropositions(),
                cloned, 0);
            root.getUndoInterface().executeCommand(addEvent);
            repaint();
        }
        
        private void addToLabel(LabelGeometrySubject l, IdentifierSubject i)
        {
            addToNode((SimpleNodeSubject)l.getParent(), i);
        }
        
        private void addToEdge(EdgeSubject edge, IdentifierSubject ip, final DropTargetDropEvent e)
        {
            addToLabelGroup(edge.getLabelBlock(), ip, e);
        }
        
        private void addToLabelGroup(LabelBlockSubject l, IdentifierSubject i, final DropTargetDropEvent e)
        {
            final IdentifierSubject cloned = i.clone();
            int pos = 0;
            Point2D p = e.getLocation();
            for (Proxy proxy : l.getEventList())
            {
                try
                {
                    ProxyShape s = getShapeProducer().getShape(proxy);
                    Rectangle2D r = s.getShape().getBounds();
                    if (p.getY() < r.getCenterY())
                    {
                        break;
                    }
                    pos++;
                }
                catch (VisitorException v)
                {
                    v.printStackTrace();
                }
            }
            if (pos == -1)
            {
                pos = 0;
            }
            Command addEvent = new AddEventCommand(l,
                cloned, pos);
            root.getUndoInterface().executeCommand(addEvent);
            repaint();
        }
    }
    
    public void update(EditorChangedEvent e)
    {
        if (e instanceof ToolbarChangedEvent)
        {
            MouseListener[] mouse = getMouseListeners();
            MouseMotionListener[] motion = getMouseMotionListeners();
            for (int i = 0; i < mouse.length; i++)
            {
                removeMouseListener(mouse[i]);
            }
            for (int i = 0; i < motion.length; i++)
            {
                removeMouseMotionListener(motion[i]);
            }
            switch (getCommand())
            {
                case SELECT:
                    mController = new SelectListener();
                    break;
                case EDGE:
                    mController = new EdgeListener();
                    break;
                case NODE:
                    mController = new NodeListener();
                    break;
                case NODEGROUP:
                    mController = new GroupNodeListener();
                    break;
                case INITIAL:
                    mController = new InitialListener();
                    break;
            }
            addMouseListener(mController);
            addMouseMotionListener(mController);
        }
    }
    
    private class SelectListener
        extends ToolController
    {
        public int getHighlightPriority(ProxySubject s)
        {
            if (s instanceof SimpleNodeSubject)
            {
                return 5;
            }
            else if (s instanceof EdgeSubject)
            {
                if (draggingSource || draggingTarget)
                {
                    return -1;
                }
                return 4;
            }
            else if (s instanceof LabelGeometrySubject)
            {
                if (draggingSource || draggingTarget)
                {
                    return -1;                }
                return 3;
            }
            else if (s instanceof LabelBlockSubject)
            {
                if (draggingSource || draggingTarget)
                {
                    return -1;
                }
                return 2;
            }
            else if (s instanceof GroupNodeSubject)
            {
                return 1;
            }
            return -1;
        }
        
        public void mouseClicked(MouseEvent e)
        {
            requestFocusInWindow();
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                // Singleclick?
                if (e.getClickCount() == 1)
                {
                    // What was clicked?
                    ProxySubject s = mFocusedObject;
                    // Special stuff for labelgroup clicks?
                    // (This is not working properly!)
                    if (isSelected(s) && s != null &&
                        previouslySelected.contains(s) &&
                        s instanceof LabelBlockSubject)
                    {
                        LabelBlockSubject l = (LabelBlockSubject) s;
                        ProxySubject label = null;
                        // find the Label which was clicked
                        for (ProxySubject sub : l.getEventListModifiable())
                        {
                            try
                            {
                                ProxyShape shape = getShapeProducer().getShape(sub);
                                if (shape.getShape().contains(e.getX(), e.getY()))
                                {
                                    label = sub;
                                    break;
                                }
                            }
                            catch (VisitorException vis)
                            {
                                vis.printStackTrace();
                            }
                        }
                        if (label != null)
                        {
                            Command c;
                            if (e.isControlDown())
                            {
                                if (isSelected(label))
                                {
                                    c = new UnSelectCommand(ControlledSurface.this, label);
                                }
                                else
                                {
                                    c = new SelectCommand(ControlledSurface.this, label);
                                }
                            }
                            else
                            {
                                c = new CompoundCommand();
                                List<ProxySubject> deselect =
                                    new LinkedList<ProxySubject>(l.getEventListModifiable());
                                deselect.retainAll(selectedObjects);
                                ((CompoundCommand)c).addCommand
                                    (new UnSelectCommand(ControlledSurface.this,
                                    deselect));
                                ((CompoundCommand)c).addCommand
                                    (new SelectCommand(ControlledSurface.this,
                                    label));
                                ((CompoundCommand)c).end();
                            }
                            root.getUndoInterface().executeCommand(c);
                        }
                    }
                }
                else if (e.getClickCount() == 2)
                {
                    // Doubleclick
                    // Change names on double clicking a label, change order when clicking labelgroup!
          /* What's this? No comments?
             if (selectedNode != null)
             {
             if (selectedNode.getPropGroup().wasClicked(e.getX(), e.getY()) && selectedNode.getPropGroup().getVisible())
             {
             EditorPropGroup p = selectedNode.getPropGroup();
           
             p.setSelectedLabel(e.getX(), e.getY());
             repaint();
             }
             }
           */
                    
                    // What was clicked?
                    ProxySubject s = getObjectAtPosition(e.getX(), e.getY());
                    if (s != null)
                    {
                        if (s instanceof LabelGeometrySubject)
                        {
                            LabelGeometrySubject l = (LabelGeometrySubject) s;
                            JTextField text = new NameEditField(l);
                            ControlledSurface.this.add(text);
                            text.setVisible(true);
                            text.requestFocus();
                        }
                    }
                }
            }
        }
        
        public void mousePressed(MouseEvent e)
        {
            // This is for triggering the popup
            maybeShowPopup(e);
            setDrag(e);
            previouslySelected.clear();
            previouslySelected.addAll(selectedObjects);
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                lastX = e.getX();
                lastY = e.getY();
                
                ProxySubject s = mFocusedObject;
                if (s == null)
                {
                    // Clicking on whitespace!
                    
                    // If control is down, we may select multiple things...
                    if (!e.isControlDown())
                    {
                        previouslySelected.clear();
                        UnSelectCommand unselect =
                            new UnSelectCommand(ControlledSurface.this,
                            selectedObjects);
                        root.getUndoInterface().executeCommand(unselect);
                    }
                    
                    // If SELECT is active, this means that we're starting a drag-select...
                    // Start of drag-select?
                    dragSelect = true;
                }
                else
                {
                    // Clicking on something!
                    
                    // Should we unselect the currently selected objects?
                    if (!isSelected(s))
                    {
                        // If control is down, we may select multiple things...
                        if (!e.isControlDown())
                        {
                            UnSelectCommand unselect =
                                new UnSelectCommand(ControlledSurface.this,
                                selectedObjects);;
                                root.getUndoInterface().executeCommand(unselect);
                                
                        }
                        SelectCommand select =
                            new SelectCommand(ControlledSurface.this, s);
                        root.getUndoInterface().executeCommand(select);
                    }
                    else
                    {
                        if (e.isControlDown())
                        {
                            if (!hasSelectedLabel())
                            {
                                UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, s);
                                root.getUndoInterface().executeCommand(unselect);
                            }
                        }
                    }
                    
                    // Select stuff!
                    //selectChange(o);
                    
              /*
                if (o.getType() == EditorObject.EDGE)
                {
                EdgeSubject edge = (EdgeSubject) o;
                if (edge.wasClicked(e.getX(), e.getY()))
                {
                return;
                }
                }
               */
                    draggingInitial(s, e);
                    
                    draggingEdge(s, e);
                    
                    resizingGroupNode(s, e);
                    // Find offset values if a label or group was clicked
                    setOffset(s, e);
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
                ProxySubject s = getObjectAtPosition(e.getX(), e.getY());
                if (s != null)
                {
                    if (s instanceof SimpleNodeSubject)
                    {
                        SimpleNodeSubject n = (SimpleNodeSubject) s;
                        
                    }
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON3)
            {
                
            }
            
            repaint(false);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            if (hasDragged)
            {
                Command unselect = new UnSelectCommand(ControlledSurface.this, previouslySelected);
                Command select = new SelectCommand(ControlledSurface.this, selectedObjects);
                root.getUndoInterface().executeCommand(unselect);
                root.getUndoInterface().executeCommand(select);
            }
            if (mDummy != null || draggingSource || draggingTarget)
            {
                updateModel(e);
            }
            
            // This is for triggering the popup
            maybeShowPopup(e);
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                // Stop resizing nodegroup
                if (nodeGroupIsSelected() && (selectedObjects.size() == 1) && !newGroup)
                {
                    GroupNodeSubject ng = (GroupNodeSubject) selectedObjects.get(0);
                    mResize = null;
                    
                    if (ng.getGeometry().getRectangle().isEmpty())
                    {
                        Command c = new DeleteNodeGroupCommand(getGraph(), ng);
                        root.getUndoInterface().executeCommand(c);
                    }
                }
                if (newGroup && !getDragRectangle().isEmpty())
                {
                    mResize = null;
                    Command c = new CreateNodeGroupCommand(getGraph(), getDragRectangle());
                    newGroup = false;
                    root.getUndoInterface().executeCommand(c);
                }
                
                // Redefine edge if it has been changed
                if (edgeIsSelected() && (selectedObjects.size() == 1))
                {
                    ProxySubject s = selectedObjects.get(0);
                    EdgeSubject edge;
                    if (s instanceof EdgeSubject)
                    {
                        edge = (EdgeSubject) s;
                    }
                    else
                    {
                        edge = (EdgeSubject) selectedObjects.get(1);
                    }
                    //edge.setTPoint(edge.getTPointX(), edge.getTPointY());
                }
                
            }
            
            repaint(hasDragged);
            
            dragSelect = false;
            hasDragged = false;
        }
        
        public void mouseDragged(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
            //if (e.getButton() == MouseEvent.BUTTON1) // Why not?
            {
                setDrag(e);
                hasDragged = true;
                
                // DragSelect!
                if (dragSelect)
                {
                    toBeSelected = getDragSelection();
                    
                    // Select all that should be selected...
                    //System.out.println(previouslySelected.size());
                    UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);
                    unselect.execute();
                    // These have been selected previously and should still be
                    // selected no matter what
                    SelectCommand select = new SelectCommand(ControlledSurface.this, previouslySelected);
                    select.execute();
                    // These are in the current drag selection
                    select = new SelectCommand(ControlledSurface.this, toBeSelected);
                    select.execute();
                    
                    repaint(false);
                    
                    return;
                }
                
                // Multiple selection?
                
                // Drag all selected objects
                
                // is this the start of the move or a continuation of it
                if (!selectedObjects.isEmpty() && !draggingSource && !draggingTarget)
                {
                    if (mDummy == null)
                    {
                        mDummy = new EditorGraph(getGraph());
                        mDummyShape = new SubjectShapeProducer(mDummy, getModule());
                        mDummy.addModelObserver(ControlledSurface.this);
                    }
                }
                // Find the distances that the mouse has dragged (for moving and stuff)
                int dx = 0;
                int dy = 0;
                // Are we using snap?
                if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap
                    && !draggingInitial)
                {
                    lastX = findGrid(lastX);
                    lastY = findGrid(lastY);
                    
                    int currX = findGrid(e.getX());
                    int currY = findGrid(e.getY());
                    
                    // If the first selected node or nodegroup is not correctly
                    // aligned already, we need a modifier to get it on the
                    // grid again...
                    Point2D mod = getMod();
                    
                    dx = currX - lastX + (int) mod.getX();
                    dy = currY - lastY + (int) mod.getY();
                }
                else
                {
                    dx = e.getX() - lastX;
                    dy = e.getY() - lastY;
                }
                
                // Update position
                lastX += dx;
                lastY += dy;
                
                // No move?
                if ((dx == 0) && (dy == 0))
                {
                    return;
                }
        /* // Code for preventing nodes from ending up in the same place (but we allow that now)
           for (int i = 0; i < nodes.size(); i++)
           {
           if (((SimpleNodeSubject) nodes.get(i)).getPosition().distance(dx, dy) < selectedNode.getWidth() && ((SimpleNodeSubject) nodes.get(i)) != selectedNode)
           {
           return;
           }
           }
         
           for (int i = 0; i < nodeGroups.size(); i++)
           {
           if (((EditorNodeGroup) nodeGroups.get(i)).getBounds().intersects(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)) &&!((EditorNodeGroup) nodeGroups.get(i)).getBounds().contains(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)))
           {
           return;
           }
           }
         */
                
                // Move every selected object!
                if (!draggingSource && !draggingTarget)
                {
                    for (Iterator<ProxySubject> it = selectedObjects.iterator(); it.hasNext(); )
                    {
                        ProxySubject s = it.next();
                        if (mResize != null && s instanceof GroupNodeSubject)
                        {
                            resizeGroupNode((GroupNodeSubject) s, mResize.resize(e.getPoint()));
                        }
                        else
                        {
                            setObjectPosition(s, dx, dy);
                        }
                    }
                }
                
                examineCollisions();
                
            }
            
            repaint(false);
        }
        
        public void mouseMoved(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
        
    }
    
    private class NodeListener
        extends ToolController
    {
        public int getHighlightPriority(ProxySubject s)
        {
            if (s instanceof SimpleNodeSubject)
            {
                return 2;
            }
            else if (s instanceof LabelGeometrySubject)
            {
                return 1;
            }
            return -1;
        }
        
        public void mouseClicked(MouseEvent e)
        {
            requestFocusInWindow();
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                // Singleclick?
                if (e.getClickCount() == 1)
                {
                    // What was clicked?
                    //EditorObject o = getObjectAtPosition(e.getX(), e.getY());
                    
                    // Should we add a new node?
                    
                    /** Nonsense?
                  else if (T.getPlace() == EditorToolbar.EDGE)
                  {
                  if (o == null)
                  {
                  UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);;
               
                  return;
                  }
                     */
                }
                // Doubleclick?
                else if (e.getClickCount() == 2)
                {
                    // What was clicked?
                    ProxySubject s = getObjectAtPosition(e.getX(), e.getY());
                    if (s != null && s instanceof LabelGeometrySubject)
                    {
                        LabelGeometrySubject l = (LabelGeometrySubject) s;
                        JTextField text = new NameEditField(l);
                        ControlledSurface.this.add(text);
                        text.setVisible(true);
                        text.requestFocus();
                    }
                }
            }
            
            // Repaint is done when you release the mouse button?
            // (But that's before the click?)
            repaint();
        }
        
        public void mousePressed(MouseEvent e)
        {
            // This is for triggering the popup
            maybeShowPopup(e);
            setDrag(e);
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                lastX = e.getX();
                lastY = e.getY();
                
                ProxySubject s = mFocusedObject;
                if (s == null)
                {
                    // Clicking on whitespace!
                    
                    // If control is down, we may select multiple things...
                    if (!e.isControlDown())
                    {
                        UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);
                        root.getUndoInterface().executeCommand(unselect);
                        int posX;
                        int posY;
                        
                        if (nodesSnap)
                        {
                            posX = findGrid(e.getX());
                            posY = findGrid(e.getY());
                        }
                        else
                        {
                            posX = e.getX();
                            posY = e.getY();
                        }
                        
                        Command createNode = new CreateNodeCommand(getGraph(), posX, posY);
                        root.getUndoInterface().executeCommand(createNode);
                        //addLabel(getLastNode(), "", 0, break20);
                        
                        //SimpleNodeProxy np = new SimpleNodeProxy("s" + nodes.size());
                        //PointGeometryProxy gp = new PointGeometryProxy(posX,posY);
                        //np.setPointGeometry(gp);
                        //graph.getNodes().add(np);
                    }
                }
                else
                {
                    // Clicking on something!
                    
                    // Should we unselect the currently selected objects?
                    if (!isSelected(s))
                    {
                        // If control is down, we may select multiple things...
                        if (!e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                        SelectCommand select = new SelectCommand(ControlledSurface.this, s);
                        root.getUndoInterface().executeCommand(select);
                    }
                    else
                    {
                        if (e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, s);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                    }
                    
                    // Select stuff!
                    //selectChange(o);
                    draggingInitial(s, e);
                    // Find offset values if a label or group was clicked
                    setOffset(s, e);
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
                System.out.println("Button 2!");
            }
            else if (e.getButton() == MouseEvent.BUTTON3)
            {
            }
            
            repaint(false);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            if (mDummy != null)
            {
                updateModel(e);
            }
            
            // This is for triggering the popup
            maybeShowPopup(e);
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
            }
            
            repaint(hasDragged);
            
            dragSelect = false;
            hasDragged = false;
        }
        
        public void mouseDragged(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
            //if (e.getButton() == MouseEvent.BUTTON1) // Why not?
            {
                setDrag(e);
                hasDragged = true;
                
                if (!selectedObjects.isEmpty())
                {
                    if (mDummy == null)
                    {
                        mDummy = new EditorGraph(getGraph());
                        mDummyShape = new SubjectShapeProducer(mDummy, getModule());
                        mDummy.addModelObserver(ControlledSurface.this);
                    }
                }
                
                // Find the distances that the mouse has dragged (for moving and stuff)
                int dx = 0;
                int dy = 0;
                // Are we using snap?
                if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap
                    && !draggingInitial)
                {
                    lastX = findGrid(lastX);
                    lastY = findGrid(lastY);
                    
                    int currX = findGrid(e.getX());
                    int currY = findGrid(e.getY());
                    
                    // If the first selected node or nodegroup is not correctly
                    // aligned already, we need a modifier to get it on the
                    // grid again...
                    Point2D mod = getMod();
                    
                    dx = currX - lastX + (int) mod.getX();
                    dy = currY - lastY + (int) mod.getY();
                }
                else
                {
                    dx = e.getX() - lastX;
                    dy = e.getY() - lastY;
                }
                
                // Update position
                lastX += dx;
                lastY += dy;
                
                for (Iterator<ProxySubject> it = selectedObjects.iterator(); it.hasNext(); )
                {
                    ProxySubject s = it.next();
                    
                    setObjectPosition(s, dx, dy);
                }
            }
            
            repaint(false);
        }
        
        public void mouseMoved(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
    }
    
    private class EdgeListener
        extends ToolController
    {
        public int getHighlightPriority(ProxySubject s)
        {
            if (s instanceof SimpleNodeSubject)
            {
                return 4;
            }
            else if (s instanceof EdgeSubject)
            {
                if ((hasDragged && (nodeIsSelected() || nodeGroupIsSelected()))
                || draggingSource || draggingTarget)
                {
                    return -1;
                }
                return 2;
            }
            else if (s instanceof LabelBlockSubject)
            {
                if ((hasDragged && (nodeIsSelected() || nodeGroupIsSelected()))
                || draggingSource || draggingTarget)
                {
                    return -1;
                }
                return 1;
            }
            else if (s instanceof GroupNodeSubject)
            {
                return 3;
            }
            return -1;
        }
        
        public void mousePressed(MouseEvent e)
        {
            // This is for triggering the popup
            maybeShowPopup(e);
            setDrag(e);
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                lastX = e.getX();
                lastY = e.getY();
                
                ProxySubject s = mFocusedObject;
                if (s == null)
                {
                    if (!e.isControlDown())
                    {
                        UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);;
                        root.getUndoInterface().executeCommand(unselect);
                    }
                }
                else
                {
                    // Clicking on something!
                    
                    // Should we unselect the currently selected objects?
                    if (!isSelected(s))
                    {
                        // If control is down, we may select multiple things...
                        if (!e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                        SelectCommand select = new SelectCommand(ControlledSurface.this, s);
                        root.getUndoInterface().executeCommand(select);
                    }
                    else
                    {
                        if (e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, s);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                    }
                    
              /*
                if (o.getType() == EditorObject.EDGE)
                {
                EdgeSubject edge = (EdgeSubject) o;
                if (edge.wasClicked(e.getX(), e.getY()))
                {
                return;
                }
                }
               */
                    draggingEdge(s, e);
                    // Find offset values if a label or group was clicked
                    setOffset(s, e);
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
                System.out.println("Button 2!");
            }
            else if (e.getButton() == MouseEvent.BUTTON3)
            {
            }
            
            repaint(false);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            if (mDummy != null || draggingSource || draggingTarget)
            {
                updateModel(e);
            }
            
            // This is for triggering the popup
            maybeShowPopup(e);
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                // Draw an edge if EditorToolbar.EDGE is selected
                if (hasDragged && ((nodeIsSelected() || nodeGroupIsSelected())))
                {
                    while (selectedObjects.size() != 0)
                    {
                        // This is thus the startingpoint
                        ProxySubject s1 = selectedObjects.get(0);
                        UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, s1);
                        root.getUndoInterface().executeCommand(unselect);
                        
                        // This is the targetpoint
                        ProxySubject s2 = getObjectAtPosition(e.getX(), e.getY());
                        if (s2 instanceof SimpleNodeSubject)
                        {
                            SimpleNodeSubject n2 = (SimpleNodeSubject) s2;
                            Point2D p2 = n2.getPointGeometry().getPoint();
                            Point2D p1 = null;
                            // Add edge
                            if (s1 instanceof SimpleNodeSubject)
                            {
                                SimpleNodeSubject n = (SimpleNodeSubject) s1;
                                p1 = n.getPointGeometry().getPoint();
                            }
                            else if (s1 instanceof GroupNodeSubject)
                            {
                                GroupNodeSubject n = (GroupNodeSubject) s1;
                                Rectangle2D rect = n.getGeometry().getRectangle();
                                p1 = new Point((int)rect.getX() + xoff,
                                    (int)rect.getY() + yoff);
                            }
                            Command createEdge = new CreateEdgeCommand(getGraph(),
                                (NodeSubject)s1,
                                n2, p1, p2);
                            root.getUndoInterface().executeCommand(createEdge);
                        }
                    }
                    
                    UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);;
                    root.getUndoInterface().executeCommand(unselect);
                }
                
                
                if (edgeIsSelected() && (selectedObjects.size() == 1))
                {
                    ProxySubject obj = selectedObjects.get(0);
                    EdgeSubject edge;
                    if (obj instanceof EdgeSubject)
                    {
                        edge = (EdgeSubject) obj;
                    }
                    else
                    {
                        edge = (EdgeSubject) selectedObjects.get(1);
                    }
                    
                    NodeSubject n = (NodeSubject) getNodeOrNodeGroupAtPosition(e.getX(), e.getY());
                    
                    if (n != null)
                    {
                        if (draggingSource || (draggingTarget && n instanceof SimpleNodeSubject))
                        {
                            Command moveEdge = new MoveEdgeCommand(ControlledSurface.this,
                                edge, n,
                                draggingSource,
                                e.getX(), e.getY());
                            root.getUndoInterface().executeCommand(moveEdge);
                        }
                    }
                    
                    draggingSource = false;
                    draggingTarget = false;
                    //edge.setTPoint(edge.getTPointX(), edge.getTPointY());
                }
                
            }
            
            repaint(hasDragged);
            
            dragSelect = false;
            hasDragged = false;
        }
        
        public void mouseDragged(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
            //if (e.getButton() == MouseEvent.BUTTON1) // Why not?
            {
                setDrag(e);
                hasDragged = true;
                
                // Find the distances that the mouse has dragged (for moving and stuff)
                int dx = 0;
                int dy = 0;
                // Are we using snap?
                dx = e.getX() - lastX;
                dy = e.getY() - lastY;
                
                // Update position
                lastX += dx;
                lastY += dy;
                
                // Single selection! (Multiple selection is only allowed in SELECT-mode.)
                
                // Edge drawing...
                // No move?
                if ((dx == 0) && (dy == 0))
                {
                    return;
                }
                // is this the start of the move or a continuation of it
                if (!selectedObjects.isEmpty() && !draggingSource && !draggingTarget)
                {
                    for (final ProxySubject object : selectedObjects)
                    {
                        if (object instanceof EdgeSubject)
                        {
                            EdgeSubject edge = (EdgeSubject) object;
                            if (mDummy == null)
                            {
                                mDummy = new EditorGraph(getGraph());
                                mDummyShape = new SubjectShapeProducer(mDummy, getModule());
                                mDummy.addModelObserver(ControlledSurface.this);
                            }
                            setEdgePosition(edge, dx, dy);
                        }
                    }
                }
            }
            
            repaint(false);
        }
        
        public void mouseMoved(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
    }
    
    private class GroupNodeListener
        extends ToolController
    {
        public int getHighlightPriority(ProxySubject s)
        {
            if (s instanceof GroupNodeSubject)
            {
                return 1;
            }
            return -1;
        }
        
        public void mousePressed(MouseEvent e)
        {
            // This is for triggering the popup
            maybeShowPopup(e);
            setDrag(e);
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                lastX = e.getX();
                lastY = e.getY();
                
                ProxySubject o = mFocusedObject;
                if (o == null)
                {
                    
                    // If control is down, we may select multiple things...
                    if (!e.isControlDown())
                    {
                        UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);;
                        root.getUndoInterface().executeCommand(unselect);
                    }
                    
                    // If NODEGROUP is active, we're adding a new group
                    // GroupNodeSubject nodeGroup;
                    newGroup = true;
                }
                else
                {
                    // Clicking on something!
                    
                    // Should we unselect the currently selected objects?
                    if (!isSelected(o))
                    {
                        // If control is down, we may select multiple things...
                        if (!e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, selectedObjects);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                        SelectCommand select = new SelectCommand(ControlledSurface.this, o);
                        root.getUndoInterface().executeCommand(select);
                    }
                    else
                    {
                        if (e.isControlDown())
                        {
                            UnSelectCommand unselect = new UnSelectCommand(ControlledSurface.this, o);
                            root.getUndoInterface().executeCommand(unselect);
                        }
                    }
                    resizingGroupNode(o, e);
                    
                    setOffset(o, e);
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON2)
            {
                System.out.println("Button 2!");
            }
            else if (e.getButton() == MouseEvent.BUTTON3)
            {
            }
            
            repaint(false);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            if (mDummy != null)
            {
                updateModel(e);
            }
            
            // This is for triggering the popup
            maybeShowPopup(e);
            
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                // Stop resizing nodegroup
                if (nodeGroupIsSelected() && (selectedObjects.size() == 1) && !newGroup)
                {
                    GroupNodeSubject ng = (GroupNodeSubject) selectedObjects.get(0);
                    if (ng.getGeometry().getRectangle().isEmpty())
                    {
                        Command c = new DeleteNodeGroupCommand(getGraph(), ng);
                        root.getUndoInterface().executeCommand(c);
                    }
                }
                if (newGroup && !getDragRectangle().isEmpty())
                {
                    Command c = new CreateNodeGroupCommand(getGraph(),
                        getDragRectangle());
                    root.getUndoInterface().executeCommand(c);
                }
                newGroup = false;
            }
            
            repaint(hasDragged);
            
            dragSelect = false;
            hasDragged = false;
        }
        
        public void mouseDragged(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
            if (nodeGroupIsSelected())
            {
                if (mDummy == null)
                {
                    mDummy = new EditorGraph(getGraph());
                    mDummyShape = new SubjectShapeProducer(mDummy, getModule());
                    mDummy.addModelObserver(ControlledSurface.this);
                }
            }
            //if (e.getButton() == MouseEvent.BUTTON1) // Why not?
            {
                setDrag(e);
                hasDragged = true;
                // Find the distances that the mouse has dragged (for moving and stuff)
                int dx = 0;
                int dy = 0;
                // Are we using snap?
                if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap)
                {
                    lastX = findGrid(lastX);
                    lastY = findGrid(lastY);
                    
                    int currX = findGrid(e.getX());
                    int currY = findGrid(e.getY());
                    
                    // If the first selected node or nodegroup is not correctly
                    // aligned already, we need a modifier to get it on the
                    // grid again...
                    Point2D mod = getMod();
                    
                    dx = currX - lastX + (int) mod.getX();
                    dy = currY - lastY + (int) mod.getY();
                }
                else
                {
                    dx = e.getX() - lastX;
                    dy = e.getY() - lastY;
                }
                
                // Update position
                lastX += dx;
                lastY += dy;
                
                // DragSelect!
                
                // Single selection! (Multiple selection is only allowed in SELECT-mode.)
                
                // Are we resizing a nodegroup?
                if (nodeGroupIsSelected())
                {
                    GroupNodeSubject nodeGroup = (GroupNodeSubject) selectedObjects.get(0);
                    
                    //Rectangle2D.Double b = new Rectangle2D.Double();
                    //b.setRect(nodeGroup.getBounds());
                    if (mResize != null)
                    {
                        resizeGroupNode(nodeGroup, mResize.resize(e.getPoint()));
                    }
                    else
                    {
                        setObjectPosition(nodeGroup, dx, dy);
                    }
                    
                    
            /* // Prevent nodegroups from moving onto nodes?
               if (intersectsRectangle(nodeGroup.getBounds()))
               {
               nodeGroup.setBounds(b);
               }
             */
                    
                    examineCollisions();
                }
            }
            
            repaint(false);
        }
        
        public void mouseMoved(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
    }
    
    private class InitialListener
        extends ToolController
    {
        public int getHighlightPriority(ProxySubject s)
        {
            if (s instanceof SimpleNodeSubject)
            {
                return 1;
            }
            return -1;
        }
        
        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
            setDrag(e);
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                ProxySubject o = mFocusedObject;
                if (o instanceof SimpleNodeSubject)
                {
                  SimpleNodeSubject n = (SimpleNodeSubject) o;
                  Command initial = new SetNodeInitialCommand(getGraph(), n);
                  root.getUndoInterface().executeCommand(initial);
                  repaint();
                }
            }
        }
        
        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }
        
        public void mouseDragged(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
        
        public void mouseMoved(MouseEvent e)
        {
            updateHighlighting(e.getPoint());
        }
    }
    
    private abstract class ToolController
        extends MouseAdapter
        implements MouseMotionListener
    {
        public abstract int getHighlightPriority(ProxySubject s);
        
        public void mouseMoved(MouseEvent e)
        {
         
          updateHighlighting(e.getPoint());
        }
    }
  
	
  private class KeySpy
    extends KeyAdapter
  {	

        public void keyPressed(KeyEvent e)
        {
            //System.err.println(e.getKeyCode());
            if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (e.getKeyCode() == KeyEvent.VK_DELETE))
            {
                deleteSelected();
            }
            // to be reimplemented
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP)
            {
                //System.err.println("UP");
                boolean hasMoved = false;
                CompoundCommand upMove = new CompoundCommand("Move Event");
                for (ProxySubject o : selectedObjects)
                {
                    if (o instanceof LabelBlockSubject)
                    {
                        LabelBlockSubject l = (LabelBlockSubject)o;
                        if (hasSelected(l))
                        {
                            List<AbstractSubject> labels =
                                new ArrayList(l.getEventListModifiable());
                            labels.retainAll(selectedObjects);
                            //System.err.println(labels);
                            int index = l.getEventList().size();
                            for (AbstractSubject i : labels)
                            {
                                int index2 = l.getEventList().indexOf(i);
                                if (index2 < index)
                                {
                                    index = index2;
                                }
                            }
                            if (index > 0)
                            {
                                index--;
                            }
                            hasMoved = true;
                            Command c =
                                new ReorganizeListCommand(l, labels, index);
                            upMove.addCommand(c);
                        }
                    }
                }
                upMove.end();
                if (hasMoved)
                {
                    e.consume();
                    root.getUndoInterface().executeCommand(upMove);
                }
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN)
            {
                //System.err.println("Down");
                boolean hasMoved = false;
                CompoundCommand downMove = new CompoundCommand("Move Event");
                for (ProxySubject o : selectedObjects)
                {
                    if (o instanceof LabelBlockSubject)
                    {
                        LabelBlockSubject l = (LabelBlockSubject)o;
                        if (hasSelected(l))
                        {
                            List<AbstractSubject> labels =
                                new ArrayList(l.getEventListModifiable());
                            labels.retainAll(selectedObjects);
                            //System.err.println(labels);
                            int index = 0;
                            for (AbstractSubject i : labels)
                            {
                                int index2 = l.getEventList().indexOf(i);
                                if (index2 > index)
                                {
                                    index = index2;
                                }
                            }
                            if (index < l.getEventList().size() - 1)
                            {
                                index = index + 2 - labels.size();
                            }
                            
                            hasMoved = true;
                            Command c =
                                new ReorganizeListCommand(l, labels, index);
                            downMove.addCommand(c);
                        }
                    }
                }
                downMove.end();
                if (hasMoved)
                {
                    e.consume();
                    root.getUndoInterface().executeCommand(downMove);
                }
            }
        }
    }
    
    //	public Command upCommand()
    //	{
    //		int index = getSubject().getEventList().size();
    //		for (IdentifierSubject i : mSelectedLabels)
    //		{
    //			int index2 = getSubject().getEventList().indexOf(i);
    //			if (index2 < index)
    //			{
    //				index = index2;
    //			}
    //		}
    //		if (index > 0)
    //		{
    //			index--;
    //		}
    //		return new ReorganizeListCommand(getSubject(), mSelectedLabels, index);
    //	}
    //
    //	public Command downCommand()
    //	{
    //		int index = 0;
    //		for (IdentifierSubject i : mSelectedLabels)
    //		{
    //			int index2 = getSubject().getEventList().indexOf(i);
    //			if (index2 > index)
    //			{
    //				index = index2;
    //			}
    //		}
    //		if (index < getSubject().getEventList().size() - 1)
    //		{
    //			index = index + 2 - mSelectedLabels.size();
    //		}
    //		return new ReorganizeListCommand(getSubject(), mSelectedLabels, index);
    //	}
    
    private class NameEditField
        extends JTextField
    {
        private final LabelGeometrySubject mLabel;
        private final SimpleNodeSubject mNode;
        
        public NameEditField(LabelGeometrySubject label)
        {
            mLabel = label;
            mNode = (SimpleNodeSubject)label.getParent();
            Point p = new Point((int)label.getOffset().getX(),
                (int)label.getOffset().getY());
            p.translate((int)mNode.getPointGeometry().getPoint().getX(),
                (int)mNode.getPointGeometry().getPoint().getY());
            setText(mNode.getName());
            setLocation(p);
            setSize(getPreferredSize());
            setBorder(new EmptyBorder(getBorder().getBorderInsets(this)));
            setOpaque(false);
            addFocusListener(new NameEditSpy());
            addActionListener(new NameEditSpy());
            getDocument().addDocumentListener(new DocumentListener()
            {
                public void changedUpdate(final DocumentEvent event)
                {
                    setSize(getPreferredSize());
                }
                public void insertUpdate(final DocumentEvent event)
                {
                    setSize(getPreferredSize());
                }
                public void removeUpdate(final DocumentEvent event)
                {
                    setSize(getPreferredSize());
                }
            });
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
            getActionMap().put("enter", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    reName();
                }
            });
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
            getActionMap().put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setText(mNode.getName());
                    reName();
                }
            });
            mDontDraw.add(mLabel);
        }
        
        private void reName()
        {
            if (!getText().equals(mNode.getName()))
            {
                if (!getText().equals("") &&
                    !getGraph().getNodesModifiable().containsName(getText()))
                {
                    Command u = new ChangeNameCommand(mNode.getName(),
                        getText(),
                        mNode);
                    u.execute();
                    ControlledSurface.this.remove(NameEditField.this);
                    mDontDraw.remove(mLabel);
                    root.getUndoInterface().addUndoable(new UndoableCommand(u));
                }
                else
                {
                    JOptionPane.showMessageDialog(ControlledSurface.this,
                        "Each node must have a unique name");
                    selectAll();
                    setVisible(true);
                    requestFocus();
                }
            }
            else
            {
                ControlledSurface.this.remove(NameEditField.this);
                mDontDraw.remove(mLabel);
            }
        }
        
        private class NameEditSpy
            extends FocusAdapter
            implements ActionListener
        {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("action performed" + e.getActionCommand());
        /*	if (getText() != mLabel.getParent().getName())
                {
                try
                {
                Command u = new ChangeNameCommand(mLabel.getParent().getName(),
                getText(),
                mLabel.getParent().getSubject());
                u.execute();
                ControlledSurface.this.remove(NameEditField.this);
                mLabel.setEditing(false);
                root.getUndoInterface().addUndoable(new UndoableCommand(u));
                }
                catch (final DuplicateNameException d)
                {
                setVisible(false);
                ControlledSurface.this.remove(NameEditField.this);
                JOptionPane.showMessageDialog(ControlledSurface.this
                , d.getMessage());
                JTextField text = new NameEditField(mLabel);
                ControlledSurface.this.add(text);
                text.setText(getText());
                text.selectAll();
                text.setVisible(true);
                text.requestFocus();
                }
                }*/
            }
            
            public void focusLost(FocusEvent e)
            {
                reName();
            }
        }
    }
    
    /**
     *  Just keeps all the logic for resizing a new node group here so
     *  the rest of controlled surface doesn't get more cluttered than it already
     *  is
     */
    private class GroupResizer
    {
        private final Point corner;
        private final int XCoord;
        private final int YCoord;
        
        public GroupResizer(Rectangle2D rect, Handle.HandleType type)
        {
            switch (type)
            {
                case NW:
                    corner = new Point((int)rect.getMaxX(), (int)rect.getMaxY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = Integer.MIN_VALUE;
                    break;
                case N:
                    corner = new Point((int)rect.getMaxX(), (int)rect.getMaxY());
                    XCoord = (int)rect.getMinX();
                    YCoord = Integer.MIN_VALUE;
                    break;
                case NE:
                    corner = new Point((int)rect.getMinX(), (int)rect.getMaxY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = Integer.MIN_VALUE;
                    break;
                case W:
                    corner = new Point((int)rect.getMaxX(), (int)rect.getMinY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = (int)rect.getMaxY();
                    break;
                case E:
                    corner = new Point((int)rect.getMinX(), (int)rect.getMinY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = (int)rect.getMaxY();
                    break;
                case SW:
                    corner = new Point((int)rect.getMaxX(), (int)rect.getMinY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = Integer.MIN_VALUE;
                    break;
                case S:
                    corner = new Point((int)rect.getMaxX(), (int)rect.getMinY());
                    XCoord = (int)rect.getMinX();
                    YCoord = Integer.MIN_VALUE;
                    break;
                case SE:
                    corner = new Point((int)rect.getMinX(), (int)rect.getMinY());
                    XCoord = Integer.MIN_VALUE;
                    YCoord = Integer.MIN_VALUE;
                    break;
                default:
                    assert(false);
                    corner = null;
                    XCoord = Integer.MIN_VALUE;
                    YCoord = Integer.MIN_VALUE;
            }
        }
        
        public Rectangle2D resize(Point2D p)
        {
            if (nodesSnap)
            {
                p = findGridPoint(p);
            }
            Rectangle2D rect = new Rectangle();
            if (XCoord != Integer.MIN_VALUE)
            {
                p.setLocation((double)XCoord, p.getY());
            }
            if (YCoord != Integer.MIN_VALUE)
            {
                p.setLocation(p.getX(), (double)YCoord);
            }
            rect.setFrameFromDiagonal(p, corner);
            return rect;
        }
    }
    
    
    //#########################################################################
    //# Data Members
    private ControlledToolbar mToolbar;
    private EditorOptions options;
    private SimpleNodeSubject sNode = null;
    private int lastX = 0;
    private int lastY = 0;
    private int xoff = 0;
    private int yoff = 0;
    private GroupResizer mResize = null;
    private boolean controlPointsMove = true;
    private boolean nodesSnap = true;
    private boolean hasDragged = false;
    private boolean draggingSource = false;
    private boolean draggingTarget = false;
    private boolean draggingInitial = false;
    
    /** List of currently selected EditorObject:s. */
    private List<ProxySubject> selectedObjects =
        new LinkedList<ProxySubject>();
    /** List of EditorObject:s that are to become selected. */
    private List<ProxySubject> toBeSelected = new LinkedList<ProxySubject>();
    private List<ProxySubject> previouslySelected =
        new ArrayList<ProxySubject>();
    private Set<ProxySubject> mDontDraw = new HashSet<ProxySubject>();
    private final Set<ProxySubject> mError = new HashSet<ProxySubject>();
    
    private Line2D mLine = null;
    
    /** The currently highlighted EditorObject (under the mouse pointer). */
    private ProxySubject mFocusedObject = null;
    
    private EditorGraph mDummy = null;
    
    private SubjectShapeProducer mDummyShape = null;
    
    private ToolController mController;
    
    private DRAGOVERSTATUS mDragOver = DRAGOVERSTATUS.NOTDRAG;
    
    private DropTarget dropTarget;
    private DropTargetListener dtListener;
    
    private boolean newGroup = false;
    
    
    //#########################################################################
    //# Class Constants
    public enum Tool
    {
        SELECT,
        NODE,
        NODEGROUP,
        INITIAL,
        EDGE,
        EVENT;
    }
    
    public static boolean mDND = false;
    /** is not being draggedOver*/
    public static final int NOTDRAG = 0;
    /** is being draggedOver and can drop data*/
    public static final int CANDROP = 1;
    /** is being draggedOver but can't drop data*/
    public static final int CANTDROP = 2;
    
    private static final DataFlavor FLAVOUR =
        new DataFlavor(IdentifierWithKind.class, "IdentifierWithKind");
}
