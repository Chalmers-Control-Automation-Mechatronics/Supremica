/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;
import  org.supremica.automata.SAT.expr.util.*;
import  java.util.*;
/**
 *
 * @author voronov
 */

public abstract class Expr {
    public enum ExprType{
        AND, OR, NOT, VAREQINT, VAREQVAR, LIT, MAND, MOR;
    }

    public ExprType type;
    public abstract Object accept(IVisitor v);
    
        
} 