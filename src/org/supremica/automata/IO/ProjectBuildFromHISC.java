
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
import org.supremica.automata.*;
import org.supremica.log.*;

/**
 * Import files from Leduc/Song HISC-format
 *
 * See the MSc thesis of Raoguang Song.
 */
public class ProjectBuildFromHISC
{
	private static Logger logger = LoggerFactory.createLogger(ProjectBuildFromHISC.class);
	private ProjectFactory theProjectFactory = null;
	private Project project = null;
	private String fileName;
	private String pathName;
	
	public ProjectBuildFromHISC()
	{
		this.theProjectFactory = new DefaultProjectFactory();
	}

	public ProjectBuildFromHISC(ProjectFactory theProjectFactory)
	{
		this.theProjectFactory = theProjectFactory;
	}

	public Project build(URL url)
		throws Exception
	{
		String protocol = url.getProtocol();

		if (protocol.equals("file"))
		{
			fileName = url.getFile();
			pathName = fileName.substring(0,fileName.lastIndexOf("/"));
		}
		else
		{
			System.err.println("Unknown protocol: " + protocol);

			return null;
		}

		InputStream stream = url.openStream();

		// Get the project
		project = theProjectFactory.getProject();

		// Build the automata objects
		return build(stream);
	}

	/**
	 * Get next line that is not empty after stripping comments.
	 */
	private String readLineNoComments(BufferedReader reader)		
		throws Exception
	{
		String line = "";
		while (line.equals(""))
		{
			// Read line
			line = reader.readLine();
			if (line == null)
				return null;

			// Strip comments and whitespaces
			if (line.contains("#"))
				line = line.substring(0,line.indexOf("#")).trim();
			else
				line = line.trim();
		}
		return line;
	}
	
	private Project build(InputStream is)
		throws Exception
	{
		InputStreamReader isReader = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isReader);

		// Loop over lines
		for (String line = readLineNoComments(reader); line != null; 
			 line = readLineNoComments(reader))
		{
			try
			{
				// Look for brackets with tags
				if (line.startsWith("["))
				{
					String currentTag = line.substring(1,line.length()-1);
					if (currentTag.equals("SYSTEM"))
					{
						// The next line holds the name of the project
						String nameLine = readLineNoComments(reader);
						project.setName(nameLine);
					}
					else if (currentTag.equals("LOW"))
					{
						// The next lines contains the names of the low level directories
						String numberLine = readLineNoComments(reader);
						int nbrOfLows = Integer.parseInt(numberLine);
						// We'll find nbrOfLows lowlevels now
						for (int i=0; i<nbrOfLows; i++)
						{
							String lowLine = readLineNoComments(reader);
							if (lowLine == null || lowLine.startsWith("["))
								throw new Exception("Expected another low level name.");
							loadSubSystem(lowLine);
						}
					}
					else if (currentTag.equals("HIGH"))
					{
						// The next line contains the name of the high level directory
						String highLine = readLineNoComments(reader);
						if (highLine == null || highLine.startsWith("["))
							throw new Exception("Expected high level name.");
						loadSubSystem(highLine);
					}
					else
					{
						throw new Exception("Unknown tag: '" + currentTag + "'.");
					}
				}
				else
				{
					throw new Exception("Syntax error: '" + line + "'.");
				}
			}
			catch(Exception ex)
			{
				throw new Exception("Error in file " + 
									fileName + ". " + ex);
			}
		}

