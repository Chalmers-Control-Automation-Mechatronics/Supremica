/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT.*;

/**
 *
 * @author voronov
 */
public interface Visitor {
    public Object visit(And      n);
    public Object visit(Or       n);
    public Object visit(Not      n);
    public Object visit(VarEqVar n);
    public Object visit(VarEqInt n);
    public Object visit(Literal  n);
    public Object visit(mAnd     n);
    public Object visit(mOr      n);
    
}
