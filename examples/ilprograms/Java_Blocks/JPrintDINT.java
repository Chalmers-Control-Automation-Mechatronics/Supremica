/**
 * This class is designed for printing in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * DINT
 */
public class JPrintDINT implements 
    org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
    
    public int output;
    public void run() {
	System.out.print(output);
    }
}
