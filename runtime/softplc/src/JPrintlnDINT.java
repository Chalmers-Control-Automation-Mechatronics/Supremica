/**
 * This class is designed for printing (with newline) in Instruction List
 * programs on standard output.
 * Handles IL data type:
 * DINT
 */
public class JPrintlnDINT implements 
    org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock {
    
    public int output;
    public void run() {
	System.out.println(output);
    }
}
