//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;


/**
 * <P>A queue implementation that prevents duplicates.</P>
 *
 * <P>This class encapsulates a queue and a hash set, which contain the
 * elements. Before adding items to the queue, the hash set is checked,
 * and if the item is already contained, it is not added.</P>
 *
 * <P>The {@link Queue} methods implemented by this class refer to the queue.
 * No methods are provided to access the hash set directly.</P>
 *
 * @author Robi Malik
 */

public class DuplicateFreeQueue<E>
  extends AbstractQueue<E>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new duplicate-free queue.
   */
  public DuplicateFreeQueue()
  {
    mQueue = new ArrayDeque<E>();
    mSet = new THashSet<E>();
  }

  /**
   * Creates a new duplicate-free queue.
   * @param  size    The initial size of the queue and hash set.
   */
  public DuplicateFreeQueue(final int size)
  {
    mQueue = new ArrayDeque<E>(size);
    mSet = new THashSet<E>(size);
  }

  /**
   * Creates a new duplicate-free queue.
   * @param  data    Initial contents of queue.
   */
  public DuplicateFreeQueue(final Collection<? extends E> data)
  {
    mQueue = new ArrayDeque<E>(data);
    mSet = new THashSet<E>(data);
  }


  //#########################################################################
  //# Interface java.util.Queue<E>
  @Override
  public void clear()
  {
    mQueue.clear();
    mSet.clear();
  }

  @Override
  public boolean contains(final Object item)
  {
    return mSet.contains(item);
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
    if (mSet.add(item)) {
      mQueue.add(item);
    }
    return true;
  }

  @Override
  public E poll()
  {
    final E item = mQueue.poll();
    if (item != null) {
      mSet.remove(item);
    }
    return item;
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
  //# Data Members
  private final Queue<E> mQueue;
  private final Set<E> mSet;

}
