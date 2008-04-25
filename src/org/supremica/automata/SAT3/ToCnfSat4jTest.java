/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.io.PrintWriter;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 *
 * @author voronov
 */
public class ToCnfSat4jTest {
    public static void main(String[] args) throws TimeoutException{
        System.out.println("comparing conversions");
        
        ExprFactory ef = new ExprFactoryArrayList();
       
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(20);
        CnfClauseAcceptorSat4j acceptor = new CnfClauseAcceptorSat4j(solver);
                      
        ToCnf tcs = new ToCnfStruct(10, ef, acceptor);
        ToCnf tcv = new ToCnfVar(ef, acceptor);
        
        
        // 2 3       
        Expr e = ef.Or(ef.Lit(2), ef.Lit(3));
        compare(e, tcs, tcv, solver);
        
        // -2 3
        // -2 4 
        e = (ef.Or(ef.Not(ef.Lit(2)), ef.And(ef.Lit(3),ef.Lit(4))));
        compare(e, tcs, tcv, solver);

        
        e = (ef.Or(
                ef.And(
                    ef.Lit(2),
                    ef.Lit(3)
                    ), 
                ef.And(
                    ef.Lit(4),
                    ef.Lit(5)
                )
         ));
        compare(e, tcs, tcv, solver);  
        
        
        e = ef.And(
                ef.And(
                    ef.Or(ef.Lit(3), ef.Lit(4)),
                    ef.Or(ef.Lit(-3), ef.Lit(-4))),
                ef.And(
                    ef.Or(ef.Lit(3), ef.Lit(-4)),
                    ef.Or(ef.Lit(-3), ef.Lit(4)))
                );
        compare(e, tcs, tcv, solver);  
        CnfClauseAcceptor acc2s = new CnfClauseAcceptorDimacsStream(
                new PrintWriter(System.err));
        CnfClauseAcceptor acc2v = new CnfClauseAcceptorDimacsStream(
                new PrintWriter(System.err));
        
        ToCnf tcs2 = new ToCnfStruct(10, ef, acc2s);
        ToCnf tcv2 = new ToCnfVar(ef, acc2v);
        tcs2.accept(e);
        tcv2.accept(e);
        
        
    }
    
    private static void compare(Expr e, ToCnf tc1, ToCnf tc2, ISolver solver) throws TimeoutException{
        solver.reset();
        tc1.accept(e);
        System.out.println(solver.isSatisfiable()?"1:SAT ":"1:UNsat ");
        solver.reset();
        
        tc2.accept(e);
        System.out.println(solver.isSatisfiable()?"2:SAT ":"2:UNsat ");
        solver.reset();                
        
    }
}
