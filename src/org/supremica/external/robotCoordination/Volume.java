package org.supremica.external.robotCoordination;  

import java.util.*;
import java.awt.Color;

/**
 * Represents an abstract volume.
 */
public interface Volume 
{
//     protected String name;
	
//     public Volume(String name) 
// 	throws Exception
//     {
// 	this.name = name;
//     }

//     public abstract void delete() 
// 	throws Exception;
	
//     public abstract void setColor(Color color) 
// 	throws Exception;
	
//     public abstract void setTransparency(double transparency) 
// 	throws Exception;
	
//     public String getName()
//     {
// 	return name;
//     }

    public void delete() 
	throws Exception;
	
    public void setColor(Color color) 
	throws Exception;
	
    public void setTransparency(double transparency) 
	throws Exception;
	
    public String getName();
}