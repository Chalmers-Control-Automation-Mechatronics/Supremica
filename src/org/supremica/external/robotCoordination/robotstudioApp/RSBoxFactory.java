package org.supremica.external.robotCoordination.robotstudioApp;  

import org.supremica.external.robotCoordination.*;

import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;

import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.Variant;
import com.inzoom.comjni.SafeArray;

import java.awt.Color;

/**
 * Skapar RobotStudio-boxar.
 */
public class RSBoxFactory 
    implements BoxFactory
{
    private double dx = 0.5;
    private double dy = 0.5;
    private double dz = 0.5;

    private static int boxIndex = 0;
    
    public RSBoxFactory(double[] boxDimensions) {
	this.dx = boxDimensions[0];
	this.dy = boxDimensions[1];
	this.dz = boxDimensions[2];
    }

    public synchronized Box createBox(Coordinate coord, Color color, double transparency) 
	throws ComJniException
    {
	Variant varColor = new Variant(new SafeArray(new int[]{color.getRed(), color.getGreen(), color.getBlue()}), false);

	Transform trans = new Transform();
	trans.setX(coord.getX()); 
	trans.setY(coord.getY()); 
	trans.setZ(coord.getZ());
	
	// Lägg till en numrering på boxarna
	Part part = RSRobotCell.addPart("Box_" + boxIndex++);
	
	part.createSolidBox(trans, dx, dy, dz);
	part.setColor(varColor);
	part.setRelativeTransparency((float) transparency);

	return new Box(coord, color, transparency);
    }
    
    public Box createBox(Coordinate coord) 
	throws ComJniException
    {
	return createBox(coord, Color.BLUE, 0.5);
    }

    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    public void setDz(double dz) { this.dz = dz; }
}