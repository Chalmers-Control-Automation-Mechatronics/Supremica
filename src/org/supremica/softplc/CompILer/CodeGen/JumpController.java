package org.supremica.softplc.CompILer.CodeGen;

import java.util.*;
import de.fub.bytecode.generic.*;

/**
 * this class handles labels and jump operations during the bytecode generation.
 * Since we have to know where labels are positioned to be able to jump to them 
 * there position in the bytecode is stored in an object of this kind during the
 * code generation process.
 * Also bytecode jumpoperations are stored until the corresponding label's position
 * is known. There after the branch/jump target is set.
 * @author Anders Röding
 */
public class JumpController
{
    Hashtable table = new Hashtable();

    /**
     * adds a new target instruction corresponding to a specific label
     * @param label the label string
     * @param target the target instruction corresponding to the label
     */
    public void addTarget(String label, InstructionHandle target)
    {
	if (table.containsKey(label))
	    {
		((JumpsAndTargetHolder) table.get(label)).addTarget(target);
	    }
	else
	    {
		JumpsAndTargetHolder jt = new JumpsAndTargetHolder();
		
		jt.addTarget(target);
		table.put(label, jt);
	    }
    }

    /**
     * adds a new jump instruction
     * @param label the target label
     * @param jump reference to the jump instruction
     */
    public void addJump(String label, BranchHandle jump)
    {
	if (table.containsKey(label))
	    {
		((JumpsAndTargetHolder) table.get(label)).addJump(jump);
	    }
	else
	    {
		JumpsAndTargetHolder jt = new JumpsAndTargetHolder();
		
		jt.addJump(jump);
		table.put(label, jt);
	    }
    }
}

/**
 * holds a target instruction and a list of corresponding jump operations,
 * corresponding to a specific label
 */
class JumpsAndTargetHolder
{
	private InstructionHandle trget = null;
	private List jumps = new LinkedList();

    /**
     * adds a target
     * @param target a jump target reference
     */
    public void addTarget(InstructionHandle target)
    {
	if (trget != null)
		{
		    System.err.println("Cannot set target for jump twice");
		}
	else if (target != null)
	    {
		trget = target;
		
		// System.err.println(trget);
		for (Iterator i = jumps.iterator(); i.hasNext(); )
		    {
			((BranchHandle) i.next()).setTarget(trget);
		    }
	    }
    }
    
    /**
     * adds a jump operation
     * @param jump a jump instruction reference
     */
    public void addJump(BranchHandle jump)
	{
		if (trget != null)
		{
			jump.setTarget(trget);
		}
		else
		{
			jumps.add(jump);
		}
	}
}
