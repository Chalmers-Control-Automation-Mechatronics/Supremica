//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   KeyRing
//###########################################################################
//# $Id: KeyRing.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class KeyRing
{

  //#########################################################################
  //# Constructors
  KeyRing()
  {
    mMap = new HashMap<String,Key>();
  }


  //#########################################################################
  //# Accessing the Map
  void clear()
  {
    mMap.clear();
  }

  void add(final Square square)
  {
    final String name = square.getKeyName();
    if (name != null) {
      Key key = mMap.get(name);
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

  Collection<Key> getKeys()
  {
    return mMap.values();
  }


  //#########################################################################
  //# Data Members
  private final Map<String,Key> mMap;

}
