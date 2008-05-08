/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
import java.util.ArrayList;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 *
 * @author voronov
 */
public class CnfClauseAcceptorSat4jTest {

    public static void main(String[] args) throws TimeoutException{
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(3);
        CnfClauseAcceptorSat4j a = new CnfClauseAcceptorSat4j(solver);
        
        ArrayList<Integer> c = new ArrayList<Integer>();
        
        c.add(1);
        c.add(2);
        c.add(3);       
        a.accept(c);
        
        c.clear();
        c.add(-1);
        a.accept(c);
        
        c.clear();
        c.add(-2);
        a.accept(c);

        c.clear();
        c.add(-3);
        a.accept(c);
        
        if(solver.isSatisfiable()) {
            System.out.println("Satisfiable !");
        } else {
            System.out.println("Unsatisfiable !");
        }
    }
}
