//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareRock
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareRock extends Square
{

  //#########################################################################
  //# Constructors
  SquareRock(final Point pos)
  {
    super(pos);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.ROCK;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

}
