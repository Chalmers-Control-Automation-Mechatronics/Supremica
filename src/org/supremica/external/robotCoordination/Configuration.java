package org.supremica.external.robotCoordination;

/**
 * A configuration is the combination of a robots position and the configuration of 
 * its joints. More than just a point in space, in other words.
 */
public interface Configuration
{
	/**
	 * Returns the name of the configuration.
	 */
	public String getName();
}
