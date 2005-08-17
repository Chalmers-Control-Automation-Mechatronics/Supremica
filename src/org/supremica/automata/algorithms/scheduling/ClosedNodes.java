package org.supremica.automata.algorithms.scheduling;

import java.util.Hashtable;
import java.util.ArrayList;

import org.supremica.log.*;

public class ClosedNodes extends Hashtable {
    public static final int CLOSED_NODE_INFO_SIZE = 2;

    private static Logger logger = LoggerFactory.createLogger(ClosedNodes.class);

    private int compositeStateSize;

    public ClosedNodes(int compositeStateSize) {
 	super();
	this.compositeStateSize = compositeStateSize;
    }

    public int putNode(int key, int[] value) {	
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
	Integer internalKey = new Integer(key);

	if (!containsKey(key)) {
	    ArrayList<int[]> values = new ArrayList<int[]>(1);
	    values.add(value);
	    put(key, values);

	    return 0;
	}
	else {
	    ArrayList<int[]> values = (ArrayList<int[]>)get(key);
	    values.ensureCapacity(values.size() + 1);
	    values.add(value);
	    put(key, values);

	    return values.size() - 1;
	}
    }

    public ArrayList<int[]> getNodeArray(int key) {
	return (ArrayList<int[]>) get(new Integer(key));
    }

    public int[] getNode(int key, int arrayIndex) throws Exception {
	return getNodeArray(key).get(arrayIndex);
    }

    public int[] getNode(int key) throws Exception {
	ArrayList<int[]> values = getNodeArray(key);
	
	if (values.size() > 1) 
	    throw new Exception("Overload - the key corresponds to several values");
	
	return values.get(0);
    }

    public int getArrayIndexForNode(int key, int[] node) {
	ArrayList<int[]> values = getNodeArray(key);

	if (values == null)
	    return -1;
	
	for (int i=0; i<values.size(); i++) {
	    boolean equal = true;

	    int[] currValue = values.get(i);

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

    public void removeNode(int key, int arrayIndex) {
	ArrayList<int[]> values = getNodeArray(key);
	
	values.remove(arrayIndex);
	put(key, values);
    }
    
    public void replaceNode(int key, int arrayIndex, int[] newNode) {
	ArrayList<int[]> values = getNodeArray(key);

	values.remove(arrayIndex);
	values.add(arrayIndex, newNode);
	put(key, values);
    }

    public void replaceNode(int key, int[] newNode) {
	replaceNode(key, 0, newNode);
    }

    public boolean containsKey(int key) {
	return containsKey(new Integer(key));
    }

    public Object get(int key) {
	return get(new Integer(key));
    }
}