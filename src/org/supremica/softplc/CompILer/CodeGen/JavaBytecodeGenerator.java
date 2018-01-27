package org.supremica.softplc.CompILer.CodeGen;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import org.supremica.softplc.CompILer.CodeGen.Constants.IlCallOperator;
import org.supremica.softplc.CompILer.CodeGen.Constants.IlJumpOperator;
import org.supremica.softplc.CompILer.CodeGen.Constants.IllegalOperatorException;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.HelpMethods;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.IECDirectVariable;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.IECSymbolicVariable;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeANY;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeANY_ELEMENTARY;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeANY_NUM;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeBOOL;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeConstant;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeFUNCTION_BLOCK;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.TypeWSTRING;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTboolean_literal;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTcharacter_string;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTderived_function_block_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTelementary_type_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTfb_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTfield_selector;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTfunction_block_body;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTfunction_block_declaration;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_call_operator;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_expression;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_fb_call;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_jump_operation;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_jump_operator;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_operand_list;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_param_list;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_simple_instruction;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTil_simple_operation;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTinput_output_declarations;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTio_var_declarations;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTjava_block_declaration;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTlabel;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTlocated_var_decl;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTlocated_var_declarations;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTlocated_var_spec_init;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTlocation;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTmulti_element_variable;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTnumeric_literal;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTother_var_declarations;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTprogram_declaration;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTprogram_type_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTrecord_variable;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTsimple_instr_list;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTsimple_spec_init;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTsimple_specification;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTsimple_type_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTstructured_variable;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTtemp_var_decl;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar1_declaration;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar1_init_decl;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar1_list;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar_declaration;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar_declarations;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvar_init_decl;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvariable;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.ASTvariable_name;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.Node;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.SimpleNode;

import de.fub.bytecode.generic.BranchInstruction;

/**
 * This class is the main component for generating java bytecode from an
 * IEC 6-1131 Instruction List syntax tree.
 *
 * The input should be an abstract syntax tree of an IL source file. That
 * tree is then traversed by this class using a visitor pattern defined in
 * {@link org.supremica.softplc.CompILer.Parser.SimpleNodeVisitor}.
 * While traversing different instances of
 * {@link org.supremica.softplc.CompILer.CodeGen.Builder} is
 * used by JavaBytecodeGenerator to generate the actual bytecode.
 *
 * Some kinds of nodes in the syntax tree will not visited because
 * the tree is supposed to have passed a checker like
 * {@link org.supremica.softplc.CompILer.Checker.VariableChecker}. Therefore
 * some nodes are not meaningful while actually generating code.
 *
 * Since the return type of the instruction list in an IL expression
 * is not evaluated by the current checker, the type is always set to BOOL.
 * Therefore we have only implemented a few IL expression operators.
 * Since BOOL in java is treated like an int also DINT should work since
 * DINT is implemented as a java int.
 *
 * Abbreviations used later on in the documentation of this class
 * <pre>
 * POU - Program Organisation Unit (eg. program, function block etc.
 *       as defined by IEC 6-1131)
 * TOS - Top Of Stack, refers to the top of the stack in Java bytecode.
 * </pre>
 * @author Anders Röding
 */
