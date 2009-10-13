package net.sourceforge.waters.analysis.distributed.safetyverifier;

/**
 * A fixed size hashtable with no collision support. Used to detect
 * If an object is in the cache, but where it doesn't need to behave
 * as a proper hashmap.
 */
class HashCache
{
  public HashCache(int capacity)
  {
    mObjects = new Object[capacity];
  }

  private static int rehash(int h)
  {
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
  }

  public void put(Object o)
  {
    int hc = rehash(o.hashCode());
    mObjects[Math.abs(hc % mObjects.length)] = o;
  }
  
  public boolean contains(Object o)
  {
    int hc = rehash(o.hashCode());
    Object cached = mObjects[Math.abs(hc % mObjects.length)];
    return cached != null && o.equals(cached);
  }
  
  public void invalidate()
  {
    for (int i = 0; i < mObjects.length; i++)
      {
	mObjects[i] = null;
      }
  }

  private final Object[] mObjects;
}