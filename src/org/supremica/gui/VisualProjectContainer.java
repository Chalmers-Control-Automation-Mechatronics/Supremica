/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;

public class VisualProjectContainer
{
	private static Logger logger = LoggerFactory.createLogger(VisualProjectContainer.class);
	private List theProjects;
	private VisualProject currentProject;
	private VisualProjectContainerListeners projectListeners = null;

	public VisualProjectContainer()
	{
		theProjects = new LinkedList();
	}

	public void addProject(VisualProject theProject)
	{
		addProject(theProject, false);
	}

	public void addProject(VisualProject theProject, boolean setActive)
	{
		theProjects.add(theProject);

		if (setActive)
		{
			currentProject = theProject;
		}
	}

	public void removeProject(VisualProject theProject)
	{
		theProjects.remove(theProject);

		if (currentProject == theProject)
		{
			currentProject = null;
		}
	}

	public VisualProject getProject(String name)
	{
		if (name == null)
		{
			return null;
		}

		for (Iterator projIt = iterator(); projIt.hasNext(); )
		{
			VisualProject currProject = (VisualProject) projIt.next();

			if (name.equals(currProject.getName()))
			{
				return currProject;
			}
		}

		return null;
	}

	public VisualProject getActiveProject()
	{
		return currentProject;
	}

	public void setActiveProject(VisualProject theProject)
	{
		this.currentProject = theProject;
	}

	public Iterator iterator()
	{
		return theProjects.iterator();
	}

	public String getUniqueProjectName()
	{    // Implement this
		return "Untitled";
	}

	public VisualProjectContainerListeners getListeners()
	{
		if (projectListeners == null)
		{
			projectListeners = new VisualProjectContainerListeners(this);
		}

		return projectListeners;
	}

	public void addListener(VisualProjectContainerListener listener)
	{
		Listeners currListeners = getListeners();

		currListeners.addListener(listener);
	}

	private void notifyListeners()
	{
		if (projectListeners != null)
		{
			projectListeners.notifyListeners();
		}
	}

	private void notifyListeners(int mode, Project p)
	{
		if (projectListeners != null)
		{
			projectListeners.notifyListeners(mode, p);
		}
	}

	public void beginTransaction()
	{
		if (projectListeners != null)
		{
			projectListeners.beginTransaction();
		}
	}

	public void endTransaction()
	{
		if (projectListeners != null)
		{
			projectListeners.endTransaction();
		}
	}
}
