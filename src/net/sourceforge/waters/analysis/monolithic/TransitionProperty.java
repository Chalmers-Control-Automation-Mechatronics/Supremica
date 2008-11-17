//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TransitionProperty
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;


/**
 * <P>Transition between two state tuples.</P>
 *
 * <P>It contains the property of transition between two state tuples</P>
 *
 * @author Peter Yunil Park
 */

class TransitionProperty
{
    /** Source tuple of transition */
    private EncodedStateTuple sourceTuple;

    /** Target tuple of transition */
    private EncodedStateTuple targetTuple;

    /** Event of transition */
    private int event;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new transition tuple. It creates a transition of state tuple
     * from given parameters
     * @param  sourceTuple source transition tuple
     * @param  targetTuple target transition tuple
     * @param  event event of transition
     */
    public TransitionProperty(EncodedStateTuple sourceTuple, EncodedStateTuple targetTuple, int event)
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
    public EncodedStateTuple getSourceTuple()
    {
	return sourceTuple;
    }

    /**
     * returns target state tuple
     * @return target state tuple
     */
    public EncodedStateTuple getTargetTuple()
    {
	return targetTuple;
    }

    /**
     * returns event
     * @return event between source and target
     */
    public int getEvent()
    {
	return event;
    }
}
