/***************** DefaultAutomatonViewerFactory,java ******************/
/* The default implementation of the AutomatonViewerFactory. Simply
 * creates a plain AutomatonViewer
 */
package org.supremica.gui;

import org.supremica.automata.Automaton;
import org.supremica.gui.AutomatonViewerFactory;

public class DefaultAutomatonViewerFactory
	implements AutomatonViewerFactory
{
	public AutomatonViewer createAutomatonViewer(Automaton automaton)
		throws Exception
	{
		return new AutomatonViewer(automaton);
	}
}