
package org.supremica.util.BDD;

/**
 * Decides if a BDDAutomaton is Plant or Supervisor/Spec
 */


public class AutomatonTypeMembership implements GroupMembership {
	private boolean plant;

	public AutomatonTypeMembership(boolean plant) {
		this.plant = plant;
	}

	public boolean shouldInclude(BDDAutomaton a) {
		if(plant) {
			return a.getType() == Automaton.TYPE_PLANT;
		} else {
			return  a.getType() == Automaton.TYPE_SPEC || a.getType() ==  Automaton.TYPE_SUPERVISOR;
		}
	}

}
