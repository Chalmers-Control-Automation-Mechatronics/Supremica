
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
package org.supremica.automata;

import java.util.*;
import java.net.URL;
import org.supremica.log.*;
import org.supremica.automata.execution.*;

/**
 * A project is a automata object together with actions and controls.
 * A Project might be encapulated in a gui.VisualProject object
 * if this is a graphical application.
 */
public class Project
	extends Automata
{
	private static Logger logger = LoggerFactory.createLogger(Project.class);
	private Actions theActions = null;
	private Controls theControls = null;
	private URL animationURL = null;

	public Project()
	{
		theActions = new Actions();
		theControls = new Controls();
	}

	public Project(String name)
	{
		this();
		setName(name);
	}

	public Project(Project otherProject)
	{
		super(otherProject);

		theActions = new Actions(otherProject.theActions);
		theControls = new Controls(otherProject.theControls);

		setName(otherProject.getName());
	}

	public Actions getActions()
	{
		return theActions;
	}

	public Controls getControls()
	{
		return theControls;
	}

	public boolean hasAnimation()
	{
		return animationURL != null;
		/*
		if (animationPath == null)
		{
			return false;
		}
		if (animationPath.equals(""))
		{
			return false;
		}
		return true;
		*/
	}

	public URL getAnimationURL()
	{
		return animationURL;
	}

	/**
	 * Set an absolute path
	 **/
	public void setAnimationURL(URL url)
	{
		animationURL = url;
	}

/*
	public InputProtocol getInputProtocol()
	{
		return inputProtocol;
	}

	public void setInputProtocol(InputProtocol theProtocol)
	{
		this.inputProtocol = theProtocol;
	}
*/

	public void addAttributes(Project otherProject)
	{
		addActions(otherProject.getActions());
		addControls(otherProject.getControls());
		setAnimationURL(otherProject.getAnimationURL());
	}


	private void addActions(Actions otherActions)
	{
		if (theActions == null)
		{
			theActions = new Actions();
		}
		theActions.addActions(otherActions);
		notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
	}

	private void addControls(Controls otherControls)
	{
		if (theControls == null)
		{
			theControls = new Controls();
		}
		theControls.addControls(otherControls);
		notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
	}

	public void clearActions()
	{
		if (theActions != null)
		{
			theActions.clear();
			notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
		}
	}

	public void clearControls()
	{
		if (theControls != null)
		{
			theControls.clear();
			notifyListeners(AutomataListeners.MODE_ACTIONS_OR_CONTROLS_CHANGED, null);
		}
	}

	public void clear()
	{
		super.clear();
		theActions.clear();
		theControls.clear();
	}

	public boolean equalProject(Project other)
	{
		if (!equalAutomata(other))
		{
			return false;
		}
		// Add more checks here
		return true;
	}
}
