package org.supremica.external.robotCoordination;  

import com.inzoom.comjni.ComJniException;
import java.awt.Color;

/**
 * Represents an abstract box.
 */
public interface Box 
	extends Volume
{
 //    protected Coordinate coord;

//     public Box(String name, Coordinate coord) 
// 	throws Exception
//     {
// 	super(name);
	
// 	this.coord = coord;
//     }
    	
//     public Coordinate getCoordinate() 
//     { 
// 	return coord; 
//     }

//     public void setCoordinate(Coordinate coord) 
//     {
// 	this.coord = coord;
//     }

        	
    public Coordinate getCoordinate();

    public void setCoordinate(Coordinate coord);
}