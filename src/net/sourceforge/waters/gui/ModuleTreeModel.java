//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleTreeModel
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.CollectionSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * A tree model for the aliases panels.
 *
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
    mChildrenGetterVisitor = new ChildrenGetterVisitor();
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
      mListeners = new LinkedList<TreeModelListener>();
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
      ("AliasesTreeModel does not support value change!");
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
        final ProxySubject ancestor = getVisibleAncestorInTree(source);
        if (ancestor != null) {
          final TreePath path = createPath(ancestor);
          final TreeModelEvent newevent =
            new TreeModelEvent(this, path, null, null);
          fireStructureChanged(newevent);
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
   * to the tree must be found. Since graphs ({@link
   * net.sourceforge.waters.model.module.GraphProxy GraphProxy}) are not
   * associated with any tree, their children will not have any ancestor
   * in a tree.
   * @return The given {@link Proxy} or its closest ancestor that has a
   *         direct node to be rendered in a tree, or <CODE>null</CODE> if no
   *         suitable can be determined.
   */
  ProxySubject getVisibleAncestorInTree(final Subject subject)
  {
    final Proxy proxy;
    if (subject instanceof Proxy) {
      proxy = (Proxy) subject;
    } else {
      proxy = SubjectTools.getProxyParent(subject);
    }
    return getVisibleAncestorInTree(proxy);
  }

  /**
   * Finds the closest ancestor of the given {@link Proxy} that is displayed
   * in a tree. This method checks the types of objects and their parents to
   * determine whether an object is to be displayed in some tree view. For an
   * ancestor to be found, a contiguous sequence of parents associated to the
   * tree must be found. Since graphs (
   * {@link net.sourceforge.waters.model.module.GraphProxy GraphProxy}) are
   * not associated with any tree, their children will not have any ancestor
   * in a tree.
   *
   * @return The given {@link Proxy} or its closest ancestor that has a
   *         direct node to be rendered in a tree, or <CODE>null</CODE> if no
   *         suitable can be determined.
   */
  ProxySubject getVisibleAncestorInTree(final Proxy proxy)
  {
    Subject subject = (Subject) proxy;
    if (subject == null) {
      return null;
    }
    while (!isInTree(subject)) {
      if (subject instanceof GraphProxy) {
        return null;
      }
      subject = subject.getParent();
    }
    return (ProxySubject) subject;
  }

  /**
   * Finds the closest proper ancestor of the given object that has a direct
   * node to be rendered in a tree.
   * @return The closest parent displayed in trees, which is not equal to the
   * argument, or <CODE>null</CODE> if no such parent can be found.
   */
  ProxySubject getProperAncestorInTree(final Subject subject)
  {
    final Proxy proxy = SubjectTools.getProxyParent(subject);
    return getVisibleAncestorInTree(proxy);
  }

  /**
   * Finds the closest proper ancestor of the given node that has a direct
   * node to be rendered in a tree.
   * @return The closest parent displayed in trees, which is not equal to the
   * argument, or <CODE>null</CODE> if no such parent can be found.
   */
  ProxySubject getProperAncestorInTree(final ProxySubject node)
  {
    final Proxy parent = SubjectTools.getProxyParent(node);
    return getVisibleAncestorInTree(parent);
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
    if (mListeners != null) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesInserted(event);
      }
    }
  }

  private void fireNodesRemoved(final TreeModelEvent event)
  {
    if (mListeners != null) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesRemoved(event);
      }
    }
  }

  @SuppressWarnings("unused")
  private void fireNodesChanged(final TreeModelEvent event)
  {
    if (mListeners != null) {
      final Collection<TreeModelListener> copy =
        new ArrayList<TreeModelListener>(mListeners);
      for (final TreeModelListener listener : copy) {
        listener.treeNodesChanged(event);
      }
    }
  }

  private void fireStructureChanged(final TreeModelEvent event)
  {
    if (mListeners != null) {
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
    public ListSubject<? extends ProxySubject> visitForeachProxy(final ForeachProxy foreach)
    {
      final ForeachSubject foreachSub = (ForeachSubject) foreach;
      return foreachSub.getBodyModifiable();
    }

    @Override
    public ListSubject<? extends ProxySubject> visitGroupNodeProxy(final GroupNodeProxy simple){
      final GroupNodeSubject node = (GroupNodeSubject)simple;
      return node.getPropositions().getEventIdentifierListModifiable();
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
    public ListSubject<? extends ProxySubject> visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      final ParameterBindingSubject para = (ParameterBindingSubject)binding;
      if(para.getExpression() instanceof EventListExpressionSubject){
        final EventListExpressionSubject list = (EventListExpressionSubject) para.getExpression();
         return list.getEventIdentifierListModifiable();
      }
      return null;
    }

    @Override
    public ListSubject<? extends ProxySubject> visitSimpleNodeProxy(final SimpleNodeProxy simple){
      final SimpleNodeSubject node = (SimpleNodeSubject)simple;
      return node.getPropositions().getEventIdentifierListModifiable();
    }
  }


  //#########################################################################
  //# Tree visibility
  private boolean isInTree(final Subject subject)
  {
    if (subject instanceof Proxy && mChildrenGetterVisitor.getChildren((Proxy) subject) != null) {
      return true; // internal node
    }
    final Subject nonEventListAncestor = getNonEventListAncestor(subject);
    if (subject.getParent() instanceof CollectionSubject<?>
        && isInTree(nonEventListAncestor)) {
      return true; // leaf node
    }
    return false;
  }

  private ProxySubject getNonEventListAncestor(final Subject subject)
  {
    ProxySubject parent = SubjectTools.getProxyParent(subject);
    while (parent instanceof EventListExpressionSubject) {
      parent = SubjectTools.getProxyParent(parent);
    }
    return parent;
  }

  //#########################################################################
  //# Data Members
  private final ProxySubject mRoot;
  private Collection<TreeModelListener> mListeners;
  private final ListSubject<? extends ProxySubject> mRootList;
  private final ChildrenGetterVisitor mChildrenGetterVisitor;

}
