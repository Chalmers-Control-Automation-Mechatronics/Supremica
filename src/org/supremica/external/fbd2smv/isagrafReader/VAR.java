package org.supremica.external.fbd2smv.isagrafReader;

import java.util.*;
import org.supremica.external.fbd2smv.isagrafReader.*;

public class VAR
{
    private String index;
    private String name;
    
    public VAR(String index, String name)
    {
	this.index = index;
	this.name = name;
    }
    
    public int hashCode()
    {
	return index.hashCode();
    }
    
    public boolean equals(Object other)
    {
	return index.equals(((VAR)other).index);
    }
    
    public String getIndex()
    {
	return index;
    }

    public String getName()
    {
	return name;
    }

    public String toString()
    {
	return "Index: " + index + " Name: " + name;
    }

    public boolean isOutputVariable(List theArcs)
    {
	for (Iterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
	    {String S = null;
	    ARC currARC = (ARC)arcIt.next();
	    if (index.equals(S.valueOf(currARC.getTargetIndex()))) 
		    {
			return true;
		    }
            }
    
	return false;
    }
}
