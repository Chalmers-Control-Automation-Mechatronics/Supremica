//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControlLoopChecker
//###########################################################################
//# $Id: StateProperty.java,v 1.1 2006-08-08 22:32:37 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.ArrayList;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A property of state used for control loop checker.</P>
 *
 * <P>It contains the information of states in synchronised product of automaton.</P>
 * <P>Contents:</P>
 * <P> 1. Name of current state</P>
 * <P> 2. Root of current state</P>
 * <P> 3. A flag indicates whether the state has been visited</P>
 * <P> 4. A flag indicates whether the state is in a strongly connected component</P>
 * <P> 5. Counter for the order visited</P>
 *
 * @author Peter Yunil Park
 */

public class StateProperty
{
    /** current node v */
    private ArrayList<StateProxy> mNode;

    /** whether v is visited: visited[v] */
    private boolean mVisited;

    /** whether v is in component: inComponent[v] */
    private boolean mInComponent;


    public StateProperty(ArrayList<StateProxy> node)
    {
	this.mNode = node;
    }

    
    public ArrayList<StateProxy> getNode()
    { 
	return this.mNode; 
    }

    public boolean getVisited()
    { 
	return this.mVisited; 
    }

    public boolean getInComponent()
    { 
	return this.mInComponent; 
    }

    public void setVisited(boolean visited)
    { 
	this.mVisited = visited; 
    }

    public void setInComponent(boolean inComponent)
    { 
	this.mInComponent = inComponent;
    }
}
