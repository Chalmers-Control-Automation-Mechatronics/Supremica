//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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

    /** The root of the Strongly Connected Component this state belongs to */
    private int mRoot;


    //#########################################################################
    //# Constructor
    /**
     * It creates an empty state tuple.
     */
    EncodedStateTuple(){
      mRoot = Integer.MAX_VALUE;
    }

    /**
     * It creates an empty state tuple.
     * @param size number of integers used to store encoded state
     */
    EncodedStateTuple(final int size)
    {
	mEncodedStateCodes = new int[size];
    mRoot = Integer.MAX_VALUE;
    }

    /**
     * Creates a state tuple with given encoded state tuple (integer array).
     */
    EncodedStateTuple(final int[] encodedStateCodes)
    {
	mEncodedStateCodes = encodedStateCodes;
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
