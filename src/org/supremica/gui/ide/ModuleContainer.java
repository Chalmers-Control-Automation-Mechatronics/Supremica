package org.supremica.gui.ide;

import java.util.*;
import javax.swing.JComponent;
import net.sourceforge.waters.model.module.ModuleProxy;

class ModuleContainer
{
	private IDE ide;
	private final ModuleProxy module;
	private EditorEventsPanel editorEventsPanel = null;
	private EditorComponentsPanel editorComponentsPanel = null;
	private EditorAliasesPanel editorAliasesPanel = null;
	private EditorParametersPanel editorParametersPanel = null;

	ModuleContainer(IDE ide, ModuleProxy module)
	{
		this.ide = ide;
		this.module = module;
	}

	public String getName()
	{
		return module.getName();
	}

	public ModuleProxy getModuleProxy()
	{
		return module;
	}

	public JComponent getEventsPanel()
	{
		if (editorEventsPanel == null)
		{
			editorEventsPanel = new EditorEventsPanel(ide, this, "Events");
		}
		return editorEventsPanel;
	}

	public JComponent getAliasesPanel()
	{
		if (editorAliasesPanel == null)
		{
			editorAliasesPanel = new EditorAliasesPanel(ide, this, "Aliases");
		}
		return editorAliasesPanel;
	}

	public JComponent getComponentsPanel()
	{
		if (editorComponentsPanel == null)
		{
			editorComponentsPanel = new EditorComponentsPanel(ide, this, "Components");
		}
		return editorComponentsPanel;
	}

	public JComponent getParametersPanel()
	{
		if (editorParametersPanel == null)
		{
			editorParametersPanel = new EditorParametersPanel(ide, this, "Parameters");
		}
		return editorParametersPanel;
	}
}