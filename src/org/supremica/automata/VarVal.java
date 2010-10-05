/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata;

import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 *
 * @author Sajed, Alexey, Zhennan
 */
public class VarVal {

    VariableComponentProxy variable;
    IntConstantProxy value;

    public VarVal(VariableComponentProxy variable, IntConstantProxy value)
    {
        this.variable = variable;
        this.value = value;
    }

    public VariableComponentProxy getVariable()
    {
        return variable;
    }

    public IntConstantProxy getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return (variable.hashCode()+value.getValue());
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof VarVal)
        {
            VarVal vv = (VarVal)obj;
            return (vv.getVariable().equals(variable) && vv.getValue().equals(value));
        }
        else
        {
            return false;
        }
    }

}
