/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;
import org.supremica.automata.SAT.expr.Expr.ExprType;
/**
 *
 * @author voronov
 */
public class ConverterBoolToCnfStruct {
    public Environment env;
    public ConverterBoolToCnfStruct(Environment iEnv)
    {
        env = iEnv;
    }
    
    public Expr toCnf(Expr f)
    {
        Variable v = env.vars.get(env.addBool());
        Literal l = new Literal(v, true);
        return toCnf(new And(f, l), v);
    }
    public Expr toCnf(Expr f, Variable vf)
    {
        if(atomic(f))
            return clause(vf, f);
        Expr c1, c2, l,r;
        Variable vl, vr;
        switch(f.type)
        {
        case AND:
            l = ((And)f).left;
            r = ((And)f).right;
            vl = env.vars.get(env.addBool());
            vr = env.vars.get(env.addBool());
            c1 = toCnf(l, vl);
            c2 = toCnf(r, vr);
            return new And(
                    clause(vf, new And(
                        new Literal(vl, true),
                        new Literal(vr, true))
                    ), 
                    new And(c1,c2));
        case OR:
            l = ((Or)f).left;
            r = ((Or)f).right;
            vl = env.vars.get(env.addBool());
            vr = env.vars.get(env.addBool());
            c1 = toCnf(l, vl);
            c2 = toCnf(r, vr);
            return new And(
                    clause(vf, new Or(
                        new Literal(vl, true),
                        new Literal(vr, true))
                    ), 
                    new And(c1,c2));
        case NOT:
            l = ((Not)f).child;
            vl = env.vars.get(env.addBool());
            c1 = toCnf(l, vl);
            return new And(
                    clause(vf, new Not(
                        new Literal(vl, true))
                    ), 
                    c1);
        default: 
            throw new IllegalArgumentException("unrecognized node type");
            
        }
    }
    
    
    public Expr clause(Variable v, Expr f)
    {
        Literal la, lna;
        la = new Literal(v, true);
        lna = new Literal(v, false);
        return ConverterBoolToCnfSat.convertAll(new And(
                    new Or(lna, f),
                    new Or(la, new Not(f))));        
    }
    
    public Expr convert2(Expr node)
    {
        Variable v, vl, vr;
        Expr l, r, al, ar;
        switch(node.type)
        {
        case NOT:
            v = env.vars.get(env.addBool());
            return new And(
                    new Literal(v, false),
                    assign2(v, ((Not)node).child));
        case AND:
            l  = ((And)node).left;
            r  = ((And)node).right;
            vl = env.vars.get(env.addBool());
            al = assign2(vl, l);
            vr = env.vars.get(env.addBool());
            ar = assign2(vr, r);
            return new And(
                    new And(al, ar), 
                    new And(
                        new Literal(vl, true), 
                        new Literal(vr, true))
                    );            
        case OR:
            l = ((Or)node).left;
            r = ((Or)node).right;
            return new Not(new And(new Not(l), new Not(r)));
            
        default:
            throw new IllegalArgumentException("unrecognized node type");
        }
                
    }
    
    private Expr mkNot(Expr n)
    {
        if(n.type==ExprType.LIT)
            return new Literal(((Literal)n).variable, !((Literal)n).isPositive);
        else 
            return new Not(n);
    }

    private Expr assign2(Variable v, Expr node)
    {
        Variable v1;
        switch(node.type)
        {
        case NOT:
            v1 = env.vars.get(env.addBool());
            return new And(
                    new Literal(v1, false),
                    assign2(v1, convert2(((Not)node).child))
                    );
        default:
            throw new IllegalArgumentException("later");
        }
    }
            
    public Expr convert(Expr node)
    {
        Variable v, va, vb, vc;
        Expr c, cb,cc, c1,c2,c3;
        Literal la, lna, lb, lnb, lc, lnc;
        switch(node.type)
        {
            case NOT:
                va = env.vars.get(env.addBool());
                vb = env.vars.get(env.addBool());

                cb = convert(((Not)node).child);
                
                la  = new Literal(va, true);
                lna = new Literal(va, false);
                lb  = new Literal(vb, true);
                lnb = new Literal(vb, false);
                
                c1 = new Or(lna, lb);
                c2 = new Or(la, lnb);
                c  = new And(c1,c2);
                
                return new And(c, cb);
            case OR:
                va = env.vars.get(env.addBool());
                vb = env.vars.get(env.addBool());
                vc = env.vars.get(env.addBool());
                
                cb = convert(((Or)node).left);
                cc = convert(((Or)node).right);
                
                la  = new Literal(va, true);
                lna = new Literal(va, false);
                lb  = new Literal(vb, true);
                lnb = new Literal(vb, false);
                lc  = new Literal(vc, true);
                lnc = new Literal(vc, false);
                
                c1 = new Or(lna, new Or(lb,lc));
                c2 = new Or(la, lnb);
                c3 = new Or(la, lnc);
                c =  new And(c1, new And(c2, c3));
                return new And(la, new And(c, new And(cb,cc)));
            case AND:
                va = env.vars.get(env.addBool());
                vb = env.vars.get(env.addBool());
                vc = env.vars.get(env.addBool());
                
                cb = convert(((And)node).left);
                cc = convert(((And)node).right);
                
                la  = new Literal(va, true);
                lna = new Literal(va, false);
                lb  = new Literal(vb, true);
                lnb = new Literal(vb, false);
                lc  = new Literal(vc, true);
                lnc = new Literal(vc, false);
                
                c1 = new Or(la, new Or(lnb,lnc));
                c2 = new Or(lna, lb);
                c3 = new Or(lna, lc);
                c =  new And(c1, new And(c2, c3));
                return new And(la, new And(c, new And(cb,cc)));
            case LIT:
                return node;
            default:                    
                throw new IllegalArgumentException("Illegal (non-cnf?) node type");                
        }
    }
    
