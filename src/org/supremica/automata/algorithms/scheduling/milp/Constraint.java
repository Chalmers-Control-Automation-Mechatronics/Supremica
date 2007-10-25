/*
 * MutexConstraint.java
 *
 * Created on den 23 oktober 2007, 17:53
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

/**
 *
 */
public class Constraint
{
    /** 
     * The fingerprint of this constraint. It consists of different indices 
     * depending on the constraint types, namely:
     *
     * Mutex: [zone_index, first_plant_index, second_plant_index, repeated_booking_index];
     * Alt.paths: [plant_time_index, from_state_index, to_state_index];
     * Alt.paths (summation): [plant_time_index, from_state_index];
     */
    private int[] id;
    private String body;
    
    /** Creates a new instance of MutexConstraint */
    public Constraint(int[] id, String body)
    {
        this.id = id;
        this.body = body;
    }
    
    public int[] getId()
    {
        return id;
    }
    
    public String getBody()
    {
        return body;
    }
}
