/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import java.util.Collection;

/**
 * This interface should provide easy iteration, as well as encapsulation of 
 * a single integer in case Type is LIT. 
 * 
 * @author voronov
 */
public interface Expr extends Collection<Expr> {

    public enum Type{ AND, OR, LIT };
        
    public Type getType();        
}
