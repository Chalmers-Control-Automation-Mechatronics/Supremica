package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorComponentsPanel
	extends WhiteScrollPane
{
	private String name;
	private ModuleContainer moduleContainer;

	EditorComponentsPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

}