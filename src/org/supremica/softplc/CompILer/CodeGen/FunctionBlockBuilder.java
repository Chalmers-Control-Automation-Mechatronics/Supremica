package org.supremica.softplc.CompILer.CodeGen;
import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import org.supremica.log.Logger;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;
import java.io.File;

/**
 * The class FunctionBlockBuilder is used to generate java bytecode parts that
 * are specific to IL function blocks and not common with IL programs. The
 * common parts of code are instead generated in {@link ProgramAndFBBuilder}
 * @author Anders Röding
 */
public class FunctionBlockBuilder
    extends ProgramAndFBBuilder
{
    /**
     * array of interfaces that IL function blocks should implement
     */
    private static String[] interfaces = {"org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_FunctionBlock" };

	//XXX skall denna finns
    public File getTempFile() {return null;};

    /**Constructor ProgramBuilder constructs a new frame for
     * IL program generation
     * @param functionBlockName name of IL function block be
     *                         generated (i.e. classfile name)
     * @param outDir output directory for class files
     * @param logger a logger object for messages
     * @param debug set whether debug messages should appear at standard output.
     *              Only used if there is no logger provided
     */
    public FunctionBlockBuilder(String functionBlockName, String outDir, Logger logger, 
				boolean debug)
    {
	this.logger = logger;
	this.debug = debug;
	implementedInterfaces = interfaces;
	className = functionBlockName;
	classFileName = outDir + "/" + className.concat(".class");

	/* create the new function block class */
	classGen = new ClassGen(className, "java.lang.Object", "<generated>",
				Constants.ACC_PUBLIC, implementedInterfaces);
	constPoolGen = classGen.getConstantPool();
	fac = new InstructionFactory(classGen, constPoolGen);
	/* create the run method of the IL program */
	mgRun = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
			      null, "run", className, ilRun, constPoolGen);
	/* create MethodGen for constructor method */
	mgInit = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
			       Type.NO_ARGS, new String[]{}, "<init>",
			       className, ilInit, constPoolGen);

	/* run super class' <init> method */
	ilInit.append(InstructionConstants.ALOAD_0);
	ilInit.append(fac.createInvoke("java.lang.Object", "<init>",
				       Type.VOID, Type.NO_ARGS,
				       Constants.INVOKESPECIAL));
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
	if (global)
	    {
		error("Global variables not allowed in function blocks");
	    }
	else
	    {
		super.emitVarField(varName, type, global, inputOutputVar);
	    }
    }

    /**
     * emitDirectInit is used to set init values to direct output variables,
     * but in the case of function blocks direct variables are not allowed
     * and errors will be generated.
     * @param v the direct variable
     * @param i the value the variable should be set to
     */
    public void emitDirectInit(IECDirectVariable v, TypeBOOL i)
    {
	error("Direct variables are not allowed in function and therefore " + 
	      "you can't initialise such vars." + v);
    }

    /**
     * emitLoadVariable pushes the value of a specified variable
     * on the stack without manipulating
     * previous stack values
     * This method also applies to AT-defined variables since these
     * already should have been changed into DirectVariables by a Checker.
     * @param var direct variable to load
     */
    InstructionList emitLoadVariable(IECDirectVariable var)
    {
	error("Reference to direct variable not allowed in function blocks"+var);
	return new InstructionList();
    }

    /**
     * emitStoreVariable takes the top of stack value and stores it in
     * the specified variable
     * @param var variable to store TOS value in
     */
    InstructionList emitStoreVariable(IECDirectVariable var)
    {
	error("Reference to direct variable not allowed in function blocks"+var);
	return new InstructionList();
    }
}
