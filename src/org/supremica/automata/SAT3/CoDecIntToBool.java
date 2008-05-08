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
public interface CoDecIntToBool {

    public int width(int value);    
    public ArrayList<Boolean> toBits(int value, int width);
}
