//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainers
//###########################################################################
//# $Id: ModuleContainers.java,v 1.4 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;


public class ModuleContainers
{
	private IDE ide;
	private List<ModuleContainer> moduleContainers =
		new LinkedList<ModuleContainer>();
	private ModuleContainer activeModuleContainer = null;

	private int newModuleCounter = 1;

	public ModuleContainers(IDE ide)
	{
		this.ide = ide;
	}

	public ModuleContainer getModuleContainer(final String name)
	{
		for (final ModuleContainer currModuleContainer : moduleContainers) {
			if (name.equals(currModuleContainer.getName()))	{
				return currModuleContainer;
			}
		}
		return null;
	}

	public void add(ModuleContainer moduleContainer)
	{
		moduleContainers.add(moduleContainer);
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
		return (ModuleContainer) moduleContainers.get(0);
	}


	public ModuleContainer getLast()
	{
		final int index = moduleContainers.size() - 1;
		return (ModuleContainer) moduleContainers.get(index);
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


	public Iterator<ModuleContainer> iterator()
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
		final String name = getNewModuleName("Module");
		final Collection<ParameterProxy> pl = Collections.emptyList();
		final Collection<AliasProxy> al = Collections.emptyList();
		final Collection<EventDeclProxy> el = Collections.emptyList();
		final Collection<Proxy> prl = Collections.emptyList();
		final ModuleSubject newModule =
			new ModuleSubject(name, null, pl, al, el, prl, prl);
		final ModuleContainer newModuleContainer =
			new ModuleContainer(ide, newModule);
		return newModuleContainer;
	}

}
