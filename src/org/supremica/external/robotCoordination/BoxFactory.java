package org.supremica.external.robotCoordination;

import java.awt.Color;

public interface BoxFactory 
{
    /**
     * Creates a box with given coordinates, color and transparency.
     */
    public Box createBox(Coordinate coord, Color color, double transparency) 
	throws Exception;
    
    public void setDx(double dx); 
    public void setDy(double dy);
    public void setDz(double dz);

}