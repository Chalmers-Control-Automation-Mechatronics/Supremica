/**
 * This class is designed for printing (with newline) in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * REAL
 */
public class JPrintlnREAL implements 
     org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
	
    public float output;
    public void run() {
	System.out.println(output);
    }
}
