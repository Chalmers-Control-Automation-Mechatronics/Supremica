
/************************ Manipulator.java ******************/

// Implementations of the Manipulator interface determine whether to discard the new element or the old ones
// manpulator::manipulate should return true if the new element (elem) should be added by the Structure
// Why a specific class/interface for this?
// We're not sure exactly by what criteria to determine which element to keep.
// Manipulators let us easily experiment
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.OrderedSetIterator;

interface Manipulator
{
	public boolean manipulate(Element elem, OrderedSetIterator begin, OrderedSetIterator beyond, Structure struct);
}
