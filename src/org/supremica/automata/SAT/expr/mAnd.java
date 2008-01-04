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
public class mAnd extends Expr  implements Iterable<Expr> {
    public List<Expr> childs = new ArrayList<Expr>();

    public mAnd(){
        type = ExprType.MAND;
    }
    public void add(Expr n){
        childs.add(n);
    }
    public Iterator<Expr> iterator(){
        return childs.iterator();
    }
    public Object accept(Visitor v){
        return v.visit(this);
    }                
}
