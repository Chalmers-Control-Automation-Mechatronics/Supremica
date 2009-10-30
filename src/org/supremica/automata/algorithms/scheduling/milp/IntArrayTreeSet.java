/*
 * IntArrayTreeSet.java
 *
 * Created on den 24 oktober 2007, 15:05
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.TreeSet;

/**
 * This class extends TreeSet to store int[]-objects in such a way that 
 * int[i1 ... k1 ... n1] comes before [i2 ... k2 ... n2] if (k1 < k2) and 
 * j1 == j2 \forall j \in {i..k}
 * 
 */
public class IntArrayTreeSet
        extends TreeSet<int[]>
{
    private static final long serialVersionUID = 1L;

    /** 
     * Creates a new instance of IntArrayTreeSet, using IntArrayComparator to
     * order tree nodes. 
     */
    public IntArrayTreeSet()
    {
        super(new IntArrayComparator());
    }    
    
    /**
     * Returns the pointer to the specified element, if it is stored in the tree.
     */
    public int[] get(int[] element)
    {
        if (! contains(element))
        {
            return null;
        }
        else
        {
            return tailSet(element).first();
        }
    }
}
