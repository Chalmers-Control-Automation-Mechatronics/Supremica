package org.supremica.softplc.CompILer.Parser;

import org.supremica.softplc.CompILer.Parser.SyntaxTree.*;
import java.lang.Exception;

public interface SimpleNodeVisitor
{
	Object visitStandard(SimpleNode n, Object o);

	Object visitBOOLEAN_LITERAL(ASTboolean_literal n, Object o);

	Object visitCHARACTER_STRING(ASTcharacter_string n, Object o);

	Object visitDERIVED_FUNCTION_BLOCK_NAME(ASTderived_function_block_name n, Object o);

	Object visitELEMENTARY_TYPE_NAME(ASTelementary_type_name n, Object o);

	Object visitFB_NAME(ASTfb_name n, Object o);

	Object visitFIELD_SELECTOR(ASTfield_selector n, Object o);

	Object visitFUNCTION_BLOCK_BODY(ASTfunction_block_body n, Object o);

	Object visitFUNCTION_BLOCK_DECLARATION(ASTfunction_block_declaration n, Object o);

	Object visitIL_CALL_OPERATOR(ASTil_call_operator n, Object o);

	Object visitIL_EXPRESSION(ASTil_expression n, Object o);

	Object visitIL_FB_CALL(ASTil_fb_call n, Object o);

	Object visitIL_JUMP_OPERATION(ASTil_jump_operation n, Object o);

	Object visitIL_JUMP_OPERATOR(ASTil_jump_operator n, Object o);

	Object visitIL_OPERAND_LIST(ASTil_operand_list n, Object o);

	Object visitIL_PARAM_LIST(ASTil_param_list n, Object o);

	Object visitIL_SIMPLE_INSTRUCTION(ASTil_simple_instruction n, Object o);

	Object visitIL_SIMPLE_OPERATION(ASTil_simple_operation n, Object o);

	Object visitINPUT_OUTPUT_DECLARATIONS(ASTinput_output_declarations n, Object o);

	Object visitIO_VAR_DECLARATIONS(ASTio_var_declarations n, Object o);

	Object visitJAVA_BLOCK_DECLARATION(ASTjava_block_declaration n, Object o);

	Object visitLABEL(ASTlabel n, Object o);

	Object visitLOCATED_VAR_DECL(ASTlocated_var_decl n, Object o);

	Object visitLOCATED_VAR_DECLARATIONS(ASTlocated_var_declarations n, Object o);

	Object visitLOCATED_VAR_SPEC_INIT(ASTlocated_var_spec_init n, Object o);

	Object visitLOCATION(ASTlocation n, Object o);

	Object visitMULTI_ELEMENT_VARIABLE(ASTmulti_element_variable n, Object o);

	Object visitNUMERIC_LITERAL(ASTnumeric_literal n, Object o);

	Object visitOTHER_VAR_DECLARATIONS(ASTother_var_declarations n, Object o);

	Object visitPROGRAM_DECLARATION(ASTprogram_declaration n, Object o);

	Object visitPROGRAM_TYPE_NAME(ASTprogram_type_name n, Object o);

	Object visitRECORD_VARIABLE(ASTrecord_variable n, Object o);

	Object visitSIMPLE_INSTR_LIST(ASTsimple_instr_list n, Object o);

	Object visitSIMPLE_SPEC_INIT(ASTsimple_spec_init n, Object o);

	Object visitSIMPLE_SPECIFICATION(ASTsimple_specification n, Object o);

	Object visitSIMPLE_TYPE_NAME(ASTsimple_type_name n, Object o);

	Object visitSTRUCTURED_VARIABLE(ASTstructured_variable n, Object o);

	Object visitTEMP_VAR_DECL(ASTtemp_var_decl n, Object o);

	Object visitVAR_DECLARATIONS(ASTvar_declarations n, Object o);

	Object visitVAR_DECLARATION(ASTvar_declaration n, Object o);

	Object visitVAR_INIT_DECL(ASTvar_init_decl n, Object o);

	Object visitVAR1_DECLARATION(ASTvar1_declaration n, Object o);

	Object visitVAR1_INIT_DECL(ASTvar1_init_decl n, Object o);

	Object visitVAR1_LIST(ASTvar1_list n, Object o);

	Object visitVARIABLE(ASTvariable n, Object o)
		throws Exception;

	Object visitVARIABLE_NAME(ASTvariable_name n, Object o);
}
