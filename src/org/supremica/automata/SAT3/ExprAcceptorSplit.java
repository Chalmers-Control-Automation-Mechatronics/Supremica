/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

/**
 * Takes two ExprAcceptors and feeds them the same data from producer
 * @author voronov
 */
public class ExprAcceptorSplit implements ExprAcceptor {
    
    private ExprAcceptor ac1, ac2; 
    
    public ExprAcceptorSplit(ExprAcceptor acc1, ExprAcceptor acc2){
        this.ac1 = acc1;
        this.ac2 = acc2;
    }

    public void accept(Expr expr) {
        ac1.accept(expr);
        ac2.accept(expr);
    }

}
