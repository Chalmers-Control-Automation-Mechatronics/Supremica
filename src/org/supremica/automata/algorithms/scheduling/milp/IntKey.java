/*
 * IntKey.java
 *
 * Created on den 23 oktober 2007, 11:23
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

/**
 * Encapsulates two int[]-objects and creates a unique hash code for this 
 * combination. Used as a hashtable key (in internalPrecTable) to distinguish 
 * between different orderings of precedence variables.
 */
public class IntKey
{
    int[] firstInt, secondInt;
    IntKey(int[] firstInt, int[] secondInt)
    {
        this.firstInt = firstInt;
        this.secondInt = secondInt;        
    }

    public int hashCode()
    {
        return 31*firstInt.hashCode() + secondInt.hashCode();
    }
    
    /**
     * Compares two IntKey-objects and returns true if they contain identical 
     * int[]-instances (thus two IntKeys can differ although  the elements in 
     * the underlying int[]'s are identical). 
     *
     * @param   obj the (hopefully IntKey) object to be compared with
     * @return  true if the underlying int[]-instances are identical.
     */
    public boolean equals(Object obj)
    {
        if (! (obj instanceof IntKey))
        {
            return false;
        }

        IntKey intKey = (IntKey) obj;
        if (firstInt.equals(intKey.getFirstInt()) && secondInt.equals(intKey.getSecondInt()))
        {
            return true;
        }
        
        return false;
    }
    
    public int[] getFirstInt()
    {
        return firstInt;
    }
    public int[] getSecondInt()
    {
        return secondInt;
    }
}