package org.supremica.softplc.CompILer.CodeGen;
import java.util.*;
import de.fub.bytecode.generic.*;

public class JumpController {
	Hashtable table = new Hashtable();

	public void addTarget(String label, InstructionHandle target) {
		if (table.containsKey(label)) {
			((JumpsAndTargetHolder)table.get(label)).addTarget(target);
		}
		else {
			JumpsAndTargetHolder jt = new JumpsAndTargetHolder();
			jt.addTarget(target);
			table.put(label,jt);
		}
	}

	public void addJump(String label, BranchHandle jump) {
		if (table.containsKey(label)) {
			((JumpsAndTargetHolder)table.get(label)).addJump(jump);
		}
		else {
			JumpsAndTargetHolder jt = new JumpsAndTargetHolder();
			jt.addJump(jump);
			table.put(label,jt);
		}
	}
}
 class JumpsAndTargetHolder {
		private InstructionHandle trget = null;
		private List jumps = new LinkedList();

		public void addTarget(InstructionHandle target) {
			if (trget != null) {
				System.err.println("Cannot set target for jump twice");
			}
			else if (target != null) {
				trget = target;
				//System.err.println(trget);
				for (Iterator i = jumps.iterator(); i.hasNext();) {
					((BranchHandle)i.next()).setTarget(trget);

				}
			}
		}

		public void addJump(BranchHandle jump) {
			if (trget != null) {
				jump.setTarget(trget);
			}
			else {
				jumps.add(jump);
			}
		}
	}

