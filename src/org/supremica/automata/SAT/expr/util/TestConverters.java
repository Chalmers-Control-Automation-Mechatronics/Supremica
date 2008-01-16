/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;

import org.supremica.automata.SAT.expr.*;

/**
 *
 * @author alex
 */
public class TestConverters {

    public static void main(String[] args){
        Environment env = new Environment();
        Literal v1 = new Literal(env.vars.get(env.addBool()), true);
        Literal v2 = new Literal(env.vars.get(env.addBool()), true);
        Literal v3 = new Literal(env.vars.get(env.addBool()), true);
        Literal v4 = new Literal(env.vars.get(env.addBool()), true);
        Literal v5 = new Literal(env.vars.get(env.addBool()), true);
        Literal v6 = new Literal(env.vars.get(env.addBool()), true);
        //Expr e = new Or(new And(v1,v2), new And(v3,v4));
        mOr e = new mOr();
        e.add(new And(v1,v2));
        e.add(new Not(new And(v3,v4)));
        e.add(v6);
        System.out.println(ExhaustiveSearch.isSatisfiable(e, env));
//        System.out.println(ExhaustiveSearch.isSatisfiable(
//                ConverterBoolToCnfSat.convertAll(e), env));
        System.out.println(ExhaustiveSearch.isSatisfiable(
                ConverterBoolToCnfSatNew.convert(e), env));
        
        System.out.println(PrinterInfix.print(e));
//        System.out.println(PrinterInfix.print(
//                ConverterBoolToCnfSat.convertAll(e)));
        System.out.println(PrinterInfix.print(
                ConverterBoolToCnfSatNew.convert(e)));
        
    }
}
