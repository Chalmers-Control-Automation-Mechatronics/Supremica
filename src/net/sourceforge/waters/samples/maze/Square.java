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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


abstract class Square
{

  //#########################################################################
  //# Constructors
  Square(final Point pos)
  {
    mPosition = pos;
    mActions = new HashMap<Integer,Collection<Action>>();
    mIsReachable = true;
    mCanGetRock = true;
  }


  //#########################################################################
  //# Getters and Setters
  abstract int getSquareKind();
  abstract int[] getEnteringActions();

  Point getPosition()
  {
    return mPosition;
  }

  String getKeyName()
  {
    return null;
  }

  boolean isReachable()
  {
    return mIsReachable;
  }

  void setReachable(final boolean reachable)
  {
    mIsReachable = reachable;
  }

  boolean canGetRock()
  {
    return mCanGetRock;
  }

  void setCanGetRock(final boolean canget)
  {
    mCanGetRock = canget;
  }

  boolean canExit()
  {
    return true;
  }


  //#########################################################################
  //# Names
  String getName()
  {
    final StringBuilder result = new StringBuilder("sq_");
    result.append(mPosition.x);
    result.append('_');
    result.append(mPosition.y);
    return result.toString();
  }

  String getTemplateName()
  {
    final int kind = getSquareKind();
    if (canGetRock()) {
      return TEMPLNAME_ROCK[kind];
    } else {
      return TEMPLNAME_NOROCK[kind];
    }
  }


  //#########################################################################
  //# Action Recording
  Collection<Integer> getActionKinds()
  {
    return mActions.keySet();
  }

  Collection<Action> getActions(final int kind)
  {
    Collection<Action> list = mActions.get(kind);
    if (list == null) {
      list = new LinkedList<Action>();
      mActions.put(kind, list);
    }
    return list;
  }

  void addAction(final int kind, final Action action)
  {
    final Collection<Action> list = getActions(kind);
    list.add(action);
  }

  void addActions(final int kind, final Collection<Action> actions)
  {
    final Collection<Action> list = getActions(kind);
    list.addAll(actions);
  }

  void createActions()
  {
    int[] entering = getEnteringActions();
    for (int i = 0; i < entering.length; i++) {
      getActions(entering[i]);
    }
    if (canGetRock()) {
      getActions(Action.CRUSH);
      getActions(Action.CLEAR);
    }
  }


  //#########################################################################
  //# Data Members
  private final Point mPosition;
  private final Map<Integer,Collection<Action>> mActions;
  private boolean mIsReachable;
  private boolean mCanGetRock;


  //#########################################################################
  //# Class Constants
  static final int FREE = 0;
  static final int HERO = 1;
  static final int EXIT = 2;
  static final int ROCK = 3;
  static final int DOOR = 4;
  static final int GATE = 5;
  static final int KEY  = 6;

  private static final String TEMPLNAME_ROCK[] = {
    "free_rock",
    "hero_rock",
    "exit_rock",
    "rock",
    "door_rock",
    "gate_rock",
    "key_rock"
  };

  private static final String TEMPLNAME_NOROCK[] = {
    "free_norock",
    "hero_norock",
    null,
    null,
    "door_norock",
    "door_norock",
    "key_norock"
  };

}








