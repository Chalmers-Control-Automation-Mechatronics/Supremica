//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   StateLookupFactory
//###########################################################################
//# $Id: StateLookupFactory.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.IndexedCollectionProxy;


class StateLookupFactory {

  //#########################################################################
  //# Constructor
  StateLookupFactory(final IndexedCollectionProxy statemap)
  {
    mStateMap = statemap;
  }


  //#########################################################################
  //# State Lookup
  StateProxy findState(final String name)
    throws NameNotFoundException
  {
    return (StateProxy) mStateMap.find(name);
  }


  //#########################################################################
  //# Data Members
  private final IndexedCollectionProxy mStateMap;

}
