//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   BFSSeachSpace
//###########################################################################
//# $Id$
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

public class BFSSeachSpace<E>
  extends AbstractQueue<E>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new search space.
   */
  public BFSSeachSpace()
  {
    mQueue = new ArrayDeque<E>();
    mVisited = new THashSet<E>();
  }

  /**
   * Creates a new search space.
   * @param  size    The initial size of the visited hash set.
   */
  public BFSSeachSpace(final int size)
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


  //#########################################################################
  //# Data Members
  private final Queue<E> mQueue;
  private final Set<E> mVisited;

}
