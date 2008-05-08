/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.io.PrintWriter;

/**
 *
 * @author voronov
 */
public class ExprAcceptorPlainPrint implements ExprAcceptor  {
    
    ExprFactory ef;
    PrintWriter pw;
        
    public ExprAcceptorPlainPrint(ExprFactory ef, PrintWriter pw){
        this.ef = ef;
        this.pw = pw;
    }

    /**
     * accepts expression Expr and prints it in a simple form
     * comment: println is used for autoflushing of PrintWriter
     * @param expr expression to print
     */
    public void accept(Expr expr) {
        internalAccept(expr);
        pw.println();
    }
    
    private void internalAccept(Expr expr) {
        switch(expr.getType()){
            case LIT:
                pw.print(ef.LitToString(expr)+" ");
                break;
            case AND:
                pw.print("AND(");
                for(Expr elem: expr)
                    internalAccept(elem);
                pw.print(")");
                break;
            case OR:
                pw.print("OR(");
                for(Expr elem: expr)
                    internalAccept(elem);
                pw.print(")");
                break;
        }
    }

}
