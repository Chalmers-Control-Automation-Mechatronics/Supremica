
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.List class which accepts
 * only objects of a given type for add operations. Is backed by
 * another implementation of List.
 */
class TypeSafeList
	extends TypeSafeCollection
	implements List
{
	private List _list;

	/**
	 * Constructs a new TypeSafeList object.
	 * @param list the List to be wrapped
	 * @param type the type of the objects which can be added
	 */
	public TypeSafeList(List list, Class type)
	{
		super(list, type);

		_list = list;
	}

	/**
	 * see java.util.List
	 * Checks the type of the objects in the Collection.
	 * @throws ClassCastException if the type of an object doesn't match
	 */
	public boolean addAll(int index, Collection c)
	{
		Iterator it = c.iterator();

		while (it.hasNext())
		{
			checkType(it.next());
		}

		return _list.addAll(index, c);
	}

	/**
	 * see java.util.List
	 */
	public Object get(int index)
	{
		return _list.get(index);
	}

	/**
	 * See java.util.List.
	 * Checks the type of the object
	 * @throws ClassCastException if the type of the object doesn't match
	 */
	public Object set(int index, Object element)
	{
		checkType(element);

		return _list.set(index, element);
	}

	/**
	 * See java.util.List.
	 * Checks the type of the object
	 * @throws ClassCastException if the type of the object doesn't match
	 */
	public void add(int index, Object element)
	{
		checkType(element);
		_list.add(index, element);
	}

	/**
	 * see java.util.List
	 */
	public Object remove(int index)
	{
		return _list.remove(index);
	}

	/**
	 * see java.util.List
	 */
	public int indexOf(Object o)
	{
		return _list.indexOf(o);
	}

	/**
	 * see java.util.List
	 */
	public int lastIndexOf(Object o)
	{
		return _list.lastIndexOf(o);
	}

	/**
	 * Inner class which implements the java.util.ListIterator
	 * interface. Checks the objects which are added by this ListIterator.
	 * Is backed another implementation of ListIterator.
	 */
	private class TypeSafeListIterator
		implements ListIterator
	{
		private ListIterator _iterator;

		/**
		 * @param iterator The ListIterator to be wrapped
		 */
		private TypeSafeListIterator(ListIterator iterator)
		{
			_iterator = iterator;
		}

		/**
		 * see java.util.ListIterator
		 */
		public boolean hasNext()
		{
			return _iterator.hasNext();
		}

		/**
		 * see java.util.ListIterator
		 */
		public Object next()
		{
			return _iterator.next();
		}

		/**
		 * see java.util.ListIterator
		 */
		public boolean hasPrevious()
		{
			return _iterator.hasPrevious();
		}

		/**
		 * see java.util.ListIterator
		 */
		public Object previous()
		{
			return _iterator.previous();
		}

		/**
		 * see java.util.ListIterator
		 */
		public int nextIndex()
		{
			return _iterator.nextIndex();
		}

		/**
		 * see java.util.ListIterator
		 */
		public int previousIndex()
		{
			return _iterator.previousIndex();
		}

		/**
		 * see java.util.ListIterator
		 */
		public void remove()
		{
			_iterator.remove();
		}

		/**
		 * See java.util.ListIterator
		 * Checks the type of the object.
		 * @throws ClassCastException if the type of the object doesn't match
		 */
		public void set(Object o)
		{
			checkType(o);
			_iterator.set(o);
		}

		/**
		 * See java.util.ListIterator
		 * Checks the type of the object.
		 * @throws ClassCastException if the type of the object doesn't match
		 */
		public void add(Object o)
		{
			checkType(o);
			_iterator.add(o);
		}
	}

	/**
	 * see java.util.List
	 * Returns a type safe java.util.ListIterator
	 */
	public ListIterator listIterator()
	{
		return new TypeSafeListIterator(_list.listIterator());
	}

	/**
	 * see java.util.List
	 * Returns a type safe java.util.ListIterator
	 */
	public ListIterator listIterator(int index)
	{
		return new TypeSafeListIterator(_list.listIterator(index));
	}

	/**
	 * see java.util.List
	 * Returns a type safe java.util.List
	 */
	public List subList(int fromIndex, int toIndex)
	{
		return TypeSafeCollections.typeSafeList(_list.subList(fromIndex, toIndex), getType());
	}
}
