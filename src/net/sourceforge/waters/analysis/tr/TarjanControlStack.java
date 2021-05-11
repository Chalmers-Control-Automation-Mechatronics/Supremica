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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TIntArrayList;


/**
 * <P>The control stack for the iterative version of Tarjan's algorithm.</P>
 *
 * <P>The data structure consists of two array lists to hold the control
 * stack data and the link information of states.</P>
 *
 * <P>The control stack data has several entries, which are identified by
 * integers representing an index in an array list. This is called the
 * <I>stack index</I>, which refers to a block of three consecutive
 * integers:</P>
 * <OL>
 * <LI>The stack index of the next entry below on the stack,
 *     or -1 for the bottom-most entry.</LI>
 * <LI>The state index, which for unexpanded state pairs is the true state
 *     pair index, and for expanded state pairs its position on the component
 *     stack. Expanded state pairs are tagged by setting the {@link
 *     #MSB1 MSB1} of the state index. For unexpanded state pairs, the link
 *     entry in contains the stack
 *     index of the entry above their entry on the stack.</LI>
 * <LI>The parent index, which is the true state pair index of the
 *     state pair from where the current stack entry is being expanded.</LI>
 * </OL>
 * <P>The stack always contains a dummy top entry ({@link #mDummyTop}),
 * with the entry below it ({@link #mUsedTop}) being the first entry
 * to actually contain data. Unused stack entries are collected in a
 * linked list of free entries ({@link #mNextFree}).</P>
 *
 * <P>The link information associated a single integer to the true state index
 * of each visited state. If a state is on the control stack, the link points
 * to the entry on top of it on the control stack. For states not on the
 * control stack, the link can be used from outside using the {@link
 * #getLink(int) getLink()} and {@link #setLink(int, int) setLink()}
 * methods.</P>
 *
 * @author Nicholas McGrath
 */

public class TarjanControlStack
{
  //#########################################################################
  //# Constructor
  /**
   * Creates an empty control stack.
   */
  public TarjanControlStack()
  {
    mLinks = new TIntArrayList();
    mStack = new TIntArrayList();
    clear();
  }

  /**
   * Creates an empty control stack.
   * @param  initialSize  Estimate for the number of entries to be stored.
   */
  public TarjanControlStack(final int initialSize)
  {
    mLinks = new TIntArrayList(initialSize);
    mStack = new TIntArrayList(3 * initialSize);
    clear();
  }


  //########################################################################
  //# Stack Access
  /**
   * Resets the control stack and links list to be empty.
   */
  public void clear()
  {
    mLinks.clear();
    clearStack();
  }

  /**
   * Adds a new entry to the top of the stack. Also adds a link entry to
   * point to the new dummy top index.
   * @param  index  The state index to be stored in the new entry.
   * @param  parent The parent index to be stored in the new entry.
   */
  public void push(final int index, final int parent)
  {
    final int newTop = allocateEntry();
    setStackLink(newTop, mDummyTop);
    setStackIndex(mDummyTop, index);
    setStackParent(mDummyTop, parent);
    mLinks.add(newTop);
    mUsedTop = mDummyTop;
    mDummyTop = newTop;
  }

  /**
   * Removes the top-most entry from the control stack.
   */
  public void pop()
  {
    final int newTop = getStackLink(mUsedTop);
    setStackLink(mDummyTop, mNextFree);
    mNextFree = mDummyTop;
    mDummyTop = mUsedTop;
    mUsedTop = newTop;
  }

  /**
   * Changes the given stack entry to become the new top. This method
   * rearranges the stack to the new order and also updates any link
   * entries to point to the new predecessors of stack entries affected by
   * the move.
   * @param  stackPosAbove  The stack index of a stack entry above
   *                        the stack entry to become the new top.
   * @param  newParent      The true state index of the new parent state,
   *                        i.e., the parent from where the moved state is
   *                        considered to be expanded from after the move.
   */
  public void moveToTop(final int stackPosAbove, final int newParent)
  {
    if (stackPosAbove != mDummyTop) {
      final int pos = getStackLink(stackPosAbove);
      final int stackPosBelow = getStackLink(pos);
      setStackParent(pos, newParent);
      setStackLink(stackPosAbove, stackPosBelow);
      setStackLink(pos, mUsedTop);
      setStackLink(mDummyTop, pos);
      final int index = getStackIndex(pos);
      setLink(index, mDummyTop);
      final int indexBelow = getStackIndex(stackPosBelow);
      if ((indexBelow & MSB1) == 0) {
        setLink(indexBelow, stackPosAbove);
      }
      final int oldTopIndex = getStackIndex(mUsedTop);
      if ((oldTopIndex & MSB1) == 0) {
        setLink(oldTopIndex, pos);
      }
      mUsedTop = pos;
    }
  }

  /**
   * Returns whether the stack is empty.
   */
  public boolean isEmpty()
  {
    return mUsedTop < 0;
  }

  /**
   * Returns whether the top-most entry of the stack is flagged as
   * expanded.
   * @return <CODE>true</CODE> if the {@link #MSB1} of the state
   *         index of the top entry is set.
   * @throws IndexOutOfBoundsException if the stack is empty.
   */
  public boolean isTopExpanded()
  {
    return (getStackIndex(mUsedTop) & MSB1) != 0;
  }

  /**
   * Retrieves the state index of the top-most entry of the stack,
   * without the {@link #MSB1}.
   * @throws IndexOutOfBoundsException if the stack is empty.
   */
  public int getTopIndex()
  {
    return getStackIndex(mUsedTop) & ~MSB1;
  }

  /**
   * Retrieves the parent index of the top-most entry of the stack.
   * @throws IndexOutOfBoundsException if the stack is empty.
   */
  public int getTopParent()
  {
    return getStackParent(mUsedTop);
  }

