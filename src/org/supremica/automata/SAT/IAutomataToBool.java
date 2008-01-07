/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;

import java.io.PrintWriter;
import org.sat4j.specs.ISolver;

/**
 *
 * @author voronov
 */
public interface IAutomataToBool {
    public void printDimacsCnfStr(PrintWriter pwOut);
    public void printDimacsSatStr(PrintWriter pwOut);
    public void chargeSolver(ISolver solver);
    public void decode(int[] answer);
    public void decode(String answer);    
}
