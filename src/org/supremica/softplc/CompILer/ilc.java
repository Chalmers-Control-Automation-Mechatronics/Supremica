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
	if (args.length < 1)
	    {
		System.err.println("Usage: ilcompiler file.il");
		return;
	    }
	try {
	    BufferedReader ilReader = new BufferedReader(new FileReader(new File(args[0])));
	    new ilc(ilReader);
	}
	catch (Throwable e) {
	    System.err.println("Error reading file " + args[0]);
	}
    }

    public ilc(BufferedReader ilReader) {
	this(ilReader, null);
    }

    public ilc(BufferedReader ilReader, Logger logger) {
	try {
	    parser p = new parser(ilReader);
	    
	    try {
		SimpleNode n = p.Start();
		
		n.dump("");
		
		new VariableChecker(n);
		//XXX new VaribleChecker(n,logger);
		
		n.dump("");
		
		JavaBytecodeGenerator jb = new JavaBytecodeGenerator(n, null/*output directory*/);
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
