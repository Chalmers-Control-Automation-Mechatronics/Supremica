package org.supremica.gui;

import java.awt.Cursor;
import org.supremica.automata.IO.AutomataSerializer;
import att.grappa.Graph;

public interface DotBuilderObserver
{
	public void setGraph(Graph theGraph);
}