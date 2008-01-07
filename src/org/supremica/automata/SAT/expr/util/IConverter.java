/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.Expr;

/**
 *
 * @author voronov
 */
public interface IConverter {
    public Expr convert(Expr e);
}
