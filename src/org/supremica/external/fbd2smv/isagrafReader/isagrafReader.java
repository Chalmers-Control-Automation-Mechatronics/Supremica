package org.supremica.external.fbd2smv.isagrafReader;

import java.io.*;
import java.util.*;
import org.supremica.external.fbd2smv.util.*;
import org.supremica.external.fbd2smv.fbdProject.*;

public class isagrafReader
{
    private String fbdProjectPath;
	private fbdProject fbdProj = new fbdProject();


	public isagrafReader(String fbdProjectPath) throws IOException
	{
		this.fbdProjectPath = fbdProjectPath;
		buildDictionary();
		//buildProgramList();
		buildPrograms();
	}


	public fbdProject getFbdProject()
	{
		return fbdProj;
	}



    private void buildDictionary() throws IOException
    {
		FileReader fr = new FileReader(fbdProjectPath + "appli.dlo");
		DLOReader dloReader = new DLOReader(fr);
	    fbdProj.dictionarySetBooleans(dloReader.getBooleans());
		fr.close();
	}


	private void buildPrograms() throws IOException
	{
		LinkedList programFiles;
		FileFinder fileFinder = new FileFinder();
		programFiles = fileFinder.getFiles(fbdProjectPath, "lsf");


		for (int i=0; i<programFiles.size(); i++)
			{
			    System.out.println("==== PROGRAM: " + (String)programFiles.get(i) + " =====");
				buildProgram((String)programFiles.get(i));
			}

	}

	private void buildProgram(String programName) throws IOException
	{
		FileReader fr = new FileReader(fbdProjectPath + programName);
		LSFReader lsfReader = new LSFReader(fr);
		fr.close();
		Program program = new Program(programName.substring(0, programName.indexOf(".")), lsfReader.getVariables(), lsfReader.getBoxes(), lsfReader.getArcs());
		fbdProj.addProgram(program);
	}


    private void buildProgramList() throws IOException
	{
		FileFinder fileFinder = new FileFinder();
		fbdProj.setProgramList(fileFinder.getFiles(fbdProjectPath, "lsf"));
	}


	private void buildFBDElements() throws IOException
	{
		
	}


}
