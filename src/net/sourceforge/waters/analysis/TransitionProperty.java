//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TransitionProperty
//###########################################################################
//# $Id: TransitionProperty.java,v 1.2 2006-08-20 22:51:38 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.List;
import java.util.ArrayList;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>Transition Property of state tuple</P>
 *
 * <P>It contains the property of transition between two state tuples</P>
 *
 * @author Peter Yunil Park
 */

public class TransitionProperty
{
    /** Source tuple of transition */
    private ArrayList<Integer> sourceTuple;

    /** Target tuple of transition */
    private ArrayList<Integer> targetTuple;

    /** Event of transition */
    private int event;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new transition tuple. It creates a transition of state tuple
     * from given parameters
     * @param  sourceTuple source of transition tuple
     * @param  targetTuple target of transition tuple
     * @param  event event of transition
     */
    public TransitionProperty(ArrayList<Integer> sourceTuple, ArrayList<Integer> targetTuple, int event)
    {
	this.sourceTuple = sourceTuple;
	this.targetTuple = targetTuple;
	this.event = event;
    }

    
    //#########################################################################
    //# Invocation
    /**
     * returns source state tuple
     * @return source state tuple
     */
    public ArrayList<Integer> getSourceTuple()
    {
	return this.sourceTuple;
    }

    /**
     * returns target state tuple
     * @return target state tuple
     */
    public ArrayList<Integer> getTargetTuple()
    {
	return this.targetTuple;
    }

    /**
     * returns event
     * @return event between source and target
     */
    public int getEvent()
    {
	return this.event;
    }
}
