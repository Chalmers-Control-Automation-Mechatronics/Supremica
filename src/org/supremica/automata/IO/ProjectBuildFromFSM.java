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
package org.supremica.automata.IO;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.supremica.automata.*;
import org.supremica.automata.execution.*;
import org.supremica.log.*;

public class ProjectBuildFromFSM
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromFSM.class);

	private ProjectFactory theProjectFactory = null;
	private Project currProject = null;
	private Automaton currAutomaton = null;
	private Alphabet currAlphabet = null;

	private InputProtocol inputProtocol = InputProtocol.UnknownProtocol;
	private File thisFile = null;


	public ProjectBuildFromFSM()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromFSM(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(URL url)
		throws Exception
	{
		String protocol = url.getProtocol();

		if (protocol.equals("file"))
		{
			inputProtocol = InputProtocol.FileProtocol;
			String fileName = url.getFile();
			thisFile = new File(fileName);

		}
		else if (protocol.equals("jar"))
		{
			inputProtocol = InputProtocol.JarProtocol;
		}
		else
		{
			inputProtocol = InputProtocol.UnknownProtocol;
			System.err.println("Unknown protocol: " + protocol);
			return null;
		}

		InputStream stream = url.openStream();
		return build(stream);
	}

	private Project build(InputStream is)
		throws Exception
	{
		//InputSource source = new InputSource(is);
		//return build(source);
		return null;
	}


}
