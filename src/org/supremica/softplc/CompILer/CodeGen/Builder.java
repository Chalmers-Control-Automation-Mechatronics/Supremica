package org.supremica.softplc.CompILer.CodeGen;

import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import de.fub.bytecode.generic.*;

public interface Builder
{
	void emitIL_SIMPLE_OPERATION(String operator, Object arg);

	void emitIL_EXPRESSION(String operator, TypeConstant t, Object arg);

	void emitStackSpace(int size);

	void emitLoad(Object arg);

	void dumpCode();

	void emitVarField(String varName, Object type, boolean global, boolean inputOutputVar);

	void emitDirectInit(IECDirectVariable v, TypeBOOL i);

	BranchInstruction emitIL_FB_CALL_Start(IlCallOperator op);

	void emitIL_FB_CALL_End(BranchInstruction callCondition);

	void emitIL_FB_CALL_Run(String fbName, String fbTypeName);
    
	void emitIL_FB_CALL_SetInputs();

	void emitIL_FB_CALL_SetOutputs();

	// il_fb_call är en hackfunktion
	void emitIL_FB_CALL(IlCallOperator op, String fb_name, Object[] args);

	void emitLABEL(String label);

	void emitIL_JUMP_OPERATION(IlJumpOperator op, String targetLabel);
}
