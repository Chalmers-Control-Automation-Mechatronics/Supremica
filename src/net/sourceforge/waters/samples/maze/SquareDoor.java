//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareDoor
//###########################################################################
//# $Id: SquareDoor.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareDoor extends SquareWithID
{

  //#########################################################################
  //# Constructors
  SquareDoor(final Point pos, final String door)
  {
    super(pos, door);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.DOOR;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

}
