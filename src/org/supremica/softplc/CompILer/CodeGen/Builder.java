package org.supremica.softplc.CompILer.CodeGen;
import org.supremica.softplc.CompILer.CodeGen.Datatypes.*;
import org.supremica.softplc.CompILer.CodeGen.Constants.*;
import de.fub.bytecode.generic.*;
public interface Builder{

    public void emitIL_SIMPLE_OPERATION(String operator, Object arg);
    public void emitIL_EXPRESSION(String operator, TypeConstant t, Object arg);

	public void emitStackSpace(int size);
	public void emitLoad(Object arg);

    public void dumpCode();

	public void emitVarField(String varName, Object type,
							 boolean global, boolean inputOutputVar);
	public void emitDirectInit(IECDirectVariable v, TypeBOOL i);

	public BranchInstruction emitIL_FB_CALL_Start(IlCallOperator op);
	public void emitIL_FB_CALL_End(BranchInstruction callCondition);
	public void emitIL_FB_CALL_Run(String fbName, String fbTypeName);
	public void emitIL_FB_CALL_SetInputs();
	public void emitIL_FB_CALL_SetOutputs();

	//il_fb_call är en hackfunktion
	public void emitIL_FB_CALL(IlCallOperator op,String fb_name,Object[] args);
    public void emitLABEL(String label);
    public void emitIL_JUMP_OPERATION(IlJumpOperator op, String targetLabel);

}
