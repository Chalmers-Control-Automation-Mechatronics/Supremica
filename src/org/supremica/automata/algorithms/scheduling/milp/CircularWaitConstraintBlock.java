/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.ArrayList;

/**
 *
 * @author avenir
 */
public class CircularWaitConstraintBlock 
    extends ArrayList<int[]>
{
    private boolean hasBuffer;
    
    public CircularWaitConstraintBlock()
    {
        super();
    }
    
    public void add(int[] constraint, boolean hasBuffer)
    {
        super.add(constraint);
        this.hasBuffer = hasBuffer;
    }
    
    public boolean hasBuffer()
    {
        return hasBuffer;
    }
    
    public void setBuffer(boolean hasBuffer)
    {
        this.hasBuffer = hasBuffer;
    }
}
