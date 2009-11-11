//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.ReorganizeListCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.renderer.GeneralShape;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.GeometryTools;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderObserver;
import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.gui.transfer.GraphTransferable;
import net.sourceforge.waters.gui.transfer.GuardActionBlockTransferable;
import net.sourceforge.waters.gui.transfer.IdentifierTransferable;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.ProxyTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.plain.module.GraphElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachEventSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public class ControlledSurface
  extends EditorSurface
  implements SelectionOwner, Observer, EmbedderObserver,
             FocusListener, DragGestureListener,
             SupremicaPropertyChangeListener
{
  //#########################################################################
  //# Constructors
  public ControlledSurface(final GraphSubject graph,
                           final ModuleSubject module,
                           final EditorWindowInterface root,
                           final ControlledToolbar toolbar,
                           final WatersPopupActionManager manager)
    throws GeometryAbsentException
  {
    super(graph, module, new SubjectShapeProducer(graph, module));
    mRoot = root;
    mToolbar = toolbar;
    mPopupFactory = manager == null ? null : new GraphPopupFactory(manager);
    setFocusable(true);
    final DropTargetListener dtListener = new DTListener();
    new DropTarget(this, dtListener);
    addKeyListener(new KeySpy());
    updateTool();

    final SpringEmbedder embedder = new SpringEmbedder(graph);
    final boolean runEmbedder = embedder.setUpGeometry();
    if (runEmbedder) {
      runEmbedder(false);
    }
    mHasGroupNodes = GraphTools.updateGroupNodeHierarchy(graph);
    mNeedsHierarchyUpdate = false;
    mSizeMayHaveChanged = true;

    mIsPermanentFocusOwner = false;
    addFocusListener(this);
    graph.addModelObserver(mGraphModelObserver);
    module.getEventDeclListModifiable().addModelObserver
      (mEventDeclListModelObserver);
    if (root != null) {
      if (toolbar != null) {
        toolbar.attach(this);
      }
      final UndoInterface undoer = root.getUndoInterface();
      undoer.attach(this);
    }
  }

  /**
   * Creates an immutable controlled surface.
   */
  public ControlledSurface(final GraphSubject graph,
                           final ModuleSubject module)
    throws GeometryAbsentException
  {
    this(graph, module, null, null, null);
  }


  //#########################################################################
  //# Simple Access
  public UndoInterface getUndoInterface()
  {
    return mRoot.getUndoInterface();
  }

  public EditorWindowInterface getEditorInterface()
  {
    return mRoot;
  }

  public ModuleSubject getModule()
  {
    return (ModuleSubject) super.getModule();
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
      return super.getGraph();
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

  /**
   * Returns the position where items can be pasted in this panel.
   * This is either the current position of the mouse cursor,
   * or the center of the viewport, if the cursor is not within the
   * window.
   */
  public Point getPastePosition()
  {
    final Point point;
    if (mCurrentPoint == null) {
      final Rectangle rect = getVisibleRect();
      final int x = rect.x + (rect.width >> 1);
      final int y = rect.y + (rect.height >> 1);
      point = new Point(x, y);
    } else {
      point = mCurrentPoint;
    }
    return findGrid(point);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  public UndoInterface getUndoInterface(final Action action)
  {
    return getUndoInterface();
  }

  public boolean hasNonEmptySelection()
  {
    return !mSelectedList.isEmpty();
  }

  public boolean canSelectMore()
  {
    final GraphSubject graph = getGraph();
    final LabelBlockSubject blocked = graph.getBlockedEvents();
    if (blocked != null && !isSelected(blocked)) {
      return true;
    }
    for (final NodeSubject node : graph.getNodesModifiable()) {
      if (!isSelected(node)) {
        return true;
      }
    }
    for (final EdgeSubject edge : graph.getEdgesModifiable()) {
      if (!isSelected(edge)) {
        return true;
      }
    }
    return false;
  }

  public boolean isSelected(final Proxy proxy)
  {
    return mSelectedSet.contains(proxy);
  }

  public List<ProxySubject> getCurrentSelection()
  {
    return new ArrayList<ProxySubject>(mSelectedList);
  }

  public List<ProxySubject> getAllSelectableItems()
  {
    final GraphSubject graph = getGraph();
    final LabelBlockSubject blocked = graph.getBlockedEvents();
    final Collection<NodeSubject> nodes = graph.getNodesModifiable();
    final Collection<EdgeSubject> edges = graph.getEdgesModifiable();
    final int size = (blocked == null ? 0 : 1) + nodes.size() + edges.size();
    final List<ProxySubject> result = new ArrayList<ProxySubject>(size);
    if (blocked != null) {
      result.add(blocked);
    }
    result.addAll(nodes);
    result.addAll(edges);
    return result;
  }

  public ProxySubject getSelectionAnchor()
  {
    if (mSelectedList.size() == 1) {
      return mSelectedList.iterator().next();
    } else {
      return null;
    }
  }

  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  public Proxy getSelectableAncestor(final Proxy item)
  {
    return mSelectableVisitor.getSelectableAncestor(item);
  }

  public void replaceSelection(final List<? extends Proxy> items)
  {

    if (!mSelectedList.equals(items)) {
      mSelectedList.clear();
      mSelectedSet.clear();
      mSelectableVisitor.addToSelectionList(items);
      fireSelectionChanged();
    }
  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    if (mSelectableVisitor.addToSelectionList(items)) {
      fireSelectionChanged();
    }
  }

  public void removeFromSelection(final List<? extends Proxy> items)
  {
    boolean change = false;
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      if (mSelectedSet.remove(subject)) {
        mSelectedList.remove(subject);
        change = true;
      }
      if (subject instanceof LabelBlockSubject) {
        final LabelBlockSubject block = (LabelBlockSubject) subject;
        for (final Proxy child : block.getEventListModifiable()) {
          if (mSelectedSet.remove(child)) {
            mSelectedList.remove(child);
            change = true;
          }
        }
      }
    }
    if (change) {
      fireSelectionChanged();
    }
  }

  public GraphInsertPosition getInsertPosition(final Proxy item)
  {
    final GraphSubject graph = getGraph();
    return new GraphInsertPosition(graph, null);
  }

  public void insertCreatedItem(final Proxy proxy, final Object insobj)
  {
    throw new UnsupportedOperationException
      ("ControlledSurface does not support generic insert!");
  }

  public boolean canCopy(final List<? extends Proxy> items)
  {
    final DataFlavor flavor = mDataFlavorVisitor.getDataFlavor(items);
    return flavor != null;
  }

  public Transferable createTransferable(final List<? extends Proxy> items)
  {
    final DataFlavor flavor = mDataFlavorVisitor.getDataFlavor(items);
    if (flavor == null) {
      throw new IllegalArgumentException("No data flavour found!");
    } else if (WatersDataFlavor.GRAPH.equals(flavor)) {
      return mGraphTransferableVisitor.createTransferable(items);
    } else if (WatersDataFlavor.IDENTIFIER_LIST.equals(flavor)) {
      return mIdentifierListTransferableVisitor.createTransferable(items);
    } else if (WatersDataFlavor.GUARD_ACTION_BLOCK.equals(flavor)) {
      final GuardActionBlockProxy block =
        findFirst(items, GuardActionBlockProxy.class);
      return new GuardActionBlockTransferable(block);
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      // *** BUG *** Parse it also !!!
      final LabelGeometrySubject geo =
        findFirst(items, LabelGeometrySubject.class);
      final NodeSubject node = (NodeSubject) geo.getParent();
      final String text = node.getName();
      return new StringSelection(text);
    } else {
      throw new IllegalArgumentException
        ("Unexpected data flavour: " + flavor.getHumanPresentableName() + "!");
    }
  }

  public boolean canPaste(final Transferable transferable)
  {
    if (transferable.isDataFlavorSupported(WatersDataFlavor.GRAPH)) {
      return true;
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.IDENTIFIER_LIST)) {
      final Proxy target = getPasteTarget();
      return mIdentifierPasteVisitor.canPaste(target, transferable);
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.GUARD_ACTION_BLOCK)) {
      final Proxy target = getPasteTarget();
      return
        target != null &&
        (target instanceof EdgeProxy ||
         target instanceof GuardActionBlockProxy);
    } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      final Proxy target = getPasteTarget();
      return target instanceof NodeProxy;      
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public List<InsertInfo> getInsertInfo(Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    if (transferable.isDataFlavorSupported(WatersDataFlavor.GRAPH)) {
      final GraphSubject graph = getGraph();
      final List<Proxy> list =
        (List<Proxy>) transferable.getTransferData(WatersDataFlavor.GRAPH);
      final Proxy data = list.iterator().next();
      final GraphSubject newgraph = (GraphSubject) cloner.getClone(data);
      final LabelBlockSubject newblocked = newgraph.getBlockedEvents();
      final Collection<NodeSubject> newnodes = newgraph.getNodesModifiable();
      final Collection<EdgeSubject> newedges = newgraph.getEdgesModifiable();
      final Point2D newpos =
        GeometryTools.getTopLeftPosition(newblocked, newnodes);
      final Point2D pastepos = getPastePosition();
      final double dx = pastepos.getX() - newpos.getX();
      final double dy = pastepos.getY() - newpos.getY();
      final Point2D delta = new Point2D.Double(dx, dy);
      if (newblocked != null) {
        newgraph.setBlockedEvents(null);
        final LabelBlockSubject blocked = graph.getBlockedEvents();
        if (blocked == null) {
          GeometryTools.translate(newblocked, delta);
          addInsertInfo(inserts, newblocked, graph);
        } else {
          final ListSubject<? extends ProxySubject> eventlist =
            blocked.getEventListModifiable();
          final ListSubject<? extends ProxySubject> neweventlist =
            newblocked.getEventListModifiable();
          int pos = eventlist.size();
          for (final ProxySubject newident : neweventlist) {
            addInsertInfo(inserts, newident, eventlist, pos++);
          }
          neweventlist.clear();
        }
      }
      final IndexedSetSubject<NodeSubject> existing =
        graph.getNodesModifiable();
      final Set<String> newnames = new HashSet<String>(newnodes.size());
      for (final NodeSubject newnode : newnodes) {
        String name = newnode.getName();
        if (existing.containsName(name) || newnames.contains(name)) {
          name = GraphTools.findNewSimpleNodeName(graph, newnames);
          newnode.setName(name);
        }
        newnames.add(name);
        GeometryTools.translate(newnode, delta);
        addInsertInfo(inserts, newnode, graph);
      }
      newnodes.clear();
      for (final EdgeSubject newedge : newedges) {
        GeometryTools.translate(newedge, delta);
        addInsertInfo(inserts, newedge, graph);
      }
      newedges.clear();
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.IDENTIFIER_LIST)) {
      final ProxySubject target = getPasteTarget();
      mIdentifierPasteVisitor.addInsertInfo(target, -1, transferable, inserts);
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.GUARD_ACTION_BLOCK)) {
      final List<Proxy> list =
        (List<Proxy>) transferable.getTransferData(WatersDataFlavor.GRAPH);
      final Proxy data = list.iterator().next();
      final GuardActionBlockSubject newblock =
        (GuardActionBlockSubject) cloner.getClone(data);
      final ProxySubject target = getPasteTarget();
      if (target instanceof EdgeSubject) {
        final EdgeSubject edge = (EdgeSubject) target;
        final GuardActionBlockSubject oldvalue = edge.getGuardActionBlock();
        addInsertInfo(inserts, newblock, edge, oldvalue);
      } else if (target instanceof GuardActionBlockSubject) {
        final EdgeSubject edge = (EdgeSubject) target.getParent();
        addInsertInfo(inserts, newblock, edge, target);
      } else {
        throw new IllegalStateException
          ("Unexpected target for guard/action block paste: " +
           target.getClass().getName() + "1");
      }
    } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      final String data = 
        (String) transferable.getTransferData(DataFlavor.stringFlavor);
      final SimpleIdentifierElement ident = new SimpleIdentifierElement(data);
      final NodeSubject node = (NodeSubject) getPasteTarget();
      final String oldname = node.getName();
      final SimpleIdentifierElement oldident =
        new SimpleIdentifierElement(oldname);
      addInsertInfo(inserts, ident, node, oldident);
    } else {
      throw new UnsupportedFlavorException(null);
    }
    return inserts; 
  }

  public boolean canDelete(final List<? extends Proxy> items)
  {
    for (final Proxy proxy : items) {
      if (!(proxy instanceof LabelGeometryProxy)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final GraphSubject graph = getGraph();
    final Map<ListSubject<AbstractSubject>,Boolean> eventlists =
      new IdentityHashMap<ListSubject<AbstractSubject>,Boolean>();
    // Are there any event labels?
    for (final Proxy proxy : items) {
      final ProxySubject subject = (ProxySubject) proxy;
      if (subject.getParent().getParent() instanceof LabelBlockSubject) {
        final ListSubject<AbstractSubject> eventlist =
          (ListSubject<AbstractSubject>) subject.getParent();
        eventlists.put(eventlist, true);
      }
    }
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    final Set<Proxy> lookup = new HashSet<Proxy>(items);
    if (!eventlists.isEmpty()) {
      // Deleting event labels: visit all label blocks, and add deleted items
      // in order of appearance to undo list ...
      for (final ListSubject<AbstractSubject> eventlist :
             eventlists.keySet()) {
        int pos = 0;
        for (final AbstractSubject ident : eventlist) {
          if (lookup.contains(ident)) {
            addInsertInfo(inserts, ident, eventlist, pos);
          }
          pos++;
        }
      }
    } else {
      // Not deleting event labels: then delete everything except node labels
      // First find any edges to be deleted ...
      for (final EdgeSubject edge : graph.getEdgesModifiable()) {
        if (lookup.contains(edge) ||
            lookup.contains(edge.getSource()) ||
            lookup.contains(edge.getTarget())) {
          addInsertInfo(inserts, edge, graph);
          lookup.add(edge);
        }
      }
      // Now for the other stuff ...
      for (final Proxy proxy : items) {
        final ProxySubject subject = (ProxySubject) proxy;
        if (subject instanceof NodeSubject) {
          addInsertInfo(inserts, subject, graph);
        } else if (subject instanceof LabelBlockSubject) {
          final Subject parent = subject.getParent();
          if (parent == graph) {
            addInsertInfo(inserts, subject, graph);
          } else if (!lookup.contains(parent)) {
            final LabelBlockSubject block = (LabelBlockSubject) subject;
            final EdgeSubject edge = (EdgeSubject) parent;
            final List<AbstractSubject> elist = block.getEventListModifiable();
            final List<AbstractSubject> clonedlist =
              new ArrayList<AbstractSubject>(elist);
            addInsertInfo(inserts, block, edge, clonedlist);
          }
        } else if (subject instanceof GuardActionBlockSubject) {
          final Subject parent = subject.getParent();
          if (!lookup.contains(parent)) {
            addInsertInfo(inserts, subject, (ProxySubject) parent);
          }
        }
      }
    }
    return inserts;
  }

  @SuppressWarnings("unchecked")
  public void insertItems(List<InsertInfo> inserts)
  {
    final GraphSubject graph = getGraph();
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      if (proxy instanceof NodeSubject) {
        final NodeSubject node = (NodeSubject) proxy;
        final Collection<NodeSubject> nodes = graph.getNodesModifiable();
        nodes.add(node);
      } else if (proxy instanceof EdgeSubject) {
        final EdgeSubject edge = (EdgeSubject) proxy;
        final Collection<EdgeSubject> edges = graph.getEdgesModifiable();
        edges.add(edge);
      } else if (proxy instanceof IdentifierSubject ||
                 proxy instanceof ForeachEventSubject) {
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<Proxy> eventlist = Casting.toList(inspos.getList());
        final int pos = inspos.getPosition();
        eventlist.add(pos, proxy);
      } else if (proxy instanceof GuardActionBlockSubject) {
        final GuardActionBlockSubject block = (GuardActionBlockSubject) proxy;
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        final EdgeSubject edge = (EdgeSubject) inspos.getParent();
        edge.setGuardActionBlock(block);
      } else if (proxy instanceof LabelBlockSubject) {
        final LabelBlockSubject block = (LabelBlockSubject) proxy;
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        if (inspos.getParent() == graph) {
          graph.setBlockedEvents(block);
        } else {
          final List<AbstractSubject> list = block.getEventListModifiable();
          final List<AbstractSubject> clonedlist =
            (List<AbstractSubject>) inspos.getOldValue();
          list.addAll(clonedlist);
        }
      } else if (proxy instanceof SimpleIdentifierElement) {
        final SimpleIdentifierProxy ident = (SimpleIdentifierProxy) proxy;
        final String name = ident.getName();
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        final NodeSubject node = (NodeSubject) inspos.getParent();
        node.setName(name);        
      }
    }
  }

  public void deleteItems(List<InsertInfo> inserts)
  {
    final GraphSubject graph = getGraph();
    final Collection<NodeSubject> nodes = graph.getNodesModifiable();
    final Collection<EdgeSubject> edges = graph.getEdgesModifiable();
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      if (proxy instanceof NodeSubject) {
        nodes.remove(proxy);
      } else if (proxy instanceof EdgeSubject) {
        edges.remove(proxy);
      } else if (proxy instanceof IdentifierSubject ||
                 proxy instanceof ForeachEventSubject) {
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<? extends ProxySubject> eventlist = inspos.getList();
        eventlist.remove(proxy);
      } else if (proxy instanceof GuardActionBlockSubject) {
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        final EdgeSubject edge = (EdgeSubject) inspos.getParent();
        final GuardActionBlockSubject oldvalue =
          (GuardActionBlockSubject) inspos.getOldValue();
        edge.setGuardActionBlock(oldvalue);
      } else if (proxy instanceof LabelBlockSubject) {
        final LabelBlockSubject block = (LabelBlockSubject) proxy;
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        if (inspos.getParent() == graph) {
          graph.setBlockedEvents(null);
        } else {
          final List<AbstractSubject> list = block.getEventListModifiable();
          list.clear();
        }
      } else if (proxy instanceof SimpleIdentifierElement) {
        final GraphInsertPosition inspos =
          (GraphInsertPosition) insert.getInsertPosition();
        final NodeSubject node = (NodeSubject) inspos.getParent();
        final SimpleIdentifierProxy oldvalue =
          (SimpleIdentifierProxy) inspos.getOldValue();
        final String oldname = oldvalue.getName();
        node.setName(oldname);        
      }
    }
  }

  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (!list.isEmpty()) {
      // Beware---the list may contain identifiers, but the proxy
      // shape producer does not support them :-(
      final Set<Proxy> scrollable = new HashSet<Proxy>();
      for (final Proxy proxy : list) {
        if (proxy instanceof IdentifierSubject ||
            proxy instanceof ForeachEventSubject) {
          final AbstractSubject subject = (AbstractSubject) proxy;
          final ProxySubject parent = 
            (ProxySubject) subject.getAncestor(LabelBlockSubject.class,
                                               SimpleNodeSubject.class);
          scrollable.add(parent);
        } else {
          scrollable.add(proxy);
        }
      }
      final Rectangle2D bounds =
        getShapeProducer().getMinimumBoundingRectangle(scrollable);
      final int x = (int) Math.floor(bounds.getX());
      final int width =
        (int) Math.ceil(bounds.getX() + bounds.getWidth()) - x;
      final int y = (int) Math.floor(bounds.getY());
      final int height =
        (int) Math.ceil(bounds.getY() + bounds.getHeight()) - y;
      final Rectangle rect = new Rectangle(x, y, width, height);
      scrollRectToVisible(rect);
    }
  }

  public void activate()
  {
    if (!isFocusOwner()) {
      try {
        final GraphSubject graph = getGraph();
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) graph.getParent();
        mRoot.getModuleWindowInterface().showEditor(comp);
        requestFocusInWindow();
      } catch (final GeometryAbsentException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }

  public void close()
  {
    final ProxyShapeProducer shaper = getShapeProducer();
    shaper.close();
    final GraphSubject graph = getGraph();
    graph.removeModelObserver(mGraphModelObserver);
    final ModuleSubject module = getModule();
    module.getEventDeclListModifiable().removeModelObserver
      (mEventDeclListModelObserver);
    if (mToolbar != null) {
      mToolbar.detach(this);
    }
    if (mRoot != null) {
      final UndoInterface undoer = getUndoInterface();
      undoer.detach(this);
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  public void attach(final Observer observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<Observer>();
    }
    mObservers.add(observer);
  }

  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
    if (mObservers.isEmpty()) {
      mObservers = null;
    }
  }
  
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    if (mObservers != null) {
      // Just in case they try to register or deregister observers
      // in response to the update ...
      final List<Observer> copy = new LinkedList<Observer>(mObservers);
      for (final Observer observer : copy) {
        observer.update(event);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.Observer
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case TOOL_SWITCH:
      if (mEmbedder == null) {
        updateTool();
      }
      break;
    case UNDOREDO:
      updateGroupNodeHierarchy();
      break;
    default:
      break;
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
      commitSecondaryGraph("Automatic Layout", false, mIsEmbedderUndoable);
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
    checkGroupNodeHierarchyUpdate(event);
    updateError();
    mController.updateHighlighting();
    repaint();
    mSizeMayHaveChanged = true;
  }


  //#########################################################################
  //# Interface java.awt.event.FocusListener
  public void focusGained(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = true;
      repaint();
    }
  }

  public void focusLost(final FocusEvent event)
  {
    if (!event.isTemporary()) {
      mIsPermanentFocusOwner = false;
      repaint();
    }
  }


  //#########################################################################
  //# Interface java.awt.dnd.DragGestureListener
  public void dragGestureRecognized(final DragGestureEvent event)
  {
    if (mInternalDragAction instanceof InternalDragActionDND) {
      final InternalDragActionDND action =
        (InternalDragActionDND) mInternalDragAction;
      final List<IdentifierSubject> toBeDragged =
        action.getIdentifiersToBeDragged();
      final Transferable trans = new IdentifierTransferable(toBeDragged);
      try {
        event.startDrag(DragSource.DefaultCopyDrop, trans);
      } catch (final InvalidDnDOperationException exception) {
        throw new IllegalArgumentException(exception);
      }
    }
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
  {
    getShapeProducer().clear();
    repaint();
  }


  //#########################################################################
  //# Repainting
  protected void paintComponent(final Graphics graphics)
  {
    super.paintComponent(graphics);
    if (mInternalDragAction == null) {
      adjustSize();
    }
  }

  void adjustSize()
  {
    if (mSizeMayHaveChanged) {
      mSizeMayHaveChanged = false;
      final Dimension dim = calculatePreferredSize();
      if (!dim.equals(getPreferredSize())) {
        setPreferredSize(dim);
        revalidate();
      }
    }
  }

  Dimension calculatePreferredSize()
  {
    final Rectangle2D area = getShapeProducer().getMinimumBoundingRectangle();
    final int width = (int) Math.ceil(area.getWidth());
    final int height = (int) Math.ceil(area.getHeight());
    final int extra = Config.GUI_EDITOR_GRID_SIZE.get() * 10;
    final int x = width + extra;
    final int y = height + extra;
    return new Dimension(x, y);
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
      final DragSource source = DragSource.getDefaultDragSource();
      source.createDefaultDragGestureRecognizer
        (this, DnDConstants.ACTION_COPY, this);
      mController.installed();
    }
  }


  //#########################################################################
  //# State Renaming
  private void editStateName(final SimpleNodeSubject node)
  {
    final String name = node.getName();
    final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(name);
    new StateNameInputCell(node, ident);
  }


  //#########################################################################
  //# Smart Creation Commands
  /**
   * Creates a simple node and selects it.
   * This method creates and executes an {@link InsertCommand} for a simple
   * node on this panel.
   * @param  pos        the coordinate of the new node.
   */
  void doCreateSimpleNode(final Point2D pos)
  {
    final GraphSubject graph = getGraph();
    final SimpleNodeSubject node = GraphTools.getCreatedSimpleNode(graph, pos);
    final Command cmd = new InsertCommand(node, this);
    getUndoInterface().executeCommand(cmd);
  }

  /**
   * Creates a group node and selects it.
   * This method creates and executes an {@link InsertCommand} for a group
   * node on this panel.
   * @param  rect     the geometry of the new group node.
   */
  void doCreateGroupNode(final Rectangle2D rect)
  {
    final GraphSubject graph = getGraph();
    final GroupNodeSubject group = GraphTools.getCreatedGroupNode(graph, rect);
    final Command cmd = new InsertCommand(group, this);
    getUndoInterface().executeCommand(cmd);
  }

  /**
   * Creates an edge and selects it.
   * This method creates and executes an {@link InsertCommand} for an edge
   * on this panel.
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
    final GraphSubject graph = getGraph();
    final EdgeSubject edge =
      GraphTools.getCreatedEdge(graph, source, target, start, end);
    final Command cmd = new InsertCommand(edge, this);
    getUndoInterface().executeCommand(cmd);
  }

  public void runEmbedder(final boolean undoable)
  {
    mIsEmbedderUndoable = undoable;
    createSecondaryGraph();
    final SimpleComponentSubject comp =
      (SimpleComponentSubject) getGraph().getParent();
    final String name = comp == null ? "graph" : comp.getName();
    final long timeout = Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.get();
    mEmbedder = new SpringEmbedder(mSecondaryGraph.getNodesModifiable(),
                                   mSecondaryGraph.getEdgesModifiable());
    mEmbedder.addObserver(this);
    final Thread thread = new Thread(mEmbedder);
    final JDialog dialog =
      new SpringAbortDialog(mRoot.getFrame(), name, mEmbedder, timeout);
    dialog.setLocationRelativeTo(mRoot.getFrame());
    dialog.setVisible(true);
    thread.start();
  }


  //#########################################################################
  //# Auxiliary Methods for Selection Handling
  /**
   * Clears the selection.
   */
  private void clearSelection()
  {
    if (!mSelectedList.isEmpty()) {
      mSelectedList.clear();
      mSelectedSet.clear();
      fireSelectionChanged();
    }
  }

  /**
   * Replaces the selection.
   * This method ensures that only the given item is selected.
   */
  private void replaceSelection(final ProxySubject item)
  {
    if (mSelectedList.size() != 1 || !isSelected(item)) {
      mSelectedList.clear();
      mSelectedSet.clear();
      mSelectedList.add(item);
      mSelectedSet.add(item);
      fireSelectionChanged();
    }
  }

  /**
   * Adds an item to the selection.
   */
  private void addToSelection(final ProxySubject item)
  {
    if (mSelectedSet.add(item)) {
      mSelectedList.add(item);
      fireSelectionChanged();
    }
  }

  /**
   * Removes an item from the selection.
   */
  private void removeFromSelection(final ProxySubject item)
  {
    if (isSelected(item)) {
      final List<ProxySubject> list = Collections.singletonList(item);
      removeFromSelection(list);
    }
  }

  private void replaceLabelSelection(final ProxySubject item)
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
      removeFromSelection(victims);
      addToSelection(item);
    } else {
      replaceSelection(item);
    }
  }

  /**
   * Toggles the selection of an item.
   * This method selects the given item if it is not yet selected,
   * and unselects it if selected.
   */
  private void toggleSelection(final ProxySubject item)
  {
    if (isSelected(item)) {
      removeFromSelection(item);
    } else {
      addToSelection(item);
    }
  }


  //#########################################################################
  //# Low-level Selection Handling
  private void fireSelectionChanged()
  {
    if (mInternalDragAction == null) {
      final EditorChangedEvent event = new SelectionChangedEvent(this);
      fireEditorChangedEvent(event);
    }
    repaint();
  }


  //#########################################################################
  //# Rendering Hints
  public ProxySubject getOriginal(final ProxySubject item)
  {
    assert(item != null);
    if (mSecondaryGraph == null) {
      return item;
    } else {
      return mSecondaryGraph.getOriginal(item);
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

  private boolean hasSelected(final EventListExpressionSubject expr)
  {
    for (final Proxy proxy : expr.getEventList()) {
      if (isSelected(proxy)) {
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

  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    final ProxySubject item = (ProxySubject) proxy;
    final boolean focused = isRenderedFocused(item);
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
    int priority = getPriority(item);
    if (mDontDraw.contains(item)) {
      priority = -1;
    } else if (selected) {
      priority += 6;
    }
    final DRAGOVERSTATUS dragover;
    if (focused && mInternalDragAction != null) {
      dragover = mInternalDragAction.getExternalDragStatus();
    } else {
      dragover = DRAGOVERSTATUS.NOTDRAG;
    }
    return new RenderingInformation
      (showHandles, focused,
       EditorColor.getColor(item, dragover, selected,
                            error, mIsPermanentFocusOwner),
       EditorColor.getShadowColor(item, dragover, selected,
                                  error, mIsPermanentFocusOwner),
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
  //# Group Node Hierarchy
  private void checkGroupNodeHierarchyUpdate(final ModelChangeEvent event)
  {
    final GraphSubject graph = getGraph();
    final Set<NodeSubject> nodes = graph.getNodesModifiable();
    final Subject source = event.getSource();
    switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        if (source == nodes) {
          final Object value = event.getValue();
          if (mHasGroupNodes || value instanceof GroupNodeSubject) {
            mNeedsHierarchyUpdate = true;
          }
        }
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        if (source == nodes) {
          mNeedsHierarchyUpdate |= mHasGroupNodes;
        }
        break;
      case ModelChangeEvent.GEOMETRY_CHANGED:
        if (source instanceof NodeSubject) {
          mNeedsHierarchyUpdate |= mHasGroupNodes;
        }
        break;
    default:
      break;
    }    
  }

  private void updateGroupNodeHierarchy()
  {
    if (mNeedsHierarchyUpdate) {
      final GraphSubject graph = getGraph();
      mHasGroupNodes = GraphTools.updateGroupNodeHierarchy(graph);
      mNeedsHierarchyUpdate = false;
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

  /**
   * Gets the item that should receive the data of a paste operation.
   * This is either the edited graph, if the selection is empty, or the
   * single selected item, if the selection contains only one element.
   */
  private ProxySubject getPasteTarget()
  {
    if (hasNonEmptySelection()) {
      return getSelectionAnchor();
    } else {
      return getGraph();
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
      mSecondaryGraph.addModelObserver(mGraphModelObserver);
      return true;
    } else {
      return false;
    }
  }

  private void clearSecondaryGraph()
  {
    if (mSecondaryGraph != null) {
      mSecondaryGraph.removeModelObserver(mGraphModelObserver);
      mSecondaryGraph = null;
      mSecondaryShapeProducer = null;
      repaint();
    }
  }

  private void commitSecondaryGraph(final String description,
                                    final boolean selecting,
                                    final boolean undoable)
  {
    if (mSecondaryGraph != null) {
      final Command cmd =
        mSecondaryGraph.createUpdateCommand(this, description, selecting);
      if (cmd == null) {
        // ignore
      } else if (undoable) {
        getUndoInterface().executeCommand(cmd);
      } else {
        cmd.execute();
      }
    }
  }


  //#########################################################################
  //# Data Transfer Auxiliaries
  private void addInsertInfo(final List<InsertInfo> inserts,
                             final Proxy item,
                             final ProxySubject parent)
  {
    addInsertInfo(inserts, item, parent, null);
  }

  private void addInsertInfo(final List<InsertInfo> inserts,
                             final Proxy item,
                             final ProxySubject parent,
                             final Object oldvalue)
  {
    final GraphInsertPosition inspos =
      new GraphInsertPosition(parent, oldvalue);
    final InsertInfo insert = new InsertInfo(item, inspos);
    inserts.add(insert);
  }

  private void addInsertInfo(final List<InsertInfo> inserts,
                             final Proxy item,
                             final ListSubject<? extends ProxySubject> list,
                             final int pos)
  {
    final ListInsertPosition inspos = new ListInsertPosition(list, pos);
    final InsertInfo insert = new InsertInfo(item, inspos);
    inserts.add(insert);
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static <T extends Proxy>
    T findFirst(final List<? extends Proxy> items,
                final Class<T> clazz)
  {
    for (final Proxy proxy : items) {
      final Class<? extends Proxy> pclazz = proxy.getClass();
      if (clazz.isAssignableFrom(pclazz)) {
        return clazz.cast(proxy);
      }
    }
    return null;
  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class GraphModelObserver implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      checkGroupNodeHierarchyUpdate(event);
      updateError();
      mController.updateHighlighting();
      repaint();
      mSizeMayHaveChanged = true;
    }

  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class EventDeclListModelObserver implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      if (event.getKind() == ModelChangeEvent.GEOMETRY_CHANGED) {
        repaint();
      }
    }

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
      fireSelectionChanged();
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
      final ProxySubject object;
      if (mCurrentPoint == null) {
        object = null;
      } else {
        final Collection<ProxySubject> objects =
          getFocusableObjectsAtPosition(mCurrentPoint);
        if (objects.isEmpty()) {
          object = null;
        } else {
          object = Collections.max(objects, mComparator);
        }
      }
      if (object != mFocusedObject) {
        mFocusedObject = object;
        repaint();
      }
    }

    /**
     * Stops any external drag-and-drop.
     * There is no notification if an external drag-and-drop is cancelled,
     * but we know for sure that it is finished when we receive normal mouse
     * events again. Therefore, this method is called first by certain mouse
     * listeners.
     */
    void abortExternalDrag(final MouseEvent event)
    {
      if (mInternalDragAction != null &&
          mInternalDragAction instanceof InternalDragActionDND) {
        final Point point = event.getPoint();
        mInternalDragAction.cancelDrag(point);
        mInternalDragAction = null;
        repaint();
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
      mPopupFactory.maybeShowPopup
        (ControlledSurface.this, event, mFocusedObject);
    }

    public void mouseReleased(final MouseEvent event)
    {
      mPopupFactory.maybeShowPopup
        (ControlledSurface.this, event, mFocusedObject);
      if (mInternalDragAction != null) {
        final Point point = event.getPoint();
        if (mInternalDragAction.hasDragged()) {
          mInternalDragAction.commitDrag(point);
        } else {
          mInternalDragAction.cancelDrag(point);
        }
        mInternalDragAction = null;
        fireSelectionChanged();
        final Rectangle rect = getVisibleRect();
        if (rect.contains(point)) {
          updateHighlighting(point);
        } else {
          updateHighlighting(null);
        }
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
      abortExternalDrag(event);
      final Point point = event.getPoint();
      updateHighlighting(point);
    }

    public void mouseExited(final MouseEvent event)
    {
      if (mInternalDragAction != null) {
        abortExternalDrag(event);
        final Point point = event.getPoint();
        updateHighlighting(point);
      } else {
        updateHighlighting(null);
      }
    }


    //#######################################################################
    //# Interface java.awt.MouseMotionListener
    public void mouseMoved(final MouseEvent event)
    {
      abortExternalDrag(event);
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
      abortExternalDrag(event);
      requestFocusInWindow();
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
      if (item instanceof LabelGeometrySubject) {
        return 5;
      } else if (item instanceof SimpleNodeSubject) {
        return 4;
      } else if (item instanceof EdgeSubject) {
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
          final SimpleNodeSubject node =
            (SimpleNodeSubject) mFocusedObject.getParent();
          editStateName(node);
        } else if (mFocusedObject instanceof EdgeSubject) {
          EditorEditEdgeDialog.showDialog((EdgeSubject) mFocusedObject, mRoot);
        } else if (mFocusedObject instanceof GuardActionBlockSubject) {
          final EdgeSubject edge = (EdgeSubject) mFocusedObject.getParent();
          EditorEditEdgeDialog.showDialog(edge, mRoot);
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      super.mousePressed(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
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
      if (item instanceof LabelGeometrySubject) {
        return 2;
      } if (item instanceof SimpleNodeSubject) {
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
          final SimpleNodeSubject node =
            (SimpleNodeSubject) mFocusedObject.getParent();
          editStateName(node);
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      super.mousePressed(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
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
      abortExternalDrag(event);
      requestFocusInWindow();
      mPopupFactory.maybeShowPopup
        (ControlledSurface.this, event, mFocusedObject);
      if (event.getButton() == MouseEvent.BUTTON1) {
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
          mFocusedObject != null) {
        if (mFocusedObject instanceof SimpleNodeSubject) {
          final WatersPopupActionManager manager = mPopupFactory.getMaster();
          final IDEAction action =
            manager.getNodeSelfloopAction(mFocusedObject);
          manager.invokeMouseClickAction(action, event);        
        } else if (mFocusedObject instanceof EdgeSubject) {
          EditorEditEdgeDialog.showDialog((EdgeSubject) mFocusedObject, mRoot);
        } else if (mFocusedObject instanceof GuardActionBlockSubject) {
          final EdgeSubject edge = (EdgeSubject) mFocusedObject.getParent();
          EditorEditEdgeDialog.showDialog(edge, mRoot);
        }
      }
    }

    public void mousePressed(final MouseEvent event)
    {
      abortExternalDrag(event);
      requestFocusInWindow();
      mPopupFactory.maybeShowPopup
        (ControlledSurface.this, event, mFocusedObject);
      if (event.getButton() == MouseEvent.BUTTON1) {
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
      mPreviousSelection = null;
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

    @SuppressWarnings("unused")
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

    //#######################################################################
    //# Temporrary Selection
    void copyCurrentSelection()
    {
      mPreviousSelection = new ArrayList<ProxySubject>(mSelectedList);
    }

    List<ProxySubject> getPreviousSelection()
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
     */
    void commitDrag(final Point point)
    {
    }

    /**
     * Cancels this operation. This method is called when the user has
     * only clicked rather than dragged the mouse. Sometimes the selection
     * needs to be updated in such a case. Subclasses must call the
     * superclass method also.
     */
    void cancelDrag(final Point point)
    { 
    }

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final ProxySubject item)
    {
      return -1;
    }

    DRAGOVERSTATUS getExternalDragStatus()
    {
      return DRAGOVERSTATUS.NOTDRAG;
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
     * Backup of the selection when this action was started,
     * if requested by calling {@link #copyCurrentSelection()}.
     */
    private List<ProxySubject> mPreviousSelection;
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
      ControlledSurface.this.commitSecondaryGraph(null, true, true);
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
      copyCurrentSelection();
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        final List<ProxySubject> dragged = getDragSelection();
        if (wasControlDown()) {
          clearSelection();
          addToSelection(getPreviousSelection());
          if (mSelectedSet.containsAll(dragged)) {
            removeFromSelection(dragged);
          } else {
            addToSelection(dragged);
          }
        } else {
          clearSelection();
          addToSelection(dragged);
        }
        return true;
      } else {
        return false;
      }
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      final ProxySubject label = getLabelToBeSelected();
      if (label == null) {
        if (wasControlDown()) {
          if (mFocusedObject != null) {
            toggleSelection(mFocusedObject);
          }
        } else {
          if (mFocusedObject == null) {
            clearSelection();
          } else {
            replaceSelection(mFocusedObject);
          }
        }
      } else {
        if (wasControlDown()) {
          toggleSelection(label);
        } else {
          replaceLabelSelection(label);
        }
      }
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      final Rectangle rect = getDragRectangle();
      final MiscShape shape =
        new GeneralShape(rect,
                         EditorColor.GRAPH_SELECTED_FOCUSSED,
                         EditorColor.shadow(EditorColor.GRAPH_SELECTED_FOCUSSED));
      return Collections.singletonList(shape);
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Returns a list of all objects within a drag rectangle,
     * i.e., the items to be selected.
     */
    private List<ProxySubject> getDragSelection()
    {
      final GraphSubject graph = getGraph();
      final Rectangle dragrect = getDragRectangle();
      final List<ProxySubject> selection = new LinkedList<ProxySubject>();
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
      mClickedObjectWasSelected = clicked != null && isSelected(clicked);
      mMovedObject = mFocusedObject;
      if (mMovedObject == null || !mClickedObjectWasSelected) {
        if (!wasControlDown()) {
          clearSelection();
        }
        if (mMovedObject != null) {
          addToSelection(mMovedObject);
        }
      }
      Point2D snap = null;
      if (Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get()) {
        // Move operation snaps to grid when a node is moved.
        for (final ProxySubject item : mSelectedList) {
          if (item instanceof SimpleNodeSubject) {
            final SimpleNodeSubject simple = (SimpleNodeSubject) item;
            snap = simple.getPointGeometry().getPoint();
            break;
          } else if (item instanceof GroupNodeSubject) {
            final GroupNodeSubject group = (GroupNodeSubject) item;
            final Rectangle2D rect = group.getGeometry().getRectangle();
            snap = new Point2D.Double(rect.getX(), rect.getY());
            break;
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
      super.commitSecondaryGraph();
      mMoveVisitor = null;
    }

    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      if (mClickedObject != null) {
        if (!wasControlDown()) {
          replaceLabelSelection(mClickedObject);
        } else if (mClickedObjectWasSelected) {
          removeFromSelection(mClickedObject);
        }
      }
      mMoveVisitor = null;
    }

    //#######################################################################
    //# Data Members
    private final Point2D mSnapPoint;
    private final ProxySubject mClickedObject;
    private final boolean mClickedObjectWasSelected;
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
      mClickedLabel = label;
      addToSelection(mClickedLabel);
      mExternalDragStatus = DRAGOVERSTATUS.NOTDRAG;
    }

    private InternalDragActionDND(final Point point)
    {
      super(point, point, false);
      mClickedLabel = null;
      mExternalDragStatus = DRAGOVERSTATUS.NOTDRAG;
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final DropTargetDragEvent event)
    {
      final Point point = event.getLocation();
      continueDrag(point);
      mController.updateHighlighting(point);
      final Transferable transferable = event.getTransferable();
      final EventListExpressionSubject elist =
        mIdentifierPasteVisitor.getIdentifierPasteTarget
          (mFocusedObject, transferable);
      final Line2D line;
      if (elist != null &&
          elist instanceof LabelBlockSubject &&
          !elist.getEventList().isEmpty()) {
        final Rectangle2D bounds =
          getShapeProducer().getShape(elist).getShape().getBounds();
        final double x1 = bounds.getMinX();
        final double x2 = bounds.getMaxX();
        double y;
        if (elist == mFocusedObject) {
          y = bounds.getMinY();
          mDropIndex = 0;
          for (final ProxySubject item : elist.getEventListModifiable()) {
            final ProxyShape shape = getShapeProducer().getShape(item);
            final Rectangle2D rect = shape.getShape().getBounds();
            if (point.getY() < rect.getCenterY()) {
              y = rect.getMinY();
              break;
            } else {
              y = rect.getMaxY();
              mDropIndex++;
            }
          }
        } else {
          y = bounds.getMaxY();
          mDropIndex = -1;
        }
        line = new Line2D.Double(x1, y, x2, y);
      } else {
        line = null;
        mDropIndex = -1;
      }
      if (elist == null) {
        mExternalDragStatus = DRAGOVERSTATUS.CANTDROP;
        event.acceptDrag(0);
      } else {
        mExternalDragStatus = DRAGOVERSTATUS.CANDROP;
        event.acceptDrag(DnDConstants.ACTION_COPY);
      }
      if (line == null ? mLine != null : !line.equals(mLine)) {
        mLine = line;
        repaint();
      }
      return true;
    }

    void commitDrag(final DropTargetDropEvent event)
    {
      final Point point = event.getLocation();
      commitDrag(point);
      try {
        if (mExternalDragStatus == DRAGOVERSTATUS.CANDROP) {
          final Transferable transferable = event.getTransferable();
          final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
          mIdentifierPasteVisitor.addInsertInfo
            (mFocusedObject, mDropIndex, transferable, inserts);
          final Command cmd =
            new InsertCommand(inserts, ControlledSurface.this);
          getUndoInterface().executeCommand(cmd);
          event.dropComplete(true);
        } else {
          event.dropComplete(false);
        }
        mExternalDragStatus = DRAGOVERSTATUS.NOTDRAG;
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
          toggleSelection(mClickedLabel);
        } else {
          replaceLabelSelection(mClickedLabel);
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

    DRAGOVERSTATUS getExternalDragStatus()
    {
      return mExternalDragStatus;
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
      for (final ProxySubject selected : mSelectedList) {
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

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      if (mLine == null) {
        return Collections.emptyList();
      } else {
        final MiscShape shape =
          new GeneralShape(mLine, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
        return Collections.singletonList(shape);
      }
    }

    private final ProxySubject mClickedLabel;
    private Line2D mLine;
    private int mDropIndex;
    private DRAGOVERSTATUS mExternalDragStatus;

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
      replaceSelection(mFocusedObject);
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
        if (len2 < GeometryTools.EPSILON_SQ) {
          return false;
        }
        final Point2D dir = new Point2D.Double(dx, dy);
        node.getInitialArrowGeometry().setPoint(dir);
        return true;
      } else {
        return false;
      }
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
      clearSelection();
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
        new GeneralShape(rect, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
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
      replaceSelection(mFocusedObject);
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
          ("Unknown group node handle type: " + handle.getType());
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
        final List<GroupNodeSubject> groups =
          Collections.singletonList(mGroup);
        final List<InsertInfo> deletes = getDeletionVictims(groups);
        final Command cmd = new DeleteCommand(deletes, ControlledSurface.this);
        getUndoInterface().executeCommand(cmd);
      } else {
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
      mAnchor = GeometryTools.getDefaultPosition(mSource, getDragStart());
      mIsSource = false;
      mOrigEdge = null;
      mCanCreateSelfloop = false;
      replaceSelection(mFocusedObject);
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
      replaceSelection(mFocusedObject);
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
          final int radius = Config.GUI_EDITOR_NODE_RADIUS.get() + 2;
          final int threshold = radius * radius;
          if (dist < threshold) {
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
          current = GeometryTools.getDefaultPosition(focused, current);
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
      if (node == null) {
        if (mSource != null) {
          replaceSelection(mSource);
        }
      } else {
        super.commitSecondaryGraph();
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
    private void createCopiedEdge()
    {
      if (mCopiedEdge == null) {
        if (mSource != null) {
          final NodeSubject source =
            (NodeSubject) mSecondaryGraph.getCopy(mSource);
          final PointGeometrySubject geo =
            source instanceof GroupNodeSubject ?
            new PointGeometrySubject(mAnchor) :
            null;
          mCopiedEdge =
            new EdgeSubject(source, null, null, null, null, geo, null);
          mSecondaryGraph.getEdgesModifiable().add(mCopiedEdge);
          mOrigEdge = (EdgeSubject) mSecondaryGraph.getOriginal(mCopiedEdge);
          replaceSelection(mOrigEdge);
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
      mMovedTypes = new HashSet<Class<? extends Proxy>>(8);
      for (final ProxySubject item : mSelectedList) {
        final Class<? extends Proxy> iface = item.getProxyInterface();
        mMovedTypes.add(iface);
      }
      final Collection<ProxySubject> moved;
      if (mMovedTypes.contains(SimpleNodeProxy.class) ||
          mMovedTypes.contains(GroupNodeProxy.class)) {
        final Collection<EdgeSubject> edges = getGraph().getEdgesModifiable();
        mEdgeMap = new HashMap<EdgeProxy,MovingEdge>(edges.size());
        moved = new LinkedList<ProxySubject>(mSelectedList);
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
        moved = mSelectedList;
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

    @SuppressWarnings("unused")
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
          return isSelected(parent);
        } else {
          return mEdgeMap.containsKey(parent);
        }
      } else if (parent instanceof NodeProxy) {
        return isSelected(parent);
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private final Set<Class<? extends Proxy>> mMovedTypes;
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
      final boolean sel1 = isRenderedSelected(item1);
      final boolean sel2 = isRenderedSelected(item2);
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
      // to be reimplemented
      if (e.getKeyCode() == KeyEvent.VK_UP ||
          e.getKeyCode() == KeyEvent.VK_KP_UP)
        {
          //System.err.println("UP");
          boolean hasMoved = false;
          CompoundCommand upMove = new CompoundCommand("Move Event");
          for (ProxySubject o : mSelectedList)
            {
              if (o instanceof LabelBlockSubject)
                {
                  LabelBlockSubject l = (LabelBlockSubject)o;
                  if (hasSelected(l))
                    {
                      List<AbstractSubject> labels =
                        new ArrayList<AbstractSubject>(l.getEventListModifiable());
                      labels.retainAll(mSelectedList);
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
              getUndoInterface().executeCommand(upMove);
            }
        }
      if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN)
        {
          //System.err.println("Down");
          boolean hasMoved = false;
          CompoundCommand downMove = new CompoundCommand("Move Event");
          for (ProxySubject o : mSelectedList)
            {
              if (o instanceof LabelBlockSubject)
                {
                  LabelBlockSubject l = (LabelBlockSubject)o;
                  if (hasSelected(l))
                    {
                      List<AbstractSubject> labels =
                        new ArrayList<AbstractSubject>(l.getEventListModifiable());
                      labels.retainAll(mSelectedList);
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
              getUndoInterface().executeCommand(downMove);
            }
        }
    }
  }


  //#########################################################################
  //# Inner Class StateNameInputCell
  private class StateNameInputCell
    extends SimpleExpressionCell
    implements FocusListener
  {

	//#######################################################################
    //# Constructor
    private StateNameInputCell(final SimpleNodeSubject node,
                               final SimpleIdentifierSubject ident)
    {
      super(ident, new StateNameInputParser(ident));
      final StateNameInputParser parser =
        (StateNameInputParser) getFormattedInputParser();
      parser.setCell(this);

      mNode = node;
      final LabelGeometrySubject geo = node.getLabelGeometry();
      final Point2D pgeo = node.getPointGeometry().getPoint();
      final Point2D lgeo = geo.getOffset();
      final Rectangle rect = ControlledSurface.this.getVisibleRect();
      int x = (int) Math.round(pgeo.getX() + lgeo.getX());
      int y = (int) Math.round(pgeo.getY() + lgeo.getY());
      int width = STATE_INPUT_WIDTH;
      int height = getPreferredSize().height;
      if (width > rect.width) {
        width = rect.width;
      }
      final int xmax = rect.x + rect.width;
      if (x + width > xmax) {
        x = xmax - width;
      }
      final Point pos = new Point(x, y);
      final Dimension size = new Dimension(width, height);
      setLocation(pos);
      setSize(size);
      setErrorDisplay(new LoggerErrorDisplay());

      setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
      addFocusListener(this);
      final Action enter = new AbstractAction("<enter>") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(final ActionEvent event)
          {
            try {
              commitEdit();
              cancel();
            } catch (final java.text.ParseException exception) {
              requestFocusInWindow();
            }
          }
        };
      addEnterAction(enter);
      final Action escape = new AbstractAction("<escape>") {
          private static final long serialVersionUID = 1L;
          public void actionPerformed(final ActionEvent event)
          {
            cancel();
          }
        };
      addEscapeAction(escape);

      ControlledSurface.this.add(this);
      setVisible(true);
      requestFocusInWindow();
      mDontDraw.add(geo);
      ControlledSurface.this.repaint();
    }

    //#######################################################################
    //# Cancalling
    private void cancel()
    {
      if (mNode != null) {
        final LabelGeometrySubject geo = mNode.getLabelGeometry();
        mNode = null;
        mDontDraw.remove(geo);
        // Warning: ControlledSurface.remove() calls commitEdit() again !!!
        ControlledSurface.this.remove(this);
        ControlledSurface.this.repaint();
      }
    }

    //#######################################################################
    //# Overrides for Superclass javax.swing.JFormattedTextField
    public void commitEdit()
      throws java.text.ParseException
    {
      if (mNode != null) {
        // Try to parse and commit the current value in the text field ...
        super.commitEdit();
        // If we get here without exception, the value has been parsed
        // successfully. Let us change the state name.
        final SimpleNodeSubject newnode = mNode.clone();
        final String newname = getText();
        newnode.setName(newname);
        final Command cmd =
          new EditCommand(mNode, newnode,
                          ControlledSurface.this, "State Renaming");
        getUndoInterface().executeCommand(cmd);
      }
    }

    //#######################################################################
    //# Interface java.awt.event.FocusListener
    public void focusGained(final FocusEvent event)
    {
    }

    public void focusLost(final FocusEvent event)
    {
      cancel();
    }

    //#######################################################################
    //# Data Members
    private SimpleNodeSubject mNode;

    //#######################################################################
    //# Class Constants
	private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Inner Class StateNameInputParser
  private class StateNameInputParser
    extends SimpleIdentifierInputParser
  {

    //#######################################################################
    //# Constructor
    private StateNameInputParser(final SimpleIdentifierProxy oldident)
    {
      super(oldident, mRoot.getModuleWindowInterface().getExpressionParser());
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    public SimpleIdentifierProxy parse(final String text)
      throws ParseException
    {
      final SimpleIdentifierProxy ident = super.parse(text);
      final String oldname = getOldName();
      if (!text.equals(oldname)) {
        if (getGraph().getNodesModifiable().containsName(text)) {
          throw new ParseException
            ("State name '" + text + "' is already taken!", 0);
        }
      }
      return ident;
    }

  }


  //#########################################################################
  //# Inner Class DataFlavorVisitor
  private class DataFlavorVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private DataFlavor getDataFlavor(final List<? extends Proxy> list)
    {
      mHasGraph = false;
      mHasIdentifiers = false;
      mNumGuardActionBlocks = 0;
      mNumNodeLabels = 0;
      for (final Proxy proxy : list) {
        try {
          proxy.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
        if (mHasGraph) {
          break;
        }
      }
      if (mHasGraph) {
        return WatersDataFlavor.GRAPH;
      } else if (mHasIdentifiers) {
        return WatersDataFlavor.IDENTIFIER_LIST;
      } else if (mNumGuardActionBlocks == 1 && mNumNodeLabels == 0) {
        return WatersDataFlavor.GUARD_ACTION_BLOCK;
      } else if (mNumGuardActionBlocks == 0 && mNumNodeLabels == 1) {
        return DataFlavor.stringFlavor;
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        return visitLabelBlockProxy(block);
      } else {
        return null;
      }
    }

    public DataFlavor visitForeachEventProxy(final ForeachEventProxy foreach)
    {
      mHasIdentifiers = true;
      return null;
    }

    public DataFlavor visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      mNumGuardActionBlocks++;
      return null;
    }

    public DataFlavor visitIdentifierProxy(final IdentifierProxy ident)
    {
      mHasIdentifiers = true;
      return null;
    }

    public DataFlavor visitLabelBlockProxy(final LabelBlockProxy block)
    {
      if (block.getEventList().isEmpty()) {
        final LabelBlockSubject subject = (LabelBlockSubject) block;
        if (subject.getParent() instanceof GraphProxy) {
          mHasGraph = true;
        }
      } else {
        mHasIdentifiers |= true;
      }
      return null;
    }

    public DataFlavor visitLabelGeometryProxy(final LabelGeometryProxy geo)
    {
      mNumNodeLabels++;
      return null;
    }

    public DataFlavor visitNodeProxy(final NodeProxy node)
    {
      mHasGraph = true;
      return null;
    }

    //#######################################################################
    //# Data Members
    private boolean mHasGraph;
    private boolean mHasIdentifiers;
    private int mNumGuardActionBlocks;
    private int mNumNodeLabels;
  }


  //#########################################################################
  //# Inner Class SelectableVisitor
  private class SelectableVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean addToSelectionList(final List<? extends Proxy> proxies)
    {
      boolean change = false;
      for (final Proxy proxy : proxies) {
        final ProxySubject subject = (ProxySubject) proxy;
        if (subject instanceof IdentifierSubject) {
          final IdentifierSubject ident = (IdentifierSubject) subject;
          final LabelBlockSubject block =
            ident.getAncestor(LabelBlockSubject.class);
          if (block != null && mSelectedSet.add(block)) {
            mSelectedList.add(block);
            change = true;
          }
        }
        final ProxySubject ancestor = getSelectableAncestor(subject);
        if (ancestor != null && mSelectedSet.add(ancestor)) {
          mSelectedList.add(ancestor);
          change = true;
        }
      }
      return change;
    }

    private ProxySubject getSelectableAncestor(final Proxy proxy)
    {
      try {
        return (ProxySubject) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
    {
      return edge;
    }

    public Proxy visitForeachEventProxy(final ForeachEventProxy foreach)
    {
      return visitEventListMember(foreach);
    }

    public GuardActionBlockProxy visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      return block;
    }

    public Proxy visitIdentifierProxy(final IdentifierProxy ident)
    {
      return visitEventListMember(ident);
    }

    public Proxy visitLabelBlockProxy(final LabelBlockProxy block)
    {
      if (block.getEventList().isEmpty()) {
        final LabelBlockSubject subject = (LabelBlockSubject) block;
        final Subject parent = subject.getParent();
        if (parent instanceof GraphProxy) {
          return block;
        } else {
          return (EdgeSubject) parent;
        }
      } else {
        return block;
      }
    }

    public LabelGeometryProxy visitLabelGeometryProxy
      (final LabelGeometryProxy geo)
    {
      return geo;
    }

    public NodeProxy visitNodeProxy(final NodeProxy node)
    {
      return node;
    }

    //#######################################################################
    //# Auxiliary Methods
    private AbstractSubject visitEventListMember(final Proxy proxy)
    {
      final AbstractSubject subject = (AbstractSubject) proxy;
      final Subject ancestor = subject.getAncestor(SimpleNodeSubject.class,
                                                   LabelBlockSubject.class);
      if (ancestor instanceof SimpleNodeSubject) {
        return (SimpleNodeSubject) ancestor;
      } else {
        return subject;
      }
    }
  }


  //#########################################################################
  //# Inner Class GraphTransferableVisitor
  private class GraphTransferableVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ProxyTransferable<?> createTransferable
      (final List<? extends Proxy> list)
    {
      mTransferredBlock = null;
      mTransferredNodes = new HashSet<NodeProxy>();
      mTransferredEdges = new LinkedList<EdgeProxy>();
      for (final Proxy proxy : list) {
        try {
          proxy.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
      final Iterator<EdgeProxy> iter = mTransferredEdges.iterator();
      while (iter.hasNext()) {
        final EdgeProxy edge = iter.next();
        final NodeProxy source = edge.getSource();
        final NodeProxy target = edge.getTarget();
        if (!mTransferredNodes.contains(source) ||
            !mTransferredNodes.contains(target)) {
          iter.remove();
        }
      }
      final GraphProxy graph = new GraphElement
        (false, mTransferredBlock, mTransferredNodes, mTransferredEdges, null);
      final ProxyTransferable<?> transferable = new GraphTransferable(graph);
      mTransferredBlock = null;
      mTransferredNodes = null;
      mTransferredEdges = null;
      return transferable;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      mTransferredEdges.add(edge);
      return null;
    }

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
    {
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      if (subject.getParent() instanceof GraphProxy) {
        mTransferredBlock = block;
      }
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
    {
      mTransferredNodes.add(node);
      return null;
    }

    //#######################################################################
    //# Data Members
    private LabelBlockProxy mTransferredBlock;
    private Collection<NodeProxy> mTransferredNodes;
    private Collection<EdgeProxy> mTransferredEdges;
  }


  //#########################################################################
  //# Inner Class IdentifierListTransferableVisitor
  private class IdentifierListTransferableVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ProxyTransferable<? extends Proxy> createTransferable
      (final List<? extends Proxy> list)
    {
      final int size = list.size();
      mTransferredItems = new ProxyAccessorHashMapByContents<Proxy>(size);
      for (final Proxy proxy : list) {
        try {
          proxy.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
      final List<Proxy> transferlist =
        new ArrayList<Proxy>(mTransferredItems.values());
      final ProxyTransferable<? extends Proxy> transferable =
        new IdentifierTransferable(transferlist);
      mTransferredItems = null;
      return transferable;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
      throws VisitorException
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null) {
        visitLabelBlockProxy(block);
      }
      return null;
    }

    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      mTransferredItems.addProxy(ident);
      return null;
    }

    public Object visitLabelBlockProxy(final LabelBlockProxy block)
      throws VisitorException
    {
      final List<Proxy> eventlist = block.getEventList();
      return visitCollection(eventlist);
    }

    //#######################################################################
    //# Data Members
    private ProxyAccessorMap<Proxy> mTransferredItems;
  }


  //#########################################################################
  //# Inner Class IdentifierPasteVisitor
  private class IdentifierPasteVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean canPaste(final Proxy focussed,
                             final Transferable transferable)
    {
      return getIdentifierPasteTarget(focussed, transferable) != null;
    }

    private EventListExpressionSubject getIdentifierPasteTarget
      (final Proxy focussed, final Transferable transferable)
    {
      try {
        final List<? extends Proxy> data = getTransferData(transferable);
        return getIdentifierPasteTarget(focussed, data);
      } catch (final IOException exception) {
        return null;
      } catch (final UnsupportedFlavorException exception) {
        return null;
      }
    }

    private EventListExpressionSubject getIdentifierPasteTarget
      (final Proxy focussed, final List<? extends Proxy> data)
    {
      if (focussed == null) {
        return null;
      } else {
        try {
          mTransferData = data;
          return (EventListExpressionSubject) focussed.acceptVisitor(this);
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
    }

    private void addInsertInfo(final Proxy focussed,
                               final int startpos,
                               final Transferable transferable,
                               final List<InsertInfo> inserts)
      throws IOException, UnsupportedFlavorException
    {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final List<? extends Proxy> data = getTransferData(transferable);
      final EventListExpressionSubject elist =
        getIdentifierPasteTarget(focussed, data);
      if (elist != null) {
        final ListSubject<AbstractSubject> list =
          elist.getEventListModifiable();
        int pos = startpos < 0 ? list.size() : startpos;
        for (final Proxy proxy : data) {
          final ProxySubject newident = (ProxySubject) cloner.getClone(proxy);
          ControlledSurface.this.addInsertInfo(inserts, newident, list, pos++);
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public EventListExpressionProxy visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public EventListExpressionProxy visitEdgeProxy(final EdgeProxy edge)
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      return visitLabelBlockProxy(block);
    }

    public EventListExpressionProxy visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      final GuardActionBlockSubject subject = (GuardActionBlockSubject) block;
      final EdgeSubject edge = (EdgeSubject) subject.getParent();
      return visitEdgeProxy(edge);
    }

    public EventListExpressionProxy visitLabelBlockProxy
      (final LabelBlockProxy block)
    {
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      if (subject.getParent() == getGraph()) {
        return isContainingAll(block) ? null : block;
      } else {
        final ModuleContext context =
          mRoot.getModuleWindowInterface().getModuleContext();
        return
          context.canDropOnEdge(mTransferData) && !isContainingAll(block) ?
            block : null;
      }
    }

    public EventListExpressionProxy visitLabelGeometryProxy
      (final LabelGeometryProxy geo)
    {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
      return visitSimpleNodeProxy(node);
    }

    public EventListExpressionProxy visitPlainEventListProxy
      (final PlainEventListProxy elist)
    {
      final ModuleContext context =
        mRoot.getModuleWindowInterface().getModuleContext();
      return
        context.canDropOnNode(mTransferData) && !isContainingAll(elist) ?
        elist : null;
    }

    public EventListExpressionProxy visitSimpleNodeProxy
      (final SimpleNodeProxy node)
    {
      final PlainEventListProxy elist = node.getPropositions();
      return visitPlainEventListProxy(elist);
    }

    //#######################################################################
    //# Auxiliary Methods
    @SuppressWarnings("unchecked")
	private List<? extends Proxy> getTransferData
      (final Transferable transferable)
      throws IOException, UnsupportedFlavorException
    {
      return (List<Proxy>) transferable.getTransferData
        (WatersDataFlavor.IDENTIFIER_LIST);
    }

    @SuppressWarnings("unchecked")
	private boolean isContainingAll(final EventListExpressionProxy elist)
    {
      final List<? extends Proxy> list = elist.getEventList();
      final ProxyAccessorMap<Proxy> map =
        new ProxyAccessorHashMapByContents(list);
      return map.containsAll(mTransferData);
    }

    //#######################################################################
    //# Data Members
    private List<? extends Proxy> mTransferData;
  }


  //#########################################################################
  //# Inner Class GraphInsertPosition
  private static class GraphInsertPosition
  {
    //#######################################################################
    //# Constructor
    private GraphInsertPosition(final ProxySubject parent,
                                final Object oldvalue)
    {
      mParent = parent;
      mOldValue = oldvalue;
    }

    //#######################################################################
    //# Simple Access
    private ProxySubject getParent()
    {
      return mParent;
    }

    public Object getOldValue()
    {
      return mOldValue;
    }

    //#######################################################################
    //# Data Members
    private final ProxySubject mParent;
    private final Object mOldValue;
  }


  //#########################################################################
  //# Data Members
  private final EditorWindowInterface mRoot;
  private final ControlledToolbar mToolbar;

  /**
   * List of currently selected items.
   */
  private List<ProxySubject> mSelectedList = new LinkedList<ProxySubject>();
  /**
   * Set of currently selected items. This holds the same contents as
   * {@link #mSelectedList} in hash set, for faster lookup.
   */
  private Set<ProxySubject> mSelectedSet = new HashSet<ProxySubject>();
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
   * The currently highlighted item (under the mouse pointer).
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
  /**
   * Whether the current spring embedding is undoable.
   * This is false for the initial layout of a graph without geometry.
   */
  private boolean mIsEmbedderUndoable;
  /**
   * Whether the group node hierarchy needs updating. This flag is set to
   * <CODE>true</CODE> when certain changes are detected in the graph. It
   * causes a recalculation of the hierarchy when the next undo/redo event
   * is received.
   */
  private boolean mNeedsHierarchyUpdate;
  /**
   * Whether the graph is assumed to have group nodes. This is determined
   * while updating group node hierarchy, and used to determine more
   * accurately whether the hierarchy needs updating again.
   */
  private boolean mHasGroupNodes;
  private boolean mSizeMayHaveChanged;
  private boolean mIsPermanentFocusOwner;

  private ToolController mController;
  private ToolController mSelectController;
  private ToolController mNodeController;
  private ToolController mGroupNodeController;
  private ToolController mEdgeController;
  private ToolController mEmbedderController;

  private final PopupFactory mPopupFactory;

  private final GraphModelObserver mGraphModelObserver =
    new GraphModelObserver();
  private final EventDeclListModelObserver mEventDeclListModelObserver =
    new EventDeclListModelObserver();
  private final SelectableVisitor mSelectableVisitor = new SelectableVisitor();
  private final DataFlavorVisitor mDataFlavorVisitor = new DataFlavorVisitor();
  private final GraphTransferableVisitor mGraphTransferableVisitor =
    new GraphTransferableVisitor();
  private final IdentifierListTransferableVisitor
    mIdentifierListTransferableVisitor =
    new IdentifierListTransferableVisitor();
  private final IdentifierPasteVisitor mIdentifierPasteVisitor =
    new IdentifierPasteVisitor();
  private final HighlightComparator mComparator = new HighlightComparator();
  private List<Observer> mObservers;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final int STATE_INPUT_WIDTH = 128;

}
