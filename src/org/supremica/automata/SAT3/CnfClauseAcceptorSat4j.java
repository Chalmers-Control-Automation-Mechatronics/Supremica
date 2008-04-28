/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 *
 * @author voronov
 */
public class CnfClauseAcceptorSat4j implements CnfClauseAcceptor{
    
    ISolver solver;
    /**
     * 
     * @param solver   solver that is already aware of number of clauses ("newVar(howmany)") 
     */
    public CnfClauseAcceptorSat4j(ISolver solver){        
        this.solver = solver;
    }

    public void accept(Collection<Integer> c) {        
        try {
            IVecInt vi = new VecInt();
//            System.err.print("adding clause: ");
            for (int i : c) {                
                vi.push(i);         
//                System.err.print(""+i+" ");
            }
            solver.addClause(vi);
//            System.err.println();
        } catch (ContradictionException ex) {
            Logger.getLogger(CnfClauseAcceptorSat4j.class.getName()).log(Level.SEVERE, "contradiction in the clause!", ex);
        }
    }

}
