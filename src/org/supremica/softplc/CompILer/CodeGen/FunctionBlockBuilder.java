package org.supremica.softplc.CompILer.CodeGen;
import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;
import java.io.File;

/** The class FunctionBlockBuilder is used to generate code parts that
 *  are specific to Function Blocks and not common with Programs. The
 *  common parts of code are instead generated in @see ProgramAndFBBuilder.
 */
public class FunctionBlockBuilder
	extends ProgramAndFBBuilder
{
	private String[] programInterfaces = {"org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock" };

	/*
	 * BCEL objects used to create bytecode for the runArgs method
	 *     that only function blocks has
	 */
	InstructionList ilRunArgs = new InstructionList();
	MethodGen mgRunArgs;

    public File getTempFile() {return null;};

	/**Constructor ProgramBuilder constructs a new frame for
	 * IL program generation
	 *@param functionBlockName name of IL function block be
	 *                         generated (i.e. classfile name)
	 */
	public FunctionBlockBuilder(String functionBlockName)
	{
		implementedInterfaces = programInterfaces;
		className = functionBlockName;
		classFileName = className.concat(".class");

		/* create the new function block class */
		classGen = new ClassGen(className, "java.lang.Object", "<generated>",
								Constants.ACC_PUBLIC, implementedInterfaces);
		constPoolGen = classGen.getConstantPool();
		fac = new InstructionFactory(classGen, constPoolGen);
		/* create the run method of the IL program */
		mgRun = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
							  null, "run", className, ilRun, constPoolGen);
		/* create the runArgs method of the IL program */
		mgRunArgs = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
								  Type.NO_ARGS, null, "runArgs", className,
								  ilRunArgs, constPoolGen);
		/* create MethodGen for constructor method */
		mgInit = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
							   Type.NO_ARGS, new String[]{}, "<init>",
							   className, ilInit, constPoolGen);

		/* run super class' <init> method */
		ilInit.append(InstructionConstants.ALOAD_0);
		ilInit.append(fac.createInvoke("java.lang.Object", "<init>",
									   Type.VOID, Type.NO_ARGS,
									   Constants.INVOKESPECIAL));
		/* XXX initialize owner field */

		// XXX varför behöver man owner???? Anders undrar vad han tänkt på:(
		// behövs det så skall det troligen ha typen IECProgram eller IECFunctionBlock eller IECProgramOrganisationUnit
		// ilInit.append(InstructionConstants.THIS); //this
		// ilInit.append(InstructionConstants.ALOAD_1);
		// ilInit.append(fac.createFieldAccess(className,owner,
		// Type.OBJECT,
		// Constants.PUTFIELD));
	}

	/**should be called when the IL function block generation is finished.
	 * This method will then dump the generated code to a class file.
	 */
	public void dumpCode()
	{
		ilRunArgs.append(InstructionConstants.RETURN);
		mgRunArgs.setMaxStack();
		classGen.addMethod(mgRunArgs.getMethod());
		super.dumpCode();
	}

	/*
	 * Generates a bytecode field representing a specific variable.
	 * @param varName the name of the variable
	 * @param type the CompILer type of the variable (e.g. T_BOOL)
	 * @param global decide whether this variable should be globally declared
	 *               or not, only false is allowed in Function Blocks
	 * @param inputOutputVar decide whether this variable should be
	 *                       accessible from parent POU (e.g. Program that
	 *                       instanciated the FunctionBlock)
	 *
	 */
	public void emitVarField(String varName, Object type, boolean global,
							 boolean inputOutputVar)
	{

		/*
		 * XXX Det mesta av koden här bör kanske läggas i en egen class
		 * för field-generering(global variables are only allowed at
		 *               configuration, resource or program level)
		 */
		if (global)
		{
			System.err.println("Global variables not allowed in function " +
							   "blocks");
			errorsPresent = true;
		}
		else
		{
			super.emitVarField(varName, type, global, inputOutputVar);
		}
	}

	/**emitDirectInit is used to set init values to direct output variables,
	 * but in the case of function blocks direct variables are not allowed
	 * and errors will be generated.
	 * @param v the direct variable
	 * @param i the value the variable should be set to
	 */
	public void emitDirectInit(IECDirectVariable v, TypeBOOL i)
	{
		System.err.println("Direct variables are not allowed in function");
		System.err.println("and therefore you can't initialise such vars.");

		errorsPresent = true;
	}

	/**emitLoadVariable pushes the value of a specified variable
	 * on the stack without manipulating
	 * previous stack values
	 * This method also applies to AT-defined variables since these
	 * already should have been changed into DirectVariables by a Checker.
	 * @param var direct variable to load
	 */
	InstructionList emitLoadVariable(IECDirectVariable var)
	{
		System.err.println("Reference to direct variable not allowed " +
						   "in function blocks");
		errorsPresent = true;
		return new InstructionList();
	}

	/**emitStoreVariable takes the top of stack value and stores it in
	 * the specified variable
	 * @param var variable to store TOS value in
	 */
	InstructionList emitStoreVariable(IECDirectVariable var)
	{
		System.err.println("Reference to direct variable not allowed " +
						   "in function blocks");
		errorsPresent = true;
		return new InstructionList();
	}
}
