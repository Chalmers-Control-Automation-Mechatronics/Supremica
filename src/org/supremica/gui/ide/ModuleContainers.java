package org.supremica.gui.ide;

import java.util.*;
import net.sourceforge.waters.model.module.ModuleProxy;

public class ModuleContainers
{
	private IDE ide;
	private LinkedList moduleContainers = new LinkedList();
	private ModuleContainer activeModuleContainer = null;

	private int newModuleCounter = 1;

	public ModuleContainers(IDE ide)
	{
		this.ide = ide;
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
		moduleContainers.addLast(moduleContainer);
	}

	public void remove(ModuleContainer moduleContainer)
	{
		moduleContainers.remove(moduleContainer);
		if (moduleContainer == activeModuleContainer)
		{
			activeModuleContainer = null;
		}
	}

	void setActive(ModuleContainer moduleContainer)
	{

		if (getActiveModuleContainer() != moduleContainer)
		{
			activeModuleContainer = moduleContainer;
		}
		ide.setTitle(ide.getName() + " [" + moduleContainer.getName() + "]");
	}

	public ModuleContainer getActiveModuleContainer()
	{
		return activeModuleContainer;
	}

	public ModuleContainer getFirst()
	{
		return (ModuleContainer)moduleContainers.getFirst();
	}

	public ModuleContainer getLast()
	{
		return (ModuleContainer)moduleContainers.getLast();
	}

	public ModuleContainer getNext(ModuleContainer moduleContainer)
	{
		int moduleIndex = moduleContainers.indexOf(moduleContainer);
		int nextModuleIndex = moduleIndex + 1;
		if (nextModuleIndex == size())
		{
			nextModuleIndex = 0;
		}
		if (size() >= 1)
		{
			return (ModuleContainer)moduleContainers.get(nextModuleIndex);
		}
		return null;
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