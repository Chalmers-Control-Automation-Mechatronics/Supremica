/**
 * This class is designed for printing in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * WSTRING
 */
    public class JPrintWSTRING implements 
     org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
	
	public String output;
	public void run() {
	    System.out.print(output);
	}
    }
