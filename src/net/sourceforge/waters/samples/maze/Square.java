//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   Square
//###########################################################################
//# $Id: Square.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
    mActions = new HashMap();
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

  boolean canGetRock()
  {
    return mCanGetRock;
  }

  void setCanGetRock(final boolean canget)
  {
    mCanGetRock = canget;
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
  Collection getActionKinds()
  {
    return mActions.keySet();
  }

  Collection getActions(final int kind)
  {
    final Integer key = new Integer(kind);
    Collection list = (Collection) mActions.get(key);
    if (list == null) {
      list = new LinkedList();
      mActions.put(key, list);
    }
    return list;
  }

  void addAction(final int kind, final Action action)
  {
    final Collection list = getActions(kind);
    list.add(action);
  }

  void addActions(final int kind, final Collection actions)
  {
    final Collection list = getActions(kind);
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
  private final Map mActions;
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
    "exit_norock",
    null,
    "door_norock",
    "gate_norock",
    "key_norock"
  };

}
