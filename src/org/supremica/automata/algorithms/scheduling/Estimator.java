
/******************* Estimator.java ***********************/
package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;

public interface Estimator
{

	// Assumptions about automata:
	//      * plants are the resources
	//      * specs are the product routes
	public Automata getAutomata();    // Return the stored automata   

	public int h(Element state);    // For this composite state, return an estimate
}
