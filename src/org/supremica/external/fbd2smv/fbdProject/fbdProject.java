package org.supremica.external.fbd2smv.fbdProject;

import java.util.*;
import org.supremica.external.fbd2smv.isagrafReader.*;

public class fbdProject
{
    private Dictionary dictionary  = new Dictionary();
    private LinkedList programs    = new LinkedList();
    private LinkedList programList = new LinkedList();

    //private HashMap    variables   = new HashMap();
    //private HashMap    boxes       = new HashMap();
    //private LinkedList    arcs        = new HashMap();    
    
    
    
    public fbdProject()
    {
	
    }
    
    public void dictionarySetBooleans(LinkedList booleans)
    {
	dictionary.setBooleans(booleans);
    }
    
    public LinkedList dictionaryGetBooleans()
    {
	return dictionary.getBooleans();
    }
    
    public LinkedList dictionaryGetIntegers()
    {
	return dictionary.getIntegers();
    }


    public void addProgram(Program program)
    {
	programs.add(program);
    }

    public void setProgramList(LinkedList programList)
    {
	this.programList = programList;
    }
    
    public LinkedList getProgramList()
    {
	return programList;
    }
    
    public LinkedList getPrograms()
    {
	return programs;
    }


    public boolean isOutputVariable(String name)
    {
	Program program;
	LinkedList arcs;    
	HashMap variables;

	for (int i=0; i<programs.size(); i++)
	    {
		program = (Program)programs.get(i);
		arcs = program.getArcs();
		variables = program.getVariables();
		VAR variable = (VAR)variables.get(name);
		
		if (variable != null) {
		    String index = variable.getIndex();
		    
		    for (int j=0; j<arcs.size(); j++ )
			{String S = null;
			ARC currARC = (ARC)arcs.get(j);
			if (index.equals(S.valueOf(currARC.getTargetIndex()))) 
			    {
			    return true;
			    }
			}
		}

	    }
	
	return false;
	
    }


}

