package org.supremica.external.fbd2smv.isagrafReader;

import java.util.*;
//import org.supremica.external.fbd2smv.isagrafReader.*;

public class FBDElement implements java.lang.Comparable
{
    String programName;
    String elementName;
    String elementType;
    int programIndex;
    int elementIndex;
    int x;
    int y;

    public FBDElement(String programName, int programIndex, String elementName, String elementType, int elementIndex, int x, int y)
    {
	this.programName  = programName;
	this.elementName  = elementName;
	this.elementType  = elementType;
	this.elementIndex = elementIndex;
	this.programIndex = programIndex;
	this.x = x;
	this.y = y;
    }	

    public String getProgramName()
    {
	return programName; 
    }

    public String getElementName()
    {
	return elementName;
    }

    public void setElementName(String name)
    {
	elementName = name;
    }

    public String getElementType()
    {
	return elementType;
    }

    public int getProgramIndex()
    {
	return programIndex;
    }

    public int getElementIndex()
    {
	return elementIndex;
    }

    public int getX()
    {
	return x;
    }

    public int getY()
    {
	return y;
    }

 
    public int compareTo(Object o)
    {
	int obj_programIndex;
	int obj_x;
	int obj_y;
	
	obj_programIndex = ((FBDElement)o).programIndex;
	obj_x = ((FBDElement)o).x;
	obj_y = ((FBDElement)o).y;

	if (this.programIndex < obj_programIndex)
	    {
		return -1;
	    }
	else
	    {
		if (this.programIndex == obj_programIndex)
		    {
			if (this.y < obj_y)
			    {
				return -1;
			    }
			else 
			    {
				if (this.y == obj_y)
				    {
					if (this.x < obj_x) 
					    {
						return -1;
					    }
					else if (this.x == obj_x) 
					    {
						return 0;
					    }
					else
					    {
						return 1;
					    }
				    }
				else
				    {
					return 1;
				    }
			    }
			
		    }
		else
		    {
			return 1;
		    }
	    }


    }

}
