//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareKey
//###########################################################################
//# $Id: SquareKey.java,v 1.1 2005-02-17 01:43:36 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareKey extends SquareWithID
{

  //#########################################################################
  //# Constructors
  SquareKey(final Point pos, final String key)
  {
    super(pos, key);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.KEY;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE, Action.PICKUP};

}
