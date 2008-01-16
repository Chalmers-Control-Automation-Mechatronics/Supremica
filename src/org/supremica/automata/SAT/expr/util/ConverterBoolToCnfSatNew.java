/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import java.util.ArrayList;
import  org.supremica.automata.SAT.expr.*;
import  org.supremica.automata.SAT.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;

/**
 *
 * @author voronov
 */
public class ConverterBoolToCnfSatNew {
    
    /** Convert to CNF (conjunction of disjunctions)
     * All variables should be already represented as boolean literals
     */
    /** Convert to CNF (conjunction of disjunctions)
     * All variables should be already represented as boolean literals
     */
    public static Expr convert(Expr node)           
    {
        switch(node.type)
        {
            case NOT:
                return convert(pushNegationDown(((Not)node).child));
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
            case OR:
                return toCNFPushDisjunctionDown((Or)node);
            case MOR:
                return toCNFPushDisjunctionDown((mOr)node);
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
    static Expr pushNegationDown(Expr c)
    {
        //Expr c = node.child;
        switch(c.type)
        {
            case AND:
                And a = (And)c;
                return (
                        new Or( 
                            pushNegationDown(a.left), 
                            pushNegationDown(a.right) 
                         
                        ));                
            case OR:                
                Or o = (Or)c;
                return (
                        new And(
                            pushNegationDown(o.left), 
                            pushNegationDown(o.right)
                        
                        ));                
            case MOR:
                mAnd ma = new mAnd();
                for(Expr n1: (mOr)c)
                    ma.add(pushNegationDown(n1));
                return ma;
            case MAND:
                mOr mo = new mOr();
                for(Expr n1: (mAnd)c)
                    mo.add(pushNegationDown(n1));                
                return mo;            
            case NOT:
                return (((Not)c).child);
            case LIT:
                Literal l = (Literal)c;
                return new Literal(l.variable, !l.isPositive);
            default:
                throw new IllegalArgumentException(
                        "Illegal child node type: " 
                        + c.type.toString());

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
        ArrayList<Expr> ands = new ArrayList<Expr>();
        mOr ors = new mOr();
        for(Expr e: node){           
            switch(e.type){
                case MOR:
                    for(Expr e1: (mOr)e)
                        ors.add(e1);
                    break;
                case AND:
                    mAnd a1 = new mAnd();
                    a1.add(((And)e).left);
                    a1.add(((And)e).right);
                    ands.add(a1);
                    break;
                case MAND:
                    ands.add(e);
                    break;
                case OR:
                    ors.add(((Or)e).left);
                    ors.add(((Or)e).right);
                    break;
                case LIT:
                    ors.add(e);
                    break;
                case NOT:
                    //ors.add(pushNegationDown(((Not)e).child));
                    throw new IllegalArgumentException(
                            "Unexpected NOT. removeNegations first");
                default:
                    throw new IllegalArgumentException(
                            "Illegal node type: " 
                            + e.type.toString());                        
            }
        }
        if(ors.childs.size()>0)
            ands.add(ors);
        ArrayList<ArrayList<Expr>> fullList = new ArrayList<ArrayList<Expr>>();
        for(Expr e: ands){
            ArrayList<Expr> smallList = new ArrayList<Expr>();
            switch(e.type){
                case MAND:
                    for(Expr e1: (mAnd)e)
                        smallList.add(e1);
                    break;
                    
                case MOR:
                    smallList.add(e);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Illegal node type: " 
                            + node.type.toString());                        
            }
            fullList.add(smallList);
        }
        ArrayList<ArrayList<Expr>> resList = permutes(fullList);
        mAnd res = new mAnd();
        for(ArrayList<Expr> smallList: resList){
            mOr o = new mOr();
            for(Expr e: smallList)
                o.add(e);
            if(o.childs.size()>0)
                res.add(o);
        }

        return res;
    }
    
    public static ArrayList<ArrayList<Expr>> permutes(
            ArrayList<ArrayList<Expr>> source){
        
        ArrayList<ArrayList<Expr>> res = new ArrayList<ArrayList<Expr>>();
        ArrayList<Expr> lead = new ArrayList<Expr>();
        if(source.size()>0){
            ArrayList<Expr> cur = source.remove(0);//source.get(0);
            for(Expr e: cur){
                lead.add(e);
            }
            
            ArrayList<ArrayList<Expr>> next = permutes(source);
            for(Expr leadElem: lead){
                if(next.size()<1){
                    ArrayList<Expr> resElem = new ArrayList<Expr>();
                    resElem.add(leadElem);
                    res.add(resElem);                    
                } else for(ArrayList<Expr> nextElem: next){
                    ArrayList<Expr> resElem = new ArrayList<Expr>();
                    resElem.add(leadElem);
                    resElem.addAll(nextElem);
                    res.add(resElem);
                }
            }            
        }
        return res;                
    }
}
