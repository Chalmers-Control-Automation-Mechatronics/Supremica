/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr.util;
import org.supremica.automata.SAT.expr.*;
import org.supremica.automata.SAT.*;
import java.util.*;
/**
 *
 * @author voronov
 */
public class ConverterVarEqToBoolWithFactory {
    Environment envInt;
    Environment envBool;
    
    static ExprFactoryM exprFac = new ExprFactoryM();
    
    List<int[]> intIdToBoolId = new ArrayList<int[]>();
    
    public ConverterVarEqToBoolWithFactory(Environment ienv, Environment benv){
        if(ienv==null)
            throw new IllegalArgumentException("int env can't be null");
        if(benv==null)
            throw new IllegalArgumentException("bool env can't be null");
        envInt  = ienv;        
        envBool = benv;
        envIntToBool(ienv);
    }
    
    public Expr initConvert(Expr iNode)
    {
        if(iNode==null)
            throw new IllegalArgumentException("can't convert null node");
        //nodeBool = convert(addForbiddenByDomain(nodeInt, envInt));
        Expr fd = ForbiddenByDomain(envInt);
        return  (fd==null)? convert(iNode) : 
            exprFac.And(convert(iNode), convert(fd));
    }
    /*private static Expr addForbiddenByDomain(Expr n, Environment env){
        Expr res = n;
        for(Variable v: env.vars){
            int maxVal = 1 << v.domain.significantBits();
            for(int i = v.domain.size(); i < maxVal; i++)
                res = new And( res, (new Not(new VarEqInt(v, i, true))));
        }
        return res;
    }*/
    private Expr ForbiddenByDomain(Environment env){        
//        Expr res = null;
//        for(Variable v: env.vars){
//            if(v.domain.size()>2){ // this if should not be necessary...
//                int maxVal = 1 << v.domain.significantBits();
//                for(int i = v.domain.size(); i < maxVal; i++){
//                    // here!
//                    //Expr f = new Not(new VarEqInt(v, i, true));
//                    Expr f = /*NodeCnfConverter.convertAll*/(
//                            /*convert*/(exprFac.Not(exprFac.VarEqInt(v, i, true))));
//                    res = (res==null) ? f : (new And( res, f));
//                }
//            }
//        }
//        return res;        
        Expr res = exprFac.InitAnd();
        for(Variable v: env.vars){
            if(v.domain.size()>2){ // we forbid something only if domain is bigger than boolean
                int maxVal = 1 << v.domain.significantBits();
                for(int i = v.domain.size(); i < maxVal; i++){
                    Expr f = exprFac.Not(exprFac.VarEqInt(v, i, true));
                    res = exprFac.And(res, f);
                }
            }
        }
        return res;        
    }


    /**
     * This method has side effect setting intIdToBoolId 
     * @param envI
     */
    private void envIntToBool(Environment envI)
    {
        Environment envB = envBool;
        Domain boolDomain = Domain.BINARY();
        envB.add("dummy_index_0", boolDomain);
        for(Variable v: envI.vars){
            int[] varIds = new int[v.significantBits()];
            for(int bitIndex = 0; bitIndex < v.significantBits(); bitIndex++)
                varIds[bitIndex] = 
                        envB.add(v.Name + "_bit" + bitIndex, boolDomain);
            intIdToBoolId.add(varIds);
        }
    }
    
