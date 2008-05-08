/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.ArrayList;

/**
 *
 * @author voronov
 */
public class CoDecIntToBoolLinear implements CoDecIntToBool {

    public int width(int value) {
        return value;
    }

    public ArrayList<Boolean> toBits(int value, int width) {
        ArrayList<Boolean> res = new ArrayList<Boolean>();
        for(int i = 0; i < width; i++)
            res.add(value == i);        
        return res;
    }

}
