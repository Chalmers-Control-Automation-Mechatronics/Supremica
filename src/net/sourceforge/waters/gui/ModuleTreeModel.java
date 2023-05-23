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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.NestedBlockSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


/**
 * A tree model for browsing various parts of a module. Used by the
 * components and alias panels, and for editing node properties.
 * @see ModuleTree
 * @author Carly Hona, Robi Malik, Tom Levy
 */

class ModuleTreeModel
  implements TreeModel, ModelObserver
{

  //#########################################################################
  //# Constructor
  ModuleTreeModel(final ProxySubject root,
                  final ListSubject<? extends ProxySubject> list)
  {
    mRoot = root;
    mRootList = list;
    mRootList.addModelObserver(this);
  }


  //#########################################################################
  //# Clean Up
  void close()
  {
    mRootList.removeModelObserver(this);
    mListeners = null;
  }


  //#########################################################################
  //# Interface javax.swing.tree.TreeModel
  @Override
  public void addTreeModelListener(final TreeModelListener listener)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<>();
    }
    mListeners.add(listener);
  }

  @Override
  public Proxy getChild(final Object parent, final int index)
  {
    final Proxy proxy = (Proxy) parent;
    final List<? extends Proxy> children =
      mChildrenGetterVisitor.getChildren(proxy);
    if (children == null) {
      throw new IllegalArgumentException
        ("Tree node of class " + ProxyTools.getShortClassName(parent) +
         " has no children!");
    } else {
      return children.get(index);
    }
  }

  @Override
  public int getChildCount(final Object parent)
  {
    final Proxy proxy = (Proxy) parent;
    final List<? extends Proxy> children =
      mChildrenGetterVisitor.getChildren(proxy);
    if (children == null) {
      return 0;
    } else {
      return children.size();
    }
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child)
  {
    final ProxySubject proxy = (ProxySubject) parent;
    final List<? extends Proxy> children =
      mChildrenGetterVisitor.getChildren(proxy);
    if (children == null) {
      return -1;
    } else {
      return children.indexOf(child);
    }
  }

  @Override
  public ProxySubject getRoot()
  {
    return mRoot;
  }

  @Override
  public boolean isLeaf(final Object node)
  {
    final Proxy proxy = (Proxy) node;
    return mChildrenGetterVisitor.getChildren(proxy) == null;
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener listener)
  {
    mListeners.remove(listener);
    if (mListeners.isEmpty()) {
      mListeners = null;
    }
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newvalue)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support value change!");
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  @Override
  public void modelChanged(final ModelChangeEvent event)
  {
    if (mListeners != null) {
      final Subject source = event.getSource();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
      case ModelChangeEvent.ITEM_REMOVED:
        final ProxySubject nonEventListAncestor =
          getNonEventListAncestor(source);
        if (mChildrenGetterVisitor.getChildren(nonEventListAncestor) != null) {
          final Object value = event.getValue();
          final int index = event.getIndex();
          final TreeModelEvent newevent =
            createTreeModelEvent(nonEventListAncestor, index, value);
          if (index < 0) {
            fireStructureChanged(newevent);
          } else if (event.getKind() == ModelChangeEvent.ITEM_ADDED) {
            fireNodesInserted(newevent);
          } else {
            fireNodesRemoved(newevent);
          }
          break;
        }
        // fall through ...
      case ModelChangeEvent.NAME_CHANGED:
      case ModelChangeEvent.STATE_CHANGED:
      case ModelChangeEvent.GEOMETRY_CHANGED:
      case ModelChangeEvent.GENERAL_NOTIFICATION:
        final ProxySubject ancestor = getVisibleAncestorInTree(source);
        if (ancestor != null) {
          final TreePath path = createPath(ancestor);
          final TreeModelEvent newevent = new TreeModelEvent(this, path);
          fireNodesChanged(newevent);
        }
        break;
      default:
        break;
      }
    }
  }

  @Override
  public int getModelObserverPriority()
  {
    return ModelObserver.DEFAULT_PRIORITY;
  }


  //#########################################################################
  //# Computing Paths
  TreePath createPath(ProxySubject node)
  {
    final List<Subject> list = new LinkedList<Subject>();
    while (node != null) {
      list.add(node);
      node = getProperAncestorInTree(node);
    }
    final int len = list.size();
    final Object[] path = new Object[len];
    int i = len;
    for (final Subject nodei : list) {
      path[--i] = nodei;
    }
    return new TreePath(path);
  }

  /**
   * Finds the closest ancestor of the given object that is displayed in
   * a tree. This method checks the types of objects and their parents to
   * determine whether an object is to be displayed in some tree view.
   * It does not check whether the object is actually contained in this tree.
   * For an ancestor to be found, a contiguous sequence of parents associated
   * to the tree must be found.
   * @return The given {@link Subject} or its closest ancestor that has a
   *         direct node to be rendered in a tree, or <CODE>null</CODE> if no
   *         suitable can be determined.
   */
  ProxySubject getVisibleAncestorInTree(final Subject subject)
  {
    final ProxySubject proxy;
    if (subject instanceof Proxy) {
      proxy = (ProxySubject) subject;
    } else {
      proxy = SubjectTools.getProxyParent(subject);
    }
    return mVisibleAncestorVisitor.getVisibleAncestorInTree(proxy);
  }

  /**
   * Finds the closest ancestor of the given {@link Proxy} that is displayed
   * in a tree. This method checks the types of objects and their parents to
   * determine whether an object is to be displayed in some tree view. For an
   * ancestor to be found, a contiguous sequence of parents associated to the
   * tree must be found.
   * @return The given {@link Proxy} or its closest ancestor that has a
   *         direct node to be rendered in a tree, or <CODE>null</CODE> if no
   *         suitable ancestor can be determined.
   */
  ProxySubject getVisibleAncestorInTree(final ProxySubject proxy)
  {
    return mVisibleAncestorVisitor.getVisibleAncestorInTree(proxy);
  }

  /**
   * Finds the closest proper ancestor of the given object that has a direct
   * node to be rendered in a tree.
   * @return The closest parent displayed in trees, which is not equal to the
   * argument, or <CODE>null</CODE> if no such parent can be found.
   */
  ProxySubject getProperAncestorInTree(final Subject subject)
  {
    final ProxySubject proxy = SubjectTools.getProxyParent(subject);
    return getVisibleAncestorInTree(proxy);
  }


  //#########################################################################
  //# Auxiliary Methods
  private TreeModelEvent createTreeModelEvent
    (final ProxySubject parent, final int index, final Object child)
  {
    final TreePath path = createPath(parent);
    if (index >= 0) {
      final int[] indexes = {index};
      final Object[] children = {child};
      return new TreeModelEvent(this, path, indexes, children);
    } else {
      return new TreeModelEvent(this, path);
    }
  }

  private void fireNodesInserted(final TreeModelEvent event)
  {
    if (mListeners != null && belongsToTree(event.getTreePath())) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesInserted(event);
      }
    }
  }

  private void fireNodesRemoved(final TreeModelEvent event)
  {
    if (mListeners != null && belongsToTree(event.getTreePath())) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesRemoved(event);
      }
    }
  }

  private void fireNodesChanged(final TreeModelEvent event)
  {
    if (mListeners != null && belongsToTree(event.getTreePath())) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesChanged(event);
      }
    }
  }

  private void fireStructureChanged(final TreeModelEvent event)
  {
    if (mListeners != null && belongsToTree(event.getTreePath())) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeStructureChanged(event);
      }
    }
  }

  ListSubject<? extends ProxySubject>  getChildren(final Proxy parent){
    return mChildrenGetterVisitor.getChildren(parent);
  }


  //#########################################################################
  //# Tree visibility
  private ProxySubject getNonEventListAncestor(final Subject subject)
  {
    ProxySubject parent = SubjectTools.getProxyParent(subject);
    while (parent instanceof EventListExpressionSubject) {
      parent = SubjectTools.getProxyParent(parent);
    }
    return parent;
  }

  private boolean belongsToTree(final TreePath path)
  {
    return path.getPathComponent(0) == mRoot;
  }


  //#########################################################################
  //# Inner Class ChildrenGetterVisitor
  private class ChildrenGetterVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    @SuppressWarnings("unchecked")
    private ListSubject<? extends ProxySubject> getChildren(final Proxy proxy)
    {
      try {
        return (ListSubject<? extends ProxySubject>) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    @Override
    public ListSubject<? extends ProxySubject> visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public ListSubject<? extends ProxySubject> visitEventAliasProxy(final EventAliasProxy alias)
    {
      final EventAliasSubject event = (EventAliasSubject) alias;
      if (event.getExpression() instanceof PlainEventListSubject) {
        final PlainEventListSubject plain =
          (PlainEventListSubject) event.getExpression();
        return plain.getEventIdentifierListModifiable();
      } else {
        return null;
      }
    }

    @Override
    public ListSubject<? extends ProxySubject> visitNestedBlockProxy
      (final NestedBlockProxy nested)
    {
      final NestedBlockSubject nestedSub = (NestedBlockSubject) nested;
      return nestedSub.getBodyModifiable();
    }

    @Override
    public IndexedListSubject<ParameterBindingSubject> visitInstanceProxy(final InstanceProxy inst)
    {
      final InstanceSubject instance = (InstanceSubject)inst;
      return instance.getBindingListModifiable();
    }

    @Override
    public ListSubject<? extends ProxySubject> visitModuleProxy(final ModuleProxy module)
    {
      return mRootList;
    }

    @Override
    public ListSubject<? extends ProxySubject> visitNodeProxy
      (final NodeProxy proxy)
    {
      final NodeSubject node = (NodeSubject) proxy;
      return node.getPropositions().getEventIdentifierListModifiable();
    }

    @Override
    public ListSubject<? extends ProxySubject> visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      if (binding.getExpression() instanceof EventListExpressionSubject) {
        final EventListExpressionSubject list =
          (EventListExpressionSubject) binding.getExpression();
        return list.getEventIdentifierListModifiable();
      }
      return null;
    }
  }


  //#########################################################################
  //# Inner Class VisibleAncestorVisitor
  private class VisibleAncestorVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    ProxySubject getVisibleAncestorInTree(final ProxySubject proxy)
    {
      try {
        if (proxy == null) {
          return null;
        } else {
          return (ProxySubject) proxy.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitAliasProxy(final AliasProxy alias)
    {
      return alias;
    }

    @Override
    public Object visitComponentProxy(final ComponentProxy comp)
    {
      return comp;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierSubject subject = (IdentifierSubject) ident;
      final Subject parent = subject.getParent();
      if (parent instanceof Proxy) {
        final Proxy proxyParent = (ProxySubject) parent;
        return proxyParent.acceptVisitor(this);
      } else {
        final Proxy proxyParent = SubjectTools.getProxyParent(parent);
        final Object visibleAncestor = proxyParent.acceptVisitor(this);
        return visibleAncestor == proxyParent ? ident : visibleAncestor;
      }
    }

    @Override
    public Object visitModuleProxy(final ModuleProxy module)
    {
      return module == mRoot ? module : null;
    }

    @Override
    public Object visitNestedBlockProxy(final NestedBlockProxy nested)
      throws VisitorException
    {
      final NestedBlockSubject subject = (NestedBlockSubject) nested;
      final ProxySubject parent = SubjectTools.getProxyParent(subject);
      return parent.acceptVisitor(this) != null ? nested : null;
    }

    @Override
    public Object visitNodeProxy(final NodeProxy node)
    {
      return node == mRoot ? node : null;
    }

    @Override
    public Object visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return binding;
    }

    @Override
    public Object visitPlainEventListProxy(final PlainEventListProxy plain)
      throws VisitorException
    {
      final PlainEventListSubject subject = (PlainEventListSubject) plain;
      final ProxySubject parent = SubjectTools.getProxyParent(subject);
      return parent.acceptVisitor(this);
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionSubject subject = (SimpleExpressionSubject) expr;
      final Proxy proxyParent = SubjectTools.getProxyParent(subject);
      return proxyParent.acceptVisitor(this);
    }
  }


  //#########################################################################
  //# Data Members
  private final ProxySubject mRoot;
  private Collection<TreeModelListener> mListeners;
  private final ListSubject<? extends ProxySubject> mRootList;

  private final ChildrenGetterVisitor mChildrenGetterVisitor =
    new ChildrenGetterVisitor();
  private final VisibleAncestorVisitor mVisibleAncestorVisitor =
    new VisibleAncestorVisitor();

}
