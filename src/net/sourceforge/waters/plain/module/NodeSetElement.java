//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   NodeSetElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.Collection;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedArraySet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.module.NodeProxy;


class NodeSetElement extends IndexedArraySet<NodeProxy> {

  //#########################################################################
  //# Constructor
  NodeSetElement(final Collection<? extends NodeProxy> nodes)
    throws DuplicateNameException, ItemNotFoundException, NameNotFoundException
  {
    super(nodes.size());
    for (final NodeProxy node : nodes) {
      final Collection<NodeProxy> children = node.getImmediateChildNodes();
      checkAllUnique(children);
      insertUnique(node);
    }
  }
  
  //#########################################################################
  //# Overrides from base class IndexedArrayList
  protected ItemNotFoundException createItemNotFound(final String name)
  {
    return new ItemNotFoundException
      ("Graph does not contain the node named '" + name + "'!");
  }
  
  protected NameNotFoundException createNameNotFound(final String name)
  {
    return new NameNotFoundException
      ("Graph does not contain a node named '" + name + "'!");
  }
  
  protected DuplicateNameException createDuplicateName(final String name)
  {
    return new DuplicateNameException
      ("Graph already contains a node named '" + name + "'!");
  }
  
}