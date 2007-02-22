//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id: ControlledSurface.java,v 1.125 2007-02-22 06:37:42 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.waters.gui.EditorSurface.DRAGOVERSTATUS;
import net.sourceforge.waters.gui.command.*;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;
import net.sourceforge.waters.gui.renderer.GeneralShape;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderObserver;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.*;

import org.supremica.properties.Config;
import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Clipboard;


public class ControlledSurface
  extends EditorSurface
  implements Observer, ModelObserver, EmbedderObserver, ClipboardOwner
{
  //#########################################################################
  //# Constructors
  public ControlledSurface(final GraphSubject graph,
                           final ModuleSubject module,
                           final EditorWindowInterface root,
                           final ControlledToolbar toolbar)
    throws GeometryAbsentException
  {
    super(graph, module, new SubjectShapeProducer(graph, module));
    mRoot = root;
    mToolbar = toolbar;
    if (root != null && toolbar != null) {
      toolbar.attach(this);
    }
    setFocusable(true);
    final DropTargetListener dtListener = new DTListener();
    final DropTarget dropTarget = new DropTarget(this, dtListener);
    mExternalDragSource = DragSource.getDefaultDragSource();
    mDGListener = new DGListener();
    final DragSourceListener dsListener = new DSListener();
    mExternalDragSource.addDragSourceListener(dsListener);
    addKeyListener(new KeySpy());
    final SpringEmbedder embedder = new SpringEmbedder(graph);
    final boolean runEmbedder = embedder.setUpGeometry();
    graph.addModelObserver(this);
    updateTool();
    if (runEmbedder) {
      runEmbedder();
    }
  }

  /**
   * Creates an immutable controlled surface.
   */
  public ControlledSurface(final GraphSubject graph,
                           final ModuleSubject module)
    throws GeometryAbsentException
  {
    this(graph, module, null, null);
  }
  
  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    
  }


  //#########################################################################
  //# Simple Access
  public EditorWindowInterface getEditorInterface()
  {
    return mRoot;
  }

  public ModuleSubject getModule()
  {
    return (ModuleSubject) super.getModule();
  }

  public void setOptionsVisible(boolean v)
  {
    mOptions.setVisible(v);
  }

  public GraphSubject getGraph()
  {
    return (GraphSubject) super.getGraph();
  }

  public GraphProxy getDrawnGraph()
  {
    if (mSecondaryGraph != null) {
      return mSecondaryGraph;
    } else {
      return getGraph();
    }
  }

  public SubjectShapeProducer getShapeProducer()
  {
    if (mSecondaryGraph != null) {
      assert(mSecondaryShapeProducer != null);
      return mSecondaryShapeProducer;
    } else {
      return (SubjectShapeProducer) super.getShapeProducer();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event instanceof ToolbarChangedEvent && mEmbedder == null) {
      updateTool();
    }
  }


  //#########################################################################
  //# Interfacenet.sourceforge.waters.gui.springembedder.EmbedderObserver
  public void embedderChanged(final EmbedderEvent event)
  {
    switch (event.getType()) {
    case EMBEDDER_START:
      if (mEmbedderController == null) {
        mEmbedderController = new EmbedderController();
      }
      updateController(mEmbedderController);
      // *** BUG ***
      // Must disable menus and keyboard also!
      // ***
      break;
    case EMBEDDER_STOP:
      mEmbedder.removeObserver(this);
      mEmbedder = null;
      commitSecondaryGraph();
      clearSecondaryGraph();
      updateTool();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    if (mInternalDragAction == null) {
      final Rectangle area = getDrawnAreaBounds();
      final int extra = Config.GUI_EDITOR_GRID_SIZE.get() * 10;
      final int x = area.width + extra;
      final int y = area.height + extra;
      final Dimension dim = new Dimension(x, y);
      if (!dim.equals(getPreferredSize())) {
        setPreferredSize(dim);
        revalidate();
      }
    }
    updateError();
    mController.updateHighlighting();
    repaint();
  }


  //#########################################################################
  //# Toolbar
  private void updateTool()
  {
    switch (mToolbar.getTool()) {
    case SELECT:
      if (mSelectController == null) {
        mSelectController = new SelectController();
      }
      updateController(mSelectController);
      break;
    case NODE:
      if (mNodeController == null) {
        mNodeController = new NodeController();
      }
      updateController(mNodeController);
      break;
    case GROUPNODE:
      if (mGroupNodeController == null) {
        mGroupNodeController = new GroupNodeController();
      }
      updateController(mGroupNodeController);
      break;
    case EDGE:
      if (mEdgeController == null) {
        mEdgeController = new EdgeController();
      }
      updateController(mEdgeController);
      break;
    default:
      throw new IllegalStateException
        ("Unknown tool: " + mToolbar.getTool() + "!");
    }
  }

  private void updateController(final ToolController controller)
  {
    if (mController != controller) {
      for (final MouseListener listener : getMouseListeners()) {
        removeMouseListener(listener);
      }
      for (final MouseMotionListener listener : getMouseMotionListeners()) {
        removeMouseMotionListener(listener);
      }
      mController = controller;
      addMouseListener(mController);
      addMouseMotionListener(mController);
      mExternalDragSource.createDefaultDragGestureRecognizer
        (this, mExternalDragAction, mDGListener);
      mController.installed();
    }
  }


  //#########################################################################
  //# Smart Creation Commands
  /**
   * Creates an edge and selects it.
   * This method creates and executes a compound command consisting of
   * an edge creation and a selection command.
   * @param  source   the source node of the new edge.
   * @param  target   the target node of the new edge.
   * @param  start    the start point to be used,
   *                  or <CODE>null</CODE> for simple source nodes.
   * @param  end      the end point to be used,
   *                  or <CODE>null</CODE> for simple target nodes.
   */
  void doCreateEdge(final NodeSubject source,
                    final NodeSubject target,
                    final Point2D start,
                    final Point2D end)
  {
    final CompoundCommand compound = new CompoundCommand("Edge Creation");
    if (!mSelectedObjects.isEmpty()) {
      final Command unselect = new UnSelectCommand(this, mSelectedObjects);
      compound.addCommand(unselect);
    }
    final CreateEdgeCommand create =
      new CreateEdgeCommand(getGraph(), source, target, start, end);
    final Command select = new SelectCommand(this, create.getCreatedEdge());
    compound.addCommand(create);
    compound.addCommand(select);
    compound.end();
    mRoot.getUndoInterface().executeCommand(compound);
  }

  /**
   * Changes the start or end of an edge using a move edge command.
   */
  void doMoveEdge(final EdgeSubject edge,
                  final NodeSubject node,
                  final boolean isSource,
                  final Point2D anchor)
  {
    final Command move =
      new MoveEdgeCommand(this, edge, node, isSource, anchor);
    mRoot.getUndoInterface().executeCommand(move);
  }

  /**
   * Creates a simple node and selects it.
   * This method creates and executes a compound command consisting of
   * an simple node creation and a selection command.
   * @param  x        the x coordinate of the new node.
   * @param  y        the y coordinate of the new node.
   */
  void doCreateSimpleNode(final Point2D pos)
  {
    final CompoundCommand compound = new CompoundCommand("Node Creation");
    if (!mSelectedObjects.isEmpty()) {
      final Command unselect = new UnSelectCommand(this, mSelectedObjects);
      compound.addCommand(unselect);
    }
    final CreateNodeCommand create = new CreateNodeCommand(getGraph(), pos);
    final Command select = new SelectCommand(this, create.getCreatedNode());
    compound.addCommand(create);
    compound.addCommand(select);
    compound.end();
    mRoot.getUndoInterface().executeCommand(compound);
  }

  /**
   * Creates a group node and selects it.
   * This method creates and executes a compound command consisting of
   * an group node creation and a selection command.
   * @param  rect     the geometry of the new group node.
   */
  void doCreateGroupNode(final Rectangle2D rect)
  {
    final CompoundCommand compound =
      new CompoundCommand("Group Node Creation");
    if (!mSelectedObjects.isEmpty()) {
      final Command unselect = new UnSelectCommand(this, mSelectedObjects);
      compound.addCommand(unselect);
    }
    final CreateNodeGroupCommand create =
      new CreateNodeGroupCommand(getGraph(), rect);
    final Command select =
      new SelectCommand(this, create.getCreatedGroupNode());
    compound.addCommand(create);
    compound.addCommand(select);
    compound.end();
    mRoot.getUndoInterface().executeCommand(compound);
  }

  /**
   * Adds a set of event labels to a label block and selects them.
   * This method creates and executes a compound command consisting of
   * an event addition and a selection command. Labels to be added as
   * propositions to nodes are handled differently and correctly.
   * @param  elist        the label block to be modified.
   * @param  identifiers  the event labels to be added. They are cloned
   *                      before insertion.
   * @param  pos          the position in the list where the new labels
   *                      are to be added. Insertion occurs before this
   *                      position.
   */
  void doAddToEventList
    (final EventListExpressionSubject elist,
     final Collection<IdentifierSubject> identifiers,
     final int pos)
  {
    final AddEventCommand add = new AddEventCommand(elist, identifiers, pos);
    final Subject parent = elist.getParent();
    if (parent instanceof NodeSubject) {
      final NodeSubject node = (NodeSubject) parent;
      doReplaceSelection(node);
      mRoot.getUndoInterface().executeCommand(add);
    } else {
      doReplaceSelection(elist);
      final CompoundCommand compound = new CompoundCommand("Event Addition");
      final Collection<IdentifierSubject> added = add.getAddedIdentifiers();
      final Command select = new SelectCommand(this, added);
      compound.addCommand(add);
      compound.addCommand(select);
      compound.end();
      mRoot.getUndoInterface().executeCommand(compound);
    }
  }

  /**
   * Deletes all selected objects using a compound command consisting
   * of an unselect and several delete commands.
   */
  public void doDeleteSelected()
  {
    final Collection<ProxySubject> victims = new LinkedList<ProxySubject>();
    final List<Command> commands = new LinkedList<Command>();

    // Are there any selected event labels?
    for (final ProxySubject item : mSelectedObjects) {
      if (item.getParent().getParent() instanceof EventListExpressionSubject) {
        victims.add(item);
      }
    }
    if (!victims.isEmpty()) {
      doRemoveFromSelection(victims);
      final CompoundCommand compound = new CompoundCommand("Label Deletion");
      for (final ProxySubject victim : victims) {
        final AbstractSubject item = (AbstractSubject) victim;
        final EventListExpressionSubject parent =
          (EventListExpressionSubject) item.getParent().getParent();
        final Command remove = new RemoveEventCommand(parent, item);
        commands.add(remove);
      }
    } else {
      // Not deleting event labels: then delete everything except node labels
      // First find any edges to be deleted ...
      for (final EdgeSubject edge : getGraph().getEdgesModifiable()) {
        final boolean selected = isSelected(edge);
        if (selected) {
          victims.add(edge);
        }
        if (selected ||
            isSelected(edge.getSource()) ||
            isSelected(edge.getTarget())) {
          final Command remove = new DeleteEdgeCommand(getGraph(), edge);
          commands.add(remove);
          final LabelBlockSubject block = edge.getLabelBlock();
          if (isSelected(block)) {
            victims.add(block);
          }
          final GuardActionBlockSubject ga = edge.getGuardActionBlock();
          if (isSelected(ga)) {
            victims.add(ga);
          }
        }
      }
      // Now for the other stuff ...
      for (final ProxySubject victim : mSelectedObjects) {
        if (victim instanceof SimpleNodeSubject) {
          victims.add(victim);
          final SimpleNodeSubject node = (SimpleNodeSubject) victim;
          final Command remove = new DeleteNodeCommand(getGraph(), node);
          commands.add(remove);
          final LabelGeometrySubject label = node.getLabelGeometry();
          if (isSelected(label)) {
            victims.add(label);
          }
        } else if (victim instanceof GroupNodeSubject) {
          victims.add(victim);
          final GroupNodeSubject group = (GroupNodeSubject) victim;
          final Command remove = new DeleteNodeGroupCommand(getGraph(), group);
          commands.add(remove);
        } else if (victim instanceof LabelBlockSubject &&
                   !victims.contains(victim)) {
          victims.add(victim);
          final LabelBlockSubject block = (LabelBlockSubject) victim;
          for (final ProxySubject child : block.getEventListModifiable()) {
            final AbstractSubject item = (AbstractSubject) child;
            final Command remove = new RemoveEventCommand(block, item);
            commands.add(remove);
          }
        }
        // *** BUG ***
        // What about guard/action blocks?
        // ***
      }
    }
    if (!victims.isEmpty()) {
      final CompoundCommand compound = new CompoundCommand("Deletion");
      final UnSelectCommand unselect = new UnSelectCommand(this, victims);
      compound.addCommand(unselect);
      for (final Command command : commands) {
        compound.addCommand(command);
      }
      compound.end();
      mRoot.getUndoInterface().executeCommand(compound);
    }
  }

  public void runEmbedder()
  {
    createSecondaryGraph();
    final SimpleComponentSubject comp =
      (SimpleComponentSubject) getGraph().getParent();
    final String name = comp == null ? "graph" : comp.getName();
    final long timeout = Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.get();
    mEmbedder = new SpringEmbedder(mSecondaryGraph.getNodeSubjects(),
                                   mSecondaryGraph.getEdgeSubjects());
    mEmbedder.addObserver(this);
    final Thread thread = new Thread(mEmbedder);
    final JDialog dialog =
      new SpringAbortDialog(mRoot.getFrame(), name, mEmbedder, timeout);
    dialog.setLocationRelativeTo(mRoot.getFrame());
    dialog.setVisible(true);
    thread.start();
  }


  //#########################################################################
  //# High-level Selection Handling
  /**
   * Clears the selection.
   * This method clears the selection using an unselect command.
   */
  private void doClearSelection()
  {
    if (!mSelectedObjects.isEmpty()) {
      final UnSelectCommand unselect =
        new UnSelectCommand(this, mSelectedObjects);
      mRoot.getUndoInterface().executeCommand(unselect);
    }
  }

  /**
   * Adds an item to the selection.
   * This method adds the given item to the selection using a select command. 
   */
  private void doAddToSelection(final ProxySubject item)
  {
    if (!isSelected(item)) {
      final SelectCommand select = new SelectCommand(this, item);
      mRoot.getUndoInterface().executeCommand(select);
    }
  }

  /**
   * Adds several items to the selection.
   * This method adds the items in the given collection to the selection
   * using a select command. 
   */
  private void doAddToSelection(final Collection<? extends ProxySubject> items)
  {
    if (!items.isEmpty()) {
      final SelectCommand select = new SelectCommand(this, items);
      mRoot.getUndoInterface().executeCommand(select);
    }
  }

  /**
   * Removes an item from the selection.
   * This method removes the items in the given collection from the
   * selection using an unselect command. 
   */
  private void doRemoveFromSelection(final ProxySubject item)
  {
    if (isSelected(item)) {
      final UnSelectCommand unselect = new UnSelectCommand(this, item);
      mRoot.getUndoInterface().executeCommand(unselect);
    }
  }

  /**
   * Removes several items from the selection.
   * This method removes the given item from the selection using an
   * unselect command. 
   */
  private void doRemoveFromSelection
    (final Collection<? extends ProxySubject> items)
  {
    if (!items.isEmpty()) {
      final UnSelectCommand unselect = new UnSelectCommand(this, items);
      mRoot.getUndoInterface().executeCommand(unselect);
    }
  }

  /**
   * Replaces the selection.
   * This method ensures that only the given item is selected,
   * by first clearing the selection and and then adding the item to
   * the selection using unselect and select commands. 
   */
  private void doReplaceSelection(final ProxySubject item)
  {
    if (isSelected(item)) {
      if (mSelectedObjects.size() > 1) {
        final List<ProxySubject> victims = new LinkedList<ProxySubject>();
        for (final ProxySubject sel : mSelectedObjects) {
          if (sel != item) {
            victims.add(sel);
          }
        }
        doRemoveFromSelection(victims);
      }
    } else {
      doClearSelection();
      doAddToSelection(item);
    }
  }

  /**
   * Replaces the selection.
   * This method ensures that only the given items are selected,
   * by first clearing the selection and and then adding the items to
   * the selection using unselect and select commands. 
   */
  private void doReplaceSelection
    (final Collection<? extends ProxySubject> items)
  {
    doClearSelection();
    doAddToSelection(items);
  }

  private void doReplaceLabelSelection(final ProxySubject item)
  {
    if (item.getParent().getParent() instanceof EventListExpressionSubject) {
      final LabelBlockSubject block =
        (LabelBlockSubject) item.getParent().getParent();
      final List<ProxySubject> victims = new LinkedList<ProxySubject>();
      for (final ProxySubject label : block.getEventListModifiable()) {
        if (label != item && isSelected(label)) {
          victims.add(label);
        }
      }
      doRemoveFromSelection(victims);
      doAddToSelection(item);
    } else {
      doReplaceSelection(item);
    }
  }

  /**
   * Toggles the selection of an item.
   * This method selects the given item if it is not yet selected,
   * and unselects it if selected, using a select or unselect command. 
   */
  private void doToggleSelection(final ProxySubject item)
  {
    if (isSelected(item)) {
      doRemoveFromSelection(item);
    } else {
      doAddToSelection(item);
    }
  }


  //#########################################################################
  //# Low-level Selection Handling
  public void select(final ProxySubject item)
  {
    if (!isSelected(item)) {
      mSelectedObjects.add(item);
      repaint();
    }
  }

  public void unselect(final ProxySubject item)
  {
    if (mSelectedObjects.remove(item)) {
      if (item instanceof LabelBlockSubject) {
        final LabelBlockSubject block = (LabelBlockSubject) item;
        final List<AbstractSubject> children = block.getEventListModifiable();
        mSelectedObjects.removeAll(children);
      }
      repaint();
    }
  }


  //#########################################################################
  //# Rendering Hints
  public ProxySubject getOriginal(final ProxySubject item)
  {
    assert(item != null);
    if (mSecondaryGraph == null) {
      return item;
    } else {
      return (ProxySubject) mSecondaryGraph.getOriginal(item);
    }
  }

  private boolean isRenderedSelected(final ProxySubject item)
  {
    final ProxySubject original = getOriginal(item);
    if (original == null) {
      return false;
    } else if (isSelected(original)) {
      return true;
    } else if (original instanceof LabelGeometrySubject ||
               original instanceof LabelBlockSubject ||
               original instanceof GuardActionBlockSubject) {
      final ProxySubject parent = (ProxySubject) original.getParent();
      return isSelected(parent);
    } else {
      return false;
    }
  }

  private boolean isSelected(final Proxy item)
  {
    return mSelectedObjects.contains(item);
  }

  private boolean hasSelected(EventListExpressionSubject e)
  {
    for (Subject s : mSelectedObjects)
      {
        if (e.getEventList().contains(s))
          {
            return true;
          }
      }
    return false;
  }

  private boolean isRenderedFocused(final ProxySubject item)
  {
    final ProxySubject original = getOriginal(item);
    if (original == null) {
      return false;
    } else {
      return isFocused(original);
    }
  }

  private boolean isFocused(final ProxySubject item)
  {
    if (mFocusedObject == null) {
      return false;
    } else if (mFocusedObject == item) {
      return true;
    } else if (mFocusedObject instanceof SimpleNodeSubject ||
               mFocusedObject instanceof EdgeSubject) {
      return item.getParent() == mFocusedObject;
    } else if (mFocusedObject instanceof LabelBlockSubject ||
               mFocusedObject instanceof GuardActionBlockSubject ||
               mFocusedObject instanceof LabelGeometrySubject) {
      return mFocusedObject.getParent() == item;
    } else {
      return false;
    }
  }
 
  private DRAGOVERSTATUS getDragOver(ProxySubject s)
  {
    if (!isFocused(s)) {
      return DRAGOVERSTATUS.NOTDRAG;
    }
    return mExternalDragStatus;
  }

  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    final ProxySubject item = (ProxySubject) proxy;
    final boolean isFocused = isRenderedFocused(item);
    final boolean selected = isRenderedSelected(item);
    final boolean showHandles;
    if (!selected) {
      showHandles = false;
    } else if (mInternalDragAction != null) {
      // *** BUG ***
      // Label block boundary treated as handle---ugly!
      // ***
      showHandles = item instanceof LabelBlockSubject;
    } else {
      showHandles = mController.canBeSelected(item);
    }
    final boolean error = isError(item);
    EditorSurface.DRAGOVERSTATUS dragOver =
      EditorSurface.DRAGOVERSTATUS.NOTDRAG;
    int priority = getPriority(item);
    if (mDontDraw.contains(item)) {
      priority = -1;
    } else if (selected) {
      priority += 6;
    }
    if (isFocused) {
      dragOver = mExternalDragStatus;
    }
    return new RenderingInformation
      (showHandles, isFocused,
       EditorColor.getColor(item, dragOver, selected, error),
       EditorColor.getShadowColor(item, dragOver, selected, error),
       priority);
  }

  public List<MiscShape> getDrawnObjects()
  {
    if (mInternalDragAction == null) {
      return Collections.emptyList();
    } else {
      return mInternalDragAction.getDrawnObjects();
    }
  }


  //#########################################################################
  //# Error Display
  private void updateError()
  {
    if (!mIsCommittingSecondaryGraph) {
      mError.clear();
      final Collection<NodeProxy> nodes = getDrawnGraph().getNodes();
      for (final NodeProxy n1 : nodes) {
        final Shape s1 = getShapeProducer().getShape(n1).getShape();
        for (final NodeProxy n2 : nodes) {
          if (n1 != n2 &&
              !(n1 instanceof GroupNodeProxy &&
                n2 instanceof GroupNodeProxy)) {
            final Shape s2 = getShapeProducer().getShape(n2).getShape();
            if (overlap(s1, s2)) {
              final NodeSubject node1 = (NodeSubject) n1;
              final NodeSubject node2 = (NodeSubject) n2;
              mError.add(node1);
              mError.add(node2);
            }
          }
        }
      }
    }
  }

  private boolean isError(final ProxySubject subject)
  {
    return mError.contains(subject);
  }


  //#########################################################################
  //# Geometry Auxiliaries
  /**
   * Returns the closest coordinate (works for both x and y) lying on the grid.
   */
  private int findGrid(final double x)
  {
    final int gridSize = Config.GUI_EDITOR_GRID_SIZE.get();
    return gridSize * (int) Math.round(x / gridSize);
  }

  /**
   * Finds the closest point to p lying on the grid.
   */
  private Point findGrid(final Point2D point)
  {
    final int x = findGrid(point.getX());
    final int y = findGrid(point.getY());
    return new Point(x, y);
  }

  private Point2D findNodeAnchorPoint(final NodeSubject node,
                                      final Point2D click)
  {
    if (node instanceof SimpleNodeSubject) {
      final SimpleNodeSubject simple = (SimpleNodeSubject) node;
      return simple.getPointGeometry().getPoint();
    } else if (node instanceof GroupNodeSubject) {
      final GroupNodeSubject group = (GroupNodeSubject) node;
      final Rectangle2D rect = group.getGeometry().getRectangle();
      return GeometryTools.findIntersection(rect, click);
    } else {
      throw new ClassCastException
        ("Unknown node type: " + node.getClass().getName() + "!");
    }
  }

  private boolean overlap(Shape s1, Shape s2)
  {
    if (s1.equals(s2))
      {
        return true;
      }
    Rectangle2D r1 = s1.getBounds2D();
    Rectangle2D r2 = s2.getBounds2D();
    return r1.intersects(r2) && !(r1.contains(r2) || r2.contains(r1));
  }

  /**
   * Finds all focusable objects at a given positition.
   * This method examines the graph and determines which objects are
   * active, i.e., can be selected by a click at the given position
   * in the current context.
   */
  private Collection<ProxySubject> getFocusableObjectsAtPosition
    (final Point point)
  {
    final Collection<ProxySubject> collection = new LinkedList<ProxySubject>();
    final GraphSubject graph = getGraph();
    for (final NodeSubject node : graph.getNodesModifiable()) {
      collectFocusableObjectAtPosition(node, point, collection);
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        final LabelGeometrySubject geo = simple.getLabelGeometry();
        collectFocusableObjectAtPosition(geo, point, collection);
      }
    }
    for (final EdgeSubject edge : graph.getEdgesModifiable()) {
      collectFocusableObjectAtPosition(edge, point, collection);
      final LabelBlockSubject block = edge.getLabelBlock();
      collectFocusableObjectAtPosition(block, point, collection);
      final GuardActionBlockSubject ga = edge.getGuardActionBlock();
      collectFocusableObjectAtPosition(ga, point, collection);
    }
    collectFocusableObjectAtPosition
      (graph.getBlockedEvents(), point, collection);
    return collection;
  }

  private void collectFocusableObjectAtPosition
    (final ProxySubject item,
     final Point point,
     final Collection<ProxySubject> collection)
  {
    if (item == null || getHighlightPriority(item) < 0) {
      return;
    }
    final ProxyShape shape = getShapeProducer().getShape(item);
    if (shape == null) {
      return;
    }
    final int x = point.x;
    final int y = point.y;
    if (!shape.isClicked(x, y)) {
      return;
    }
    collection.add(item);
  }

  private int getHighlightPriority(final ProxySubject item)
  {
    if (mInternalDragAction == null) {
      return mController.getHighlightPriority(item);
    } else {
      return mInternalDragAction.getHighlightPriority(item);
    }
  }

  /**
   * Finds the handle clicked by a mouse event.
   * @param  item   The item under the cursor.
   * @param  event  The mouse event being processed.
   * @return The handle clicked, or <CODE>null</CODE>
   */
  private Handle getClickedHandle(final ProxySubject item,
                                  final MouseEvent event)
  {
    if (isSelected(item)) {
      final int x = event.getX();
      final int y = event.getY();
      final ProxyShape shape = getShapeProducer().getShape(item);
      return shape.getClickedHandle(x, y);
    } else {
      return null;
    }
  }

  /**
   * Determines which item is in focus for selection or dragging.
   * If the item currently in focus is a selected label block,
   * the item really to be selected may be a label under the cursor,
   * otherwise thos method returns the item in focus.
   * lead to a drag-move or drag-and-drop operation. A drag-move or
   * @param  event  The mouse event being processed.
   * @return The item to be dragged or <CODE>null</CODE>
   */
  private ProxySubject getItemToBeSelected(final MouseEvent event)
  {
    final ProxySubject item = mFocusedObject;
    if (item == null) {
      return null;
    } else if (item instanceof LabelBlockSubject && isSelected(item)) {
      final LabelBlockSubject block = (LabelBlockSubject) item;
      final Point point = event.getPoint();
      final ProxySubject label = getLabelToBeSelected(block, point);
      return label == null ? item : label;
    } else {
      return item;
    }
  }

  private ProxySubject getLabelToBeSelected(final LabelBlockSubject block,
                                            final Point point)
  {
    for (final ProxySubject sub : block.getEventListModifiable()) {
      final ProxyShape shape = getShapeProducer().getShape(sub);
      if (shape.getShape().contains(point)) {
        return sub;
      }
    }
    return null;
  }


  //#########################################################################
  //# Controller Auxiliaries
  private boolean createSecondaryGraph()
  {
    if (mSecondaryGraph == null) {
      mSecondaryGraph = new EditorGraph(getGraph());
      mSecondaryShapeProducer = new SubjectShapeProducer
        (mSecondaryGraph, mSecondaryGraph, getModule());
      mSecondaryGraph.addModelObserver(ControlledSurface.this);
      return true;
    } else {
      return false;
    }
  }

  private void clearSecondaryGraph()
  {
    if (mSecondaryGraph != null) {
      mSecondaryGraph.removeModelObserver(ControlledSurface.this);
      mSecondaryGraph = null;
      mSecondaryShapeProducer = null;
      repaint();
    }
  }

  private void commitSecondaryGraph()
  {
    if (mSecondaryGraph != null) {
      final Command move =
        new MoveObjects(mSecondaryGraph.getChanged(), getGraph());
      mRoot.getUndoInterface().executeCommand(move);
    }
  }

  private void maybeShowPopup(final MouseEvent event)
  {
    if (event.isPopupTrigger()) {
      final ProxySubject s = mFocusedObject;
      if (s != null) {
        if (s instanceof SimpleNodeSubject) {
          SimpleNodeSubject node = (SimpleNodeSubject) s;
          EditorNodePopupMenu popup = new EditorNodePopupMenu(this, node);
          popup.show(this, event.getX(), event.getY());
        } else if (s instanceof GroupNodeSubject) {
          GroupNodeSubject node = (GroupNodeSubject) s;
          EditorNodeGroupPopupMenu popup =
            new EditorNodeGroupPopupMenu(this, node);
          popup.show(this, event.getX(), event.getY());
        } else if (s instanceof EdgeSubject) {
          EdgeSubject edge = (EdgeSubject) s;
          EditorEdgePopupMenu popup = new EditorEdgePopupMenu(this, edge);
          popup.show(this, event.getX(), event.getY());
        } else if (s instanceof LabelBlockSubject) {
          LabelBlockSubject label = (LabelBlockSubject) s;
          EditorLabelBlockPopupMenu popup = new
            EditorLabelBlockPopupMenu(this, label);
          popup.show(this, event.getX(), event.getY());
        } else if (s instanceof GuardActionBlockSubject) {
          GuardActionBlockSubject ga = (GuardActionBlockSubject) s;
          EditorGuardActionBlockPopupMenu popup = new
            EditorGuardActionBlockPopupMenu(this, ga);
          popup.show(this, event.getX(), event.getY());
        }
      } else {
        final EditorWindowInterface iface = getEditorInterface();
        final EditorSurfacePopupMenu popup =
          new EditorSurfacePopupMenu(iface);
        popup.show(this, event.getX(), event.getY());
      }
    }
  }
  
  public Point getCurrentPoint()
  {
    return new Point(mCurrentPoint);
  }
  
  public Collection<ProxySubject> getSelected()
  {
    return Collections.unmodifiableCollection(mSelectedObjects);
  }

  public void createOptions(EditorWindowInterface root)
  {
    mOptions = new EditorOptions(root);
  }


  //#########################################################################
  //# Inner Class DTListener
  private class DTListener extends DropTargetAdapter
  {

    //#######################################################################
    //# Interface java.awt.dnd.DropTargetAdapter
    public void dragOver(final DropTargetDragEvent event)
    { 
      final Point point = event.getLocation();
      final InternalDragActionDND action = getInternalDragAction(point);
      action.continueDrag(event);
    }

    public void drop(final DropTargetDropEvent event)
    {
      final Point point = event.getLocation();
      final InternalDragActionDND action = getInternalDragAction(point);
      action.commitDrag(event);
      mInternalDragAction = null;
      mController.updateHighlighting(point);
    }

    //###################################################################
    //# Auxiliary Methods
    private InternalDragActionDND getInternalDragAction(final Point point)
    {
      if (mInternalDragAction == null) {
        final InternalDragActionDND action = new InternalDragActionDND(point);
        mInternalDragAction = action;
        return action;
      } else {
        return (InternalDragActionDND) mInternalDragAction;
      }
    }

  }


  //#########################################################################
  //# Inner Class ToolController
  private abstract class ToolController
    implements MouseListener, MouseMotionListener
  {

    //#######################################################################
    //# Highlighting and Selecting
    int getHighlightPriority(ProxySubject item)
    {
      return -1;
    }

    boolean canBeSelected(final ProxySubject item)
    {
      return getHighlightPriority(item) > 0;
    }

    /**
     * Updates highlighting based on the given new location of the mouse
     * pointer.
     */
    void updateHighlighting(final Point point)
    {
      mCurrentPoint = point;
      updateHighlighting();
    }

    /**
     * Updates highlighting based on the last recorded location of the mouse
     * pointer.
     */
    void updateHighlighting()
    {
      if (mCurrentPoint != null) {
        final Collection<ProxySubject> objects =
          getFocusableObjectsAtPosition(mCurrentPoint);
        final ProxySubject object;
        if (objects.isEmpty()) {
          object = null;
        } else {
          object = Collections.max(objects, mComparator);
        }
        if (object != mFocusedObject) {
          mFocusedObject = object;
          repaint();
        }
      }
    }

    //#######################################################################
    //# Additional Callbacks
    /**
     * Called after the listener is installed due to tool selection.
     */
    void installed()
    {
      requestFocusInWindow();
      updateHighlighting();
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
    }

    public void mousePressed(final MouseEvent event)
    {
      requestFocusInWindow();
      maybeShowPopup(event);
    }

    public void mouseReleased(final MouseEvent event)
    {
      maybeShowPopup(event);
      if (mInternalDragAction != null) {
        final Point point = event.getPoint();
        final boolean needRepaint;
        if (mInternalDragAction.hasDragged()) {
          mInternalDragAction.commitDrag(point);
        } else {
          mInternalDragAction.cancelDrag(point);
        }
        mInternalDragAction = null;
        updateHighlighting(point);
      }
    }

    public void mouseDragged(final MouseEvent event)
    {
      final Point point = event.getPoint();
      updateHighlighting(point);
      if (mInternalDragAction != null) {
        mInternalDragAction.continueDrag(point);
      }
    }

    public void mouseEntered(final MouseEvent event)
    {
      final Point point = event.getPoint();
      updateHighlighting(point);
    }

    public void mouseExited(final MouseEvent event)
    {
      final Point point = event.getPoint();
      updateHighlighting(point);
    }


    //#######################################################################
    //# Interface java.awt.MouseMotionListener
    public void mouseMoved(final MouseEvent event)
    {
      final Point point = event.getPoint();
      updateHighlighting(point);
    }

  }


  //#########################################################################
  //# Inner Class EmbedderController
  /**
   * A dummy controller for spring embedding.
   * Presently, no interactions are allowed while the spring embedder
   * is running, but this controller could be extended to support
   * dragging during the animation.
   */
  private class EmbedderController
    extends ToolController
  {

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mousePressed(final MouseEvent event)
    {
      // No popup!
    }

    public void mouseReleased(final MouseEvent event)
    {
      // No popup!
    }

  }


  //#########################################################################
  //# Inner Class SelectController
  private class SelectController
    extends ToolController
  {

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      if (item instanceof SimpleNodeSubject) {
        return 5;
      } else if (item instanceof EdgeSubject) {
        return 4;
      } else if (item instanceof LabelGeometrySubject) {
        return 3;
      } else if (item instanceof LabelBlockSubject ||
                 item instanceof GuardActionBlockSubject) {
        return 2;
      } else if (item instanceof GroupNodeSubject) {
        return 1;
      } else {
        return -1;
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      super.mouseClicked(event);
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2 &&
          mFocusedObject != null) {
        if (mFocusedObject instanceof LabelGeometrySubject) {
          final LabelGeometrySubject label =
            (LabelGeometrySubject) mFocusedObject;
          final JTextField text = new NameEditField(label);
          ControlledSurface.this.add(text);
          text.setVisible(true);
          text.requestFocusInWindow();
          repaint();
        } else if (mFocusedObject instanceof EdgeSubject) {
          EditorEditEdgeDialog.showDialog((EdgeSubject) mFocusedObject);
        } else if (mFocusedObject instanceof GuardActionBlockSubject) {
          final EdgeSubject edge = (EdgeSubject) mFocusedObject.getParent();
          EditorEditEdgeDialog.showDialog(edge);
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      super.mousePressed(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Point point = event.getPoint();
        final ProxySubject item = getItemToBeSelected(event);
         if (item == null || event.isControlDown()) {
          // Clicking on whitespace --- drag select.
          mInternalDragAction = new InternalDragActionSelect(event);
        } else if (item != mFocusedObject) {
          if (isSelected(item)) {
            // Selected label in a label block --- EXTERNAL dragging only
            mInternalDragAction = new InternalDragActionDND(event, item);
          } else {
            // Unselected label --- move label block or select label
            mInternalDragAction = new InternalDragActionMove(event, item);
          }
        } else {
          final Handle handle = getClickedHandle(item, event);
          if (handle == null) {
            mInternalDragAction = new InternalDragActionMove(event);
          } else {
            switch (handle.getType()) {
            case INITIAL:
              mInternalDragAction = new InternalDragActionInitial(event);
              break;
            case SOURCE:
            case TARGET:
              mInternalDragAction =
                new InternalDragActionEdge(event, handle);
              break;
            case NW:
            case N:
            case NE:
            case W:
            case E:
            case SW:
            case S:
            case SE:
              mInternalDragAction =
                new InternalDragActionResizeGroupNode(event, handle);
              break;
            default:
              throw new IllegalStateException
                ("Unknown handle type: " + handle.getType());
            }
          }
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class NodeController
  private class NodeController
    extends ToolController
  {

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      if (item instanceof SimpleNodeSubject) {
        return 2;
      } else if (item instanceof LabelGeometrySubject) {
        return 1;
      } else {
        return -1;
      }
    }

    /**
     * Prevents stacking of nodes.
     * When the grid is active and there is no item in focus,
     * this methods checks whether there is a simple node at the nearest
     * grid point, and if so, changes focus to that node.
     */
    void updateHighlighting()
    {
      super.updateHighlighting();
      if (mCurrentPoint != null &&
          mFocusedObject == null &&
          mInternalDragAction == null &&
          Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get()) {
        final ProxyShapeProducer shaper = getShapeProducer();
        final Point snapped = findGrid(mCurrentPoint);
        if (!snapped.equals(mCurrentPoint)) {
          final int x = snapped.x;
          final int y = snapped.y;
          for (final NodeSubject node : getGraph().getNodesModifiable()) {
            if (node instanceof SimpleNodeSubject) {
              final ProxyShape shape = shaper.getShape(node);
              if (shape.isClicked(x, y)) {
                mCurrentPoint = snapped;
                super.updateHighlighting();
                return;
              }
            }
          }
        }
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      super.mouseClicked(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (event.getClickCount() == 1 && mFocusedObject == null) {
          // Create node.
          final Point point = event.getPoint();
          final Point snapped;
          if (Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get()) {
            snapped = findGrid(point);
          } else {
            snapped = point;
          }
          doCreateSimpleNode(snapped);
        } else if (event.getClickCount() == 2 &&
                   mFocusedObject != null &&
                   mFocusedObject instanceof LabelGeometrySubject) {
          // Double-click to rename nodes
          final LabelGeometrySubject label =
            (LabelGeometrySubject) mFocusedObject;
          final JTextField text = new NameEditField(label);
          ControlledSurface.this.add(text);
          text.setVisible(true);
          text.requestFocusInWindow();
          repaint();
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      super.mousePressed(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Point point = event.getPoint();
        if (event.isControlDown() || mFocusedObject == null) {
          // Clicking on whitespace --- drag select.
          mInternalDragAction = new InternalDragActionSelect(event);
        } else {
          final Handle handle = getClickedHandle(mFocusedObject, event);
          if (handle == null) {
            mInternalDragAction = new InternalDragActionMove(event);
          } else {
            mInternalDragAction = new InternalDragActionInitial(event);
          }
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class GroupNodeController
  private class GroupNodeController
    extends ToolController
  {

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      if (item instanceof GroupNodeSubject) {
        return 1;
      } else {
        return -1;
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mousePressed(final MouseEvent event)
    {
      maybeShowPopup(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Point point = event.getPoint();
        if (event.isControlDown()) {
          mInternalDragAction = new InternalDragActionSelect(event);
        } else if (mFocusedObject == null) {
          // Create new nodegroup
          mInternalDragAction = new InternalDragActionCreateGroupNode(event);
        } else if (mFocusedObject instanceof GroupNodeSubject) {
          final Handle handle = getClickedHandle(mFocusedObject, event);
          if (handle == null) {
            mInternalDragAction = new InternalDragActionMove(event);
          } else {
            mInternalDragAction =
              new InternalDragActionResizeGroupNode(event, handle);
          } 
        } else {
          mInternalDragAction = new InternalDragActionMove(event);
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class EdgeController
  private class EdgeController
    extends ToolController
  {

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      if (item instanceof SimpleNodeSubject) {
        return 4;
      } else if (item instanceof EdgeSubject) {
        return 2;
      } else if (item instanceof LabelBlockSubject ||
                 item instanceof GuardActionBlockSubject) {
        return 1;
      } else if (item instanceof GroupNodeSubject) {
        return 3;
      } else {
        return -1;
      }
    }

    boolean canBeSelected(final ProxySubject item)
    {
      return 
        item instanceof EdgeSubject ||
        item instanceof LabelBlockSubject ||
        item instanceof GuardActionBlockSubject;
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      super.mouseClicked(event);
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2 &&
          mFocusedObject != null &&
          mFocusedObject instanceof SimpleNodeSubject) {
        // Double click simple node to create selfloop
        final SimpleNodeSubject node = (SimpleNodeSubject) mFocusedObject;
        doCreateEdge(node, node, null, null);
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      maybeShowPopup(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        final Point point = event.getPoint();
        final ProxySubject item = getItemToBeSelected(event);
        if (item == null || event.isControlDown()) {
          mInternalDragAction = new InternalDragActionSelect(event);
        } else if (item != mFocusedObject) {
          if (isSelected(item)) {
            // Selected label in a label block --- EXTERNAL dragging only
            mInternalDragAction = new InternalDragActionDND(event, item);
          } else {
            // Unselected label --- move label block or select label
            mInternalDragAction = new InternalDragActionMove(event, item);
          }
        } else if (item instanceof NodeSubject) {
          // Clicking on node or nodegroup --- create edge.
          mInternalDragAction = new InternalDragActionEdge(event);
        } else if (item instanceof EdgeSubject) {
          final Handle handle = getClickedHandle(mFocusedObject, event);
          if (handle == null) {
            mInternalDragAction = new InternalDragActionMove(event);
          } else {
            mInternalDragAction =
              new InternalDragActionEdge(event, handle);
          }
        } else {
          mInternalDragAction = new InternalDragActionMove(event);
        }
      }
    }

  }


  //#########################################################################
  //# Inner Class InternalDragAction
  /**
   * An internal dragging operation. This class supports the different
   * dragging operations through a common interface. When a drag action is
   * recognised by a mouse listener, it creates and instantiates the
   * appropriate subclass of this class. Then the methods {@link
   * #continueDrag(Point)} and {@link #commitDrag(Point)} are called
   * automatically as the mouse moves and is released.
   */
  private abstract class InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragAction(final MouseEvent event)
    {
      this(event, event.getPoint());
    }

    private InternalDragAction(final MouseEvent event, final Point snapped)
    {
      this(event.getPoint(), snapped, event.isControlDown());
    }

    private InternalDragAction(final Point point,
                               final Point snapped,
                               final boolean controlDown)
    {
      mWasControlDown = controlDown;
      mPreviousSelection = mSelectedObjects;
      mDragStart = point;
      mDragStartOnGrid = snapped;
      mDragCurrent = point;
      mDragCurrentOnGrid = snapped;
    }

    //#######################################################################
    //# Simple Access
    boolean wasControlDown()
    {
      return mWasControlDown;
    }

    boolean hasDragged()
    {
      return mHasDragged;
    }

    void setHasDragged()
    {
      mHasDragged = true;
    }

    Point getDragStart()
    {
      return mDragStart;
    }

    Point getDragStartOnGrid()
    {
      return mDragStartOnGrid;
    }

    Point getDragCurrent()
    {
      return mDragCurrent;
    }

    Point getDragCurrentOnGrid()
    {
      return mDragCurrentOnGrid;
    }

    Rectangle getDragRectangle()
    {
      if (mDragRectangle == null) {
        final int x;
        final int y;
        final int width;
        final int height;
        final Point start = shouldSnapToGrid() ? mDragStartOnGrid : mDragStart;
        final int x1 = start.x;
        final int y1 = start.y;
        final Point current =
          shouldSnapToGrid() ? mDragCurrentOnGrid : mDragCurrent;
        final int x2 = current.x;
        final int y2 = current.y;
        if (x1 < x2) {
          x = x1;
          width = x2 - x1;
        } else {
          x = x2;
          width = x1 - x2;
        }
        if (y1 < y2) {
          y = y1;
          height = y2 - y1;
        } else {
          y = y2;
          height = y1 - y2;
        }
        mDragRectangle = new Rectangle(x, y, width, height);
      }
      return mDragRectangle;
    }

    void setDragCurrent(final Point point, final Point snapped)
    {
      mDragCurrent = point;
      mDragCurrentOnGrid = snapped;
    }

    /**
     * Determines whether this internal drag operation should snap to grid.
     * This is <CODE>false</CODE> by default, and gets overridden by
     * subclasses.
     */
    boolean shouldSnapToGrid()
    {
      return false;
    }

    Collection<ProxySubject> getPreviousSelection()
    {
      return mPreviousSelection;
    }

    //#######################################################################
    //# Dragging
    /**
     * Continues this internal dragging operation. This method updates the
     * mouse pointer positions {@link #mDragCurrent} and {@link
     * #mDragCurrentOnGrid}. This method is overriden to perform additional
     * changes that need to be displayed. Subclasses must call the
     * superclass method also.
     * @param  point    The position of the mouse pointer.
     * @return <CODE>true</CODE> if the new position differs from the
     *         previously recorded one.
     */
    boolean continueDrag(final Point point)
    {
      if (mDragCurrent.equals(point)) {
        return false;
      }
      mDragCurrent = point;
      if (shouldSnapToGrid()) {
        final Point snapped = findGrid(point);
        if (mDragCurrentOnGrid.equals(snapped)) {
          return false;
        }
        mDragCurrentOnGrid = snapped;
      } else {
        mDragCurrentOnGrid = mDragCurrent;
      }
      mDragRectangle = null;
      setHasDragged();
      repaint();
      return true;
    }

    /**
     * Completes this internal dragging operation. This method is overriden
     * to create and executes the appropriate command to reflect all the
     * changed made during the frag operation. Subclasses must call the
     * superclass method also.
     * @param  event   The current mouse event.
     * @return <CODE>true</CODE> if changes have been applied.
     */
    void commitDrag(final Point point)
    {
      mSelectedObjects = mPreviousSelection;
      repaint();
    }

    /**
     * Cancels this operation. This method is called when the user has
     * only clicked rather than dragged the mouse. Sometimes the selection
     * needs to be updated in such a case. Subclasses must call the
     * superclass method also.
     * @param  event   The current mouse event.
     * @return <CODE>true</CODE> if changes have been applied.
     */
    void cancelDrag(final Point point)
    {
      if (mSelectedObjects != mPreviousSelection) {
        mSelectedObjects = mPreviousSelection;
        repaint();
      }
    }

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      return -1;
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      return Collections.emptyList();
    }

    //#######################################################################
    //# Data Members
    /**
     * Whether control was pressed when this drag action was started.
     */
    private final boolean mWasControlDown;
    /**
     * Backup of the selection when this action was started.
     * The selection is automatically stored away at the beginning of every
     * drag action and restored at the end. It is up to the subclasses to
     * apply appropriate changes to the copy during the drag operation, and
     * to the original selection in the end.
     */
    private final Collection<ProxySubject> mPreviousSelection;
    /**
     * Whether the mouse has been dragged during this operation.
     */
    private boolean mHasDragged;
    /**
     * The position of the mouse cursor when the current internal dragging
     * operation was started.
     */
    private final Point mDragStart;
    /**
     * The position of {@link #mDragStart} snapped to grid.
     */
    private final Point mDragStartOnGrid;
    /**
     * The last position of the mouse cursor evalutaed during an internal
     * dragging operation.
     */
    private Point mDragCurrent;
    /**
     * The position of {@link #mDragCurrent} snapped to grid.
     */
    private Point mDragCurrentOnGrid;
    /**
     * Cached copy of drag rectangle.
     */
    private Rectangle mDragRectangle;
  }


  //#########################################################################
  //# Inner Class BigInternalDragAction
  private class BigInternalDragAction
    extends InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private BigInternalDragAction(final MouseEvent event)
    {
      super(event);
    }

    private BigInternalDragAction(final MouseEvent event, final Point snapped)
    {
      super(event, snapped);
    }

    //#######################################################################
    //# Simple Access
    boolean createSecondaryGraph()
    {
      return ControlledSurface.this.createSecondaryGraph();
    }

    void clearSecondaryGraph()
    {
      ControlledSurface.this.clearSecondaryGraph();
    }

    void commitSecondaryGraph()
    {
      ControlledSurface.this.commitSecondaryGraph();
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        createSecondaryGraph();
        return true;
      } else {
        return false;
      }
    }

    void commitDrag(final Point point)
    {
      super.commitDrag(point);
      mIsCommittingSecondaryGraph = true;
      commitSecondaryGraph();
      clearSecondaryGraph();
      mIsCommittingSecondaryGraph = false;
      updateError();
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      clearSecondaryGraph();
    }

  }


  //#########################################################################
  //# Inner Class InternalDragActionSelect
  private class InternalDragActionSelect
    extends InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionSelect(final MouseEvent event)
    {
      super(event);
      if (mFocusedObject instanceof LabelBlockSubject &&
          isSelected(mFocusedObject)) {
        mLabelBlock = (LabelBlockSubject) mFocusedObject;
      } else {
        mLabelBlock = null;
      }
      if (mFocusedObject == null) {
        mCurrentDragSelection = Collections.emptyList();
      } else {
        mCurrentDragSelection = Collections.singletonList(mFocusedObject);
      }
      if (wasControlDown()) {
        mSelectedObjects = new HashSet<ProxySubject>(mSelectedObjects);
        mSelectedObjects.removeAll(mCurrentDragSelection);
      } else {
        mSelectedObjects = new HashSet<ProxySubject>();
        mSelectedObjects.addAll(mCurrentDragSelection);
      }
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        if (wasControlDown()) {
          mSelectedObjects = new HashSet<ProxySubject>(getPreviousSelection());
          mCurrentDragSelection = getDragSelection();
          if (mSelectedObjects.containsAll(mCurrentDragSelection)) {
            mSelectedObjects.removeAll(mCurrentDragSelection);
          } else {
            mSelectedObjects.addAll(mCurrentDragSelection);
          }
        } else {
          mSelectedObjects.removeAll(mCurrentDragSelection);
          mCurrentDragSelection = getDragSelection();
          mCurrentDragSelection.removeAll(mSelectedObjects);
          mSelectedObjects.addAll(mCurrentDragSelection);
        }
        return true;
      } else {
        return false;
      }
    }

    void commitDrag(final Point point)
    {
      super.commitDrag(point);
      if (!wasControlDown()) {
        doReplaceSelection(mCurrentDragSelection);
      } else if (mSelectedObjects.containsAll(mCurrentDragSelection)) {
        doRemoveFromSelection(mCurrentDragSelection);
      } else {
        doAddToSelection(mCurrentDragSelection);
      }
      repaint();
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      final ProxySubject label = getLabelToBeSelected();
      if (label == null) {
        if (wasControlDown()) {
          for (final ProxySubject item : mCurrentDragSelection) {
            doToggleSelection(item);
          }
        } else {
          doReplaceSelection(mCurrentDragSelection);
        }
      } else {
        if (wasControlDown()) {
          doToggleSelection(label);
        } else {
          doReplaceLabelSelection(label);
        }
      }
      repaint();
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      final Rectangle rect = getDragRectangle();
      final MiscShape shape =
        new GeneralShape(rect,
                         EditorColor.SELECTCOLOR,
                         EditorColor.shadow(EditorColor.SELECTCOLOR));
      return Collections.singletonList(shape);
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Returns a list of all objects within a drag rectangle,
     * i.e., the items to be selected.
     */
    private Collection<ProxySubject> getDragSelection()
    {
      final GraphSubject graph = getGraph();
      final Rectangle dragrect = getDragRectangle();
      final LinkedList<ProxySubject> selection =
        new LinkedList<ProxySubject>();
      for (final NodeSubject node : graph.getNodesModifiable()) {
        includeShape(selection, dragrect, node);
        if (node instanceof SimpleNodeSubject) {
          final SimpleNodeSubject simple = (SimpleNodeSubject) node;
          final LabelGeometrySubject geo = simple.getLabelGeometry();
          includeShape(selection, dragrect, geo);
        }
      }
      for (final EdgeSubject edge : graph.getEdgesModifiable()) {
        includeShape(selection, dragrect, edge);
        final LabelBlockSubject block = edge.getLabelBlock();
        includeShape(selection, dragrect, block);
        final GuardActionBlockSubject ga = edge.getGuardActionBlock();
        includeShape(selection, dragrect, ga);
      }
      final LabelBlockSubject blocked = graph.getBlockedEvents();
      includeShape(selection, dragrect, blocked);
      return selection;
    }

    private void includeShape(final Collection<ProxySubject> selection,
                              final Rectangle dragrect,
                              final ProxySubject item)
    {
      if (item != null && mController.canBeSelected(item)) {
        final ProxyShape shape = getShapeProducer().getShape(item);
        if (shape != null &&
            dragrect.contains(shape.getShape().getBounds())) {
          selection.add(item);
        }
      }
    }

    private ProxySubject getLabelToBeSelected()
    {
      if (mLabelBlock != null) {
        final Point point = getDragStart();
        return ControlledSurface.this.getLabelToBeSelected(mLabelBlock, point);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final LabelBlockSubject mLabelBlock;
    private Collection<ProxySubject> mCurrentDragSelection;

  }


  //#########################################################################
  //# Inner Class InternalDragActionMove
  private class InternalDragActionMove
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionMove(final MouseEvent event)
    {
      this(event, mFocusedObject);
    }

    private InternalDragActionMove(final MouseEvent event,
                                   final ProxySubject clicked)
    {
      super(event);
      mClickedObject = clicked;
      mMovedObject = mFocusedObject;
      if (mMovedObject == null || !isSelected(mMovedObject)) {
        if (wasControlDown()) {
          mSelectedObjects = new LinkedList<ProxySubject>(mSelectedObjects);
        } else {
          mSelectedObjects = new LinkedList<ProxySubject>();
        }
        if (mMovedObject != null) {
          mSelectedObjects.add(mMovedObject);
        }
      }
      Point2D snap = null;
      if (Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get()) {
        // Move operation snaps to grid when a node is moved.
        for (final ProxySubject item : mSelectedObjects) {
          if (item instanceof SimpleNodeSubject) {
            final SimpleNodeSubject simple = (SimpleNodeSubject) item;
            snap = simple.getPointGeometry().getPoint();
          } else if (item instanceof GroupNodeSubject) {
            final GroupNodeSubject group = (GroupNodeSubject) item;
            final Rectangle2D rect = group.getGeometry().getRectangle();
            snap = new Point2D.Double(rect.getX(), rect.getY());
          }
        }
      }
      mSnapPoint = snap;
    }

    //#######################################################################
    //# Simple Access
    boolean shouldSnapToGrid()
    {
      return mSnapPoint != null;
    }

    boolean createSecondaryGraph()
    {
      if (super.createSecondaryGraph()) {
        mMoveVisitor = new MoveVisitor();
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      final Point start = getDragStart();
      if (shouldSnapToGrid()) {
        final double rx = mSnapPoint.getX();
        final double ry = mSnapPoint.getY();
        final int ix = start.x;
        final int iy = start.y;
        final double dx = point.getX() - ix;
        final double dy = point.getY() - iy;
        final int sx = (int) Math.round(findGrid(rx + dx) - rx);
        final int sy = (int) Math.round(findGrid(ry + dy) - ry);
        final Point snapped = new Point(ix + sx, iy + sy);
        if (snapped.equals(getDragCurrentOnGrid())) {
          return false;
        }
        setHasDragged();
        setDragCurrent(point, snapped);
        createSecondaryGraph();
      } else if (!super.continueDrag(point)) {
        return false;
      }
      final int dx = getDragCurrentOnGrid().x - start.x;
      final int dy = getDragCurrentOnGrid().y - start.y;
      mMoveVisitor.moveAll(dx, dy);
      return true;
    }

    void commitSecondaryGraph()
    {
      if (mMovedObject != null && !isSelected(mMovedObject)) {
        if (wasControlDown()) {
          doAddToSelection(mMovedObject);
        } else {
          doReplaceSelection(mMovedObject);
        }
      } 
      super.commitSecondaryGraph();
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      if (mMovedObject == null) {
        doClearSelection();
      } else if (wasControlDown()) {
        doToggleSelection(mClickedObject);
      } else {
        doReplaceLabelSelection(mClickedObject);
      }
    }

    //#######################################################################
    //# Data Members
    private final Point2D mSnapPoint;
    private final ProxySubject mClickedObject;
    private final ProxySubject mMovedObject;
    private MoveVisitor mMoveVisitor;

  }


  //#########################################################################
  //# Inner Class InternalDragActionDND
  private class InternalDragActionDND
    extends InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionDND(final MouseEvent event,
                                  final ProxySubject label)
    {
      super(event);
      mLabelBlock = (LabelBlockSubject) mFocusedObject;
      mClickedLabel = label;
      doAddToSelection(mClickedLabel);
    }

    private InternalDragActionDND(final Point point)
    {
      super(point, point, false);
      mLabelBlock = null;
      mClickedLabel = null;
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final DropTargetDragEvent event)
    {
      final Point point = event.getLocation();
      continueDrag(point);
      mController.updateHighlighting(point);

      IdentifierWithKind ikind;
      EventListExpressionSubject elist = null;
      Line2D line = null;
      try {
        ikind = (IdentifierWithKind)
          event.getTransferable().getTransferData(FLAVOUR);
      } catch (final UnsupportedFlavorException exception) {
        throw new IllegalArgumentException(exception);
      } catch (final IOException exception) {
        throw new IllegalArgumentException(exception);
      }

      final EventType etype = ikind.getKind();
      if (mFocusedObject != null) {
        if (mFocusedObject instanceof SimpleNodeSubject) {
          if (etype == EventType.UNKNOWN || etype == EventType.NODE_EVENTS) {
            final SimpleNodeSubject node =
              (SimpleNodeSubject) mFocusedObject;
            elist = node.getPropositions();
          }
        } else if (mFocusedObject instanceof LabelGeometrySubject) {
          if (etype == EventType.UNKNOWN || etype == EventType.NODE_EVENTS) {
            final SimpleNodeSubject node =
              (SimpleNodeSubject) mFocusedObject.getParent();
            elist = node.getPropositions();
          }
        } else if (mFocusedObject instanceof EdgeSubject) {
          if (etype == EventType.UNKNOWN || etype == EventType.EDGE_EVENTS) {
            final EdgeSubject edge = (EdgeSubject) mFocusedObject;
            elist = edge.getLabelBlock();
          }
        } else if (mFocusedObject instanceof LabelBlockSubject) {
          if (etype == EventType.UNKNOWN || etype == EventType.EDGE_EVENTS) {
            elist = (LabelBlockSubject) mFocusedObject;
          }
        }
      }

      if (elist != null) {
        boolean allpresent = true;
        Set<IdentifierSubject> contents =
          new TreeSet<IdentifierSubject>(NamedComparator.getInstance());
        for (final AbstractSubject item : elist.getEventListModifiable()) {
          if (item instanceof IdentifierSubject) {
            final IdentifierSubject ident = (IdentifierSubject) item;
            contents.add(ident);
          }
        }
        for (final IdentifierSubject ident : ikind.getIdentifiers()) {
          if (!contents.contains(ident)) {
            allpresent = false;
            break;
          }
        }
        if (allpresent) {
          elist = null;
        } else if (elist instanceof LabelBlockSubject) {
          final LabelBlockSubject block = (LabelBlockSubject) elist;
          final Rectangle2D bounds =
            getShapeProducer().getShape(block).getShape().getBounds();
          final double x1 = bounds.getMinX();
          final double x2 = bounds.getMaxX();
          double y;
          if (elist == mFocusedObject) {
            y = bounds.getMinY();
            for (final ProxySubject item : block.getEventListModifiable()) {
              final ProxyShape shape = getShapeProducer().getShape(item);
              final Rectangle2D rect = shape.getShape().getBounds();
              if (point.getY() < rect.getCenterY()) {
                y = rect.getMinY();
                break;
              } else {
                y = rect.getMaxY();
              }
            }
          } else {
            y = bounds.getMaxY();
          }
          line = new Line2D.Double(x1, y, x2, y);
        }
      }

      final int operation = DnDConstants.ACTION_COPY;
      if (elist == null) {
        mExternalDragStatus = EditorSurface.DRAGOVERSTATUS.CANTDROP;
      } else {
        mExternalDragStatus = EditorSurface.DRAGOVERSTATUS.CANDROP;
      }
      if (line == null ? mLine != null : !line.equals(mLine)) {
        mLine = line;
        repaint();
      }
      event.getDropTargetContext().getDropTarget().
        setDefaultActions(operation);
      event.acceptDrag(operation);

      return true;
    }

    void commitDrag(final DropTargetDropEvent event)
    {
      final Point point = event.getLocation();
      commitDrag(point);
      try {
        if (mExternalDragStatus == EditorSurface.DRAGOVERSTATUS.CANDROP) {
          final IdentifierWithKind ikind = (IdentifierWithKind)
            event.getTransferable().getTransferData(FLAVOUR);
          final Collection<IdentifierSubject> identifiers =
            ikind.getIdentifiers();
          if (mFocusedObject instanceof SimpleNodeSubject) {
            addToNode((SimpleNodeSubject) mFocusedObject, identifiers);
          } else if (mFocusedObject instanceof EdgeSubject) {
            addToEdge((EdgeSubject) mFocusedObject, identifiers);
          } else if (mFocusedObject instanceof LabelBlockSubject) {
            addToLabelGroup((LabelBlockSubject) mFocusedObject,
                            identifiers, event);
          } else if (mFocusedObject instanceof LabelGeometrySubject) {
            addToLabel((LabelGeometrySubject) mFocusedObject, identifiers);
          }
          event.dropComplete(true);
          requestFocusInWindow();
        } else {
          event.dropComplete(false);
        }
        mExternalDragStatus = EditorSurface.DRAGOVERSTATUS.NOTDRAG;
      } catch (final UnsupportedFlavorException exception) {
        throw new IllegalArgumentException(exception);
      } catch (final IOException exception) {
        throw new IllegalArgumentException(exception);
      }
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      if (mClickedLabel != null) {
        if (wasControlDown()) {
          doToggleSelection(mClickedLabel);
        } else {
          doReplaceLabelSelection(mClickedLabel);
        }
      }
    }

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      if (item instanceof EdgeSubject ||
          item instanceof LabelBlockSubject) {
        return 2;
      } else if (item instanceof SimpleNodeSubject) {
        return 1;
      } else {
        return -1;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Gets the list of currently selected labels.
     */
    private List<IdentifierSubject> getIdentifiersToBeDragged()
    {
      final List<IdentifierSubject> result =
        new LinkedList<IdentifierSubject>();
      for (final ProxySubject selected : mSelectedObjects) {
        if (selected instanceof LabelBlockSubject) {
          final LabelBlockSubject block = (LabelBlockSubject) selected;
          for (final ProxySubject item : block.getEventListModifiable()) {
            if (item.getParent().getParent() instanceof
                  EventListExpressionSubject
                && isSelected(item)) {
              // *** BUG ***
              // What about non-identifiers?
              // ***
              final IdentifierSubject ident = (IdentifierSubject) item;
              result.add(ident);
            }
          }
        }
      }
      return result;
    }

    private void addToNode(final SimpleNodeSubject node,
                           final Collection<IdentifierSubject> identifiers)
    {
      final EventListExpressionSubject elist = node.getPropositions();
      doAddToEventList(elist, identifiers, elist.getEventList().size());
    }

    private void addToLabel(final LabelGeometrySubject label,
                            final Collection<IdentifierSubject> identifiers)
    {
      final SimpleNodeSubject node = (SimpleNodeSubject) label.getParent();
      addToNode(node, identifiers);
    }

    private void addToEdge(final EdgeSubject edge,
                           final Collection<IdentifierSubject> identifiers)
    {
      final LabelBlockSubject block = edge.getLabelBlock();
      doAddToEventList(block, identifiers, block.getEventList().size());
    }

    private void addToLabelGroup
      (final LabelBlockSubject block,
       final Collection<IdentifierSubject> identifiers,
       final DropTargetDropEvent event)
    {
      final double y = event.getLocation().getY();
      int pos = 0;
      for (final AbstractSubject item : block.getEventListModifiable()) {
        final ProxyShape shape = getShapeProducer().getShape(item);
        final Rectangle2D rect = shape.getShape().getBounds();
        if (y < rect.getCenterY()) {
          break;
        }
        pos++;
      }
      if (pos == -1) {
        pos = 0;
      }
      doAddToEventList(block, identifiers, pos);
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      if (mLine == null) {
        return Collections.emptyList();
      } else {
        final MiscShape shape =
          new GeneralShape(mLine, EditorColor.SELECTCOLOR, null);
        return Collections.singletonList(shape);
      }
    }

    //#######################################################################
    //# Data Members
    private final LabelBlockSubject mLabelBlock;
    private final ProxySubject mClickedLabel;
    private Line2D mLine;

  }


  //#########################################################################
  //# Inner Class InternalDragActionInitial
  private class InternalDragActionInitial
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionInitial(final MouseEvent event)
    {
      super(event);
      mNode = (SimpleNodeSubject) mFocusedObject;
      mSelectedObjects = Collections.singletonList(mFocusedObject);
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        final SimpleNodeSubject node = getNodeCopy();
        final Point2D current = getDragCurrent();
        final Point2D pos = node.getPointGeometry().getPoint();
        final double dx = current.getX() - pos.getX();
        final double dy = current.getY() - pos.getY();
        final double len2 = dx * dx + dy * dy;
        if (len2 < GeometryTools.EPSILON2) {
          return false;
        }
        final Point2D dir = new Point2D.Double(dx, dy);
        node.getInitialArrowGeometry().setPoint(dir);
        return true;
      } else {
        return false;
      }
    }

    void commitSecondaryGraph()
    {
      doReplaceLabelSelection(mNode);
      super.commitSecondaryGraph();
    }

    //#######################################################################
    //# Auxiliary Methods
    private SimpleNodeSubject getNodeCopy()
    {
      if (mNodeCopy == null) {
        mNodeCopy = (SimpleNodeSubject) mSecondaryGraph.getCopy(mNode);
      }
      return mNodeCopy;
    }

    //#######################################################################
    //# Data Members
    private final SimpleNodeSubject mNode;
    private SimpleNodeSubject mNodeCopy;

  }


  //#########################################################################
  //# Inner Class InternalDragActionCreateGroupNode
  private class InternalDragActionCreateGroupNode
    extends InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionCreateGroupNode(final MouseEvent event)
    {
      super(event,
            Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get() ?
            findGrid(event.getPoint()) : event.getPoint());
      mSelectedObjects = Collections.emptyList();
    }

    //#######################################################################
    //# Simple Access
    boolean shouldSnapToGrid()
    {
      return Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get();
    }

    //#######################################################################
    //# Dragging
    void commitDrag(final Point point)
    {
      super.commitDrag(point);
      if (hasDragged()) {
        final Rectangle2D rect = getDragRectangle();
        if (!rect.isEmpty()) {
          doCreateGroupNode(rect);
        }
        repaint();
      }
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      final Rectangle rect = getDragRectangle();
      final MiscShape shape =
        new GeneralShape(rect, EditorColor.SELECTCOLOR, null);
      return Collections.singletonList(shape);
    }

  }


  //#########################################################################
  //# Inner Class InternalDragActionResizeGroupNode
  private class InternalDragActionResizeGroupNode
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionResizeGroupNode(final MouseEvent event,
                                              final Handle handle)
    {
      super(event);
      mGroup = (GroupNodeSubject) mFocusedObject;
      mSelectedObjects = Collections.singletonList(mFocusedObject);
      final Rectangle2D rect = mGroup.getGeometry().getRectangle();
      switch (handle.getType()) {
      case NW:
        mFixedCorner = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = Double.MIN_VALUE;
        break;
      case N:
        mFixedCorner = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
        mFixedX = rect.getMinX();
        mFixedY = Double.MIN_VALUE;
        break;
      case NE:
        mFixedCorner = new Point2D.Double(rect.getMinX(), rect.getMaxY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = Double.MIN_VALUE;
        break;
      case W:
        mFixedCorner = new Point2D.Double(rect.getMaxX(), rect.getMinY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = rect.getMaxY();
        break;
      case E:
        mFixedCorner = new Point2D.Double(rect.getMinX(), rect.getMinY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = rect.getMaxY();
        break;
      case SW:
        mFixedCorner = new Point2D.Double(rect.getMaxX(), rect.getMinY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = Double.MIN_VALUE;
        break;
      case S:
        mFixedCorner = new Point2D.Double(rect.getMaxX(), rect.getMinY());
        mFixedX = rect.getMinX();
        mFixedY = Double.MIN_VALUE;
        break;
      case SE:
        mFixedCorner = new Point2D.Double(rect.getMinX(), rect.getMinY());
        mFixedX = Double.MIN_VALUE;
        mFixedY = Double.MIN_VALUE;
        break;
      default:
        throw new IllegalStateException
          ("Unknown node group handle type: " + handle.getType());
      }
    }

    //#######################################################################
    //# Simple Access
    boolean shouldSnapToGrid()
    {
      return Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get();
    }

    //#######################################################################
    //# Simple Access
    boolean createSecondaryGraph()
    {
      if (super.createSecondaryGraph()) {
        mGroupCopy = (GroupNodeSubject) mSecondaryGraph.getCopy(mGroup);
        mEdges = new LinkedList<EdgeSubject>();
        for (final EdgeSubject edge : getGraph().getEdgesModifiable()) {
          if (edge.getSource() == mGroup || edge.getTarget() == mGroup) {
            mEdges.add(edge);
          }
        }
        return true;
      } else {
        return false;
      }
    }

    void commitSecondaryGraph()
    {
      final Rectangle2D rect = getCurrentRectangle();
      if (rect.isEmpty()) {
        // Delete group node dragged to empty size
        doClearSelection();
        final Command cmd = new DeleteNodeGroupCommand(getGraph(), mGroup);
        mRoot.getUndoInterface().executeCommand(cmd);
      } else {
        doReplaceSelection(mGroup);
        super.commitSecondaryGraph();
      }
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        final Rectangle2D rect = getCurrentRectangle();
        mGroupCopy.getGeometry().setRectangle(rect);
        for (final EdgeSubject edge : mEdges) {
          if (edge.getSource() == mGroup) {
            final PointGeometrySubject geo = edge.getStartPoint();
            if (geo != null) {
              final Point2D old = geo.getPoint();
              final Point2D neo = rescalePoint(old);
              final double dx = neo.getX() - old.getX();
              final double dy = neo.getY() - old.getY();
              mSecondaryGraph.moveEdgeStart(edge, dx, dy);
              mSecondaryGraph.transformEdge(edge, true);
            }
          }
          if (edge.getTarget() == mGroup) {
            final PointGeometrySubject geo = edge.getEndPoint();
            if (geo != null) {
              final Point2D old = geo.getPoint();
              final Point2D neo = rescalePoint(old);
              final double dx = neo.getX() - old.getX();
              final double dy = neo.getY() - old.getY();
              mSecondaryGraph.moveEdgeEnd(edge, dx, dy);
              mSecondaryGraph.transformEdge(edge, false);
            }
          }
          // *** BUG ***
          // What about selfloops?
          // ***
        }
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private Rectangle2D getCurrentRectangle()
    {
      final Rectangle2D rect = new Rectangle2D.Double();
      final Point current = getDragCurrentOnGrid();
      final Point2D point;
      if (mFixedX != Double.MIN_VALUE) {
        point = new Point2D.Double(mFixedX, current.y);
      } else if (mFixedY != Double.MIN_VALUE) {
        point = new Point2D.Double(current.x, mFixedY);
      } else {
        point = new Point2D.Double(current.x, current.y);
      }
      rect.setFrameFromDiagonal(point, mFixedCorner);
      return rect;
    }

    private Point2D rescalePoint(final Point2D point)
    {
      final double x0 = point.getX();
      final double y0 = point.getY();
      final Rectangle2D oldrect = mGroup.getGeometry().getRectangle();
      final double oldx = oldrect.getX();
      final double oldy = oldrect.getY();
      final double oldwidth = oldrect.getWidth();
      final double oldheight = oldrect.getHeight();
      final double relx = 
        oldwidth < GeometryTools.EPSILON ? 0.5 : (x0 - oldx) / oldwidth;
      final double rely = 
        oldheight < GeometryTools.EPSILON ? 0.5 : (y0 - oldy) / oldheight;
      final Rectangle2D newrect = mGroupCopy.getGeometry().getRectangle();
      final double newx = newrect.getX();
      final double newy = newrect.getY();
      final double newwidth = newrect.getWidth();
      final double newheight = newrect.getHeight();
      final double x1 = newx + relx * newwidth;
      final double y1 = newy + rely * newheight;
      return new Point2D.Double(x1, y1);
    }

    //#######################################################################
    //# Data Members
    private final GroupNodeSubject mGroup;
    private final Point2D mFixedCorner;
    private final double mFixedX;
    private final double mFixedY;

    private GroupNodeSubject mGroupCopy;
    private Collection<EdgeSubject> mEdges;
  }


  //#########################################################################
  //# Inner Class InternalDragActionEdge
  /**
   * Drag action to create edges or to drag one of their end points.
   */
  private class InternalDragActionEdge
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    /**
     * Creates a drag action to create a new edge.
     */
    private InternalDragActionEdge(final MouseEvent event)
    {
      super(event);
      mSource = (NodeSubject) mFocusedObject;
      mAnchor = findNodeAnchorPoint(mSource, getDragStart());
      mIsSource = false;
      mOrigEdge = null;
      mCanCreateSelfloop = false;
      mSelectedObjects = Collections.singletonList(mFocusedObject);
      mFocusedObject = null;
    }

    /**
     * Creates a drag action to redirect the redirect the source or
     * target of an edge.
     */
    private InternalDragActionEdge(final MouseEvent event,
                                   final Handle handle)
    {
      super(event);
      mSource = null;
      mAnchor = null;
      mOrigEdge = (EdgeSubject) mFocusedObject;
      mCanCreateSelfloop = true;
      mSelectedObjects = Collections.singletonList(mFocusedObject);
      switch (handle.getType()) {
      case SOURCE:
        mIsSource = true;
        break;
      case TARGET:
        mIsSource = false;
        break;
      default:
        throw new IllegalStateException
          ("Unknown edge handle type: " + handle.getType() + "!");
      }  
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        Point2D current = getDragCurrent();
        if (!mCanCreateSelfloop) {
          final double dist = mAnchor.distanceSq(current);
          if (dist < SELFLOOP_THRESHOLD) {
            return false;
          }
          mCanCreateSelfloop = true;
        }
        createCopiedEdge();
        final NodeSubject focused;
        if (mFocusedObject == null) {
          focused = null;
        } else {
          focused = (NodeSubject) mSecondaryGraph.getCopy(mFocusedObject);
          current = GeometryTools.defaultPosition(focused, current);
        }
        if (mIsSource) {
          final PointGeometrySubject geo = mCopiedEdge.getStartPoint();
          if (geo == null) {
            final PointGeometrySubject newgeo =
              new PointGeometrySubject(current);
            mCopiedEdge.setStartPoint(newgeo);
          } else {
            geo.setPoint(current);
          }
          mCopiedEdge.setSource(focused);
        } else {
          final PointGeometrySubject geo = mCopiedEdge.getEndPoint();
          if (geo == null) {
            final PointGeometrySubject newgeo =
              new PointGeometrySubject(current);
            mCopiedEdge.setEndPoint(newgeo);
          } else {
            geo.setPoint(current);
          }
          mCopiedEdge.setTarget(focused);
        }
        return true;
      } else {
        return false;
      }
    }

    void commitSecondaryGraph()
    {
      final NodeSubject node = (NodeSubject) mFocusedObject;
      final Point2D anchor;
      if (node == null) {
        return;
      } else if (mSource != null) {
        // Creating edge ...
        final Point2D end = findNodeAnchorPoint(node, getDragCurrent());
        doCreateEdge(mSource, node, mAnchor, end);
      } else {
        // Moving edge, maybe ...
        if (node instanceof SimpleNodeSubject) {
          if (mIsSource) {
            if (node == mOrigEdge.getSource()) {
              return;
            }
          } else {
            if (node == mOrigEdge.getTarget()) {
              return;
            }
          }
          anchor = null;
        } else if (node instanceof GroupNodeSubject) {
          final Point current = getDragCurrent();
          anchor = findNodeAnchorPoint(node, current);
          if (mIsSource) {
            if (node == mOrigEdge.getSource() &&
                mOrigEdge.getStartPoint() != null &&
                anchor.equals(mOrigEdge.getStartPoint().getPoint())) {
              return;
            }
          } else {
            if (getGraph().isDeterministic() ||
                node == mOrigEdge.getTarget() && 
                mOrigEdge.getEndPoint() != null &&
                anchor.equals(mOrigEdge.getEndPoint().getPoint())) {
              return;
            }
          }
        } else {
          throw new IllegalStateException
            ("Unknown node type: " + node.getClass().getName() + "!");
        }
        doMoveEdge(mOrigEdge, node, mIsSource, anchor);
      }
    }

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      final int prio;
      if (item instanceof SimpleNodeSubject) {
        prio = 2;
      } else if (item instanceof GroupNodeSubject &&
                 (mIsSource || !getGraph().isDeterministic())) {
        prio = 1;
      } else {
        return -1;
      }
      if (item == mSource && !mCanCreateSelfloop) {
        return -1;
      } else {
        return prio;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    void createCopiedEdge()
    {
      if (mCopiedEdge == null) {
        if (mSource != null) {
          mOrigEdge = new EdgeSubject(mSource, null);
          mCopiedEdge = mSecondaryGraph.addEdge(mOrigEdge);
          mSelectedObjects =
            Collections.singletonList((ProxySubject) mOrigEdge);
        } else { 
          mCopiedEdge = (EdgeSubject) mSecondaryGraph.getCopy(mOrigEdge);
          GeometryTools.createDefaultGeometry(mCopiedEdge);
        }
      }
    }


    //#######################################################################
    //# Data Members
    /**
     * When creating an edge, its source node; otherwise <CODE>null</CODE>.
     */
    private final NodeSubject mSource;
    /**
     * When creating an edge, its start point; otherwise <CODE>null</CODE>.
     */
    private final Point2D mAnchor;
    /**
     * <CODE>true</CODE> when the start point of an edge is being dragged;
     * <CODE>false</CODE> when dragging the end point.
     */
    private final boolean mIsSource;
    /**
     * The edge in the original graph worked upon by this action.
     */
    private EdgeSubject mOrigEdge;
    /**
     * The copy of the edge worked upon in the secondary graph.
     */
    private EdgeSubject mCopiedEdge;
    /**
     * Whether this action can create a selfloop edge. To create a new
     * selfloop, the user must drag away from the source node, and then
     * back onto it. This is to avoid accidental selfloops by erratic
     * clicking.
     */
    private boolean mCanCreateSelfloop;

  }


  //#########################################################################
  //# Inner Class MoveVisitor
  /**
   * Auxiliary class to support moving of a group of objects using the
   * visitor interface. This class also contains the logic to determine
   * whether edges connected to moving nodes are to be moved or transformed.
   */
  private class MoveVisitor extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private MoveVisitor()
    {
      boolean moveNodes = false;
      for (final ProxySubject item : mSelectedObjects) {
        if (item instanceof NodeSubject) {
          moveNodes = true;
          break;
        }
      }
      final Collection<ProxySubject> moved;
      if (moveNodes) {
        final Collection<EdgeSubject> edges = getGraph().getEdgesModifiable();
        mEdgeMap = new HashMap<EdgeProxy,MovingEdge>(edges.size());
        moved = new LinkedList<ProxySubject>(mSelectedObjects);
        for (final EdgeSubject edge : edges) {
          final boolean selected = isSelected(edge);
          if (selected ||
              isSelected(edge.getSource()) ||
              isSelected(edge.getTarget())) {
            final MovingEdge entry = new MovingEdge(edge);
            mEdgeMap.put(edge, entry);
            if (!selected) {
              moved.add(edge);
            }
          }
        }
      } else {
        mEdgeMap = null;
        moved = mSelectedObjects;
      }
      mMovedObjects = Collections.unmodifiableCollection(moved);
    }

    //#######################################################################
    //# Invocation
    private void moveAll(final int dx, final int dy)
    {
      try {
        assert(mSecondaryGraph != null);
        mDeltaX = dx;
        mDeltaY = dy;
        for (final ProxySubject item : mMovedObjects) {
          item.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    private void move(final ProxySubject item, final int dx, final int dy)
    {
      try {
        assert(mSecondaryGraph != null);
        mDeltaX = dx;
        mDeltaY = dy;
        item.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      final EdgeSubject edge0 = (EdgeSubject) edge;
      if (mEdgeMap == null) {
        mSecondaryGraph.moveEdgeHandle(edge0, mDeltaX, mDeltaY);
      } else {
        final MovingEdge entry = mEdgeMap.get(edge0);
        entry.move(mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitGroupNodeProxy(final GroupNodeProxy group)
    {
      final GroupNodeSubject group0 = (GroupNodeSubject) group;
      mSecondaryGraph.moveGroupNode(group0, mDeltaX, mDeltaY);
      return null;
    }

    public Object visitForeachEventProxy(final ForeachEventProxy foreach)
    {
      return null;
    }

    public Object visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      if (!isParentMoved(block)) {
        final GuardActionBlockSubject block0 = (GuardActionBlockSubject) block;
        mSecondaryGraph.moveGuardActionBlock(block0, mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      return null;
    }

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
    {
      if (!isParentMoved(block)) {
        final LabelBlockSubject block0 = (LabelBlockSubject) block;
        mSecondaryGraph.moveLabelBlock(block0, mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitLabelGeometryProxy(final LabelGeometryProxy label)
    {
      if (!isParentMoved(label)) {
        final LabelGeometrySubject label0 = (LabelGeometrySubject) label;
        mSecondaryGraph.moveLabelGeometry(label0, mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final SimpleNodeSubject node0 = (SimpleNodeSubject) node;
      mSecondaryGraph.moveSimpleNode(node0, mDeltaX, mDeltaY);
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean isParentMoved(final Proxy item)
    {
      final Subject subject = (Subject) item;
      final ProxySubject parent = (ProxySubject) subject.getParent();
      if (parent instanceof EdgeProxy) {
        if (mEdgeMap == null) {
          return mSelectedObjects.contains(parent);
        } else {
          return mEdgeMap.containsKey(parent);
        }
      } else if (parent instanceof NodeProxy) {
        return mSelectedObjects.contains(parent);
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<ProxySubject> mMovedObjects;
    private final Map<EdgeProxy,MovingEdge> mEdgeMap;
    private int mDeltaX;
    private int mDeltaY;

  }


  //#########################################################################
  //# Inner Class MovingEdge
  private class MovingEdge
  {
    
    //#######################################################################
    //# Constructor
    private MovingEdge(final EdgeSubject edge)
    {
      mEdge = edge;
      final boolean source = isSelected(edge.getSource());
      final boolean target = isSelected(edge.getTarget());
      if (!source && !target) {
        mType = MovingEdgeType.MOVE_FOLLOW;
      } else if (edge.getGeometry() == null) {
        mType = MovingEdgeType.MOVE_DONT;
      } else if (source && !target) {
        mType = MovingEdgeType.MOVE_SOURCE;
      } else if (!source && target) {
        mType = MovingEdgeType.MOVE_TARGET;
      } else {
        mType = MovingEdgeType.MOVE_FOLLOW;
      }
      mMovingSource = source && edge.getStartPoint() != null;
      mMovingTarget = target && edge.getEndPoint() != null;
    }

    //#######################################################################
    //# Moving
    void move(final double dx, final double dy)
    {
      if (mMovingSource) {
        mSecondaryGraph.moveEdgeStart(mEdge, dx, dy);
      }
      if (mMovingTarget) {
        mSecondaryGraph.moveEdgeEnd(mEdge, dx, dy);
      }
      switch (mType) {
      case MOVE_DONT:
        break;
      case MOVE_FOLLOW:
        mSecondaryGraph.moveEdgeHandle(mEdge, dx, dy);
        break;
      case MOVE_SOURCE:
        mSecondaryGraph.transformEdge(mEdge, true);
        break;
      case MOVE_TARGET:
        mSecondaryGraph.transformEdge(mEdge, false);
        break;
      default:
        throw new IllegalStateException("Unknown move type: " + mType + "!");
      }
    }

    //#######################################################################
    //# Data Members
    private final EdgeSubject mEdge;
    private final MovingEdgeType mType;
    private final boolean mMovingSource;
    private final boolean mMovingTarget;
    
  }


  //#########################################################################
  //# Inner Class MovingEdgeType
  private enum MovingEdgeType {
    MOVE_DONT,
    MOVE_SOURCE,
    MOVE_TARGET,
    MOVE_FOLLOW;
  }


  //#########################################################################
  //# Inner Class HighlightComparator
  private class HighlightComparator
    implements Comparator<ProxySubject>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    public int compare(final ProxySubject item1, final ProxySubject item2)
    {
      final boolean sel1 = isSelected(item1);
      final boolean sel2 = isSelected(item2);
      if (sel1 && !sel2) {
        return 1;
      } else if (!sel1 && sel2) {
        return -1;
      } else {
        final int prio1 = getHighlightPriority(item1);
        final int prio2 = getHighlightPriority(item2);
        return prio1 - prio2;
      }
    }
    
  }


  //#########################################################################
  //# Inner Class KeySpy
  private class KeySpy
    extends KeyAdapter
  {
    public void keyPressed(KeyEvent e)
    {
      //System.err.println(e.getKeyCode());
      if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
          e.getKeyCode() == KeyEvent.VK_DELETE) {
        doDeleteSelected();
      }
      // to be reimplemented
      if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP)
        {
          //System.err.println("UP");
          boolean hasMoved = false;
          CompoundCommand upMove = new CompoundCommand("Move Event");
          for (ProxySubject o : mSelectedObjects)
            {
              if (o instanceof LabelBlockSubject)
                {
                  LabelBlockSubject l = (LabelBlockSubject)o;
                  if (hasSelected(l))
                    {
                      List<AbstractSubject> labels =
                        new ArrayList(l.getEventListModifiable());
                      labels.retainAll(mSelectedObjects);
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
              mRoot.getUndoInterface().executeCommand(upMove);
            }
        }
      if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN)
        {
          //System.err.println("Down");
          boolean hasMoved = false;
          CompoundCommand downMove = new CompoundCommand("Move Event");
          for (ProxySubject o : mSelectedObjects)
            {
              if (o instanceof LabelBlockSubject)
                {
                  LabelBlockSubject l = (LabelBlockSubject)o;
                  if (hasSelected(l))
                    {
                      List<AbstractSubject> labels =
                        new ArrayList(l.getEventListModifiable());
                      labels.retainAll(mSelectedObjects);
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
              mRoot.getUndoInterface().executeCommand(downMove);
            }
        }
    }
  }


  private class NameEditField
    extends JTextField
  {
    private final LabelGeometrySubject mLabel;
    private final SimpleNodeSubject mNode;

    public NameEditField(LabelGeometrySubject label)
    {
      mLabel = label;
      mNode = (SimpleNodeSubject)label.getParent();
      Point p = new Point((int) label.getOffset().getX(),
                          (int) label.getOffset().getY());
      p.translate((int) mNode.getPointGeometry().getPoint().getX(),
                  (int) mNode.getPointGeometry().getPoint().getY());
      setText(mNode.getName());
      setLocation(p);
      setSize(getPreferredSize());
      setBorder(new EmptyBorder(getBorder().getBorderInsets(this)));
      setOpaque(false);
      addFocusListener(new NameEditSpy());
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
              mRoot.getUndoInterface().addUndoable(new UndoableCommand(u));
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
    {
      public void focusLost(FocusEvent e)
      {
        reName();
      }
    }
  }


  //#########################################################################
  //# Inner Class DSListener
  private class DSListener extends DragSourceAdapter
  {
    public void dragOver(DragSourceDragEvent e)
    {
      if (e.getTargetActions() == DnDConstants.ACTION_COPY)
        {
          e.getDragSourceContext().setCursor
            (DragSource.DefaultCopyDrop);
        }
      else
        {
          e.getDragSourceContext().setCursor
            (DragSource.DefaultCopyNoDrop);
        }
    }
  }


  //#########################################################################
  //# Inner Class DGListener
  private class DGListener implements DragGestureListener
  {
    public void dragGestureRecognized(final DragGestureEvent event)
    {
      if (mInternalDragAction instanceof InternalDragActionDND) {
        final InternalDragActionDND action =
          (InternalDragActionDND) mInternalDragAction;
        final List<IdentifierSubject> toBeDragged =
          action.getIdentifiersToBeDragged();
        final Transferable trans =
          new IdentifierTransfer(toBeDragged, EventType.EDGE_EVENTS);
        try {
          event.startDrag(DragSource.DefaultCopyDrop, trans);
        } catch (final InvalidDnDOperationException exception) {
          throw new IllegalArgumentException(exception);
        }
      }
    }
  }

  private Collection<IdentifierSubject> DNDLabel(Point pos)
  {
    // *** BUG ***
    // What about non-identifiers?
    // ***
    Subject parent = null;
    Collection<IdentifierSubject> labels = null;
    for (final ProxySubject item : mSelectedObjects) {
      parent = item.getParent();
      if (parent.getParent() instanceof EventListExpressionSubject &&
          getShapeProducer().getShape(item).getShape().
            getBounds().contains(pos)) {
        labels = new ArrayList<IdentifierSubject>();
        break;
      }
    }
    if (labels != null) {
      for (final ProxySubject item : mSelectedObjects) {
        if (item instanceof IdentifierSubject &&
            item.getParent() == parent) {
          final IdentifierSubject ident = (IdentifierSubject) item;
          labels.add(ident);
        }
      }
    }
    return labels;
  }



  //#########################################################################
  //# Data Members
  private final EditorWindowInterface mRoot;
  private final ControlledToolbar mToolbar;
  private EditorOptions mOptions;

  /**
   * List of currently selected items.
   */
  private Collection<ProxySubject> mSelectedObjects =
    new LinkedList<ProxySubject>();
  /**
   * Set of items not to be drawn, because they are being dragged and
   * displayed through alternative means.
   */
  private final Set<ProxySubject> mDontDraw = new HashSet<ProxySubject>();
  /**
   * Set of items to be highlighted as erroneous.
   */
  private final Set<ProxySubject> mError = new HashSet<ProxySubject>();
  /**
   * The currently highlighted EditorObject (under the mouse pointer).
   */
  private ProxySubject mFocusedObject = null;
  /**
   * The last recorded position of the mouse cursor.
   */
  private Point mCurrentPoint;
  /**
   * A temporary copy of the currently shown graph.  It contains the
   * uncommitted changes while the user is dragging some objects with the
   * mouse in an incomplete move operation. This variable is
   * <CODE>null</CODE> if no move operation is in progress. The secondary
   * graph is created at the start of each move operation, and disposed
   * after committing the changes.
   */
  private EditorGraph mSecondaryGraph = null;
  /**
   * The shape producer associated with the secondary graph.
   * It is non-null if and only if the secondary graph is non-null, i.e.,
   * while a move operation is in progress.
   */
  private SubjectShapeProducer mSecondaryShapeProducer = null;
  /**
   * A flag, indicating that the secondary graph is being committed.
   */
  private boolean mIsCommittingSecondaryGraph = false;
  /**
   * The internal dragging operation currently in progress,
   * or <CODE>null</CODE>.
   */
  private InternalDragAction mInternalDragAction;
  /**
   * The current spring embedder, if any is running.
   */
  private SpringEmbedder mEmbedder;

  private ToolController mController;
  private ToolController mSelectController;
  private ToolController mNodeController;
  private ToolController mGroupNodeController;
  private ToolController mEdgeController;
  private ToolController mEmbedderController;

  private DragSource mExternalDragSource;
  private final DragGestureListener mDGListener;
  private int mExternalDragAction = DnDConstants.ACTION_COPY;
  private DRAGOVERSTATUS mExternalDragStatus = DRAGOVERSTATUS.NOTDRAG;

  private final HighlightComparator mComparator = new HighlightComparator();


  //#########################################################################
  //# Class Constants
  private static final DataFlavor FLAVOUR =
    new DataFlavor(IdentifierWithKind.class, "IdentifierWithKind");

  private static final int SELFLOOP_AUX_RADIUS =
    SimpleNodeProxyShape.RADIUS + 2;
  private static final double SELFLOOP_THRESHOLD =
    SELFLOOP_AUX_RADIUS * SELFLOOP_AUX_RADIUS;

}
