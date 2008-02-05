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
public class ConverterVarEqToBoolLinear  implements IConverterVarEqToBool {
    Environment envInt;
    Environment envBool;
    
    List<int[]> intIdToBoolId = new ArrayList<int[]>();
    
    public ConverterVarEqToBoolLinear(Environment ienv, Environment benv){
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
        return  convert(iNode);
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
            int[] varIds = new int[v.domain.size()];
            for(int val = 0; val < v.domain.size(); val++)
                varIds[val] = 
                        envB.add(v.Name + "_eq" + val, boolDomain);
            intIdToBoolId.add(varIds);
        }
    }
    
    public /*Environment*/void envIntFromBool()
    {
        for(Variable varInt: envInt.vars)
        {
            int value = 0;
            for(int val = 0; val < varInt.domain.size(); val++){
                int idBool = intIdToBoolId.get(varInt.id)[val];            
                Variable varBool = envBool.vars.get(idBool);
                if(envBool.getValueFor(varBool)>0)
                    value = val;
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
            return new And(l,r);//return l==null?r:(r==null?l:new And(l,r));
        case OR:
            l = convert(((Or)n).left);
            r = convert(((Or)n).right);
            return new Or(l,r); //return l==null?r:(r==null?l:new Or(l,r));
        case MAND:
            mAnd ma = new mAnd();
            for(Expr n1: (mAnd)n)
                ma.add(convert(n1));
            return ma;
        case MOR:
            mOr mo = new mOr();
            for(Expr n1: (mOr)n)
                mo.add(convert(n1));
            return mo;
        case NOT:
            return new Not(convert(((Not)n).child));
        case LIT:
            return n;
//            return createLiteral(
//                    ((Literal)n).variable, 
//                    0, //if it is literal, it has only one bit
//                    ((Literal)n).isPositive);
//            //return n;
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
        Expr res = null; 
        for(int i = 0; i < ve.variable.domain.size(); i++){
            Expr n = createLiteral(ve.variable, ve.value, ve.value==i);            
            res = (res==null) ? n : new And(n, res);
        }
        return res;        
    }    
    private Expr toBoolExpr(VarEqVar ve)
    {
        Expr res = null;
        for(int i = 0; i < ve.var1.domain.size(); i++){
            Expr n = getEqVarBitNode(ve,i);
            res = (res==null) ? n : new And(n, res);
        }
        return res;        
    }
    private And getEqVarBitNode(VarEqVar ve, int bitIndex)
    {
        /* a=b   <=>   (a&b)|(!a&!b)  <=>  (!a|b)&(a|!b)  */
        return new And(
                new Or(
                    createLiteral(ve.var1, bitIndex, false),
                    createLiteral(ve.var2, bitIndex, true)
                    ),
                new Or(
                    createLiteral(ve.var1, bitIndex, true),
                    createLiteral(ve.var2, bitIndex, false)
                    )
                );
    }
    private Literal createLiteral(Variable v, int value, boolean isPositive){
        int[] ids = intIdToBoolId.get(v.id);
        if(ids==null)
            throw new IllegalArgumentException(
                    "can't find bit ids for variable " + v.Name + ", id:" +v.id);
        int id = ids[value];
        Variable bv = envBool.vars.get(id);
        if(bv==null)
            throw new IllegalArgumentException(
                    "can't find boolean variable with id " + id);        
        Literal l = new Literal(bv, isPositive);
        return l;
    }       
}
