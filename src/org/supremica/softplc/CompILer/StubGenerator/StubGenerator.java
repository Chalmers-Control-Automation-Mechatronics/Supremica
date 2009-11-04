package org.supremica.softplc.CompILer.StubGenerator;

import java.lang.reflect.*;
import java.io.*;

/**
 * Given a Class a IEC 61131 function block with the corresponding attributes
 * will be generated.
 */
public class StubGenerator
{
	public StubGenerator() {}

	/**
	 * Create an IEC 61131 Function block declaration.
	 */
	public void generate(Class<?> theClass, PrintWriter pw)
	{
		pw.print("FUNCTION_BLOCK ");

		String className = theClass.getName();
		String mangledClassName = className.replace('.', '_');

		pw.println(mangledClassName);
		pw.println("(* Original class name: " + className + " *)");
		pw.println("\tVAR_IN_OUT");

		Field[] theFields = theClass.getFields();

		for (int i = 0; i < theFields.length; i++)
		{
			Field currField = theFields[i];
			int modifiers = currField.getModifiers();

			if (Modifier.isPublic(modifiers))
			{
				Class<?> currFieldType = currField.getType();
				String currFieldName = currField.getName();

				if (currFieldType.isPrimitive())
				{
					if (currFieldType == Boolean.TYPE)
					{
						pw.println("\t\t" + currFieldName + " : BOOL;");
					}
					else if (currFieldType == Integer.TYPE)
					{
						pw.println("\t\t" + currFieldName + " : DINT;");
					}
					else if (currFieldType == Float.TYPE)
					{
						pw.println("\t\t" + currFieldName + " : REAL;");
					}
				}
				else
				{    // Do nothing
				}
			}
		}

		pw.println("\tEND_VAR");

		//pw.println("END:");
		pw.println("END_FUNCTION_BLOCK");
	}

	public static void main(String[] args)
	{
		StubGenerator generator = new StubGenerator();

		generator.generate(java.lang.Integer.class, new PrintWriter(System.out, true));
	}
}
