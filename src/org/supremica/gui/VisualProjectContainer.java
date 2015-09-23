//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;

public class VisualProjectContainer
{
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(VisualProjectContainer.class);
    private List<VisualProject> theProjects;
    private VisualProject currentProject;
    private VisualProjectContainerListeners projectListeners = null;

    public VisualProjectContainer()
    {
        theProjects = new LinkedList<VisualProject>();
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

        for (Iterator<VisualProject> projIt = iterator(); projIt.hasNext(); )
        {
            VisualProject currProject = projIt.next();

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

    public Iterator<VisualProject> iterator()
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

    @SuppressWarnings("unused")
	private void notifyListeners()
    {
        if (projectListeners != null)
        {
            projectListeners.notifyListeners();
        }
    }

    @SuppressWarnings("unused")
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
