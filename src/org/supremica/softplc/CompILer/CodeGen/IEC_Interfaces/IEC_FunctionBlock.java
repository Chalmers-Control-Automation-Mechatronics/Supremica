package org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces;

/**
 *  Interface that our IEC_FunctionBlocks
 *  have to implement
 *  @author Anders Röding
 *  @version 1.0
 */
public interface IEC_FunctionBlock
{
	/**run is used for il CAL operations of this kind ie.
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
	 */
	void run();
}
