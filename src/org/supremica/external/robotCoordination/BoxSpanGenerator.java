package org.supremica.external.robotCoordination;

import java.awt.Toolkit;
import java.util.*;

public class BoxSpanGenerator
    implements RobotListener
{
    Cell cell; 
    Robot robot; 
    Hashtable<Coordinate, Status> matrix; 
    List<Coordinate> zoneBoxes; 
    List<Coordinate> surfaceBoxes;
	
    public BoxSpanGenerator(Cell cell, Robot robot, Hashtable<Coordinate, Status> matrix, 
			    List<Coordinate> zoneBoxes, List<Coordinate> surfaceBoxes)
    {
	this.cell = cell;
	this.robot = robot;
	this.matrix = matrix;
	this.zoneBoxes = zoneBoxes;
	this.surfaceBoxes = surfaceBoxes;
    }

    /**
     * Each time a robot collides with a box, add a layer of boxes,
     * (check with the matrix first).
     */
    public void collisionStart(Volume volume, int time)
    {
	// 		assert(volume instanceof Box);

	/*
	  Box box = (Box) volume;
	  Coordinate coord = box.getCoordinate();
	  // Don't need this one anymore
	  box.delete();

	  // If someone has already collided with this box, this is now a zoneBox!!!
	  Status status = matrix.get(coord);
	  if (status.occupied)
	  {
	  // Beep and add!
	  Toolkit.getDefaultToolkit().beep();
	  zoneBoxes.add(coord);
	  }
	  // No matter what, box has now been occupied
	  status.occupied = true;
	  assert(status.checked = true);
	  status.checked = true;

	  // Add new surfaceboxes!
	  int x = coord.getX();
	  int y = coord.getY();
	  int z = coord.getZ();
		
	  Coordinate newCoord;
	  Box newBox;
	  Status newStatus;
				
	  // Down
	  newCoord = new Coordinate(x,y,z-1);
	  if (!matrix.containsKey(newCoord))
	  {
	  newStatus = new Status();
	  newStatus.occupied = false;
	  newStatus.checked = true;
	  matrix.put(newCoord, newStatus);
	  }
	  else
	  {
	  newStatus = matrix.get(newCoord);
	  // If it's already checked, move on!
	  if (!newStatus.checked)
	  {
	  Box box = cell.createBox(newCoord);
	  surfaceBoxes.add(box);
	  }
	  }

	  // Right
	  newCoord = new Coordinate(x,y-1,z);
	  // Back
	  newCoord = new Coordinate(x-1,y,z);
	  // Forward
	  newCoord = new Coordinate(x+1,y,z);
	  // Left
	  newCoord = new Coordinate(x,y+1,z);
	  // Up
	  newCoord = new Coordinate(x,y,z+1);
	*/
    }

    /**
     * Don't care about these right now.
     */
    public void collisionEnd(Volume volume, int time)
    {		
    }
}