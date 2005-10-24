package org.supremica.external.robotCoordination;

class BoxSpanGenerator
	implements RobotListener
{
	public BoxSpanGenerator()
	{

	}

	/**
	 * Each time a robot collides with a box, add a layer of boxes,
	 * but check with the matrix first.
	 */
// 	public void startCollision(Volume volume, Configuration configuration, int time)
	public void startCollision(String name, int time)
	{
// 		assert(volume instanceof Box);

		// TODO:
		// Leta fram boxen med namnet name i boxesToExamine.

		/*
		Box box = (Box) volume;
		Coordinate coorde = box.getCoordinate();

		int x = coord.getX();
		int y = coord.getY();
 		int z = coord.getZ();
		
		Coordinate newCoord;
		Box newBox;
		Status newStatus;
		
		// Down
		newCoord = new Coordinate(x,y,z-1);
		boxesToExamine.add(newCoord);
		// Right
		newCoord = new Coordinate(x,y-1,z);
		boxesToExamine.add(newCoord);
		// Back
		newCoord = new Coordinate(x-1,y,z);
		boxesToExamine.add(newCoord);
		// Forward
		newCoord = new Coordinate(x+1,y,z);
		boxesToExamine.add(newCoord);
		// Left
		newCoord = new Coordinate(x,y+1,z);
		boxesToExamine.add(newCoord);
		// Up
		newCoord = new Coordinate(x,y,z+1);
		boxesToExamine.add(newCoord);
		*/
	}

	/**
	 * Don't care about these.
	 */
// 	public void endCollision(Volume volume, Configuration configuration, int time)
	public void endCollision(String name, int time)
	{		
	}
}