    public Expr assign(Expr node, Variable v)
    {
        if(atomic(node))
            return clauseEq(v, node);
        
        Variable va, vb, vc;
        Expr a, b, c, cb,cc, c1,c2,c3, l,r;
        Literal la, lna, lb, lnb, lc, lnc;
        switch(node.type)
        {
            case OR:
                l = ((Or)node).left;
                r = ((Or)node).right;
                
                b = l.type==ExprType.LIT ? l:
                    assign(l, env.vars.get(env.addBool()));
                c = r.type==ExprType.LIT ? r:
                    assign(r, env.vars.get(env.addBool()));
                vb = env.vars.get(env.addBool());
                vc = env.vars.get(env.addBool());
                               
                a = clauseEq(v, 
                        new Or(
                            new Literal(vb, true), 
                            new Literal(vc, true)
                        ));
                return new And(a, new And(b, c));
            case AND:
                vb = env.vars.get(env.addBool());
                vc = env.vars.get(env.addBool());
                
                b = assign(((Or)node).left,  vb);
                c = assign(((Or)node).right, vc);
                
                a = clauseEq(v, 
                        new And(
                            new Literal(vb, true), 
                            new Literal(vc, true)
                        ));
                return new And(a, new And(b, c));
            case NOT:
                vb = env.vars.get(env.addBool());
                b = assign(((Not)node).child, vb);
                
                a = clauseEq(v, new Literal(vb, false));
                return new And(a, b);
            case LIT:
                return node;
            default:                    
                throw new IllegalArgumentException("Illegal (non-cnf?) node type");                
        }
        
    }
    private boolean atomic(Expr node)
    {
        switch(node.type)
        {
        case NOT:
            return (((Not)node).child).type == ExprType.LIT;
        case AND:
            return ((((And)node).left).type == ExprType.LIT) &&
                    ((((And)node).right).type == ExprType.LIT);
        case OR:
            return ((((Or)node).left).type == ExprType.LIT) &&
                    ((((Or)node).right).type == ExprType.LIT);
        case LIT:
            return true;
        default: 
            throw new IllegalArgumentException("Illegal (non-cnf) node type");                     
        }
        
    }
    private Expr clauseEq(Variable va, Expr node)
    {
        Variable vb, vc;
        Literal a, b, c, na, nb, nc;
        switch(node.type)
        {
        case NOT:
            vb = ((Literal)((Not)node).child).variable;
            
            a  = new Literal(va, true);
            na = new Literal(va, false);

            b  = new Literal(vb, true);
            nb = new Literal(vb, false);
            return new And(
                    new Or(na, nb),
                    new Or(a, b));
        case AND:
            vb = ((Literal)((And)node).left).variable;
            vc = ((Literal)((And)node).right).variable;

            a  = new Literal(va, true);
            na = new Literal(va, false);

            b  = new Literal(vb, true);
            nb = new Literal(vb, false);

            c  = new Literal(vb, true);
            nc = new Literal(vb, false);
            
            
            
            return new And( new And(
                    new Or(new Or(a,nb),nc), 
                    new Or(na, b)),
                    new Or(na,c)
                    );
        case OR:
            vb = ((Literal)((Or)node).left).variable;
            vc = ((Literal)((Or)node).right).variable;

            a  = new Literal(va, true);
            na = new Literal(va, false);

            b  = new Literal(vb, true);
            nb = new Literal(vb, false);

            c  = new Literal(vb, true);
            nc = new Literal(vb, false);
            
            
            
            return new And( new And(
                    new Or(new Or(na,b),c), 
                    new Or(a, nb)),
                    new Or(a, nc)
                    );
        default:
            throw new IllegalArgumentException("unrecognized node type");
        }
    }

}
