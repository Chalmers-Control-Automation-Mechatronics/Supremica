
package org.supremica.util.BDD;

/**
 * OOP when it really stinks :)
 * deiced whetger this Automaton should be included in this group
 */

public interface GroupMembership {
    public boolean shouldInclude(BDDAutomaton automaton);
}
