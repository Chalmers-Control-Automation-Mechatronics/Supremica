/**
 * This class is designed for printing in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * REAL
 */
public class JPrintREAL implements 
     org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
	
    public float output;
    public void run() {
	System.out.print(output);
    }
}
