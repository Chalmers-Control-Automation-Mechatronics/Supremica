package org.supremica.external.fbd2smv.fbdProject;

import java.util.*;
import org.supremica.external.fbd2smv.isagrafReader.*;

public class Program
{
    private HashMap    variables;
    private HashMap    boxes;
    private LinkedList boxesList  = new LinkedList();
    private LinkedList boxesWithoutCornersList  = new LinkedList();

    private LinkedList arcs;    
    private String     name;
	

    public Program(String name, HashMap variables, HashMap boxes, LinkedList arcs)
    {
	this.name = name;
	this.variables = variables;
	this.boxes = boxes;
	this.arcs = arcs;

	/*
	 * Skapa en lista med boxarna för att kunna sortera 
	 * i boxordning
	 */
	for (Iterator boxIt = boxes.values().iterator(); boxIt.hasNext(); )
	    {
		BOX currBOX = (BOX)boxIt.next();
		boxesList.add(currBOX);
		if (!currBOX.getName().equals("{\\div}"))
		    {
			boxesWithoutCornersList.add(currBOX);
		    }
	    }
	Collections.sort(boxesList);
	Collections.sort(boxesWithoutCornersList);


    }

    public String getName()
    {
	return name;
    }


    public HashMap getVariables()
    {
	return variables;
    }

    
    public HashMap getBoxes()
    {
	return boxes;
    }

	
    public LinkedList getBoxesList()
    {
	return boxesList;
    }

    public LinkedList getBoxesWithoutCornersList()
    {
	return boxesWithoutCornersList;
    }
    
    public LinkedList getArcs()
    {
	return arcs;
    }


}

