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
    mHash = new IntHashData(initialcap, new StateTupleHashStrategy());
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
   * @return 
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

  private class StateTupleHashStrategy implements HashStrategy
  {
    public boolean equal(Object obj, int x)
    {
      StateTuple state = (StateTuple)obj;
      int[] data = new int[mStateLength];
      mData.retrieve(x, data, mStateLength);
      return java.util.Arrays.equals(data, state.getStateArray());
    }

    public boolean equalIndirect(int x, int y)
    {
      int[] datax = new int[mStateLength];
      int[] datay = new int[mStateLength];
      mData.retrieve(x, datax, mStateLength);
      mData.retrieve(y, datay, mStateLength);
      return java.util.Arrays.equals(datax, datay);
    }

    public int computeHash(Object obj)
    {
      StateTuple state = (StateTuple)obj;
      return java.util.Arrays.hashCode(state.getStateArray());
    }

    public int computeIndirectHash(int ptr)
    {
      int[] data = new int[mStateLength];
      mData.retrieve(ptr, data, mStateLength);
      return java.util.Arrays.hashCode(data);
    }
  }


  private int mSize = 0;
  private final float mLoadFactor = 0.5f;

  private final int mStateLength;
  private final IntHashData mHash;
  private final MemorySlab mData;
}