/** ilc is a compiler for parts of the IEC Instruction List language.
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer;

import org.supremica.softplc.CompILer.CodeGen.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.Checker.*;
import org.supremica.log.*;
import java.io.*;

public class ilc
{
    public static void main(String[] args) {
	System.out.println("ilc startar...");
	if (args.length != 2)
	    {
		System.err.println("Usage: ilcompiler file.il outputDir");
		return;
	    }
	new ilc(args[1], args[2]);
    }

    public ilc(String ilFile, String outDir) {
	this(ilFile, outDir, null);
    }

    public ilc(String ilFile, String outDir, Logger logger) {
	try {
	    BufferedReader ilReader = new BufferedReader(new FileReader(new File(ilFile)));
	
	    parser p = new parser(ilReader);
	    
	    try {
		SimpleNode n = p.Start();
		
		n.dump("");
		
		new VariableChecker(n);
		//XXX new VaribleChecker(n,logger);
		
		n.dump("");
		
		JavaBytecodeGenerator jb = new JavaBytecodeGenerator(n, outDir);
		//XXXnew JavaBytecodeGenerator(n, null/*output directory*/, logger);
		File temp = jb.getTempFile();
	    }
	    catch (Exception e)
		{
		    System.out.println("Ooops");
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
	}
	catch (Throwable e)
	    {
		System.out.println("Unable to parse input " + e);
	    }
    }
}
