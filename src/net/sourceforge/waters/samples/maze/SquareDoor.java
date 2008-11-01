//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   SquareDoor
//###########################################################################
//# $Id$
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
    mIsUnlockable = true;
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

  boolean isUnlockable()
  {
    return mIsUnlockable;
  }

  void setUnlockable(final boolean unlockable)
  {
    mIsUnlockable = unlockable;
  }

  String getTemplateName()
  {
    if (mIsUnlockable) {
      return super.getTemplateName();
    } else {
      return TEMPLNAME_LOCKED;
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mIsUnlockable;


  //#########################################################################
  //# Class Constants
  private static final int[] ENTERING = {Action.MOVE};

  private static final String TEMPLNAME_LOCKED = "door_locked";

}
