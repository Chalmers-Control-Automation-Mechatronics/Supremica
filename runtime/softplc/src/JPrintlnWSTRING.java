/**
 * This class is designed for printing (with newline) in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * WSTRING
 */
    public class JPrintlnWSTRING implements 
     org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
	
	public String output;
	public void run() {
	    System.out.println(output);
	}
    }
