package org.supremica.softplc.CompILer.Checker;
//import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
//import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import java.util.*;
import java.util.LinkedList;


/**
 * Checks a IEC 6-1131 Instruction List syntax tree
 * @author Thomas Isakson
 */
public class VariableChecker implements SimpleNodeVisitor {

	/*
     * VariableCHecker har följande uppgifter:
     *  - Ersätt alla förekomster av located variables med
     *    motsvarande direct_variable.
     *    Exempel:
     *    Om variabeln VL är deklarerad som VL AT %IX10: BOOL;
     *    ersätter vi "ST VL" med "ST %IX10" i programmet.
	 *
     *    Annorlunda uttryckt, ersätt
     *       variable: symbolic_variable B
     *       variable_name: B
     *    med
     *       variable: direct_variable %IX10
     *
     *  - Kolla att det inte finns några förekomster av
     *    direct_variables i function_blocks. Om det finns ska
     *    ett fel returneras.
     */



	/* Hashtable with symbolic variables placed 
	 * in the PROGRAM block 
	 * Keys  : variable names
	 * Values: variable types
	 */
	Hashtable symbolicVariables;

	/* Hashtable with located variables placed 
	 * in the PROGRAM block 
	 * Keys  : locations
	 * Values: variable names
	 */
	Hashtable locatedVariables;

	/* Hashtable with all function blocks 
	 * Keys  : function block names
	 * Values: hashtables with symbolic variables
	 */
	Hashtable functionBlocks;

	/* Object that is passed down the tree by the 
     * visitor 
     */
	VCinfo    VC;

	/* When an error such as an undeclared variable is 
     * encountered by the checker, success is set to 
     * FALSE
     */
	boolean success = true;

	SimpleNode abstractSyntaxTreeRoot;


    public VariableChecker(SimpleNode abstractSyntaxTreeRoot){
		this.abstractSyntaxTreeRoot = abstractSyntaxTreeRoot;
    }

