package org.supremica.automata.algorithms.scheduling;

import java.util.Hashtable;

import org.supremica.log.*;

public class ClosedNodes extends Hashtable {
    private static Logger logger = LoggerFactory.createLogger(ClosedNodes.class);

    private int compositeStateSize;

    public ClosedNodes(int compositeStateSize) {
 	super();
	this.compositeStateSize = compositeStateSize;
    }

    public void  putNode(Integer key, int[] value) {	
	if (!containsKey(key))
	    put(key, value);
	else {
	    int[] oldNode = (int[])remove(key);
	    int[] newNode = new int[oldNode.length + value.length - compositeStateSize];
	    
	    for (int i=0; i<oldNode.length; i++) 
		newNode[i] = oldNode[i];
	    for (int i=0; i<value.length-compositeStateSize; i++) 
		newNode[i + oldNode.length] = value[i + compositeStateSize];

	    put(key, newNode);
	}
    }

    public int[] getNode(Integer key) {
	return (int[])get(key);
    }
    
    //TODO:
    public void replaceNode(int[] oldNode, int[] newNode) {
	
    }
}