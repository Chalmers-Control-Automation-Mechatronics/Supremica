/** ilc is a compiler for parts of the IEC Instruction List language.
 * @author Anders R�ding
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
	if (args.length != 2)
	    {
		System.err.println("Usage: ilcompiler file.il outputDir");
		return;
	    }
	new ilc(args[0], args[1]);
    }

    public ilc(String ilFile, String outDir) {
	this(ilFile, outDir, null, false);
    }

    public ilc(String ilFile, String outDir, Logger logger, boolean debug) {
	try {
	    BufferedReader ilReader = new BufferedReader(new FileReader(new File(ilFile)));
	    
	    if (logger != null)
		logger.info("Compiling " + ilFile + "...");
	    else
		System.out.println("Compiling " + ilFile + "...");
	    parser p = new parser(ilReader);
	    
	    try {
		SimpleNode n = p.Start();
		
		// XXX
		//n.dump("");
		
		new VariableChecker(n);
		//XXX new VaribleChecker(n,logger);
		//System.out.println("VarChecker f�rdig");
		// XXX
		//n.dump("");
		
		JavaBytecodeGenerator jb = new JavaBytecodeGenerator(n, outDir, logger, debug);
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
