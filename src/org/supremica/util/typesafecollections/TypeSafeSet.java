
/**
 * (c) Piet Jonas, 2001
 * @author Piet Jonas
 * @version 1.0
 */
package org.supremica.util.typesafecollections;

import java.util.*;

/**
 * Implementation of the java.util.Set class which accepts
 * only objects of a given type for add operations. Is backed by
 * another implementation of Set.
 */
class TypeSafeSet
	extends TypeSafeCollection
	implements Set
{
	private Set _set;

	/**
	 * Constructs a new TypeSafeSet object.
	 * @param set the Set to be wrapped
	 * @param type the type of the objects which can be added
	 */
	public TypeSafeSet(Set set, Class type)
	{
		super(set, type);

		_set = set;
	}
}
