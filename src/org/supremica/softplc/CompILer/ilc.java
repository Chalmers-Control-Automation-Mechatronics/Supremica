/** ilc is a compiler for parts of the IEC Instruction List language.
 * @author Anders Röding
 */
package org.supremica.softplc.CompILer;

import org.supremica.softplc.CompILer.CodeGen.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.Checker.*;
import java.io.*;

public class ilc
{
    public static void main(String[] args) {
	if (args.length < 1)
	    {
		System.err.println("Usage: ilcompiler file.il");
		return;
	    }
	try {
	    BufferedReader ilReader = new BufferedReader(new FileReader(new File(args[0])));
	}
	catch (Throwable e) {
	    System.err.println("Error reading file " + args[0]);
	}
    }

    public ilc(BufferedReader ilReader) {
	try {
	    parser p = new parser(ilReader);
	    
	    try {
		SimpleNode n = p.Start();
		
		n.dump("");
		
		new VariableChecker(n);
		
		n.dump("");
		
		JavaBytecodeGenerator jb = new JavaBytecodeGenerator(n, null/*output directory*/);
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
    
    /*
      public static void main(String[] args)
      {
      try
      {
      parser p = new parser(new BufferedReader(new FileReader(new File(args[0]))));
      
      try
		{
		SimpleNode n = p.Start();
		
		n.dump("");
		
		new VariableChecker(n);

				n.dump("");

				JavaBytecodeGenerator jb = new JavaBytecodeGenerator(n, null);
				File temp = jb.getTempFile();
			}
			catch (Exception e)
			{
				System.out.println("Oops.");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		catch (Throwable e)
		{
			System.out.println("Unable to parse input " + e);
		}
	}
    */
}
