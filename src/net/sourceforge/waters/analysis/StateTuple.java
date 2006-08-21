//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateTuple
//###########################################################################
//# $Id: StateTuple.java,v 1.1 2006-08-21 03:45:51 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;


/**
 * <P>A state tuple of synchronized model.</P>
 *
 * @author Peter Yunil Park
 */

public class StateTuple
{
    /** Array of integers for state tuple */
    private int[] mStateCodes;


    /**
     * <P>A state tuple in synchronized model.</P>
     *
     * @author Peter Yunil Park
     */
    public StateTuple(int size)
    {
	mStateCodes = new int[size];
    }
    
    public StateTuple(StateTuple tuple)
    {
	mStateCodes = new int[tuple.mStateCodes.length];
	for(int i = 0; i < tuple.mStateCodes.length; i++){
	    mStateCodes[i] = tuple.mStateCodes[i];
	}
    }
    
    
    /**
     * Get required state from state tuple
     */
    public int get(int index)
    {
	return mStateCodes[index];
    }
    
    /**
     * Set state tuple
     */
    public void set(int index, int state)
    {
	mStateCodes[index] = state;
    }
    
    /**
     * Get hash code of state tuple
     */
    public int hashCode()
    {
	int result = 0;
	for(int i = 0; i < mStateCodes.length; i++){
	    result *= 5; // 31
	    result += mStateCodes[i];
	}
	return result;
    }
    
    /**
     * Check if passed object is same as current state tuple
     */
    public boolean equals(Object other)
    {
	if(other != null && getClass() == other.getClass()){
	    StateTuple tuple = (StateTuple)other;
	    for(int i = 0; i < mStateCodes.length; i++){
		if(mStateCodes[i] != tuple.get(i)){
		    return false;
		}
	    }
	}
	else{
	    return false;
	}

	return true;
    }
}
