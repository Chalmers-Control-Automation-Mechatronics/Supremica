//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   Action
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;


class Action
{

  //#########################################################################
  //# Constructors
  Action(final int kind, final Point source, final Point target)
  {
    mKind = kind;
    mSource = source;
    mTarget = target;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final Action action = (Action) partner;
      return
	mKind == action.mKind &&
	mSource.equals(action.mSource) &&
	mTarget.equals(action.mTarget);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mKind + 5 + mSource.hashCode() + 25 * mTarget.hashCode();
  }


  //#########################################################################
  //# Naming
  String getName()
  {
    final StringBuffer result = new StringBuffer(ACTNAME[mKind]);
    result.append('_');
    result.append(mSource.x);
    result.append('_');
    result.append(mSource.y);
    result.append('_');
    result.append(mTarget.x);
    result.append('_');
    result.append(mTarget.y);
    return result.toString();
  }

  boolean isEscapeAction()
  {
    return mKind == ESCAPE;
  }

  static String getTemplateName(final int kind)
  {
    return TEMPLNAME[kind];
  }


  //#########################################################################
  //# Simple Access
  Point getSource()
  {
    return mSource;
  }

  Point getTarget()
  {
    return mTarget;
  }


  //#########################################################################
  //# Data Members
  private final int mKind;
  private final Point mSource;
  private final Point mTarget;


  //#########################################################################
  //# Class Constants
  static final int MOVE = 0;
  static final int PICKUP = 1;
  static final int ESCAPE = 2;
  static final int PUSH = 3;

  static final int CRUSH = PUSH;
  static final int CLEAR = 4;
  static final int EXIT = 5;
  static final int UNLOCK = 6;

  private static final String ACTNAME[] = {
    "walk",
    "pickup",
    "escape",
    "push"
  };

  private static final String TEMPLNAME[] = {
    "enter",
    "pickup",
    "escape",
    "crush",
    "clear",
    "exit",
    "unlock"
  };

}
