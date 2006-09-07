//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateTuple
//###########################################################################
//# $Id: StateTuple.java,v 1.3 2006-09-07 10:37:35 robi Exp $
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
    //private boolean mVisited;
    //private boolean mInComponent;
    
    
    //#########################################################################
    //# Constructor
    /**
     * Create state tuple with given size
     * @param size size of state (same as number of automaton in the model)
     */
    public StateTuple(int size)
    {
	mStateCodes = new int[size];
    }

    /**
     * Creates state tuple with given integer array.
     */    
    public StateTuple(int[] stateCodes)
    {
	mStateCodes = new int[stateCodes.length];
	for(int i = 0; i < stateCodes.length; i++){
	    mStateCodes[i] = stateCodes[i];
	}
    }

    /**
     * Create state tuple with given tuple (copying tuple)
     * @param tuple original tuple information
     */    
    public StateTuple(StateTuple tuple)
    {
	mStateCodes = new int[tuple.mStateCodes.length];
	for(int i = 0; i < tuple.mStateCodes.length; i++){
	    mStateCodes[i] = tuple.mStateCodes[i];
	}
    }
    
    /*
    public boolean getVisited()
    {
	return mVisited;
    }

    public void setVisited(boolean visited)
    {
	mVisited = visited;
    }

    public boolean getInComponent()
    {
	return mInComponent;
    }

    public void setInComponent(boolean inComponent)
    {
	mInComponent = inComponent;
    }
    */
    
    /**
     * Get whole state codes
     */
    public int[] getCodes()
    {
	return mStateCodes;
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
