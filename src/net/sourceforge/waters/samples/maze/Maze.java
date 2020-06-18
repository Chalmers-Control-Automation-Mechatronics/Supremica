//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    mAvailableKeys = new KeyRing();
    mCollectedKeys = new HashSet<String>();
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
  //# Action Evaluation
  boolean applyMove(final Point source, final Point target)
  {
    final Square sourceSquare = mSquares.get(source);
    if (sourceSquare == null || sourceSquare.getSquareKind() != Square.HERO) {
      throw new IllegalStateException("No hero at position " + source + "!");
    }
    final Point dir = new Point(target.x - source.x, target.y - source.y);
    if (!dir.equals(NORTH) && !dir.equals(SOUTH) &&
        !dir.equals(EAST) && !dir.equals(WEST)) {
      throw new IllegalArgumentException
        ("Can't move from " + source + " to " + target + " in one step!");
    }
    final Square targetSquare = mSquares.get(target);
    if (targetSquare == null) {
      throw new IllegalStateException
        ("Target square " + target + " is not accessible!");
    }
    final Square newSourceSquare = new SquareFree(source);
    final Square newTargetSquare = new SquareHero(target);
    switch (targetSquare.getSquareKind()) {
    case Square.FREE:
      mSquares.put(source, newSourceSquare);
      mSquares.put(target, newTargetSquare);
      return false;
    case Square.HERO:
      throw new IllegalStateException
        ("Target square " + target + " contains another hero!");
    case Square.EXIT:
      mSquares.put(source, newSourceSquare);
      return true;
    case Square.ROCK:
      {
        final Point behind = new Point(target.x + dir.x, target.y + dir.y);
        final Square behindSquare = mSquares.get(behind);
        if (behindSquare == null) {
          throw new IllegalStateException
            ("Trying to push rock from " + target +
             " to inaccessible square " + behind + "!");
        }
        switch (behindSquare.getSquareKind()) {
        case Square.FREE:
        case Square.EXIT:
        case Square.DOOR:
        case Square.KEY:
          final Square newBehindSquare = new SquareRock(behind);
          mSquares.put(source, newSourceSquare);
          mSquares.put(target, newTargetSquare);
          mSquares.put(behind, newBehindSquare);
          return false;
        default:
          throw new IllegalStateException
            ("Trying to push rock from " + target + " to blocked square " +
             behind + "!");
        }
      }
    case Square.DOOR:
    case Square.GATE:
      {
        final String key = targetSquare.getKeyName();
        if (mCollectedKeys.contains(key)) {
          mSquares.put(source, newSourceSquare);
          mSquares.put(target, newTargetSquare);
          return false;
        } else {
          throw new IllegalStateException
            ("Can't unlock at " + target + " without key '" + key + "'!");
        }
      }
    case Square.KEY:
      {
        final String key = targetSquare.getKeyName();
        mCollectedKeys.add(key);
        mSquares.put(source, newSourceSquare);
        mSquares.put(target, newTargetSquare);
        return false;
      }
    default:
      throw new IllegalStateException
        ("Illegal target square type " + targetSquare.getSquareKind() + "!");
    }
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
      if (square.canExit()) {
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
              if (crushed != null && crushed.canGetRock()) {
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
  }

  private void createUnlockActions()
  {
    for (final Key key : mAvailableKeys.getKeys()) {
      final Collection<Square> locations = key.getLocations();
      final Collection<Square> locks = key.getLocks();
      for (final Square lock : locks) {
        final Point lockpos = lock.getPosition();
	for (final Square location : locations) {
          final Collection<Action> pickups =
	    location.getActions(Action.PICKUP);
          for (final Action pickup : pickups) {
            final Point startpos = pickup.getSource();
            if (!lockpos.equals(startpos)) {
              lock.addAction(Action.UNLOCK, pickup);
            }
          }
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
    mAvailableKeys.clear();
    for (final Square square : mSquares.values()) {
      mAvailableKeys.add(square);
    }
    for (final Key key : mAvailableKeys.getKeys()) {
      final ReachabilityVisitor visitor = new KeyReachabilityVisitor(key);
      reachability(visitor);
      final Collection<Square> locations = key.getLocations();
      int numkeys = locations.size();
      for (final Square location : locations) {
        if (!location.isReachable()) {
          final Point pos = location.getPosition();
          final Square free = new SquareFree(pos);
          mSquares.put(pos, free);
          numkeys--;
          changed = true;
        }
      }
      if (numkeys == 0) {
        for (final Square lock : key.getLocks()) {
          if (lock.getSquareKind() == Square.DOOR) {
            final SquareDoor door = (SquareDoor) lock;
            if (door.isUnlockable()) {
              door.setUnlockable(false);
              mHasLockedDoors = true;
              changed = true;
            }
          } else {
            removeSquare(lock);
            changed = true;
          }
        }
      }
    }
    return changed;
  }

  private boolean locateHero()
  {
    final ReachabilityVisitor visitor = new WalkingReachabilityVisitor();
    reachability(visitor);
    mNumExits = 0;
    boolean changed = false;
    final Iterator<Square> iter = mSquares.values().iterator();
    while (iter.hasNext()) {
      final Square square = iter.next();
      if (!square.isReachable()) {
        iter.remove();
        changed = true;
      } else if (square.getSquareKind() == Square.EXIT) {
        mNumExits++;
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
          visitRock(south, open);
          visitRock(north, open);
        }
      }
      final Square east = getNeighbour(pos, EAST);
      if (east != null) {
        final Square west = getNeighbour(pos, WEST);
        if (west != null) {
          visitRock(west, open);
          visitRock(east, open);
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

  private boolean visitRock(final Square square, final List<Square> open)
  {
    if (!square.canGetRock() &&
        !(mNumExits == 1 && square.getSquareKind() == Square.EXIT)) {
      square.setCanGetRock(true);
      open.add(square);
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Reachability
  private void reachability(ReachabilityVisitor visitor)
  {
    final List<Square> open = new LinkedList<Square>();
    for (final Square square : mSquares.values()) {
      square.setReachable(false);
      if (square.getSquareKind() == Square.HERO && visitor.visit(square)) {
        open.add(square);
      }
    }
    while (!open.isEmpty()) {
      final Square square = open.remove(0);
      final Point pos = square.getPosition();
      final Square north = getNeighbour(pos, NORTH);
      if (north != null && !north.isReachable() && visitor.visit(north)) {
        open.add(north);
      }
      final Square south = getNeighbour(pos, SOUTH);
      if (south != null && !south.isReachable() && visitor.visit(south)) {
        open.add(south);
      }
      final Square east = getNeighbour(pos, EAST);
      if (east != null && !east.isReachable() && visitor.visit(east)) {
        open.add(east);
      }
      final Square west = getNeighbour(pos, WEST);
      if (west != null && !west.isReachable() && visitor.visit(west)) {
        open.add(west);
      }
    }
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
  //# Inner Class ReachabilityVisitor
  private interface ReachabilityVisitor {
    public boolean visit(Square square);
  }

  private class WalkingReachabilityVisitor
    implements ReachabilityVisitor
  {
    public boolean visit(final Square square)
    {
      square.setReachable(true);
      return true;
    }
  }

  private class KeyReachabilityVisitor
    implements ReachabilityVisitor
  {
    private KeyReachabilityVisitor(final Key key)
    {
      mKeyName = key.getName();
    }

    public boolean visit(final Square square)
    {
      square.setReachable(true);
      final String keyname = square.getKeyName();
      return !mKeyName.equals(keyname);
    }

    private final String mKeyName;
  }        


  //#########################################################################
  //# Data Members
  private final String mName;
  private final Map<Point,Square> mSquares;
  private final KeyRing mAvailableKeys;
  private final Set<String> mCollectedKeys;

  private boolean mHasLockedDoors;
  private int mNumExits;


  //#########################################################################
  //# Class Constants
  private static final Point NORTH = new Point(0, 1);
  private static final Point SOUTH = new Point(0, -1);
  private static final Point EAST = new Point(1, 0);
  private static final Point WEST = new Point(-1, 0);

  private static final Point DIRECTIONS[] = {NORTH, EAST, SOUTH, WEST};

}
