//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   CompiledNode
//###########################################################################
//# $Id: CompiledNode.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.sourceforge.waters.model.base.EmptyIterator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class CompiledNode
{

  //#########################################################################
  //# Constructors
  CompiledNode(final SimpleNodeProxy node, final StateProxy state)
  {
    mNode = node;
    mState = state;
    mEdges = new LinkedList();
    mTransitions = new IdentityHashMap();
  }

  CompiledNode(final GroupNodeProxy node)
  {
    mNode = node;
    mState = null;
    mEdges = new LinkedList();
    mTransitions = null;
  }


  //#########################################################################
  //# Simple Access
  NodeProxy getNode()
  {
    return mNode;
  }

  StateProxy getState()
  {
    return mState;
  }

  Iterator getEdgeIterator()
  {
    return mEdges.iterator();
  }

  void addEdge(final EdgeProxy edge)
  {
    mEdges.add(edge);
  }

  Iterator getCompiledTransitionIterator(final EventProxy event)
  {
    final Collection list = (Collection) mTransitions.get(event);
    if (list == null) {
      return EmptyIterator.getInstance();
    } else {
      return list.iterator();
    }
  }

  void addTransition(final TransitionProxy trans,
		     final NodeProxy group)
  {
    final CompiledTransition entry = new CompiledTransition(trans, group);
    final EventProxy event = trans.getEvent();
    Collection list = (Collection) mTransitions.get(event);
    if (list == null) {
      list = new LinkedList();
      mTransitions.put(event, list);
    }
    list.add(entry);
  }

  boolean hasChildNode(final NodeProxy node)
  {
    collectChildNodes();
    return mChildNodes.containsKey(node);
  }

  void clearChildNodes()
  {
    mChildNodes = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void collectChildNodes()
  {
    if (mChildNodes == null) {
      mChildNodes = new IdentityHashMap();
      final Iterator iter = mNode.getChildNodeIterator();
      while (iter.hasNext()) {
	final NodeProxy node = (NodeProxy) iter.next();
	if (mNode != node) {
	  mChildNodes.put(node, node);
	}
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final NodeProxy mNode;
  private final StateProxy mState;
  private final Collection mEdges;
  private final Map mTransitions;

  private Map mChildNodes;

}
