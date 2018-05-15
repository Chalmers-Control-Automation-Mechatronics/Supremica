//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

public class MemorySlab
{
  public MemorySlab()
  {
    //Initialise
    mCurrentChunk = -1;
    addChunk();
  }

  /**
   * Adds a chunk and resets the chunk position.
   */
  private void addChunk()
  {
    mChunks[++mCurrentChunk] = new int[CHUNK_SIZE];
    mChunkPos = 0;
  }

  /**
   * Allocates space and stores data into the slab, expanding it if necessary.
   */
  public int allocate(int[] data)
  {
    assert(data.length < CHUNK_SIZE);

    if (mChunkPos + data.length > CHUNK_SIZE)
      addChunk();

    System.arraycopy(data, 0, mChunks[mCurrentChunk], mChunkPos, data.length);

    int ptr = mCurrentChunk << CHUNK_BITS | mChunkPos;
    mChunkPos += data.length;

    return ptr;
  }

  /**
   * Copies data out of the memory slab into a temporary buffer.
   */
  public void retrieve(int ptr, int[] buffer, int length)
  {
    final int chunk = ptr >>> CHUNK_BITS;
    final int pos = ptr & CHUNK_MASK;
    System.arraycopy(mChunks[chunk], pos, buffer, 0, length);
  }

  /**
   * Reads a single word from the chunk.
   */
  public int read(int ptr)
  {
    final int chunk = ptr >>> CHUNK_BITS;
    final int pos = ptr & CHUNK_MASK;
    return mChunks[chunk][pos];
  }

  /**
   * Gets direct access to a chunk array corresponding to a pointer.
   * @param ptr pointer to get chunk for
   * @return the underlying chunk array for direct access.
   */
  public int[] getChunk(int ptr)
  {
    return mChunks[ptr >>> CHUNK_BITS];
  }

  /**
   * Returns the position within the chunk that this pointer
   * corresponds to.
   * @param ptr Pointer
   * @return the position within the chunk that this pointer
   * corresponds to.
   */
  public int getChunkPosition(int ptr)
  {
    return ptr & CHUNK_MASK;
  }

  //Number of bits to use in the 
  private static final int CHUNK_BITS = 22;
  private static final int CHUNK_MASK = ~0 >>> (32 - CHUNK_BITS);
  private static final int CHUNK_SIZE = 1 << CHUNK_BITS;
  private static final int NUM_CHUNKS = 1 << (32 - CHUNK_BITS);

  private int mCurrentChunk;
  private int mChunkPos;
  private final int[][] mChunks = new int[NUM_CHUNKS][];
}
