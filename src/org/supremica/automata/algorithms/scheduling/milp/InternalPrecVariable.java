/*
 * InternalPrecVariable.java
 *
 * Created on den 23 oktober 2007, 11:22
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

/**
 * This class represents an internal precedence variable that is in MILP-
 * formulation a boolean of type "r_x1_st_y1_before_r_x2_y2". This class
 * contains the name of the variable as it appears in the MILP-formulation and 
 * its (unique) index in the array of internal precedence variables 
 * (currVariableCombination or currBoolCombination), that is used
 * to contruct constraints forbidding impossible or unused combinations. 
 */
public class InternalPrecVariable
{
    /** The name of the internal precedence variable. */
    private String name;
    /** The index of the variable in the array of internal precedence variables. */
    private int index;
    
    InternalPrecVariable(String name, int index)
    {
        this.name = name;
        this.index = index;
    }
    
    String getName()
    {
        return name;
    }
    
    int getIndex()
    {
        return index;
    }
}