//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.Collection;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.ProxyTools;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.queue.TIntQueue;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;


/**
 * <P>A search space of <CODE>int</CODE> primitives for depth-first search.</P>
 *
 * <P>This class encapsulates a stack and a hash set.
 * The stack contains unvisited items to be processed in last-in-first-out
 * order. The hash set contains all visited items, i.e., all items that
 * have ever been added to the stack. Items contained in this set are never
 * added to the stack a second time.</P>
 *
 * @author Robi Malik
 */

public class DFSIntSearchSpace
  implements TIntQueue
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new search space.
   */
  public DFSIntSearchSpace()
  {
    mStack = new TIntArrayStack();
    mVisited = new TIntHashSet();
  }

  /**
   * Creates a new search space.
   * @param  size          The initial size of the visited hash set.
   */
  public DFSIntSearchSpace(final int size)
  {
    mStack = new TIntArrayStack();
    mVisited = new TIntHashSet(size);
  }

  /**
   * Creates a new search space.
   * @param  size          The initial size of the visited hash set.
   * @param  noEntryValue  A value to represent NULL or missing items.
   */
  public DFSIntSearchSpace(final int size, final int noEntryValue)
  {
    mStack = new TIntArrayStack(size, noEntryValue);
    mVisited = new TIntHashSet(size, 0.5f, noEntryValue);
  }


  //#########################################################################
  //# Interface gnu.trove.queue.TIntQueue
  /**
   * Removes the top element from the stack and returns it.
   */
  @Override
  public int element()
  {
    if (mStack.size() > 0) {
      return mStack.pop();
    } else {
      throw new NoSuchElementException
        ("Attempting to retrieve element from empty queue!");
    }
  }

  /**
   * Adds the given item to the stack, if it has not been visited before.
   * @return <CODE>true</CODE>
   */
  @Override
  public boolean offer(final int item)
  {
    if (mVisited.add(item)) {
      mStack.push(item);
    }
    return true;
  }

  /**
   * Returns the top element of the stack, without removing it.
   */
  @Override
  public int peek()
  {
    return mStack.peek();
  }

  /**
   * Removes the top element from the stack and returns it.
   */
  @Override
  public int poll()
  {
    return mStack.pop();
  }


  //#########################################################################
  //# Interface interface gnu.trove.TIntCollection
  /**
   * Returns whether the stack is empty, i.e., whether a call to
   * {@link #poll()} will return a result.
   */
  @Override
  public boolean isEmpty()
  {
    return mStack.size() == 0;
  }

  /**
   * Adds the given item to the stack, if it has not been visited before.
   * @return <CODE>true</CODE> if the item has been added,
   *         <CODE>false</CODE> if it already was in the visited set.
   */
  @Override
  public boolean add(final int item)
  {
    if (mVisited.add(item)) {
      mStack.push(item);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds to the stack any item from the given collection that have
   * not been visited before.
   * @return <CODE>true</CODE> if some item has been added,
   *         <CODE>false</CODE> if all items already were in the visited set.
   */
  @Override
  public boolean addAll(final Collection<? extends Integer> items)
  {
    boolean result = false;
    for (final int item : items) {
      result |= add(item);
    }
    return result;
  }

  /**
   * Adds to the stack any item from the given collection that have
   * not been visited before.
   * @return <CODE>true</CODE> if some item has been added,
   *         <CODE>false</CODE> if all items already were in the visited set.
   */
  @Override
  public boolean addAll(final TIntCollection items)
  {
    boolean result = false;
    final TIntIterator iter = items.iterator();
    while (iter.hasNext()) {
      final int item = iter.next();
      result |= add(item);
    }
    return result;
  }

  /**
   * Adds to the stack any item from the given array that have
   * not been visited before.
   * @return <CODE>true</CODE> if some item has been added,
   *         <CODE>false</CODE> if all items already were in the visited set.
   */
  @Override
  public boolean addAll(final int[] items)
  {
    boolean result = false;
    for (final int item : items) {
      result |= add(item);
    }
    return result;
  }

  /**
   * Clears this search space.
   * This method removes all items both from the queue and from the
   * visited set.
   */
  @Override
  public void clear()
  {
    mStack.clear();
    mVisited.clear();
  }

  /**
   * Returns whether the given item has been visited.
   * @return <CODE>true</CODE> if the given item has ever been added to
   *         this search space.
   */
  @Override
  public boolean contains(final int item)
  {
    return mVisited.contains(item);
  }

  /**
   * Returns whether all items in the given collection have been visited.
   * @return <CODE>true</CODE> if the given items have ever been added to
   *         this search space.
   */
  @Override
  public boolean containsAll(final Collection<?> items)
  {
    return mVisited.containsAll(items);
  }

  /**
   * Returns whether all items in the given collection have been visited.
   * @return <CODE>true</CODE> if the given items have ever been added to
   *         this search space.
   */
  @Override
  public boolean containsAll(final TIntCollection items)
  {
    return mVisited.containsAll(items);
  }

  /**
   * Returns whether all items in the given array have been visited.
   * @return <CODE>true</CODE> if the given items have ever been added to
   *         this search space.
   */
  @Override
  public boolean containsAll(final int[] items)
  {
    return mVisited.containsAll(items);
  }

  /**
   * Calls the given procedure for all items in the visited set,
   * in an unspecified order.
   */
  @Override
  public boolean forEach(final TIntProcedure proc)
  {
    return mVisited.forEach(proc);
  }

  /**
   * Returns the value used to represent NULL or missing items.
   */
  @Override
  public int getNoEntryValue()
  {
    return mStack.getNoEntryValue();
  }

  /**
   * Returns an iterator over all items in the visited set,
   * in an unspecified order.
   */
  @Override
  public TIntIterator iterator()
  {
    return mVisited.iterator();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean remove(final int item)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean removeAll(final Collection<?> items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean removeAll(final TIntCollection items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean removeAll(final int[] items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean retainAll(final Collection<?> items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean retainAll(final TIntCollection items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public boolean retainAll(final int[] items)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not support deletions!");
  }

  /**
   * Returns the number of items still waiting to be processed on the stack.
   */
  @Override
  public int size()
  {
    return mStack.size();
  }

  /**
   * Returns an array containing all visited items in the search space.
   */
  @Override
  public int[] toArray()
  {
    return mVisited.toArray();
  }

  /**
   * Returns an array containing all visited items in the search space.
   */
  @Override
  public int[] toArray(final int[] array)
  {
    return mVisited.toArray(array);
  }


  //#########################################################################
  //# Visited Set Access
  /**
   * Returns whether the given item has been visited.
   * @return <CODE>true</CODE> if the given item has ever been added to
   *         this search space.
   */
  public boolean isVisited(final int item)
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
  private final TIntStack mStack;
  private final TIntHashSet mVisited;

}
