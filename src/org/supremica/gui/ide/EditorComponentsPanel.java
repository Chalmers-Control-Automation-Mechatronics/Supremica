package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorComponentsPanel
	extends WhiteScrollPane
{
	private String title;
	private ModuleContainer moduleContainer;

	EditorComponentsPanel(ModuleContainer moduleContainer, String title)
	{
		this.moduleContainer = moduleContainer;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}