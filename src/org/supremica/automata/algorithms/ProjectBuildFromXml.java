
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import java.io.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.supremica.automata.*;

public class ProjectBuildFromXml
	extends AutomataBuildFromXml
{
	private final static String projectStr = "SupremicaProject";
	private final static String layoutStr = "Layout";
	private final static String executionStr = "Execution";
	private final static String actionsStr = "Actions";
	private final static String actionStr = "Action";
	private final static String controlsStr = "Controls";
	private final static String controlStr = "Control";

	private Project currProject = null;

	public ProjectBuildFromXml() {}

	public Project buildProject(File file)
		throws Exception
	{
		return buildProject(file, false);
	}

	public Project buildProject(File file, boolean validate)
		throws Exception
	{
		return buildProject(file.getCanonicalPath(), validate);
	}

	public Project buildProject(String fileName)
		throws Exception
	{
		return buildProject(fileName, false);
	}

	public Project buildProject(InputStream is)
		throws Exception
	{
		return buildProject(is, false);
	}

	public Project buildProject(Reader r)
		throws Exception
	{
		return buildProject(r, false);
	}

	public Project buildProject(InputStream is, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(is);

		return buildProject(source, validate);
	}

	public Project buildProject(Reader r, boolean validate)
		throws Exception
	{
		InputSource source = new InputSource(r);

		return buildProject(source, validate);
	}

	public Project buildProject(String fileName, boolean validate)
		throws Exception
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setValidating(validate);

		SAXParser parser = parserFactory.newSAXParser();

		try
		{
			parser.parse(new File(fileName), this);
		}
		catch (SAXException ex)
		{
			throw new Exception(ex.getMessage());
		}

		return currProject;
	}

	public Project buildProject(InputSource is, boolean validate)
		throws Exception
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setValidating(validate);

		SAXParser parser = parserFactory.newSAXParser();

		try
		{
			parser.parse(is, this);
		}
		catch (SAXException ex)
		{
			throw new Exception(ex.getMessage());
		}

		return currProject;
	}

	public void startElement(String name, AttributeList attributes)
		throws SAXException
	{

		// in order of frequency
		if (transitionStr.equals(name))
		{
			doTransition(attributes);
		}
		else if (stateStr.equals(name))
		{
			doState(attributes);
		}
		else if (eventStr.equals(name))
		{
			doEvent(attributes);
		}
		else if (automatonStr.equals(name))
		{
			doAutomaton(attributes);
		}
		else if (projectStr.equals(name))
		{
			doProject(attributes);
		}
		else if (layoutStr.equals(name))
		{
			doLayout(attributes);
		}
		else if (executionStr.equals(name))
		{
			doExecution(attributes);
		}
		else if (actionsStr.equals(name))
		{
			doActions(attributes);
		}
		else if (actionStr.equals(name))
		{
			doAction(attributes);
		}
		else if (controlsStr.equals(name))
		{
			doControls(attributes);
		}
		else if (controlStr.equals(name))
		{
			doControl(attributes);
		}
		else if (eventsStr.equals(name)) {}
		else if (statesStr.equals(name)) {}
		else if (transitionsStr.equals(name)) {}
		else
		{
			throwException("Unknown element: " + name);
		}
	}

	public final void doProject(AttributeList attributes)
		throws SAXException
	{
		currProject = new Project();

		String name = attributes.getValue("name");

		if (name != null)
		{
			currAutomata.setName(name);
		}

		String owner = attributes.getValue("owner");

		if (name != null)
		{
			currAutomata.setOwner(owner);
		}

		String hash = attributes.getValue("hash");

		if (hash != null)
		{
			currAutomata.setHash(hash);
		}

		int majorVersion = 0;
		String majorStringVersion = attributes.getValue("major");

		if (majorStringVersion != null)
		{
			majorVersion = Integer.parseInt(majorStringVersion);
		}

		int minorVersion = 0;
		String minorStringVersion = attributes.getValue("minor");

		if (minorStringVersion != null)
		{
			minorVersion = Integer.parseInt(minorStringVersion);
		}

		if (majorVersion > 0)
		{
			throw new SAXException("Unsupported file format.");
		}

		if (minorVersion > 9)
		{
			throw new SAXException("Unsupported file format.");
		}
	}


	public final void doLayout(AttributeList attributes)
		throws SAXException
	{
		currProject = new Project();

		String name = attributes.getValue("name");

		if (name != null)
		{
			currAutomata.setName(name);
		}

		String owner = attributes.getValue("owner");

		if (name != null)
		{
			currAutomata.setOwner(owner);
		}

		String hash = attributes.getValue("hash");

		if (hash != null)
		{
			currAutomata.setHash(hash);
		}

		int majorVersion = 0;
		String majorStringVersion = attributes.getValue("major");

		if (majorStringVersion != null)
		{
			majorVersion = Integer.parseInt(majorStringVersion);
		}

		int minorVersion = 0;
		String minorStringVersion = attributes.getValue("minor");

		if (minorStringVersion != null)
		{
			minorVersion = Integer.parseInt(minorStringVersion);
		}

		if (majorVersion > 0)
		{
			throw new SAXException("Unsupported file format.");
		}

		if (minorVersion > 9)
		{
			throw new SAXException("Unsupported file format.");
		}
	}

	public final void doExecution(AttributeList attributes)
		throws SAXException
	{
		currProject = new Project();

		String name = attributes.getValue("name");

		if (name != null)
		{
			currAutomata.setName(name);
		}

		String owner = attributes.getValue("owner");

		if (name != null)
		{
			currAutomata.setOwner(owner);
		}

		String hash = attributes.getValue("hash");

		if (hash != null)
		{
			currAutomata.setHash(hash);
		}

		int majorVersion = 0;
		String majorStringVersion = attributes.getValue("major");

		if (majorStringVersion != null)
		{
			majorVersion = Integer.parseInt(majorStringVersion);
		}

		int minorVersion = 0;
		String minorStringVersion = attributes.getValue("minor");

		if (minorStringVersion != null)
		{
			minorVersion = Integer.parseInt(minorStringVersion);
		}

		if (majorVersion > 0)
		{
			throw new SAXException("Unsupported file format.");
		}

		if (minorVersion > 9)
		{
			throw new SAXException("Unsupported file format.");
		}
	}
}
