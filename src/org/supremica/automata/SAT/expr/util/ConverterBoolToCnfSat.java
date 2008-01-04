/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;

/**
 *
 * @author voronov
 */
public class ConverterBoolToCnfSat {
    
    /** Convert to CNF (conjunction of disjunctions)
     * All variables should be already represented as boolean literals
     */
    public static Expr convertAll(Expr n)
    {
        while(!isInCNF(n))
            n = convert(n);
        return n;
    }
    /** Convert to CNF (conjunction of disjunctions)
     * All variables should be already represented as boolean literals
     */
    private static Expr convert(Expr node)           
    {
        switch(node.type)
        {
            case NOT:
                return toCNFPushNegationDown((Not)node);
            case OR:
                return toCNFPushDisjunctionDown((Or)node);
            case AND:
                Expr left  = ((And)node).left;
                Expr right = ((And)node).right;
                return new And(convert(left), convert(right));
            case MAND:
                mAnd ma = new mAnd();
                for(Expr n1: (mAnd)node)
                    ma.add(convert(n1));
                return ma;
            case LIT:
                return node;
            case MOR:
                if(isInCNF(node))
                    return node;
                else
                    throw new IllegalArgumentException(
                            "can't convert mOr that is not in cnf");
                    //return toCNFPushDisjunctionDown((mOr)node);
            default:                    
                throw new IllegalArgumentException(
                        "Illegal (non-cnf?) node type: " 
                        + node.type.toString());
        }
    }
    
    
    /**
     * !(a&b) = !a | !b
     * !(a|b) = !a & !b
     */
    static Expr toCNFPushNegationDown(Not node)
    {
        Expr c = node.child;
        switch(c.type)
        {
            case AND:
                And a = (And)c;
                return (
                        new Or( 
                            new Not(a.left), new Not(a.right) 
                        ));                
            case OR:                
                Or o = (Or)c;
                return (
                        new And(
                            new Not(o.left), new Not(o.right)
                        ));                
            case MOR:
                mAnd ma = new mAnd();
                for(Expr n1: (mOr)c)
                    ma.add(new Not(n1));
                return ma;
            case MAND:
                mOr mo = new mOr();
                for(Expr n1: (mAnd)c){
                    //Expr t = toCNFPushNegationDown(new Not(n1));
                    //System.err.println(PrinterInfix.print(t));
                    //System.err.println();
                    //mo.add(t);
                    mo.add(new Not(n1));
                }
                return mo;            
            case NOT:
                Not n = (Not)c;
                return (n.child);
            case LIT:
                Literal l = (Literal)c;
                return new Literal(l.variable, !l.isPositive);
            default:
                throw new IllegalArgumentException(
                        "Illegal (non-cnf?) node type: " 
                        + node.type.toString());

        }        
    }
    
    /** (a&b)|c = (a|c)&(b|c) */
    static Expr toCNFPushDisjunctionDown(Or node)
    {
        Expr left  = node.left;
        Expr right = node.right;
        if(left.type.equals(ExprType.AND))
        {
            // (a&b)|c = (a|c)&(b|c)
            Expr lefta = ((And)left).left;
            Expr leftb = ((And)left).right;
            return (
                    new And(
                        new Or(lefta, right),
                        new Or(leftb, right)
                    ));
        }
        else if(right.type.equals(ExprType.AND))
        {
            // c|(a&b) = (c|a)&(c|b)
            Expr a = ((And)right).left;
            Expr b = ((And)right).right;
            return (
                    new And(
                        new Or(left, a),
                        new Or(left, b)
                    ));
        }
        else
        {
            return new Or(convert(left), convert(right));
        }                            
    }
    
    public static boolean isInCNF(Expr n){
        return isInCNF(n, false);
    }
    private static boolean isInCNF(Expr n, boolean seenOr)
    {
        switch(n.type)
        {
        case LIT:
            return true;
        case AND:
            And a = (And)n;
            return !seenOr && 
                    isInCNF(a.left, seenOr) && 
                    isInCNF(a.right, seenOr);
        case MAND:
            if(seenOr)
                return false;
            for(Expr n1: (mAnd)n)
                if(!isInCNF(n1, seenOr))
                    return false;
            return true;
        case OR:
            Or o = (Or)n;
            return isInCNF(o.left, true) && 
                    isInCNF(o.right, true);
        case MOR:
            for(Expr n1: (mOr)n)
                if(!isInCNF(n1, true))
                    return false;
            return true;                
        default:
            return false;
        }
    }
    /** (a&b)|c = (a|c)&(b|c) */
    /* a|b|(d&e)|f = a|b|d|f & a|b|e|f  */
    static Expr toCNFPushDisjunctionDown(mOr node)
    {
        //Expr and = null;
        mOr or = new mOr();
        for(Expr e: node){
            //if(and!=null)
            //    or.add(e);
            //else
                switch(e.type){
                    case AND: 
                    case MAND:
                        throw new IllegalArgumentException(
                            "can't convert conjunction inside of mOr");
                        //and = e;
                        //break;
                    case OR:
                        or.add(((Or)e).left);
                        or.add(((Or)e).right);
                        break;
                    case MOR:
                        for(Expr e2: (mOr)e)
                            or.add(e2);
                        break;
                    case LIT:
                        or.add(e);
                        break;
                    case NOT:
                        or.add(toCNFPushNegationDown((Not)e));
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Illegal node type: " 
                                + node.type.toString());                        
                }
        }
        //if(and==null)
            return or;
        /*else if(or.childs.size()<1)
            return and;
        else 
        switch(and.type){
            case AND:
                mOr or2 = new mOr(or);
                or.add( ((And)and).left );
                or2.add( ((And)and).right );
                return new And(or, or2);                
            case MAND:
                mAnd resAnd = new mAnd();
                for(Expr ea: (mAnd)and){
                    mOr or3 = new mOr(or);
                    or3.add(ea);
                    resAnd.add(or3);
                }
                return resAnd;
            default:
                throw new IllegalArgumentException(
                        "Illegal (not And or mAnd) node type: " 
                        + node.type.toString());                        
        } */           
    }
    
}
