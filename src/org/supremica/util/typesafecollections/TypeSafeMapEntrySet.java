
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.Set class which contains
 * java.util.Map.Entry objects which accept
 * objects of a given type for add operations only.
 * The Set and the stored Map.Entry objects are backed by other
 * implementations of the corresponding interfaces.
 */
class TypeSafeMapEntrySet
	extends TypeSafeSet
{
	private Set _set;
	private Class _type;

	public TypeSafeMapEntrySet(Set set, Class type)
	{
		super(set, type);

		_set = set;
		_type = type;
	}

	/**
	 * Inner class which implements java.util.Map.Entry. Accepts only
	 * objects of a given type for the setValue() method. It backed by
	 * another implementation of Map.Entry
	 */
	private class TypeSafeMapEntry
		implements Map.Entry
	{
		private Map.Entry _entry;

		/**
		 * Contructs a new TypeSafeMapEntry object.
		 * @param entry the Map.Entry object to be wrapped
		 */
		private TypeSafeMapEntry(Map.Entry entry)
		{
			_entry = entry;
		}

		/**
		 * see java.util.Map.Entry
		 */
		public boolean equals(Object o)
		{
			return _entry.equals(o);
		}

		/**
		 * see java.util.Map.Entry
		 */
		public Object getKey()
		{
			return _entry.getKey();
		}

		/**
		 * see java.util.Map.Entry
		 */
		public Object getValue()
		{
			return _entry.getValue();
		}

		/**
		 * see java.util.Map.Entry
		 */
		public int hashCode()
		{
			return _entry.hashCode();
		}

		/**
		 * See java.util.Map.Entry
		 * Checks the type of the value object
		 */
		public Object setValue(Object value)
		{
			checkType(value);

			return _entry.setValue(value);
		}
	}

	/**
	 * Returns a new type safe java.util.Map.Entry wrapper.
	 * @param entry the Map.Entry to be wrapped
	 * @return The new Map.Entry wrapper
	 */
	protected Map.Entry typeSafeMapEntry(Map.Entry entry)
	{
		return new TypeSafeMapEntry(entry);
	}

	/**
	 * Inner class which implements java.util.Iterator. Wraps all returned
	 * Map.Entry objects by with a type safe Map.Entry wrapper.
	 * Is backed by another implementation of Iterator.
	 */
	private class TypeSafeMapEntryIterator
		implements Iterator
	{
		private Iterator _it;

		private TypeSafeMapEntryIterator(Iterator it)
		{
			_it = it;
		}

		/**
		 * see java.util.Iterator
		 */
		public boolean hasNext()
		{
			return _it.hasNext();
		}

		/**
		 * See java.util.Iterator
		 * Wraps the original Map.Entry with a type safe Map.Entry wrapper
		 */
		public Object next()
		{
			return typeSafeMapEntry((Map.Entry) _it.next());
		}

		/**
		 * See java.util.Iterator
		 */
		public void remove()
		{
			_it.remove();
		}
	}

	/**
	 * See java.util.Set
	 * Returns an java.util.Iterator which gives access to
	 * type safe Map.Entry objects only.
	 */
	public Iterator iterator()
	{
		return new TypeSafeMapEntryIterator(_set.iterator());
	}

	/**
	 * See java.util.Set
	 * Returns an array of Object which contains
	 * type safe Map.Entry objects only.
	 */
	public Object[] toArray()
	{
		Object[] old = _set.toArray();
		Object[] result = new Object[old.length];

		for (int i = 0; i < old.length; i++)
		{
			result[i] = typeSafeMapEntry((Map.Entry) old[i]);
		}

		return result;
	}

	/**
	 * See java.util.Set
	 * Returns an array of Object which contains
	 * type safe Map.Entry objects only.
	 */
	public Object[] toArray(Object[] a)
	{
		Object[] result = _set.toArray(a);

		for (int i = 0; i < result.length; i++)
		{
			result[i] = typeSafeMapEntry((Map.Entry) result[i]);
		}

		return result;
	}

	/**
	 * See java.util.Set
	 * Doesn't check the type of the object!
	 */
	public boolean add(Object o)
	{
		return _set.add(o);
	}

	/**
	 * See java.util.Set
	 * Doesn't check the type of the object!
	 */
	public boolean addAll(Collection c)
	{
		return _set.addAll(c);
	}
}
