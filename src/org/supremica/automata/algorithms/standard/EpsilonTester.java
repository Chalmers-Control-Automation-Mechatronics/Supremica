
/*
 * Created on 2003-jul-24
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.supremica.automata.algorithms.standard;

import org.supremica.automata.*;

/**
 * @author knut
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

// Here we determine whether the passed event is epsilon or not
// Depending on the way we want to interpret epsilon, instantiate diferent objects
public interface EpsilonTester
{
	boolean isThisEpsilon(LabeledEvent event);

	// debug only
	String showWhatYouGot();

	// debug only
}
