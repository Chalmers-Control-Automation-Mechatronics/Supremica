package org.supremica.softplc.CompILer.CodeGen;

import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import org.supremica.log.Logger;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;

/**The class ProgramAndFBBuilder handles all common parts of code generation
 * for its sub classes.
 */
public abstract class ProgramAndFBBuilder
    implements Builder
{
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
    
        /**
     * Logger prints nice error, info and debug messages in the Supremica console
     */
    Logger logger;
    boolean debug; //only used when not started withing Supremica
    /* if errorsPresent is set to true no bytecode will be dumped */
    boolean errorsPresent = false;


    //XXX
    void debug (Object message) {
	if (logger != null)
	    logger.debug(message);
	else if (debug) {
	    System.out.println("Debug: " + message);
	}
    }
    //XXX
    void error (Object message) {
	if (logger != null)
	    logger.error(message);
	else
	    System.err.println("Error: " + message);
	errorsPresent = true;
    }
    //XXX
    void info (Object message) {
	if (logger != null)
	    logger.info(message);
	else
	    System.out.println(message);
    }
    //XXX fråga knut hur denna fungerar
    void warn (Object message) {
	if (logger != null)
	    logger.warn(message);
	else
	    System.out.println("Warning: " + message);
    }
    
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
			error(e);
		    }
	    }
	else
	    {
		error(className + " contains errors!");
		error("No code was written for program organization unit " + className + ".");
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
		error("Variables of this kind " + type.toString() +
				   "is not implemented.");
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
		error("Variables of this type not yet implemented: " + type);
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
		error("Initialised variables of type " + type.getType() + " not implemented." + 
		      "Try manual initialisation.");
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
			else if (type == TypeConstant.T_DERIVED)
			    { //XXX den här måste kontrolleras
				il.append(fac.createFieldAccess
					  (/*fb type name*/var.getTypeName(),
					   var.getFieldSelector(), new ObjectType(var.getFieldSelectorTypeName())
					   , Constants.GETFIELD));
				warn("Not tested. loading derived types from derived types"); //XXX stämmer det
			    }
			else
			    {
				error("IO variables in function blocks can not be of type "+type);
			    }
		    }
		else
		    {    /* the derived variable itself should be loaded*/
			il.append(InstructionConstants.THIS);
			il.append(fac.createFieldAccess(className, varName,
							new ObjectType(var.getTypeName()),
							Constants.GETFIELD));

			// XXX should implement this
			warn("Not tested properly sofar. Only loading of elements in " +
			      "derived variables are implemented " +
			      "so far. Not load of derived itself"); //XXX
		    }
	    }
	else
	    {
		error("Loading symbolic variables of type " + type + " not yet implemented");
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
			else if (type == TypeConstant.T_DERIVED) //XXX funkar inte detta utan att ha typnamnet för derived var så
			                                         //XXX struntar vi i det.
			    {
				il.append(InstructionConstants.SWAP);
				il.append(fac.createFieldAccess
					  (/*fb type name*/var.getTypeName(),
					   var.getFieldSelector(), new ObjectType(var.getFieldSelectorTypeName()),
					   Constants.PUTFIELD));
			    }
			else
			    {
				error("IO variables in function blocks can "+
				      "not have type " + type);
			    }
		    }
		else
		    {    /* the derived variable itself */
			// XXX should implement this
			il.append(InstructionConstants.THIS);
			il.append(InstructionConstants.SWAP);
			il.append(fac.createFieldAccess(className, varName,
							new ObjectType(var.getTypeName()),
							Constants.PUTFIELD));
			warn("Not properly tested. Only storing of elements in " +
			      "derived variables are implemented " +
			      "so far. Not store of derived itself."); //XXX
		    }
	    }
	else
	    {
		error("Storing variables of type " + type + " not yet implemented");
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
		error("Illegal operator: " + operator);
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
	else if (op == IlSimpleOperator.STN )
	    {
		emitSTN(arg);
	    }
	//XXX	else if (op == IlSimpleOperator.NOT )
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
	//XXX else if (op == IlSimpleOperator.XOR )
	else if (op == IlSimpleOperator.ANDN)
	    {
		emitANDN(arg);
	    }
	//XXX else if (op == IlSimpleOperator.ORN )
	//XXX else if (op == IlSimpleOperator.XORN)
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
		error("param size (emitStackSpace) must be 1 or 2." + 
		      " Probably you have used unimplemented IL constructs.");
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
			error("Loading of " + arg.toString() + " not yet " + "implemented.");
		    }
	    }
	else if (arg instanceof IECVariable)
	    {
		il.append(emitLoadVariable((IECVariable) arg));
	    }
	else
	    {
		error("Loading of " + arg + " not yet implemented.");
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
		emitNOT();
	    }
	else
	    {
		System.err.println("LDN can only be used on the BOOL datatype." +
				   "Not on " + arg);
		errorsPresent = true;
	    }
    }

    /**emitST stores the negated top of stack value (IL's result register) in a
     * specified variable
     * @param arg where the value is to be stored (must be of type BOOL)
     */
    private void emitSTN(Object arg)
    {
	if (arg instanceof IECConstant)
	    {
		System.err.println("Fatal error: Operator STN cannot " +
				   "store in constant");
	    }
	else if (arg instanceof IECVariable)
	    {
		IECVariable var = (IECVariable) arg;
		/*
		 * first we must duplicate stack value to be able to keep
		 * IL's result register value
		 */
		if (var.getType() == TypeConstant.T_BOOL ||
		    (var instanceof IECSymbolicVariable && 
		     var.getType() == TypeConstant.T_DERIVED &&
		     ((IECSymbolicVariable)var).getFieldSelectorType() 
		     == TypeConstant.T_BOOL))
		    {
			ilRun.append(InstructionConstants.DUP);
			emitNOT();
			ilRun.append(emitStoreVariable(var));
		    }
		else
		    {
			error("STN cannot store i variable of type " + 
			      var.getType() +". Only in BOOL variables.");
		    }
	    }
	else
	    {
		error("Can't store in anything else than a variable");
	    }
    }
    /**emitST stores the top of stack value (IL's result register) in a
     * specified variable
     * @param arg where the value is to be stored
     */
    private void emitST(Object arg)
    {
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
			error("Store for " + var.getType() +
			      " not yet implemented");
		    }
	    }
	else
	    {
		error("Can't store in anything else than a variable");
	    }
    }

    /**emitNOT negates the boolean value on the stack
     */
    private void emitNOT() {
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
    /**emitS set the argument (a variable) to true if the TOS value is true
     * @param arg an IL BOOL-variable
     */
    private void emitS(Object arg)
    {
	if (arg instanceof IECConstant)
	    {
		error("Operator S cannot store in constant");
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
			error("Operator S can only be used on BOOL");
		    }
	    }
	else
	    {
		error("S not implemented for arguments like" + arg);
	    }
    }

    /**emitR set the argument (a variable) to false if the TOS value is true
     * @param arg an IL BOOL-variable
     */
    private void emitR(Object arg)
    {
	if (arg instanceof IECConstant)
	    {
		error("Operator R cannot store in constant");
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
			error("Operator S can only be used on BOOL");
		    }
	    }
	else
	    {
		System.err.println("R not yet implemented for arguments like " + arg);
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
			error("ADD not yet implemented for " + t);
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
			error("ADD not yet implemented for "+ t);
		    }
	    }
	else
	    {
		error("ADD not yet implemented for "+ arg);
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
			error("Illegal type or not yet implemented (AND " + arg + ")");
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
			error("Illegal type or not yet implemented (AND " + arg + ")");
		    }
	    }
	else
	    {
		error("AND not yet implemented for" + arg);
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
			error("Illegal type or not yet implemented (ANDN " + t +")");
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
			error("Illegal type or not yet implemented (emitANDN "+ t +")");
		    }
	    }
	else
	    {
		error("ANDN not yet implemented for "+ arg);
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
			error("Illegal type or not yet " + "implemented (emitOR)");
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
			error("Illegal type or not yet " + "implemented (emitOR)");
		    }
	    }
	else
	    {
		error("Not yet implemented");
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
			error("Not yet implemented");
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
			error("Not yet implemented");
		    }
	    }
	else
	    {
		error("Not yet implemented");
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
			System.err.println("MUL not yet implemented for type" + t);
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
			System.err.println("MUL not yet implemented for type " + t);
		    }
	    }
	else
	    {
		error("MUL not yet implemented for " + arg);
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
			error("DIV not yet implemented for constants of type " + t);
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
			error("DIV not yet implemented for variables of type " + t);
		    }
	    }
	else
	    {
		error("DIV not yet implemented for " + arg);
	    }
    }

    /**emitMOD DINT: arg1 mod arg2: returns 0 when arg1 MOD 0, else arg1 - (arg1/arg2)*arg2
     * REAL: where neither operand is an infinity, a zero, or NaN, the floating-point remainder
     * result from a dividend value1' and a divisor value2' is defined by the mathematical relation
     * result = value1' - (value2' * q), where q is an integer that is negative only if value1' / value2'
     * is negative and positive only if value1' / value2' is positive, and whose magnitude is as large
     * as possible without exceeding the magnitude of the true mathematical quotient of
     * value1' and value2'.
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
		else if (t == TypeConstant.T_REAL)
		    {
			ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
			ilRun.append(InstructionConstants.FREM);
		    }
		else
		    {
			error("Type error using MOD");
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
		else if (t == TypeConstant.T_REAL)
		    {
			ilRun.append(emitLoadVariable(var));
			ilRun.append(InstructionConstants.FREM);
		    }
		else
		    {
			error("Type error using MOD");
		    }
	    }
	else
	    {
		error("MOD not implemented for " + arg);
	    }
    }

    //XXX Testa EQ för alla olika typer

    /**emitEQ test for equality and put the result on the stack
     * @param arg an IL BOOL-variable
     */
    private void emitEQ(Object arg)
    {
	if (arg instanceof IECConstant)
	    {
		TypeConstant t = ((IECConstant) arg).getType();

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }			    
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
			error("Type error using EQ");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }			    

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
		        error("Type error using EQ");
		    }
	    }
	else
	    {
		error("EQ not implemented for " + arg);
	    }
    }

    private void emitGT(Object arg)
    {
	if (arg instanceof IECConstant)
	    {
		TypeConstant t = ((IECConstant) arg).getType();

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }
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
			error("Type error using GT");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }
		    
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
			error("Type error using GT");
		    }
	    }
	else
	    {
		error("GT not yet implemented for " + arg);
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }
			
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
			error("Type error using GE");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }
		    
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
			error("Type error using GE");
		    }
	    }
	else
	    {
		error("GE not implemented for " + arg);
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }

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
			error("Type error using LT");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }
			
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
			error("Type error using LT");
		    }
	    }
	else
	    {
		error("LT not implemented for " + arg);
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }

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
			error("Type error using LE");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }

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
			error("Type error using LE");
		    }
	    }
	else
	    {
		error("LE not implemented for " + arg);
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT ||
		    t == TypeConstant.T_REAL)
		    {
			if (t == TypeConstant.T_BOOL)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeBOOL) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else if (t == TypeConstant.T_DINT)
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeDINT) arg).getValue()));
				ilRun.append(InstructionConstants.ISUB);
			    }
			else
			    {
				ilRun.append(new PUSH(constPoolGen, ((TypeREAL) arg).getValue()));
				ilRun.append(InstructionConstants.FSUB);
				ilRun.append(InstructionConstants.F2I);
			    }

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
			error("Type error using NE");
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

		if (t == TypeConstant.T_BOOL ||  t == TypeConstant.T_DINT
		                             ||  t == TypeConstant.T_REAL) {
		    ilRun.append(emitLoadVariable(var));
		    if (t == TypeConstant.T_BOOL || t == TypeConstant.T_DINT) {
			ilRun.append(InstructionConstants.ISUB);
		    }
		    else {
			ilRun.append(InstructionConstants.FSUB);
			ilRun.append(InstructionConstants.F2I);
		    }
			
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
			error("Type error using NE");
		    }
	    }
	else
	    {
		error("NE not implemented not implemented for " + arg);
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
		error("Illegal operator: " + operator);
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
		error("Not implemented: " + operator + ", operand of type: " + t);
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
		error("AND( not implemented for type: " + t);
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
		error("ANDN( not implemented for type: " + t);
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
		error("OR( not implemented for type: " + t);
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
     * else {error("ANDN not implemented for type: " + t);}
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
		error("ORN( not implemented for type: " + t);
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
		error("Error in jump operation");
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
		//XXX jovisst,,,,error("Calls to function blocks not yet " + "implemented");
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
			else if (var.getType() == TypeConstant.T_WSTRING)
			    {
				ilRun.append(fac.createInvoke("java.io.PrintStream", print_type, Type.VOID, new Type[]{ Type.STRING }, Constants.INVOKEVIRTUAL));
			    }
			else
			    {
				errorsPresent = true;

				error("Cannot print things of type: " + var.getType());
			    }
		    }
		else
		    {
			error("Print not implemented for this type");
		    }
	    }
    }
}
