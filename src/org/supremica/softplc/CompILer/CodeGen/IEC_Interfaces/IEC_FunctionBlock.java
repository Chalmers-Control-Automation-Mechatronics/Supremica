package org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces;
/**
 *  Interface that our IEC_FunctionBlocks
 *  have to implement
 *  @author Anders Röding
 *  @version 1.0
 */
public interface IEC_FunctionBlock {

	/**run is used for il CAL operations of this kind
	 *   var
	 *      neg : fb;
	 *      a,b : BOOL;
	 *   end_var
	 *   LD TRUE
	 *   ST neg.inputvariable1
	 *   CAL neg
	 *   LD neg.outputvariable1
	 *   ST a
	 * and of this kind
	 *   var
	 *      neg : fb;
	 *      a,b : BOOL;
	 *   end_var
	 *   LD TRUE
	 *   ST a
	 *   CAL neg(inputvariable1 := a, outputvariable2 => b)
	 * and run is also called from within runArgs
	 */
	void run();

	/*runArgs is used for il CAL operations of this kind
	 *   var
	 *      neg : fb;
	 *      a,b : BOOL;
	 *   end_var
	 *   LD TRUE
	 *   ST a
	 *   CAL neg(a)
	 *   LD neg.outputvariable1
	 *   ST a
	 * where neg's internal name of the inputvariable is unknown
	 * Call operations using this style must have the appropriate number
	 * of inputs
	 */

	//void runArgs(String a, int b)
	// is an example of a runArgs method for a function block with two inputs

	// IEC_FunctionBlock(Object owner);
	// owner should be used for implementing var_external declaration.
}
