/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata;

import java.util.Map;
import net.sourceforge.waters.model.module.NodeProxy;

/**
 *
 * @author sajed
 */
public class EFAState {
    
    private NodeProxy location;
    Map<String,Integer> var2val;
    
    public EFAState(NodeProxy location, Map<String,Integer> var2val){
        this.location = location;
        this.var2val = var2val;
    }

    public NodeProxy getLocation() {
        return location;
    }

    public Map<String, Integer> getVar2val() {
        return var2val;
    }

    public String getName()
    {
        String output = location.getName()+"[";
        for(String s:var2val.keySet())
            output += (s+"="+var2val.get(s)+",");
        output = output.substring(0,output.length()-1);
        output+= "]";
        return output;
    }
    
    @Override
    public int hashCode(){
        
        int hashcode = 0;
        for(String var: var2val.keySet()){
            hashcode += var2val.get(var);
        }
        return (location.hashCode() + hashcode);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EFAState) {
            EFAState es = (EFAState) obj;
            if (!es.location.equals(this.location)) {
                return false;
            } else {
                for(String var: var2val.keySet()){
                    if(!var2val.get(var).equals(es.getVar2val().get(var)))
                        return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }
}
