package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorAliasesPanel
	extends WhiteScrollPane
{
	private ModuleContainer moduleContainer;
	private String title;

	EditorAliasesPanel(ModuleContainer moduleContainer, String title)
	{
		this.moduleContainer = moduleContainer;
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

}