    /**
     * Checks the syntax tree
     * @return true if no errors were encountered
	 */
	public boolean check() {
		symbolicVariables = new Hashtable();
		locatedVariables  = new Hashtable();
		functionBlocks    = new Hashtable();
		VC                = new VCinfo();

        Node[] children;

		/* The tree is visited twice:
		 *  - the first time, only the declaration parts
         *    are visited, and the variables are put in 
         *    hashtables for later retrieval
         *  - the second time, the rest of the program
         *    is checked
         */
		for(int j=1; j<3; j++) {
			((VCinfo)VC).pass = j;

			children = abstractSyntaxTreeRoot.getChildren();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					SimpleNode n = (SimpleNode)children[i];
					if (n != null) {
						n.visit(this, VC);
					}
				}
			}
		}
		return success;
	}




	/**
	 * Creates a "LD" simple operation node.
	 *
	 * @param type Can be one of "numeric_literal", 
	 *                           "character_string", 
	 *                           "boolean_literal" or
	 *                           "symbolic_variable"
	 *
	 * @param value If type is "symbolic_variable", value 
	 *              should be the variable name.
	 *
	 * @param blockName If the current location in the syntax
     *                  tree is the program block, blockName
	 *                  should be null. Otherwise, blockName
	 *                  should be the name of the function
	 *                  block of the current location.
	 */
	private ASTil_simple_operation create_LD_Node(String type, String value, String blockName) {
		ASTil_simple_operation jjtn_simple_op;

		jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
		jjtn_simple_op.setName("LD");

		if (type == "numeric_literal") {
			ASTnumeric_literal jjtn_numeric_literal = new ASTnumeric_literal(parserTreeConstants.JJTNUMERIC_LITERAL);
			jjtn_numeric_literal.setName(value);
			jjtn_simple_op.jjtAddChild(jjtn_numeric_literal, 0);

		} else if (type == "character_string") {
			ASTcharacter_string jjtn_character_string = new ASTcharacter_string(parserTreeConstants.JJTCHARACTER_STRING);
			jjtn_character_string.setName(value);
			jjtn_simple_op.jjtAddChild(jjtn_character_string, 0);

		} else if (type == "boolean_literal") {
			ASTboolean_literal jjtn_boolean_literal = new ASTboolean_literal(parserTreeConstants.JJTBOOLEAN_LITERAL);
			jjtn_boolean_literal.setName(value);
			jjtn_simple_op.jjtAddChild(jjtn_boolean_literal, 0);

		} else if (type == "symbolic_variable") {
			Variable v = getVariable(value, blockName);

			ASTvariable jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
			jjtn_variable.setName(v.name);
			jjtn_variable.setTypeName(v.typeName);
			jjtn_variable.setIsDirectVariable(v.directVariable);
			jjtn_variable.setFieldSelector(v.fieldSelector);
			jjtn_variable.setFieldSelectorTypeName(v.fieldSelectorTypeName);
			jjtn_variable.setIsFunctionBlock(v.isFunctionBlock);

			jjtn_simple_op.jjtAddChild(jjtn_variable, 0);

		} else if (type == "direct_variable") {
			ASTvariable jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
			jjtn_variable.setName(value);
			jjtn_variable.setIsDirectVariable(true);

			jjtn_simple_op.jjtAddChild(jjtn_variable, 0);

		}

		return jjtn_simple_op;
	}



	/**
	 * Creates a "ST" simple operation node.
	 *
	 * @param variableName
	 *
	 * @param blockName If the current location in the syntax
     *                  tree is the program block, blockName
	 *                  should be null. Otherwise, blockName
	 *                  should be the name of the function
	 *                  block of the current location.
	 */
	private ASTil_simple_operation create_ST_Node(String variableName, String blockName) {
		ASTil_simple_operation jjtn_simple_op;
		ASTvariable            jjtn_variable;
		Variable               v;

		jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
		jjtn_simple_op.setName("ST");

		v = getVariable(variableName, blockName);

		if (v==null) {
			/* The variable was not declared */
			return null;
		} else {
			jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
			jjtn_variable.setName(v.name);
			jjtn_variable.setTypeName(v.typeName);
			jjtn_variable.setIsDirectVariable(v.directVariable);
			jjtn_variable.setFieldSelector(v.fieldSelector);
			jjtn_variable.setFieldSelectorTypeName(v.fieldSelectorTypeName);
			jjtn_variable.setIsFunctionBlock(v.isFunctionBlock);
			
			jjtn_simple_op.jjtAddChild(jjtn_variable, 0);

			return jjtn_simple_op;
		}

	}



	/**
	 * Returns a Variable object with all available
	 * properties of the requested variable
	 *
	 * @param variableName Can be a simple symbolic variable or a
	 *                     function block variable, i.e. 
	 *                     "myfb.myfieldselector"
	 *
	 * @param blockName If the current location in the syntax
     *                  tree is the program block, blockName
	 *                  should be null. Otherwise, blockName
	 *                  should be the name of the function
	 *                  block of the current location.
	 */
	private Variable getVariable(String variableName, String blockName) {
		Hashtable fbVariables;
		Variable v = new Variable();
		StringTokenizer tokens = new StringTokenizer(variableName, ".", false);

		int TokenCount = tokens.countTokens();
		v.name = tokens.nextToken();

		if (TokenCount == 2)
		{
			v.fieldSelector = tokens.nextToken();
		}


		/*
		 * Om variabeln är en direct variable ...
         */
		if (locatedVariables.containsValue(v.name))
			{
				v.directVariable = true;
				return v;
			}


		/*
         * Check that the variable is declared, and retrieve
		 * the variable's type.
         */
		if (blockName == null)
			/* Look for the variable in the program's declarations */
			{
				if (!(   symbolicVariables.containsKey(v.name)
						 || locatedVariables.containsKey(v.name)
						 )) {
					success = false;
					System.err.println("Error: Undeclared variable: " + v.name);
					return null;
				}

				v.typeName = (String)symbolicVariables.get(v.name);
			}
		else
			/* Look for the variable in the function block's declarations */
			{
				fbVariables = (Hashtable)functionBlocks.get(blockName);

				if (!fbVariables.containsKey(v.name)) {
					success = false;
					System.err.println("Error in function block " + blockName + ": Undeclared variable: " + v.name);
					return null;
				}

				v.typeName = (String)fbVariables.get(v.name);
			}



		/*
		 * Om variabeln är en located variable ...
         */
		if (locatedVariables.containsKey(v.name))
			{
				v.directVariable = true;
				v.name = ((String)locatedVariables.get(v.name));
			}


		/*
         * Om variabeln är ett funktionsblock
         * ska detta anges i variabel-noden, och
         * fieldselector-typen ska sättas.
         */
		else if (functionBlocks.containsKey(v.typeName))
			{

				v.isFunctionBlock = true;

				fbVariables = (Hashtable)functionBlocks.get(v.typeName);
				v.fieldSelectorTypeName = (String)fbVariables.get(v.fieldSelector);
			}

		return v;
	}



   	public String getFunctionBlockTypeName(String fbVariableName, String blockName) {
		Hashtable fbVariables;

		/*
         * Kolla att det anropade funktionsblocket är deklarerat och hämta typeName
         */
		if (blockName == null)
			/* funktionsblocket finns i ett program */
			{
				if (!(   symbolicVariables.containsKey(fbVariableName)
						 || locatedVariables.containsKey(fbVariableName)
						 )) {
					success = false;
					System.err.println("Error: Undeclared function block: " + fbVariableName);
				}

				return (String)symbolicVariables.get(fbVariableName);
			}
		else
			/* funktionsblocket finns i ett function block */
			{
				fbVariables = (Hashtable)functionBlocks.get(blockName);

				if (!fbVariables.containsKey(fbVariableName)) {
					success = false;
					System.err.println("Error: Undeclared function block: " + fbVariableName);
				}

				return (String)fbVariables.get(fbVariableName);
			}

	}





	public Object visitIL_PARAM_LIST(ASTil_param_list n, Object o){
		LinkedList inParameters;
		LinkedList outParameters;
		int i;
		int pos;
 		Node parent;
		Node greatParent;
		ASTil_simple_operation jjtn_simple_op;
		Param p;
		String fb_name;
		String functionBlockName;


		if (((VCinfo)o).blockType=="program") {
			functionBlockName = null;
		} else {
			functionBlockName = ((VCinfo)o).functionBlockName;
		}



		inParameters = n.getInParameters();
		outParameters = n.getOutParameters();

		parent = n.jjtGetParent();

		/* Skapa referens till function_block_body */
		greatParent = parent.jjtGetParent();

		fb_name = ((ASTfb_name)(((ASTil_fb_call)parent).getChildren())[1]).getName();

		/* il_fb_call's index i function_block_body's barn
           (il_fb_call är il_param_list's förälder)          */
		pos = ((VCinfo)o).childIndex;

		if (inParameters != null) {
			for (i = 0; i < inParameters.size(); i++)
				{

					p = (Param)inParameters.get(i);

					/*
                     * LD
                     */
					jjtn_simple_op = create_LD_Node(p.parameterType, p.value, functionBlockName);

					greatParent.jjtInsertChild(jjtn_simple_op, pos);

					((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
					pos = pos + 1;




					/*
                     * ST
                     */
					jjtn_simple_op = create_ST_Node(fb_name + "." + p.parameter, functionBlockName);

					if (jjtn_simple_op != null) {
						greatParent.jjtInsertChild(jjtn_simple_op, pos);

						((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
						pos = pos + 1;
					}
				}

		}

		/* Ta bort noden il_param_list och inkrementera pos
         * så att output-parameter-noderna läggs in efter
         * il_fb_call-noden
         */
		parent.jjtDeleteChild(2);
		pos = pos + 1;


		if (outParameters != null) {
			for (i = 0; i < outParameters.size(); i++)
				{
					p = (Param)outParameters.get(i);


					/*
                     * LD
                     */
					jjtn_simple_op = create_LD_Node("symbolic_variable", fb_name + "." + p.parameter, functionBlockName);

					greatParent.jjtInsertChild(jjtn_simple_op, pos);

					((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
					pos = pos + 1;



					/*
                     * ST
                     */
					jjtn_simple_op = create_ST_Node(p.value, functionBlockName);

					greatParent.jjtInsertChild(jjtn_simple_op, pos);

					((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
					pos = pos + 1;


				}
		}


		return o;
	}


	public Object visitPROGRAM_DECLARATION(ASTprogram_declaration n,
										   Object o){

		Node[] children = n.getChildren();
		String programname =(String)
			((SimpleNode)children[0]).visit(this, o);

		for (int i = 1; i < children.length; i++) {
			SimpleNode c = (SimpleNode)children[i];
			if (c != null) {
				((VCinfo)o).blockType = "program";
				c.visit(this, o);
			}
		}
		return null;
	}


    public Object visitFUNCTION_BLOCK_DECLARATION(
								ASTfunction_block_declaration n,
								Object o){

        Node[] children = n.getChildren();
		String fbName;

	    fbName = ((ASTderived_function_block_name)children[0]).getName();

		if (((VCinfo)o).pass == 1) {
			// Lägg in en ny hashtabell i den stora hashtabellen med
			// alla funktionsblock
			functionBlocks.put(fbName, new Hashtable());
		}

		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					((VCinfo)o).blockType = "functionBlock";
					((VCinfo)o).functionBlockName = fbName;
					c.visit(this, o);
					//					c.visit(this, fbName);
				}
			}
		}

        return null;

    }



    public Object visitJAVA_BLOCK_DECLARATION(
								ASTjava_block_declaration n,
								Object o){

        Node[] children = n.getChildren();
		String fbName;

	    fbName = ((ASTderived_function_block_name)children[0]).getName();

		if (((VCinfo)o).pass == 1) {
			// Lägg in en ny hashtabell i den stora hashtabellen med
			// alla funktionsblock
			functionBlocks.put(fbName, new Hashtable());
		}

		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					((VCinfo)o).blockType = "functionBlock";
					((VCinfo)o).functionBlockName = fbName;
					c.visit(this, o);
				}
			}
		}
        return null;

    }



    public Object visitVAR1_DECLARATION(ASTvar1_declaration n, Object o) {
        Node[] children = n.getChildren();

		/*
         * Sätt variabeltypen i var1_list
         */
		ASTsimple_specification simple_specification;
		ASTvar1_list var1_list;

		var1_list = (ASTvar1_list)children[0];
		/*
         * Här förutsätter jag att children[1] är av typen ASTsimple_specification.
         * Det kan dock även vara av typen ASTsubrange_specification eller
         * ASTenumerated_specification. I så fall blir det problem...
         * /Thomas
         */
		simple_specification = (ASTsimple_specification)children[1];

		var1_list.setTypeName(simple_specification.getTypeName());


		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return o;

	}


    public Object visitVAR1_INIT_DECL(ASTvar1_init_decl n, Object o) {

		Node[] children = n.getChildren();

		/*
         * Sätt variabeltypen i var1_list
         */
		ASTsimple_spec_init simple_spec_init;
		ASTvar1_list var1_list;

		var1_list = (ASTvar1_list)children[0];
		simple_spec_init = (ASTsimple_spec_init)children[1];

		var1_list.setTypeName(simple_spec_init.getTypeName());


		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

    }

    public Object visitVAR1_LIST(ASTvar1_list n, Object o) {
		String[] names;
		int i;
		Hashtable tmpHash;
		int numElements;


		//varför ha denna och inte
		// names = ((ASTvar1_list)n.getChildren()[0]).getNames();
		//och därmed slippa att ha en array med alla namnen i
		// ASTvar1_init_decl, ASTvar1_list och ASTvar1_declaration
		names = n.getNames();

		if (((VCinfo)o).blockType == "program") {
			for(i=0; i<names.length; i++){
				numElements = symbolicVariables.size() / 2;
				symbolicVariables.put(names[i], n.getTypeName());
				symbolicVariables.put("" + numElements, names[i]);
			}
		} else if (((VCinfo)o).blockType == "functionBlock") {
			String fbName = ((VCinfo)o).functionBlockName;

			tmpHash = (Hashtable)functionBlocks.get(fbName);

			// Lägg in variablerna i hashtabellen
			for(i=0; i<names.length; i++) {
				numElements = tmpHash.size() / 2;
				tmpHash.put(names[i], n.getTypeName());
				tmpHash.put("" + numElements, names[i]);
			}

			functionBlocks.put(fbName, tmpHash);

		}

		return null;
    }


	public Object visitLOCATED_VAR_DECL(ASTlocated_var_decl n, Object o) {
		// Lägg in variabeln i hashtabellen
		locatedVariables.put(n.getVariableName(), n.getLocation());

        Node[] children = n.getChildren();
        for (int i = 0; i < children.length; i++) {
            SimpleNode c = (SimpleNode)children[i];
            if (c != null) {
                c.visit(this, o);
            }
		}
		return null;
	}


	public Object visitVARIABLE(ASTvariable n, Object o) {
		String typeName = null;

		if (!n.isDirectVariable()) {

			/*
             * Kolla att variabeln är deklarerad och hämta typeName
             */
			if (((VCinfo)o).blockType == "program")
				{
					typeName = (String)symbolicVariables.get(n.getName());

					if (!(   symbolicVariables.containsKey(n.getName())
							 || locatedVariables.containsKey(n.getName())
					    )) {
						success = false;
						System.err.println("Error: Undeclared variable: " + n.getName());
					}
				}
			else if (((VCinfo)o).blockType == "functionBlock")
				{
					Hashtable fbVariables = (Hashtable)functionBlocks.get(((VCinfo)o).functionBlockName);
					if (!fbVariables.containsKey(n.getName())) {
						success = false;
						System.err.println("Error: Undeclared variable: " + n.getName());
					}

					typeName = (String)fbVariables.get(n.getName());

				}
			else
				{
					success = false;
					System.out.println("*** Fel i visitVariable");
				}

			n.setTypeName(typeName);


			/*
			 * Om variabeln är en located variable
			 * ska location sättas in i variabel-noden
             */
			if (locatedVariables.containsKey(n.getName()))
				{
					n.setIsDirectVariable(true);
					n.setName((String)locatedVariables.get(n.getName()));
				}


			/*
             * Om variabeln är ett funktionsblock
             * ska detta anges i variabel-noden, och
             * fieldselector-typen ska sättas.
             */
			else if (functionBlocks.containsKey(typeName))
				{
					n.setIsFunctionBlock(true);

					Hashtable fbVariables = (Hashtable)functionBlocks.get(typeName);
					
					/* kolla att det finns en field_selector */
					if (n.getFieldSelector() != null) {
						n.setFieldSelectorTypeName((String)fbVariables.get(n.getFieldSelector()));
					}
				}

		}



        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}


    public Object visitFB_NAME(ASTfb_name n, Object o){
		String typeName = null;

		if (((VCinfo)o).blockType == "functionBlock") {
			Hashtable fbVariables = (Hashtable)functionBlocks.get(((VCinfo)o).functionBlockName);
			if (!fbVariables.containsKey(n.getName())) {
			    success = false;
			    System.err.println("Error: Undeclared variable: " + n.getName());
			} else {
				typeName = (String)fbVariables.get(n.getName());
				n.setTypeName(typeName);
			}
		} else
			{
				if (symbolicVariables.containsKey(n.getName())) {
					n.setTypeName((String)symbolicVariables.get(n.getName()));
				}
			}
		return null;
	}



    public Object visitFUNCTION_BLOCK_BODY(ASTfunction_block_body n, Object o){
		if (((VCinfo)o).pass == 2) {
			((VCinfo)o).childIndex = -1;

			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
						c.visit(this, o);
					}
				}
			}
		}
        return null;
    }


	public Object visitLOCATED_VAR_DECLARATIONS(ASTlocated_var_declarations n,
				  					            Object o){
		if (((VCinfo)o).pass == 1) {

			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						c.visit(this, o);
					}
				}
			}
		}
        return null;
	}


	public Object visitLOCATION(ASTlocation n, Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
	}

	public Object visitLOCATED_VAR_SPEC_INIT(ASTlocated_var_spec_init n,
				  					            Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
		return null;
	}


    public Object visitSIMPLE_INSTR_LIST(ASTsimple_instr_list n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitIL_SIMPLE_INSTRUCTION(ASTil_simple_instruction n,
											 Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitIL_SIMPLE_OPERATION(ASTil_simple_operation n,Object o) {

        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitIL_EXPRESSION(ASTil_expression n, Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitIL_JUMP_OPERATION(ASTil_jump_operation n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitIL_JUMP_OPERATOR(ASTil_jump_operator n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitNUMERIC_LITERAL(ASTnumeric_literal n, Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitBOOLEAN_LITERAL(ASTboolean_literal n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }


    public Object visitVARIABLE_NAME(ASTvariable_name n, Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitELEMENTARY_TYPE_NAME(ASTelementary_type_name n,
											Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

	public Object visitSIMPLE_TYPE_NAME(ASTsimple_type_name n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
	}


    public Object visitPROGRAM_TYPE_NAME(ASTprogram_type_name n, Object o){
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

    public Object visitDERIVED_FUNCTION_BLOCK_NAME(ASTderived_function_block_name n,
										 Object o){
		return null;
	}








    /*This part takes care of some variable declarations*/
    public Object visitOTHER_VAR_DECLARATIONS(ASTother_var_declarations n,Object o) {
		if (((VCinfo)o).pass == 1) {
			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						c.visit(this, o);
					}
				}
			}
		}
        return null;

    }

    public Object visitVAR_DECLARATIONS(ASTvar_declarations n, Object o) {
		if (((VCinfo)o).pass == 1) {
			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						c.visit(this, o);
					}
				}
			}
		}
        return null;

    }

    public Object visitVAR_INIT_DECL(ASTvar_init_decl n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

    }



    public Object visitSIMPLE_SPECIFICATION(ASTsimple_specification n,
											Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

    }

	public Object visitLABEL(ASTlabel n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}

	public Object visitCHARACTER_STRING(ASTcharacter_string n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}

	public Object visitIL_FB_CALL(ASTil_fb_call n, Object o) {
        Node[] children = n.getChildren();
		Node[] params   = null;
		Node[] p2 = null;

		if (children != null) {
			int childNum = children.length;
			int i;

			for (i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}

		}
        return null;

	}

	public Object visitIL_CALL_OPERATOR(ASTil_call_operator n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}

    /*
	public Object visitIL_CALL_JAVA_OPERATOR(ASTil_call_java_operator n,
											 Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}
    */

	public Object visitIL_OPERAND_LIST(ASTil_operand_list n, Object o) {
		LinkedList operands;
		int i;
		int pos;
		Node parent;
		Node greatParent;
		ASTil_simple_operation jjtn_simple_op;
		Operand operand;
		String fb_name;
		String fb_type;
		Hashtable fbVariables;
		Enumeration e;
		String functionBlockName;
		Variable v;

		if (((VCinfo)o).blockType=="program") {
			functionBlockName = null;
		} else {
			functionBlockName = ((VCinfo)o).functionBlockName;
		}


		operands = n.getOperands();

		parent = n.jjtGetParent();

		/* Skapa referens till function_block_body */
		greatParent = parent.jjtGetParent();

		fb_name = ((ASTfb_name)(((ASTil_fb_call)parent).getChildren())[1]).getName();


		fb_type = getFunctionBlockTypeName(fb_name, functionBlockName);


		/* il_fb_call's index i function_block_body's barn
           (il_fb_call är il_param_list's förälder)          */
		pos = ((VCinfo)o).childIndex;

		if (fb_type != null) {
			fbVariables = (Hashtable)functionBlocks.get(fb_type);

			for (i = 0; i < operands.size(); i++) {
				operand = (Operand)operands.get(i);

				/*
				 * LD
				 */
				if (((VCinfo)o).blockType=="program") {
					jjtn_simple_op = create_LD_Node(operand.type, operand.value, null);
				} else {
					jjtn_simple_op = create_LD_Node(operand.type, operand.value, ((VCinfo)o).functionBlockName);
				}

				greatParent.jjtInsertChild(jjtn_simple_op, pos);

				((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
				pos = pos + 1;



				/*
				 * ST
				 */
				if (((VCinfo)o).blockType=="program") {
					jjtn_simple_op = create_ST_Node(fb_name + "." + (String)fbVariables.get(""+i), null);
				} else {
					jjtn_simple_op = create_ST_Node(fb_name + "." + (String)fbVariables.get(""+i), ((VCinfo)o).functionBlockName);
				}

				greatParent.jjtInsertChild(jjtn_simple_op, pos);

				((VCinfo)o).childIndex = ((VCinfo)o).childIndex + 1;
				pos = pos + 1;

			}

			/* Ta bort noden il_operand_list
			 */
			parent.jjtDeleteChild(2);
		}

        return o;

	}

	public Object visitSIMPLE_SPEC_INIT(ASTsimple_spec_init n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}


    public Object visitFIELD_SELECTOR(ASTfield_selector n,
											 Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
    }

	//tillägg av Anders 2002-03-21 för att kunna implementa SimpleNodeVisitor
	public Object visitINPUT_OUTPUT_DECLARATIONS(
				 			 ASTinput_output_declarations n ,Object o) {
		if (((VCinfo)o).pass == 1) {
			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						c.visit(this, o);
					}
				}
			}
		}
		return o;
	}

	public Object visitIO_VAR_DECLARATIONS(ASTio_var_declarations n,
										   Object o) {
		if (((VCinfo)o).pass == 1) {
			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						c.visit(this, o);
					}
				}
			}
		}
        return o;
	}


	public Object visitTEMP_VAR_DECL(ASTtemp_var_decl n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return o;
	}


	public Object visitVAR_DECLARATION(ASTvar_declaration n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return o;

	}


	public Object visitSTRUCTURED_VARIABLE(ASTstructured_variable n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;

	}


	public Object visitMULTI_ELEMENT_VARIABLE(ASTmulti_element_variable n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
	}


	public Object visitRECORD_VARIABLE(ASTrecord_variable n, Object o) {
        Node[] children = n.getChildren();
		if (children != null) {
			for (int i=0; i < children.length; i++) {
				SimpleNode c = (SimpleNode)children[i];
				if (c != null) {
					c.visit(this, o);
				}
			}
		}
        return null;
	}


	// skall troligen tas bort när vi är färdiga
	public Object visitStandard(SimpleNode n, Object o){
		System.out.println(n.toString() + "WARNING: visitStandard");
		return null;
	}


	private class VCinfo{
		public String  blockType;
		public String  functionBlockName;
		public int     pass;
		public int     childIndex;

		public VCinfo() {
			pass = 1;
		}

	}

	private class Variable{
		public String name;
		public String typeName;
		public boolean directVariable;
		public String fieldSelector;
		public String fieldSelectorTypeName;
		public boolean isFunctionBlock;
	}


}
