
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.SortedMap class which accepts
 * only key and value objects of a given type for put operations.
 * Is backed by another implementation of SortedMap.
 */
class TypeSafeSortedMap
	extends TypeSafeMap
	implements SortedMap
{
	private SortedMap _map;

	/**
	 * Constructs a new TypeSafeSortedMap object.
	 * @param map the map to be wrapped
	 * @param keyType the type of the objects which can be used as keys
	 * @param valueType the type of the objects which can be added as values
	 */
	public TypeSafeSortedMap(SortedMap map, Class keyType, Class valueType)
	{
		super(map, keyType, valueType);

		_map = map;
	}

	/**
	 * see java.util.SortedMap
	 */
	public Comparator comparator()
	{
		return _map.comparator();
	}

	/**
	 * see java.util.SortedMap
	 */
	public Object firstKey()
	{
		return _map.firstKey();
	}

	/**
	 * see java.util.SortedMap
	 */
	public Object lastKey()
	{
		return _map.lastKey();
	}

	/**
	 * See java.util.SortedMap
	 * Returns a type safe java.util.SortedMap
	 */
	public SortedMap headMap(Object toKey)
	{
		return new TypeSafeSortedMap(_map.headMap(toKey), getKeyType(), getValueType());
	}

	/**
	 * See java.util.SortedMap
	 * Returns a type safe java.util.SortedMap
	 */
	public SortedMap subMap(Object fromKey, Object toKey)
	{
		return new TypeSafeSortedMap(_map.subMap(fromKey, toKey), getKeyType(), getValueType());
	}

	/**
	 * See java.util.SortedMap
	 * Returns a type safe java.util.SortedMap
	 */
	public SortedMap tailMap(Object fromKey)
	{
		return new TypeSafeSortedMap(_map.tailMap(fromKey), getKeyType(), getValueType());
	}
}
