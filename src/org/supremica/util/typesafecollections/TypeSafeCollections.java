
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * This class contains a Collection of static methods which return
 * a wrapper to an existing container interface of the
 * Java Collection Framework. The wrappers make sure, that only objects
 * of a given type can be added to the container.
 */
public class TypeSafeCollections
{

	/**
	 * Returns a wrapper for a type safe java.util.Collection.
	 * @param collection The java.util.Collection object to be wrapped
	 * @param type The type of the objects you can add
	 * @return The type safe Collection
	 */
	public static Collection typeSafeCollection(Collection collection, Class type)
	{
		return new TypeSafeCollection(collection, type);
	}

	/**
	 * Returns a wrapper for a type safe java.util.List.
	 * @param list The java.util.List object to be wrapped
	 * @param type The type of the objects you can add
	 * @return The type safe java.util.List
	 */
	public static List typeSafeList(List list, Class type)
	{
		return new TypeSafeList(list, type);
	}

	/**
	 * Returns a wrapper for a type safe java.util.Set.
	 * @param set The java.util.Set object to be wrapped
	 * @param type The type of the objects you can add
	 * @return The type safe java.util.Set
	 */
	public static Set typeSafeSet(Set set, Class type)
	{
		return new TypeSafeSet(set, type);
	}

	/**
	 * Returns a wrapper for a type safe java.util.SortedSet.
	 * @param set The java.util.SortedSet object to be wrapped
	 * @param type The type of the objects you can add
	 * @return The type safe java.util.SortedSet
	 */
	public static SortedSet typeSafeSortedSet(SortedSet set, Class type)
	{
		return new TypeSafeSortedSet(set, type);
	}

	/**
	 * Returns a wrapper for a type safe java.util.Map.
	 * @param map The java.util.Map object to be wrapped
	 * @param keyType The type of the objects you use as keys
	 * @param valueType The type of the objects you add as values
	 * @return The type safe java.util.Map
	 */
	public static Map typeSafeMap(Map map, Class keyType, Class valueType)
	{
		return new TypeSafeMap(map, keyType, valueType);
	}

	/**
	 * Returns a wrapper for a type safe java.util.SortedMap.
	 * @param map The java.util.SortedMap object to be wrapped
	 * @param keyType The type of the objects you use as keys
	 * @param valueType The type of the objects you add as values
	 * @return The type safe java.util.SortedMap
	 */
	public static Map typeSafeSortedMap(SortedMap map, Class keyType, Class valueType)
	{
		return new TypeSafeSortedMap(map, keyType, valueType);
	}
}
