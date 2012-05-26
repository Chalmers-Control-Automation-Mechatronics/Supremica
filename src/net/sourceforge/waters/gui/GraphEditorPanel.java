//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   GraphEditorPanel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import gnu.trove.THashSet;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
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
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.renderer.GeneralShape;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.Handle.HandleType;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.IndexedSetSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.properties.Config;


/**
 * A component to edit a graph. The graph editor panel is used by
 * the IDE to edit a ({@link GraphProxy}). It contains all the mouse
 * handlers to support the various editing tools, cut-copy-paste, as
 * well as drag-and-drop.
 *
 * @author Robi Malik, Gian Perrone, Carly Hona
 */

public class GraphEditorPanel
  extends BackupGraphPanel
  implements SelectionOwner, Observer, FocusListener
{
  //#########################################################################
  //# Constructors
  public GraphEditorPanel(final GraphSubject graph,
                          final ModuleSubject module,
                          final EditorWindowInterface root,
                          final ControlledToolbar toolbar,
                          final WatersPopupActionManager manager)
    throws GeometryAbsentException
  {
    super(graph, module);
    mRoot = root;
    mRenderingContext = new EditorRenderingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, mRenderingContext);
    setShapeProducer(producer);
    mToolbar = toolbar;
    mPopupFactory =
      manager == null ? null : new GraphPopupFactory(manager, root);
    setFocusable(true);
    addKeyListener(new KeySpy());
    updateTool();
    ensureGeometryExists();
    mHasGroupNodes = GraphTools.updateGroupNodeHierarchy(graph);
    mNeedsHierarchyUpdate = false;
    mSizeMayHaveChanged = true;
    mIsPermanentFocusOwner = false;
    registerGraphObserver();
    registerSupremicaPropertyChangeListeners();
    addFocusListener(this);
    module.getEventDeclListModifiable().addModelObserver
      (mEventDeclListModelObserver);
    if (root != null) {
      if (toolbar != null) {
        toolbar.attach(this);
      }
      final UndoInterface undoer = root.getUndoInterface();
      undoer.attach(this);
    }
    setTransferHandler(new GraphEditorPanelTransferHandler());
  }

  /**
   * Creates an immutable controlled surface.
   */
  public GraphEditorPanel(final GraphSubject graph,
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
  //# Repaint Support
  public void registerSupremicaPropertyChangeListeners()
  {
    super.registerSupremicaPropertyChangeListeners();
    Config.GUI_EDITOR_GRID_SIZE.addPropertyChangeListener(this);
  }

  public void unregisterSupremicaPropertyChangeListeners()
  {
    super.registerSupremicaPropertyChangeListeners();
    Config.GUI_EDITOR_GRID_SIZE.removePropertyChangeListener(this);
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
    return mSelectableAncestorVisitor.getSelectableAncestor(item);
  }

  public void replaceSelection(final List<? extends Proxy> items)
  {

    if (!mSelectedList.equals(items)) {
      mSelectedList.clear();
      mSelectedSet.clear();
      mSelectableChildVisitor.addToSelectionList(items, false);
      fireSelectionChanged();
    }
  }

  public void addToSelection(final List<? extends Proxy> items)
  {
    if (mSelectableChildVisitor.addToSelectionList(items, false)) {
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

  @Override
  public boolean canPaste(final Transferable transferable)
  {
    if (transferable.isDataFlavorSupported(WatersDataFlavor.GRAPH)) {
      return true;
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.IDENTIFIER)) {
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
      if (getPasteTarget() instanceof NodeProxy) {
        try {
          final ExpressionParser parser =
            mRoot.getModuleWindowInterface().getExpressionParser();
          final String data =
            (String) transferable.getTransferData(DataFlavor.stringFlavor);
          parser.parseSimpleIdentifier(data);
          return true;
        } catch (final ParseException exception) {
          return false;
        } catch (final UnsupportedFlavorException exception) {
          throw new WatersRuntimeException(exception);
        } catch (final IOException exception) {
          throw new WatersRuntimeException(exception);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    if (transferable.isDataFlavorSupported(WatersDataFlavor.GRAPH)) {
      final GraphSubject graph = getGraph();
      final List<GraphProxy> list = (List<GraphProxy>)
        transferable.getTransferData(WatersDataFlavor.GRAPH);
      final GraphProxy newgraph = list.iterator().next();
      final LabelBlockSubject newblocked =
        (LabelBlockSubject) newgraph.getBlockedEvents();
      final Set<NodeSubject> newnodes =
        (Set<NodeSubject>) (Object) newgraph.getNodes();
      final Collection<EdgeSubject> newedges =
        (Collection<EdgeSubject>) (Object) newgraph.getEdges();
      final Point2D newpos =
        GeometryTools.getTopLeftPosition(newblocked, newnodes);
      final Point2D pastepos = getPastePosition();
      final double dx = pastepos.getX() - newpos.getX();
      final double dy = pastepos.getY() - newpos.getY();
      final Point2D delta = new Point2D.Double(dx, dy);
      if (newblocked != null) {
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
      final Set<String> newnames = new THashSet<String>(newnodes.size());
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
      for (final EdgeSubject newedge : newedges) {
        GeometryTools.translate(newedge, delta);
        addInsertInfo(inserts, newedge, graph);
      }
    } else if (transferable.isDataFlavorSupported(WatersDataFlavor.EDGE)) {
      final GraphSubject graph = getGraph();
      final List<EdgeSubject> edges = (List<EdgeSubject>)
        transferable.getTransferData(WatersDataFlavor.GRAPH);
      for (final EdgeSubject edge : edges) {
        addInsertInfo(inserts, edge, graph);
      }
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.IDENTIFIER)) {
      final ProxySubject target = getPasteTarget();
      mIdentifierPasteVisitor.addInsertInfo(target, -1, transferable, inserts);
    } else if (transferable.isDataFlavorSupported
                 (WatersDataFlavor.GUARD_ACTION_BLOCK)) {
      final List<GuardActionBlockSubject> list =
        (List<GuardActionBlockSubject>) transferable.getTransferData
                                               (WatersDataFlavor.GRAPH);
      final GuardActionBlockSubject newblock = list.iterator().next();
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
      else if(subject.getParent().getParent() instanceof ForeachSubject){
        final ForeachSubject foreach = (ForeachSubject)subject.getParent().getParent();
        final ListSubject<AbstractSubject> eventlist =
          (ListSubject<AbstractSubject>) foreach.getBodyModifiable();
        eventlists.put(eventlist, true);
      }
    }
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    if (!eventlists.isEmpty()) {
      // Deleting event labels: visit all label blocks, and add deleted items
      // in order of appearance to undo list ...
      final Set<? extends Proxy> lookup = getRootsOfLabelSelection(items);
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
    final Set<Proxy> lookup = new THashSet<Proxy>(items);
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

  public void insertItems(final List<InsertInfo> inserts)
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
                 proxy instanceof ForeachSubject) {
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<?> untyped = inspos.getList();
        @SuppressWarnings("unchecked")
        final List<Proxy> eventlist = (List<Proxy>) untyped;
        int pos = inspos.getPosition();
        if(pos == -1){
          pos = eventlist.size();
        }
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
          @SuppressWarnings("unchecked")
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

  public void deleteItems(final List<InsertInfo> inserts)
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
                 proxy instanceof ForeachSubject) {
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
      final Set<Proxy> scrollable = new THashSet<Proxy>();
      for (final Proxy proxy : list) {
        if (proxy instanceof IdentifierSubject ||
            proxy instanceof ForeachSubject) {
          final Subject subject = (Subject) proxy;
          final ProxySubject parent =
            (ProxySubject) SubjectTools.getAncestor(subject,
                                                    LabelBlockSubject.class,
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
    super.close();
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
      if (!isEmbedderRunning()) {
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
      closeEmbedder();
      commitSecondaryGraph("Automatic Layout", false, mIsEmbedderUndoable);
      clearSecondaryGraph();
      updateTool();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Repaint Support
  protected void graphChanged(final ModelChangeEvent event)
  {
    checkGroupNodeHierarchyUpdate(event);
    updateError();
    mController.updateHighlighting();
    super.graphChanged(event);
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
  //# Repainting
  @Override
  protected void paintComponent(final Graphics graphics)
  {
    super.paintComponent(graphics);
    if (mInternalDragAction == null) {
      adjustSize();
    }
  }

  protected void adjustSize()
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

  @Override
  protected Dimension calculatePreferredSize()
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
  Command doCreateSimpleNode(final Point2D pos)
  {
    final GraphSubject graph = getGraph();
    final SimpleNodeSubject node = GraphTools.getCreatedSimpleNode(graph, pos);
    final GraphInsertPosition inspos = new GraphInsertPosition(graph, null);
    final InsertInfo insert = new InsertInfo(node, inspos);
    final List<InsertInfo> inserts = Collections.singletonList(insert);
    final Command cmd = new InsertCommand(inserts, this, null);
    getUndoInterface().executeCommand(cmd);
    return cmd;
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
    final GraphInsertPosition inspos = new GraphInsertPosition(graph, null);
    final InsertInfo insert = new InsertInfo(group, inspos);
    final List<InsertInfo> inserts = Collections.singletonList(insert);
    final Command cmd = new InsertCommand(inserts, this, null);
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
    final Command cmd = new InsertCommand(edge, this, null);
    getUndoInterface().executeCommand(cmd);
  }

  public void runEmbedder(final boolean undoable)
  {
    mIsEmbedderUndoable = undoable;
    createEmbedder();
    runEmbedder();
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
      addToSelection(item);
    }
  }

  /**
   * Adds an item to the selection.
   */
  private void addToSelection(final ProxySubject item)
  {
    if(mSelectableChildVisitor.addToSelectionList(Collections.singletonList(item),
                                             false)){
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
    if(!(item instanceof LabelBlockSubject) &&
      SubjectTools.getAncestor(item, LabelBlockSubject.class) != null){
      mSelectedList.clear();
      mSelectedSet.clear();
      addToSelection(item);
    }
    else {
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

  private boolean isSourceOfDrag(){
    return mRoot.getModuleWindowInterface().getRootWindow().getFocusTracker()
      .getWatersSelectionOwner() == GraphEditorPanel.this;
  }

  //TODO where to put this?
  private boolean addToSelectedSet(final ProxySubject subject){
    if (mSelectedSet.add(subject)) {
      mSelectedList.add(subject);
      return true;
    }
    return false;
  }


  //#########################################################################
  //# Low-level Selection Handling
  private void fireSelectionChanged()
  {
    mLastCommand = null;
    if (mInternalDragAction == null) {
      final EditorChangedEvent event = new SelectionChangedEvent(this);
      fireEditorChangedEvent(event);
    }
    repaint();
  }


  //#########################################################################
  //# Rendering Hints
  private boolean isRenderedSelected(final ProxySubject item)
  {
    final ProxySubject original = getOriginal(item);
    if (original == null) {
      return false;
    } else if (isSelected(original)) {
      return true;
    } else {
      return false;
    }
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

  private boolean overlap(final Shape s1, final Shape s2)
  {
    if (s1.equals(s2))
      {
        return true;
      }
    final Rectangle2D r1 = s1.getBounds2D();
    final Rectangle2D r2 = s2.getBounds2D();
    return r1.intersects(r2) && !(r1.contains(r2) || r2.contains(r1));
  }

  /**
   * Finds all focusable objects at a given position.
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
   * otherwise this method returns the item in focus.
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
      final ProxySubject label =
        getLabelToBeSelected(block.getEventListModifiable(), point);
      return label == null ? item : label;
    } else {
      return item;
    }
  }

  private ProxySubject getLabelToBeSelected(final ListSubject<AbstractSubject> list,
                                            final Point point)
  {
    for (final ProxySubject sub : list) {
      final ProxyShape shape = getShapeProducer().getShape(sub);
      if (shape.getShape().contains(point)) {
        return sub;
      }
      if (sub instanceof ForeachSubject) {
        final ForeachSubject foreach = (ForeachSubject) sub;
        final ProxySubject proxy =
          getLabelToBeSelected(foreach.getBodyModifiable(), point);
        if (proxy != null) {
          return proxy;
        }
      }
    }
    return null;
  }

  private static Set<? extends Proxy> getRootsOfLabelSelection
    (final Collection<? extends Proxy> items)
  {
    if (items.size() <= 1) {
      return new THashSet<Proxy>(items);
    } else {
      final Set<Proxy> set = new THashSet<Proxy>(items);
      final int size = items.size();
      final Set<Proxy> reduced = new THashSet<Proxy>(size);
      for (final Proxy proxy : items) {
        ProxySubject parent = (ProxySubject) proxy;
        while (true) {
          parent = SubjectTools.getProxyParent(parent);
          if (parent == null || parent instanceof LabelBlockProxy) {
            reduced.add(proxy);
            break;
          } else if (set.contains(parent)) {
            break;
          }
        }
      }
      return reduced;
    }
  }


  //#########################################################################
  //# Secondary Graph
  protected void commitSecondaryGraph(final String description)
  {
    commitSecondaryGraph(description, false, false);
  }

  protected void commitSecondaryGraph(final String description,
                                      final boolean selecting,
                                      final boolean undoable)
  {
    final EditorGraph graph = getSecondaryGraph();
    if (graph != null) {
      final Command cmd =
        graph.createUpdateCommand(this, description, selecting);
      mLastCommand = cmd;
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


  //##########################################################################
  //# Inner Class EditorRenderingContext
  private class EditorRenderingContext extends ModuleRenderingContext
  {

    //#######################################################################
    //# Constructor
    private EditorRenderingContext()
    {
      super(mRoot.getModuleWindowInterface().getModuleContext());
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
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
      final DragOverStatus dragover;
      if (focused && mInternalDragAction != null) {
        dragover = mInternalDragAction.getExternalDragStatus();
      } else {
        dragover = DragOverStatus.NOTDRAG;
      }
      return new RenderingInformation
        (showHandles, focused,
         EditorColor.getColor(item, dragover, selected,
                              error, mIsPermanentFocusOwner),
         EditorColor.getShadowColor(item, dragover, selected,
                                    error, mIsPermanentFocusOwner),
         priority);
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

    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
    }

  }



  //#########################################################################
  //# Inner Class ToolController
  private abstract class ToolController
    implements MouseListener, MouseMotionListener
  {

    //#######################################################################
    //# Highlighting and Selecting
    int getHighlightPriority(final ProxySubject item)
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
      final ProxySubject item = getItemToBeSelected(event);
      if (canBeSelected(item)) {
        final List<ProxySubject> list = getListOfSelectedLabels();
        if (item instanceof LabelBlockSubject){
          if (isSelected(item)) {
            if (event.isShiftDown()) {
              if(list.isEmpty()){
                removeFromSelection(item);
              }
              else{
                //do nothing if there are labels selected and you shift click on label space
              }
            } else {
              if (!list.isEmpty()) {
                removeFromSelection(list);
              }
            }
          }
          else{
            if (event.isShiftDown()) {
              if(!list.isEmpty()){
                removeFromSelection(list);
              }
              toggleSelection(item);
            }
            else{
              replaceSelection(item);
            }
          }
        }
        else if(item instanceof IdentifierSubject ||
              item instanceof ForeachSubject) {
          if (isSelected(item)) {
            if (event.isShiftDown()) {
              removeFromSelection(item);
            }
            else{
              replaceSelection(item);
            }
          }
          else{
            if (event.isShiftDown()) {
              if(list.isEmpty()){
                final LabelBlockSubject block = SubjectTools.getAncestor(item, LabelBlockSubject.class);
                toggleSelection(block);
              }
              else{
                toggleSelection(item);
              }
            }
            else{
                replaceSelection(item);
            }
          }
        }
        else{
          if (isSelected(item)) {
            if (event.isShiftDown()) {
              removeFromSelection(item);
            } else {
              //nothing
            }
          }else{
            if (event.isShiftDown()) {
              removeFromSelection(list);
              toggleSelection(item);
            } else {
              replaceSelection(item);
            }
          }
        }
      } else {
        if(event.isShiftDown()){
          //nothing??
        }else{
          clearSelection();
        }
      }
    }

    private List<ProxySubject> getListOfSelectedLabels(){
      final List<ProxySubject> list = new LinkedList<ProxySubject>();
      for (final ProxySubject sub : mSelectedList) {
        if (sub instanceof IdentifierSubject || sub instanceof ForeachSubject) {
          list.add(sub);
        }
      }
      return list;
    }

    public void mousePressed(final MouseEvent event)
    {
      mStartPoint = event.getPoint();
      requestFocusInWindow();
      mPopupFactory.maybeShowPopup
        (GraphEditorPanel.this, event, mFocusedObject);
    }

    public void mouseReleased(final MouseEvent event)
    {
      mStartPoint = null;
      mPopupFactory.maybeShowPopup
        (GraphEditorPanel.this, event, mFocusedObject);
      if (mInternalDragAction != null) {
        final Point point = event.getPoint();
        mInternalDragAction.commitDrag(point);

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
        mInternalDragAction.continueDrag(event);
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

    protected Point mStartPoint;

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
        return 6;
      } else if (item instanceof SimpleNodeSubject) {
        return 5;
      } else if (item instanceof EdgeSubject) {
        return 4;
      } else if (item instanceof LabelBlockSubject ||
                 item instanceof GuardActionBlockSubject) {
        return 3;
      } else if (item instanceof GroupNodeSubject) {
        return 2;
      } else if (item instanceof IdentifierSubject || item instanceof ForeachSubject) {
        return 1;
      }else {
        return -1;
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    public void mouseClicked(final MouseEvent event)
    {
      super.mouseClicked(event);
      //double clicks
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

    public void mouseDragged(final MouseEvent event)
    {
      if(mInternalDragAction == null){
        if(mFocusedObject == null){
          mInternalDragAction = new InternalDragActionSelect(mStartPoint);
        }
        else{
          final ProxySubject subject = getItemToBeSelected(event);
          if(mFocusedObject == subject || !isSelected(subject)){
            final Handle handle = getClickedHandle(subject, event);
            if(handle == null){
              mInternalDragAction = new InternalDragActionMove(mStartPoint);
            }
            else{
              switch (handle.getType()) {
              case INITIAL:
                mInternalDragAction = new InternalDragActionInitial(mStartPoint);
                break;
              case SOURCE:
              case TARGET:
                mInternalDragAction =
                  new InternalDragActionEdge(handle, mStartPoint);
                break;
              case NW:
              case N:
              case NE:
              case W:
              case E:
              case SW:
              case S:
              case SE:
                mInternalDragAction = new InternalDragActionResizeGroupNode(handle, mStartPoint);
                break;
              default:
                throw new IllegalStateException
                  ("Unknown handle type: " + handle.getType());
              }
            }
          }
          else{
             mInternalDragAction = new InternalDragActionDND(mFocusedObject, mStartPoint, event.isShiftDown());
          }
        }
      }
      super.mouseDragged(event);



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
      final boolean noSelections = mSelectedList.isEmpty();
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
          final Command c = getUndoInterface().getLastCommand();
          //only create a node if there no selections or a node was just created
          if (noSelections || (mLastNodeCommand != null && mLastNodeCommand.equals(c))){
            mLastNodeCommand = doCreateSimpleNode(snapped);
          }
          else{
            mLastNodeCommand = null;
          }
        } else if (event.getClickCount() == 2 && mFocusedObject != null
                   && mFocusedObject instanceof LabelGeometrySubject) {
          // Double-click to rename nodes
          final SimpleNodeSubject node =
            (SimpleNodeSubject) mFocusedObject.getParent();
          editStateName(node);
        } else {
          mLastNodeCommand = null;
        }
      }
    }

    Command mLastNodeCommand = null;

    public void mouseDragged(final MouseEvent event)
    {
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction =
            new InternalDragActionSelect(mStartPoint);
        } else {
          final ProxySubject subject = getItemToBeSelected(event);
          if (mFocusedObject == subject || !isSelected(subject)) {
            final Handle handle = getClickedHandle(subject, event);
            if (handle == null) {
              mInternalDragAction =
                new InternalDragActionMove(mStartPoint);
            } else {
              if(handle.getType() == HandleType.INITIAL){
                mInternalDragAction =
                  new InternalDragActionInitial(mStartPoint);
              }
              else{
                mInternalDragAction =
                new InternalDragActionSelect(mStartPoint);
              }
            }
          }
        }
      }
      super.mouseDragged(event);
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
      super.mousePressed(event);
    }

    public void mouseDragged(final MouseEvent event){
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction = new InternalDragActionCreateGroupNode(mStartPoint);
        } else {
          final Handle handle = getClickedHandle(mFocusedObject, event);
          if (handle == null) {
            mInternalDragAction = new InternalDragActionMove(mStartPoint);
          } else {
            mInternalDragAction =
              new InternalDragActionResizeGroupNode(handle, mStartPoint);
          }
        }
      }
      super.mouseDragged(event);
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
        return 5;
      } else if (item instanceof GroupNodeSubject) {
        return 4;
      } else if (item instanceof EdgeSubject) {
        return 3;
      } else if (item instanceof LabelBlockSubject ||
                 item instanceof GuardActionBlockSubject) {
        return 2;
      } else if (item instanceof IdentifierSubject || item instanceof ForeachSubject) {
        return 1;
      }else {
        return -1;
      }
    }

    boolean canBeSelected(final ProxySubject item)
    {
      return
        item instanceof EdgeSubject ||
        item instanceof LabelBlockSubject ||
        item instanceof GuardActionBlockSubject ||
        item instanceof IdentifierSubject || item instanceof ForeachSubject;
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

    public void mouseDragged(final MouseEvent event)
    {
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction =
            new InternalDragActionSelect(mStartPoint);
        } else {
          final ProxySubject item = getItemToBeSelected(event);
          if(mFocusedObject == item || !isSelected(item)){
            final Handle handle = getClickedHandle(item, event);
            if(handle == null && canBeSelected(item)){
              mInternalDragAction = new InternalDragActionMove(mStartPoint);
            }
            else{
              if (item instanceof NodeSubject) {
                // Clicking on node or nodegroup --- create edge.
                mInternalDragAction =
                  new InternalDragActionEdge(mStartPoint);
              } else if (item instanceof EdgeSubject) {
                if (handle == null) {
                  mInternalDragAction =
                    new InternalDragActionMove(mStartPoint);
                } else {
                  mInternalDragAction =
                    new InternalDragActionEdge(handle, mStartPoint);
                }
              }
            }
          }
          else{
            mInternalDragAction =
              new InternalDragActionDND(item, mStartPoint, event.isShiftDown());
          }

        }
      }
      super.mouseDragged(event);
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
    private InternalDragAction(final Point point)
    {
      mPreviousSelection = null;
      mDragStart = point;
      mDragCurrent = point;
      mDragCurrentOnGrid = point;
    }

    //#######################################################################
    //# Simple Access

    Point getDragStart()
    {
      return mDragStart;
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
        final int x1 = mDragStart.x;
        final int y1 = mDragStart.y;
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
    //# Temporary Selection
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
     * #mDragCurrentOnGrid}. This method is overridden to perform additional
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
      repaint();
      return true;
    }


    boolean continueDrag(final MouseEvent event)
    {
      return continueDrag(event.getPoint());
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

    DragOverStatus getExternalDragStatus()
    {
      return DragOverStatus.NOTDRAG;
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
     * Backup of the selection when this action was started,
     * if requested by calling {@link #copyCurrentSelection()}.
     */
    private List<ProxySubject> mPreviousSelection;
    /**
     * The position of the mouse cursor when the current internal dragging
     * operation was started.
     */
    private final Point mDragStart;
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
    private BigInternalDragAction(final Point start)
    {
      super(start);
    }

    //#######################################################################
    //# Simple Access
    boolean createSecondaryGraph()
    {
      return GraphEditorPanel.this.createSecondaryGraph();
    }

    void clearSecondaryGraph()
    {
      GraphEditorPanel.this.clearSecondaryGraph();
    }

    void commitSecondaryGraph()
    {
      GraphEditorPanel.this.commitSecondaryGraph(null, true, true);
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
    private InternalDragActionSelect(final Point start)
    {
      super(start);
      if (mFocusedObject instanceof LabelBlockSubject &&
          isSelected(mFocusedObject)) {
        mLabelBlock = (LabelBlockSubject) mFocusedObject;
      } else {
        mLabelBlock = null;
      }
      copyCurrentSelection();
      mShiftDown = false;
    }

    // TODO Make sure this gets called so shift-drag-select works again.
    private InternalDragActionSelect(final Point start, final boolean shift)
    {
      this(start);
      mShiftDown = shift;
    }

    //#######################################################################
    //# Dragging
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        final List<ProxySubject> dragged = getDragSelection();
        if (mShiftDown) {
           clearSelection();
          addToSelection(getPreviousSelection());
          if (mSelectedSet.containsAll(dragged)) {
            removeFromSelection(dragged);
          } else {
            final List<ProxySubject> list = getListWithoutLabels();
            clearSelection();
            list.addAll(dragged);
            addToSelection(list);
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

    private List<ProxySubject> getListWithoutLabels(){
      final List<ProxySubject> list = new LinkedList<ProxySubject>();
      for (final ProxySubject sub : mSelectedList) {
        if (!(sub instanceof IdentifierSubject || sub instanceof ForeachSubject)
            && sub != mLabelBlock) {
          list.add(sub);
        }
      }
      return list;
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

    //#######################################################################
    //# Data Members
    private final LabelBlockSubject mLabelBlock;
    private boolean mShiftDown;

  }


  //#########################################################################
  //# Inner Class InternalDragActionMove
  private class InternalDragActionMove
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionMove(final Point start)
    {
      this(mFocusedObject, start, false);
    }

    private InternalDragActionMove(final ProxySubject clicked,
                                   final Point start,
                                   final boolean shiftDown)
    {
      super(start);
      mClickedObjectWasSelected = clicked != null && isSelected(clicked);
      mMovedObject = mFocusedObject;
      //mCanSelectLabels = canSelectLabels(mClickedObject);
      if (mMovedObject == null || !mClickedObjectWasSelected) {
        if (!shiftDown && mMovedObject != null) {
          replaceSelection(mMovedObject);
        } else if (!shiftDown) {
          clearSelection();
        } else if (mMovedObject != null) {
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
      mMoveVisitor = null;
    }


    //#######################################################################
    //# Data Members

    private final Point2D mSnapPoint;
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
    private InternalDragActionDND(final ProxySubject label,
                                  final Point start,
                                  final boolean shiftDown)
    {
      super(start);
      mShiftDown = shiftDown;
      mClickedLabel = label;
      if (label != null) {
        addToSelection(label);
      }
      mExternalDragStatus = DragOverStatus.NOTDRAG;
      mHasDragged = false;
    }

    private InternalDragActionDND(final Point point)
    {
      this(null, point, false);
    }

    //#######################################################################
    //# Dragging
    @Override
    boolean continueDrag(final MouseEvent event)
    {
      final boolean draggedNow = super.continueDrag(event.getPoint());
      if (!mHasDragged && draggedNow) {
        mHasDragged = true;
        if (event.isShiftDown()) {
          getTransferHandler().exportAsDrag(GraphEditorPanel.this, event,
                                            TransferHandler.COPY);
        } else {
          getTransferHandler().exportAsDrag(GraphEditorPanel.this, event,
                                            TransferHandler.MOVE);
        }
      }
      return draggedNow;
    }

    @Override
    void cancelDrag(final Point point)
    {
      super.cancelDrag(point);
      if (mClickedLabel != null) {
        if (mShiftDown) {
          toggleSelection(mClickedLabel);
        } else {
          replaceLabelSelection(mClickedLabel);
        }
      }
    }

    @SuppressWarnings("unchecked")
    boolean canImport(final TransferSupport support)
    {
      final Point point = support.getDropLocation().getDropPoint();
      super.continueDrag(point);
      mController.updateHighlighting(point);
      final Transferable transferable = support.getTransferable();
      try {
        mDraggedList = (List<ProxySubject>) transferable
            .getTransferData(WatersDataFlavor.IDENTIFIER);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      }
      final EventListExpressionSubject elist =
        mIdentifierPasteVisitor.getIdentifierPasteTarget(mFocusedObject,
                                                         transferable);
      final int dropAction = support.getDropAction();
      Line2D line = null;
      if (elist != null) {
        if (elist instanceof LabelBlockSubject) {
          final Rectangle2D bounds =
            getShapeProducer().getShape(elist).getShape().getBounds();
          mX = bounds.getMinX();
          final double x2 = bounds.getMaxX();
          if(mFocusedObject instanceof EdgeProxy){
            mY = 0;
            mX = 0;
            mDropIndex = -1;
            mRect = null;
            mDropList = elist.getEventListModifiable();
          }
          else if (elist == mFocusedObject) {
            mY = bounds.getMinY();
            mDropIndex = 0;
            mDropList = elist.getEventListModifiable();
            mRect = null;
            for (final ProxySubject item : elist.getEventListModifiable()) {
              final ProxyShape shape = getShapeProducer().getShape(item);
              final Rectangle2D rect = shape.getShape().getBounds();
              mX = rect.getMinX();
              if (point.getY() < rect.getCenterY()) {
                mY = rect.getMinY();
                break;
              } else {
                mY = rect.getMaxY();
                mDropIndex++;
              }
              if (item instanceof ForeachSubject) {
                if (descendForeachBlock((ForeachSubject) item, rect, point, dropAction)) {
                  break;
                }
              }
            }
          } else {
            mY = bounds.getMaxY();
            mDropIndex = -1;
          }
          if(mY == 0 || mX == 0){
            line = null;
          }
          else{
            line = new Line2D.Double(mX, mY, x2, mY);
          }
        } else if (elist instanceof PlainEventListSubject) {
          final PlainEventListSubject plain = (PlainEventListSubject) elist;
          mDropList = plain.getEventListModifiable();
          mDropIndex = -1;
          line = null;
        }
        setExternalDragStatus(dropAction);
      } else {
        line = null;
        mRect = null;
        mDropIndex = -1;
        mExternalDragStatus = DragOverStatus.CANTDROP;
      }
      if (line == null ? mLine != null : !line.equals(mLine)) {
        mLine = line;
        repaint();
      }
      return mExternalDragStatus == DragOverStatus.CANDROP;
    }

    boolean importData(final TransferSupport support){
      final Point point = support.getDropLocation().getDropPoint();
      commitDrag(point);
      boolean finished = false;
      try {
        if (mExternalDragStatus == DragOverStatus.CANDROP) {
          final Transferable transferable = support.getTransferable();
          final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
          mIdentifierPasteVisitor.addInsertInfo(mFocusedObject, mDropIndex,
                                                mDropList, transferable,
                                                inserts,
                                                support.getDropAction());
          if (!inserts.isEmpty()) {
            final Command ins =
              new InsertCommand(inserts, GraphEditorPanel.this, null);
            if (support.getDropAction() == GraphEditorPanelTransferHandler.MOVE) {
              List<InsertInfo> deletes = new LinkedList<InsertInfo>();
              final List<ProxySubject> list = getCurrentSelection();
              deletes = getDeletionVictims(list);
              final Command del =
                new DeleteCommand(deletes, GraphEditorPanel.this, true);
              final CompoundCommand compound =
                new CompoundCommand("Move Labels");
              compound.addCommand(del);
              compound.addCommand(ins);
              getUndoInterface().executeCommand(compound);
            } else {
              getUndoInterface().executeCommand(ins);
            }
          }
        }
        finished = true;
        mExternalDragStatus = DragOverStatus.NOTDRAG;
      } catch (final UnsupportedFlavorException exception) {
        throw new IllegalArgumentException(exception);
      } catch (final IOException exception) {
        throw new IllegalArgumentException(exception);
      }
      mInternalDragAction = null;
      fireSelectionChanged();
      mController.updateHighlighting(point);
      return finished;
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

    DragOverStatus getExternalDragStatus()
    {
      return mExternalDragStatus;
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Gets the list of currently selected labels.
     */
    private List<ProxySubject> getIdentifiersToBeDragged()
    {
      List<ProxySubject> result =
        new LinkedList<ProxySubject>();
      for (final ProxySubject selected : mSelectedList) {
        if (selected instanceof LabelBlockSubject) {
          final LabelBlockSubject block = (LabelBlockSubject) selected;
          result = getSelections(block.getEventListModifiable(), result);
        }
        else if(selected instanceof ForeachSubject){
          final ForeachSubject foreach = (ForeachSubject) selected;
          if(!result.contains(foreach)){
            result.add(foreach);
          }
          result = getSelections(foreach.getBodyModifiable(), result);
        }
        else if(selected.getParent().getParent() instanceof ForeachSubject){
          final ForeachSubject foreach = (ForeachSubject)selected.getParent().getParent();
          result = getSelections(foreach.getBodyModifiable(), result);
        }
      }
      return result;
    }

    private List<ProxySubject> getSelections(final ListSubject<AbstractSubject> list,
                                             List<ProxySubject> result){
      for (final ProxySubject item : list) {
        if (isSelected(item)) {
          if(item instanceof IdentifierSubject){
            final IdentifierSubject ident = (IdentifierSubject) item;
            if(!result.contains(ident)){
              result.add(ident);
            }
          }
          else if(item instanceof ForeachSubject){
            final ForeachSubject foreach = (ForeachSubject) item;
            if(!result.contains(foreach)){
              result.add(foreach);
            }
            result = getSelections(foreach.getBodyModifiable(), result);
          }
        }
      }
      return result;
    }

    private void setLineAtEnd(final ForeachSubject foreach)
    {
      final List<Proxy> list = foreach.getBody();
      Proxy proxy = null;
      if (list.size() == 0) {
        proxy = foreach;
      } else {
        proxy = list.get(list.size() - 1);
      }
      final ProxyShape shape = getShapeProducer().getShape(proxy);
      final Rectangle2D rect = shape.getShape().getBounds();
      if (proxy instanceof ForeachSubject && list.size() > 0) {
        final ForeachSubject nextForeach = (ForeachSubject) proxy;
        setLineAtEnd(nextForeach);
      } else {
        mY = rect.getMaxY();
      }
      mX = rect.getMinX();
    }

    private boolean descendForeachBlock(final ForeachSubject foreach,
                                        final Rectangle2D rect,
                                        final Point point,
                                        final int dropAction)
    {
      int drop = 0;
      if (point.getY() < rect.getCenterY() + 5
          && point.getY() > rect.getCenterY() - 5) {
        mDropIndex = foreach.getBody().size();
        mRect = rect;
        mDropList = foreach.getBodyModifiable();
        setLineAtEnd(foreach);
        if(mDropList.isEmpty()){
          mY = 0;
        }
        return true;
      } else {
        mRect = null;
      }
      for (final Proxy proxy : foreach.getBody()) {
        final ProxyShape shape2 = getShapeProducer().getShape(proxy);
        final Rectangle2D rect2 = shape2.getShape().getBounds();
        if (point.getY() < rect2.getCenterY()) {
          mY = rect2.getMinY();
          mX = rect2.getMinX();
          mDropIndex = drop;
          mDropList = foreach.getBodyModifiable();
          return true;
        } else {
          drop++;
          mY = rect2.getMaxY();
        }
        mRect = null;
        if (proxy instanceof ForeachSubject) {
          if (descendForeachBlock((ForeachSubject) proxy, rect2, point, dropAction)) {
            return true;
          }
        }
      }
      return false;
    }

    private void setExternalDragStatus(final int dropAction)
    {
      final List<? extends Proxy> selected =
        GraphEditorPanel.this.getCurrentSelection();
      if (isSourceOfDrag()) {
        for (final Proxy p : selected) {
          if (!(p instanceof LabelBlockSubject) &&
            SubjectTools.isAncestor((Subject) p, mDropList)) {
            mExternalDragStatus = DragOverStatus.CANTDROP;
            return;
          }
        }
      }
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      for (final ProxySubject item : mDraggedList) {
        if (eq.contains(mDropList, item)) {
          for (final Proxy proxy : selected) {
            final ProxySubject sub = (ProxySubject) proxy;
            if (sub.getParent().equals(mDropList)) {
              if (dropAction == DnDConstants.ACTION_MOVE) {
                mExternalDragStatus = DragOverStatus.CANDROP;
                return;
              }
            }
          }
          mExternalDragStatus = DragOverStatus.CANTDROP;
        } else {
          mExternalDragStatus = DragOverStatus.CANDROP;
          return;
        }
      }
    }

    //#######################################################################
    //# Rendering
    List<MiscShape> getDrawnObjects()
    {
      if (mLine == null && mRect == null) {
        return Collections.emptyList();
      } else {
        final List<MiscShape> list = new ArrayList<MiscShape>();
        if (mLine != null) {
          final MiscShape shape =
            new GeneralShape(mLine, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
          list.add(shape);
        }
        if (mRect != null) {
          final MiscShape shape =
            new GeneralShape(mRect, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
          list.add(shape);
        }
        return list;
      }
    }

    //#######################################################################
    //# Data Members
    private double mY;
    private double mX;
    private final ProxySubject mClickedLabel;
    private Line2D mLine;
    private Rectangle2D mRect;
    private int mDropIndex;
    private ListSubject<AbstractSubject> mDropList;
    private List<ProxySubject> mDraggedList;
    private DragOverStatus mExternalDragStatus;
    private final boolean mShiftDown;
    private boolean mHasDragged;

  }


  //#########################################################################
  //# Inner Class InternalDragActionInitial
  private class InternalDragActionInitial
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionInitial(final Point start)
    {
      super(start);
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
        GraphTools.setInitialArrowOffset(node, dir);
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
        mNodeCopy = (SimpleNodeSubject) getSecondaryGraph().getCopy(mNode);
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
    private InternalDragActionCreateGroupNode(final Point start)
    {
      super(Config.GUI_EDITOR_NODES_SNAP_TO_GRID.get() ?
            findGrid(start) : start);
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
      final Rectangle2D rect = getDragRectangle();
      if (!rect.isEmpty()) {
        doCreateGroupNode(rect);
      }
      repaint();
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
    private InternalDragActionResizeGroupNode(final Handle handle,
                                              final Point start)
    {
      super(start);
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
        mGroupCopy = (GroupNodeSubject) getSecondaryGraph().getCopy(mGroup);
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
        final Command cmd = new DeleteCommand(deletes, GraphEditorPanel.this);
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
        final EditorGraph graph = getSecondaryGraph();
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
              graph.moveEdgeStart(edge, dx, dy);
              graph.transformEdge(edge, true);
            }
          }
          if (edge.getTarget() == mGroup) {
            final PointGeometrySubject geo = edge.getEndPoint();
            if (geo != null) {
              final Point2D old = geo.getPoint();
              final Point2D neo = rescalePoint(old);
              final double dx = neo.getX() - old.getX();
              final double dy = neo.getY() - old.getY();
              graph.moveEdgeEnd(edge, dx, dy);
              graph.transformEdge(edge, false);
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
    private InternalDragActionEdge(final Point start)
    {
      super(start);
      mSource = (NodeSubject) mFocusedObject;
      mAnchor = GeometryTools.getDefaultPosition(mSource, getDragStart());
      mIsSource = false;
      mOrigEdge = null;
      mCanCreateSelfloop = false;
      replaceSelection(mFocusedObject);
      mFocusedObject = null;
    }

    /**
     * Creates a drag action to redirect the source or
     * target of an edge.
     */
    private InternalDragActionEdge(final Handle handle,
                                   final Point start)
    {
      super(start);
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
          focused = (NodeSubject) getSecondaryGraph().getCopy(mFocusedObject);
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
        if (mSource != null && mController.canBeSelected(mSource)) {
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
        final EditorGraph graph = getSecondaryGraph();
        if (mSource != null) {
          final NodeSubject source = (NodeSubject) graph.getCopy(mSource);
          final PointGeometrySubject geo =
            source instanceof GroupNodeSubject ?
            new PointGeometrySubject(mAnchor) :
            null;
          mCopiedEdge =
            new EdgeSubject(source, null, null, null, null, geo, null);
          graph.getEdgesModifiable().add(mCopiedEdge);
          mOrigEdge = (EdgeSubject) graph.getOriginal(mCopiedEdge);
          replaceSelection(mOrigEdge);
        } else {
          mCopiedEdge = (EdgeSubject) graph.getCopy(mOrigEdge);
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
  private class MoveVisitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private MoveVisitor()
    {
      mMovedTypes = new THashSet<Class<? extends Proxy>>(8);
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
        assert(getSecondaryGraph() != null);
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
        assert(getSecondaryGraph() != null);
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
        getSecondaryGraph().moveEdgeHandle(edge0, mDeltaX, mDeltaY);
      } else {
        final MovingEdge entry = mEdgeMap.get(edge0);
        entry.move(mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitGroupNodeProxy(final GroupNodeProxy group)
    {
      final GroupNodeSubject group0 = (GroupNodeSubject) group;
      getSecondaryGraph().moveGroupNode(group0, mDeltaX, mDeltaY);
      return null;
    }

    public Object visitForeachProxy(final ForeachProxy foreach)
    {
      return null;
    }

    public Object visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      if (!isParentMoved(block)) {
        final GuardActionBlockSubject block0 = (GuardActionBlockSubject) block;
        getSecondaryGraph().moveGuardActionBlock(block0, mDeltaX, mDeltaY);
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
        getSecondaryGraph().moveLabelBlock(block0, mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitLabelGeometryProxy(final LabelGeometryProxy label)
    {
      if (!isParentMoved(label)) {
        final LabelGeometrySubject label0 = (LabelGeometrySubject) label;
        getSecondaryGraph().moveLabelGeometry(label0, mDeltaX, mDeltaY);
      }
      return null;
    }

    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final SimpleNodeSubject node0 = (SimpleNodeSubject) node;
      getSecondaryGraph().moveSimpleNode(node0, mDeltaX, mDeltaY);
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
    // TODO Compiler bug? Why can't the following two be final???
    private Collection<ProxySubject> mMovedObjects = null;
    private Map<EdgeProxy,MovingEdge> mEdgeMap = null;
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
      final EditorGraph graph = getSecondaryGraph();
      if (mMovingSource) {
        graph.moveEdgeStart(mEdge, dx, dy);
      }
      if (mMovingTarget) {
        graph.moveEdgeEnd(mEdge, dx, dy);
      }
      switch (mType) {
      case MOVE_DONT:
        break;
      case MOVE_FOLLOW:
        graph.moveEdgeHandle(mEdge, dx, dy);
        break;
      case MOVE_SOURCE:
        graph.transformEdge(mEdge, true);
        break;
      case MOVE_TARGET:
        graph.transformEdge(mEdge, false);
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
  private class KeySpy extends KeyAdapter
  {

    public void keyPressed(final KeyEvent e)
    {
      final int keyCode = e.getKeyCode();
      final boolean up =
        keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_KP_UP;
      final boolean down =
        keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN;
      final boolean left =
        keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT;
      final boolean right =
        keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT;
      if (isAGeometryMove() && (up || down || left || right)) {
        mMoveVisitor = new MoveVisitor();
        createSecondaryGraph();
        final int x = left ? -1 : right ? 1 : 0;
        final int y = up ? -1 : down ? 1 : 0;
        mMoveVisitor.moveAll(x, y);
        commitGraph(null, true, true);
        mMoveVisitor = null;
        clearSecondaryGraph();
      } else if (up || down) {
        final CompoundCommand move = new CompoundCommand();
        boolean execute = false;
        final List<ProxySubject> selectionList = getReorderedSelectionList();
        final List<InsertInfo> deletes = getDeletes(selectionList);
        final List<InsertInfo> inserts = getInserts(selectionList, up, down);
        if (inserts != null && deletes != null) {
          final Command del =
            new DeleteCommand(deletes, GraphEditorPanel.this, true);
          final Command ins =
            new InsertCommand(inserts, GraphEditorPanel.this, null);
          move.addCommand(del);
          move.addCommand(ins);
          final List<Proxy> proxies = InsertInfo.getProxies(inserts);
          final String named = ProxyNamer.getCollectionClassName(proxies);
          move.setName(named + " Movement");
          execute = true;
        }
        if (execute) {
          move.end();
          e.consume();
          getUndoInterface().executeCommand(move);
        }
      }
    }

    private void commitGraph(final String description,
                             final boolean selecting,
                             final boolean undoable){
      final EditorGraph graph = getSecondaryGraph();
      if (graph != null) {
        final UndoInterface undoInterface = mRoot.getUndoInterface();
          if (mLastCommand != null
              && mLastCommand == undoInterface.getLastCommand()) {
            mLastCommand.setUpdatesSelection(false);
            undoInterface.undo();
            undoInterface.removeLastCommand();
          }

        final Command cmd =
          graph.createUpdateCommand(GraphEditorPanel.this, description, selecting);
        mLastCommand = cmd;
        if (cmd == null) {
          // ignore
        } else if (undoable) {
          getUndoInterface().executeCommand(cmd);
        } else {
          cmd.execute();
        }
      }
    }

    private boolean isAGeometryMove()
    {
      for (final ProxySubject proxy : mSelectedList) {
        if (proxy instanceof ForeachSubject) {
          return false;
        } else if (proxy instanceof IdentifierSubject) {
          return false;
        }
      }
      return true;
    }

    private List<InsertInfo> getDeletes(final List<ProxySubject> selections)
    {
      final List<InsertInfo> deletes = new LinkedList<InsertInfo>();
      ListSubject<? extends ProxySubject> initialList = null;
      int index;
      for (int i = 0; i < selections.size(); i++) {
        final ProxySubject proxy = selections.get(i);
        initialList = getInitialList(proxy);
        index = initialList.indexOf(proxy);
        final ListInsertPosition delpos =
          new ListInsertPosition(initialList, index);
        final InsertInfo delete = new InsertInfo(proxy, delpos);
        deletes.add(delete);
      }
      return deletes;
    }

    private List<InsertInfo> getInserts(final List<ProxySubject> selections,
                                        final boolean up, final boolean down)
    {
      boolean hasMoved = false;
      final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
      ProxySubject lead = selections.get(0);
      if (down) {
        lead = selections.get(selections.size() - 1);
      }
      ListSubject<? extends ProxySubject> insertList = getInitialList(lead);
      int index = insertList.indexOf(lead);
      final ProxySubject parent = SubjectTools.getProxyParent(lead);
      final ProxySubject grandParent = SubjectTools.getProxyParent(parent);
      if (up) {
        if (index == 0) {
          if (grandParent instanceof EdgeSubject) {
            if (selections.size() == 1) {
              return null;
            }
          } else {
            insertList = getInitialList(parent);
            index = insertList.indexOf(parent);
          }
        } else if (insertList.get(index - 1) instanceof ForeachSubject) {
          final ForeachSubject sibling =
            (ForeachSubject) insertList.get(index - 1);
          insertList = sibling.getBodyModifiable();
          index = -1;
        } else {
          index--;
        }
      } else if (down) {
        if (index == insertList.size() - 1) {
          if (grandParent instanceof EdgeSubject) {
            if (selections.size() == 1) {
              return null;
            }
            index = -1;
          } else {
            insertList = getInitialList(parent);
            index = insertList.indexOf(parent) + 1;
          }
        } else if (insertList.get(index + 1) instanceof ForeachSubject) {
          final ForeachSubject sibling =
            (ForeachSubject) insertList.get(index + 1);
          insertList = sibling.getBodyModifiable();
          index = 0;
        } else {
          index++;
          //make sure indexes are adjusted for deletions
          for (int y = 0; y < selections.size(); y++) {
            final ProxySubject p = selections.get(y);
            if (insertList.contains(p)) {
              index--;
            }
          }
          index++;
        }
      }
      //catch empty move commands with multiple items
      if (insertList.containsAll(selections)) {
        boolean contained = true;
        if (index == 0) {
          for (int i = 0; i < selections.size(); i++) {
            if (!insertList.get(i).equals(selections.get(i))) {
              contained = false;
            }
          }
        } else if (index == -1) {
          int last = insertList.size() - 1;
          int lastSelection = selections.size() - 1;
          while (lastSelection >= 0) {
            if (!insertList.get(last).equals(selections.get(lastSelection))) {
              contained = false;
            }
            last--;
            lastSelection--;
          }
        } else {
          contained = false;
        }
        if (contained) {
          return null;
        }
      }
      for (int y = 0; y < selections.size(); y++) {
        final ProxySubject p = selections.get(y);
        final ListInsertPosition inspos =
          new ListInsertPosition(insertList, index);
        final InsertInfo insert = new InsertInfo(p, inspos);
        inserts.add(insert);
        hasMoved = true;
        if (index != -1) {
          index++;
        }
      }
      if (!hasMoved) {
        return null;
      }
      return inserts;
    }

    private ListSubject<? extends ProxySubject> getInitialList(final ProxySubject proxy)
    {
      final ProxySubject parent = SubjectTools.getProxyParent(proxy);
      if (parent instanceof ForeachSubject) {
        return ((ForeachSubject) parent).getBodyModifiable();
      } else {
        return ((LabelBlockSubject) parent).getEventListModifiable();
      }
    }

    private List<ProxySubject> getReorderedSelectionList()
    {
      final List<ProxySubject> newList =
        new ArrayList<ProxySubject>(mSelectedList.size());
      int i = 0;
      while (i < mSelectedList.size()) {
        final ProxySubject proxy = mSelectedList.get(i);
        if ((proxy instanceof IdentifierSubject || proxy instanceof ForeachSubject)
            && !hasAncestorInSelection(proxy, mSelectedList)) {
          newList.add(proxy);
          i++;
        } else {
          mSelectedList.remove(i);
        }
      }
      mComparator = new PositionComparator();
      Collections.sort(newList, mComparator);
      return newList;
    }

    private boolean hasAncestorInSelection(final ProxySubject proxy,
                                           final List<ProxySubject> proxies)
    {
      for (int i = 0; i < proxies.size(); i++) {
        if (SubjectTools.isAncestor((ProxySubject) proxies.get(i), proxy)) {
          if (proxies.get(i) instanceof ForeachSubject
              && proxies.get(i) != proxy) {
            return true;
          }
        }
      }
      return false;
    }

  private class PositionComparator implements Comparator<ProxySubject>{

    public int compare(final ProxySubject proxy1, final ProxySubject proxy2)
    {
      final SubjectShapeProducer prod = getShapeProducer();
      final double ypos1 = prod.getShape(proxy1).getBounds2D().getY();
      final double ypos2 = prod.getShape(proxy2).getBounds2D().getY();
      if (ypos1 < ypos2) {
        return -1;
      }
      else if (ypos1 > ypos2) {
        return 1;
      }
      return 0;
    }
  }

    private MoveVisitor mMoveVisitor;
    private PositionComparator mComparator;
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
      mNode = node;
      final LabelGeometrySubject geo = node.getLabelGeometry();
      final Point2D pgeo = node.getPointGeometry().getPoint();
      final Point2D lgeo = geo.getOffset();
      final Rectangle rect = GraphEditorPanel.this.getVisibleRect();
      int x = (int) Math.round(pgeo.getX() + lgeo.getX());
      final int y = (int) Math.round(pgeo.getY() + lgeo.getY());
      int width = STATE_INPUT_WIDTH;
      final int height = getPreferredSize().height;
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

      GraphEditorPanel.this.add(this);
      setVisible(true);
      requestFocusInWindow();
      mDontDraw.add(geo);
      GraphEditorPanel.this.repaint();
    }

    //#######################################################################
    //# Cancelling
    private void cancel()
    {
      if (mNode != null) {
        final LabelGeometrySubject geo = mNode.getLabelGeometry();
        mNode = null;
        mDontDraw.remove(geo);
        // Warning: ControlledSurface.remove() calls commitEdit() again !!!
        GraphEditorPanel.this.remove(this);
        GraphEditorPanel.this.repaint();
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
        final String newname = getText();
        final String oldname = mNode.getName();
        if (!newname.equals(oldname)) {
          final SimpleNodeSubject newnode = mNode.clone();
          newnode.setName(newname);
          final Command cmd =
            new EditCommand(mNode, newnode,
                            GraphEditorPanel.this, "State Renaming");
          getUndoInterface().executeCommand(cmd);
        }
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
  //# Inner Class SelectableChildVisitor
  private class SelectableChildVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean addToSelectionList(final List<? extends Proxy> proxies,
                                       final boolean ctrlDown)
    {
      boolean change = false;
      for (final Proxy proxy : proxies) {
        final ProxySubject subject = (ProxySubject) proxy;
        final LabelBlockSubject block = SubjectTools.getAncestor(subject, LabelBlockSubject.class);
        if (block != null) {
          change |= addToSelectedSet(block);
          if (subject instanceof IdentifierSubject) {
              if (!block.getEventList().isEmpty()) {
                change |= addToSelectedSet(subject);
              }
          } else if (subject instanceof ForeachSubject) {
            final ForeachSubject foreach = (ForeachSubject) subject;
              if (block.getEventListModifiable().size() > 1
                  || foreach.getBodyModifiable().size() > 0) {
                change |= addToSelectedSet(subject);
              }
              change |= addChildrenToSelection(foreach);
          }
        }

        if (block == null) {
          final ProxySubject ancestor = mSelectableAncestorVisitor.getSelectableAncestor(subject);
          if (ancestor != null && addToSelectedSet(ancestor)) {
            change = true;
            if(ancestor instanceof EdgeSubject){
              final EdgeSubject edge = (EdgeSubject)ancestor;
              final LabelBlockSubject labelBlock = edge.getLabelBlock();
              final GuardActionBlockSubject guard = edge.getGuardActionBlock();
              addToSelectedSet(labelBlock);
              if(guard != null) {
                addToSelectedSet(guard);
              }
            }
            else if(ancestor instanceof SimpleNodeSubject){
              final SimpleNodeSubject node = (SimpleNodeSubject)ancestor;
              final LabelGeometrySubject label = node.getLabelGeometry();
              addToSelectedSet(label);
            }
          }
        }
      }
      return change;
    }

    private boolean addChildrenToSelection(final ForeachSubject foreach)
    {
      final ListSubject<AbstractSubject> list = foreach.getBodyModifiable();
      boolean change = false;
      for (final ProxySubject p : list) {
          change |= addToSelectedSet(p);
        if (p instanceof ForeachSubject) {
          change |= addChildrenToSelection((ForeachSubject) p);
        }
      }
      return change;
    }
  }


  //#########################################################################
  //# Inner Class SelectableVisitor
  private class SelectableAncestorVisitor
    extends DefaultModuleProxyVisitor
  {

    private ProxySubject getSelectableAncestor(final Proxy proxy)
    {
      try {
        if (proxy instanceof Subject) {
          return (ProxySubject) proxy.acceptVisitor(this);
        } else {
          return null;
        }
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

    public Proxy visitForeachProxy(final ForeachProxy foreach)
    {
      return visitEventListMember(foreach);
    }

    public Object visitGeometryProxy(final GeometryProxy proxy)
      throws VisitorException
    {
      final GeometrySubject geo = (GeometrySubject)proxy;
      final Proxy parent = (Proxy) geo.getParent();
      return parent.acceptVisitor(this);
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
      final Subject ancestor =
        SubjectTools.getAncestor(subject,
                                 SimpleNodeSubject.class,
                                 LabelBlockSubject.class);
      if (ancestor instanceof SimpleNodeSubject) {
        return (SimpleNodeSubject) ancestor; }
      else {
        return subject;
      }
    }
  }


  //#########################################################################
  //# Inner Class IdentifierPasteVisitor
  private class IdentifierPasteVisitor
    extends DefaultModuleProxyVisitor
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
        final ModuleEqualityVisitor eq =
          ModuleEqualityVisitor.getInstance(false);
        final ListSubject<AbstractSubject> list =
          elist.getEventListModifiable();
        int pos = startpos < 0 ? list.size() : startpos;

        for (final Proxy proxy : data) {
          if (!eq.contains(list, proxy)) {
            final ProxySubject newident =
              (ProxySubject) cloner.getClone(proxy);
            GraphEditorPanel.this.addInsertInfo(inserts, newident,
                                                list,
                                                pos++);
          }
        }
      }
    }

    private void addInsertInfo(final Proxy focussed, final int startpos,
                               final ListSubject<AbstractSubject> dropList,
                               final Transferable transferable,
                               final List<InsertInfo> inserts,
                               final int dropAction)
      throws IOException, UnsupportedFlavorException
    {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final List<? extends Proxy> data = getTransferData(transferable);
      final EventListExpressionSubject elist =
        getIdentifierPasteTarget(focussed, data);
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      if (elist != null) {
        final ListSubject<AbstractSubject> list =
          elist.getEventListModifiable();
        int pos = startpos < 0 ? list.size() : startpos;
        final List<? extends Proxy> selected;
        if (isSourceOfDrag()) {
          selected = GraphEditorPanel.this.getCurrentSelection();
        } else {
          selected = dropList;
        }

        final int p = pos;
        for (int i = 0; i < data.size(); i++) {
          for (final Proxy proxy : selected) {
            final ProxySubject sub = (ProxySubject) proxy;
            if (eq.equals(data.get(i), proxy)) {
              if (sub.getParent().equals(dropList)) {
                if (dropList.indexOf(sub) == p
                    || dropList.indexOf(sub) == p - 1) {
                  if (isContiguous(selected, data, dropList)) {
                    return;
                  }
                } else if (dropAction != DnDConstants.ACTION_MOVE) {
                  if (isContiguous(selected, data, dropList)) {
                    return;
                  }
                }
                if (dropList.indexOf(sub) < p) {
                  pos--;
                }
              } else {
                if (eq.contains(dropList, proxy)) {
                  data.remove(i);
                }
              }
            }
          }
        }

        for (final Proxy proxy : data) {
          final ProxySubject newident = (ProxySubject) cloner.getClone(proxy);
          GraphEditorPanel.this.addInsertInfo(inserts, newident, dropList,
                                              pos++);
        }
      }
    }

    private boolean isContiguous(final List<? extends Proxy> selected,
                                 final List<? extends Proxy> data,
                                 final ListSubject<AbstractSubject> dropList){
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      int in = -1;
      final boolean cont = true;
      for (int i = 0; i < data.size(); i++) {
        for (final Proxy proxy : selected) {
          final ProxySubject sub = (ProxySubject) proxy;
          if (eq.equals(data.get(i), proxy)) {
            if (sub.getParent().equals(dropList)) {
              if (dropList.indexOf(sub) == in + 1 || in == -1) {
                in = dropList.indexOf(sub);
              } else {
                return false;
              }
            }
          }
        }
      }
      return cont;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    public EventListExpressionProxy visitProxy(final Proxy proxy)
    {
      return null;
    }

     //TODO maybe need to change the point of this visitor ??

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
        // Any event can be dropped on the blocked events list.
        return isContainingAll(block) ? null : block;
      } else {
        final ModuleContext context =
          mRoot.getModuleWindowInterface().getModuleContext();
        if(context.canDropOnEdge(mTransferData)){
          return block;
        }
        return null;

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
        (WatersDataFlavor.IDENTIFIER);
    }

	private boolean isContainingAll(final EventListExpressionProxy elist)
    {
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
      final List<? extends Proxy> list = elist.getEventList();
      final ProxyAccessorSet<Proxy> map = new ProxyAccessorHashSet<Proxy>(eq, list);
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
  //# Inner Class GraphEditorPanelTransferHandler
  private class GraphEditorPanelTransferHandler extends TransferHandler
  {
    @Override
    public int getSourceActions(final JComponent c)
    {
      return COPY_OR_MOVE;
    }

    @Override
    public Transferable createTransferable(final JComponent c)
    {
      final InternalDragActionDND action =
        (InternalDragActionDND) mInternalDragAction;
      final List<ProxySubject> toBeDragged =
        action.getIdentifiersToBeDragged();
      final Transferable trans =
        WatersDataFlavor.createTransferable(toBeDragged);
      return trans;
    }

    @Override
    public void exportDone(final JComponent c, final Transferable t,
                           final int action)
    {
    }

    @Override
    public boolean canImport(final TransferSupport support)
    {
      if(!(mInternalDragAction instanceof InternalDragActionDND)){
        mInternalDragAction =
          new InternalDragActionDND(support.getDropLocation().getDropPoint());
      }
      final InternalDragActionDND dragAction = (InternalDragActionDND)mInternalDragAction;

      if(support.getDropAction() == MOVE && !isSourceOfDrag()){
        support.setDropAction(COPY);
      }

      return dragAction.canImport(support);
    }

    @Override
    public boolean importData(final TransferSupport support)
    {
      if(!(mInternalDragAction instanceof InternalDragActionDND)){
        mInternalDragAction =
          new InternalDragActionDND(support.getDropLocation().getDropPoint());
      }
      final InternalDragActionDND dragAction = (InternalDragActionDND)mInternalDragAction;
      return dragAction.importData(support);
    }

    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final EditorWindowInterface mRoot;
  private final ControlledToolbar mToolbar;

  /**
   * List of currently selected items.
   */
  private final List<ProxySubject> mSelectedList = new LinkedList<ProxySubject>();
  /**
   * Set of currently selected items. This holds the same contents as
   * {@link #mSelectedList} in hash set, for faster lookup.
   */
  private final Set<ProxySubject> mSelectedSet = new THashSet<ProxySubject>();
  /**
   * Set of items not to be drawn, because they are being dragged and
   * displayed through alternative means.
   */
  private final Set<ProxySubject> mDontDraw = new THashSet<ProxySubject>();
  /**
   * Set of items to be highlighted as erroneous.
   */
  private final Set<ProxySubject> mError = new THashSet<ProxySubject>();
  /**
   * The currently highlighted item (under the mouse pointer).
   */
  private ProxySubject mFocusedObject = null;
  /**
   * The last recorded position of the mouse cursor.
   */
  private Point mCurrentPoint;
  /**
   * The rendering context passed to the shape producers.
   */
  private final RenderingContext mRenderingContext;
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

  private final EventDeclListModelObserver mEventDeclListModelObserver =
    new EventDeclListModelObserver();
  SelectableChildVisitor mSelectableChildVisitor = new SelectableChildVisitor();
  SelectableAncestorVisitor mSelectableAncestorVisitor = new SelectableAncestorVisitor();

  private final IdentifierPasteVisitor mIdentifierPasteVisitor =
    new IdentifierPasteVisitor();
  private final HighlightComparator mComparator = new HighlightComparator();
  private List<Observer> mObservers;

  private Command mLastCommand = null;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final int STATE_INPUT_WIDTH = 128;

}
