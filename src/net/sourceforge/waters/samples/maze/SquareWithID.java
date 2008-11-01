//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareWithID
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


abstract class SquareWithID extends Square
{

  //#########################################################################
  //# Constructors
  SquareWithID(final Point pos, final String key)
  {
    super(pos);
    mKey = key;
  }


  //#########################################################################
  //# Getters and Setters
  String getKeyName()
  {
    return mKey;
  }


  //#########################################################################
  //# Data Members
  private final String mKey;

}
