package net.sourceforge.waters.analysis.distributed.safetyverifier;

import net.sourceforge.waters.analysis.distributed.hashkit.*;

/**
 * A set of state tuples. This stores the data into big arrays, so
 * should avoid a lot of the overhead that comes from storing states
 * in objects.
 *
 * When a state is added, a pointer is returned that can be used to
 * look up the state later. This means that it can be queued more
 * efficiently and then later retrieved.
 *
 * This set doesn't support removals. All states are assumed to have
 * the same lifetime as the set. This makes the memory allocation more
 * efficient.
 *
 * @author Sam Douglas
 */
public class StateTupleSet
{
  public StateTupleSet(int statelength, int initialcapacity)
  {
    int initialcap = PrimeFinder.nextPrime(initialcapacity);
    mStateLength = statelength;

    mStrategy = new DirectHashStrategy();

    mHash = new IntHashData(initialcap, mStrategy);
    mData = new MemorySlab();
  }


  /**
   * Add a state to the set and return a pointer to it for later
   * reference. If the state already exists in the set, the return
   * value will be <code>-index -1</code>. This means that you can
   * check if a state is already in the set and add it if not in a
   * single operation. Adding a unique state will return the positive
   * index that can be used to access the state later.
   * @param state to add to the set.
   */
  public int add(StateTuple state)
  {
    final int size = mSize;
    final int capacity = mHash.getCapacity();

    if (size / (float)capacity > mLoadFactor)
      {
	final int newcap = PrimeFinder.nextPrime(capacity * 2);
	mHash.rehash(newcap);
      }

    final int index = mHash.getInsertionIndex(state);
    if (index >= 0)
      {
	//State tuple is not in the set, add it.
	final int[] data = new int[mStateLength + 1];
	System.arraycopy(state.getStateArray(), 0, data, 0, mStateLength);
	data[mStateLength] = state.getDepthHint();
	final int ptr = mData.allocate(data);
	mSize++;
	mHash.store(index, ptr);

	return ptr;
      }
    else
      {
	//Item is already in the set. Retrieve the pointer
	//and return it negated.
	final int rindex = -(index + 1);
	return -mHash.getData(rindex) - 1;
      }
  }


  /**
   * Checks if the set contains a given state.
   * @param state to search for.
   * @return true if state is in the set.
   */
  public boolean contains(StateTuple state)
  {
    return mHash.index(state) >= 0;
  }

  /**
   * Gets the state tuple associated with the pointer.  This will
   * create a new state tuple object, so reference equality is not at
   * all preserved.
   * @param ptr Pointer to the state to get.
   * @return The extracted state tuple
   */
  public StateTuple get(int ptr)
  {
    int[] statedata = new int[mStateLength];
    mData.retrieve(ptr, statedata, mStateLength);
    int depth = mData.read(ptr + mStateLength);
    return new StateTuple(statedata, depth);
  }

  /**
   * Gets a pointer to a stored state tuple if it exists, or -1
   * if it is not in the set.
   * @param state to lookup pointer for.
   * @return Pointer to state, or -1 if not present.
   */
  public int lookup(StateTuple state)
  {
    final int index = mHash.index(state);
    if (index < 0)
      return -1;
    else
      {
	return mHash.getData(index);
      }
  }

  /**
   * Get the depth for a state, given a pointer to the tuple.
   * @param ptr the state to get the depth for
   * @return The depth for the state
   */
  public int getDepth(int ptr)
  {
    return mData.read(ptr + mStateLength);
  }


  /**
   * Gets the number of items in the set.
   * @return the number of items in the set.
   */
  public int size()
  {
    return mSize;
  }

  private class DirectHashStrategy implements HashStrategy
  {
    public boolean equal(Object obj, int x)
    {
      final int[] chunk = mData.getChunk(x);
      final int pos = mData.getChunkPosition(x);
      final int[] state = ((StateTuple)obj).getStateArray();

      for (int i = 0; i < mStateLength; i++)
	{
	  if (state[i] != chunk[pos + i])
	    return false;
	}

      return true;
    }

    public boolean equalIndirect(int x, int y)
    {
      final int[] chunkx = mData.getChunk(x);
      final int[] chunky = mData.getChunk(y);
      int posx = mData.getChunkPosition(x);
      int posy = mData.getChunkPosition(y);
      final int posx_end = posx + mStateLength;

      while (posx < posx_end)
	{
	  if (chunkx[posx] != chunky[posy])
	    return false;
	  posx++;
	  posy++;
	}
      return true;
    }

    public int computeHash(Object obj)
    {
      StateTuple state = (StateTuple)obj;
      return computeHash_sdbm(state.getStateArray(), 0, mStateLength);
    }

    public int computeIndirectHash(int ptr)
    {
      final int[] chunk = mData.getChunk(ptr);
      final int pos = mData.getChunkPosition(ptr);
      return computeHash_sdbm(chunk, pos, mStateLength);
    }

    @SuppressWarnings("unused")
    private int computeHash_java(final int[] ar, final int start, final int length)
    {
      final int end = start + length;
      int hash = 1;
      for (int i = start; i < end; i++)
	{
	  hash = 31 * hash + ar[i];
	}

      return hash;
    }

    @SuppressWarnings("unused")
    private int computeHash_bernstein(final int[] ar, int start, final int length)
    {
      final int end = start + length;
      int hash = 5381;

      while (start < end)
	{
	  hash = ((hash << 5) + hash) + ar[start];
	  start++;
	}

      return hash;
    }

    private int computeHash_sdbm(final int[] ar, int start, final int length)
    {
      final int end = start + length;
      int hash = 0;

      while (start < end)
	{
	  hash = ar[start] + (hash << 6) + (hash << 16) - hash;
	  start++;
	}

      return hash;
    }
  }


  private int mSize = 0;
  private final float mLoadFactor = 0.5f;

  private final HashStrategy mStrategy;

  private final int mStateLength;
  private final IntHashData mHash;
  private final MemorySlab mData;
}