package org.supremica.softplc.CompILer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.Logger;

import org.supremica.softplc.CompILer.Checker.VariableChecker;
import org.supremica.softplc.CompILer.CodeGen.JavaBytecodeGenerator;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.SimpleNode;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.parser;

/**
 * ilc is a compiler for parts of the IEC Instruction List language.
 *
 * @author Anders Röding
 */

public class ilc
{
	private static parser p;

	public static void main(final String[] args)
	{
		if (args.length != 2)
		{
			System.err.println("Usage: ilcompiler file.il outputDir");

			return;
		}

		new ilc(args[0], args[1]);
	}

	public ilc(final String ilFile, final String outDir)
	{
		this(ilFile, outDir, null, false);
	}

	public ilc(final String ilFile, final String outDir, final Logger logger, final boolean debug)
	{
		try
		{
			final BufferedReader ilReader = new BufferedReader(new FileReader(new File(ilFile)));

			if (logger != null)
			{
				logger.info("Compiling " + ilFile + "...");
			}
			else
			{
				System.out.println("Compiling " + ilFile + "...");
			}

			if (p == null)
			{
				p = new parser(ilReader);
			}
			else
			{
				parser.ReInit(ilReader);
			}

			try
			{
				final SimpleNode n = parser.Start();
				final VariableChecker v = new VariableChecker(n);

				//XXX new VaribleChecker(n,logger);
				if (v.check())
				{
					new JavaBytecodeGenerator(n, outDir, logger, debug);
				}
				else
				{
					System.err.println("VariableChecker failed");
				}
			}
			catch (final Exception e)
			{
				if (logger != null)
				{
					logger.error(e.getMessage());
				}
				else
				{
					System.out.println(e.getMessage());
				}

				e.printStackTrace();
			}
		}
		catch (final Throwable e)
		{
			if (logger != null)
			{
				logger.error("Unable to parse input " + e);
			}
			else
			{
				System.out.println("Unable to parse input " + e);
			}
		}
	}
}
