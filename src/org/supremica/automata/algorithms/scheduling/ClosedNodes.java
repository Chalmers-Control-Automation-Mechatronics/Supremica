package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Hashtable;


@SuppressWarnings("unchecked")
public class ClosedNodes extends Hashtable<Object,Object> {

	private static final long serialVersionUID = 1L;
	public static final int CLOSED_NODE_INFO_SIZE = 2;

    @SuppressWarnings("unused")
	private final int compositeStateSize;

    public ClosedNodes(final int compositeStateSize) {
		super();
		this.compositeStateSize = compositeStateSize;
    }

	public int putNode(final int key, final int[] value) {
		// 	if (!containsKey(key))
		// 	    put(key, value);
		// 	else {
		// 	    int[] oldNode = (int[])remove(key);
		// 	    int[] newNode = new int[oldNode.length + value.length - compositeStateSize];

		// 	    for (int i=0; i<oldNode.length; i++)
		// 		newNode[i] = oldNode[i];
		// 	    for (int i=0; i<value.length-compositeStateSize; i++)
		// 		newNode[i + oldNode.length] = value[i + compositeStateSize];

		// 	    put(key, newNode);
		// 	}
		if (!containsKey(key)) {
			final ArrayList<int[]> values = new ArrayList<int[]>(1);
			values.add(value);
			put(key, values);

			return 0;
		}
		else {
			final ArrayList<int[]> values = (ArrayList<int[]>)get(key);
			values.ensureCapacity(values.size() + 1);
			values.add(value);
			put(key, values);

			return values.size() - 1;
		}
    }

 	public ArrayList<int[]> getNodeArray(final int key) {
		return (ArrayList<int[]>) get(key);
    }

    public int[] getNode(final int key, final int arrayIndex) throws Exception {
		try {
			return getNodeArray(key).get(arrayIndex);
		}
		catch (final Exception e) {
			throw e;
		}
    }

    public int[] getNode(final int key) throws Exception {
		final ArrayList<int[]> values = getNodeArray(key);

		if (values.size() > 1)
			throw new Exception("Overload - the key corresponds to several values");

		return values.get(0);
    }

    public int getArrayIndexForNode(final int key, final int[] node) {
		final ArrayList<int[]> values = getNodeArray(key);

		if (values == null)
			return -1;

		for (int i=0; i<values.size(); i++) {
			boolean equal = true;

			final int[] currValue = values.get(i);

			for (int j=0; j<currValue.length; j++) {
				if (currValue[j] != node[j]) {
					equal = false;
					break;
				}
			}

			if (equal)
				return i;
		}

		return -1;
    }

	public void removeNode(final int key, final int arrayIndex) {
		final ArrayList<int[]> values = getNodeArray(key);
		values.remove(arrayIndex);
		put(key, values);
    }

	public void replaceNode(final int key, final int arrayIndex, final int[] newNode) {
		final ArrayList<int[]> values = getNodeArray(key);

		values.remove(arrayIndex);
		values.add(arrayIndex, newNode);
		put(key, values);
    }

    public void replaceNode(final int key, final int[] newNode) {
		replaceNode(key, 0, newNode);
    }

    public boolean containsKey(final int key) {
		return containsKey(key);
    }

    public Object get(final int key) {
		return get(key);
    }
}