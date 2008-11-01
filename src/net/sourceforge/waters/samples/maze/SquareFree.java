//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareFree
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareFree extends Square
{

  //#########################################################################
  //# Constructors
  SquareFree(final Point pos)
  {
    super(pos);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.FREE;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

}
