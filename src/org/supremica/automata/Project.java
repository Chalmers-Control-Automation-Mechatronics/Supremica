
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
import org.supremica.gui.*;
import org.supremica.log.*;

/**
 * A set of Automata with common actions and an AutomatonContainer.
 * @see org.supremica.gui.AutomatonContainer
 */
public class Project
	extends Automata
{
	private static Logger logger = LoggerFactory.createLogger(Project.class);
	private AutomatonContainer theContainer = null;
	private Automata selectedAutomata = null;
	private String name = null;

	public Project()
	{
		this("");
	}

	public Project(String name)
	{
		theContainer = new AutomatonContainer();
		this.name = name;
	}

	public Project(Project otherProject)
	{
		super(otherProject);

		theContainer = new AutomatonContainer(otherProject.theContainer);

		try
		{
			theContainer.add(otherProject);
		}
		catch (Exception e)
		{
			logger.error("Error while copying project");
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public AutomatonContainer getAutomatonContainer()
	{
		return theContainer;
	}

	public void setSelectedAutomata(Automata theAutomata)
	{
		this.selectedAutomata = theAutomata;
	}

	public Automata getSelectedAutomata()
	{
		return selectedAutomata;
	}

	public void clearSelection()
	{
		selectedAutomata = null;
	}

	public boolean containsAutomaton(String name)
	{
		return false;
	}

	// What is this supposed to do!!?
	public void close() {}
}
