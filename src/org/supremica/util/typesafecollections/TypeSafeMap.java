
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.Map class which accepts
 * only key and value objects of a given type for put operations.
 * Is backed by another implementation of Map.
 */
class TypeSafeMap
	implements Map
{
	private Map _map;
	private Class _keytype;
	private Class _valuetype;

	/**
	 * Constructs a new TypeSafeMap object.
	 * @param map the map to be wrapped
	 * @param keyType the type of the objects which can be used as keys
	 * @param valueType the type of the objects which can be added as values
	 */
	public TypeSafeMap(Map map, Class keyType, Class valueType)
	{
		_map = map;
		_keytype = keyType;
		_valuetype = valueType;
	}

	/**
	 * Tests whether an object is an instance of the allowed type.
	 * @param o The object to be checked
	 * @throws ClassCastException if the object is not of the right type
	 */
	protected void checkType(Object o, Class type)
	{
		if (!type.isInstance(o))
		{
			throw new ClassCastException(type.getName() + " expected but " + o.getClass().getName() + " found");
		}
	}

	/**
	 * Tests whether a key object is an instance of the allowed type.
	 * @param o The key object to be checked
	 * @throws ClassCastException if the object is not of the right type
	 */
	protected void checkKeyType(Object o)
	{
		checkType(o, _keytype);
	}

	/**
	 * Tests whether a value object is an instance of the allowed type.
	 * @param o The value object to be checked
	 * @throws ClassCastException if the object is not of the right type
	 */
	protected void checkValueType(Object o)
	{
		checkType(o, _valuetype);
	}

	/**
	 * Returns the type of the keys.
	 * @return The class descriptor for the accepted key objects
	 */
	protected Class getKeyType()
	{
		return _keytype;
	}

	/**
	 * Returns the type of the values.
	 * @return The class descriptor for the accepted value objects
	 */
	protected Class getValueType()
	{
		return _valuetype;
	}

	/**
	 * see java.util.Map
	 */
	public int size()
	{
		return _map.size();
	}

	/**
	 * see java.util.Map
	 */
	public boolean isEmpty()
	{
		return _map.isEmpty();
	}

	/**
	 * see java.util.Map
	 */
	public boolean containsKey(Object key)
	{
		return _map.containsKey(key);
	}

	/**
	 * see java.util.Map
	 */
	public boolean containsValue(Object value)
	{
		return _map.containsValue(value);
	}

	/**
	 * see java.util.Map
	 */
	public Object get(Object key)
	{
		return _map.get(key);
	}

	/**
	 * see java.util.Map
	 * Checks the types of the key and value objects
	 */
	public Object put(Object key, Object value)
	{
		checkKeyType(key);
		checkValueType(value);

		return _map.put(key, value);
	}

	/**
	 * see java.util.Map
	 */
	public Object remove(Object key)
	{
		return _map.remove(key);
	}

	/**
	 * see java.util.Map
	 * Checks the types of the key and value objects in the Map
	 */
	public void putAll(Map t)
	{
		Iterator it = t.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry entry = (Map.Entry) it.next();

			checkKeyType(entry.getKey());
			checkValueType(entry.getValue());
		}

		_map.putAll(t);
	}

	/**
	 * see java.util.Map
	 */
	public void clear()
	{
		_map.clear();
	}

	/**
	 * see java.util.Map
	 */
	public Set keySet()
	{
		return _map.keySet();
	}

	/**
	 * see java.util.Map
	 */
	public Collection values()
	{
		return _map.values();
	}

	/**
	 * see java.util.Map
	 * Returns a type safe java.util.Set which prevents values of the
	 * wrong type to be set via the stored java.util.Map.Entry object.
	 */
	public Set entrySet()
	{
		return new TypeSafeMapEntrySet(_map.entrySet(), getValueType());
	}

	/**
	 * see java.util.Map
	 */
	public boolean equals(Object o)
	{
		return _map.equals(o);
	}
}
