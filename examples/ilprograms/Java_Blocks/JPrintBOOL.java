/**
 * This class is designed for printing in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * BOOL
 */
public class JPrintBOOL implements 
     org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
	
    public boolean output;
    public void run() {
	System.out.print(output);
    }
}
