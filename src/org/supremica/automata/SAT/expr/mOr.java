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
public class mOr extends Expr  implements Iterable<Expr> {
    public List<Expr> childs  = new ArrayList<Expr>();

    public mOr(){
        type = ExprType.MOR;        
    }
    /** makes copy */
    public mOr(mOr or){
        type = ExprType.MOR;
        for(Expr e: or)
            add(e);
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
