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

	// IEC_Program(Array in, Array out);
}
