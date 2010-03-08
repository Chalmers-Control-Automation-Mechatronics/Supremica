//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventDeclDeleteVisitor
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ListInsertPosition;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;


class EventDeclDeleteVisitor
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Constructor
  EventDeclDeleteVisitor(final ModuleWindowInterface root)
  {
    mRoot = root;
    mNames = null;
    mDeletionVictims = null;
    mComponent = null;
    mVictim = null;
  }


  //#########################################################################
  //# Invocation
  List<InsertInfo> getDeletionVictims
    (final List<? extends EventDeclProxy> decls)
  {
    try {
      final int size = decls.size();
      mNames = new HashSet<String>(size);
      for (final EventDeclProxy decl : decls) {
        final String name = decl.getName();
        mNames.add(name);
      }
      mDeletionVictims = new LinkedList<InsertInfo>();
      mHasShownDialog = false;
      final ModuleProxy module = mRoot.getModuleSubject();
      module.acceptVisitor(this);
      for (final EventDeclProxy decl : decls) {
        final InsertInfo info = new InsertInfo(decl);
        mDeletionVictims.add(info);
      }
      return mDeletionVictims;
    } catch (final VisitorException exception) {
      return null;
    } finally {
      mNames = null;
      mDeletionVictims = null;
      mComponent = null;
      mVictim = null;
    }
  }

  void insertItems(final List<InsertInfo> inserts)
  {
    final EditorWindowInterface iface = mRoot.getActiveEditorWindowInterface();
    final GraphEditorPanel surface;
    final GraphProxy visiblegraph;
    if (iface == null) {
      surface = null;
      visiblegraph = null;
    } else {
      surface = iface.getGraphEditorPanel();
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
        final List<Proxy> list = Casting.toList(inspos.getList());
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

  void deleteItems(final List<InsertInfo> deletes)
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
          final EditorWindowInterface iface =
            mRoot.getEditorWindowInterface(comp);
          if (iface != null) {
            final SelectionOwner panel = iface.getGraphEditorPanel();
            final List<ProxySubject> list = Collections.singletonList(proxy);
            panel.removeFromSelection(list);
          }
        }
        final ListInsertPosition inspos =
          (ListInsertPosition) insert.getInsertPosition();
        final List<Proxy> list = Casting.toList(inspos.getList());
        final int index = inspos.getPosition();
        list.remove(index);
      }
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
  public Object visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
  {
    final EventListExpressionProxy block = edge.getLabelBlock();
    return block.acceptVisitor(this);
  }

  public Object visitEventAliasProxy(final EventAliasProxy alias)
    throws VisitorException
  {
    mComponent = alias;
    mVictim = alias;
    final ExpressionProxy expr = alias.getExpression();
    return expr.acceptVisitor(this);
  }

  public Object visitEventListExpressionProxy
    (final EventListExpressionProxy elist)
    throws VisitorException
  {
    mVictim = null;
    final Collection<? extends Proxy> list = elist.getEventList();
    return visitCollection(list);
  }

  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    final Collection<? extends Proxy> list = foreach.getBody();
    return visitCollection(list);
  }

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

  public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    handleNamedIdentifier(ident, name);
    return null;
  }

  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final Collection<? extends Proxy> components = module.getComponentList();
    visitCollection(components);
    final Collection<? extends Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);
    return null;
  }

  public Object visitNodeProxy(final NodeProxy node)
    throws VisitorException
  {
    final EventListExpressionProxy props = node.getPropositions();
    return props.acceptVisitor(this);
  }

  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    mComponent = comp;
    final GraphProxy graph = comp.getGraph();
    return graph.acceptVisitor(this);
  }

  public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    handleNamedIdentifier(ident, name);
    return null;
  }

  public Object visitVariableComponentProxy(final VariableComponentProxy var)
    throws VisitorException
  {
    mComponent = var;
    final Collection<? extends Proxy> list = var.getVariableMarkings();
    return visitCollection(list);
  }

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
    final StringBuffer buffer = new StringBuffer(256);
    buffer.append("The event '");
    buffer.append(name);
    buffer.append("' is still used in ");
    final String type = ProxyNamer.getItemClassName(mComponent);
    buffer.append(type);
    buffer.append(" '");
    final IdentifierProxy ident = mComponent.getIdentifier();
    buffer.append(ident);
    buffer.append("'.\n");
    buffer.append("Would you like to delete all occurrences of ");
    buffer.append(mNames.size() == 1 ? "this event?" : "these events?");
    showAutomaton();
    final Frame frame = mRoot.getRootWindow();
    final int choice =
      JOptionPane.showConfirmDialog(frame, buffer, "Event in Use!",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
    if (choice != JOptionPane.OK_OPTION) {
      throw new VisitorException();
    }
  }

  private void showAutomaton()
  {
    if (mComponent instanceof SimpleComponentSubject) {
      try {
        final SimpleComponentSubject comp =
          (SimpleComponentSubject) mComponent;
        final EditorWindowInterface iface = mRoot.showEditor(comp);
        // Select them ...
        comp.acceptVisitor(this);
        final List<Proxy> selection = findSelection(mDeletionVictims);
        final SelectionOwner panel = iface.getGraphEditorPanel();
        panel.replaceSelection(selection);
        panel.scrollToVisible(selection);
        mDeletionVictims.clear();
        // Restore keyboard focus!
        mRoot.showEvents();
      } catch (final GeometryAbsentException exception) {
        // No geometry? --- Nice try anyway ...
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }
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

}