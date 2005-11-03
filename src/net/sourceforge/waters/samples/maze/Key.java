//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   Key
//###########################################################################
//# $Id: Key.java,v 1.2 2005-11-03 01:24:16 robi Exp $
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
    mLocations = new LinkedList<Square>();
    mLocks = new LinkedList<Square>();
  }


  //#########################################################################
  //# Getters and Setters
  String getName()
  {
    return mName;
  }

  Collection<Square> getLocations()
  {
    return Collections.unmodifiableCollection(mLocations);
  }

  Collection<Square> getLocks()
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
  private final Collection<Square> mLocations;
  private final Collection<Square> mLocks;

}
