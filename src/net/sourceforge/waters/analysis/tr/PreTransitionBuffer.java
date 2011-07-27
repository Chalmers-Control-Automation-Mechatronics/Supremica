//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   PreTransitionBuffer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.TIntArrayList;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * A buffer to hold transitions temporarily, so they later can be be added
 * efficiently to a {@link ListBufferTransitionRelation}.
 *
 * @author Robi Malik
 */

public class PreTransitionBuffer
{

  //#########################################################################
  //# Constructors
  public PreTransitionBuffer(final int numEvents)
  {
    mStateShift = AutomatonTools.log2(numEvents);
    mEventMask = (1 << mStateShift) - 1;
    mBlocks = new ArrayList<int[]>();
    mCurrentState = mCurrentEvent = mCurrentOffset = -1;
    mCurrentBlock = null;
    mNumTransitions = mCurrentFanout = mMaxFanout = 0;
  }


  //#########################################################################
  //# Recording Transitions
  public void addTransition(final int from, final int event, final int to)
  {
    if (mCurrentState != from || mCurrentEvent != event) {
      close();
      final int key = (from << mStateShift) | event;
      append(key);
      mCurrentState = from;
      mCurrentEvent = event;
      mCurrentFanout = 1;
    } else {
      mCurrentFanout++;
    }
    append(to);
    mNumTransitions++;
  }

  /**
   * Gets the number of transitions added to this buffer.
   */
  public int size()
  {
    return mNumTransitions;
  }


  //#########################################################################
  //# Storing Transitions
  public void addOutgoingTransitions(final ListBufferTransitionRelation rel)
  {
    close();
    final TIntArrayList states = new TIntArrayList(mMaxFanout);
    final int size = (mBlocks.size() - 1) * BLOCK_SIZE + mCurrentOffset + 1;
    int[] block = null;
    int blockno = 0;
    int offset = BLOCK_SIZE;
    int pos = 0;
    while (pos < size) {
      if (offset >= BLOCK_SIZE) {
        block = mBlocks.get(blockno++);
        offset = 0;
      }
      final int key = block[offset++];
      int state;
      do {
        if (offset >= BLOCK_SIZE) {
          block = mBlocks.get(blockno++);
          offset = 0;
        }
        state = block[offset++];
        states.add(state & ~TAG_END);
      } while ((state & TAG_END) == 0);
      final int from = key >>> mStateShift;
      final int event = key & mEventMask;
      rel.addTransitions(from, event, states);
      pos += 1 + states.size();
      states.clear();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void append(final int data)
  {
    if (mCurrentBlock == null) {
      mCurrentBlock = new int[BLOCK_SIZE];
      mBlocks.add(mCurrentBlock);
      mCurrentOffset = 0;
    } else if (++mCurrentOffset >= BLOCK_SIZE) {
      mCurrentBlock = new int[BLOCK_SIZE];
      mBlocks.add(mCurrentBlock);
      mCurrentOffset = 0;
    }
    mCurrentBlock[mCurrentOffset] = data;
  }

  private void close()
  {
    if (mCurrentBlock != null) {
      mCurrentBlock[mCurrentOffset] |= TAG_END;
      if (mCurrentFanout > mMaxFanout) {
        mMaxFanout = mCurrentFanout;
      }
    }
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter printer = new PrintWriter(writer);
    dump(printer);
    return writer.toString();
  }

  public void dump(final PrintWriter printer)
  {
    final int size = (mBlocks.size() - 1) * BLOCK_SIZE + mCurrentOffset + 1;
    if (size <= 0) {
      printer.print("<empty>");
    } else {
      int[] block = null;
      int blockno = 0;
      int offset = BLOCK_SIZE;
      int pos = 0;
      WritePos wpos = WritePos.NEW_LIST;
      while (pos < size) {
        if (offset >= BLOCK_SIZE) {
          block = mBlocks.get(blockno++);
          offset = 0;
        }
        final int key = block[offset++];
        final int from = key >>> mStateShift;
        final int event = key & mEventMask;
        int state;
        do {
          if (offset >= BLOCK_SIZE) {
            block = mBlocks.get(blockno++);
            offset = 0;
          }
          state = block[offset++];
          switch (wpos) {
          case NEW_LINE:
            printer.println();
            // fall through ...
          case NEW_LIST:
            wpos = WritePos.INSIDE;
            break;
          default:
            printer.print(", ");
            break;
          }
          printer.print(from);
          printer.print(" -");
          printer.print(event);
          printer.print("-> ");
          printer.print(state & ~TAG_END);
          pos++;
        } while (pos < size && (state & TAG_END) == 0);
        wpos = WritePos.NEW_LINE;
      }
    }
  }

  private enum WritePos {
    NEW_LIST,
    NEW_LINE,
    INSIDE
  }


  //#########################################################################
  //# Data Members
  private final int mStateShift;
  private final int mEventMask;
  private final List<int[]> mBlocks;

  private int mNumTransitions;
  private int mCurrentState;
  private int mCurrentEvent;
  private int[] mCurrentBlock;
  private int mCurrentOffset;
  private int mCurrentFanout;
  private int mMaxFanout;

  //#########################################################################
  //# Class Constants
  private static final int BLOCK_SIZE = 1024;
  private static final int TAG_END = 0x80000000;

}
