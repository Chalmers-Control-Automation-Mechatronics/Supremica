package org.supremica.gui.ide;

import java.util.*;
import net.sourceforge.waters.model.module.ModuleProxy;

class ModuleContainers
{
	private IDE ide;
	private LinkedList moduleContainers = new LinkedList();
	private int newModuleCounter = 1;

	public ModuleContainers(IDE ide)
	{
		this.ide = ide;
		add(createNewModuleContainer());
	}

	public ModuleContainer getModuleContainer(String name)
	{
		for(Iterator modIt = moduleContainers.iterator(); modIt.hasNext(); )
		{
			ModuleContainer currModuleContainer = (ModuleContainer)modIt.next();
			if (name.equals(currModuleContainer.getName()))
			{
				return currModuleContainer;
			}
		}
		return null;
	}

	public void add(ModuleContainer moduleContainer)
	{
		moduleContainers.addFirst(moduleContainer);
		setActive(moduleContainer);
	}

	public void remove(ModuleContainer moduleContainer)
	{
		// The module container will at least contain one module
		if (moduleContainers.size() >= 2)
		{
			moduleContainers.remove(moduleContainer);
		}
	}

	void setActive(ModuleContainer moduleContainer)
	{

		if (getActiveModuleContainer() != moduleContainer)
		{
			remove(moduleContainer);
			add(moduleContainer);
		}
		ide.setTitle(ide.getIDEName() + " [" + moduleContainer.getName() + "]");
	}

	public ModuleContainer getActiveModuleContainer()
	{
		return (ModuleContainer)moduleContainers.getFirst();
	}

	public int size()
	{
		return moduleContainers.size();
	}

	public Iterator iterator()
	{
		return moduleContainers.iterator();
	}


	public String getNewModuleName(String prefix)
	{
		String nameSuggestion = prefix + newModuleCounter++;
		while (getModuleContainer(nameSuggestion) != null)
		{
			nameSuggestion = prefix + newModuleCounter++;
		}
		return nameSuggestion;

	}

	public ModuleContainer createNewModuleContainer()
	{
		ModuleProxy newModule = new ModuleProxy(getNewModuleName("Module"));
		ModuleContainer newModuleContainer = new ModuleContainer(ide, newModule);
		return newModuleContainer;
	}

}