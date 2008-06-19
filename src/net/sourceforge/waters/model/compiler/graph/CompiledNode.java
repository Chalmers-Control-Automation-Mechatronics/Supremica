//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.graph
//# CLASS:   CompiledNode
//###########################################################################
//# $Id: CompiledNode.java,v 1.1 2008-06-19 11:34:55 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

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
    mEdges = new LinkedList<EdgeProxy>();
    mTransitions =
      new IdentityHashMap<EventProxy,Collection<CompiledTransition>>();
  }

  CompiledNode(final GroupNodeProxy node)
  {
    mNode = node;
    mState = null;
    mEdges = new LinkedList<EdgeProxy>();
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

  Collection<EdgeProxy> getEdges()
  {
    return mEdges;
  }

  void addEdge(final EdgeProxy edge)
  {
    mEdges.add(edge);
  }

  Collection<CompiledTransition> getCompiledTransitions
    (final EventProxy event)
  {
    Collection<CompiledTransition> result = mTransitions.get(event);
    if (result == null) {
      return Collections.emptySet();
    } else {
      return result;
    }
  }

  void addTransition(final TransitionProxy trans, final NodeProxy group)
  {
    final CompiledTransition entry = new CompiledTransition(trans, group);
    final EventProxy event = trans.getEvent();
    Collection<CompiledTransition> list = mTransitions.get(event);
    if (list == null) {
      list = new LinkedList<CompiledTransition>();
      mTransitions.put(event, list);
    }
    list.add(entry);
  }

  boolean hasProperChildNode(final NodeProxy node)
  {
    collectProperChildNodes();
    return mProperChildNodes.containsKey(node);
  }

  void clearProperChildNodes()
  {
    mProperChildNodes = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void collectProperChildNodes()
  {
    if (mProperChildNodes == null) {
      mProperChildNodes = new IdentityHashMap<NodeProxy,NodeProxy>();
      collectProperChildNodes(mNode);
    }
  }

  private void collectProperChildNodes(final NodeProxy node)
  {
    final Collection<NodeProxy> children = node.getImmediateChildNodes();
    for (final NodeProxy child : children) {
      mProperChildNodes.put(child, child);
      collectProperChildNodes(child);
    }
  }


  //#########################################################################
  //# Data Members
  private final NodeProxy mNode;
  private final StateProxy mState;
  private final Collection<EdgeProxy> mEdges;
  private final Map<EventProxy,Collection<CompiledTransition>> mTransitions;

  private Map<NodeProxy,NodeProxy> mProperChildNodes;

}
