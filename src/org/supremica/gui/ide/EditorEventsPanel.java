package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorEventsPanel
	extends WhiteScrollPane
{
	private String name;
	private ModuleContainer moduleContainer;

	EditorEventsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
	}

	public String getName()
	{
		//System.err.println("getTitle: " + title);
		return name;
	}

}