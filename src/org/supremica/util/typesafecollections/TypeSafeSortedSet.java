
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.SortedSet class which accepts
 * only objects of a given type for add operations. Is backed by
 * another implementation of SortedSet.
 */
class TypeSafeSortedSet
	extends TypeSafeSet
	implements SortedSet
{
	private SortedSet _set;

	/**
	 * Constructs a new TypeSafeSortedSet object.
	 * @param set the SortedSet to be wrapped
	 * @param type the type of the objects which can be added
	 */
	public TypeSafeSortedSet(SortedSet set, Class type)
	{
		super(set, type);

		_set = set;
	}

	/**
	 * see java.util.SortedSet
	 */
	public Comparator comparator()
	{
		return _set.comparator();
	}

	/**
	 * see java.util.SortedSet
	 */
	public Object first()
	{
		return _set.first();
	}

	/**
	 * see java.util.SortedSet
	 */
	public Object last()
	{
		return _set.last();
	}

	/**
	 * See java.util.SortedSet
	 * Returns a type safe java.util.SortedSet
	 */
	public SortedSet headSet(Object toElement)
	{
		return new TypeSafeSortedSet(_set.headSet(toElement), getType());
	}

	/**
	 * See java.util.SortedSet
	 * Returns a type safe java.util.SortedSet
	 */
	public SortedSet subSet(Object fromElement, Object toElement)
	{
		return new TypeSafeSortedSet(_set.subSet(fromElement, toElement), getType());
	}

	/**
	 * See java.util.SortedSet
	 * Returns a type safe java.util.SortedSet
	 */
	public SortedSet tailSet(Object fromElement)
	{
		return new TypeSafeSortedSet(_set.tailSet(fromElement), getType());
	}
}
