/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import java.io.PrintWriter;
import org.supremica.automata.SAT.expr.Expr;

/**
 *
 * @author voronov
 */
public interface IPrinter {
    public void print(Expr e, PrintWriter pwOut);
}
