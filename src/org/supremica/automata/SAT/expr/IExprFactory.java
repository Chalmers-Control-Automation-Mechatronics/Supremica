/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;

/**
 *
 * @author voronov
 */
public interface IExprFactory {
    public Expr And(Expr e1, Expr e2);
    public Expr Or(Expr e1, Expr e2);
    public Expr InitAnd();
    public Expr InitOr();
}