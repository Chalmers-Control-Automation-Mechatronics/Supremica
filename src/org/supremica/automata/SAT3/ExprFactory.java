/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.Collection;

/**
 *
 * @author voronov
 */
public interface ExprFactory {
    
    public Collection<Integer> PlainToCollection(Expr expr);
    
    
    /**
     * Create empty OR expression (useful for destructive add)
     * @return
     */
    public Expr Or();
    
    
    /**
     * non-destructively combine two expressions using OR
     * @param e1
     * @param e2
     * @return
     */
    public Expr Or(Expr e1, Expr e2);
    
    
    /**
     * Create empty AND expression (useful for destructive add)
     * @return
     */
    public Expr And();
    
    
    /**
     * non-destructively combine two expressions using AND
     * @param e1
     * @param e2
     * @return
     */
    public Expr And(Expr e1, Expr e2);
    
    
    /**
     * Create new Literal
     * @param varNumber
     * @return
     */
    public Expr Lit(int varNumber);
    
    
    /**
     * Destructively add second argument into first expression and return this 
     * first expression updated
     * @param big AND or OR expression
     * @param e   any expression to add to "big"
     * @return    "big" expression updated
     */
    public Expr add(Expr big, Expr e);
    
    
    public Expr Not(Expr expr);
    
    /**
     * Converts Literal to String
     * @param expr 
     * @return
     */
    public String LitToString(Expr expr);    

}
