/********************* AutomatonViewerFactory.java *********************/
/* Factory for allowing VisualProject to create different flavors of
 * AutomatonViewers. Use your own personal favorite!
 */
package org.supremica.gui;

import org.supremica.automata.Automaton;
import org.supremica.gui.AutomatonViewer;

public interface AutomatonViewerFactory
{
	AutomatonViewer createAutomatonViewer(Automaton automaton) throws Exception;
}