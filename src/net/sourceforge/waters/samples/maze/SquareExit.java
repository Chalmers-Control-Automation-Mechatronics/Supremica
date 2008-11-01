//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareExit
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareExit extends Square
{

  //#########################################################################
  //# Constructors
  SquareExit(final Point pos)
  {
    super(pos);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.EXIT;
  }
  
  int[] getEnteringActions()
  {
    return canGetRock() ? ENTERING_ROCK : ENTERING_NOROCK;
  }

  boolean canExit()
  {
    return canGetRock();
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING_ROCK = {Action.MOVE, Action.ESCAPE};
  private static final int[] ENTERING_NOROCK = {Action.ESCAPE};

}
