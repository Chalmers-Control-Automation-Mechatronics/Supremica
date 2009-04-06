package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.util.LinkedList;
import java.util.List;

public class VarList {

	private List<Var> vars;

	private VarList() {

		vars = new LinkedList<Var>();
	}

	public static VarList getInstance() {

		return new VarList();
	}

	// Duplicat variable names will be ignored
	public void append(VarList newVars) {

		if (newVars.getVars().isEmpty()) {

			return;
		} else if (vars.isEmpty()) {

			vars = newVars.getVars();
		} else {

			for (Var var : newVars.getVars()) {
				append(var);

			}

		}

	}

	
//	Duplicate element will be ignored
	public void append(Var inVar) {

		for (Var var : vars) {

			if (var.getName().equals(inVar.getName())) {

				return;
			}

		}

		vars.add(inVar);

	}

	public List<Var> getVars() {
		return vars;
	}

}