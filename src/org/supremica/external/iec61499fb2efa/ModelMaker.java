/*
 *   Copyright (C) 2006 Goran Cengic
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   To contact author please refer to contact information in the README file.
 */

/*
 * @author Goran Cengic
 */
package org.supremica.external.iec61499fb2efa;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;

import net.sourceforge.fuber.xsd.libraryelement.*;
import org.supremica.automata.Automata;

// TODO: load IEC 61499 application

// TODO: make instance queue model

// TODO: make event execution thread model

// TODO: make jobs queue model

// TODO: make algorithms execution thread model

class ModelMaker
{

    private JAXBContext context;
    private Unmarshaller unmarshaller;

    private List libraryPathList = new LinkedList();

	private JaxbFBNetwork jaxbSystemFBNetwork;
	private Map jaxbFBTypes = new HashMap();

	private Automata automata = new Automata();
	
	private ModelMaker() 
	{
		// create unmarshaller
		try
		{
			context = JAXBContext.newInstance("net.sourceforge.fuber.xsd.libraryelement");
			unmarshaller = context.createUnmarshaller();
		}
		catch (Exception e)
		{
			System.err.println(e);
			System.exit(1);
		}
	}

    // find the fileName in libraries and return the corresponding File
    private File getFile(String fileName)
    {
		File theFile = new File(fileName);

		if (libraryPathList != null)
		{
			for (Iterator iter = libraryPathList.iterator();iter.hasNext();)
			{
				File curLibraryDir = (File) iter.next();
				theFile = new File(curLibraryDir, fileName);
				//System.out.println("ModelMaker.getFile(" + fileName + "): Looking for file in " + theFile.toString());
				if (theFile.exists())
				{
					break;
				}
			}
		}

		if (!theFile.exists())
		{
			System.err.println("ModelMaker.getFile(" + fileName + "): The file " + fileName + " does not exist in the specified libraries...");
			if (libraryPathList != null)
			{
				for (Iterator iter = libraryPathList.iterator();iter.hasNext();)
				{
					System.err.println("\t" + ((File) iter.next()).getAbsolutePath() + File.separator);
				}
			}
			else
			{
				System.err.println("\t. (current directory)");
			}
			System.err.println();
			System.err.println("Usage: ModelMaker [-o outputFile] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			System.exit(1);
		}

		return theFile;
    }

    private void load(String fileName)
    {
	
		System.out.println("ModelMaker.load(" + fileName + "): Loading file " + fileName);
		
		File file = getFile(fileName);
		
		try
		{
			Object unmarshalledXmlObject = unmarshaller.unmarshal(file);
			if (unmarshalledXmlObject instanceof JaxbSystem)
			{ 
				JaxbSystem theSystem = (JaxbSystem) unmarshalledXmlObject;
				if (theSystem.isSetDevice())
				{
					JaxbDevice theDevice = (JaxbDevice) theSystem.getDevice().get(0);
					if(theDevice.isSetResource())
					{
						JaxbResource theResource = (JaxbResource) theDevice.getResource().get(0);
						if (theResource.isSetFBNetwork())
						{
							System.out.println("ModelMaker.load(" + fileName + "): The file is IEC 61499 system.");
							jaxbSystemFBNetwork = ((JaxbFBNetwork) theResource.getFBNetwork());
						}
					}
				}
			}
			else if (unmarshalledXmlObject instanceof JaxbFBType)
			{
				JaxbFBType theType = (JaxbFBType) unmarshalledXmlObject;
				if (theType.isSetName())
				{
					System.out.println("ModelMaker.load(" + fileName + "): The file is IEC 61499 FB type.");
					jaxbFBTypes.put(theType.getName(),theType);
				}				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
	

	public void makeModel(String outputFileName, String systemFileName, String libraryPathBase, String libraryPath)
	{
		// convert libraryPath string into list of Files
		if (libraryPath == null) // libraryPath is not specified
		{
			if (libraryPathBase == null)
			{
				libraryPathList = null;
			}
			else
			{
				File libraryPathBaseFile = new File(libraryPathBase);

				if (!libraryPathBaseFile.isDirectory())
				{
					System.err.println("ModelMaker(): Specified library base is not a directory!: " + libraryPathBaseFile.getName());
				}
				else if (!libraryPathBaseFile.exists())
				{
					System.err.println("ModelMaker(): Specified library base does not exist!: " + libraryPathBaseFile.getName());
				}
				else
				{
					libraryPathList.add(libraryPathBaseFile);
				}
			}
		}
		else // libraryPath is specified by the user
		{
		
			while (true)
			{
				
				File curLibraryDir;

				if (libraryPath.indexOf(File.pathSeparatorChar) == -1)
				{
					curLibraryDir = new File(libraryPathBase, libraryPath);
				}
				else
				{
					curLibraryDir = new File(libraryPathBase, libraryPath.substring(0,libraryPath.indexOf(File.pathSeparatorChar)));
				}

				if (!curLibraryDir.isDirectory())
				{
					System.err.println("ModelMaker(): Specified library path element " + curLibraryDir.getAbsolutePath() + " is not a directory!");
				}
				else if (!curLibraryDir.exists())
				{
					System.err.println("ModelMaker(): Specified library path element " + curLibraryDir.getAbsolutePath() + " does not exist!");
				}
				else
				{
					libraryPathList.add(curLibraryDir);
				}

				if (libraryPath.indexOf(File.pathSeparatorChar) == -1)
				{
					break;
				}

				libraryPath = libraryPath.substring(libraryPath.indexOf(File.pathSeparatorChar)+1);
			}

		}

		load(systemFileName);
	}




	public static void main(String args[])
    {
		String outputFileName = null;
		String systemFileName = null;
		String libraryPathBase = null;
		String libraryPath = null;

		if (args.length == 0)
		{
			System.err.println("Usage: ModelMaker [-o outputFileName] [-lb libraryPathBase] [-lp libraryDirectory]... file.sys");
			return;
		}

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-o"))
			{
				if (i + 1 < args.length)
				{
					outputFileName = args[i + 1];
				}
			}
			if (args[i].equals("-lb"))
			{
				if (i + 1 < args.length)
				{
					libraryPathBase = args[i + 1];
				}
			}
			if (args[i].equals("-lp"))
			{
				if (i + 1 < args.length)
				{
					if (libraryPath == null)
					{
						libraryPath = args[i + 1];
					}
					else
					{
						libraryPath = libraryPath + File.pathSeparator + args[i + 1];
					}
				}
			}
			if (i == args.length-1)
			{
				systemFileName = args[i];
				if (outputFileName == null)
				{
					outputFileName = systemFileName + ".wmod";
				}
			}
			
		}			

// 		System.out.println("Input arguments: " 
// 						   + "\n\t output file name: " + outputFileName 
// 						   + "\n\t system file name: " + systemFileName 
// 						   + "\n\t library path base: " + libraryPathBase 
// 						   + "\n\t library path: " + libraryPath);

		(new ModelMaker()).makeModel(outputFileName,systemFileName,libraryPathBase,libraryPath);
		System.exit(0);
	}

}
