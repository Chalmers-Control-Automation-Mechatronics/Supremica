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

package net.sourceforge.waters.gui.dialog;

import gnu.trove.set.hash.THashSet;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.ComponentEditorPanel;


/**
 * @author Carly Hona, Robi Malik
 */

public class EventDeclDeleteVisitor
  extends DefaultModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  public EventDeclDeleteVisitor(final ModuleWindowInterface root)
  {
    mRoot = root;
    mNames = null;
    mDeletionVictims = null;
    mComponent = null;
    mVictim = null;
  }


  //#########################################################################
  //# Invocation
  public List<InsertInfo> getDeletionVictims
    (final List<? extends EventDeclProxy> decls, final String action)
  {
    try {
      mAction = action;
      final int size = decls.size();
      mNames = new THashSet<String>(size);
      for (final EventDeclProxy decl : decls) {
        final String name = decl.getName();
        mNames.add(name);
      }
      mDeletionVictims = new LinkedList<InsertInfo>();
      mHasShownDialog = mCancelled = false;
      final ModuleProxy module = mRoot.getModuleSubject();
      try {
        module.acceptVisitor(this);
      } catch (final VisitorException exception) {
        // User selected No or Cancel in dialog ...
      }
      if (mCancelled) {
        return null;
      } else {
        for (final EventDeclProxy decl : decls) {
          final InsertInfo info = new InsertInfo(decl);
          mDeletionVictims.add(0, info);
        }
        return mDeletionVictims;
      }
    } finally {
      mNames = null;
      mDeletionVictims = null;
      mComponent = null;
      mVictim = null;
    }
  }

  public void insertItems(final List<InsertInfo> inserts)
  {
    final ComponentEditorPanel panel = mRoot.getActiveComponentEditorPanel();
    final GraphEditorPanel surface;
    final GraphProxy visiblegraph;
    if (panel == null) {
      surface = null;
      visiblegraph = null;
    } else {
      surface = panel.getGraphEditorPanel();
      visiblegraph = surface.getGraph();
    }
    final ModuleSubject module = mRoot.getModuleSubject();
    final List<EventDeclSubject> events = module.getEventDeclListModifiable();
    final List<Proxy> selection = new LinkedList<Proxy>();
    ProxySubject last = null;
    for (final InsertInfo insert : inserts) {
      final ProxySubject proxy = (ProxySubject) insert.getProxy();
      if (proxy instanceof EventDeclProxy) {
        final EventDeclSubject decl = (EventDeclSubject) proxy;
        events.add(decl);
      } else {
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<?> untyped = inspos.getList();
        @SuppressWarnings("unchecked")
        final List<Proxy> list = (List<Proxy>) untyped;
        final int index = inspos.getPosition();
        list.add(index, proxy);
        if (proxy instanceof IdentifierSubject) {
          final IdentifierSubject subject = (IdentifierSubject) proxy;
          final GraphSubject graph =
            SubjectTools.getAncestor(subject, GraphSubject.class);
          if (graph == visiblegraph) {
            final ProxySubject selectable = findSelectable(proxy);
            if (selectable != last) {
              selection.add(selectable);
              last = selectable;
            }
          }
        }
      }
    }
    if (!selection.isEmpty()) {
      surface.replaceSelection(selection);
      surface.scrollToVisible(selection);
    }
  }

  public void deleteItems(final List<InsertInfo> deletes)
  {
    final ModuleSubject module = mRoot.getModuleSubject();
    final List<EventDeclSubject> events = module.getEventDeclListModifiable();
    final int size = deletes.size();
    final ListIterator<InsertInfo> iter = deletes.listIterator(size);
    while (iter.hasPrevious()) {
      final InsertInfo insert = iter.previous();
      final ProxySubject proxy = (ProxySubject) insert.getProxy();
      if (proxy instanceof EventDeclProxy) {
        events.remove(proxy);
      } else {
        if (proxy instanceof IdentifierSubject) {
          final IdentifierSubject subject = (IdentifierSubject) proxy;
          final SimpleComponentSubject comp =
            SubjectTools.getAncestor(subject, SimpleComponentSubject.class);
          final ComponentEditorPanel iface =
            mRoot.getComponentEditorPanel(comp);
          if (iface != null) {
            final SelectionOwner panel = iface.getGraphEditorPanel();
            final List<ProxySubject> list = Collections.singletonList(proxy);
            panel.removeFromSelection(list);
          }
        }
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<?> untyped = inspos.getList();
        @SuppressWarnings("unchecked")
        final List<Proxy> list = (List<Proxy>) untyped;
        final int index = inspos.getPosition();
        list.remove(index);
      }
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
  public Object visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    final EventListExpressionProxy block = edge.getLabelBlock();
    return block.acceptVisitor(this);
  }

  @Override
  public Object visitEventAliasProxy(final EventAliasProxy alias)
    throws VisitorException
  {
    mComponent = alias;
    mVictim = alias;
    final ExpressionProxy expr = alias.getExpression();
    return expr.acceptVisitor(this);
  }

  @Override
  public Object visitEventListExpressionProxy
    (final EventListExpressionProxy elist)
    throws VisitorException
  {
    mVictim = null;
    final Collection<? extends Proxy> list = elist.getEventIdentifierList();
    return visitCollection(list);
  }

  @Override
  public Object visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    final EventListExpressionProxy blocked = graph.getBlockedEvents();
    if (blocked != null) {
      blocked.acceptVisitor(this);
    }
    final Collection<NodeProxy> nodes = graph.getNodes();
    visitCollection(nodes);
    final Collection<EdgeProxy> edges = graph.getEdges();
    visitCollection(edges);
    return null;
  }

  @Override
  public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    handleNamedIdentifier(ident, name);
    return null;
  }

  @Override
  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final Collection<? extends Proxy> components = module.getComponentList();
    visitCollection(components);
    final Collection<? extends Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);
    return null;
  }

  @Override
  public Object visitNestedBlockProxy(final NestedBlockProxy block)
    throws VisitorException
  {
    final Collection<? extends Proxy> list = block.getBody();
    return visitCollection(list);
  }

  @Override
  public Object visitNodeProxy(final NodeProxy node)
    throws VisitorException
  {
    final EventListExpressionProxy props = node.getPropositions();
    return props.acceptVisitor(this);
  }

  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    mComponent = comp;
    final GraphProxy graph = comp.getGraph();
    return graph.acceptVisitor(this);
  }

  @Override
  public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    handleNamedIdentifier(ident, name);
    return null;
  }

  @Override
  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    mComponent = var;
    final Collection<? extends Proxy> list = var.getVariableMarkings();
    return visitCollection(list);
  }

  @Override
  public Object visitVariableMarkingProxy(final VariableMarkingProxy marking)
    throws VisitorException
  {
    mVictim = marking;
    final IdentifierProxy prop = marking.getProposition();
    prop.acceptVisitor(this);
    mVictim = null;
    return null;
  }


  //#########################################################################
  //# Showing the Dialog
  @SuppressWarnings("unchecked")
  private void handleNamedIdentifier(final IdentifierProxy ident,
                                     final String name)
    throws VisitorException
  {
    if (mNames.contains(name)) {
      maybeShowDialog(name);
      final Proxy proxy = mVictim == null ? ident : mVictim;
      final ProxySubject subject = (ProxySubject) proxy;
      final ListSubject<? extends Proxy> list =
        (ListSubject<? extends Proxy>) subject.getParent();
      final int index = list.indexOf(proxy);
      final ListInsertPosition inspos = new ListInsertPosition(list, index);
      final InsertInfo info = new InsertInfo(proxy, inspos);
      mDeletionVictims.add(info);
    }
  }

  private void maybeShowDialog(final String name)
    throws VisitorException
  {
    if (!mHasShownDialog) {
      mHasShownDialog = true;
      showDialog(name);
    }
  }

  private void showDialog(final String name)
    throws VisitorException
  {
    final StringBuilder buffer = new StringBuilder(256);
    buffer.append("The event '");
    buffer.append(name);
    buffer.append("' is still used in ");
    final String type = ProxyNamer.getItemClassName(mComponent);
    buffer.append(type);
    buffer.append(" '");
    final IdentifierProxy ident = mComponent.getIdentifier();
    buffer.append(ident);
    buffer.append("'.\n");
    buffer.append("Would you like to ");
    buffer.append(mAction);
    //buffer.append("delete");
    buffer.append(" all occurrences of ");
    buffer.append(mNames.size() == 1 ? "this event?" : "these events?");
    final List<? extends Proxy> oldSelection = showAutomaton();
    final Frame frame = mRoot.getRootWindow();
    final int choice =
      JOptionPane.showConfirmDialog(frame, buffer, "Event in Use!",
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
    deselectEvents(oldSelection);
    switch (choice) {
    case JOptionPane.YES_OPTION:
      return;
    case JOptionPane.NO_OPTION:
      throw new VisitorException();
    default:
      mCancelled = true;
      throw new VisitorException();
    }
  }

  private void deselectEvents(final List<? extends Proxy> oldSelection)
  {
    if (oldSelection != null) {
      try {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) mComponent;
        final ComponentEditorPanel iface = mRoot.showEditor(comp);
        final SelectionOwner panel = iface.getGraphEditorPanel();
        panel.replaceSelection(oldSelection);
      } catch (final GeometryAbsentException exception) {
        // cannot happen
      }
    }
  }


  private List<? extends Proxy> showAutomaton()
  {
    if (mComponent instanceof SimpleComponentSubject) {
      try {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) mComponent;
        final ComponentEditorPanel iface = mRoot.showEditor(comp);
        // Select them ...
        comp.acceptVisitor(this);
        final List<Proxy> selection = findSelection(mDeletionVictims);
        final SelectionOwner panel = iface.getGraphEditorPanel();
        final List<? extends Proxy> oldSelection = panel.getCurrentSelection();
        panel.replaceSelection(selection);
        panel.scrollToVisible(selection);
        mDeletionVictims.clear();
        // Restore keyboard focus!
        mRoot.showEvents();
        return oldSelection;
      } catch (final GeometryAbsentException exception) {
        // No geometry? --- Nice try anyway ...
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }
    return null;
  }

  private List<Proxy> findSelection(final List<InsertInfo> inserts)
  {
    final int size = inserts.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    ProxySubject last = null;
    for (final InsertInfo insert : inserts) {
      final ProxySubject proxy = (ProxySubject) insert.getProxy();
      final ProxySubject selectable = findSelectable(proxy);
      if (selectable != last) {
        result.add(selectable);
        last = selectable;
      }
    }
    return result;
  }

  private ProxySubject findSelectable(final ProxySubject subject)
  {
    final ProxySubject parent = findNodeOrEdge(subject);
    if (parent instanceof EdgeProxy) {
      return subject;
    } else if (parent instanceof NodeProxy) {
      return parent;
    } else {
      return null;
    }
  }

  private ProxySubject findNodeOrEdge(Subject subject)
  {
    while (subject != null &&
           !(subject instanceof NodeProxy) &&
           !(subject instanceof EdgeProxy)) {
      subject = subject.getParent();
    }
    return (ProxySubject) subject;
  }


  //#########################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;

  private Set<String> mNames;
  private List<InsertInfo> mDeletionVictims;
  private IdentifiedProxy mComponent;
  private Proxy mVictim;
  private boolean mHasShownDialog;
  private boolean mCancelled;
  private String mAction;

}
