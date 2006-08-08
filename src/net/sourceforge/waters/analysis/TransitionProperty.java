//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TransitionProperty
//###########################################################################
//# $Id: TransitionProperty.java,v 1.1 2006-08-08 22:32:37 yip1 Exp $
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
    private ArrayList<StateProxy> sourceTuple;

    /** Target tuple of transition */
    private ArrayList<StateProxy> targetTuple;

    /** Event of transition */
    private EventProxy event;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new transition tuple. It creates a transition of state tuple
     * from given parameters
     * @param  sourceTuple source of transition tuple
     * @param  targetTuple target of transition tuple
     * @param  event event of transition
     */
    public TransitionProperty(ArrayList<StateProxy> sourceTuple, ArrayList<StateProxy> targetTuple, EventProxy event)
    {
	this.sourceTuple = sourceTuple;
	this.targetTuple = targetTuple;
	this.event = event;
    }

    
    //#########################################################################
    //# Invocation
    public ArrayList<StateProxy> getSourceTuple()
    {
	return this.sourceTuple;
    }

    public ArrayList<StateProxy> getTargetTuple()
    {
	return this.targetTuple;
    }

    public EventProxy getEvent()
    {
	return this.event;
    }
}
