package org.supremica.softplc.CompILer.Checker;
//import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
//import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import org.supremica.softplc.CompILer.Parser.*;
import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import java.util.*;
import java.lang.Exception;
import java.util.LinkedList;

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

	//	List declaredDataTypes = new LinkedList();
	Hashtable locatedVariables;
	Hashtable symbolicVariables;
	Hashtable functionBlocks;
	VCinfo    VC;
	boolean success = true;

	SimpleNode abstractSyntaxTreeRoot;

    public VariableChecker(SimpleNode abstractSyntaxTreeRoot){
		this.abstractSyntaxTreeRoot = abstractSyntaxTreeRoot;
    }


	public boolean check() {
		locatedVariables  = new Hashtable();
		symbolicVariables = new Hashtable();
		functionBlocks    = new Hashtable();
		VC                = new VCinfo();

        Node[] children;

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
		//				return success;
		return true;

	}



	//skall troligen tas bort när vi är färdiga
	public Object visitStandard(SimpleNode n, Object o){
		System.out.println(n.toString() + "WARNING: visitStandard");
		return null;
	}

	public Object visitIL_PARAM_LIST(ASTil_param_list n, Object o){
		LinkedList inParameters;
		LinkedList outParameters;
		int i;
		int pos = 2;
		Node parent;
		Node greatParent;
		ASTil_simple_operation jjtn_simple_op;
		ASTnumeric_literal jjtn_numeric_literal;
		ASTboolean_literal jjtn_boolean_literal;
		ASTcharacter_string jjtn_character_string;
		ASTvariable jjtn_variable;
		Param p;
		String fb_name;
		String fb_type;
		String fieldSelectorType = null;
		String outParameterType = null;
		String inParameterType = null;

		inParameters = n.getInParameters();
		outParameters = n.getOutParameters();

		parent = n.jjtGetParent();
		greatParent = parent.jjtGetParent();
		fb_name = ((ASTfb_name)(((ASTil_fb_call)parent).getChildren())[1]).getName();

		pos = ((VCinfo)o).childIndex;

		if (inParameters != null) {
			for (i = 0; i < inParameters.size(); i++)
				{
					System.out.println("*** PARAM: " + ((Param)inParameters.get(i)).parameter + " VALUE=" + ((Param)inParameters.get(i)).value + " TYPE=" + ((Param)inParameters.get(i)).parameterType);

					p = (Param)inParameters.get(i);

					/*
                     * LD
                     */
					jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
					jjtn_simple_op.setName("LD");
					greatParent.jjtInsertChild(jjtn_simple_op, pos);
					pos = pos + 1;

					if (p.parameterType == "numeric_literal") {
						jjtn_numeric_literal = new ASTnumeric_literal(parserTreeConstants.JJTNUMERIC_LITERAL);
						jjtn_numeric_literal.setName(p.value);
						jjtn_simple_op.jjtAddChild(jjtn_numeric_literal, 0);
					} else if (p.parameterType == "character_string") {
						jjtn_character_string = new ASTcharacter_string(parserTreeConstants.JJTCHARACTER_STRING);
						jjtn_character_string.setName(p.value);
						jjtn_simple_op.jjtAddChild(jjtn_character_string, 0);
					} else if (p.parameterType == "boolean_literal") {
						jjtn_boolean_literal = new ASTboolean_literal(parserTreeConstants.JJTBOOLEAN_LITERAL);
						jjtn_boolean_literal.setName(p.value);
						jjtn_simple_op.jjtAddChild(jjtn_boolean_literal, 0);
					} else if (p.parameterType == "symbolic_variable") {
						if (((VCinfo)o).blockType == "program")
							{
								inParameterType = (String)symbolicVariables.get(p.value);
							}
						else
							{
								//								Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_name);
								//								fb_type = (String)fbVariables.get(p.parameter);
							}



						/*
						 * Om variabeln är ett funktionsblock
						 * ska detta anges i variabel-noden, och
						 * fieldselector-typen ska sättas.
						 */
						if (functionBlocks.containsKey(p.value))
							{

								//								Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_type);
								//								fieldSelectorType = ((String)fbVariables.get(p.parameter));
							}



						jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
						jjtn_variable.setName(p.value);
						jjtn_variable.setTypeName(inParameterType);

						/*
						jjtn_variable.setIsFunctionBlock(true);
						jjtn_variable.setFieldSelector(p.parameter);
						jjtn_variable.setFieldSelectorTypeName(fieldSelectorType);
						*/


						jjtn_simple_op.jjtAddChild(jjtn_variable, 0);
					}


					/*
                     * ST
                     */
					if (((VCinfo)o).blockType == "program")
						{
							fb_type = (String)symbolicVariables.get(fb_name);
						}
					else
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_name);
							fb_type = (String)fbVariables.get(p.parameter);
						}



					/*
					 * Om variabeln är en located variable
					 * ska location sättas in i variabel-noden
					 */
					if (locatedVariables.containsKey(p.value))
						{
							//n.setIsDirectVariable(true);
							//n.setName((String)locatedVariables.get(p.value));
						}


					/*
					 * Om variabeln är ett funktionsblock
					 * ska detta anges i variabel-noden, och
					 * fieldselector-typen ska sättas.
					 */
					else if (functionBlocks.containsKey(fb_type))
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_type);
							fieldSelectorType = ((String)fbVariables.get(p.parameter));
						}


					jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
					jjtn_simple_op.setName("ST");
					greatParent.jjtInsertChild(jjtn_simple_op, pos);
					pos = pos + 1;

					jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
					jjtn_variable.setName(fb_name);
					jjtn_variable.setTypeName(fb_type);
					jjtn_variable.setIsFunctionBlock(true);
					jjtn_variable.setFieldSelector(p.parameter);
					jjtn_variable.setFieldSelectorTypeName(fieldSelectorType);

					jjtn_simple_op.jjtAddChild(jjtn_variable, 0);


				}
		}

		parent.jjtDeleteChild(2);
		pos = pos + 1;

		if (outParameters != null) {
			for (i = 0; i < outParameters.size(); i++)
				{
					System.out.println("*** PARAM: " + ((Param)outParameters.get(i)).parameter + " VALUE=" + ((Param)outParameters.get(i)).value);


					p = (Param)outParameters.get(i);

					/*
                     * LD
                     */
					jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
					jjtn_simple_op.setName("LD");
					greatParent.jjtInsertChild(jjtn_simple_op, pos);
					pos = pos + 1;


					if (((VCinfo)o).blockType == "program")
						{
							fb_type = (String)symbolicVariables.get(fb_name);
						}
					else
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_name);
							fb_type = (String)fbVariables.get(p.parameter);
						}



					/*
					 * Om variabeln är ett funktionsblock
					 * ska detta anges i variabel-noden, och
					 * fieldselector-typen ska sättas.
					 */
					if (functionBlocks.containsKey(fb_type))
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_type);
							fieldSelectorType = ((String)fbVariables.get(p.parameter));
						}



					jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
					jjtn_variable.setName(fb_name);
					jjtn_variable.setTypeName(fb_type);
					jjtn_variable.setIsFunctionBlock(true);
					jjtn_variable.setFieldSelector(p.parameter);
					jjtn_variable.setFieldSelectorTypeName(fieldSelectorType);

					jjtn_simple_op.jjtAddChild(jjtn_variable, 0);




					/*
                     * ST
                     */


					if (((VCinfo)o).blockType == "program")
						{
							fb_type = (String)symbolicVariables.get(fb_name);
						}
					else
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_name);
							fb_type = (String)fbVariables.get(p.value);
						}



					/*
					 * Om variabeln är ett funktionsblock
					 * ska detta anges i variabel-noden, och
					 * fieldselector-typen ska sättas.
					 */

					if (functionBlocks.containsKey(fb_type))
						{
							Hashtable fbVariables = (Hashtable)functionBlocks.get(fb_type);
							fieldSelectorType = ((String)fbVariables.get(p.value));
						}


					jjtn_simple_op = new ASTil_simple_operation(parserTreeConstants.JJTIL_SIMPLE_OPERATION);
					jjtn_simple_op.setName("ST");
					greatParent.jjtInsertChild(jjtn_simple_op, pos);
					pos = pos + 1;



					if (((VCinfo)o).blockType == "program")
						{
							outParameterType = (String)symbolicVariables.get(p.value);
						}
					else
						{
							//							Hashtable fbVariables = (Hashtable)functionBlocks.get(p.value);
							//							fb_type = (String)fbVariables.get(p.parameter);
						}

					System.out.println("### OutParameterType=" + outParameterType);

					jjtn_variable = new ASTvariable(parserTreeConstants.JJTVARIABLE);
					jjtn_variable.setName(p.value);
					jjtn_variable.setTypeName(outParameterType);

					/*
					jjtn_variable.setIsFunctionBlock(true);
					jjtn_variable.setFieldSelector(p.parameter);
					jjtn_variable.setFieldSelectorTypeName(fieldSelectorType);
					*/


					jjtn_simple_op.jjtAddChild(jjtn_variable, 0);


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



		//varför ha denna och inte
		// names = ((ASTvar1_list)n.getChildren()[0]).getNames();
		//och därmed slippa att ha en array med alla namnen i
		// ASTvar1_init_decl, ASTvar1_list och ASTvar1_declaration
		names = n.getNames();

		if (((VCinfo)o).blockType == "program") {
			for(i=0; i<names.length; i++){
				symbolicVariables.put(names[i], n.getTypeName());
			}
		} else if (((VCinfo)o).blockType == "functionBlock") {
			String fbName = ((VCinfo)o).functionBlockName;

			tmpHash = (Hashtable)functionBlocks.get(fbName);

			// Lägg in variablerna i hashtabellen
			for(i=0; i<names.length; i++) {
				tmpHash.put(names[i], n.getTypeName());
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


	public Object visitVARIABLE(ASTvariable n, Object o) throws Exception{
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
						throw new Exception("Undeclared variable: " + n.getName());
					}
				}
			else if (((VCinfo)o).blockType == "functionBlock")
				{
					Hashtable fbVariables = (Hashtable)functionBlocks.get(((VCinfo)o).functionBlockName);
					if (!fbVariables.containsKey(n.getName())) {
						success = false;
						System.err.println("Error: Undeclared variable: " + n.getName());
						throw new Exception("Undeclared variable: " + n.getName());
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
					n.setFieldSelectorTypeName((String)fbVariables.get(n.getFieldSelector()));
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

			Node[] children = n.getChildren();
			if (children != null) {
				for (int i=0; i < children.length; i++) {
					SimpleNode c = (SimpleNode)children[i];
					if (c != null) {
						((VCinfo)o).childIndex = i;
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

			/*
			if (childNum > 2) {
				if (((SimpleNode)children[2]).toString() == "il_param_list") {
					params = ((SimpleNode)children[2]).getChildren();
					for (i=0; i<params.length; i++) {
						p2 = ((SimpleNode)params[i]).getChildren();
						System.out.println("***IL_FB_CALL: " + ((SimpleNode)p2[0]).toString());
					}
				}
			}
			*/
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


	public Object visitIL_OPERAND_LIST(ASTil_operand_list n, Object o) {
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


	private class VCinfo{
		public String  blockType;
		public String  functionBlockName;
		public int     pass;
		public int     childIndex;

		public VCinfo() {
			pass = 1;
		}

	}

}
