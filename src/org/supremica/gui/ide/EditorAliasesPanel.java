package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;

class EditorAliasesPanel
	extends WhiteScrollPane
{
	private ModuleContainer moduleContainer;
	private String name;

	EditorAliasesPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

}