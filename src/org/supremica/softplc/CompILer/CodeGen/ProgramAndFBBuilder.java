package org.supremica.softplc.CompILer.CodeGen;

import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;
import java.io.File;

/**The class ProgramAndFBBuilder handles all common parts of code generation
 * for its sub classes.
 */
public abstract class ProgramAndFBBuilder
	implements Builder
{
	/* if errorsPresent is set to true no bytecode will be dumped */
	boolean errorsPresent = false;
	String className;
	String classFileName;
	String[] implementedInterfaces;

	/* name of owner field for use with external variables */

	// XXX Vad skall man ha denna till undrar Anders, som inte vet
	// XXX varför han har skapat den.
	// XXX String owner = "owner";

	/* jumpController keeps track of all jumps contained in a POU */
	JumpController jumpController = new JumpController();
	/* BCEL objects used to create bytecode */
	ClassGen classGen;
	ConstantPoolGen constPoolGen;
	MethodGen mgRun;    /* methodGen for creating the run() method */
	MethodGen mgInit;    /* methodGen for creating the <init> method */
	InstructionList ilRun = new InstructionList();
	InstructionList ilInit = new InstructionList();
	InstructionFactory fac;


    

	/**dumpCode should be called when the IL POU generation is finished.
	 * This method will then dump the generated code to a class file.
	 */
	public void dumpCode()
	{
		ilRun.append(InstructionConstants.RETURN);
		ilInit.append(InstructionConstants.RETURN);
		mgRun.setMaxStack();
		mgInit.setMaxStack();
		classGen.addMethod(mgRun.getMethod());
		classGen.addMethod(mgInit.getMethod());

		if (!errorsPresent)
		{
			try
			{
				classGen.getJavaClass().dump(classFileName);
			}
			catch (java.io.IOException e)
			{
				System.err.println(e);
			}
		}
		else
		{
			System.err.println(className + " contains errors.");
			System.err.print("No code was written for program organization");
			System.err.println(" unit " + className + ".");
		}
	}

	/**emitVarField generates a bytecode field representing a specific variable
	 * @param varName the name of the variable
	 * @param type the CompILer type of the variable (e.g. T_BOOL)
	 * @param global decide whether this variable should be globally declared
	 *               or not (global variables are only allowed at
	 *               configuration, resource or program level)
	 * @param inputOutputVar decide whether this variable should be
	 *                       accessible from owner POU (e.g. Program)
	 */
	public void emitVarField(String varName, Object type, boolean global,
							 boolean inputOutputVar)
	{
		if (type instanceof TypeConstant)
		{
			emitVarField(varName, (TypeConstant) type, global, inputOutputVar);
		}
		else if (type instanceof TypeANY_ELEMENTARY)
		{
			emitVarField(varName, (TypeANY_ELEMENTARY) type, global,
						 inputOutputVar);
		}
		else if (type instanceof TypeFUNCTION_BLOCK)
		{
			emitVarField(varName, (TypeFUNCTION_BLOCK) type, global,
						 inputOutputVar);
		}
		else
		{
			System.err.println("Variables of this kind " + type.toString() +
							   "is not implemented.");
			errorsPresent = true;
		}
	}

	/**emitVarField generates a bytecode field representing a specific variable
	 * @param varName the name of the variable
	 * @param type the CompILer type of the variable (e.g. T_BOOL)
	 * @param global decide whether this variable should be globally declared
	 *               or not (global variables are only allowed at
	 *               configuration, resource or program level)
	 * @param inputOutputVar decide whether this variable should be
	 *                       accessible from owner POU (e.g. Program)
	 */
	public void emitVarField(String varName, TypeConstant type,
							 boolean global, boolean inputOutputVar)
	{
		int accessFlags = Constants.ACC_PRIVATE;
		if (global || inputOutputVar)
		{
			accessFlags = Constants.ACC_PUBLIC;
		}
		if (type == TypeConstant.T_BOOL)
		{
			FieldGen var = new FieldGen(accessFlags, Type.BOOLEAN, varName,
										constPoolGen);
			classGen.addField(var.getField());
			constPoolGen.addFieldref(className, varName,"Z"/*bcode basetype*/);
		}
		else if (type == TypeConstant.T_DINT)
		{
			FieldGen var = new FieldGen(accessFlags, Type.INT, varName,
										constPoolGen);
			classGen.addField(var.getField());
			constPoolGen.addFieldref(className, varName,"I"/*bcode basetype*/);
		}
		else if (type == TypeConstant.T_REAL)
		{
			FieldGen var = new FieldGen(accessFlags, Type.FLOAT, varName,
										constPoolGen);
			classGen.addField(var.getField());
			constPoolGen.addFieldref(className, varName,"F"/*bcode basetype*/);
		}
		else if (type == TypeConstant.T_WSTRING)
		{
			FieldGen var = new FieldGen(accessFlags, Type.STRING, varName,
										constPoolGen);
			classGen.addField(var.getField());
			constPoolGen.addFieldref(className, varName, "Ljava/lang/String;");
		}
		else
		{
			System.err.println("Variables of this type not yet implemented: "
							   + type);
			errorsPresent = true;
		}
	}

	/**emitVarField generates a bytecode field representing a specific variable
	 * @param varName the name of the variable
	 * @param type the CompILer type of the variable
	 * @param global decide whether this variable should be globally declared
	 *               or not (global variables are only allowed at
	 *               configuration, resource or program level)
	 * @param inputOutputVar decide whether this variable should be
	 *                       accessible from owner POU (e.g. Program)
	 */
	public void emitVarField(String varName, TypeANY_ELEMENTARY type,
							 boolean global, boolean inputOutputVar)
	{
		TypeConstant t = type.getType();
		if ((t == TypeConstant.T_BOOL) || (t == TypeConstant.T_DINT) ||
			(t == TypeConstant.T_REAL) || (t == TypeConstant.T_WSTRING))
		{
			/* create a field */
			emitVarField(varName, type.getType(), global, inputOutputVar);
			/* initialise the variable */
			IECSymbolicVariable var = new IECSymbolicVariable(varName,
															  type.getType());
			ilInit.append(emitLoadG(type));
			ilInit.append(emitStoreVariable(var));
		}
		else
		{
			System.err.println("Initialised variables of type " +
							   type.getType() + " not implemented.");
			System.err.println("Try manual initialisation.");
			errorsPresent = true;
		}
	}

	/**emitVarField generates a bytecode field representing a specific variable
	 * @param varName the name of the variable
	 * @param type the CompILer type of the variable
	 * @param global decide whether this variable should be globally declared
	 *               or not (global variables are only allowed at
	 *               configuration, resource or program level)
	 * @param inputOutputVar decide whether this variable should be
	 *                       accessible from owner POU (e.g. Program)
	 */
	public void emitVarField(String varName, TypeFUNCTION_BLOCK type,
							 boolean global, boolean inputOutputVar)
	{
		// create a field
		int accessFlags = Constants.ACC_PRIVATE;
		if (global || inputOutputVar)
		{
			accessFlags = Constants.ACC_PUBLIC;
		}
		ObjectType fbType = new ObjectType(type.getName());
		FieldGen var = new FieldGen(accessFlags, fbType, varName,constPoolGen);

		classGen.addField(var.getField());
		constPoolGen.addFieldref(className, varName, "L" + type.getName()+";");
		/* create a new instance and make the field refer to the new instance*/
		ilInit.append(InstructionConstants.THIS);
		ilInit.append(fac.createNew(fbType));
		ilInit.append(InstructionConstants.DUP);
		ilInit.append(fac.createInvoke(type.getName(), "<init>", Type.VOID,
									   Type.NO_ARGS, Constants.INVOKESPECIAL));
		ilInit.append(fac.createFieldAccess(className, varName, fbType,
											Constants.PUTFIELD));
	}

	/**emitDirectInit is used to set init values to direct output variables
	 * @param v the direct variable
	 * @param i the value the variable should be set to
	 */
	abstract public void emitDirectInit(IECDirectVariable v, TypeBOOL i);

	/**emitLoadVariable pushes the value of a specified variable
	 * on the stack without manipulating
	 * previous stack values
	 * @param var variable to load
	 */
	InstructionList emitLoadVariable(IECVariable var)
	{
		if (var instanceof IECSymbolicVariable)
		{
			return emitLoadVariable((IECSymbolicVariable) var);
		}
		else
		{
			return emitLoadVariable((IECDirectVariable) var);
		}
	}

	/**emitLoadVariable pushes the value of a specified variable
	 * on the stack without manipulating
	 * previous stack values
	 * @param var symbolic variable to load
	 */
	InstructionList emitLoadVariable(IECSymbolicVariable var)
	{
		InstructionList il = new InstructionList();
		TypeConstant type = var.getType();
		String varName = var.getName();

		if (type == TypeConstant.T_BOOL)
		{
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, varName, Type.BOOLEAN,
											Constants.GETFIELD));
		}
		else if (type == TypeConstant.T_DINT)
		{
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, varName, Type.INT,
											Constants.GETFIELD));
		}
		else if (type == TypeConstant.T_REAL)
		{
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, varName, Type.FLOAT,
											Constants.GETFIELD));
		}
		else if (type == TypeConstant.T_WSTRING)
		{
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, varName, Type.STRING,
											Constants.GETFIELD));
		}
		else if (type == TypeConstant.T_DERIVED /*including function blocks */)
		{
			if (var.getFieldSelector() != null)
			{    /* we have got a variable in a derived variable */

				/* first get reference to the derived variable object*/
				il.append(InstructionConstants.THIS);
				il.append(fac.createFieldAccess
						  (className, varName,
						   new ObjectType(var.getTypeName()),
						   Constants.GETFIELD));
				/* then load value */
				type = var.getFieldSelectorType();

				// int och real och string måste vara med
				if (type == TypeConstant.T_BOOL)
				{
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.BOOLEAN
							   , Constants.GETFIELD));
				}
				else if (type == TypeConstant.T_DINT)
				{
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.INT
							   , Constants.GETFIELD));
				}
				else if (type == TypeConstant.T_REAL)
				{
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.FLOAT
							   , Constants.GETFIELD));
				}
				else if (type == TypeConstant.T_WSTRING)
				{
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.STRING
							   , Constants.GETFIELD));
				}
				else
				{
					System.err.println("IO variables in function blocks can "
									   + "not be of type " + type);
					errorsPresent = true;
				}
			}
			else
			{    /* the derived variable itself should be loaded*/
				// XXX should implement this
				System.err.println("Only loading of elements in " +
								   "derived variables are implemented " +
								   "so far.");
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Loading symbolic variables of type " + type +
							   " not yet implemented");
			errorsPresent = true;
		}
		return il;
	}

	/**emitLoadVariable pushes the value of a specified variable
	 * on the stack without manipulating
	 * previous stack values
	 * This method also applies to AT-defined variables since these
	 * already should have been changed into DirectVariables by a Checker.
	 * @param var direct variable to load
	 */
	abstract InstructionList emitLoadVariable(IECDirectVariable var);

	/**emitStoreVariable takes the top of stack value and stores it in
	 * the specified variable
	 * @param var variable to store TOS value in
	 */
	InstructionList emitStoreVariable(IECVariable var)
	{
		if (var instanceof IECSymbolicVariable)
		{
			return emitStoreVariable((IECSymbolicVariable) var);
		}
		else
		{
			return emitStoreVariable((IECDirectVariable) var);
		}
	}

	/**emitStoreVariable takes the top of stack value and stores it in
	 * the specified variable
	 * @param var variable to store TOS value in
	 */
	InstructionList emitStoreVariable(IECSymbolicVariable var)
	{
		InstructionList il = new InstructionList();
		TypeConstant type = var.getType();
		String varName = var.getName();

		if (type == TypeConstant.T_BOOL)
		{
			il.append(InstructionConstants.THIS);
			il.append(InstructionConstants.SWAP);
			il.append(fac.createFieldAccess(className, varName, Type.BOOLEAN,
											Constants.PUTFIELD));
		}
		else if (type == TypeConstant.T_DINT)
		{
			il.append(InstructionConstants.THIS);
			il.append(InstructionConstants.SWAP);
			il.append(fac.createFieldAccess(className, varName, Type.INT,
											Constants.PUTFIELD));
		}
		else if (type == TypeConstant.T_REAL)
		{
			il.append(InstructionConstants.THIS);
			il.append(InstructionConstants.SWAP);
			il.append(fac.createFieldAccess(className, varName, Type.FLOAT,
											Constants.PUTFIELD));
		}
		else if (type == TypeConstant.T_WSTRING)
		{
			il.append(InstructionConstants.THIS);
			il.append(InstructionConstants.SWAP);
			il.append(fac.createFieldAccess(className, varName, Type.STRING,
											Constants.PUTFIELD));
		}
		else if (type == TypeConstant.T_DERIVED /*including function blocks*/)
		{
			if (var.getFieldSelector() != null)
			{	/* reference to a variable in the derived variable */
				/* first get reference to derived variable object */
				il.append(InstructionConstants.THIS);
				il.append(fac.createFieldAccess
						  (className, varName,
						   new ObjectType(var.getTypeName()),
						   Constants.GETFIELD));
				/* then store value*/
				type = var.getFieldSelectorType();

				if (type == TypeConstant.T_BOOL)
				{
					il.append(InstructionConstants.SWAP);
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.BOOLEAN,
							   Constants.PUTFIELD));
				}
				else if (type == TypeConstant.T_DINT)
				{
					il.append(InstructionConstants.SWAP);
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.INT,
							   Constants.PUTFIELD));
				}
				else if (type == TypeConstant.T_REAL)
				{
					il.append(InstructionConstants.SWAP);
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.FLOAT,
							   Constants.PUTFIELD));
				}
				else if (type == TypeConstant.T_WSTRING)
				{
					il.append(InstructionConstants.SWAP);
					il.append(fac.createFieldAccess
							  (/*fb type name*/var.getTypeName(),
							   var.getFieldSelector(), Type.STRING,
							   Constants.PUTFIELD));
				}
				else
				{
					System.err.println("IO variables in function blocks can "+
									   "not have type " + type);
					errorsPresent = true;
				}
			}
			else
			{    /* the derived variable itself */
				// XXX should implement this
				System.err.println("Only loading of elements in " +
								   "derived variables are implemented " +
								   "so far.");
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Storing variables of type " + type +
							   " not yet implemented");
			errorsPresent = true;
		}
		return il;
	}

	/**emitStoreVariable takes the top of stack value and stores it in
	 * the specified variable
	 * This method also applies to AT-defined variables since these
	 * already should have been changed into DirectVariables by a Checker.
	 * @param var variable to store TOS value in
	 */
	abstract InstructionList emitStoreVariable(IECDirectVariable var);

	/**emitIL_SIMPLE_OPERATION choose operator function
	 * @param oparator operator to choose
	 * @param arg operator argument
	 */
	public void emitIL_SIMPLE_OPERATION(String operator, Object arg)
	{
		IlSimpleOperator op = IlSimpleOperator.ADD;
		try
		{
			op = IlSimpleOperator.getOperator(operator);
		}
		catch (IllegalOperatorException e)
		{
			System.err.println("Illegal operator: " + operator);
			errorsPresent = true;
		}
		if (op == IlSimpleOperator.LD)
		{
			emitLD(arg);
		}
		else if (op == IlSimpleOperator.LDN)
		{
			emitLDN(arg);
		}
		else if (op == IlSimpleOperator.ST)
		{
			emitST(arg);
		}
		// else if (op == IlSimpleOperator.STN )
		// else if (op == IlSimpleOperator.NOT )
		else if (op == IlSimpleOperator.S)
		{
			emitS(arg);
		}
		else if (op == IlSimpleOperator.R)
		{
			emitR(arg);
		}
		// else if (op == IlSimpleOperator.S1  )
		// else if (op == IlSimpleOperator.R1  )
		// else if (op == IlSimpleOperator.CLK )
		// else if (op == IlSimpleOperator.CU  )
		// else if (op == IlSimpleOperator.CD  )
		// else if (op == IlSimpleOperator.PV  )
		// else if (op == IlSimpleOperator.IN  )
		// else if (op == IlSimpleOperator.PT  )
		else if (op == IlSimpleOperator.AND)
		{
			emitAND(arg);
		}
		else if (op == IlSimpleOperator.OR)
		{
			emitOR(arg);
		}
		// else if (op == IlSimpleOperator.XOR )
		else if (op == IlSimpleOperator.ANDN)
		{
			emitANDN(arg);
		}
		// else if (op == IlSimpleOperator.ORN )
		// else if (op == IlSimpleOperator.XORN)
		else if (op == IlSimpleOperator.ADD)
		{
			emitADD(arg);
		}
		else if (op == IlSimpleOperator.SUB)
		{
			emitSUB(arg);
		}
		else if (op == IlSimpleOperator.MUL)
		{
			emitMUL(arg);
		}
		else if (op == IlSimpleOperator.DIV)
		{
			emitDIV(arg);
		}
		else if (op == IlSimpleOperator.MOD)
		{
			emitMOD(arg);
		}
		else if (op == IlSimpleOperator.GT)
		{
			emitGT(arg);
		}
		else if (op == IlSimpleOperator.GE)
		{
			emitGE(arg);
		}
		else if (op == IlSimpleOperator.EQ)
		{
			emitEQ(arg);
		}
		else if (op == IlSimpleOperator.LT)
		{
			emitLT(arg);
		}
		else if (op == IlSimpleOperator.LE)
		{
			emitLE(arg);
		}
		else if (op == IlSimpleOperator.NE)
		{
			emitNE(arg);
		}
	}

     /**emitStackSpace is used when opening a new scope in IL
	 * @param size nr of spaces on JVM-stack to reserve
	 */
	public void emitStackSpace(int size)
	{
		if (size == 1)
		{
			ilRun.append(InstructionConstants.ICONST_0);/*reserves one space*/
		}
		else if (size == 2)
		{
			/*reserves two spaces*/
			ilRun.append(new PUSH(constPoolGen,(long)0));
		}
		else
		{
			System.err.println("param size (emitStackSpace) must be 1 or 2");
			//XXX ge fel här??
		}
	}

	/**emitLoadG pushes a value on the stack without manipulating
	 * previous values
	 * @param arg the value to push
	 * @return BCEL instructions for loading
	 */
	InstructionList emitLoadG(Object arg)
	{
		InstructionList il = new InstructionList();

		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();
			if (t == TypeConstant.T_DINT)
			{
				il.append(new PUSH(constPoolGen,
								   (int) ((TypeDINT) arg).getValue()));
			}
			else if (t == TypeConstant.T_REAL)
			{
				il.append(new PUSH(constPoolGen,
								   (float) ((TypeREAL) arg).getValue()));
			}
			else if (t == TypeConstant.T_BOOL)
			{
				il.append(new PUSH(constPoolGen,
								   (boolean) ((TypeBOOL) arg).getValue()));
			}
			else if (t == TypeConstant.T_WSTRING)
			{
				il.append(new PUSH(constPoolGen,
								   (String) ((TypeWSTRING) arg).getValue()));
			}
			else
			{
				System.err.println("Loading of " + arg.toString() +
								   " not yet " + "implemented.");
				errorsPresent = true;
			}
		}
		else if (arg instanceof IECVariable)
		{
			il.append(emitLoadVariable((IECVariable) arg));
		}
		else
		{
			System.err.println("Loading of " + arg + " not yet implemented.");
			errorsPresent = true;
		}
		return il;
	}

    /**emitLoad pushes a value on the stack without manipulating
    * previous values
    * @param arg the value to push
    */
    public void emitLoad(Object arg)
    {
		ilRun.append(emitLoadG(arg));
	}

     /**emitLD replaces the top of stack value (simulating IL's LD-instruction
	 * behaviour)
	 * @param arg the value to push
	 */
	private void emitLD(Object arg)
	{
		ilRun.append(InstructionConstants.POP);
		emitLoad(arg);
	}

	/**emitLDN replaces the top of stack value (simulating IL's LDN-instruction
	 * behaviour)
	 * @param arg the value to push
	 */
	private void emitLDN(Object arg)
	{
		if ((arg instanceof IECConstant &&
			 ((IECConstant)arg).getType() == TypeConstant.T_BOOL) ||
			(arg instanceof IECVariable &&
			 ((IECVariable)arg).getType() == TypeConstant.T_BOOL))
		{
			ilRun.append(InstructionConstants.POP);
			emitLoad(arg);
			/* negate value */
			InstructionHandle end_ldn, iffalse;
			BranchInstruction ifeq = new IFEQ(null);
			BranchInstruction jmp = new GOTO(null);
			ilRun.append(ifeq);    //if stack == false jump
			ilRun.append(new PUSH(constPoolGen, false));
			ilRun.append(jmp);
			iffalse = ilRun.append(new PUSH(constPoolGen, true));
			end_ldn = ilRun.append(InstructionConstants.NOP);
			ifeq.setTarget(iffalse);
			jmp.setTarget(end_ldn);
		}
		else
		{
			System.err.println("LDN can only be used on the BOOL datatype." +
							   "Not on " + arg);
			errorsPresent = true;
		}
	}

	/**emitST stores the top of stack value (IL's result register) in a
	 * specified variable
	 * @param arg where the value is to be stored
	 */
	private void emitST(Object arg)
	{    // ej färdig
		if (arg instanceof IECConstant)
		{
			System.err.println("Fatal error: Operator ST cannot " +
							   "store in constant");
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			/*
			 * first we must duplicate stack value to be able to keep
			 * IL's result register value
			 */
			if ((var.getType() == TypeConstant.T_BOOL) ||
				(var.getType() == TypeConstant.T_DINT) ||
				(var.getType() == TypeConstant.T_REAL) ||
				(var.getType() == TypeConstant.T_DERIVED) ||
				(var.getType() == TypeConstant.T_WSTRING))
			{
				ilRun.append(InstructionConstants.DUP);
				ilRun.append(emitStoreVariable(var));
			}
			else
			{
				System.err.println("Store for " + var.getType() +
								   " not yet implemented");
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Can't store in anything else than a variable");
			errorsPresent = true;
		}
	}

	/**emitS set the argument (a variable) to true if the TOS value is true
	 * @param arg an IL BOOL-variable
	 */
	private void emitS(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			System.err.println("Fatal error: Operator S cannot " +
							   "store in constant");
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			if (var.getType() == TypeConstant.T_BOOL)
			{
				BranchInstruction ifne = new IFEQ(null);
				InstructionHandle skipStore;
				ilRun.append(InstructionConstants.DUP);
				/* XXX
				 * need to check that IL's result reg has type BOOL,
				 *  (should be done in TypeChecker)
				 */
				ilRun.append(ifne);
				ilRun.append(new PUSH(constPoolGen, true));
				ilRun.append(emitStoreVariable(var));
				skipStore = ilRun.append(InstructionConstants.NOP);
				ifne.setTarget(skipStore);
			}
			else
			{
				System.err.println("Operator S can only be used on BOOL");
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitR set the argument (a variable) to false if the TOS value is true
	 * @param arg an IL BOOL-variable
	 */
	private void emitR(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			System.err.println("Fatal error: Operator R cannot " +
							   "store in constant");
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			if (var.getType() == TypeConstant.T_BOOL)
			{
				BranchInstruction ifne = new IFEQ(null);
				InstructionHandle skipStore;
				ilRun.append(InstructionConstants.DUP);
				/* XXX
				 * need to check that IL's result reg has type BOOL,
				 *  (should be done in TypeChecker)
				 */
				ilRun.append(ifne);
				ilRun.append(new PUSH(constPoolGen, false));
				ilRun.append(emitStoreVariable(var));
				skipStore = ilRun.append(InstructionConstants.NOP);
				ifne.setTarget(skipStore);
			}
			else
			{
				System.err.println("Operator S can only be used on BOOL");
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitADD add the argument with stack value and put the result
	 * on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitADD(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();
			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen,
									  ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.IADD);
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(new PUSH(constPoolGen,
									  ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FADD);
			}
			else
			{
				System.err.println("ADD not yet implemented for " + t);
				errorsPresent = true;
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();
			if ((var instanceof IECSymbolicVariable) &&
				(t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}
			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.IADD);
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.FADD);
			}
			else
			{
				System.err.println("ADD not yet implemented for "+ t);
				errorsPresent = true;
			}
		}
		else
		{
			System.err.println("Not yet implemented for "+ arg);
		}
	}

	private void emitAND(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_BOOL)
			{
				if (!((TypeBOOL) arg).getValue())
				{    // result AND FALSE -> False
					ilRun.append(InstructionConstants.POP);
					ilRun.append(InstructionConstants.ICONST_0);
				}
				/* else keep result register value */
			}
			else
			{
				System.err.println("Illegal type or not yet " +
								   "implemented (AND " + arg + ")");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) &&
				(t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}
			if (t == TypeConstant.T_BOOL)
			{
				ilRun.append(emitLoadVariable(var));

				InstructionHandle end_and;
				BranchInstruction ifne = new IFNE(null);

				ilRun.append(ifne);    // if stack != false jump
				ilRun.append(InstructionConstants.POP);
				ilRun.append(new PUSH(constPoolGen, false));

				end_and = ilRun.append(InstructionConstants.NOP);

				// kan vi optimera hoppet?
				ifne.setTarget(end_and);
			}
			else
			{
				System.err.println("Illegal type or not yet implemented " +
								   "(AND " + arg + ")");
			}
		}
		else
		{
			System.err.println("AND not yet implemented for" + arg);
		}
	}

	private void emitANDN(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_BOOL)
			{
				if (((TypeBOOL) arg).getValue())
				{    // result ANDN TRUE -> False
					ilRun.append(InstructionConstants.POP);
					ilRun.append(InstructionConstants.ICONST_0);
				}

				// else  result
			}
			else
			{
				System.err.println("Illegal type or not yet " +
								   "implemented (ANDN " + t +")");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) &&
				(t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_BOOL)
			{
				ilRun.append(emitLoadVariable(var));

				InstructionHandle end_andn;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if stack == false jump
				ilRun.append(InstructionConstants.POP);
				ilRun.append(new PUSH(constPoolGen, false));

				end_andn = ilRun.append(InstructionConstants.NOP);

				// kan vi optimera hoppet?
				ifeq.setTarget(end_andn);
			}
			else
			{
				System.err.println("Illegal type or not yet implemented " +
								   "(emitANDN "+ t +")");
			}
		}
		else
		{
			System.err.println("ANDN not yet implemented for "+ arg);
		}
	}

	//XXX Hit har Anders gått igenom formatering + lite kommentarer 2002-04-12

	private void emitOR(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_BOOL)
			{
				if (((TypeBOOL) arg).getValue())
				{    // result OR TRUE -> TRUE
					ilRun.append(InstructionConstants.POP);
					ilRun.append(InstructionConstants.ICONST_1);
				}

				// else  result register value, result OR FALSE -> RESULT
			}
			else
			{
				System.err.println("Illegal type or not yet " + "implemented (emitOR)");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_BOOL)
			{
				ilRun.append(emitLoadVariable(var));

				InstructionHandle end_or;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // stack = false
				ilRun.append(InstructionConstants.POP);
				ilRun.append(new PUSH(constPoolGen, true));

				end_or = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(end_or);
			}
			else
			{
				System.err.println("Illegal type or not yet " + "implemented (emitOR)");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitSUB sub stack value with argument and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitSUB(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(new ISUB());
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.FSUB);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitMUL multiplicates the argument with stack value and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitMUL(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(new IMUL());
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FMUL);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.IMUL);
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.FMUL);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitDIV divides stack value by argument value and put the
		 * result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitDIV(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(new IDIV());
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FDIV);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.IDIV);
			}
			else if (t == TypeConstant.T_REAL)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.FDIV);
			}
			else
			{
				System.err.println("Not yet implemented");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitMOD arg1 mod arg2: returns 0 when arg1 MOD 0, else arg1 - (arg1/arg2)*arg2
	 * @param arg an IL BOOL-variable
	 */
	private void emitMOD(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));

				InstructionHandle mod_0;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if mod 0, then jump
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.IREM);

				InstructionHandle end_mod;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end_mod

				mod_0 = ilRun.append(InstructionConstants.POP);
				ilRun.append(InstructionConstants.ICONST_0);

				end_mod = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(mod_0);
				jmp.setTarget(end_mod);
			}
			else
			{
				System.err.println("Type error using MOD");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));

				InstructionHandle mod_0;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if mod 0, then jump
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.IREM);

				InstructionHandle end_mod;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end_mod

				mod_0 = ilRun.append(InstructionConstants.POP);
				ilRun.append(InstructionConstants.ICONST_0);

				end_mod = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(mod_0);
				jmp.setTarget(end_mod);
			}
			else
			{
				System.err.println("Type error using MOD");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitEQ test two intergers for equality and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitEQ(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_BOOL)
			{
			        ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(if_true);
				jmp.setTarget(end);
			}

			else if (t == TypeConstant.T_DINT)
			{
				// preperation for comparison by subtracting values on the stack
			        ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using EQ");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifeq = new IFEQ(null);

				ilRun.append(ifeq);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifeq.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using EQ");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	private void emitGT(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifgt = new IFGT(null);

				ilRun.append(ifgt);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifgt.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using GT");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifgt = new IFGT(null);

				ilRun.append(ifgt);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifgt.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using GT");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitGE test two intergers  >=  and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitGE(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);    // preperation for comparison by

				// subtracting values on the stack
				InstructionHandle if_true;
				BranchInstruction ifge = new IFGE(null);

				ilRun.append(ifge);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifge.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using GE");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifge = new IFGE(null);

				ilRun.append(ifge);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifge.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using GE");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitLT test two intergers  <  and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitLT(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);    // preperation for comparison by

				// subtracting values on the stack
				InstructionHandle if_true;
				BranchInstruction iflt = new IFLT(null);

				ilRun.append(iflt);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				iflt.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using LT");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction iflt = new IFLT(null);

				ilRun.append(iflt);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				iflt.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using LT");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitLE test two intergers  <= and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitLE(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);    // preperation for comparison by

				// subtracting values on the stack
				InstructionHandle if_true;
				BranchInstruction ifle = new IFLE(null);

				ilRun.append(ifle);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifle.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using LE");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifle = new IFLE(null);

				ilRun.append(ifle);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifle.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using LE");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitNE test two intergers for not equality and put the result on the stack
	 * @param arg an IL BOOL-variable
	 */
	private void emitNE(Object arg)
	{
		if (arg instanceof IECConstant)
		{
			TypeConstant t = ((IECConstant) arg).getType();

			if (t == TypeConstant.T_BOOL)
			{
			        ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifne = new IFNE(null);

				ilRun.append(ifne);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifne.setTarget(if_true);
				jmp.setTarget(end);
			}
			
			else if (t == TypeConstant.T_DINT)
			{
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);    // preperation for comparison by

				// subtracting values on the stack
				InstructionHandle if_true;
				BranchInstruction ifne = new IFNE(null);

				ilRun.append(ifne);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifne.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using NE");
			}
		}
		else if (arg instanceof IECVariable)
		{
			IECVariable var = (IECVariable) arg;
			TypeConstant t = var.getType();

			if ((var instanceof IECSymbolicVariable) && (t == TypeConstant.T_FUNCTION_BLOCK))
			{
				t = ((IECSymbolicVariable) var).getFieldSelectorType();
			}

			if (t == TypeConstant.T_DINT)
			{
				ilRun.append(emitLoadVariable(var));
				ilRun.append(InstructionConstants.ISUB);

				InstructionHandle if_true;
				BranchInstruction ifne = new IFNE(null);

				ilRun.append(ifne);    // if condition is true, jump to if_true
				ilRun.append(new PUSH(constPoolGen, false));

				InstructionHandle end;
				BranchInstruction jmp = new GOTO(null);

				ilRun.append(jmp);    // always jump to end

				if_true = ilRun.append(new PUSH(constPoolGen, true));
				end = ilRun.append(InstructionConstants.NOP);

				ifne.setTarget(if_true);
				jmp.setTarget(end);
			}
			else
			{
				System.err.println("Type error using NE");
			}
		}
		else
		{
			System.err.println("Not yet implemented");
		}
	}

	/**emitIL_EXPRESSION writes an il_expr_operator to the bytecode
	 * @param operator a string defining the operator, e.g. "ADD("
	 * @param t what datatype is the operator to work on
	 * @param arg for future use, i.e. for derived types
	 */
	public void emitIL_EXPRESSION(String operator, TypeConstant t, Object arg)
	{

		// throws IllegalOperatorException{
		IlExprOperator op = IlExprOperator.ADD;

		try
		{
			op = IlExprOperator.getOperator(operator);
		}
		catch (IllegalOperatorException e)
		{
			System.err.println("Illegal operator: " + operator);
		}

		if (op == IlExprOperator.AND)
		{    // Logical AND
			emitExprAND(t, arg);
		}
		else if (op == IlExprOperator.OR)
		{    // Logical OR
			emitExprOR(t, arg);
		}

		// else if (op == IlExprOperator.XOR) { //Logical Exclusive OR
		else if (op == IlExprOperator.ANDN)
		{    // Logical e1 AND NOT e2
			emitExprANDN(t, arg);
		}

		// else if (op == IlExprOperator.ORN) { //Logical e1 OR NOT e2
		// else if (op == IlExprOperator.XORN) { //Logical e1 XOR NOT e2
		// if (op == IlExprOperator.ADD) { //Addition
		// emitExprADD(t,arg); //se nedan
		// }
		// else if (op == IlExprOperator.SUB){         //Subtraction
		// emitExprSUB(t,arg);
		// }
		// else if (op == IlExprOperator.MUL) {        //Multiplication
		// emitExprMUL(t,arg);
		// }
		// else if (op == IlExprOperator.DIV) {  //Division
		// if (t == TypeConstant.DINT){
		// ilRun.append(InstructionConstants.IDIV);}
		// }
		// Modulus
		// else if (op == IlExprOperator.MOD) {
		// if (t == TypeConstant.DINT){           //il's MOD != java's REM
		// ilRun.append(InstructionConstants.IREM);} //Kolla upp matematiken
		// }
		// else if (op == IlExprOperator.GT) {
		// else if (op == IlExprOperator.GE) {
		// else if (op == IlExprOperator.EQ) {
		// else if (op == IlExprOperator.LT) {
		// else if (op == IlExprOperator.LE) {
		// else if (op == IlExprOperator.NE) {
		else
		{
			System.err.println("Not implemented: " + operator + ", operand of type: " + t);
		}
	}

	// private void emitExprAdd(TypeConstant t, Object arg){
	private void emitExprAND(TypeConstant t, Object arg)
	{
		if (t == TypeConstant.T_BOOL)
		{

			// BranchInstruction theTarget = new Branch
			InstructionHandle end_and;
			BranchInstruction ifne = new IFNE(null);

			ilRun.append(ifne);    // stack != false
			ilRun.append(InstructionConstants.POP);
			ilRun.append(new PUSH(constPoolGen, false));

			end_and = ilRun.append(InstructionConstants.NOP);

			// kan vi optimera hoppet?
			ifne.setTarget(end_and);
		}
		else
		{
			System.err.println("AND not implemented for type: " + t);
		}
	}

	private void emitExprANDN(TypeConstant t, Object arg)
	{
		if (t == TypeConstant.T_BOOL)
		{

			// BranchInstruction theTarget = new Branch
			InstructionHandle end_andn;
			BranchInstruction ifeq = new IFEQ(null);

			ilRun.append(ifeq);    // stack != false
			ilRun.append(InstructionConstants.POP);
			ilRun.append(new PUSH(constPoolGen, false));

			end_andn = ilRun.append(InstructionConstants.NOP);

			// kan vi optimera hoppet?
			ifeq.setTarget(end_andn);
		}
		else
		{
			System.err.println("ANDN not implemented for type: " + t);
		}
	}

	private void emitExprOR(TypeConstant t, Object arg)
	{
		if (t == TypeConstant.T_BOOL)
		{
			InstructionHandle end_or;
			BranchInstruction ifeq = new IFEQ(null);

			ilRun.append(ifeq);    // stack = false
			ilRun.append(InstructionConstants.POP);
			ilRun.append(new PUSH(constPoolGen, true));

			end_or = ilRun.append(InstructionConstants.NOP);

			ifeq.setTarget(end_or);
		}
		else
		{
			System.err.println("OR not implemented for type: " + t);
		}
	}

	// private void emitExprXOR(TypeConstant t, Object arg){

	/*
	 *    private void emitExprANDN(TypeConstant t, Object arg){
	 * if (t == TypeConstant.T_BOOL) {
	 *               InstructionHandle end_andn;
	 *               BranchInstruction ifeq = new IFEQ(null);
	 *               ilRun.append(ifeq);  // stack = false
	 *               ilRun.append(InstructionConstants.POP);
	 *               ilRun.append(new PUSH(constPoolGen,false));
	 *               end_andn = ilRun.append(InstructionConstants.NOP);
	 *       }
	 * else {System.err.println("ANDN not implemented for type: " + t);}
	 * }
	 */
	private void emitExprORN(TypeConstant t, Object arg)
	{
		if (t == TypeConstant.T_BOOL)
		{
			InstructionHandle end_orn;
			BranchInstruction ifne = new IFNE(null);

			ilRun.append(ifne);    // stack = false
			ilRun.append(InstructionConstants.POP);
			ilRun.append(new PUSH(constPoolGen, true));

			end_orn = ilRun.append(InstructionConstants.NOP);
		}
		else
		{
			System.err.println("ORN not implemented for type: " + t);
		}
	}

	// private void emitExprXORN(TypeConstant t, Object arg){
	// private void emitExprADD(TypeConstant t, Object arg){
	// private void emitExprSUB(TypeConstant t, Object arg){
	// private void emitExprMUL(TypeConstant t, Object arg){
	// private void emitExprDIV(TypeConstant t, Object arg){
	// private void emitExprMOD(TypeConstant t, Object arg){
	// private void emitExprGT(TypeConstant t, Object arg){
	// private void emitExprGE(TypeConstant t, Object arg){
	// private void emitExprEQ(TypeConstant t, Object arg){
	// private void emitExprLT(TypeConstant t, Object arg){
	// private void emitExprLE(TypeConstant t, Object arg){
	// private void emitExprNE(TypeConstant t, Object arg){

	/**emitLabel stores the position of a label so that jumps can
	 * be pointed to it.
	 * @param label the name of the label
	 */
	public void emitLABEL(String label)
	{
		InstructionHandle ih = ilRun.append(InstructionConstants.NOP);

		jumpController.addTarget(label, ih);
	}

	/**emitLabel takes care of IL's jump commands (JMP, JMPC, JMPCN).
	 * @param op the jump operator
	 * @param targetLabel the label (position) to which the jump should point
	 */
	public void emitIL_JUMP_OPERATION(IlJumpOperator op, String targetLabel)
	{
		if (op == IlJumpOperator.JMP)
		{
			BranchHandle jmp = ilRun.append(new GOTO_W(null));

			jumpController.addJump(targetLabel, jmp);
		}
		else if (op == IlJumpOperator.JMPC)
		{
			BranchInstruction ifeq = new IFEQ(null);

			ilRun.append(InstructionConstants.DUP);
			ilRun.append(ifeq);

			BranchHandle jmp = ilRun.append(new GOTO_W(null));
			InstructionHandle end_jmpc = ilRun.append(InstructionConstants.NOP);

			ifeq.setTarget(end_jmpc);
			jumpController.addJump(targetLabel, jmp);
		}
		else if (op == IlJumpOperator.JMPCN)
		{
			BranchInstruction ifne = new IFNE(null);

			ilRun.append(InstructionConstants.DUP);
			ilRun.append(ifne);

			BranchHandle jmp = ilRun.append(new GOTO_W(null));
			InstructionHandle end_jmpc = ilRun.append(InstructionConstants.NOP);

			ifne.setTarget(end_jmpc);
			jumpController.addJump(targetLabel, jmp);
		}
		else
		{
			System.err.println("Error in jump operation");
		}
	}

	/* Function block call handling */
	public BranchInstruction emitIL_FB_CALL_Start(IlCallOperator op)
	{
		/* check conditions */
		BranchInstruction callCondition = null;

		if (op == IlCallOperator.CAL)
		{

			// do nothing
		}
		else if (op == IlCallOperator.CALC)
		{

			// if result reg == true -> make call
			ilRun.append(InstructionConstants.DUP);

			callCondition = new IFEQ(null);

			ilRun.append(callCondition);
		}
		else if (op == IlCallOperator.CALCN)
		{

			// if result reg == false -> make call
			ilRun.append(InstructionConstants.DUP);

			callCondition = new IFNE(null);

			ilRun.append(callCondition);
		}

		return callCondition;
	}

	/** set conditional target */
	public void emitIL_FB_CALL_End(BranchInstruction callCondition)
	{
		if (callCondition != null    /*
									  * call operator != CAL,
									  *                             see emitIL_FB_CALL_start
									  */
		)
		{
			InstructionHandle end_call = ilRun.append(InstructionConstants.NOP);

			callCondition.setTarget(end_call);
		}
	}

	public void emitIL_FB_CALL_Run(String fbName, String fbTypeName)
	{
		ObjectType fbType = new ObjectType(fbTypeName);

		ilRun.append(InstructionConstants.THIS);
		ilRun.append(fac.createFieldAccess(className, fbName, fbType, Constants.GETFIELD));
		ilRun.append(fac.createInvoke(fbTypeName, "run", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	}

	public void emitIL_FB_CALL_SetInputs() {}

	public void emitIL_FB_CALL_SetOutputs() {}

	/* hack för att få print att fungera på ett enkelt sätt */
	public void emitIL_FB_CALL(IlCallOperator op, String fb_name, Object[] args)
	{
		fb_name = fb_name.toLowerCase();

		// vi kan inte ha lowercase senare eftersom vi ska anropa java-metoder
		// --kan vi visst eftersom vi använder CALL_JAVA då istället för CAL
		/* check conditions */
		BranchInstruction callCondition = new IFEQ(null);    // dummy init

		if (op == IlCallOperator.CAL) {}
		else if (op == IlCallOperator.CALC)
		{

			// if result reg == true -> make call
			ilRun.append(InstructionConstants.DUP);

			// callCondition = new IFEQ(null);
			ilRun.append(callCondition);
		}
		else if (op == IlCallOperator.CALCN)
		{

			// if result reg == false -> make call
			ilRun.append(InstructionConstants.DUP);

			callCondition = new IFNE(null);

			ilRun.append(callCondition);
		}

		/* make call */
		if (fb_name.equals("print") || fb_name.equals("println"))
		{
			emitPRINT(args, fb_name);
		}
		else
		{
			System.err.println("Calls to function blocks not yet " + "implemented");
		}

		/* set conditional target */
		if ((op == IlCallOperator.CALC) || (op == IlCallOperator.CALCN))
		{
			InstructionHandle end_call = ilRun.append(InstructionConstants.NOP);

			callCondition.setTarget(end_call);
		}
	}

	private void emitPRINT(Object[] args, String print_type)
	{
		ObjectType pStream = new ObjectType("java.io.PrintStream");

		for (int i = 0; i < args.length; i++)
		{
			if (args[i] instanceof TypeWSTRING)
			{
				ilRun.append(fac.createFieldAccess("java.lang.System", "out", pStream, Constants.GETSTATIC));
				ilRun.append(new PUSH(constPoolGen, ((TypeWSTRING) args[i]).getValue()));
				ilRun.append(fac.createInvoke("java.io.PrintStream", print_type, Type.VOID, new Type[]{ Type.STRING }, Constants.INVOKEVIRTUAL));
			}
			else if (args[i] instanceof IECVariable)
			{
				IECVariable var = (IECVariable) args[i];

				ilRun.append(fac.createFieldAccess("java.lang.System", "out", pStream, Constants.GETSTATIC));
				ilRun.append(emitLoadVariable(var));

				if (var.getType() == TypeConstant.T_BOOL)
				{
					ilRun.append(fac.createInvoke("java.io.PrintStream", print_type, Type.VOID, new Type[]{ Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
				}
				else if (var.getType() == TypeConstant.T_DINT)
				{
					ilRun.append(fac.createInvoke("java.io.PrintStream", print_type, Type.VOID, new Type[]{ Type.INT }, Constants.INVOKEVIRTUAL));
				}
				else if (var.getType() == TypeConstant.T_REAL)
				{
					ilRun.append(fac.createInvoke("java.io.PrintStream", print_type, Type.VOID, new Type[]{ Type.FLOAT }, Constants.INVOKEVIRTUAL));
				}
				else
				{
					errorsPresent = true;

					System.err.println("Cannot print things of type: " + var.getType());
				}
			}
			else
			{
				System.err.println("Print not implemented for this type");
			}
		}
	}
}
