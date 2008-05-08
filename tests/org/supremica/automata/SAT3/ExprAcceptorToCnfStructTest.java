/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author voronov
 */
public class ExprAcceptorToCnfStructTest {

    public static void main(String[] args){
        
        
        
        ExprFactory ef = new ExprFactoryArrayList();
        CnfClauseAcceptor acceptor = new CnfClauseAcceptorDimacsStream(
                new PrintWriter(System.out));
        
        ArrayList<Integer> c = new ArrayList<Integer>();
        c.add(0);
        c.add(1);
        // 0 1 
        acceptor.accept(c);
        
        
        ExprAcceptor tc = new ExprAcceptorToCnfStruct(10, ef, acceptor);
        
        // 2 3 
        tc.accept(ef.Or(ef.Lit(2), ef.Lit(3)));

        // -2 3
        // -2 4 
        tc.accept(ef.Or(ef.Not(ef.Lit(2)), ef.And(ef.Lit(3),ef.Lit(4))));

        tc.accept(ef.Or(
                ef.And(
                    ef.Lit(2),
                    ef.Lit(3)
                    ), 
                ef.And(
                    ef.Lit(4),
                    ef.Lit(5)
                )
         ));

    
    }
}
