//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareHero
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareHero extends Square
{

  //#########################################################################
  //# Constructors
  SquareHero(final Point pos)
  {
    super(pos);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.HERO;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

}
