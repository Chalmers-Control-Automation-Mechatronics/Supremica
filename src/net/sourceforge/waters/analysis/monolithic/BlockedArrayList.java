//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


public class BlockedArrayList<T> extends AbstractList<T> {

  //#########################################################################
  //# Constructors
  public BlockedArrayList(final Class<T> clazz)
  {
    clazz_ = clazz;
    size_ = 0;
    blocks_ = new ArrayList<T[]>(128);
  }


  //#########################################################################
  //# Interface java.util.List
  @SuppressWarnings("unchecked")
  public boolean add(final T item)
  {
    final int blockno = size_ / BLOCK_SIZE;
    final T[] block;
    if (blockno < blocks_.size()) {
      block = blocks_.get(blockno);
    } else {
      block = (T[]) Array.newInstance(clazz_, BLOCK_SIZE);
      blocks_.add(block);
    }
    block[size_ % BLOCK_SIZE] = item;
    size_++;
    return true;
  }

  public T get(final int index)
  {
    final int blockno = index / BLOCK_SIZE;
    if (blockno < blocks_.size()) {
      final T[] block = blocks_.get(blockno);
      return block[index % BLOCK_SIZE];
    } else {
      throw new IndexOutOfBoundsException
        ("The index " + index + " is out of bounds of list size " +
         blocks_.size() + "!");
    }
  }

  public int size()
  {
    return size_;
  }


  //#########################################################################
  //# Data Members
  private final Class<T> clazz_;
  private int size_;                    // The number of nodes
  private final List<T[]> blocks_;            // Fixed length blocks for nodes

  private static final int BLOCK_SIZE = 1024;

}
