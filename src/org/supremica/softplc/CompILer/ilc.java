package org.supremica.softplc.CompILer;

import org.supremica.softplc.CompILer.CodeGen.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.Checker.*;
import java.io.*;

public class ilc
{
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.err.println("Usage: ilcompiler file.il");
			return;
		}

		try
		{
			parser p = new parser(new BufferedReader(new FileReader(new File(args[0]))));

			try
			{
				SimpleNode n = p.Start();

				n.dump("");

				new VariableChecker(n);

				n.dump("");

				new JavaBytecodeGenerator(n, null);
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
}
