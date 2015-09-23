//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
    final StringBuilder result = new StringBuilder(ACTNAME[mKind]);
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
