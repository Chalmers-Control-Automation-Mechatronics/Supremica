package org.supremica.softplc.CompILer.CodeGen;

import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.log.Logger;
import de.fub.bytecode.generic.*;
import java.util.*;
import java.io.File;

public class JavaBytecodeGenerator
    implements CodeGenerator, SimpleNodeVisitor
{

    /*
     * Saker som inte stämmer med IEC-standarden:
     * --"strong data typing" har vi inte (bok sid 16)
     *
     * --il_param_assignment kan endast ta il_operand och
     *  inte il_simple_instr_list
     *
     *
     * Datatyper för variabler: (tillåtna)
     * -BOOL
     * -DINT
     * -WSTRING?
     * -REAL
     */

    /*
     * Abbreviation explanations
     *
     * POU - Program Organization Unit (eg. program, function block etc.)
     */

    /**
     * builder is the current Builder class used to encode a POU into bytecode
     */
    Builder builder;

    /*
     * This list is temporarily used for distinguishing declared data types
     * and function_block types
     * This should probably be done in a Checker but since that is not
     * finished..... //XXX
     */
    List declaredDataTypes = new LinkedList();

    /**
     * Logger prints nice error, info and debug messages in the Supremica console
     */
    private Logger logger;
    private boolean debug; //only used when not started within Supremica
    
    /*
     * errorsPresent is used to make sure no code is written before
     * all detected errors are corrected
     */
    private boolean errorsPresent = false;

    private File temp;
    private String outDir;

    //XXX
    public JavaBytecodeGenerator(SimpleNode abstractSyntaxTreeRoot, String outputDir,
				 Logger logger, boolean debug)
    {
	this.logger = logger;
	this.debug = debug;
	outDir = outputDir;
	Node[] children = abstractSyntaxTreeRoot.getChildren();
	visitChildren(0, children);
    }

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
    
    /**visitChildren is used to visit all nodes in an array (
     * ie. the children array of a node) when you are not
     * interested in the returned values, just the visiting itself
     * @param startAtChild every node from this position will be visited
     *                     but not the ones before (0 to visit all nodes)
     * @param children the array with nodes to visit
     */
    void visitChildren(int startAtChild, Node[] children)
    {
	if (children != null)
	    {
		for (int i = startAtChild; i < children.length; i++)
		    {
			SimpleNode c = (SimpleNode) children[i];

			if (c != null)
			    {
				c.visit(this, null);
			    }
		    }
	    }
    }

    //XXX skall troligen tas bort när vi är färdiga
    //XXX eller ge "hårdare" felmeddelande dvs. kör inte vidare efter detta
    public Object visitStandard(SimpleNode n, Object o)
    {
	debug(n.toString() + "  WARNING: visitStandard");

	return null;
    }

    public Object visitSIMPLE_INSTR_LIST(ASTsimple_instr_list n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	visitChildren(0, children);
	return null;
    }

    public Object visitIL_SIMPLE_INSTRUCTION(ASTil_simple_instruction n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	SimpleNode c = (SimpleNode) children[0];

	if (c != null)
	    {
		c.visit(this, null);
	    }

	return null;
    }

    public Object visitIL_SIMPLE_OPERATION(ASTil_simple_operation n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	Object arg = ((SimpleNode) children[0]).visit(this, null);
	String operator = n.getName();

	if (arg != null)
	    {
		builder.emitIL_SIMPLE_OPERATION(operator, arg);
	    }

	return null;
    }

    public Object visitIL_EXPRESSION(ASTil_expression n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	String operator = n.getName();

	if (children.length == 0)
	    {
		error("IL_EXPRESSION missing operand(s)");
		return null;
		// what should happen when a il_expression doesn't have
		// any operands at all, typeChecker?? can it really happen //XXX
	    }
	else if (children.length == 1)
	    {
		try
		    {
			ASTsimple_instr_list simInstrList = (ASTsimple_instr_list)children[0];
			/* open new IL scope, size should be set by typeChecker */
			builder.emitStackSpace(1);
			simInstrList.visit(this, null);
		    }
		catch (Exception e)
		    {
			Object arg = ((SimpleNode) children[0]).visit(this, null);

			builder.emitLoad(arg);    // opens new IL scope and loads arg
		    }
	    }
	else
	    {
		Object arg = ((SimpleNode) children[0]).visit(this, null);

		builder.emitLoad(arg);    // opens new IL scope and loads arg
		((SimpleNode) children[1]).visit(this, null);
	    }

	builder.emitIL_EXPRESSION(operator, TypeConstant.T_BOOL, null);

	//XXX typen för argumentet till expr_operator måste läggas in i
	//XXX ASTn av typcheckaren ist. för att sättas till T_BOOL
	return null;
    }

    public Object visitIL_JUMP_OPERATION(ASTil_jump_operation n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	IlJumpOperator op = (IlJumpOperator) ((SimpleNode) children[0]).visit(this, null);
	String label = (String) ((SimpleNode) children[1]).visit(this, new String());

	builder.emitIL_JUMP_OPERATION(op, label);

	return null;
    }

    public Object visitIL_JUMP_OPERATOR(ASTil_jump_operator n, Object o)
    {
	debug(n.toString());

	IlJumpOperator op = IlJumpOperator.JMP;    // dummy init

	try
	    {
		op = IlJumpOperator.getOperator(n.getName());
	    }
	catch (IllegalOperatorException e)
	    {
		error("Illegal jump operator: " + n.getName());
		return null;
	    }

	return op;
    }

    public Object visitNUMERIC_LITERAL(ASTnumeric_literal n, Object o)
    {
	debug(n.toString());

	TypeANY_NUM p = HelpMethods.parseANY_NUM(n.getName());

	if (p == null)
	    {
		error("This constant type is not implemented: " + n.getName() + 
		      ". Try typed constant " + "(e.g. DINT#5)");
	    }
	return p;
    }

    public Object visitBOOLEAN_LITERAL(ASTboolean_literal n, Object o)
    {
	debug(n.toString());
	return HelpMethods.parseBOOL(n.getName());
    }

    public Object visitVARIABLE(ASTvariable n, Object o)
    {
	debug(n.toString());

	if (n.isDirectVariable())
	    {
		return new IECDirectVariable(n.getName());    // , TypeConstant.T_BOOL);
	    }
	else if (n.getType() == TypeConstant.T_DERIVED)
	    {
		if (n.isFunctionBlock())
		    {
			return new IECSymbolicVariable(n.getName(), n.getType(), n.getTypeName(), 
						       n.getFieldSelector(), n.getFieldSelectorType());
		    }
		else
		    {
			error("Function blocks are the only derived " + "datatypes allowed");
			return null;
		    }
	    }
	else
	    {
		return new IECSymbolicVariable(n.getName(), n.getType());
	    }
    }

    public Object visitVARIABLE_NAME(ASTvariable_name n, Object o)
    {
	debug(n.toString());
	return n.getName();
    }

    public Object visitFIELD_SELECTOR(ASTfield_selector n, Object o)
    {
	debug(n.toString());
	//error("Found fieldselector"); 
	/*should never happen in CodeGenerator only in
	checker ??*/

	return null;
    }

    public Object visitELEMENTARY_TYPE_NAME(ASTelementary_type_name n, Object o)
    {
	debug(n.toString());

	return HelpMethods.parseTypeConstants(n.getName());
    }

    public Object visitSIMPLE_TYPE_NAME(ASTsimple_type_name n, Object o)
    {
	debug(n.toString());

	if (!declaredDataTypes.contains(n.getName()))
	    {
		/* we have found a function block initialisation */
		return new TypeFUNCTION_BLOCK(n.getName());
	    }

	System.err.println("Found simple_type_name," + n.getName() + " that was " 
			   + "not a function block type. Not implemented.");

	return null;    
	/*
	 * if declaredDataType. Should be changed when
	 *             derived data types are implemented
	 */
    }

    public Object visitPROGRAM_TYPE_NAME(ASTprogram_type_name n, Object o)
    {
	debug(n.toString());
	return n.getName();
    }

    public Object visitDERIVED_FUNCTION_BLOCK_NAME(ASTderived_function_block_name n, Object o)
    {
	debug(n.toString());

	return n.getName();
    }

    public Object visitFB_NAME(ASTfb_name n, Object o)
    {
	debug(n.toString());

	return n.getName();
    }

    public Object visitFUNCTION_BLOCK_BODY(ASTfunction_block_body n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	builder.emitStackSpace(1);    // open new scope
	visitChildren(0, children);

	return null;
    }

    public Object visitPROGRAM_DECLARATION(ASTprogram_declaration n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	String programname = (String) ((SimpleNode) children[0]).visit(this, null);

	builder = new ProgramBuilder(programname, outDir, logger, debug);

	visitChildren(1, children);

	if (!errorsPresent) {
	    builder.dumpCode();
	} else {
	    error("Errors present! Could not generate code!");
	}
	return null;
    }

    public Object visitFUNCTION_BLOCK_DECLARATION(ASTfunction_block_declaration n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	String fbName = (String) ((SimpleNode) children[0]).visit(this, null);

	builder = new FunctionBlockBuilder(fbName, outDir, logger, debug);

	visitChildren(1, children);

	if (!errorsPresent) {
	    builder.dumpCode();
	} else {
	    error("Errors present! Could not generate code!");
	}
	return null;
    }

    public Object visitJAVA_BLOCK_DECLARATION(ASTjava_block_declaration n, Object o)
    {
	debug(n.toString());

	return null;
    }

    /* This part takes care of some variable declarations */
    public Object visitOTHER_VAR_DECLARATIONS(ASTother_var_declarations n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	visitChildren(0, children);

	return null;
    }

    public Object visitVAR_DECLARATIONS(ASTvar_declarations n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	visitChildren(0, children);

	return null;
    }

    public Object visitVAR_INIT_DECL(ASTvar_init_decl n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	SimpleNode c = (SimpleNode) children[0];

	if (c != null)
	    {
		c.visit(this, null);
	    }

	return null;
    }

    /* Located variables */
    public Object visitLOCATED_VAR_DECLARATIONS(ASTlocated_var_declarations n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	visitChildren(0, children);

	return null;
    }

    public Object visitLOCATED_VAR_DECL(ASTlocated_var_decl n, Object o)
    {
	debug(n.toString());

	int base = 0;
	Node[] children = n.getChildren();

	if (children.length == 3)
	    {    // always 2 or 3, and in case of 3
		base = 1;    // we don't care about varname anyway
	    }

	IECDirectVariable location = (IECDirectVariable) ((SimpleNode) children[base]).
	    visit(this, null);
	Object type = ((SimpleNode) children[base + 1]).visit(this, null);

	if (type instanceof TypeConstant)
	    {

		// this is a type check, should be in a checker instead
		// but left here as a precaution
		if (!((TypeConstant) type == TypeConstant.T_BOOL))
		    {
			error("Direct variables only implemented " + 
			      "for BOOL. Not for " + (TypeConstant) type);
		    }
	    }
	else if (type instanceof TypeANY_ELEMENTARY)
	    {
		if (((TypeANY_ELEMENTARY) type).getType() == TypeConstant.T_BOOL)
		    {
			TypeBOOL value = (TypeBOOL) type;
			builder.emitDirectInit(location, value);
		    }
		else
		    {
			error("Direct variables only implemented " + "for BOOL. Not for " + 
			      ((TypeANY_ELEMENTARY) type).getType());
		    }
	    }
	else
	    {    // error
		error("Direct variables only implemented for BOOL. Either the variable should " + 
		      "just be declared BOOL or initialised with a boolean value");
	    }

	return null;
    }

    public Object visitLOCATED_VAR_SPEC_INIT(ASTlocated_var_spec_init n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();    /* should always be of length 1*/

	if (children[0] instanceof ASTsimple_spec_init)
	    {
		return ((SimpleNode) children[0]).visit(this, null);
	    }
	else
	    {
		error("Initialisation of located variables only implemented for simple " + 
		      "BOOL (e.g. var AT %QX12 : BOOL := TRUE;)");

		return null;
	    }
    }

    public Object visitLOCATION(ASTlocation n, Object o)
    {
	debug(n.toString());

	return new IECDirectVariable(n.getName());
    }

    /* Input and output variables */
    public Object visitIO_VAR_DECLARATIONS(ASTio_var_declarations n, Object o)
    {
	debug(n.toString());

	SimpleNode child = (SimpleNode) (n.getChildren())[0];    // n always has exactly one child

	if (child instanceof ASTinput_output_declarations)
	    {
		child.visit(this, null);
	    }
	else
	    {    /*child instanceof ASTinput_declaration*/

		/* child instanceof ASToutput_declarations*/
		error("Only var_in_out is implemented, not var_in " + "or var_out");
	    }

	return null;
    }

    public Object visitINPUT_OUTPUT_DECLARATIONS(ASTinput_output_declarations n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	visitChildren(0, children);

	return null;
    }

    public Object visitVAR_DECLARATION(ASTvar_declaration n, Object o)
    {
	debug(n.toString());

	Node child = (n.getChildren())[0];    /* always one child*/

	if (child instanceof ASTfb_name_decl)
	    {
		/*error("ASTfb_name_decl found!!!! This is a wonder " + "please keep your program and send it to the " + "constructor");*/ //XXX
	    }

	((SimpleNode) child).visit(this, null);

	return null;
    }

    /* XXX vad skall vi göra här */
    public Object visitTEMP_VAR_DECL(ASTtemp_var_decl n, Object o)
    {
	debug(n.toString());
	((SimpleNode) n.getChildren()[0]).visit(this, null);

	return null;
    }

    /* General specification and variable declaration */
    public Object visitVAR1_INIT_DECL(ASTvar1_init_decl n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	/* Get varNames from var1_list node */
	String[] varNames = (String[]) ((SimpleNode) children[0]).visit(this, null);
	/* Get type (and initialisation) from simple_spec_init node */
	Object type = ((SimpleNode) children[1]).visit(this, null);

	if ((type instanceof TypeConstant) || 
	    (type instanceof TypeANY_ELEMENTARY) || 
	    (type instanceof TypeFUNCTION_BLOCK))
	    {
		for (int i = 0; i < varNames.length; i++)
		    {
			builder.emitVarField(varNames[i], type, false /* global IECvariable? */, false /* IO_VAR */);

			// XXX global and Io should be set by using info about the variable decl.
		    }
	    }

	// else if (type instanceof DERIVED){ // XXX to be implemented
	else
	    {
		error("In var1_init_decl: this alternative not " + 
		      "yet implemented. Probably you have used a derived type");
	    }

	return null;
    }

    public Object visitVAR1_DECLARATION(ASTvar1_declaration n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	/* Get varNames from var1_list node */
	String[] varNames = (String[]) ((SimpleNode) children[0]).visit(this, null);
	/* Get type (and initialisation) from simple_spec_init node */
	Object type = ((SimpleNode) children[1]).visit(this, null);

	if ((type instanceof TypeConstant) || (type instanceof TypeFUNCTION_BLOCK))
	    {
		for (int i = 0; i < varNames.length; i++)
		    {
			builder.emitVarField(varNames[i], type, false /* global IECvariable? */, true /* IO_VAR */);

			// XXX global and Io should be set by using info about the variable decl.
		    }
	    }

	// else if (type instanceof DERIVED){  //XXX to be implemented
	else
	    {
		error("in var1__declaration: this alternative not " + 
		      "yet implemented. Probably you have used a derived type");
	    }

	return null;
    }

    public Object visitVAR1_LIST(ASTvar1_list n, Object o)
    {
	debug(n.toString());

	return n.getNames();    // String[]
    }

    public Object visitSIMPLE_SPEC_INIT(ASTsimple_spec_init n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	Object type = ((SimpleNode) children[0]).visit(this, null);

	if (type instanceof TypeConstant)
	    {
		/* type anything but Function_Block and Derived */
		try
		    {
			TypeANY init = (TypeANY) ((SimpleNode) children[1]).visit(this, null);

			if (init.getType() == (TypeConstant) type)
			    {
				return init;
			    }
			else
			    {
				error("Error: Nonconsistent types in : " + type + " and " + 
				      init.getType());
			    }
		    }
		catch (Exception e)
		    {
			return type;    // no init just a type => return the type
		    }
	    }
	else if (type instanceof TypeFUNCTION_BLOCK)
	    {
		return type;
	    }
	else
	    {
		error("in simple_spec_init: this alternative " + "not yet implemented");
	    }

	return null;
    }

    public Object visitSIMPLE_SPECIFICATION(ASTsimple_specification n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();

	/*
	 * Simple_specification has either a simple_type_name or an
	 * elementary_type_name_child.
	 */
	return ((SimpleNode) children[0]).visit(this, null);
    }

    public Object visitLABEL(ASTlabel n, Object o)
    {
	debug(n.toString());

	if (o != null)
	    {

		// Label visited via il_jump_operation (ie. it's a jump target)
		return n.getName();
	    }
	else
	    {
		builder.emitLABEL(n.getName());

		return null;
	    }
    }

    public Object visitCHARACTER_STRING(ASTcharacter_string n, Object o)
    {
	debug(n.toString());

	return new TypeWSTRING(n.getName());
    }

    public Object visitIL_FB_CALL(ASTil_fb_call n, Object o)
    {
	debug(n.toString());

	Node[] children = n.getChildren();
	/* get operator */
	IlCallOperator op = (IlCallOperator) ((SimpleNode) children[0]).visit(this, null); 
	String fbName = (String) ((SimpleNode) children[1]).visit(this, null);
	String fbTypeName = ((ASTfb_name)children[1]).getTypeName();
	Object[] args = null;

	if (children.length == 3 /* is there any arguments */)
	    {
		if (children[2] instanceof ASTil_operand_list)
		    {

			// will only take care of cases where this node is a
			// il_operand_list
			// start: hack för print
			Node[] argNodes = (Node[]) ((SimpleNode) children[2]).visit(this, null);

			if (argNodes != null)
			    {
				args = new Object[argNodes.length];

				for (int i = 0; i < argNodes.length; i++)
				    {
					args[i] = ((SimpleNode) argNodes[i]).visit(this, null);
				    }
			    }

			builder.emitIL_FB_CALL(op, fbName, args);

			// end: hack för print
		    }
		else
		    {    /* children[2] instanceof ASTil_param_list */
			BranchInstruction callCondition = builder.emitIL_FB_CALL_Start(op);    // check callCondition

			// builder.emitIL_FB_CALL_SetInputs()
			builder.emitIL_FB_CALL_Run(fbName, fbTypeName);

			// builder.emitIL_FB_CALL_SetOutputs()
			builder.emitIL_FB_CALL_End(callCondition);
		    }
	    }
	else
	    {    // no arguments
		BranchInstruction callCondition = builder.emitIL_FB_CALL_Start(op);    // check callCondition

		builder.emitIL_FB_CALL_Run(fbName, fbTypeName);
		builder.emitIL_FB_CALL_End(callCondition);
	    }

	return null;
    }

    public Object visitIL_CALL_OPERATOR(ASTil_call_operator n, Object o)
    {
	debug(n.toString());

	IlCallOperator op = IlCallOperator.CAL;    // dummy init

	try
	    {
		op = IlCallOperator.getOperator(n.getName());
	    }
	catch (IllegalOperatorException e)
	    {
		error("Illegal call operator: " + n.getName());
		return null;
	    }

	return op;
    }

    /*    public Object visitIL_CALL_JAVA_OPERATOR(ASTil_call_java_operator n, Object o)
    {
	debug(n.toString());
	//XXX
	// IlCallOperator op = IlCallOperator.CAL; //dummy init
	// try {op = IlCallOperator.getOperator(n.getName());}
	// catch (IllegalOperatorException e) {
	// System.err.println("Illegal call operator: " +n.getName());
	// return null;
	// }
	return null;    // op;
    }
    */
    public Object visitIL_OPERAND_LIST(ASTil_operand_list n, Object o)
    {
	debug(n.toString());

	return n.getChildren();
    }

    public Object visitMULTI_ELEMENT_VARIABLE(ASTmulti_element_variable n, Object o)
    {
	debug(n.toString());
	return null;
    }

    public Object visitRECORD_VARIABLE(ASTrecord_variable n, Object o)
    {
	debug(n.toString());
	return null;
    }

    public Object visitSTRUCTURED_VARIABLE(ASTstructured_variable n, Object o)
    {
	debug(n.toString());

	return null;
    }

    public Object visitIL_PARAM_LIST(ASTil_param_list n, Object o)
    {
	debug(n.toString());
	return null;
    }

    //XXX används denna överhuvudtaget????
    public File getTempFile() {
	return temp;
    }
}
