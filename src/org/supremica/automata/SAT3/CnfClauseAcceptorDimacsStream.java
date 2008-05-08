/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.Collection;
import java.io.PrintWriter; // character (not byte), formatted (newlines)

/**
 *
 * @author voronov
 */
public class CnfClauseAcceptorDimacsStream implements CnfClauseAcceptor{
    
    private PrintWriter out = null;
    
    public CnfClauseAcceptorDimacsStream(PrintWriter out){
        
        this.out = out;
        
        /* The first line should be:
         * p cnf variables clauses
         * but on creation we have no idea about number of clause and variables
         * for now we will put spaces, and externally will replace them with numbers
         */ 
        out.println("p cnf                               "); 
    }

    public void accept(Collection<Integer> c) {
        for(int i: c)
            out.print("" + i + " ");
        out.println("0");
        out.flush();
    }
}
