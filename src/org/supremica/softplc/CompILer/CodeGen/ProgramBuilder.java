package org.supremica.softplc.CompILer.CodeGen;

import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;
import java.io.File;

public class ProgramBuilder
    extends ProgramAndFBBuilder
{
    private String[] programInterfaces = { "org.supremica.softplc.CompILer.CodeGen.IEC_Interfaces.IEC_Program" };
    /* references to the direct variable fields */
    String directOutput = "directOutput";
    String directInput = "directInput";
    private File temp;

    /**Constructor ProgramBuilder constructs a new frame for
     * IL program generation
     *@param programName name of IL program be generated (i.e. classfile name)
     */
    public ProgramBuilder(String programName)
    {
	implementedInterfaces = programInterfaces;
	className = programName;

	//	try {
	    //temp = File.createTempFile("ilc", ".class");
	    classFileName = programName.concat(".class"); //temp.getCanonicalPath();
	    //temp.deleteOnExit();
	    //}
	    //catch (Exception e) { System.err.println(e); }

	// create the new program class
	classGen = new ClassGen(className, "java.lang.Object", "<generated>", 
				Constants.ACC_PUBLIC, implementedInterfaces);
	constPoolGen = classGen.getConstantPool();
	fac = new InstructionFactory(classGen, constPoolGen);

	/* crete references to direct variable arrays */
	FieldGen var = new FieldGen(Constants.ACC_PRIVATE, new ArrayType(Type.BOOLEAN, 1), 
				    directOutput, constPoolGen);

	classGen.addField(var.getField());
	constPoolGen.addFieldref(className, directOutput, "[Z");

	var = new FieldGen(Constants.ACC_PRIVATE, new ArrayType(Type.BOOLEAN, 1), 
			   directInput, constPoolGen);

	classGen.addField(var.getField());
	constPoolGen.addFieldref(className, directInput, "[Z");

	/* create the run method of the IL program */
	mgRun = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS, null, 
			      "run", className, ilRun, constPoolGen);
	/* create MethodGen for constructor method */
	mgInit = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, 
			       new Type[]{ new ArrayType(Type.BOOLEAN, 1),
					   new ArrayType(Type.BOOLEAN, 1) }, 
			       new String[]{ "inputSignals","outputSignals" }, "<init>", 
			       className, ilInit, constPoolGen);

	/* run super class' <init> method */
	ilInit.append(InstructionConstants.ALOAD_0);
	ilInit.append(fac.createInvoke("java.lang.Object", "<init>", Type.VOID, 
				       Type.NO_ARGS, Constants.INVOKESPECIAL));
	/* initialize arrays for direct variables */
	ilInit.append(InstructionConstants.THIS);    // this
	ilInit.append(InstructionConstants.ALOAD_1);    /* reference to directInput array */
	ilInit.append(fac.createFieldAccess(className, directInput, 
					    new ArrayType(Type.BOOLEAN, 1), 
					    Constants.PUTFIELD));
	ilInit.append(InstructionConstants.THIS);    // this
	ilInit.append(InstructionConstants.ALOAD_2);    /* reference to directOutput array */
	ilInit.append(fac.createFieldAccess(className, directOutput, 
					    new ArrayType(Type.BOOLEAN, 1), 
					    Constants.PUTFIELD));
    }

    public File getTempFile() {
    	return temp;
    }

    public void emitDirectInit(IECDirectVariable v, TypeBOOL i)
    {
	if (v.isInput())
	    {
		System.err.println("Initialisation of direct input variables not allowed.");
		errorsPresent = true;
	    }
	else
	    {
		ilInit.append(new PUSH(constPoolGen, i.getValue()));
		ilInit.append(emitStoreVariable(v));
	    }
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
	InstructionList il = new InstructionList();
	TypeConstant type = var.getType();
	int nr = var.getNumber();

	if (type == TypeConstant.T_BOOL)
	    {
		String source = directInput;
		if (!var.isInput())
		{    
		    source = directOutput;
		}
		il.append(InstructionConstants.THIS);
		il.append(fac.createFieldAccess(className, source, 
						new ArrayType(Type.BOOLEAN, 1), 
						Constants.GETFIELD));
		il.append(new PUSH(constPoolGen, nr));    // ladda pos
		il.append(fac.createArrayLoad(Type.BOOLEAN));
	    }
	else
	    {
		System.err.println("Loading direct variables of type " + type + 
				   " not yet implemented");
		errorsPresent = true;
	    }
	return il;
    }

    /**emitStoreVariable takes the top of stack value and stores it in
     * the specified variable
     * This method also applies to AT-defined symbolic variables since these
     * already should have been changed into DirectVariables by a Checker.
     * @param var variable to store TOS value in
     */
    InstructionList emitStoreVariable(IECDirectVariable var)
    {
	InstructionList il = new InstructionList();
	TypeConstant type = var.getType();
	int nr = var.getNumber();

	if (var.isInput())
	{
		System.err.println(var + " is read only");
		errorsPresent = true;
	    }
	else
	    {
		if (type == TypeConstant.T_BOOL)
		    {
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, directOutput, 
							new ArrayType(Type.BOOLEAN, 1), 
							Constants.GETFIELD));
			il.append(InstructionConstants.SWAP);
			il.append(new PUSH(constPoolGen, nr));    // load position
			il.append(InstructionConstants.SWAP);
			il.append(fac.createArrayStore(Type.BOOLEAN));
		    }
		else
		    {
			System.err.println("Storing direct variables of type " + type + 
					   " not yet implemented");
			errorsPresent = true;
		    }
	    }

	return il;
    }
}
