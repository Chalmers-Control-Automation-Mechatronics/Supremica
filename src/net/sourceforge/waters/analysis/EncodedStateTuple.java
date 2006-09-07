//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateEncoding
//###########################################################################
//# $Id: EncodedStateTuple.java,v 1.2 2006-09-07 10:37:35 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;


/**
 * <P>Encoding synchronized state</P>
 *
 * @author Peter Yunil Park
 */

public class EncodedStateTuple
{
    private int mEncodedStateCodes[];
    private boolean mVisited;
    private boolean mInComponent;
    
    
    //#########################################################################
    //# Constructor
    /**
     * Create state encoding
     * @param size number of integers used to store encoded state
     */
    public EncodedStateTuple(int size)
    {
	mEncodedStateCodes = new int[size];
    }

    /**
     * Creates state encoding with given integer array.
     */
    public EncodedStateTuple(int[] encodedStateCodes)
    {
	mEncodedStateCodes = encodedStateCodes;
    }
    

    /**
     * returns current encoded state tuple codes
     * @return current encoded state tuple codes
     */
    public int[] getCodes()
    {
	return mEncodedStateCodes;
    }

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

    /**
     * Get required state from state tuple
     * @param index index of state in the automata
     * @return index of state
     */
    public int get(int index)
    {
	return mEncodedStateCodes[index];
    }

    /**
     * Get hash code of encoded state tuple
     * @return hash code
     */
    public int hashCode()
    {
	int result = 0;
	for(int i = 0; i < mEncodedStateCodes.length; i++){
	    result *= 5; // 31
	    result += mEncodedStateCodes[i];
	}
	return result;
    }
    
    /**
     * Check if passed object is same as current encoded state tuple
     * @param other other object that will be compared with current EncodedStateTuple
     * @return true if passed object is same as current, false otherwise
     */
    public boolean equals(Object other)
    {
	if(other != null && getClass() == other.getClass()){
	    EncodedStateTuple tuple = (EncodedStateTuple)other;
	    for(int i = 0; i < mEncodedStateCodes.length; i++){
		if(mEncodedStateCodes[i] != tuple.get(i)){
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
