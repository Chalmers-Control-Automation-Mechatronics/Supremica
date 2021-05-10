//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.gui;

import gnu.trove.set.hash.THashSet;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JViewport;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.IDEPropertiesAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CompoundCommand;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.compiler.ModuleCompilationErrors;
import net.sourceforge.waters.gui.dialog.EdgeEditorDialog;
import net.sourceforge.waters.gui.dialog.NodeEditorDialog;
import net.sourceforge.waters.gui.dialog.SimpleExpressionInputCell;
import net.sourceforge.waters.gui.dialog.SimpleIdentifierInputHandler;
import net.sourceforge.waters.gui.grapheditor.GraphInsertPosition;
import net.sourceforge.waters.gui.grapheditor.GraphSelection;
import net.sourceforge.waters.gui.grapheditor.ZoomSelector;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.renderer.ColorGroup;
import net.sourceforge.waters.gui.renderer.GeneralShape;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.Handle.HandleType;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.renderer.LayoutMode;
import net.sourceforge.waters.gui.renderer.MiscShape;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShape;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingInformation;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.ConfigBridge;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineKind;
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
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NestedBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.apache.logging.log4j.LogManager;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.IDEToolBar;
import org.supremica.gui.ide.ModuleContainer;
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
                          final ModuleContainer moduleContainer,
                          final ComponentEditorPanel root,
                          final IDEToolBar toolbar,
                          final WatersPopupActionManager manager)
    throws GeometryAbsentException
  {
    super(graph, module, root.getModuleWindowInterface().getModuleContext());
    mRoot = root;
    mModuleContainer = moduleContainer;
    mToolbar = toolbar;
    mZoomFactor = 1.0;
    mAdjustedZoomFactor = IconAndFontLoader.GLOBAL_SCALE_FACTOR;
    mRenderingContext = new EditorRenderingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, mRenderingContext);
    setShapeProducer(producer);
    mPopupFactory =
      manager == null ? null : new GraphPopupFactory(manager, root);
    setFocusable(true);
    addKeyListener(new KeyHandler());
    updateTool();
    ensureGeometryExists();
    mHasGroupNodes = GraphTools.updateGroupNodeHierarchy(graph);
    mNeedsHierarchyUpdate = false;
    mBoundsMayHaveChanged = true;
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
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * Creates an immutable controlled surface.
   */
  public GraphEditorPanel(final GraphSubject graph,
                          final ModuleSubject module)
    throws GeometryAbsentException
  {
    this(graph, module, null, null, null, null);
  }


  //#########################################################################
  //# Simple Access
  public UndoInterface getUndoInterface()
  {
    return mRoot.getUndoInterface();
  }

  public ComponentEditorPanel getComponentEditorPanel()
  {
    return mRoot;
  }

  /**
   * Returns the position where items can be pasted in this panel.
   * This is either the current position of the mouse cursor,
   * or the center of the viewport, if the cursor is not within the
   * window.
   */
  public Point getPastePosition()
  {
    Point point;
    if (mCurrentPoint == null) {
      final Rectangle rect = getVisibleRect();
      final int x = rect.x + (rect.width >> 1);
      final int y = rect.y + (rect.height >> 1);
      point = new Point(x, y);
      point = applyInverseTransform(point);
    } else {
      point = mCurrentPoint;
    }
    return findGrid(point);
  }


  //#########################################################################
  //# Overrides for javax.swing.JComponent
  @Override
  public String getToolTipText(final MouseEvent event)
  {
    final ModuleCompilationErrors errors =
      getModuleContext().getCompilationErrors();
    ProxySubject item = getDraggableItem(event, false);
    if (item == null) {
      return null;
    } else if (item instanceof SimpleNodeProxy) {
      return errors.getSummaryMessage(item);
    } else if (item.getParent() instanceof SimpleNodeProxy) {
      final SimpleNodeProxy parent = (SimpleNodeProxy) item.getParent();
      if (item == parent.getLabelGeometry()) {
        return errors.getSummaryMessage(parent);
      }
    } else if (item instanceof GuardActionBlockSubject) {
      final GuardActionBlockSubject ga = (GuardActionBlockSubject) item;
      item = getGuardOrAction(ga, event);
    }
    return errors.getDetailedMessage(item);
  }


  //#########################################################################
  //# Repaint Support
  @Override
  public void registerSupremicaPropertyChangeListeners()
  {
    super.registerSupremicaPropertyChangeListeners();
    Config.GUI_EDITOR_ICONSET.addOptionChangeListener(this);
    Config.GUI_EDITOR_SHOW_GRID.addOptionChangeListener(this);
    Config.GUI_EDITOR_GRID_SIZE.addOptionChangeListener(this);
  }

  @Override
  public void unregisterSupremicaPropertyChangeListeners()
  {
    super.registerSupremicaPropertyChangeListeners();
    Config.GUI_EDITOR_ICONSET.removeOptionChangeListener(this);
    Config.GUI_EDITOR_SHOW_GRID.removeOptionChangeListener(this);
    Config.GUI_EDITOR_GRID_SIZE.removeOptionChangeListener(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.transfer.SelectionOwner
  @Override
  public UndoInterface getUndoInterface(final Action action)
  {
    return getUndoInterface();
  }

  @Override
  public boolean hasNonEmptySelection()
  {
    return !mSelection.isEmpty();
  }

  @Override
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

  @Override
  public boolean isSelected(final Proxy proxy)
  {
    return mSelection.isSelected(proxy);
  }

  @Override
  public List<ProxySubject> getCurrentSelection()
  {
    return mSelection.asList();
  }

  @Override
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

  @Override
  public ProxySubject getSelectionAnchor()
  {
    return mSelection.getSelectionAnchor();
  }

  @Override
  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  @Override
  public Proxy getSelectableAncestor(final Proxy item)
  {
    return mSelectableAncestorVisitor.getSelectableAncestor(item);
  }

  @Override
  public void replaceSelection(final List<? extends Proxy> items)
  {
    if (mSelection.replace(items)) {
      fireSelectionChanged();
    }
  }

  @Override
  public void addToSelection(final List<? extends Proxy> items)
  {
    if (mSelection.selectAll(items)) {
      fireSelectionChanged();
    }
  }

  @Override
  public void removeFromSelection(final List<? extends Proxy> items)
  {
    if (mSelection.deselectAll(items)) {
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
            blocked.getEventIdentifierListModifiable();
          final ListSubject<? extends ProxySubject> neweventlist =
            newblocked.getEventIdentifierListModifiable();
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
      mIdentifierPasteVisitor.addInsertInfo(target, transferable, inserts);
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
           ProxyTools.getShortClassName(target));
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

  @Override
  public boolean canDelete(final List<? extends Proxy> items)
  {
    for (final Proxy proxy : items) {
      if (!(proxy instanceof LabelGeometryProxy)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    return mSelection.getDeletionVictims();
  }

  @Override
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
                 proxy instanceof NestedBlockSubject) {
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<?> untyped = inspos.getList();
        @SuppressWarnings("unchecked")
        final List<Proxy> eventlist = (List<Proxy>) untyped;
        int pos = inspos.getPosition();
        if (pos < 0) {
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
          final List<AbstractSubject> list = block.getEventIdentifierListModifiable();
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

  @Override
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
                 proxy instanceof NestedBlockSubject) {
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
          final List<AbstractSubject> list = block.getEventIdentifierListModifiable();
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

  @Override
  public void scrollToVisible(final List<? extends Proxy> list)
  {
    if (!list.isEmpty()) {
      // Beware---the list may contain identifiers, but the proxy
      // shape producer does not support them :-(
      final Set<Proxy> scrollable = new THashSet<Proxy>();
      for (Proxy proxy : list) {
        if (proxy instanceof IdentifierSubject ||
            proxy instanceof NestedBlockSubject) {
          final Subject subject = (Subject) proxy;
          final ProxySubject parent =
            (ProxySubject) SubjectTools.getAncestor(subject,
                                                    LabelBlockSubject.class,
                                                    SimpleNodeSubject.class);
          proxy = parent;
        }
        final Proxy copy = getCopy(proxy);
        if (copy == null) {
          scrollable.add(proxy);
        } else {
          scrollable.add(copy);
        }
      }
      final Rectangle2D bounds =
        getShapeProducer().getMinimumBoundingRectangle(scrollable);
      final AffineTransform transform = getTransform();
      final Point2D p1 = new Point2D.Double(bounds.getX(), bounds.getY());
      final Point2D p2 = transform.transform(p1, null);
      final int x0 = (int) p2.getX();
      final int y0 = (int) p2.getY();
      p1.setLocation(bounds.getMaxX(), bounds.getMaxY());
      transform.transform(p1, p2);
      final int x1 = (int) Math.ceil((int) p2.getX());
      final int y1 = (int) Math.ceil((int) p2.getY());
      // reduce the displayed area to viewport size,
      // otherwise Java scrolls to bottom-right rather than top-left
      final Rectangle view = getVisibleRect();
      final int width = Math.min(x1 - x0, view.width);
      final int height = Math.min(y1 - y0, view.height);
      final Rectangle rect = new Rectangle(x0, y0, width, height);
      scrollRectToVisible(rect);
    }
  }

  @Override
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

  @Override
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
    ToolTipManager.sharedInstance().unregisterComponent(this);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<Observer>();
    }
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
    if (mObservers.isEmpty()) {
      mObservers = null;
    }
  }

  @Override
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
  @Override
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
  @Override
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
  //# Interface java.awt.event.FocusListener
  @Override
  public void focusGained(final FocusEvent event)
  {
    // System.err.println("focus gained : " + event.isTemporary());
    if (!event.isTemporary()) {
      repaint();
    }
  }

  @Override
  public void focusLost(final FocusEvent event)
  {
    // System.err.println("focus lost : " + event.isTemporary());
    if (!event.isTemporary()) {
      repaint();
    }
  }


  //#########################################################################
  //# Repaint Support
  @Override
  protected void graphChanged(final ModelChangeEvent event)
  {
    checkGroupNodeHierarchyUpdate(event);
    updateOverlap();
    mController.updateHighlighting();
    super.graphChanged(event);
    mBoundsMayHaveChanged = true;
  }


  //#########################################################################
  //# Repainting and Resizing
  public double getZoomFactor()
  {
    return mZoomFactor;
  }

  public double getZoomFactorToFit()
  {
    final Rectangle2D area = getShapeProducer().getMinimumBoundingRectangle();
    if (area.isEmpty()) {
      return 1.0;
    } else {
      final int x = (int) Math.floor(area.getX());
      final int grid = ConfigBridge.getGridSize();
      final int x0 = Math.floorDiv(x - LOWER_MARGIN, grid) * grid;
      final double width = area.getWidth() + 2.0 * (area.getX() - x0);
      final int y = (int) Math.floor(area.getY());
      final int y0 = Math.floorDiv(y - LOWER_MARGIN, grid) * grid;
      final double height = area.getHeight() + 2.0 * (area.getY() - y0);
      final Rectangle view = getVisibleRect();
      return Math.min(view.width / width, view.height / height);
    }
  }

  public void setZoomFactor(final double zoom)
  {
    if (mZoomFactor != zoom) {
      mZoomFactor = zoom;
      mAdjustedZoomFactor = zoom * IconAndFontLoader.GLOBAL_SCALE_FACTOR;
      mBoundsMayHaveChanged = true;
      mCurrentBounds = null;
      repaint();
    }
  }

  @Override
  protected void paintComponent(final Graphics graphics)
  {
    if (mCurrentBounds == null || mInternalDragAction == null) {
      adjustSize();
    }
    super.paintComponent(graphics);
  }

  @Override
  protected AffineTransform createTransform()
  {
    adjustSize();
    final AffineTransform transform = new AffineTransform();
    transform.translate(-mCurrentBounds.x, -mCurrentBounds.y);
    transform.scale(mAdjustedZoomFactor, mAdjustedZoomFactor);
    return transform;
  }

  /**
   * Recalculates the canvas size. This method is called during redraw or
   * when graph change has been detected. It calculates the graph bounds
   * and estimates a suitable size for the canvas. If necessary, the canvas
   * is resized by changing the preferred size and revalidating. In addition,
   * if the graph grows or shrinks to the top or left, the viewport position
   * (i.e. scrollbars) is adjusted, so that only the scrollbars change but
   * the graph does not move. Nevertheless, scrolling may still occur when
   * triggered by {@link #scrollToVisible(List) scrollToVisible()}.
   */
  private void adjustSize()
  {
    if (!mBoundsMayHaveChanged) {
      return;
    }
    mBoundsMayHaveChanged = false;

    // 1. Find the area covered by the graph plus margins
    final Rectangle2D area2D = getShapeProducer().getMinimumBoundingRectangle();
    final Rectangle bounds = area2D.getBounds();
    if (!bounds.isEmpty()) {
      final int grid = ConfigBridge.getGridSize();
      final double gz = grid * mAdjustedZoomFactor;
      final int x0 = (int) (gz *
        Math.floorDiv(bounds.x - LOWER_MARGIN, grid));
      final int x1 = (int) Math.ceil(gz *
        Math.floorDiv((int) bounds.getMaxX() + grid - 1 + UPPER_MARGIN, grid));
      final int y0 = (int) (gz *
        Math.floorDiv(bounds.y - LOWER_MARGIN, grid));
      final int y1 = (int) Math.ceil(gz *
        Math.floorDiv((int) bounds.getMaxY() + grid - 1 + UPPER_MARGIN, grid));
      bounds.setBounds(x0, y0, x1 - x0, y1 - y0);
    }

    // 2. Include the top-left corner of the viewport in the area
    final Container parent = getParent();
    final boolean useViewPort =
      mCurrentBounds != null && parent != null && parent instanceof JViewport;
    if (useViewPort) {
      final Rectangle view = getVisibleRect();
      final int x = view.x + mCurrentBounds.x;
      final int y = view.y + mCurrentBounds.y;
      bounds.add(x, y);
      // If the top-left corner is not (0,0), also include the top-right
      // and/or bottom-left. to stop Swing from scrolling the graph
      if (x > bounds.x) {
        bounds.add(x + view.width, y);
      }
      if (y > bounds.y) {
        bounds.add(x, y + view.height);
      }
    }

    // 3. If the bounds have changed, set the preferred size and view position
    if (mCurrentBounds != null && mCurrentBounds.equals(bounds)) {
      return;
    }
    final Dimension preferredSize = bounds.getSize();
    setPreferredSize(preferredSize);
    revalidate();
    if (useViewPort &&
        (bounds.x != mCurrentBounds.x || bounds.y != mCurrentBounds.y)) {
      final JViewport viewport = (JViewport) parent;
      final Point viewPosition = viewport.getViewPosition();
      viewPosition.x += mCurrentBounds.x - bounds.x;
      viewPosition.y += mCurrentBounds.y - bounds.y;
      viewport.setViewPosition(viewPosition);
    }
    clearTransform();
    mCurrentBounds = bounds;
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
      removeMouseListener(mController);
      removeMouseMotionListener(mController);
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
    final GraphInsertPosition inspos = new GraphInsertPosition(graph);
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
    final GraphInsertPosition inspos = new GraphInsertPosition(graph);
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
    if (!mSelection.isEmpty()) {
      mSelection.clear();
      fireSelectionChanged();
    }
  }

  /**
   * Replaces the selection.
   * This method ensures that only the given item is selected.
   */
  private void replaceSelection(final ProxySubject item)
  {
    if (mSelection.replace(item)) {
      fireSelectionChanged();
    }
  }

  private void replaceSelection(final GraphSelection newSelection)
  {
    mSelection = newSelection;
    fireSelectionChanged();
  }

  /**
   * Adds an item to the selection.
   */
  private void addToSelection(final ProxySubject item)
  {
    if (mSelection.select(item)) {
      fireSelectionChanged();
    }
  }

  private boolean isTrackedFocusOwner()
  {
    final IDE ide = mRoot.getModuleWindowInterface().getRootWindow();
    final FocusTracker tracker = ide.getFocusTracker();
    return tracker.getWatersSelectionOwner() == GraphEditorPanel.this;
  }


  //#########################################################################
  //# Low-level Selection Handling
  private void fireSelectionChanged()
  {
    mLastCommand = null;
    mDisplacement = null;
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
    } else {
      return mSelection.isRenderedSelected(original);
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


  @Override
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
  //# Overlap Display
  private void updateOverlap()
  {
    if (!mIsCommittingSecondaryGraph) {
      mOverlap.clear();
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
              mOverlap.add(node1);
              mOverlap.add(node2);
            }
          }
        }
      }
    }
  }

  private boolean isOverlap(final ProxySubject subject)
  {
    return mOverlap.contains(subject);
  }


  //#########################################################################
  //# Geometry Auxiliaries
  /**
   * Returns the closest coordinate (works for both x and y) lying on the grid.
   */
  private int findGrid(final double x)
  {
    if (Config.GUI_EDITOR_SHOW_GRID.getValue()) {
      final int grid = Config.GUI_EDITOR_GRID_SIZE.getValue();
      return grid * (int) Math.round(x / grid);
    } else {
      return (int) Math.round(x);
    }
  }

  /**
   * Returns the closest coordinate (works for both x and y) lying on the grid.
   */
  private int findGrid(final int x)
  {
    if (Config.GUI_EDITOR_SHOW_GRID.getValue()) {
      final int grid = Config.GUI_EDITOR_GRID_SIZE.getValue();
      return grid * (int) Math.round((double) x / (double) grid);
    } else {
      return x;
    }
  }

  /**
   * Finds the closest point to p lying on the grid.
   */
  private Point findGrid(final Point point)
  {
    final int x = findGrid(point.x);
    final int y = findGrid(point.y);
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
  private Collection<Proxy> getFocusableObjectsAtPosition
    (final Point point)
  {
    final Collection<Proxy> collection = new LinkedList<Proxy>();
    final GraphProxy graph = getDrawnGraph();
    for (final NodeProxy node : graph.getNodes()) {
      collectFocusableObjectAtPosition(node, point, collection);
      if (node instanceof SimpleNodeSubject) {
        final SimpleNodeSubject simple = (SimpleNodeSubject) node;
        final LabelGeometrySubject geo = simple.getLabelGeometry();
        collectFocusableObjectAtPosition(geo, point, collection);
      }
    }
    for (final EdgeProxy edge : graph.getEdges()) {
      collectFocusableObjectAtPosition(edge, point, collection);
      final LabelBlockProxy block = edge.getLabelBlock();
      final List<Proxy> events = block.getEventIdentifierList();
      final Collection<Proxy> collection2 = new LinkedList<Proxy>();
      for (final Proxy sub : events){
        collectFocusableObjectAtPosition(sub, point, collection2);
        if (sub instanceof NestedBlockSubject){
          final NestedBlockSubject nested = (NestedBlockSubject) sub;
          iterateNested(nested, point, collection2);
        }
      }
      //only select the labelblock if the focus is on the labels within or if
      //the labelblock is already selected or if its a DND action
      if(!collection2.isEmpty() || isSelected(block) || mInternalDragAction instanceof InternalDragActionDND){
        collectFocusableObjectAtPosition(block, point, collection);
      }
      final GuardActionBlockProxy ga = edge.getGuardActionBlock();
      collectFocusableObjectAtPosition(ga, point, collection);
    }
    collectFocusableObjectAtPosition
      (graph.getBlockedEvents(), point, collection);
    return collection;
  }

  private void iterateNested(final NestedBlockSubject nested,
                             final Point point,
                             final Collection<Proxy> collection)
  {
    for (final ProxySubject sub : nested.getBodyModifiable()) {
      collectFocusableObjectAtPosition(sub, point, collection);
      if (sub instanceof NestedBlockSubject) {
        final NestedBlockSubject nested2 = (NestedBlockSubject) sub;
        iterateNested(nested2, point, collection);
      }
    }
  }

  private void collectFocusableObjectAtPosition
    (final Proxy item,
     final Point point,
     final Collection<Proxy> collection)
  {
    if (item != null && getHighlightPriority(item) >= 0) {
      final ProxyShape shape = getShapeProducer().getShape(item);
      if (shape != null && shape.isClicked(point)) {
        collection.add(item);
      }
    }
  }

  private int getHighlightPriority(final Proxy item)
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
   * @param  point  The position of the mouse click.
   * @return The handle clicked, or <CODE>null</CODE>
   */
  private Handle getClickedHandle(final ProxySubject item,
                                  final Point point)
  {
    if (mSelection.isSingleSelectedItem(item)) {
      final ProxyShapeProducer producer = getShapeProducer();
      final ProxyShape shape = producer.getShape(item);
      return shape.getClickedHandle(point);
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
   * @param  ifSelected  Flag, if set to <CODE>true</CODE> then
   *                     only return items within a label block if
   *                     that label block is selected.
   * @return The item to be dragged or <CODE>null</CODE>
   */
  private ProxySubject getDraggableItem(final MouseEvent event,
                                        final boolean ifSelected)
  {
    final ProxySubject item = mFocusedObject;
    if (item == null) {
      return null;
    } else if (item instanceof LabelBlockSubject) {
      final LabelBlockSubject block = (LabelBlockSubject) item;
      if (ifSelected && !mSelection.isSelectedLabelBlock(block)) {
        return block;
      }
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      final ProxySubject label =
        getLabelToBeSelected(block.getEventIdentifierListModifiable(), point);
      return label == null ? block : label;
    } else {
      return item;
    }
  }

  private ProxySubject getGuardOrAction(final GuardActionBlockSubject ga,
                                        final MouseEvent event)
  {
    final Point mousePosition = event.getPoint();
    final Point point = applyInverseTransform(mousePosition);
    final List<SimpleExpressionProxy> guards =
      ProxyShapeProducer.getDisplayedGuards(ga);
    ProxySubject label = getLabelToBeSelected(guards, point);
    if (label == null) {
      label = getLabelToBeSelected(ga.getActionsModifiable(), point);
    }
    return label;
  }

  private ProxySubject getLabelToBeSelected
      (final List<? extends Proxy> list, final Point point)
  {
    for (final Proxy proxy : list) {
      final ProxyShape shape = getShapeProducer().getShape(proxy);
      if (shape.getShape().contains(point)) {
        return (ProxySubject) proxy;
      }
      if (proxy instanceof NestedBlockSubject) {
        final NestedBlockSubject nested = (NestedBlockSubject) proxy;
        final ProxySubject sub =
          getLabelToBeSelected(nested.getBodyModifiable(), point);
        if (sub != null) {
          return sub;
        }
      }
    }
    return null;
  }


  //#########################################################################
  //# Secondary Graph
  @Override
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

  private void addInsertInfo(final List<InsertInfo> inserts,
                             final List<? extends Proxy> data,
                             final ListSubject<? extends ProxySubject> list,
                             int pos)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    for (final Proxy item : data) {
      if (!eq.contains(list, item)) {
        addInsertInfo(inserts, item, list, pos);
        if (pos >= 0) {
          pos++;
        }
      }
    }
  }


  private boolean isContiguous(final List<InsertInfo> deletes)
  {
    ListSubject<? extends ProxySubject> list = null;
    int index = -1;
    for (final InsertInfo info : deletes) {
      final ListInsertPosition pos =
        (ListInsertPosition) info.getInsertPosition();
      if (list == null) {
        list = pos.getList();
        index = pos.getPosition();
      } else if (pos.getList() == list && pos.getPosition() == index + 1) {
        index++;
      } else {
        return false;
      }
    }
    return true;
  }

  private int adjustInsertPosition
    (final int targetIndex,
     final ListSubject<? extends ProxySubject> target,
     final List<InsertInfo> deletes,
     final boolean checkingPositions)
  {
    int adjustedIndex = targetIndex;
    if (targetIndex > 0) {
      for (final InsertInfo info : deletes) {
        final ListInsertPosition pos =
          (ListInsertPosition) info.getInsertPosition();
        final ListSubject<? extends ProxySubject> list = pos.getList();
        if (list == target) {
          if (checkingPositions) {
            final Proxy item = info.getProxy();
            if (list.indexOf(item) >= targetIndex) {
              continue;
            }
          }
          adjustedIndex--;
        }
      }
    }
    return adjustedIndex;
  }

  private List<InsertInfo> createInserts
    (final List<InsertInfo> deletes,
     final ListSubject<? extends ProxySubject> target,
     final int startIndex,
     final boolean copying)
  {
    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    final List<InsertInfo> inserts = new ArrayList<>(deletes.size());
    int index = startIndex;
    for (final InsertInfo deleteInfo : deletes) {
      final Proxy proxy = deleteInfo.getProxy();
      final Proxy clone = copying ? cloner.getClone(proxy) : proxy;
      final ListInsertPosition insPos =
        new ListInsertPosition(target, index);
      final InsertInfo insertInfo = new InsertInfo(clone, insPos);
      inserts.add(insertInfo);
      if (index >= 0) {
        index++;
      }
    }
    return inserts;
  }


  //##########################################################################
  //# Inner Class EditorRenderingContext
  private class EditorRenderingContext extends ModuleRenderingContext
  {

    //#######################################################################
    //# Constructor
    private EditorRenderingContext()
    {
      super(getModuleContext());
      mErrorVisitor = new ErrorVisitor();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
    @Override
    public RenderingInformation getRenderingInformation(final Proxy proxy,
                                                        final ColorGroup group)
    {
      final ProxySubject item = (ProxySubject) proxy;
      final boolean focused = isRenderedFocused(item);
      final boolean selected = isRenderedSelected(item);
      final DragOverStatus dragOver;
      if (focused && mInternalDragAction != null) {
        dragOver = mInternalDragAction.getExternalDragStatus();
      } else {
        dragOver = DragOverStatus.NOTDRAG;
      }
      final boolean hasFocus = isTrackedFocusOwner();
      final ProxySubject orig = getOriginal(item);
      final ErrorRendering errorRendering =
        mErrorVisitor.getErrorRendering(orig);
      final boolean error =
        isOverlap(item) || errorRendering == ErrorRendering.COLOURED;
      final LayoutMode layout = Config.GUI_EDITOR_LAYOUT_MODE.getValue();
      final Color color =
        layout.getColor(group, dragOver, selected, error, hasFocus);
      if (color == null) {
        return null;
      }
      final Color shadow = EditorColor.shadow(color);
      final boolean underlined = errorRendering == ErrorRendering.UNDERLINED;
      final boolean showHandles = selected &&
        mSelection.getMode() == GraphSelection.SelectionMode.SUBGRAPH_SINGLE;
      int priority = getPriority(item);
      if (mDontDraw.contains(item)) {
        priority = -1;
      } else if (selected) {
        priority += 6;
      }
      return new RenderingInformation
        (selected, showHandles, underlined, focused, color, shadow, priority);
    }

    //#######################################################################
    //# Data Members
    private final ErrorVisitor mErrorVisitor;
  }


  //#########################################################################
  //# Inner Enumeration ErrorRendering
  private static enum ErrorRendering {
    NONE,
    UNDERLINED,
    COLOURED
  }


  //#########################################################################
  //# Inner Class ErrorVisitor
  private class ErrorVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private ErrorRendering getErrorRendering(final Proxy proxy)
    {
      try {
        return (ErrorRendering) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public ErrorRendering visitProxy(final Proxy proxy)
    {
      final ModuleCompilationErrors errors = getErrors();
      if (errors.isUnderlined(proxy)) {
        return ErrorRendering.UNDERLINED;
      } else if (errors.hasErrorIcon(proxy)) {
        return ErrorRendering.COLOURED;
      } else {
        return ErrorRendering.NONE;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitConditionalProxy(final ConditionalProxy cond)
      throws VisitorException
    {
      final SimpleExpressionProxy guard = cond.getGuard();
      return guard.acceptVisitor(this);
    }

    @Override
    public ErrorRendering visitEdgeProxy(final EdgeProxy edge)
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      if (block != null && block.getEventIdentifierList().isEmpty()) {
        final ModuleCompilationErrors errors = getErrors();
        if (errors.hasErrorIcon(edge)) {
          return ErrorRendering.COLOURED;
        }
      }
      return ErrorRendering.NONE;
    }

    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      final SimpleExpressionProxy range = foreach.getRange();
      return range.acceptVisitor(this);
    }

    @Override
    public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
      throws VisitorException
    {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final ProxySubject parent = (ProxySubject) subject.getParent();
      return parent.acceptVisitor(this);
    }

    @Override
    public ErrorRendering visitNodeProxy(final NodeProxy node)
    {
      final ModuleCompilationErrors errors = getErrors();
      if (errors.hasErrorIcon(node)) {
        return ErrorRendering.COLOURED;
      } else {
        return ErrorRendering.NONE;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private ModuleCompilationErrors getErrors()
    {
      return getModuleContext().getCompilationErrors();
    }
  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class EventDeclListModelObserver implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      switch (event.getKind()) {
      case ModelChangeEvent.GEOMETRY_CHANGED:
      case ModelChangeEvent.GENERAL_NOTIFICATION:
        repaint();
        break;
      default:
        break;
      }
    }

    @Override
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
    //# Simple Access
    Point getStartPoint()
    {
      return mStartPoint;
    }

    //#######################################################################
    //# Highlighting and Selecting
    int getHighlightPriority(final Proxy item)
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
      final Proxy object;
      if (mCurrentPoint == null) {
        object = null;
      } else {
        final Collection<Proxy> objects =
          getFocusableObjectsAtPosition(mCurrentPoint);
        if (objects.isEmpty()) {
          object = null;
        } else {
          final Proxy max = Collections.max(objects, mComparator);
          object = getOriginal(max);
        }
      }
      if (object != mFocusedObject) {
        mFocusedObject = (ProxySubject) object;
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
        final Point mousePosition = event.getPoint();
        final Point point = applyInverseTransform(mousePosition);
        mInternalDragAction.cancelDrag(point);
        mInternalDragAction = null;
        repaint();
      }
    }

    /**
     * Handles a double-click event on a label block.
     * This method checks whether there is a nested block (conditional or
     * foreach) at the mouse position, and if so invokes an editor dialog.
     */
    void handleLabelBlockDoubleClick(final MouseEvent event)
    {
      final ProxySubject item = getDraggableItem(event, true);
      if (item != null && item instanceof NestedBlockSubject) {
        final IDE ide = mRoot.getModuleWindowInterface().getRootWindow();
        final IDEAction action = new IDEPropertiesAction(ide, item);
        action.execute(event.getSource());
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
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mouseClicked");
      if (mClickedItem != null) {
        final boolean changed;
        if (mToggleMode) {
          changed = mSelection.deselect(mClickedItem);
        } else {
          changed = mSelection.replace(mClickedItem);
        }
        if (changed) {
          fireSelectionChanged();
        }
        mClickedItem = null;
      }
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mousePressed");
      // Issue #122: Ensure focus is transferred when clicked
      requestFocusInWindow();
      mClickedItem = null;
      final ProxySubject item = getDraggableItem(event, true);
      final boolean changed;
      if (canBeSelected(item)) {
        if (event.isShiftDown()) {
          changed = mSelection.shiftSelect(item);
        } else if (event.isControlDown()) {
          changed = mSelection.select(item);
          if (!changed) {
            mClickedItem = item;
            mToggleMode = true;
          }
        } else if (mSelection.isSelected(item)) {
          changed = false;
          mClickedItem = item;
          mToggleMode = false;
        } else {
          changed = mSelection.replace(item);
        }
      } else {
        if (event.isShiftDown() || event.isControlDown()) {
          changed = mSelection.clearLabelSelection();
        } else {
          changed = mSelection.clear();
        }
      }
      if (changed) {
        fireSelectionChanged();
      }

      final Point mousePosition = event.getPoint();
      mStartPoint = applyInverseTransform(mousePosition);
      maybeShowPopup(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mouseReleased");
      mStartPoint = null;
      maybeShowPopup(event);
      if (mInternalDragAction != null) {
        final Point mousePosition = event.getPoint();
        final Point point = applyInverseTransform(mousePosition);
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

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mouseDragged");
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      updateHighlighting(point);
      if (mInternalDragAction != null) {
        mInternalDragAction.continueDrag(event);
        mClickedItem = null;
      }
    }

    @Override
    public void mouseEntered(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mouseEntered");
      abortExternalDrag(event);
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      updateHighlighting(point);
    }

    @Override
    public void mouseExited(final MouseEvent event)
    {
      // LogManager.getLogger().info("ToolController.mouseExited");
      if (mInternalDragAction != null) {
        abortExternalDrag(event);
        final Point mousePosition = event.getPoint();
        final Point point = applyInverseTransform(mousePosition);
        updateHighlighting(point);
      } else {
        updateHighlighting(null);
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseMotionListener
    @Override
    public void mouseMoved(final MouseEvent event)
    {
      abortExternalDrag(event);
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      updateHighlighting(point);
    }

    //#######################################################################
    //# Auxiliary Methods
    private void maybeShowPopup(final MouseEvent event)
    {
      ProxySubject item = null;
      if (mFocusedObject instanceof LabelBlockProxy) {
        item = getDraggableItem(event, true);
      }
      if (item == null) {
        item = mFocusedObject;
      }
      mPopupFactory.maybeShowPopup(GraphEditorPanel.this, event, item);
    }

    //#######################################################################
    //# Data Members
    private Point mStartPoint;
    private ProxySubject mClickedItem;
    private boolean mToggleMode;
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
    @Override
    public void mousePressed(final MouseEvent event)
    {
      abortExternalDrag(event);
      requestFocusInWindow();
      // No popup!
    }

    @Override
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
    @Override
    int getHighlightPriority(final Proxy item)
    {
      if (item instanceof LabelGeometryProxy) {
        return 6;
      } else if (item instanceof SimpleNodeProxy) {
        return 5;
      } else if (item instanceof EdgeProxy) {
        return 4;
      } else if (item instanceof LabelBlockProxy ||
                 item instanceof GuardActionBlockProxy) {
        return 3;
      } else if (item instanceof GroupNodeProxy) {
        return 2;
      } else if (item instanceof IdentifierProxy ||
                 item instanceof NestedBlockProxy) {
        return 1;
      }else {
        return -1;
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      // LogManager.getLogger().info("SelectController.mouseClicked");
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
          EdgeEditorDialog.showDialog((EdgeSubject) mFocusedObject, mRoot);
        } else if (mFocusedObject instanceof GuardActionBlockSubject) {
          final EdgeSubject edge = (EdgeSubject) mFocusedObject.getParent();
          EdgeEditorDialog.showDialog(edge, mRoot);
        } else if (mFocusedObject instanceof LabelBlockSubject) {
          handleLabelBlockDoubleClick(event);
        } else if (mFocusedObject instanceof NodeSubject) {
          final NodeSubject node = (NodeSubject) mFocusedObject;
          NodeEditorDialog.showDialog(mModuleContainer,
                                      GraphEditorPanel.this, node);
        }
      }
    }

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      // LogManager.getLogger().info
      // ("SelectController.mouseDragged [" + (mInternalDragAction != null) + "]");
      if (mInternalDragAction == null) {
        final Point start = getStartPoint();
        if (mFocusedObject == null) {
          mInternalDragAction = new InternalDragActionSelect
            (start, event.isShiftDown() || event.isControlDown());
        } else {
          final ProxySubject subject = getDraggableItem(event, true);
          if (mFocusedObject == subject || !isSelected(subject)) {
            final Handle handle = getClickedHandle(subject, start);
            if (handle == null) {
              mInternalDragAction = new InternalDragActionMove
                (start, event.isShiftDown() || event.isControlDown());
            } else {
              switch (handle.getType()) {
              case INITIAL:
                mInternalDragAction = new InternalDragActionInitial(start);
                break;
              case SOURCE:
              case TARGET:
                mInternalDragAction =
                  new InternalDragActionEdge(handle, start);
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
                  new InternalDragActionResizeGroupNode(handle, start);
                break;
              default:
                throw new IllegalStateException("Unknown handle type: " +
                                                handle.getType());
              }
            }
          } else {
            mInternalDragAction = new InternalDragActionDND(start);
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
    @Override
    int getHighlightPriority(final Proxy item)
    {
      if (item instanceof LabelGeometryProxy) {
        return 2;
      } if (item instanceof SimpleNodeProxy) {
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
    @Override
    void updateHighlighting()
    {
      super.updateHighlighting();
      if (mCurrentPoint != null &&
          mFocusedObject == null &&
          mInternalDragAction == null &&
          Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue()) {
        final ProxyShapeProducer shaper = getShapeProducer();
        final Point snapped = findGrid(mCurrentPoint);
        if (!snapped.equals(mCurrentPoint)) {
          for (final NodeSubject node : getGraph().getNodesModifiable()) {
            if (node instanceof SimpleNodeSubject) {
              final ProxyShape shape = shaper.getShape(node);
              if (shape.isClicked(snapped)) {
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
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      final boolean noSelections = mSelection.isEmpty();
      super.mouseClicked(event);
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (event.getClickCount() == 1 && mFocusedObject == null) {
          // Create node.
          final Point mousePosition = event.getPoint();
          final Point point = applyInverseTransform(mousePosition);
          final Point snapped;
          if (Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue()) {
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
        } else if (event.getClickCount() == 2 && mFocusedObject != null) {
          if (mFocusedObject instanceof LabelGeometrySubject) {
            final SimpleNodeSubject node =
              (SimpleNodeSubject) mFocusedObject.getParent();
            editStateName(node);
          } else if (mFocusedObject instanceof SimpleNodeSubject) {
            final SimpleNodeSubject node = (SimpleNodeSubject)mFocusedObject;
            NodeEditorDialog.showDialog(mModuleContainer,
                                        GraphEditorPanel.this, node);
          }
        } else {
          mLastNodeCommand = null;
        }
      }
    }

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction =
            new InternalDragActionSelect(getStartPoint(),
                                         event.isShiftDown() || event.isControlDown());
        } else {
          final ProxySubject subject = getDraggableItem(event, true);
          if (mFocusedObject == subject || !isSelected(subject)) {
            final Handle handle = getClickedHandle(subject, getStartPoint());
            if (handle == null) {
              mInternalDragAction =
                new InternalDragActionMove(getStartPoint(),
                                           event.isShiftDown() || event.isControlDown());
            } else {
              if (handle.getType() == HandleType.INITIAL) {
                mInternalDragAction =
                  new InternalDragActionInitial(getStartPoint());
              } else {
                mInternalDragAction =
                  new InternalDragActionSelect(getStartPoint());
              }
            }
          }
        }
      }
      super.mouseDragged(event);
    }

    Command mLastNodeCommand = null;
  }


  //#########################################################################
  //# Inner Class GroupNodeController
  private class GroupNodeController
    extends ToolController
  {

    //#######################################################################
    //# Highlighting
    @Override
    int getHighlightPriority(final Proxy item)
    {
      if (item instanceof GroupNodeProxy) {
        return 1;
      } else {
        return -1;
      }
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      super.mouseClicked(event);
      //double clicks
      if (event.getButton() == MouseEvent.BUTTON1 &&
          event.getClickCount() == 2 &&
          mFocusedObject != null &&
          mFocusedObject instanceof GroupNodeSubject) {
        final GroupNodeSubject node = (GroupNodeSubject) mFocusedObject;
        NodeEditorDialog.showDialog(mModuleContainer,
                                    GraphEditorPanel.this, node);
      }
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      abortExternalDrag(event);
      super.mousePressed(event);
    }

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction =
            new InternalDragActionCreateGroupNode(getStartPoint());
        } else {
          final Handle handle = getClickedHandle(mFocusedObject, getStartPoint());
          if (handle == null) {
            mInternalDragAction =
              new InternalDragActionMove(getStartPoint(),
                                         event.isShiftDown() || event.isControlDown());
          } else {
            mInternalDragAction =
              new InternalDragActionResizeGroupNode(handle, getStartPoint());
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
    @Override
    int getHighlightPriority(final Proxy item)
    {
      if (item instanceof SimpleNodeProxy) {
        return 5;
      } else if (item instanceof GroupNodeProxy) {
        return 4;
      } else if (item instanceof EdgeProxy) {
        return 3;
      } else if (item instanceof LabelBlockProxy ||
                 item instanceof GuardActionBlockProxy) {
        return 2;
      } else if (item instanceof IdentifierSubject ||
                 item instanceof NestedBlockProxy) {
        return 1;
      }else {
        return -1;
      }
    }

    @Override
    boolean canBeSelected(final ProxySubject item)
    {
      return
        item instanceof EdgeSubject ||
        item instanceof LabelBlockSubject ||
        item instanceof GuardActionBlockSubject ||
        item instanceof IdentifierSubject ||
        item instanceof NestedBlockSubject;
    }

    //#######################################################################
    //# Interface java.awt.MouseListener
    @Override
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
          EdgeEditorDialog.showDialog((EdgeSubject) mFocusedObject, mRoot);
        } else if (mFocusedObject instanceof GuardActionBlockSubject) {
          final EdgeSubject edge = (EdgeSubject) mFocusedObject.getParent();
          EdgeEditorDialog.showDialog(edge, mRoot);
        } else if (mFocusedObject instanceof LabelBlockSubject) {
          handleLabelBlockDoubleClick(event);
        }
      }
    }

    @Override
    public void mouseDragged(final MouseEvent event)
    {
      if (mInternalDragAction == null) {
        if (mFocusedObject == null) {
          mInternalDragAction =
            new InternalDragActionSelect(getStartPoint(),
                                         event.isShiftDown() || event.isControlDown());
        } else {
          final ProxySubject item = getDraggableItem(event, true);
          if (mFocusedObject == item || !isSelected(item)){
            final Handle handle = getClickedHandle(item, getStartPoint());
            if (handle == null && canBeSelected(item)){
              mInternalDragAction =
                new InternalDragActionMove(getStartPoint(),
                                           event.isShiftDown() || event.isControlDown());
            } else if (item instanceof NodeSubject) {
              // Clicking on node or nodegroup --- create edge.
              mInternalDragAction = new InternalDragActionEdge(getStartPoint());
            } else if (item instanceof EdgeSubject) {
              if (handle == null) {
                mInternalDragAction =
                  new InternalDragActionMove(getStartPoint(),
                                             event.isShiftDown() || event.isControlDown());
              } else {
                mInternalDragAction =
                  new InternalDragActionEdge(handle, getStartPoint());
              }
            }
          }
          else{
            mInternalDragAction =
              new InternalDragActionDND(getStartPoint());
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
      if (point == null) {
        LogManager.getLogger().error
          ("Creating {} with null point!", ProxyTools.getShortClassName(this));
      }
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

    void modifiersChanged(final int key, final boolean pressed)
    {
    }

    //#######################################################################
    //# Temporary Selection
    void copyCurrentSelection()
    {
      mPreviousSelection = new GraphSelection(mSelection);
    }

    GraphSelection getPreviousSelection()
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
      if (ProxyTools.equals(mDragCurrent, point)) {
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
      mShiftDown = event.isShiftDown();
      mControlDown = event.isControlDown();
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      return continueDrag(point);
    }

    public boolean isControlDown(){
      return mControlDown;
    }

    public boolean isShiftDown(){
      return mShiftDown;
    }


    /**
     * Completes this internal dragging operation. This method is overridden
     * to create and executes the appropriate command to reflect all the
     * changed made during the drag operation. Subclasses must call the
     * superclass method also.
     * @param  point  The mouse position in graph coordinates.
     */
    void commitDrag(final Point point)
    {
    }

    /**
     * Cancels this operation. This method is called when the user has
     * only clicked rather than dragged the mouse. Sometimes the selection
     * needs to be updated in such a case. Subclasses must call the
     * superclass method also.
     * @param  point  The mouse position in graph coordinates.
     */
    void cancelDrag(final Point point)
    {
    }

    //#######################################################################
    //# Highlighting
    int getHighlightPriority(final Proxy item)
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
    private GraphSelection mPreviousSelection;
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
    private boolean mShiftDown;
    private boolean mControlDown;
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
    @Override
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        createSecondaryGraph();
        return true;
      } else {
        return false;
      }
    }

    @Override
    void commitDrag(final Point point)
    {
      super.commitDrag(point);
      mIsCommittingSecondaryGraph = true;
      commitSecondaryGraph();
      clearSecondaryGraph();
      mIsCommittingSecondaryGraph = false;
      updateOverlap();
    }

    @Override
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
      copyCurrentSelection();
      mShiftOrControlDown = false;
    }

    private InternalDragActionSelect(final Point start, final boolean shiftOrControlDown)
    {
      this(start);
      mShiftOrControlDown = shiftOrControlDown;
    }


    //#######################################################################
    //# Dragging
    @Override
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        final List<ProxySubject> dragged = getDragSelection();
        final GraphSelection prev = getPreviousSelection();
        final GraphSelection newSelection;
        if (mShiftOrControlDown) {
          newSelection = new GraphSelection(prev);
          for (final ProxySubject item : dragged) {
            newSelection.toggle(item);
          }
        } else {
          newSelection = new GraphSelection(dragged);
        }
        replaceSelection(newSelection);
        return true;
      } else {
        return false;
      }
    }

    //#######################################################################
    //# Rendering
    @Override
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
        final LabelBlockSubject block = edge.getLabelBlock();
        includeShape(selection, dragrect, block);
        final GuardActionBlockSubject ga = edge.getGuardActionBlock();
        includeShape(selection, dragrect, ga);
        includeShape(selection, dragrect, edge);
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
            dragrect.contains(shape.getBounds2D())) {
          selection.add(item);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mShiftOrControlDown;

  }


  //#########################################################################
  //# Inner Class InternalDragActionMove
  private class InternalDragActionMove
    extends BigInternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionMove(final Point start, final boolean shiftOrControlDown)
    {
      super(start);
      if (!shiftOrControlDown) {
        if (!isSelected(mFocusedObject)) {
          replaceSelection(mFocusedObject);
        }
      } else if (!isSelected(mFocusedObject)) {
        addToSelection(mFocusedObject);
      }
      mShouldCommit = true;
      Point2D snap = null;
      if (Config.GUI_EDITOR_SHOW_GRID.getValue() &&
          Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue()) {
        // Move operation snaps to grid when a node is moved.
        for (final ProxySubject item : mSelection) {
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

    @Override
    void modifiersChanged(final int key, final boolean pressed)
    {
      final Point start = getDragStart();
      int dx;
      int dy;
      if (key == KeyEvent.VK_CONTROL) {
        if (pressed || !shouldSnapToGrid()) {
          final Point next = nextPoint(start, mCurrentPoint);
          dx = next.x;
          dy = next.y;
        } else {
          final Point next = nextSnappedPoint(start, mCurrentPoint);
          setDragCurrent(mCurrentPoint, next);
          dx = getDragCurrentOnGrid().x - start.x;
          dy = getDragCurrentOnGrid().y - start.y;
        }
      } else {
        if (shouldSnapToGrid()) {
          final Point next = nextSnappedPoint(start, mCurrentPoint);
          setDragCurrent(mCurrentPoint, next);
        }
        dx = getDragCurrentOnGrid().x - start.x;
        dy = getDragCurrentOnGrid().y - start.y;
      }
      move(key == KeyEvent.VK_SHIFT && pressed, dx, dy);
    }

    //#######################################################################
    //# Simple Access
    @Override
    boolean shouldSnapToGrid()
    {
      return mSnapPoint != null;
    }

    @Override
    boolean createSecondaryGraph()
    {
      if (super.createSecondaryGraph()) {
        mMoveVisitor = new MoveVisitor();
        return true;
      } else {
        return false;
      }
    }

    private Point nextPoint(final Point start, final Point current)
    {
      final int dx = (int) (current.getX() - start.x);
      final int dy = (int) (current.getY() - start.y);
      return new Point(dx, dy);
    }

    private Point nextSnappedPoint(final Point start, final Point current)
    {
      final double rx = mSnapPoint.getX();
      final double ry = mSnapPoint.getY();
      final int ix = start.x;
      final int iy = start.y;
      final double x = current.getX() - ix;
      final double y = current.getY() - iy;
      final int sx = (int) Math.round(findGrid(rx + x) - rx);
      final int sy = (int) Math.round(findGrid(ry + y) - ry);
      final Point snapped = new Point(ix + sx, iy + sy);
      return snapped;
    }

    private void move(final boolean directional, int x, int y)
    {
      boolean edgeMove = false;
      if (mMoveVisitor.mMovedObjects.size() == 1) {
        final Iterator<ProxySubject> iter =
          mMoveVisitor.mMovedObjects.iterator();
        if (iter.next() instanceof EdgeSubject) {
          edgeMove = true;
        }
      }
      if (directional && !edgeMove) {
        if (Math.abs(x) < Math.abs(y)) {
          x = 0;
        } else {
          y = 0;
        }
      }
      // if the item is dragged to where it originally was then don't commit
      mShouldCommit = x != 0 || y != 0;
      mMoveVisitor.moveAll(x, y, directional && edgeMove, getDragStart());
    }

    //#######################################################################
    //# Dragging
    @Override
    boolean continueDrag(final Point point)
    {
      final Point start = getDragStart();
      int dx;
      int dy;
      if (isControlDown()) {
        final Point next = nextPoint(start, point);
        dx = next.x;
        dy = next.y;
      } else {
        if (shouldSnapToGrid()) {
          final Point next = nextSnappedPoint(start, point);
          if (next.equals(getDragCurrentOnGrid())) {
            return false;
          }
          setDragCurrent(point, next);
        } else if (!super.continueDrag(point)) {
          return false;
        }
        dx = getDragCurrentOnGrid().x - start.x;
        dy = getDragCurrentOnGrid().y - start.y;
      }
      createSecondaryGraph();
      move(isShiftDown(), dx, dy);
      return true;
    }

    @Override
    void commitSecondaryGraph()
    {
      if (mShouldCommit) {
        super.commitSecondaryGraph();
        adjustSize();
        final List<ProxySubject> movedObjects =
          new ArrayList<>(mMoveVisitor.mMovedObjects);
        scrollToVisible(movedObjects);
      }
    }

    //#######################################################################
    //# Data Members
    private final Point2D mSnapPoint;
    private MoveVisitor mMoveVisitor;
    private boolean mShouldCommit;
  }


  //#########################################################################
  //# Inner Class InternalDragActionDND
  private class InternalDragActionDND
    extends InternalDragAction
  {

    //#######################################################################
    //# Constructors
    private InternalDragActionDND(final Point point)
    {
      super(point);
      mExternalDragStatus = DragOverStatus.NOTDRAG;
    }

    //#######################################################################
    //# Dragging
    @Override
    boolean continueDrag(final MouseEvent event)
    {
      final Point mousePosition = event.getPoint();
      final Point point = applyInverseTransform(mousePosition);
      final boolean draggedNow = super.continueDrag(point);
      if (draggedNow) {
        if (event.isShiftDown() || event.isControlDown()) {
          getTransferHandler().exportAsDrag(GraphEditorPanel.this, event,
                                            TransferHandler.COPY);
        } else {
          getTransferHandler().exportAsDrag(GraphEditorPanel.this, event,
                                            TransferHandler.MOVE);
        }
      }
      return draggedNow;
    }

    boolean canImport(final TransferSupport support)
    {
      final Point mousePosition = support.getDropLocation().getDropPoint();
      final Point point = applyInverseTransform(mousePosition);
      super.continueDrag(point);
      mController.updateHighlighting(point);

      ListSubject<AbstractSubject> elist = null;
      mDropAction = support.getDropAction();
      if (mDropAction == DnDConstants.ACTION_COPY ||
          mDropAction == DnDConstants.ACTION_COPY_OR_MOVE ||
          mDropAction == DnDConstants.ACTION_MOVE) {
        final Transferable transferable = support.getTransferable();
        if (transferable.isDataFlavorSupported(WatersDataFlavor.IDENTIFIER)) {
          elist = mIdentifierPasteVisitor.getIdentifierPasteTarget
            (mFocusedObject, transferable);
        }
      }

      final Line2D oldLine = mLine;
      final Rectangle2D oldBox = mNestedBlockBox;
      mLine = null;
      mNestedBlockBox = null;
      if (elist != null) {
        final ProxySubject parent = SubjectTools.getProxyParent(elist);
        if (mFocusedObject == parent) {
          // Drop on focused label block: show precise insert position
          final LabelBlockProxyShape shape =
            (LabelBlockProxyShape) getShapeProducer().getShape(parent);
          final Rectangle2D textBounds = shape.getTextBounds();
          mY = textBounds.getY();
          if (!findDropPosition(elist, point.getY())) {
            mX = textBounds.getX();
            mDropTarget = elist;
            mDropPosition = -1;
          }
          final double maxX = textBounds.getMaxX();
          mLine = new Line2D.Double(mX, mY, maxX, mY);
        } else {
          // Drop on edge or node: drop at end of list and do not show line
          mDropTarget = elist;
          mDropPosition = -1;
        }
        try {
          if (getInsertInfo(support) == null) {
            mExternalDragStatus = DragOverStatus.CANTDROP;
          } else {
            mExternalDragStatus = DragOverStatus.CANDROP;
          }
        } catch (IOException | UnsupportedFlavorException exception) {
          throw new WatersRuntimeException(exception);
        }
      } else {
        mExternalDragStatus = DragOverStatus.CANTDROP;
      }

      if (!ProxyTools.equals(mLine, oldLine) ||
          !ProxyTools.equals(mNestedBlockBox, oldBox)) {
        repaint();
      }
      return mExternalDragStatus == DragOverStatus.CANDROP;
    }

    boolean importData(final TransferSupport support)
    {
      final Point mousePosition = support.getDropLocation().getDropPoint();
      final Point point = applyInverseTransform(mousePosition);
      commitDrag(point);
      boolean finished = false;
      try {
        if (mExternalDragStatus == DragOverStatus.CANDROP) {
          mDropAction = support.getDropAction();
          final List<InsertInfo> inserts;
          switch (mDropAction) {
          case DnDConstants.ACTION_COPY:
          case DnDConstants.ACTION_COPY_OR_MOVE:
          case DnDConstants.ACTION_MOVE:
            inserts = getInsertInfo(support);
            break;
          default:
            inserts = null;
            break;
          }
          if (inserts != null) {
            final Command ins =
              new InsertCommand(inserts, GraphEditorPanel.this, null);
            if (mDropAction == DnDConstants.ACTION_MOVE) {
              final List<InsertInfo> deletes = mSelection.getDeletionVictims();
              final Command del =
                new DeleteCommand(deletes, GraphEditorPanel.this, true);
              final CompoundCommand compound =
                new CompoundCommand("Label Movement");
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
      } catch (final UnsupportedFlavorException | IOException exception) {
        throw new IllegalArgumentException(exception);
      }
      mInternalDragAction = null;
      fireSelectionChanged();
      mController.updateHighlighting(point);
      return finished;
    }

    private List<InsertInfo> getInsertInfo(final TransferSupport support)
      throws IOException, UnsupportedFlavorException
    {
      if (isTrackedFocusOwner()) {
        // Transfer within graph - use selection instead of transferable
        if (mSelection.isEmpty()) {
          return null;
        }
        final List<InsertInfo> deletes = mSelection.getDeletionVictims();
        if (mDropAction != DnDConstants.ACTION_COPY) {
          boolean nested = false;
          for (final ProxySubject item : mSelection) {
            if (SubjectTools.isAncestor(item, mDropTarget)) {
              nested = true;
              break;
            }
          }
          boolean selected = false;
          if (!nested) {
            final int size = mDropTarget.size();
            if (size > 0) {
              if (mDropPosition < 0 || mDropPosition >= size) {
                final ProxySubject last = mDropTarget.get(size - 1);
                selected = mSelection.isSelected(last);
              } else {
                final ProxySubject after = mDropTarget.get(mDropPosition);
                selected = mSelection.isSelected(after);
                if (!selected && mDropPosition > 0) {
                  final ProxySubject before = mDropTarget.get(mDropPosition - 1);
                  selected = mSelection.isSelected(before);
                }
              }
            }
          }
          if (nested || (selected && isContiguous(deletes))) {
            if (mDropAction == DnDConstants.ACTION_MOVE) {
              return null;
            } else {
              mDropAction = DnDConstants.ACTION_COPY;
            }
          }
        }
        if (mDropAction == DnDConstants.ACTION_COPY_OR_MOVE) {
          mDropAction = DnDConstants.ACTION_MOVE;
        }
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
        final Iterator<InsertInfo> iter = deletes.iterator();
        while (iter.hasNext()) {
          final InsertInfo info = iter.next();
          final ProxySubject item = (ProxySubject) info.getProxy();
          if (mDropAction == DnDConstants.ACTION_COPY) {
            if (eq.contains(mDropTarget, item)) {
              iter.remove();
            }
          } else {
            final ListInsertPosition pos =
              (ListInsertPosition) info.getInsertPosition();
            if (pos.getList() != mDropTarget && eq.contains(mDropTarget, item)) {
              iter.remove();
            }
          }
        }
        if (deletes.isEmpty()) {
          return null;
        }
        final int adjustedDropPosition =
          adjustInsertPosition(mDropPosition, mDropTarget, deletes, true);
        return createInserts(deletes, mDropTarget, adjustedDropPosition,
                             mDropAction == DnDConstants.ACTION_COPY);
      } else {
        // External transfer --- use transferable, copy only
        final Transferable transferable = support.getTransferable();
        final List<? extends Proxy> data =
          mIdentifierPasteVisitor.getTransferData(transferable);
        final List<InsertInfo> inserts = new ArrayList<>(data.size());
        addInsertInfo(inserts, data, mDropTarget, mDropPosition);
        if (inserts.isEmpty()) {
          return null;
        }
        mDropAction = DnDConstants.ACTION_COPY;
        return inserts;
      }
    }

    //#######################################################################
    //# Highlighting
    @Override
    int getHighlightPriority(final Proxy item)
    {
      if (item instanceof EdgeProxy ||
          item instanceof LabelBlockProxy) {
        return 2;
      } else if (item instanceof SimpleNodeProxy) {
        return 1;
      } else {
        return -1;
      }
    }

    @Override
    DragOverStatus getExternalDragStatus()
    {
      return mExternalDragStatus;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean findDropPosition(final ListSubject<AbstractSubject> list,
                                     final double y)
    {
      for (int i = 0; i < list.size(); i++) {
        final ProxySubject item = list.get(i);
        final ProxyShape shape = getShapeProducer().getShape(item);
        final Rectangle2D bounds = shape.getBounds2D();
        if (y < bounds.getMaxY()) {
          if (item instanceof NestedBlockSubject) {
            final double margin = 0.25 * bounds.getHeight();
            if (y <= bounds.getMinY() + margin) {
              mX = bounds.getMinX();
              mDropTarget = list;
              mDropPosition = i;
            } else if (y >= bounds.getMaxY() - margin) {
              final NestedBlockSubject nested = (NestedBlockSubject) item;
              mX = bounds.getMinX() + LabelBlockProxyShape.INDENTATION;
              mY = bounds.getMaxY();
              mDropTarget = nested.getBodyModifiable();
              mDropPosition = 0;
            } else {
              final NestedBlockSubject nested = (NestedBlockSubject) item;
              mX = bounds.getMinX() + LabelBlockProxyShape.INDENTATION;
              mY = getBottomY(nested);
              mDropTarget = nested.getBodyModifiable();
              mDropPosition = -1;
              mNestedBlockBox = bounds;
            }
          } else {
            mX = bounds.getMinX();
            mDropTarget = list;
            if (y < bounds.getCenterY()) {
              mDropPosition = i;
            } else {
              mDropPosition = i + 1;
              mY = bounds.getMaxY();
            }
          }
          return true;
        } else {
          mY = bounds.getMaxY();
          if (item instanceof NestedBlockSubject) {
            final NestedBlockSubject nested = (NestedBlockSubject) item;
            if (findDropPosition(nested.getBodyModifiable(), y)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    private double getBottomY(final NestedBlockProxy nested)
    {
      final List<Proxy> body = nested.getBody();
      final ProxyShape shape;
      if (body.isEmpty()) {
        shape = getShapeProducer().getShape(nested);
      } else {
        final int end = body.size() - 1;
        final Proxy last = body.get(end);
        if (last instanceof NestedBlockProxy) {
          final NestedBlockProxy lastNested = (NestedBlockProxy) last;
          return getBottomY(lastNested);
        }
        shape = getShapeProducer().getShape(last);
      }
      final Rectangle2D bounds = shape.getShape().getBounds();
      return bounds.getMaxY();
    }

   //#######################################################################
    //# Rendering
    @Override
    List<MiscShape> getDrawnObjects()
    {
      if (mLine == null && mNestedBlockBox == null) {
        return Collections.emptyList();
      } else {
        final List<MiscShape> list = new ArrayList<>(2);
        if (mLine != null) {
          final MiscShape shape =
            new GeneralShape(mLine, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
          list.add(shape);
        }
        if (mNestedBlockBox != null) {
          final MiscShape shape =
            new GeneralShape(mNestedBlockBox, EditorColor.GRAPH_SELECTED_FOCUSSED, null);
          list.add(shape);
        }
        return list;
      }
    }

    //#######################################################################
    //# Data Members
    private double mX;
    private double mY;
    private Line2D mLine;
    private Rectangle2D mNestedBlockBox;
    private ListSubject<AbstractSubject> mDropTarget;
    private int mDropPosition;
    private int mDropAction;
    private DragOverStatus mExternalDragStatus;
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
    @Override
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
      super(Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue() ?
            findGrid(start) : start);
      clearSelection();
    }

    //#######################################################################
    //# Simple Access
    @Override
    boolean shouldSnapToGrid()
    {
      return Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue();
    }

    //#######################################################################
    //# Dragging
    @Override
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
    @Override
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
    @Override
    boolean shouldSnapToGrid()
    {
      return Config.GUI_EDITOR_NODES_SNAP_TO_GRID.getValue();
    }

    //#######################################################################
    //# Simple Access
    @Override
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

    @Override
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
    @Override
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
    @Override
    boolean continueDrag(final Point point)
    {
      if (super.continueDrag(point)) {
        Point2D current = getDragCurrent();
        if (!mCanCreateSelfloop) {
          final double dist = mAnchor.distanceSq(current);
          final int radius = Config.GUI_EDITOR_NODE_RADIUS.getValue() + 2;
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

    @Override
    void commitSecondaryGraph()
    {
      final NodeSubject node = (NodeSubject) mFocusedObject;
      if (node == null) {
        if (mSource != null && mController.canBeSelected(mSource)) {
          replaceSelection(mSource);
        }
      } else if (mCopiedEdge != null) {
        if (mAnchor == null) {
          // When creating new edge ...
          final ProxySubject oldNode =
            (mIsSource ? mOrigEdge.getSource() : mOrigEdge.getTarget());
          final ProxySubject copiedNode =
            (mIsSource ? mCopiedEdge.getSource() : mCopiedEdge.getTarget());
          final ProxySubject newNode = getOriginal(copiedNode);
          if (oldNode == newNode) {
            // Don't create a bogus command
            if (oldNode instanceof SimpleNodeSubject) {
              return;
            } else {
              final PointGeometrySubject oldGeo =
                (mIsSource ?
                 mOrigEdge.getStartPoint() : mOrigEdge.getEndPoint());
              final PointGeometrySubject newGeo =
                (mIsSource ?
                 mCopiedEdge.getStartPoint() : mCopiedEdge.getEndPoint());
              final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
              if (eq.equals(oldGeo, newGeo)) {
                return;
              }
            }
          }
        }
        if (!GeometryTools.isSelfloop(mCopiedEdge) &&
            mCopiedEdge.getSource() instanceof SimpleNodeSubject &&
            mCopiedEdge.getTarget() instanceof SimpleNodeSubject) {
          // Make overlapping straight edges automatically spread apart ...
          final List<EdgeSubject> edges =
            getSecondaryGraph().getEdgesModifiable();
          for (final EdgeSubject edge : edges) {
            if ((edge.getSource() == mCopiedEdge.getTarget() &&
                 edge.getTarget() == mCopiedEdge.getSource() ||
                 edge.getSource() == mCopiedEdge.getSource() &&
                 edge.getTarget() == mCopiedEdge.getTarget() &&
                 edge != mCopiedEdge) &&
                edge.getGeometry() == null) {
              final Point2D mid1 = getNewMidPointOfEdge(edge, false);
              GeometryTools.setSpecifiedMidPoint(edge, mid1,
                                                 SplineKind.INTERPOLATING);
              final boolean sameDirection =
                (edge.getSource() == mCopiedEdge.getSource());
              final Point2D mid2 =
                getNewMidPointOfEdge(mCopiedEdge, sameDirection);
              GeometryTools.setSpecifiedMidPoint(mCopiedEdge, mid2,
                                                 SplineKind.INTERPOLATING);
              break;
            }
          }
        }
        super.commitSecondaryGraph();
      }
    }

    /**
     * Used to change the midpoint to 1 grid position perpendicularly.
     **/
    private Point2D getNewMidPointOfEdge(final EdgeSubject edge,
                                         final boolean sameDirection)
    {
      final Point2D p1 = GeometryTools.getStartPoint(edge);
      final Point2D p2 = GeometryTools.getEndPoint(edge);
      final double dx = Math.abs(p2.getX() - p1.getX());
      final double dy = Math.abs(p2.getY() - p1.getY());
      final double perp = Math.pow(dx*dx + dy*dy, 0.5);
      final int gridSize = ConfigBridge.getGridSize();
      double newX = (dy / perp) * gridSize;
      double newY = (dx / perp) * gridSize;

      //make sure the arrows follow clockwise unless they go in same direction
      if(p1.getX() == p2.getX() && p1.getY() > p2.getY()){
        newX = -newX;
      } else if (p1.getY() == p2.getY() && p1.getX() < p2.getX()){
        newY = -newY;
      } else if (p1.getX() < p2.getX()){
        if(p1.getY() > p2.getY()){
          newX = -newX;
        }
        newY = -newY;
      } else if (p1.getX() > p2.getX() && p1.getY() > p2.getY()){
        newX = -newX;
      }
      if (sameDirection) {
        if (p1.getY() != p2.getY()){
          newX = -newX;
        }
        if (p1.getX() != p2.getX()){
          newY = -newY;
        }
      }

      final Point2D mid = GeometryTools.getMidPoint(p1, p2);
      return new Point2D.Double(mid.getX() + newX, mid.getY() + newY);
    }

    //#######################################################################
    //# Highlighting
    @Override
    int getHighlightPriority(final Proxy item)
    {
      final int prio;
      if (item instanceof SimpleNodeProxy) {
        prio = 2;
      } else if (item instanceof GroupNodeProxy &&
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
      final Collection<ProxySubject> moved =
        new ArrayList<>(mSelection.getNumberOfSelectedItems());
      for (final ProxySubject item : mSelection) {
        moved.add(item);
        final Class<? extends Proxy> iface = item.getProxyInterface();
        mMovedTypes.add(iface);
      }
      if (mMovedTypes.contains(SimpleNodeProxy.class) ||
          mMovedTypes.contains(GroupNodeProxy.class)) {
        final Collection<EdgeSubject> edges = getGraph().getEdgesModifiable();
        mEdgeMap = new HashMap<>(edges.size());
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
      }
      mMovedObjects = Collections.unmodifiableCollection(moved);
    }

    //#######################################################################
    //# Invocation
    private void moveAll(final int dx, final int dy, final boolean edgeMove,
                         final Point dragStart)
    {
      try {
        assert(getSecondaryGraph() != null);
        mDeltaX = dx;
        mDeltaY = dy;
        mMoveAlongHalfWay = edgeMove;
        mDragStart = dragStart;
        for (final ProxySubject item : mMovedObjects) {
          item.acceptVisitor(this);
        }
        scrollToVisible(mSelection.asList());
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
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      final EdgeSubject edge0 = (EdgeSubject) edge;
      if (mEdgeMap == null) {
        getSecondaryGraph().moveEdgeHandle(edge0, mDeltaX, mDeltaY,
                                           mMoveAlongHalfWay, mDragStart);
      } else {
        final MovingEdge entry = mEdgeMap.get(edge0);
        entry.move(mDeltaX, mDeltaY);
      }
      return null;
    }

    @Override
    public Object visitGroupNodeProxy(final GroupNodeProxy group)
    {
      final GroupNodeSubject group0 = (GroupNodeSubject) group;
      getSecondaryGraph().moveGroupNode(group0, mDeltaX, mDeltaY);
      return null;
    }

    @Override
    public Object visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      if (!isParentMoved(block)) {
        final GuardActionBlockSubject block0 = (GuardActionBlockSubject) block;
        getSecondaryGraph().moveGuardActionBlock(block0, mDeltaX, mDeltaY);
      }
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      return null;
    }

    @Override
    public Object visitLabelBlockProxy(final LabelBlockProxy block)
    {
      if (!isParentMoved(block)) {
        final LabelBlockSubject block0 = (LabelBlockSubject) block;
        getSecondaryGraph().moveLabelBlock(block0, mDeltaX, mDeltaY);
      }
      return null;
    }

    @Override
    public Object visitLabelGeometryProxy(final LabelGeometryProxy label)
    {
      if (!isParentMoved(label)) {
        final LabelGeometrySubject label0 = (LabelGeometrySubject) label;
        getSecondaryGraph().moveLabelGeometry(label0, mDeltaX, mDeltaY);
      }
      return null;
    }

    @Override
    public Object visitNestedBlockProxy(final NestedBlockProxy nested)
    {
      return null;
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      final SimpleNodeSubject node0 = (SimpleNodeSubject) node;
      getSecondaryGraph().moveSimpleNode(node0, mDeltaX, mDeltaY);
      return null;
    }

    //#######################################################################
    //# Auxiliary Methods
    @SuppressWarnings("unlikely-arg-type")
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
    private boolean mMoveAlongHalfWay;
    private Point mDragStart;

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
        graph.moveEdgeHandle(mEdge, dx, dy, false, null);
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
    implements Comparator<Proxy>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    @Override
    public int compare(final Proxy item1, final Proxy item2)
    {
      final boolean sel1 = isRenderedSelected((ProxySubject) item1);
      final boolean sel2 = isRenderedSelected((ProxySubject) item2);
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
  //# Inner Class KeyHandler
  private class KeyHandler extends KeyAdapter
  {

    @Override
    public void keyReleased(final KeyEvent e){
      final int keyCode = e.getKeyCode();
      if(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL){
        if(mInternalDragAction != null){
          mInternalDragAction.modifiersChanged(keyCode, false);
        }
      }
    }

    @Override
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
      switch (mSelection.getMode()) {
      case SUBGRAPH_SINGLE:
      case SUBGRAPH_MULTIPLE:
      case NODE_LABELS:
        if (up || down || left || right) {
          e.consume();
          final int x = left ? -1 : right ? 1 : 0;
          final int y = up ? -1 : down ? 1 : 0;
          mMoveVisitor = new MoveVisitor();
          final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
          final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
          final GraphSubject g0 = (GraphSubject) cloner.getClone(getGraph());
          final UndoInterface undoInterface = mRoot.getUndoInterface();

          if(mDisplacement == null){
            mDisplacement = new Point2D.Double(x, y);
          }
          else if(mLastCommand != null && mLastCommand == undoInterface.getLastCommand()){
            mLastCommand.setUpdatesSelection(false);
            undoInterface.undoAndRemoveLastCommand();
            mDisplacement.setLocation(x+mDisplacement.getX(),
                                      y+mDisplacement.getY());
          }
          else{
            mDisplacement.setLocation(x,y);
          }
          createSecondaryGraph();
          mMoveVisitor.moveAll((int)mDisplacement.getX(),
                               (int)mDisplacement.getY(), false, null);
          while(eq.equals(g0, getSecondaryGraph())){
            mDisplacement.setLocation(x+mDisplacement.getX(),
                                      y+mDisplacement.getY());
            mMoveVisitor.moveAll((int)mDisplacement.getX(),
                                 (int)mDisplacement.getY(), false, null);
          }
          commitGraph(null, true, true, x, y);
          mMoveVisitor = null;
          clearSecondaryGraph();
          return;
        }
      case EVENT_LABELS:
        if (up ^ down) {
          final CompoundCommand move = new CompoundCommand();
          final List<InsertInfo> deletes = mSelection.getDeletionVictims();
          final List<InsertInfo> inserts = getDisplacedInserts(deletes, up);
          if (inserts != null) {
            final Command del =
              new DeleteCommand(deletes, GraphEditorPanel.this, true);
            final Command ins =
              new InsertCommand(inserts, GraphEditorPanel.this, null);
            move.addCommand(del);
            move.addCommand(ins);
            final List<Proxy> proxies = InsertInfo.getProxies(inserts);
            final String named = ProxyNamer.getCollectionClassName(proxies);
            move.setName(named + " Movement");
            move.end();
            e.consume();
            getUndoInterface().executeCommand(move);
          }
          return;
        }
      }
      if (keyCode == KeyEvent.VK_SHIFT ||
          keyCode == KeyEvent.VK_CONTROL) {
        if (mInternalDragAction != null) {
          mInternalDragAction.modifiersChanged(keyCode, true);
        }
      }
    }

    private void commitGraph(final String description,
                             final boolean selecting,
                             final boolean undoable,
                             final int x, final int y){
      final EditorGraph graph = getSecondaryGraph();
      if (graph != null) {
        final UndoInterface undoInterface = mRoot.getUndoInterface();
          if (mLastCommand != null
              && mLastCommand == undoInterface.getLastCommand()) {
            mLastCommand.setUpdatesSelection(false);
            undoInterface.undoAndRemoveLastCommand();
          }
        final Command cmd = graph.createUpdateCommand
          (GraphEditorPanel.this, description, selecting);
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

    @SuppressWarnings("unchecked")
    private List<InsertInfo> getDisplacedInserts
      (final List<InsertInfo> deletes, final boolean up)
    {
      final InsertInfo leadInfo;
      if (up) {
        leadInfo = deletes.get(0);
      } else {
        leadInfo = deletes.get(deletes.size() - 1);
      }
      final ListInsertPosition leadPos =
        (ListInsertPosition) leadInfo.getInsertPosition();
      ListSubject<? extends ProxySubject> insertList = leadPos.getList();
      int index = leadPos.getPosition();
      final ProxySubject parent = SubjectTools.getProxyParent(insertList);
      if (up) {
        if (index == 0) {
          if (parent instanceof LabelBlockSubject) {
            if (isContiguous(deletes)) {
              return null;
            }
          } else {
            insertList =
              (ListSubject<? extends ProxySubject>) parent.getParent();
            index = insertList.indexOf(parent);
          }
        } else if (insertList.get(index - 1) instanceof NestedBlockSubject) {
          final NestedBlockSubject sibling =
            (NestedBlockSubject) insertList.get(index - 1);
          insertList = sibling.getBodyModifiable();
          index = -1;
        } else {
          index--;
        }
      } else {  // down
        if (index == insertList.size() - 1) {
          if (parent instanceof LabelBlockSubject) {
            if (isContiguous(deletes)) {
              return null;
            }
            index = -1;
          } else {
            insertList =
              (ListSubject<? extends ProxySubject>) parent.getParent();
            index = insertList.indexOf(parent) + 1;
          }
        } else if (insertList.get(index + 1) instanceof NestedBlockSubject) {
          final NestedBlockSubject sibling =
            (NestedBlockSubject) insertList.get(index + 1);
          insertList = sibling.getBodyModifiable();
          index = 0;
        } else {
          index += 2;
        }
        index = adjustInsertPosition(index, insertList, deletes, false);
      }
      return createInserts(deletes, insertList, index, false);
    }

    //#########################################################################
    //# Data Members
    private MoveVisitor mMoveVisitor;
  }


  //#########################################################################
  //# Inner Class StateNameInputCell
  /**
   * A text field to rename states. This text field appears when the user
   * double clicks a state name. It allows the user to edit the old name and
   * change the state name to the new value by pressing &lt;ENTER&gt;.
   */
  private class StateNameInputCell
    extends SimpleExpressionInputCell
    implements FocusListener
  {
    //#######################################################################
    //# Constructor
    private StateNameInputCell(final SimpleNodeSubject node,
                               final SimpleIdentifierSubject ident)
    {
      super(ident, new StateNameInputParser(ident));
      mNode = node;
      final float zoom = Math.max((float) mZoomFactor, 1.0f);
      if (zoom > 1.0f) {
        final Font font = getFont();
        final float newSize = zoom * font.getSize2D();
        final Font newFont = font.deriveFont(newSize);
        setFont(newFont);
      }
      final LabelGeometrySubject geo = node.getLabelGeometry();
      final Point2D pgeo = node.getPointGeometry().getPoint();
      final Point2D lgeo = geo.getOffset();
      pgeo.setLocation(pgeo.getX() + lgeo.getX(), pgeo.getY() + lgeo.getY());
      final AffineTransform transform = getTransform();
      final Point2D pos2d = transform.transform(pgeo, null);
      final Rectangle view = GraphEditorPanel.this.getVisibleRect();
      int x = (int) Math.round(pos2d.getX());
      final int y = (int) Math.round(pos2d.getY());
      int width = Math.round(zoom * STATE_INPUT_WIDTH);
      if (width > view.width) {
        width = view.width;
      }
      final int xmax = view.x + view.width;
      if (x + width > xmax) {
        x = xmax - width;
      }
      final Point pos = new Point(x, y);
      final int height = getPreferredSize().height;
      final Dimension size = new Dimension(width, height);
      setLocation(pos);
      setSize(size);
      setErrorDisplay(new LoggerErrorDisplay());

      setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
      addFocusListener(this);
      final Action enter = new AbstractAction("<enter>") {
        private static final long serialVersionUID = 1L;

        @Override
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
          @Override
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
    @Override
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
    @Override
    public void focusGained(final FocusEvent event)
    {
    }

    @Override
    public void focusLost(final FocusEvent event)
    {
      cancel();
    }

    //#######################################################################
    //# Data Members
    private SimpleNodeSubject mNode;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -1949718690010680463L;
  }


  //#########################################################################
  //# Inner Class StateNameInputParser
  private class StateNameInputParser
    extends SimpleIdentifierInputHandler
  {

    //#######################################################################
    //# Constructor
    private StateNameInputParser(final SimpleIdentifierProxy oldident)
    {
      super(oldident,
            mRoot.getModuleWindowInterface().getExpressionParser(),
            false);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    @Override
    public SimpleIdentifierProxy parse(final String text)
      throws java.text.ParseException
    {
      final SimpleIdentifierProxy ident = super.parse(text);
      final String oldname = getOldName();
      if (!text.equals(oldname)) {
        if (getGraph().getNodesModifiable().containsName(text)) {
          throw new java.text.ParseException
            ("State name '" + text + "' is already taken!", 0);
        }
      }
      return ident;
    }
  }


  //#########################################################################
  //# Inner Class SelectableVisitor
  private class SelectableAncestorVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
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
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public EdgeProxy visitEdgeProxy(final EdgeProxy edge)
    {
      return edge;
    }

    @Override
    public Object visitGeometryProxy(final GeometryProxy proxy)
      throws VisitorException
    {
      final GeometrySubject geo = (GeometrySubject)proxy;
      final Proxy parent = (Proxy) geo.getParent();
      return parent.acceptVisitor(this);
    }

    @Override
    public GuardActionBlockProxy visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      return block;
    }

    @Override
    public Proxy visitIdentifierProxy(final IdentifierProxy ident)
    {
      return visitEventListMember(ident);
    }

    @Override
    public Proxy visitLabelBlockProxy(final LabelBlockProxy block)
    {
      if (block.getEventIdentifierList().isEmpty()) {
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

    @Override
    public LabelGeometryProxy visitLabelGeometryProxy
      (final LabelGeometryProxy geo)
    {
      return geo;
    }

    @Override
    public Proxy visitNestedBlockProxy(final NestedBlockProxy nested)
    {
      return visitEventListMember(nested);
    }

    @Override
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

    private ListSubject<AbstractSubject> getIdentifierPasteTarget
      (final Proxy focussed, final Transferable transferable)
    {
      try {
        final List<? extends Proxy> data = getTransferData(transferable);
        return getIdentifierPasteTarget(focussed, data);
      } catch (final IOException | UnsupportedFlavorException exception) {
        return null;
      }
    }

    @SuppressWarnings("unchecked")
    private ListSubject<AbstractSubject> getIdentifierPasteTarget
      (final Proxy focussed, final List<? extends Proxy> data)
    {
      if (focussed == null) {
        return null;
      } else {
        try {
          mTransferData = data;
          final ListSubject<AbstractSubject> elist =
            (ListSubject<AbstractSubject>) focussed.acceptVisitor(this);
          if (elist == null) {
            return null;
          } else {
            final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
            return eq.containsAll(elist, data) ? null : elist;
          }
        } catch (final VisitorException exception) {
          throw exception.getRuntimeException();
        }
      }
    }

    private void addInsertInfo(final Proxy focussed,
                               final Transferable transferable,
                               final List<InsertInfo> inserts)
      throws IOException, UnsupportedFlavorException
    {
      final List<? extends Proxy> data = getTransferData(transferable);
      final ListSubject<AbstractSubject> elist =
        getIdentifierPasteTarget(focussed, data);
      if (elist != null) {
        int pos = elist.indexOf(focussed);
        if (pos >= 0) {
          pos++;
        }
        GraphEditorPanel.this.addInsertInfo(inserts, data, elist, pos);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public ListSubject<AbstractSubject> visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public ListSubject<AbstractSubject> visitEdgeProxy(final EdgeProxy edge)
    {
      final LabelBlockProxy block = edge.getLabelBlock();
      return visitLabelBlockProxy(block);
    }

    @Override
    public ListSubject<AbstractSubject> visitGuardActionBlockProxy
      (final GuardActionBlockProxy block)
    {
      final GuardActionBlockSubject subject = (GuardActionBlockSubject) block;
      final EdgeSubject edge = (EdgeSubject) subject.getParent();
      return visitEdgeProxy(edge);
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierSubject subject = (IdentifierSubject) ident;
      final ProxySubject parent = SubjectTools.getProxyParent(subject);
      return parent.acceptVisitor(this);
    }

    @Override
    public ListSubject<AbstractSubject> visitLabelBlockProxy
      (final LabelBlockProxy block)
    {
      final LabelBlockSubject subject = (LabelBlockSubject) block;
      if (subject.getParent() == getGraph()) {
        // Any event can be dropped on the blocked events list.
        return subject.getEventIdentifierListModifiable();
      } else {
        final ModuleContext context = getModuleContext();
        if (context.canDropOnEdge(mTransferData)) {
          return subject.getEventIdentifierListModifiable();
        }
        return null;
      }
    }

    @Override
    public ListSubject<AbstractSubject> visitLabelGeometryProxy
      (final LabelGeometryProxy geo)
    {
      final LabelGeometrySubject subject = (LabelGeometrySubject) geo;
      final SimpleNodeSubject node = (SimpleNodeSubject) subject.getParent();
      return visitSimpleNodeProxy(node);
    }

    @Override
    public ListSubject<AbstractSubject> visitNestedBlockProxy
      (final NestedBlockProxy block)
    {
      final ModuleContext context = getModuleContext();
      if (context.canDropOnEdge(mTransferData)) {
        final NestedBlockSubject subject = (NestedBlockSubject) block;
        return subject.getBodyModifiable();
      } else {
        return null;
      }
    }

    @Override
    public ListSubject<AbstractSubject> visitPlainEventListProxy
      (final PlainEventListProxy elist)
    {
      final ModuleContext context = getModuleContext();
      if (context.canDropOnNode(mTransferData)) {
        final PlainEventListSubject subject = (PlainEventListSubject) elist;
        return subject.getEventIdentifierListModifiable();
      } else {
        return null;
      }
    }

    @Override
    public ListSubject<AbstractSubject> visitSimpleNodeProxy
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

    //#######################################################################
    //# Data Members
    private List<? extends Proxy> mTransferData;
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
      final Collection<ProxySubject> toBeDragged = mSelection.asList();
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
      if (!(mInternalDragAction instanceof InternalDragActionDND)) {
        final Point mousePosition = support.getDropLocation().getDropPoint();
        final Point point = applyInverseTransform(mousePosition);
        mInternalDragAction = new InternalDragActionDND(point);
      }
      final InternalDragActionDND dragAction =
        (InternalDragActionDND) mInternalDragAction;
      if (support.getDropAction() == MOVE && !isTrackedFocusOwner()) {
        support.setDropAction(COPY);
      }

      return dragAction.canImport(support);
    }

    @Override
    public boolean importData(final TransferSupport support)
    {
      if (!(mInternalDragAction instanceof InternalDragActionDND)) {
        final Point mousePosition = support.getDropLocation().getDropPoint();
        final Point point = applyInverseTransform(mousePosition);
        mInternalDragAction = new InternalDragActionDND(point);
      }
      final InternalDragActionDND dragAction =
        (InternalDragActionDND) mInternalDragAction;
      return dragAction.importData(support);
    }

    private static final long serialVersionUID = 3039120453305316710L;
  }


  //#########################################################################
  //# Data Members
  private final ComponentEditorPanel mRoot;
  private final ModuleContainer mModuleContainer;
  private final IDEToolBar mToolbar;

  /**
   * The current zoom factor for scaling the graph. This is the value set
   * from the drop box in the toolbar ({@link ZoomSelector}), which may
   * need further correction.
   */
  private double mZoomFactor;
  /**
   * The adjusted zoom factor for scaling the graph. This is the value of
   * {@link #mZoomFactor} multiplied by the scale factor from the configured
   * icon set {@link Config#GUI_EDITOR_ICONSET}, which is the value used for
   * scaling the graph.
   */
  private double mAdjustedZoomFactor;

  /**
   * Set of items not to be drawn, because they are being dragged and
   * displayed through alternative means.
   */
  private final Set<ProxySubject> mDontDraw = new THashSet<ProxySubject>();
  /**
   * Set of items to be highlighted as overlapping.
   */
  private final Set<ProxySubject> mOverlap = new THashSet<ProxySubject>();
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
  /**
   * The current bounding box of the displayed area. The bounds indicate the
   * area to be covered by the canvas in graph coordinates. The bounds cover
   * all graphical objects plus margins. The bounds may be larger than the
   * panel/viewport because scrollbars are used.
   */
  private Rectangle mCurrentBounds = null;
  /**
   * A flag indicating that the bounds ({@link #mCurrentBounds}) may have
   * changed and need to be recalculated.
   */
  private boolean mBoundsMayHaveChanged = true;

  private ToolController mController;
  private ToolController mSelectController;
  private ToolController mNodeController;
  private ToolController mGroupNodeController;
  private ToolController mEdgeController;
  private ToolController mEmbedderController;

  private final PopupFactory mPopupFactory;

  private final EventDeclListModelObserver mEventDeclListModelObserver =
    new EventDeclListModelObserver();
  private final SelectableAncestorVisitor mSelectableAncestorVisitor =
    new SelectableAncestorVisitor();
  private GraphSelection mSelection = new GraphSelection();

  private final IdentifierPasteVisitor mIdentifierPasteVisitor =
    new IdentifierPasteVisitor();
  private final HighlightComparator mComparator = new HighlightComparator();
  private List<Observer> mObservers;

  private Command mLastCommand = null;
  private Point2D mDisplacement = null;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5441237464454813450L;

  private static final int LOWER_MARGIN = 32;
  private static final int UPPER_MARGIN = 128;
  private static final int STATE_INPUT_WIDTH = 128;

}
