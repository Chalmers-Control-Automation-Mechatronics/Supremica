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

	LinkedList fbdElements = fbdProj.getFBDElements();

	FileReader fr = new FileReader(fbdProjectPath + "appli.hie");
	LinkedList programList;
	HIEReader hieReader = new HIEReader(fr);
	programList = hieReader.getPrograms();

	//buildProgramList();
	buildPrograms(programList, fbdElements);
	Collections.sort(fbdElements);
    }


    public fbdProject getFbdProject()
    {
	return fbdProj;
    }



    private void buildDictionary() throws IOException
    {
	File file; 

	file = new File(fbdProjectPath + "appli.dlo");
	if (file.exists())
	{
	    FileReader fr = new FileReader(fbdProjectPath + "appli.dlo");
	    DLOReader dloReader = new DLOReader(fr);
	    fbdProj.dictionarySetBooleans(dloReader.getBooleans());
	    System.out.println("\t\tisagrafReader DCOReader");
	    fr.close();
	}

	file = new File(fbdProjectPath + "appli.dco");
	if (file.exists())
	{
	    FileReader fr = new FileReader(fbdProjectPath + "appli.dco");
	    DCOReader dcoReader = new DCOReader(fr);
	    fbdProj.dictionarySetIntegers(dcoReader.getIntegers());
	    fr.close();
	}
    }


    private void buildPrograms(LinkedList programList, LinkedList fbdElements) throws IOException
    {
	FileFinder fileFinder = new FileFinder();

	for (int i=0; i<programList.size(); i++)
	    {
		System.out.println("==== PROGRAM: " + (String)programList.get(i) + " =====");
		buildProgram((String)programList.get(i), i, fbdElements);
	    }

    }

    private void buildProgram(String programName, int programIndex, LinkedList fbdElements) throws IOException
    {
	System.out.println(programName);
	FileReader fr = new FileReader(fbdProjectPath + programName + ".lsf");
	LSFReader lsfReader = new LSFReader(fr, programName, programIndex, fbdElements);
	fr.close();
	Program program = new Program(programName, lsfReader.getVariablesByIndex(), lsfReader.getVariablesByName(), lsfReader.getBoxes(), lsfReader.getArcs());
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
