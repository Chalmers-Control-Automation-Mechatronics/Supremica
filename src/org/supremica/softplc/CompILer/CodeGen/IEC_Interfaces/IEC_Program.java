package org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces;

/**
 *  Interface that our IEC_Program
 *  have to implement
 *  @author Niclas Hamp
 *  @version 1.0
 */
public interface IEC_Program
{

	/** run() is the function to be executed once every x ms
	 */
	void run();

	/* An class implementing IEC_Program should have a constructor
	 * that takes two arguments. The arguments are two arrays of
	 * boolean (boolean[]) the first one is representing in signals and
	 * the second out signals.
	 */

	// IEC_Program(Array in, Array out);
}
