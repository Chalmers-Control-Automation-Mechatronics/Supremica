//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis;


/**
 * <P>Encoded synchronized state tuple</P>
 *
 * @author Peter Yunil Park
 */

public class EncodedStateTuple
{
    /** Encoded state tuple */
    private int mEncodedStateCodes[];
    
    /** Check if current state tuple has been already visited */
    private boolean mVisited;
    
    /** Check if current state tuple is already in some component */
    private boolean mInComponent;
    
    
    //#########################################################################
    //# Constructor
    /**
     * It creates an empty state tuple.
     */
    public EncodedStateTuple(){}

    /**
     * It creates an empty state tuple.
     * @param size number of integers used to store encoded state
     */
    public EncodedStateTuple(int size)
    {
	mEncodedStateCodes = new int[size];
    }

    /**
     * Creates a state tuple with given encoded state tuple (integer array).
     */
    public EncodedStateTuple(int[] encodedStateCodes)
    {
	mEncodedStateCodes = encodedStateCodes;
    }
    

    /**
     * It returns current encoded state tuple codes.
     * @return current encoded state tuple codes
     */
    public int[] getCodes()
    {
	return mEncodedStateCodes;
    }

    /**
     * It checks if the state tuple has been visited.
     * @return true if the state tuple has been visited, false otherwise
     */
    public boolean getVisited()
    {
	return mVisited;
    }

    /**
     * It sets the state is visited or not visited.
     * @param visited true if it needs to be set to visited, false otherwise
     */
    public void setVisited(boolean visited)
    {
	mVisited = visited;
    }

    /**
     * It checks if the state tuple is in component.
     * @return true if the state tuple is in component, false otherwise
     */
    public boolean getInComponent()
    {
	return mInComponent;
    }

    /**
     * It sets the state is in component or not in component.
     * @param inComponent true if it needs to be set to in component, false otherwise
     */
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
	    result *= 5; // magic number (it could also be 31)
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
