//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   SquareGate
//###########################################################################
//# $Id: SquareGate.java,v 1.1 2005-02-17 01:43:36 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class SquareGate extends SquareWithID
{

  //#########################################################################
  //# Constructors
  SquareGate(final Point pos, final String gate)
  {
    super(pos, gate);
  }


  //#########################################################################
  //# Getters and Setters
  int getSquareKind()
  {
    return Square.GATE;
  }
  
  int[] getEnteringActions()
  {
    return ENTERING;
  }


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

}