public class JavaBytecodeGenerator
	implements CodeGenerator
{

	/**
	 * builder is the current Builder class used to encode a POU into bytecode
	 */
	Builder builder;

	/*
	 * This list is used for distinguishing declared data types
	 * and function_block types
	 */
	List<?> declaredDataTypes = new LinkedList<Object>();

	/**
	 * Logger prints nice error, info, warning and debug messages in the Supremica console
	 */
	private final Logger logger;
	private final boolean debug;    //only used when not started within Supremica

	/*
	 * errorsPresent is used to make sure no code is written before
	 * all detected errors are corrected
	 */
	private boolean errorsPresent = false;
	@SuppressWarnings("unused")
	private File temp;

	/**
	 * output directory for generated class files
	 */
	private final String outDir;

	/**
	 * constructs a new JavaBytecodeGenerator object
	 * @param abstractSyntaxTreeRoot the root node in the syntax tree that is to be
	 *                               converted into bytecode
	 * @param outputDir directory for storing generated class files
	 * @param logger a logger object for printing nice messages
	 * @param debug if true debug messages will be written at standard output if no
	 *              logger is supplied
	 */
	public JavaBytecodeGenerator(final SimpleNode abstractSyntaxTreeRoot,
	                             final String outputDir,
	                             final Logger logger,
	                             final boolean debug)
	{
		this.logger = logger;
		this.debug = debug;
		outDir = outputDir;

		final Node[] children = abstractSyntaxTreeRoot.getChildren();

		visitChildren(0, children);
	}

	/**
	 * takes care of debug messages.
	 * @param message a message to be displayed.
	 */
	void debug(final Object message)
	{
		if (logger != null)
		{
			logger.debug(message);
		}
		else if (debug)
		{
			System.out.println("Debug: " + message);
		}
	}

	/**
	 * takes care of error messages.
	 * After calling this method calls to dumpCode() has no effect.
	 * @param message a message to be displayed.
	 */
	void error(final Object message)
	{
		if (logger != null)
		{
			logger.error(message);
		}
		else
		{
			System.err.println("Error: " + message);
		}

		errorsPresent = true;
	}

	/**
	 * takes care of info messages.
	 * @param message a message to be displayed.
	 */
	void info(final Object message)
	{
		if (logger != null)
		{
			logger.info(message);
		}
		else
		{
			System.out.println(message);
		}
	}

	/**
	 * takes care of warning messages.
	 * @param message a message to be displayed.
	 */

	//XXX fråga knut hur denna fungerar
	void warn(final Object message)
	{
		if (logger != null)
		{
			logger.warn(message);
		}
		else
		{
			System.out.println("Warning: " + message);
		}
	}

	/**visitChildren is used to visit all nodes in an array (
	 * ie. the children array of a node) when you are not
	 * interested in the returned values, just the visiting itself
	 * @param startAtChild every node from this position will be visited
	 *                     but not the ones before (0 to visit all nodes)
	 * @param children the array with nodes to visit
	 */
	void visitChildren(final int startAtChild, final Node[] children)
	{
		if (children != null)
		{
			for (int i = startAtChild; i < children.length; i++)
			{
				final SimpleNode c = (SimpleNode) children[i];

				if (c != null)
				{
					c.visit(this, null);
				}
			}
		}
	}

	/**
	 * handles IL parts that are probably not properly dealt with. If you use an IL
		 * language construct that are not supported and you in some way get around the
		 * checker you will end up here.
	 * @param n a SimpleNode (any syntax tree node)
		 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
	 * @return nothing
		 */
	@Override
  public Object visitStandard(final SimpleNode n, final Object o)
	{
		debug(n.toString());
		warn("You might have used an unsupported IL construct. " + o);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTsimple_instr_list node
		 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
	 * @return nothing
		 */
	@Override
  public Object visitSIMPLE_INSTR_LIST(final ASTsimple_instr_list n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTil_simple_instruction node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_SIMPLE_INSTRUCTION(final ASTil_simple_instruction n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final SimpleNode c = (SimpleNode) children[0];

		if (c != null)
		{
			c.visit(this, null);
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTil_simple_operation node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_SIMPLE_OPERATION(final ASTil_simple_operation n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final Object arg = ((SimpleNode) children[0]).visit(this, null);
		final String operator = n.getName();

		if (arg != null)
		{
			builder.emitIL_SIMPLE_OPERATION(operator, arg);
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTil_expression node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_EXPRESSION(final ASTil_expression n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final String operator = n.getName();

		if (children.length == 0)
		{
			error("IL_EXPRESSION missing operand(s)");

			return null;
		}
		else if (children.length == 1)
		{
			try
			{
				final ASTsimple_instr_list simInstrList = (ASTsimple_instr_list) children[0];

				builder.emitStackSpace(1);
				simInstrList.visit(this, null);
			}
			catch (final Exception e)
			{
				final Object arg = ((SimpleNode) children[0]).visit(this, null);

				builder.emitLoad(arg);    // opens new IL scope and loads arg
			}
		}
		else
		{    /* children.length == 2*/
			final Object arg = ((SimpleNode) children[0]).visit(this, null);

			builder.emitLoad(arg);    // opens new IL scope and loads arg
			((SimpleNode) children[1]).visit(this, null);
		}

		builder.emitIL_EXPRESSION(operator, TypeConstant.T_BOOL, null);

		/* The type of the argument to an expr_operator must be set by the checker
		 * in the node, not set to TypeConstants.T_BOOL. But for the moment
		 * we'll do it this way.*/
		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTil_jump_operation node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_JUMP_OPERATION(final ASTil_jump_operation n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final IlJumpOperator op = (IlJumpOperator) ((SimpleNode) children[0]).visit(this, null);
		final String label = (String) ((SimpleNode) children[1]).visit(this, new String());

		builder.emitIL_JUMP_OPERATION(op, label);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTil_jump_operator node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a jump operator*/
	@Override
  public Object visitIL_JUMP_OPERATOR(final ASTil_jump_operator n, final Object o)
	{
		debug(n.toString());

		IlJumpOperator op = IlJumpOperator.JMP;    // dummy init

		try
		{
			op = IlJumpOperator.getOperator(n.getName());
		}
		catch (final IllegalOperatorException e)
		{
			error("Illegal jump operator: " + n.getName());

			return null;
		}

		return op;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTnumeric_literal node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a numeric literal
		 */
	@Override
  public Object visitNUMERIC_LITERAL(final ASTnumeric_literal n, final Object o)
	{
		debug(n.toString());

		final TypeANY_NUM p = HelpMethods.parseANY_NUM(n.getName());

		if (p == null)
		{
			error("This constant type is not implemented: " + n.getName() + ". Try typed constant " + "(e.g. DINT#5)");
		}

		return p;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTboolean_literal node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a boolean literal*/
	@Override
  public Object visitBOOLEAN_LITERAL(final ASTboolean_literal n, final Object o)
	{
		debug(n.toString());

		return HelpMethods.parseBOOL(n.getName());
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTvariable node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a variable
		 */
	@Override
  public Object visitVARIABLE(final ASTvariable n, final Object o)
	{
		debug(n.toString());

		if (n.isDirectVariable())
		{
			return new IECDirectVariable(n.getName());
		}
		else if (n.getType() == TypeConstant.T_DERIVED)
		{
			if (n.getFieldSelector() != null)
			{
				/*n is referring to a field in a derived variable*/
				return new IECSymbolicVariable(n.getName(), n.getType(), n.getTypeName(), n.getFieldSelector(), n.getFieldSelectorType(), n.getFieldSelectorTypeName());
			}
			else
			{
				/*n is referring to the derived variable itself*/
				return new IECSymbolicVariable(n.getName(), n.getType(), n.getTypeName(), null, null, null);
			}
		}
		else
		{
			return new IECSymbolicVariable(n.getName(), n.getType());
		}
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTvariable_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return variable name
		 */
	@Override
  public Object visitVARIABLE_NAME(final ASTvariable_name n, final Object o)
	{
		debug(n.toString());

		return n.getName();
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTfield_selector node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitFIELD_SELECTOR(final ASTfield_selector n, final Object o)
	{
		debug(n.toString());

		//error("Found fieldselector");
		/*should never happen in CodeGenerator only in the checker*/
		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTelementary_type_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a type constant
		 */
	@Override
  public Object visitELEMENTARY_TYPE_NAME(final ASTelementary_type_name n, final Object o)
	{
		debug(n.toString());

		return HelpMethods.parseTypeConstants(n.getName());
	}

	/**
	 * handles all that happens when visiting the node n while traversing the syntax tree.
	 * @param n an ASTsimple_type_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return object describing the simple type name
		 */
	@Override
  public Object visitSIMPLE_TYPE_NAME(final ASTsimple_type_name n, final Object o)
	{
		debug(n.toString());

		if (!declaredDataTypes.contains(n.getName()))
		{
			/* we have found a function block initialisation */
			return new TypeFUNCTION_BLOCK(n.getName());
		}

		error("Found simple_type_name," + n.getName() + " that was " + "not a function block type. This is not implemented.");

		return null;

		/*
		 * if declaredDataType. Should be changed when
		 * other derived data types than function blocks are implemented
		 */
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTprogram_type_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return program name
		 */
	@Override
  public Object visitPROGRAM_TYPE_NAME(final ASTprogram_type_name n, final Object o)
	{
		debug(n.toString());

		return n.getName();
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTderived_function_block_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return derived function block name
		 */
	@Override
  public Object visitDERIVED_FUNCTION_BLOCK_NAME(final ASTderived_function_block_name n, final Object o)
	{
		debug(n.toString());

		return n.getName();
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTfb_name node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return function block name
		 */
	@Override
  public Object visitFB_NAME(final ASTfb_name n, final Object o)
	{
		debug(n.toString());

		return n.getName();
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTfunction_block_body node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitFUNCTION_BLOCK_BODY(final ASTfunction_block_body n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		builder.emitStackSpace(1);    // open new scope
		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTprogram_declaration node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitPROGRAM_DECLARATION(final ASTprogram_declaration n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final String programname = (String) ((SimpleNode) children[0]).visit(this, null);

		builder = new ProgramBuilder(programname, outDir, logger, debug);

		visitChildren(1, children);

		if (!errorsPresent)
		{
			builder.dumpCode();
		}
		else
		{
			error("Errors present! Could not generate code!");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTfunction_block_declaration node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitFUNCTION_BLOCK_DECLARATION(final ASTfunction_block_declaration n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final String fbName = (String) ((SimpleNode) children[0]).visit(this, null);

		builder = new FunctionBlockBuilder(fbName, outDir, logger, debug);

		visitChildren(1, children);

		if (!errorsPresent)
		{
			builder.dumpCode();
		}
		else
		{
			error("Errors present! Could not generate code!");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTjava_block_declaration node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitJAVA_BLOCK_DECLARATION(final ASTjava_block_declaration n, final Object o)
	{
		debug(n.toString());

		return null;
	}

	/* This part takes care of some variable declarations */

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTother_var_declarations node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitOTHER_VAR_DECLARATIONS(final ASTother_var_declarations n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar_declarations node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitVAR_DECLARATIONS(final ASTvar_declarations n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar_init_decl node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitVAR_INIT_DECL(final ASTvar_init_decl n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final SimpleNode c = (SimpleNode) children[0];

		if (c != null)
		{
			c.visit(this, null);
		}

		return null;
	}

	/* Located variables */

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTlocated_var_declarations node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitLOCATED_VAR_DECLARATIONS(final ASTlocated_var_declarations n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTlocated_var_decl node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitLOCATED_VAR_DECL(final ASTlocated_var_decl n, final Object o)
	{
		debug(n.toString());

		int base = 0;
		final Node[] children = n.getChildren();

		if (children.length == 3)
		{    // always 2 or 3, and in case of 3
			base = 1;    // we don't care about varname anyway
		}

		final IECDirectVariable location = (IECDirectVariable) ((SimpleNode) children[base]).visit(this, null);
		final Object type = ((SimpleNode) children[base + 1]).visit(this, null);

		if (type instanceof TypeConstant)
		{

			// This is a type check, should be in a checker instead.
			// but left here as a precaution
			if (!((TypeConstant) type == TypeConstant.T_BOOL))
			{
				error("Direct variables only implemented " + "for BOOL. Not for " + type);
			}
		}
		else if (type instanceof TypeANY_ELEMENTARY)
		{
			if (((TypeANY_ELEMENTARY) type).getType() == TypeConstant.T_BOOL)
			{
				final TypeBOOL value = (TypeBOOL) type;

				builder.emitDirectInit(location, value);
			}
			else
			{
				error("Direct variables only implemented " + "for BOOL. Not for " + ((TypeANY_ELEMENTARY) type).getType());
			}
		}
		else
		{    // error
			error("Direct variables only implemented for BOOL. Either " + "the variable should " + "just be declared BOOL or initialised with a boolean value");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTlocated_var_spec_init node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return type constant or object describing initial value.
		 *          See also returns for {@link #visitSIMPLE_SPEC_INIT}.
		 */
	@Override
  public Object visitLOCATED_VAR_SPEC_INIT(final ASTlocated_var_spec_init n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();    /* should always be of length 1*/

		if (children[0] instanceof ASTsimple_spec_init)
		{
			return ((SimpleNode) children[0]).visit(this, null);
		}
		else
		{
			error("Initialisation of located variables only implemented for " + "simple BOOL (e.g. var AT %QX12 : BOOL := TRUE;)");

			return null;
		}
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTlocation node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a direct variable
		 */
	@Override
  public Object visitLOCATION(final ASTlocation n, final Object o)
	{
		debug(n.toString());

		return new IECDirectVariable(n.getName());
	}

	/* Input and output variables */

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTio_var_declarations node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIO_VAR_DECLARATIONS(final ASTio_var_declarations n, final Object o)
	{
		debug(n.toString());

		final SimpleNode child = (SimpleNode) (n.getChildren())[0];    // n always has exactly one child

		if (child instanceof ASTinput_output_declarations)
		{
			child.visit(this, null);
		}
		else
		{

			/*child instanceof ASTinput_declaration or
			  child instanceof ASToutput_declarations*/
			error("Only var_in_out is implemented, not var_in " + "or var_out");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTinput_output_declarations node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitINPUT_OUTPUT_DECLARATIONS(final ASTinput_output_declarations n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		visitChildren(0, children);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar_declaration node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitVAR_DECLARATION(final ASTvar_declaration n, final Object o)
	{
		debug(n.toString());

		final Node child = (n.getChildren())[0];    /* always one child*/

		((SimpleNode) child).visit(this, null);

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTtemp_var_decl node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitTEMP_VAR_DECL(final ASTtemp_var_decl n, final Object o)
	{
		debug(n.toString());
		((SimpleNode) n.getChildren()[0]).visit(this, null);

		return null;
	}

	/* General specification and variable declaration */

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar1_init_decl node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitVAR1_INIT_DECL(final ASTvar1_init_decl n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		/* Get varNames from var1_list node */
		final String[] varNames = (String[]) ((SimpleNode) children[0]).visit(this, null);
		/* Get type (and initialisation) from simple_spec_init node */
		final Object type = ((SimpleNode) children[1]).visit(this, null);

		if ((type instanceof TypeConstant) || (type instanceof TypeANY_ELEMENTARY) || (type instanceof TypeFUNCTION_BLOCK))
		{
			for (int i = 0; i < varNames.length; i++)
			{
				builder.emitVarField(varNames[i], type, false /* global IECvariable? */, false /* IO_VAR */);

				/* global and Io should preferrably be set by using info
				 * about the variable decl. but that is not implemented yet
				 */
			}
		}
		else
		{
			error("In var1_init_decl: this alternative not " + "yet implemented. Probably you have used a derived type");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar1_declaration node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitVAR1_DECLARATION(final ASTvar1_declaration n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		/* Get varNames from var1_list node */
		final String[] varNames = (String[]) ((SimpleNode) children[0]).visit(this, null);
		/* Get type (and initialisation) from simple_spec_init node */
		final Object type = ((SimpleNode) children[1]).visit(this, null);

		if ((type instanceof TypeConstant) || (type instanceof TypeFUNCTION_BLOCK))
		{
			for (int i = 0; i < varNames.length; i++)
			{
				builder.emitVarField(varNames[i], type, false /* global IECvariable? */, true /* IO_VAR */);

				/* global and Io should preferrably be set by using info
				 * about the variable decl. but that is not implemented yet
				 */
			}
		}
		else
		{
			error("in var1__declaration: this alternative not " + "yet implemented. Probably you have used a derived type");
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTvar1_list node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return array of names (String[])
		 */
	@Override
  public Object visitVAR1_LIST(final ASTvar1_list n, final Object o)
	{
		debug(n.toString());

		return n.getNames();
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTsimple_spec_init node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return type constant or object describing an intial value
		 */
	@Override
  public Object visitSIMPLE_SPEC_INIT(final ASTsimple_spec_init n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		final Object type = ((SimpleNode) children[0]).visit(this, null);

		if (type instanceof TypeConstant)
		{
			/* type anything but derived*/
			try
			{
				final TypeANY init = (TypeANY) ((SimpleNode) children[1]).visit(this, null);

				if (init.getType() == (TypeConstant) type)
				{
					return init;
				}
				else
				{
					error("Error: Nonconsistent types in : " + type + " and " + init.getType());
				}
			}
			catch (final Exception e)
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

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTsimple_specification node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return either return value from {@link #visitSIMPLE_TYPE_NAME} or {@link #visitELEMENTARY_TYPE_NAME}
		 */
	@Override
  public Object visitSIMPLE_SPECIFICATION(final ASTsimple_specification n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();

		/*
		 * Simple_specification has either a simple_type_name or an
		 * elementary_type_name_child.
		 */
		return ((SimpleNode) children[0]).visit(this, null);
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTlabel node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return if visited via il_jump_operation (i.e. the label is used as a
		 *         jump target) a label name; else nothing
		 */
	@Override
  public Object visitLABEL(final ASTlabel n, final Object o)
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

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTcharacter_string node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return a character string constant
		 */
	@Override
  public Object visitCHARACTER_STRING(final ASTcharacter_string n, final Object o)
	{
		debug(n.toString());

		return new TypeWSTRING(n.getName());
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTil_fb_call node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_FB_CALL(final ASTil_fb_call n, final Object o)
	{
		debug(n.toString());

		final Node[] children = n.getChildren();
		/* get operator */
		final IlCallOperator op = (IlCallOperator) ((SimpleNode) children[0]).visit(this, null);
		final String fbName = (String) ((SimpleNode) children[1]).visit(this, null);
		final String fbTypeName = ((ASTfb_name) children[1]).getTypeName();
		if (children.length == 3 /* is there any arguments */)
		{
			error("The Checker has not done its job properly. Wrong " + "number of arguments to IL_FB_CALL");
		}
		else
		{    // no arguments
			final BranchInstruction callCondition = builder.emitIL_FB_CALL_Start(op);

			// check callCondition
			builder.emitIL_FB_CALL_Run(fbName, fbTypeName);
			builder.emitIL_FB_CALL_End(callCondition);
		}

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while traversing
		 * the syntax tree.
	 * @param n an ASTil_call_operator node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return an IL call operator
		 *  {@link org.supremica.softplc.CompILer.CodeGen.Constants.IlCallOperator}
		 */
	@Override
  public Object visitIL_CALL_OPERATOR(final ASTil_call_operator n, final Object o)
	{
		debug(n.toString());

		try
		{
			final IlCallOperator op = IlCallOperator.getOperator(n.getName());

			return op;
		}
		catch (final IllegalOperatorException e)
		{
			error("Illegal call operator: " + n.getName());

			return null;
		}
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTil_operand_list node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return the children of n
		 */
	@Override
  public Object visitIL_OPERAND_LIST(final ASTil_operand_list n, final Object o)
	{
		debug(n.toString());

		return n.getChildren();
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTmulti_element node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitMULTI_ELEMENT_VARIABLE(final ASTmulti_element_variable n, final Object o)
	{
		debug(n.toString());

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTrecord_variable node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitRECORD_VARIABLE(final ASTrecord_variable n, final Object o)
	{
		debug(n.toString());

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTstructured_variable node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitSTRUCTURED_VARIABLE(final ASTstructured_variable n, final Object o)
	{
		debug(n.toString());

		return null;
	}

	/**
	 * handles all that happens when visiting the node n while
		 * traversing the syntax tree.
	 * @param n an ASTil_param_list node
	 * @param o an argument (in most cases not used in JavaBytecodeGenerator)
		 * @return nothing
		 */
	@Override
  public Object visitIL_PARAM_LIST(final ASTil_param_list n, final Object o)
	{
		debug(n.toString());

		return null;
	}
}
