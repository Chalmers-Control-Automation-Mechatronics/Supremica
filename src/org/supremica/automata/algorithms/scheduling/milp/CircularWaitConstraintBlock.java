/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.ArrayList;

/**
 * This class implements a circular wait constraint, consisting of an information list
 * about involved boolean milp-variables (stored as int[]-arrays). Also, a boolean 
 * variable, 'hasBuffer', is stored to represent whether this block in fact 
 * corresponds to a circular wait or if it only can be used to construct unfeasibility
 * constraints. Each int[]-array contains 
 * [zoneIndex, firstPlantIndex, secondPlantIndex, firstTic, secondTic],
 * which allows to build a milp-variable, e.g. as "r" + firstPlantIndex + "_books_" + 
 * zoneIndex + "_before_" + secondPlantIndex + "var" + indexInFunctionOf(firstTic, secondTic).
 * 
 * @author Avenir Kobetski
 */
public class CircularWaitConstraintBlock 
    extends ArrayList<int[]>
{
    private static final long serialVersionUID = 1L;

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
