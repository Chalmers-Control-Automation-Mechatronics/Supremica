//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   KeyRing
//###########################################################################
//# $Id: KeyRing.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


class KeyRing
{

  //#########################################################################
  //# Constructors
  KeyRing()
  {
    mMap = new HashMap();
  }


  //#########################################################################
  //# Accessing the Map
  void add(final Square square)
  {
    final String name = square.getKeyName();
    if (name != null) {
      Key key = (Key) mMap.get(name);
      if (key == null) {
	key = new Key(name);
	mMap.put(name, key);
      }
      if (square instanceof SquareKey) {
	key.addLocation(square);
      } else {
	key.addLock(square);
      }
    }
  }

  Iterator iterator()
  {
    return mMap.values().iterator();
  }


  //#########################################################################
  //# Data Members
  private final Map mMap;

}
