package org.supremica.gui.ide;

import org.supremica.gui.WhiteScrollPane;

class EditorAliasesPanel
	extends WhiteScrollPane
{
	private static final long serialVersionUID = 1L;

	private String name;

	EditorAliasesPanel(ModuleContainer moduleContainer, String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

}