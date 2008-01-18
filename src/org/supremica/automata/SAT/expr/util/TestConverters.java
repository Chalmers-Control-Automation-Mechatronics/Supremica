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
        env.addBool(); // number 0
        Literal v1 = new Literal(env.vars.get(env.addBool()), true);
        Literal v2 = new Literal(env.vars.get(env.addBool()), true);
        Literal v3 = new Literal(env.vars.get(env.addBool()), true);
        Literal v4 = new Literal(env.vars.get(env.addBool()), true);
        Literal v5 = new Literal(env.vars.get(env.addBool()), true);
        Literal v6 = new Literal(env.vars.get(env.addBool()), true);
        //Expr e = new Or(new And(v1,v2), new And(v3,v4));
        mOr e1 = new mOr();
        mAnd a = new mAnd();
        a.add(v1);
        a.add(v2);
        a.add(v2);
        mOr b = new mOr();
        b.add(v3);
        b.add(v4);
        a.add(b);
        e1.add(a);
        e1.add((a));
        e1.add(v6);
        e1.add(b);
        System.out.println("source:");
        System.out.println(PrinterInfix.print(e1));        
        Expr e = ConverterToNonNegated.removeAllNegations(e1);
        System.out.println("no negations:");
        System.out.println(PrinterInfix.print(e));        

        System.out.println(ExhaustiveSearch.isSatisfiable(e, env));
        System.out.println(ExhaustiveSearch.isSatisfiable(
                ConverterBoolToCnfSat.convert(e), env));
        
        System.out.println("toCnfSatNew:");
        System.out.println(PrinterInfix.print(
                ConverterBoolToCnfSat.convert(e)));
        
        ConverterBoolToCnfStruct conv = new ConverterBoolToCnfStruct(env);
        Expr es = conv.convertAll(e);
        System.out.println("s-p conversion:");
        System.out.println(PrinterInfix.print(es));        
//        System.out.println(ExhaustiveSearch.isSatisfiable(
//                es, env));
        
    }
}
