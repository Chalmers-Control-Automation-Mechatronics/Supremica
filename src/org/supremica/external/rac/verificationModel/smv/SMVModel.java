/*
 * This RAC package is external to the Supremica tool and developed by 
 * Oscar Ljungkrantz. Contact:
 *
 * Oscar Ljungkrantz 
 * oscar.ljungkrantz@chalmers.se
 * +46 (0)708-706278
 * SWEDEN
 *
 * for technical discussions about RACs (Reusable Automation Components).
 * For questions about Supremica license or other technical discussions,
 * contact Supremica according to License Agreement below.
 */

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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

/**
 * The SMVModel class is used to store an SMV model of a RAC
 * including main module and any used module.
 *
 *
 * Created: Mon Apr 14 14:48:39 2008
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.external.rac.verificationModel.smv;

import org.supremica.external.rac.verificationModel.VerificationModel;
import org.supremica.external.rac.SMVModelBuilder;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.*;

public class SMVModel extends VerificationModel
{
    private Set<SMVModule> modules;
    private Map<String, String> standardModulesNameToContentMap;

    public SMVModel()
    {
	super();
	modules = new LinkedHashSet<SMVModule>(5); //initial capacity 5 and default load factor (0.75)
	standardModulesNameToContentMap = new LinkedHashMap<String, String>(5); // -||-
    }

    // Add new module (if a module with the same name is not already included) and return true. 
    // Return false if a module with the same name was already included
    public boolean addNewModule(SMVModule newModule)
    {
	return modules.add(newModule);
    }
    
    public void createFile(String fileName, String path)
    {
	File newFile = new File(path, fileName);
	try
	{
	    BufferedWriter writer = new BufferedWriter( new FileWriter( newFile ) );

	    // in this method we also go through the file, looking for SCAN_CYCLE_DENOTER
	    // and replace that with the scancycletime chosen by the verifyer
	    String scanCycle = null;

	    for (SMVModule module : modules)
	    {
		String moduleCode = module.getModuleCode();
		if (moduleCode.contains(SMVModelBuilder.SCAN_CYCLE_DENOTER))
		{
		    // Has the verifyer specified the scan cycle yet?
		    if (scanCycle == null)
		    {
			try
			{
			    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			    System.out.print("Specify scan cycle time in ms (as integer) to use in the verification process: ");
			    System.out.flush();
			    scanCycle = in.readLine();
			}
			catch(IOException exception)
			{
			    System.err.println("SMVModel: IOException! Could not read the input from the keybord!");
			}
			
		    }
		    moduleCode = moduleCode.replaceAll(SMVModelBuilder.SCAN_CYCLE_DENOTER, scanCycle + ".." + scanCycle);
		}
		
		writer.write(moduleCode, 0, moduleCode.length());
		writer.newLine(); // platform independent new line character
	    }
	    for (String standardModuleContent : standardModulesNameToContentMap.values())
	    {
		if (standardModuleContent.contains(SMVModelBuilder.SCAN_CYCLE_DENOTER))
		{
		    // Has the verifyer specified the scan cycle yet?
		    if (scanCycle == null)
		    {
			try
			{
			    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			    System.out.print("Specify scan cycle time in ms (as integer) to use in the verification process: ");
			    System.out.flush();
			    scanCycle = in.readLine();
			}
			catch(IOException exception)
			{
			    System.err.println("SMVModel: IOException! Could not read the input from the keybord!");
			}
			
		    }
		    standardModuleContent = standardModuleContent.replaceAll(SMVModelBuilder.SCAN_CYCLE_DENOTER, scanCycle + ".." + scanCycle);
		}
		writer.write(standardModuleContent, 0, standardModuleContent.length());
		writer.newLine(); // platform independent new line character
	    }
	    writer.close();
	}
	catch(IOException e)
	{
	    System.err.println("Illegal result file!");
	    e.printStackTrace();
	}

    }
    
    // Import a new standard module from a file (if a module with the same name is not already included) and return true. 
    // Return false if a module with the same name was already included
    public boolean importModuleFromFile(File theFile, String name)
    {
	try
	{	
	    
	    BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( theFile ) ) );
	    // read the first line
	    String line = reader.readLine();
	    String content = "";		
	    
	    // Alternative way of finding the module name:
	    // // Read through the first lines of comments (#) or blank lines
	    // while ( line != null && (line.trim().startsWith("#") || line.trim().length() == 0) )
	    // {
	    // content += line + "\n";
	    // line = reader.readLine();
	    // }
	    // String name = "";
	    // if (line != null)
	    // {
	    // int startIndex = line.indexOf("module ") + 7; // length of "module " is 7
	    // int endIndex = line.indexOf("(", startIndex);
	    // if (startIndex >= 7 && endIndex > startIndex) // s.indexOf(ss) returns -1 if ss not found within s
	    // {
	    // name = line.substring(startIndex, endIndex);
	    // }
	    // }
	    
	    if (name != null && !standardModulesNameToContentMap.containsKey(name))
	    {
		while (line != null)
		{
		    content += line + "\n";
		    line = reader.readLine();
		    //StringBuffer row = new StringBuffer(line);
		}
		standardModulesNameToContentMap.put(name, content);
		return true;
	    }
	}
	catch(FileNotFoundException e)
	{
	    System.err.println("SMVModelBuilder: Error! Could not find the file " + theFile.getName());
	    e.printStackTrace();
	}
	catch(IOException e)
	{
	    System.err.println("SMVModelBuilder: Error! Problems reading the file " + theFile.getName());
	    e.printStackTrace();
	}
	return false;
	
    }
    
}
