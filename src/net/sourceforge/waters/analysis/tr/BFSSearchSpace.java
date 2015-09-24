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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.set.hash.THashSet;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;


/**
 * <P>A queue for breadth-first search.</P
 *
 * <P>This class encapsulates a queue and a hash set.
 * The queue contains unvisited items to be processed in first-in-first-out
 * order. The hash set contains all visited items, i.e., all items that
 * have ever been added to the queue. Items contained in this set are never
 * added to the queue a second time.</P>
 *
 * <P>The {@link Queue} methods implemented by this class refer to the queue.
 * Specific methods are provided to access the visited set.</P>
 *
 * @author Robi Malik
 */

public class BFSSearchSpace<E>
  extends AbstractQueue<E>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new search space.
   */
  public BFSSearchSpace()
  {
    mQueue = new ArrayDeque<E>();
    mVisited = new THashSet<E>();
  }

  /**
   * Creates a new search space.
   * @param  size    The initial size of the visited hash set.
   */
  public BFSSearchSpace(final int size)
  {
    mQueue = new ArrayDeque<E>();
    mVisited = new THashSet<E>(size);
  }


  //#########################################################################
  //# Interface java.util.Queue<E>
  /**
   * Clears this search space.
   * This method removes all items both from the queue and from the
   * visited set.
   */
  @Override
  public void clear()
  {
    mQueue.clear();
    mVisited.clear();
  }

  @Override
  public boolean isEmpty()
  {
    return mQueue.isEmpty();
  }

  @Override
  public Iterator<E> iterator()
  {
    return mQueue.iterator();
  }

  @Override
  public boolean offer(final E item)
  {
    if (mVisited.add(item)) {
      mQueue.add(item);
    }
    return true;
  }

  @Override
  public E poll()
  {
    return mQueue.poll();
  }

  @Override
  public E peek()
  {
    return mQueue.peek();
  }

  @Override
  public int size()
  {
    return mQueue.size();
  }


  //#########################################################################
  //# Specific Access
  /**
   * Returns whether the given item has been visited.
   * @return <CODE>true</CODE> if the given item has ever been added to
   *         this search space.
   */
  public boolean isVisited(final E item)
  {
    return mVisited.contains(item);
  }

  /**
   * Returns the number of items in the visited set,
   * i.e., the total number of distinct items added to this search space.
   */
  public int visitedSize()
  {
    return mVisited.size();
  }

  /**
   * Adds the given item to the queue. This method is the same as the
   * {@link #add(Object) add()} and {@link #offer(Object) offer()} methods
   * except for the return value.
   * @param   item   The item to be added.
   * @return  <CODE>true</CODE> if the item was not already visited and
   *          has been added to the queue, <CODE>false</CODE> otherwise.
   */
  public boolean addIfUnvisited(final E item)
  {
    if (mVisited.add(item)) {
      mQueue.add(item);
      return true;
    } else {
      return false;
    }
  }
  /**
   * Returns all the visited items in the search space.
   */
  public Set<E> getVisitedSet()
  {
    return mVisited;
  }


  //#########################################################################
  //# Data Members
  private final Queue<E> mQueue;
  private final Set<E> mVisited;

}
