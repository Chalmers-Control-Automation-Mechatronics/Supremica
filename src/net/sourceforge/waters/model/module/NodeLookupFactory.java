//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   NodeLookupFactory
//###########################################################################
//# $Id: NodeLookupFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.IndexedCollectionProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;


class NodeLookupFactory {

  //#########################################################################
  //# Constructor
  NodeLookupFactory(final IndexedCollectionProxy statemap)
  {
    mNodeMap = statemap;
  }


  //#########################################################################
  //# Node Lookup
  NodeProxy findNode(final String name)
    throws NameNotFoundException
  {
    return (NodeProxy) mNodeMap.find(name);
  }


  //#########################################################################
  //# Data Members
  private final IndexedCollectionProxy mNodeMap;

}