		// Return
		logger.warn("There is a problem when converting HISC projects since the concepts of answer and request events do not automatically translate to ordinary supervisory control.");
		return project;
	}

	private void loadSubSystem(String directoryName)
		throws Exception
	{
		File file = new File(pathName+"/"+directoryName+"/"+directoryName+".sub");
		InputStream is = file.toURL().openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		// Loop over lines
		int nbrOfPlants = 0;
		int nbrOfSpecs = 0;
		for (String line = readLineNoComments(reader); line != null; 
			 line = readLineNoComments(reader))
		{
			try
			{
				// Look for brackets with tags
				if (line.startsWith("["))
				{
					String currentTag = line.substring(1,line.length()-1).toUpperCase();
					if (currentTag.equals("SYSTEM"))
					{
						// The next line holds the number of plants
						String number = readLineNoComments(reader);
						nbrOfPlants = Integer.parseInt(number);
						// The next line holds the number of specs
						number = readLineNoComments(reader);
						nbrOfSpecs = Integer.parseInt(number);
					}
					else if (currentTag.equals("INTERFACE"))
					{
						// The next line contains the name of the interface
						String name = readLineNoComments(reader);
						if (name == null || name.startsWith("["))
							throw new Exception("Expected the name of an interface, got '" + name +"'.");
						if (!name.endsWith(".hsc"))
							name = name + ".hsc";
						Automaton aut = loadAutomaton(pathName+"/"+directoryName+"/"+name);
						aut.setName(name.substring(0,name.indexOf(".hsc")));
						aut.setType(AutomatonType.UNDEFINED);
						project.addAutomaton(aut);
					}
					else if (currentTag.equals("PLANT"))
					{
						// The next lines contains the names of the plants
						// We'll find nbrOfPlants plants now
						for (int i=0; i<nbrOfPlants; i++)
						{
							String name = readLineNoComments(reader);
							if (name == null || name.startsWith("["))
								throw new Exception("Expected another plant name, got '" + name +"'.");
							if (!name.endsWith(".hsc"))
								name = name + ".hsc";
							Automaton aut = loadAutomaton(pathName+"/"+directoryName+"/"+name);
							aut.setName(name.substring(0,name.indexOf(".hsc")));
							aut.setType(AutomatonType.PLANT);
							project.addAutomaton(aut);
						}
					}
					else if (currentTag.equals("SPEC"))
					{
						// The next lines contains the names of the specs
						// We'll find nbrOfSpecs specs now
						for (int i=0; i<nbrOfSpecs; i++)
						{
							String name = readLineNoComments(reader);
							if (name == null || name.startsWith("["))
								throw new Exception("Expected another specification name, got '" + name +"'.");
							if (!name.endsWith(".hsc"))
								name = name + ".hsc";
							Automaton aut = loadAutomaton(pathName+"/"+directoryName+"/"+name);
							aut.setName(name.substring(0,name.indexOf(".hsc")));
							aut.setType(AutomatonType.SPECIFICATION);
							project.addAutomaton(aut);
						}
					}
					else
					{
						throw new Exception("Unknown tag: '" + currentTag + "'.");
					}
				}
				else
				{
					throw new Exception("Syntax error: '"+ line + "'.");
				}
			}
			catch(Exception ex)
			{
				throw new Exception("Error in file " + 
									file.getAbsolutePath() + ". " + ex);
			}
		}
	}

	private Automaton loadAutomaton(String fileName)
		throws Exception
	{
		File file = new File(fileName);
		InputStream is = file.toURL().openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		// The result
		Automaton aut = new Automaton();

		// Loop over lines
		for (String line = readLineNoComments(reader); line != null; 
			 line = readLineNoComments(reader))
		{
			try
			{
				// Look for brackets with tags
				if (line.startsWith("["))
				{
					String currentTag = line.substring(1,line.length()-1).toUpperCase();
					if (currentTag.equals("STATES"))
					{
						// The next line holds the number of states
						String number = readLineNoComments(reader);
						int nbrOfStates = Integer.parseInt(number);
						for (int i=0; i<nbrOfStates; i++)
						{
							String name = readLineNoComments(reader);
							if (name == null || name.startsWith("["))
								throw new Exception("Expected another state name, got '" + name +"'.");
							State state = new State(name);
							aut.addState(state);
						}
					}
					else if (currentTag.equals("INITSTATE"))
					{
						// The next line holds the name of the initial state
						String name = readLineNoComments(reader);
						State state = aut.getStateWithName(name);
						aut.setInitialState(state);
					}
					else if (currentTag.equals("MARKINGSTATES"))
					{
						// The next line(s?) holds the names of marked states
						while(true)
						{
							// Prepare to undo up to 200 characters of text (if we read too much)
							reader.mark(200);
							String name = readLineNoComments(reader);
							if (name == null || name.startsWith("["))
							{
								// We've read too much! Reset stream!
								reader.reset();
								break;
							}
							else
							{
								State state = aut.getStateWithName(name);
								state.setAccepting(true);
							}
						}
					}
					else if (currentTag.equals("EVENTS"))
					{
						// The next line(s?) holds the events
						while(true)
						{
							// Prepare to undo up to 200 characters of text (if we read too much)
							reader.mark(200);
							String eventLine = readLineNoComments(reader);
							if (eventLine == null || eventLine.startsWith("["))
							{
								// We've read too much! Reset stream!
								reader.reset();
								break;
							}
							else
							{
								// We expect three tokens in the line
								StringTokenizer st = new StringTokenizer(eventLine);
								LabeledEvent event = null;
								try
								{
									// First token is name
									event = new LabeledEvent(st.nextToken());
									// Second token is controllability ("Y"/"N")
									event.setControllable(st.nextToken().toUpperCase().equals("Y"));
									aut.getAlphabet().addEvent(event);
									// Last token is request or answer
									// event... don't know what to do
									// with it...
									st.nextToken();
								}
								catch (Exception ex)
								{
									throw new Exception("Error parsing event " + event + ".");
								}
							}
							
						}
					}
					else if (currentTag.equals("TRANSITIONS"))
					{
						// The next line(s?) holds the transitions,
						// first a line with the from-state, then a
						// number of lines with event and to-state in
						// parentheses
						while(true)
						{
							// Prepare to undo up to 200 characters of text (if we read too much)
							reader.mark(200);
							String name = readLineNoComments(reader);
							if (name == null || name.startsWith("["))
							{
								// We've read too much! Reset stream!
								reader.reset();
								break;
							}
							else
							{
								// First the name of the fromState
								State fromState = aut.getStateWithName(name);
								// Then some lines with event and toState in parentheses
								while(true)
								{
									// Prepare to undo up to 200
									// characters of text (if we read
									// too much)
									reader.mark(200);
									String infoLine = readLineNoComments(reader);
									if (infoLine == null || !infoLine.startsWith("("))
									{
										// We've read too much! Reset stream!
										reader.reset();
										break;
									}
									else
									{
										infoLine = infoLine.substring(infoLine.indexOf("(")+1,infoLine.lastIndexOf(")"));
										StringTokenizer st = new StringTokenizer(infoLine);
										LabeledEvent event = aut.getAlphabet().getEvent(st.nextToken());
										if (event == null)
											throw new Exception("Unknown event used in transition");
										State toState = aut.getStateWithName(st.nextToken());
										Arc arc = new Arc(fromState, toState, event);
										aut.addArc(arc);
									}
								}
							}
						}					
					}
					else
					{
						throw new Exception("Unknown tag: '" + currentTag + "'.");
					}
				}
				else
				{
				throw new Exception("Syntax error: '" + line + "'.");
				}
			}
			catch(Exception ex)
			{
				throw new Exception("Error in file " + 
									file.getAbsolutePath() + ". " + ex);
			}
		}

		return aut;
	}
}
