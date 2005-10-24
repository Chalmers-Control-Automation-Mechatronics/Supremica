package org.supremica.external.robotCoordination;  

import java.util.*;
import java.awt.Color;

/**
 * Represents an abstract volume.
 */
public interface Volume 
{
    public void delete() throws Exception;
	
	public void setColor(Color color) throws Exception;
	
	public void setTransparency(double transparency) throws Exception;
	
	public String getName();
}