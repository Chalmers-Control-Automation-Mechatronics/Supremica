/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata;

import java.util.List;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

/**
 *
 * @author Sajed, Alexey, Zhennan
 */
public class GuardActionLoc {

    GuardActionBlockProxy guardActionBlock;
    NodeProxy location;
    List<SimpleExpressionProxy> guards;
    List<BinaryExpressionProxy> actions;

    public GuardActionLoc(GuardActionBlockProxy guardActionBlock, NodeProxy location)
    {
        this.guardActionBlock = guardActionBlock;
        this.guards = guardActionBlock.getGuards();
        this.actions = guardActionBlock.getActions();
        this.location = location;
    }

    public GuardActionBlockProxy getGuardActionBlock()
    {
        return guardActionBlock;
    }

    public NodeProxy getLocation()
    {
        return location;
    }

    public SimpleExpressionProxy getGuard()
    {
        if(guards.size() == 0)
            return null;
        else if(guards.size() > 1)
        {
            throw new IllegalArgumentException("Several guards in the block!");
        }
        else
            return guards.get(0);
    }

    public List<BinaryExpressionProxy> getActions()
    {
        return actions;
    }

}
