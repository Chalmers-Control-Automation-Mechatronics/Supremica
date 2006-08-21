//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   TransitionProperty
//###########################################################################
//# $Id: TransitionProperty.java,v 1.3 2006-08-21 03:45:51 yip1 Exp $
//###########################################################################

package net.sourceforge.waters.analysis;


/**
 * <P>Transition between two state tuples</P>
 *
 * <P>It contains the property of transition between two state tuples</P>
 *
 * @author Peter Yunil Park
 */

public class TransitionProperty
{
    /** Source tuple of transition */
    private StateTuple sourceTuple;

    /** Target tuple of transition */
    private StateTuple targetTuple;

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
    public TransitionProperty(StateTuple sourceTuple, StateTuple targetTuple, int event)
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
    public StateTuple getSourceTuple()
    {
	return this.sourceTuple;
    }

    /**
     * returns target state tuple
     * @return target state tuple
     */
    public StateTuple getTargetTuple()
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
