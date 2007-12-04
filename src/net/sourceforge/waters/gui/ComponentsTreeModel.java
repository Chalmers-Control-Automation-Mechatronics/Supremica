//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentsTreeModel
//###########################################################################
//# $Id: ComponentsTreeModel.java,v 1.4 2007-12-04 22:30:27 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.ModuleSubject;


/**
 * A tree model for the components panel.
 * Mirrors the module hierarchy beneath the components list,
 * and also includes a root node representing the module itself.
 *
 * @author Robi Malik
 */

class ComponentsTreeModel
  implements TreeModel, ModelObserver
{

  //#########################################################################
  //# Constructor
  ComponentsTreeModel(final ModuleSubject module)
  {
    mModule = module;
    mChildrenGetterVisitor = new ChildrenGetterVisitor();
    mTypeCheckerVisitor = new TypeCheckerVisitor();
    mListeners = null;
    mModule.addModelObserver(this);
  }


  //#########################################################################
  //# Clean Up
  void close()
  {
    mModule.removeModelObserver(this);
    mListeners = null;
  }

    
  //#########################################################################
  //# Interface javax.swing.tree.TreeModel
  public void addTreeModelListener(final TreeModelListener listener)
  {
    if (mListeners == null) {
      mListeners = new LinkedList<TreeModelListener>();
    }
    mListeners.add(listener);
  }

  public Proxy getChild(final Object parent, final int index)
  {
    final Proxy proxy = (Proxy) parent;
    final List<? extends Proxy> children =
      mChildrenGetterVisitor.getChildren(proxy);
    if (children == null) {
      throw new IllegalArgumentException
        ("Tree node of class " + parent.getClass().getName() +
         " has no children!");
    } else {
      return children.get(index);
    }    
  }

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

  public int getIndexOfChild(final Object parent, final Object child)
  {
    final ProxySubject proxy = (ProxySubject) parent;
    if (isInTree(proxy)) {
      final List<? extends Proxy> children =
        mChildrenGetterVisitor.getChildren(proxy);
      if (children == null) {
        return -1;
      } else {
        return children.indexOf(child);
      }
    } else {
      return -1;
    }
  }

  public ModuleSubject getRoot()
  {
    return mModule;
  }

  public boolean isLeaf(final Object node)
  {
    final Proxy proxy = (Proxy) node;
    return mChildrenGetterVisitor.getChildren(proxy) == null;
  }

  public void removeTreeModelListener(final TreeModelListener listener)
  {
    mListeners.remove(listener);
    if (mListeners.isEmpty()) {
      mListeners = null;
    }
  }

  public void valueForPathChanged(final TreePath path, final Object newvalue)
  {
    throw new UnsupportedOperationException
      ("ComponentsTreeModel does not support value change!");
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    if (mListeners != null) {
      final Subject source = event.getSource();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        {
          final ProxySubject value = (ProxySubject) event.getValue();
          if (isInTree(value)) {
            final ProxySubject parent = (ProxySubject) source.getParent();
            final int index = event.getIndex();
            final TreeModelEvent newevent =
              createTreeModelEvent(parent, index, value);
            if (index >= 0) {
              fireNodesInserted(newevent);
            } else {
              fireStructureChanged(newevent);
            }
          }
          break;
        }
      case ModelChangeEvent.ITEM_REMOVED:
        {
          final ProxySubject parent = (ProxySubject) source.getParent();
          final Proxy value = (Proxy) event.getValue();
           if (canBeInTree(value) && isInTree(parent)) {
            final int index = event.getIndex();
            final TreeModelEvent newevent =
              createTreeModelEvent(parent, index, value);
            if (index >= 0) {
              fireNodesRemoved(newevent);
            } else {
              fireStructureChanged(newevent);
            }
          }
          break;
        }
      case ModelChangeEvent.STATE_CHANGED:
        {
          final ProxySubject psource = getVisibleAncestorInTree(source);
          if (psource != null) {
            final TreePath path = createPath(psource);
            final TreeModelEvent newevent =
              new TreeModelEvent(this, path, null, null);
            fireNodesChanged(newevent);
          }
          break;
        }
      default:
        break;
      }
    }
  }


  //#########################################################################
  //# Computing Paths
  TreePath createPath(ProxySubject node)
  {
    final List<Subject> list = new LinkedList<Subject>();
    while (node != null) {
      list.add(node);
      node = getParentInTree(node);
    }
    final int len = list.size();
    final Object[] path = new Object[len];
    int i = len;
    for (final Subject nodei : list) {
      path[--i] = nodei;
    }
    return new TreePath(path);
  }

  boolean isInTree(final ProxySubject node)
  {
    return canBeInTree(node) && getRootInTree(node) == mModule;
  }

  boolean canBeInTree(final Proxy node)
  {
    return mTypeCheckerVisitor.canBeInTree(node);
  }

  ProxySubject getRootInTree(ProxySubject node)
  {
    ProxySubject root = node;
    do {
      node = getParentInTree(node);
      if (node == null) {
        return root;
      }
      root = node;
    } while (true);
  }

  ProxySubject getParentInTree(final ProxySubject node)
  {
    final Subject parent1 = node.getParent();
    if (parent1 == null) {
      return null;
    } else {
      return (ProxySubject) parent1.getParent();
    }
  }

  ProxySubject getVisibleAncestorInTree(final Subject subject)
  {
    return mTypeCheckerVisitor.getVisibleAncestorInTree(subject);
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


  //#########################################################################
  //# Inner Class ChildrenGetterVisitor
  private static class ChildrenGetterVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private List<? extends Proxy> getChildren(final Proxy proxy)
    {
      try {
	return (List<? extends Proxy>) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public List<Proxy> visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public List<Proxy> visitForeachComponentProxy
      (final ForeachComponentProxy foreach)
    {
      return foreach.getBody();
    }

    public List<ParameterBindingProxy> visitInstanceProxy
      (final InstanceProxy inst)
    {
      return inst.getBindingList();
    }

    public List<Proxy> visitModuleProxy(final ModuleProxy module)
    {
      return module.getComponentList();
    }

  }


  //#########################################################################
  //# Inner Class TypeCheckerVisitor
  private static class TypeCheckerVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ProxySubject getVisibleAncestorInTree(Subject subject)
    {
      for (; !canBeInTree(subject); subject = subject.getParent()) {
        if (subject instanceof GraphProxy) {
          return null;
        }
      }
      return (ProxySubject) subject;
    }
           
    private boolean canBeInTree(final Subject subject)
    {
      if (subject instanceof Proxy) {
        final Proxy proxy = (Proxy) subject;
        return canBeInTree(proxy);
      } else {
        return false;
      }
    }

    private boolean canBeInTree(final Proxy proxy)
    {
      try {
	return (Boolean) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Boolean visitForeachComponentProxy
      (final ForeachComponentProxy foreach)
    {
      return true;
    }

    public Boolean visitInstanceProxy(final InstanceProxy inst)
    {
      return true;
    }

    public Boolean visitModuleProxy(final ModuleProxy module)
    {
      return true;
    }

    public Boolean visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return true;
    }

    public Boolean visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      return true;
    }

    public Boolean visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return true;
    }

  }


  //#########################################################################
  //# Data Members
  private ModuleSubject mModule;
  private Collection<TreeModelListener> mListeners;

  private final ChildrenGetterVisitor mChildrenGetterVisitor;
  private final TypeCheckerVisitor mTypeCheckerVisitor;

}
