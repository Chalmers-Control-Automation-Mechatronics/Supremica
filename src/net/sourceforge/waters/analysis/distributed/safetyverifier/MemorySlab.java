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
   * Allocate space and store data into the slab, expanding it if necessary.
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
   * Copy data out of the memory slab into a temporary buffer.
   */
  public void retrieve(int ptr, int[] buffer, int length)
  {
    final int chunk = ptr >>> CHUNK_BITS;
    final int pos = ptr & CHUNK_MASK;
    System.arraycopy(mChunks[chunk], pos, buffer, 0, length);
  }

  public int read(int ptr)
  {
    final int chunk = ptr >>> CHUNK_BITS;
    final int pos = ptr & CHUNK_MASK;
    return mChunks[chunk][pos];
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