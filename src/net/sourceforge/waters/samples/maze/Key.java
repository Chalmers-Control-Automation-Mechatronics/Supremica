//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   Key
//###########################################################################
//# $Id: Key.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;


class Key
{

  //#########################################################################
  //# Constructors
  Key(final String name)
  {
    mName = name;
    mLocations = new LinkedList();
    mLocks = new LinkedList();
  }


  //#########################################################################
  //# Getters and Setters
  String getName()
  {
    return mName;
  }

  Collection getLocations()
  {
    return Collections.unmodifiableCollection(mLocations);
  }

  Collection getLocks()
  {
    return Collections.unmodifiableCollection(mLocks);
  }

  void addLocation(final Square location)
  {
    mLocations.add(location);
  }

  void addLock(final Square lock)
  {
    mLocks.add(lock);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final Collection mLocations;
  private final Collection mLocks;

}
