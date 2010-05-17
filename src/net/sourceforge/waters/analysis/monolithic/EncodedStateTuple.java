//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   StateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;


/**
 * <P>Encoded synchronized state tuple.</P>
 *
 * @author Peter Yunil Park
 */

class EncodedStateTuple
{
    /** Encoded state tuple */
    private int mEncodedStateCodes[];

    /** Check if current state tuple has been already visited */
    private boolean mVisited;

    /** Check if current state tuple is already in some component */
    private boolean mInComponent;

    /** Represents the order which each state is visited. The first state which
     * is visited has mVisitOrder == 1. Unvisited states have mVisitOrder == Integer.MAXVALUE*/
    private int mVisitOrder;

    /** The root of the Strongly Connected Component this state belongs to */
    private int mRoot;


    //#########################################################################
    //# Constructor
    /**
     * It creates an empty state tuple.
     */
    EncodedStateTuple(){
      mVisitOrder = Integer.MAX_VALUE;
      mRoot = Integer.MAX_VALUE;
    }

    /**
     * It creates an empty state tuple.
     * @param size number of integers used to store encoded state
     */
    EncodedStateTuple(final int size)
    {
	mEncodedStateCodes = new int[size];
    mVisitOrder = Integer.MAX_VALUE;
    mRoot = Integer.MAX_VALUE;
    }

    /**
     * Creates a state tuple with given encoded state tuple (integer array).
     */
    EncodedStateTuple(final int[] encodedStateCodes)
    {
	mEncodedStateCodes = encodedStateCodes;
    mVisitOrder = Integer.MAX_VALUE;
    mRoot = Integer.MAX_VALUE;
    }


    /**
     * It returns current encoded state tuple codes.
     * @return current encoded state tuple codes
     */
    int[] getCodes()
    {
	return mEncodedStateCodes;
    }

    /**
     * It checks if the state tuple has been visited.
     * @return true if the state tuple has been visited, false otherwise
     */
    boolean getVisited()
    {
	return mVisited;
    }

    /**
     * It sets the state is visited or not visited.
     * @param visited true if it needs to be set to visited, false otherwise
     */
    void setVisited(final boolean visited)
    {
	mVisited = visited;
    }

    /**
     * It checks if the state tuple is in component.
     * @return true if the state tuple is in component, false otherwise
     */
    boolean getInComponent()
    {
	return mInComponent;
    }

    /**
     * It sets the state is in component or not in component.
     * @param inComponent true if it needs to be set to in component, false otherwise
     */
    void setInComponent(final boolean inComponent)
    {
	mInComponent = inComponent;
    }

    /**
     * Get required state from state tuple
     * @param index index of state in the automata
     * @return index of state
     */
    int get(final int index)
    {
	return mEncodedStateCodes[index];
    }

    /**
     * Sets the visit order of this state to the int argumnet. If the root was the same as the old visit order, then it is assumed
     * that the root is this state, and thus the root is changed as well.
     * @param order The integer value of the order that the state has been visited in.
     */
    public void visit(final int order)
    {
      if (mVisitOrder == mRoot)
        mRoot = order;
      mVisitOrder = order;
    }
    public int getOrder()
    {
      return mVisitOrder;
    }

    public void setRoot(final int root)
    {
      mRoot = root;
    }
    public int getRoot()
    {
      return mRoot;
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
    public boolean equals(final Object other)
    {
	if(other != null && getClass() == other.getClass()){
	    final EncodedStateTuple tuple = (EncodedStateTuple)other;
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
