/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;
import java.util.*;
//import org.supremica.automata.SAT.Node.*;

//import org.supremica.automata.SAT
/**
 *
 * @author voronov
 */
public class Environment {
    
    public List<Variable> vars;

    int[] assignment;
    
    public Map<String, Variable> nameToVar;
    
    Domain domainBool = Domain.BINARY();//new Domain(2);
    
    public Environment() {
        vars      = new ArrayList<Variable>();
        nameToVar = new HashMap<String, Variable>();
    }

    public int add(String iName, Domain iDomain)
    {
        if(nameToVar.containsKey(iName))
            return nameToVar.get(iName).id;
        
        int id = vars.size();
        Variable v = new Variable(iName,iDomain, id);
        vars.add(v);
        nameToVar.put(iName, v);
        //this is very ugly part, 
        //i hope it is not necessary, but i put it...
        //if(vars.indexOf(v)!=id){
            //v.id = vars.indexOf(v);
        //}
        if(!vars.get(v.id).equals(v)){
            throw new ArrayStoreException("element was stored with wrong id");
        }            
        return v.id;
    }
    public int addBool()
    {
        return add("b"+vars.size(),domainBool);
    }
    public void assign(Variable v, int value){
        if(assignment==null)
            assignment = new int[vars.size()];
        assignment[v.id] = value;
    }
    public int getValueFor(Variable v){
        return assignment[v.id];        
    }
}
