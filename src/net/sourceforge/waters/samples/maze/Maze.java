//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   Maze
//###########################################################################
//# $Id: Maze.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class Maze
{

  //#########################################################################
  //# Constructors
  Maze(final String name, final Collection<Square> points)
  {
    mName = name;
    mSquares = new HashMap<Point,Square>(points.size());
    for (final Square square : points) {
      final Point pos = square.getPosition();
      mSquares.put(pos, square);
    }
    mKeys = new KeyRing();
    mHasLockedDoors = false;
  }


  //#########################################################################
  //# Simple Access Methods
  String getName()
  {
    return mName;
  }

  Collection<Square> getSquares()
  {
    return mSquares.values();
  }


  //#########################################################################
  //# Action Creation
  void createActions()
  {
    doBFS();
    createMoveActions();
    createUnlockActions();
  }

  private void createMoveActions()
  {
    for (final Square square : mSquares.values()) {
      final Point pos = square.getPosition();
      square.createActions();
      for (int i = 0; i < DIRECTIONS.length; i++) {
        final Point direction = DIRECTIONS[i];
        final Square neighbour = getNeighbour(pos, direction);
        if (neighbour != null) {
          final Point npos = neighbour.getPosition();
          final int[] kinds = neighbour.getEnteringActions();
          for (int j = 0; j < kinds.length; j++) {
            final int kind = kinds[j];
            final Action action = new Action(kind, pos, npos);
            square.addAction(Action.EXIT, action);
            neighbour.addAction(kind, action);
          }
	  if (neighbour.canGetRock()) {
	    final Square crushed = getNeighbour(npos, direction);
	    if (crushed != null) {
	      final Action action = new Action(Action.PUSH, pos, npos);
	      square.addAction(Action.EXIT, action);
	      neighbour.addAction(Action.CLEAR, action);
	      crushed.addAction(Action.CRUSH, action);
	    }
          }
        }
      }
    }
  }

  private void createUnlockActions()
  {
    for (final Key key : mKeys.getKeys()) {
      final Collection<Square> locations = key.getLocations();
      final Collection<Square> locks = key.getLocks();
      for (final Square lock : locks) {
	for (final Square location : locations) {
          final Collection<Action> pickups =
	    location.getActions(Action.PICKUP);
          lock.addActions(Action.UNLOCK, pickups);
        }
      }
    }
  }


  //#########################################################################
  //# Optimisation
  private void doBFS()
  {
    boolean changed;
    do {
      changed = false;
      if (locateKeys()) {
        changed = true;
      }
      if (locateHero()) {
        changed = true;
      }
      if (mHasLockedDoors && locateRocks()) {
        changed = true;
      }
    } while (changed);
    if (!mHasLockedDoors) {
      locateRocks();
    }
  }

  private boolean locateKeys()
  {
    boolean changed = false;
    mHasLockedDoors = false;
    mKeys.clear();
    for (final Square square : mSquares.values()) {
      mKeys.add(square);
    }
    for (final Key key : mKeys.getKeys()) {
      final Collection<Square> locations = key.getLocations();
      if (locations.isEmpty()) {
        changed = true;
        for (final Square lock : key.getLocks()) {
          if (lock.getSquareKind() == Square.DOOR) {
            final SquareDoor door = (SquareDoor) lock;
            door.setUnlockable(false);
            mHasLockedDoors = true;
          } else {
            removeSquare(lock);
          }
        }
      }
    }
    return changed;
  }

  private boolean locateHero()
  {
    final List<Square> open = new LinkedList<Square>();
    for (final Square square : mSquares.values()) {
      final boolean ishero = (square.getSquareKind() == Square.HERO);
      square.setReachable(ishero);
      if (ishero) {
        open.add(square);
      }
    }
    while (!open.isEmpty()) {
      final Square square = open.remove(0);
      final Point pos = square.getPosition();
      final Square north = getNeighbour(pos, NORTH);
      if (north != null && !north.isReachable()) {
        north.setReachable(true);
        open.add(north);
      }
      final Square south = getNeighbour(pos, SOUTH);
      if (south != null && !south.isReachable()) {
        south.setReachable(true);
        open.add(south);
      }
      final Square east = getNeighbour(pos, EAST);
      if (east != null && !east.isReachable()) {
        east.setReachable(true);
        open.add(east);
      }
      final Square west = getNeighbour(pos, WEST);
      if (west != null && !west.isReachable()) {
        west.setReachable(true);
        open.add(west);
      }
    }
    boolean changed = false;
    final Iterator<Square> iter = mSquares.values().iterator();
    while (iter.hasNext()) {
      final Square square = iter.next();
      if (!square.isReachable()) {
        iter.remove();
        changed = true;
      }
    }
    return changed;
  }

  private boolean locateRocks()
  {
    final List<Square> open = new LinkedList<Square>();
    for (final Square square : mSquares.values()) {
      final boolean isrock = (square.getSquareKind() == Square.ROCK);
      square.setCanGetRock(isrock);
      if (isrock) {
        open.add(square);
      }
    }
    while (!open.isEmpty()) {
      final Square square = open.remove(0);
      final Point pos = square.getPosition();
      final Square north = getNeighbour(pos, NORTH);
      if (north != null) {
        final Square south = getNeighbour(pos, SOUTH);
        if (south != null) {
          if (!north.canGetRock()) {
            north.setCanGetRock(true);
            open.add(north);
          }
          if (!south.canGetRock()) {
            south.setCanGetRock(true);
            open.add(south);
          }
        }
      }
      final Square east = getNeighbour(pos, EAST);
      if (east != null) {
        final Square west = getNeighbour(pos, WEST);
        if (west != null) {
          if (!east.canGetRock()) {
            east.setCanGetRock(true);
            open.add(east);
          }
          if (!west.canGetRock()) {
            west.setCanGetRock(true);
            open.add(west);
          }
        }
      }
    }
    boolean changed = false;
    if (mHasLockedDoors) {
      final Iterator<Square> iter = mSquares.values().iterator();
      while (iter.hasNext()) {
        final Square square = iter.next();
        if (square.getSquareKind() == Square.DOOR && !square.canGetRock()) {
          final SquareDoor door = (SquareDoor) square;
          if (!door.isUnlockable()) {
            iter.remove();
            changed = true;
          }
        }
      }
    }
    return changed;
  }
    

  //#########################################################################
  //# Accessing Squares
  private Square getNeighbour(final Point pos, final Point direction)
  {
    final int x = pos.x + direction.x;
    final int y = pos.y + direction.y;
    final Point npos = new Point(x, y);
    return mSquares.get(npos);
  }

  private void removeSquare(final Square square)
  {
    final Point pos = square.getPosition();
    mSquares.remove(pos);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final Map<Point,Square> mSquares;
  private final KeyRing mKeys;

  private boolean mHasLockedDoors;


  //#########################################################################
  //# Class Constants
  private static final Point NORTH = new Point(0, 1);
  private static final Point SOUTH = new Point(0, -1);
  private static final Point EAST = new Point(1, 0);
  private static final Point WEST = new Point(-1, 0);

  private static final Point DIRECTIONS[] = {NORTH, EAST, SOUTH, WEST};

}