    public /*Environment*/void envIntFromBool()
    {
        for(Variable varInt: envInt.vars)
        {
            int value = 0;
            for(int bitIndex = 0; bitIndex < varInt.significantBits(); bitIndex++)
            {
                int idBool = intIdToBoolId.get(varInt.id)[bitIndex];
                Variable varBool = envBool.vars.get(idBool);
                value = value | ((1&envBool.getValueFor(varBool))<<bitIndex);
            }
            envInt.assign(varInt, value);
        }
  //      return envInt;
    }
    
/*    public Environment getBoolEnv(){
        return envBool;
    }
*/    
    public Expr convert(Expr n) {
        if(n==null)
            throw new IllegalArgumentException("can't convert null node");
        Expr l, r;
        switch(n.type){
        case AND:
            l = convert(((And)n).left);
            r = convert(((And)n).right);
            return exprFac.And(l,r);//return l==null?r:(r==null?l:new And(l,r));
        case OR:
            l = convert(((Or)n).left);
            r = convert(((Or)n).right);
            return exprFac.Or(l,r); //return l==null?r:(r==null?l:new Or(l,r));
        case MAND:
            Expr ma = exprFac.InitAnd();
            for(Expr n1: (mAnd)n)
                exprFac.And(ma, convert(n1));
            return ma;
        case MOR:
            Expr mo = exprFac.InitOr();
            for(Expr n1: (mOr)n)
                exprFac.Or(mo, convert(n1));
            return mo;
        case NOT:
            return exprFac.Not(convert(((Not)n).child));
        case LIT:
            return createLiteral(
                    ((Literal)n).variable, 
                    0, //if it is literal, it has only one bit
                    ((Literal)n).isPositive);
            //return n;
        case VAREQVAR:
            return toBoolExpr((VarEqVar)n);
        case VAREQINT:
            return toBoolExpr((VarEqInt)n);
        default:
            throw new IllegalArgumentException(
                    "Unrecognized node type: " + n.type.toString());
        }
    }   

    
    private Expr toBoolExpr(VarEqInt ve)
    {
        //Expr res = null; //getEqIntBitLiteral(ve, 0);
        //mAnd res = new mAnd();
        Expr res = exprFac.InitAnd();
        for(int i = 0; i < ve.variable.significantBits(); i++){
            Literal n = getEqIntBitLiteral(ve,i);
            res = exprFac.And(res, n);
            //res = (res==null) ? n : new And(n, res);
            //res.add(n);
        }
        return res;        
    }    
    private Expr toBoolExpr(VarEqVar ve)
    {
        //Expr res = null; //getEqVarBitNode(ve, 0);
        //mAnd res = new mAnd();
        Expr res = exprFac.InitAnd();
        for(int i = 0; i < ve.var1.significantBits(); i++){
            Expr n = getEqVarBitNode(ve,i);
            res = exprFac.And(res, n);
            //res = (res==null) ? n : new And(n, res);
            //res.add(n);
        }
        return res;        
    }
    private Literal getEqIntBitLiteral(VarEqInt ve, int bitIndex){
        boolean p = 1==((ve.value >> bitIndex) & 1);
        return createLiteral(ve.variable, bitIndex, p);
    }    
    private Expr getEqVarBitNode(VarEqVar ve, int bitIndex)
    {
        /* a=b   <=>   (a&b)|(!a&!b)  <=>  (!a|b)&(a|!b)  */
        return exprFac.And(
                exprFac.Or(
                    createLiteral(ve.var1, bitIndex, false),
                    createLiteral(ve.var2, bitIndex, true)
                    ),
                exprFac.Or(
                    createLiteral(ve.var1, bitIndex, true),
                    createLiteral(ve.var2, bitIndex, false)
                    )
                );
    }
    private Expr getEqVarBitNodeForNegation(VarEqVar ve, int bitIndex)
    {
        /* a=b   <=>   (a&b)|(!a&!b)  <=>  (!a|b)&(a|!b)  */
        return exprFac.Or(
                exprFac.And(
                    createLiteral(ve.var1, bitIndex, true),
                    createLiteral(ve.var2, bitIndex, true)
                    ), 
                exprFac.And(
                    createLiteral(ve.var1, bitIndex, false),
                    createLiteral(ve.var2, bitIndex, false)                
                    )
                );
    }
    private Literal createLiteral(Variable v, int bitIndex, boolean isPositive){
        int[] ids = intIdToBoolId.get(v.id);
        if(ids==null)
            throw new IllegalArgumentException(
                    "can't find bit ids for variable " + v.Name + ", id:" +v.id);
        int id = ids[bitIndex];
        Variable bv = envBool.vars.get(id);
        if(bv==null)
            throw new IllegalArgumentException(
                    "can't find boolean variable with id " + id);        
        Literal l = new Literal(bv, isPositive);
        return l;
    }       
}
