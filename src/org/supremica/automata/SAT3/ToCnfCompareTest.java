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
public class ToCnfCompareTest {
    public static void main(String[] args) throws TimeoutException{
        System.out.println("comparing conversions");
        
        ExprFactory ef = new ExprFactoryArrayList();
               
        // 2 3       
        Expr e1 = ef.Or(ef.Lit(2), ef.Lit(3));
        solveS(e1);
        solveS(e1);
        
        // -2 3
        // -2 4 
        //e = (ef.Or(ef.Not(ef.Lit(2)), ef.And(ef.Lit(3),ef.Lit(4))));
        //compare(e, tcs, tcv, solver);
       
        Expr e3 = (ef.Or(
                ef.And(
                    ef.Lit(2),
                    ef.Lit(3)
                    ), 
                ef.And(
                    ef.Lit(4),
                    ef.Lit(5)
                )
         ));
        solveS(e3);
        solveV(e3);
                
        Expr e5 = ef.And(
                ef.And(
                    ef.Or(ef.Lit(3), ef.Lit(4)),
                    ef.Or(ef.Lit(-3), ef.Lit(-4))),
                ef.And(
                    ef.Or(ef.Lit(3), ef.Lit(-4)),
                    ef.Or(ef.Lit(-3), ef.Lit(4)))
                );
        solveS(e5);
        solveV(e5);               
        
        Expr e = ef.Or(
                ef.And(ef.Lit(1),ef.Lit(2)),
                ef.And(ef.Lit(3),ef.Lit(4))
                );
        solveS(e);
        solveV(e);               
    }

    private static void solveS(Expr e) throws TimeoutException{
        
        ExprFactory ef = new ExprFactoryArrayList();
       
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(20);
        CnfClauseAcceptorSat4j acceptor = new CnfClauseAcceptorSat4j(solver);
                      
        ToCnf tc = new ToCnfStruct(10, ef, acceptor);        
        tc.accept(e);
        System.out.println(solver.isSatisfiable()?"S:SAT ":"S:UNsat ");
    }
    private static void solveV(Expr e) throws TimeoutException{
        
        ExprFactory ef = new ExprFactoryArrayList();
       
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(20);
        CnfClauseAcceptorSat4j acceptor = new CnfClauseAcceptorSat4j(solver);
                      
        ToCnf tc = new ToCnfVar(ef, acceptor);        
        tc.accept(e);
        System.out.println(solver.isSatisfiable()?"V:SAT ":"V:UNsat ");
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
