//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.samples.maze
//# CLASS:   Maze
//###########################################################################
//# $Id: Maze.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


class Maze
{

  //#########################################################################
  //# Constructors
  Maze(final String name, final Collection points)
  {
    mName = name;
    mSquares = new HashMap(points.size());
    final Iterator iter = points.iterator();
    while (iter.hasNext()) {
      final Square square = (Square) iter.next();
      final Point pos = square.getPosition();
      mSquares.put(pos, square);
    }
  }


  //#########################################################################
  //# Simple Access Methods
  String getName()
  {
    return mName;
  }

  Collection getSquares()
  {
    return mSquares.values();
  }


  //#########################################################################
  //# Optimisation
  void locateRocks()
  {
    final List open = new LinkedList();
    final Iterator iter = mSquares.values().iterator();
    while (iter.hasNext()) {
      final Square square = (Square) iter.next();
      final boolean isrock = (square.getSquareKind() == Square.ROCK);
      square.setCanGetRock(isrock);
      if (isrock) {
        open.add(square);
      }
    }
    while (!open.isEmpty()) {
      final Square square = (Square) open.remove(0);
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
  }
    

  //#########################################################################
  //# Action Creation
  void createActions()
  {
    final KeyRing keys = new KeyRing();
    createMoveActions(keys);
    createUnlockActions(keys);
  }

  private void createMoveActions(final KeyRing keys)
  {
    final Iterator iter = mSquares.values().iterator();
    while (iter.hasNext()) {
      final Square square = (Square) iter.next();
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
      keys.add(square);
    }
  }

  private void createUnlockActions(final KeyRing keys)
  {
    final Iterator keyiter = keys.iterator();
    while (keyiter.hasNext()) {
      final Key key = (Key) keyiter.next();
      final Collection locations = key.getLocations();
      final Collection locks = key.getLocks();
      final Iterator lockiter = locks.iterator();
      while (lockiter.hasNext()) {
        final Square lock = (Square) lockiter.next();
        final Iterator sqiter = locations.iterator();
        while (sqiter.hasNext()) {
          final Square location = (Square) sqiter.next();
          final Collection pickups = location.getActions(Action.PICKUP);
          lock.addActions(Action.UNLOCK, pickups);
        }
      }
    }
  }


  //#########################################################################
  //# Finding Neighbours
  private Square getNeighbour(final Point pos, final Point direction)
  {
    final int x = pos.x + direction.x;
    final int y = pos.y + direction.y;
    final Point npos = new Point(x, y);
    return (Square) mSquares.get(npos);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final Map mSquares;


  //#########################################################################
  //# Class Constants
  private static final Point NORTH = new Point(0, 1);
  private static final Point SOUTH = new Point(0, -1);
  private static final Point EAST = new Point(1, 0);
  private static final Point WEST = new Point(-1, 0);

  private static final Point DIRECTIONS[] = {NORTH, EAST, SOUTH, WEST};

}
