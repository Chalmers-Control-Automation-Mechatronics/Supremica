
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.Collection class which accepts
 * only objects of a given type for add operations. Is backed by
 * another implementation of Collection.
 */
class TypeSafeCollection
	implements Collection
{
	private Collection _collection;
	private Class _type;

	/**
	 * Constructs a new TypeSafeCollection object.
	 * @param collection the Collection to be wrapped
	 * @param type the type of the objects which can be added
	 */
	public TypeSafeCollection(Collection collection, Class type)
	{
		_collection = collection;
		_type = type;
	}

	/**
	 * Tests whether the an object is an instance of the allowed type.
	 * @param o The object to be checked
	 * @throws ClassCastException if the object is not of the right type
	 */
	protected void checkType(Object o)
	{
		if (!_type.isInstance(o))
		{
			throw new ClassCastException(_type.getName() + " expected but " + o.getClass().getName() + " found");
		}
	}

	/**
	 * Returns the type of objects which can be added to this collection
	 */
	protected Class getType()
	{
		return _type;
	}

	/**
	 * see java.util.Collection
	 */
	public int size()
	{
		return _collection.size();
	}

	/**
	 * see java.util.Collection
	 */
	public boolean isEmpty()
	{
		return _collection.isEmpty();
	}

	/**
	 * see java.util.Collection
	 */
	public boolean contains(Object o)
	{
		return _collection.contains(o);
	}

	/**
	 * see java.util.Collection
	 */
	public Iterator iterator()
	{
		return _collection.iterator();
	}

	/**
	 * see java.util.Collection
	 */
	public Object[] toArray()
	{
		return _collection.toArray();
	}

	/**
	 * see java.util.Collection
	 */
	public Object[] toArray(Object[] a)
	{
		return _collection.toArray(a);
	}

	/**
	 * see java.util.Collection
	 * Checks the type of the object.
	 */
	public boolean add(Object o)
	{
		checkType(o);

		return _collection.add(o);
	}

	/**
	 * see java.util.Collection
	 */
	public boolean remove(Object o)
	{
		return _collection.remove(o);
	}

	/**
	 * see java.util.Collection
	 */
	public boolean containsAll(Collection c)
	{
		return _collection.containsAll(c);
	}

	/**
	 * see java.util.Collection
	 * Checks the type of the objects in the Collection.
	 */
	public boolean addAll(Collection c)
	{
		Iterator it = c.iterator();

		while (it.hasNext())
		{
			checkType(it.next());
		}

		return _collection.addAll(c);
	}

	/**
	 * see java.util.Collection
	 */
	public boolean removeAll(Collection c)
	{
		return _collection.removeAll(c);
	}

	/**
	 * see java.util.Collection
	 */
	public boolean retainAll(Collection c)
	{
		return _collection.retainAll(c);
	}

	/**
	 * see java.util.Collection
	 */
	public void clear()
	{
		_collection.clear();
	}

	/**
	 * see java.util.Collection
	 */
	public boolean equals(Object o)
	{
		return _collection.equals(o);
	}
}