  /**
   * Sets the the parent index of the top-most entry of the stack.
   * @param  index  The new parent index, including its {@link #MSB1}.
   */
  public void setTopIndex(final int index)
  {
    setStackIndex(mUsedTop, index);
  }


  //#########################################################################
  //# Link Access
  /**
   * Retrieves the link information associated with the given state index.
   */
  public int getLink(final int index)
  {
    return mLinks.get(index);
  }

  /**
   * Stores new link information associated with the given state index.
   */
  public void setLink(final int index, final int value)
  {
    mLinks.set(index, value);
  }

  /**
   * Clears the control stack and resets all link entries to -1.
   */
  public void resetLinks()
  {
    clearStack();
    for (int i = 0; i < mLinks.size(); i++) {
      mLinks.set(i, -1);
    }
  }


  //#########################################################################
  //# Indexing
  /**
   * Retrieves the stack link, which refers to the next entry below the
   * given stack entry.
   * @param  pos    The stack index of the entry to be checked.
   */
  private int getStackLink(final int pos)
  {
    return mStack.get(pos);
  }

  /**
   * Retrieves the state index of the given stack entry,
   * including its {@link #MSB1}.
   * @param  pos    The stack index of the entry to be checked.
   */
  private int getStackIndex(final int pos)
  {
    return mStack.get(pos + 1);
  }

  /**
   * Retrieves the parent index of the given stack entry.
   * @param  pos    The stack index of the entry to be checked.
   */
  private int getStackParent(final int pos)
  {
    return mStack.get(pos + 2);
  }

  /**
   * Sets the stack link, which refers to the next entry below the
   * given stack entry.
   * @param  pos    The stack index of the entry to be updated.
   * @param  value  The stack index of the new entry below.
   */
  private void setStackLink(final int pos, final int value)
  {
    mStack.set(pos, value);
  }

  /**
   * Sets the state index of the given stack entry.
   * @param  pos    The stack index of the entry to be updated.
   * @param  index  The new state index, including its {@link #MSB1}.
   */
  private void setStackIndex(final int pos, final int index)
  {
    mStack.set(pos + 1, index);
  }

  /**
   * Sets the parent index of the given stack entry.
   * @param  pos    The stack index of the entry to be updated.
   * @param  parent The new parent index.
   */
  private void setStackParent(final int pos, final int parent)
  {
    mStack.set(pos + 2, parent);
  }


  //#########################################################################
  //# Free Nodes
  /**
   * Resets the stack to be empty without changing the links.
   */
  private void clearStack()
  {
    mStack.clear();
    mStack.add(-1);
    mStack.add(-1);
    mStack.add(-1);
    mDummyTop = 0;
    mUsedTop = mNextFree = -1;
  }

  /**
   * Allocates a stack entry. This method either enlarges the stack or
   * updates and returns the next available entry from the list of free
   * entries.
   */
  private int allocateEntry()
  {
    if (mNextFree >= 0) {
      final int free = mNextFree;
      mNextFree = getStackLink(free);
      return free;
    } else {
      final int free = mStack.size();
      mStack.add(-1);
      mStack.add(-1);
      mStack.add(-1);
      return free;
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < mStack.size(); i+= 3) {
      if (first) {
        first = false;
      } else {
        builder.append('\n');
      }
      builder.append(i);
      builder.append(": (");
      final int value = mStack.get(i+1);
      if (value == -1) {
        builder.append(value);
      } else {
        builder.append(value & ~MSB1);
        if ((value & MSB1) != 0) {
          builder.append('*');
        }
      }
      builder.append(',');
      builder.append(mStack.get(i+2));
      builder.append(") -> ");
      builder.append(mStack.get(i));
      if (i == mDummyTop) {
        builder.append(" @dummy");
      } else if (i == mUsedTop) {
        builder.append(" @top");
      } else if (i == mNextFree) {
        builder.append(" @free");
      }
    }
    return builder.toString();
  }

  public void checkIntegrity()
  {
    for (int i = 0; i < mLinks.size(); i++) {
      final int link = mLinks.get(i);
      if ((link & MSB1) == 0) {
        final int stackPosAbove = link;
        final int stackPos = getStackLink(stackPosAbove);
        final int index = getStackIndex(stackPos);
        assert index == i;
      }
    }
  }


  //#########################################################################
  //# Instance Variables
  /**
   * Array containing the link information for each state.
   * If a state is on the control stack, the link points to the entry on
   * top of it on the control stack. This is used to facilitate the {@link
   * #moveToTop(int, int) moveToTop()} operation.
   * For states not on the control stack, the link can be used from outside
   * to store an arbitrary integer value, which is accessed by the
   * {@link #getLink(int) getLink()} and {@link #setLink(int, int) setLink()}
   * methods.
   */
  private final TIntArrayList mLinks;
  /**
   * Array list containing stack data.
   */
  private final TIntArrayList mStack;
  /**
   * A fake stack top entry. The dummy top is always defined and identifies
   * the entry to be created by a following {@link #push(int, int) push()}
   * operation. It can be used as the reference to the entry above any new
   * item pushed on the stack (which needs to be stored in {@link #mLinks
   * mLinks}). The stack link of <CODE>mDummyTop</CODE> always points to
   * {@link #mUsedTop}.
   */
  private int mDummyTop;
  /**
   * The top-most stack entry that holds data, or -1 if the stack is
   * empty.
   */
  private int mUsedTop;
  /**
   * The next available unused stack entry, or -1 if all stack entries are
   * in use. If a free entry is available, its stack link refers to the
   * next available free entry.
   */
  private int mNextFree;


  //#########################################################################
  //# Class Constants
  private static final int MSB1 = 0x80000000;

}
