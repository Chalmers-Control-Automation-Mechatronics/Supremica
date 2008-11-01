//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   Square
//###########################################################################
//# $Id$
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
    final StringBuffer result = new StringBuffer("sq_");
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
