package org.supremica.external.robotCoordination.robotstudioApp;  

import org.supremica.external.robotCoordination.*;
import org.supremica.external.comInterfaces.robotstudio_3_1.RobotStudio.*;
import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.Variant;

/**
 * Representerar en RobotStudio-box.
 */
public class BoxFactory {
    private double dx = 0.5;
    private double dy = 0.5;
    private double dz = 0.5;

    private static int boxIndex = 0;
    
    public BoxFactory(double dx, double dy, double dz) {
	this.dx = dx;
	this.dy = dy;
	this.dz = dz;
    }

    public synchronized Box createBox(Coordinate coord, Variant color, double transparency) 
	throws ComJniException
    {
	Transform trans = new Transform();
	trans.setX(coord.getX()); 
	trans.setY(coord.getY()); 
	trans.setZ(coord.getZ());
	
	// L�gg till en numrering p� boxarna
	Part part = RSRobotCell.addPart("Box_" + boxIndex++);
	
	part.createSolidBox(trans, dx, dy, dz);
	part.setColor(color);
	part.setRelativeTransparency((float) transparency);

	return new Box(coord, color, transparency);
    }
    
    public Box createBox(Coordinate coord) 
	throws ComJniException
    {
	return createBox(coord, RSRobotCell.RS_BLUE, 0.5);
    }

    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    public void setDz(double dz) { this.dz = dz; }
}