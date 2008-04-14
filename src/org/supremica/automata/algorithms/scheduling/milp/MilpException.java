/*
 * MilpException.java
 *
 * Created on den 23 oktober 2007, 12:06
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import org.supremica.util.SupremicaException;

/**
 * 
 */
public class MilpException 
    extends SupremicaException
{
    
    /**
     * Creates a new instance of <code>MilpException</code> without detail message.
     */
    public MilpException()
    {
        super("Exception when solving MILP from Supremica. ");
    }
    
    
    /**
     * Constructs an instance of <code>MilpException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MilpException(String msg)
    {
        super("Exception when solving MILP from Supremica. " + msg);
    }
}
