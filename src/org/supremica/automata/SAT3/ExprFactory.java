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
    public Expr Or();
    public Expr Or(Expr e1, Expr e2);
    public Expr And();
    public Expr And(Expr e1, Expr e2);
    public Expr Lit(int varNumber);
    public Expr Not(Expr expr);

}
